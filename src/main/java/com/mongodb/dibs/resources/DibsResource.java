package com.mongodb.dibs.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.mongodb.dibs.Dibs;
import com.mongodb.dibs.DibsConfiguration;
import com.mongodb.dibs.JacksonMapper;
import com.mongodb.dibs.Smtp;
import com.mongodb.dibs.auth.Authority;
import com.mongodb.dibs.auth.Restricted;
import com.mongodb.dibs.auth.User;
import com.mongodb.dibs.model.Order;
import io.dropwizard.views.View;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.MorphiaIterator;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DibsResource {
    private static final String OK_RESPONSE = "{\"ok\": 1}";

    private final Datastore ds;
    private DibsConfiguration configuration;
    private JacksonMapper mapper = new JacksonMapper();

    public DibsResource(final DibsConfiguration configuration, final Datastore ds) {
        this.ds = ds;
        this.configuration = configuration;
    }

    @GET
    @Produces("text/html;charset=ISO-8859-1")
    public Response index(@Restricted(Authority.ROLE_PUBLIC) User user) throws URISyntaxException {
        return Response.temporaryRedirect(new URI(user.hasAuthority(Authority.ROLE_ADMIN) ? "/notify" : "/dibs"))
                       .build();
    }

    @GET
    @Path("notify")
    @Produces("text/html;charset=ISO-8859-1")
    public View notify(@Restricted(Authority.ROLE_ADMIN) User user) {
        return new View("/notify.ftl", Charsets.ISO_8859_1) {
        };
    }

    @GET
    @Path("dibs")
    @Produces("text/html;charset=ISO-8859-1")
    public View dibs(@Restricted(Authority.ROLE_PUBLIC) User user) {
        return new View("/dibs.ftl", Charsets.ISO_8859_1) {
        };
    }

    @GET
    @Path("/orders/{date}/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public String findOrders(@Restricted(Authority.ROLE_PUBLIC) User user, @PathParam("date") String dateString,
                             @PathParam("type") String type) throws IOException, ParseException {
        DateTime dateTime = DateTime.parse(dateString, DateTimeFormat.forPattern("yyyy-MM-dd"));
        DateTime next = dateTime.plusDays(1);

        boolean groupOrder = type.equalsIgnoreCase("group");
        boolean upForGrabs = type.equalsIgnoreCase("upForGrabs");
        Query<Order> query = ds.createQuery(Order.class)
                               .filter("upForGrabs", upForGrabs)
                               .field("deliveredAt").doesNotExist()
                               .field("expectedAt").greaterThanOrEq(dateTime.toDate())
                               .field("expectedAt").lessThan(next.toDate());
        if (!upForGrabs) {
            query.filter("group", groupOrder);
        }
        return groupOrder ? findGroupOrders(query) : (upForGrabs ? findUpForGrabs(query) : findSingleOrders(query));
    }

    @POST
    @Path("/notify/{date}/vendor/")
    @Produces(MediaType.APPLICATION_JSON)
    public String notifyGroup(@Restricted(Authority.ROLE_ADMIN) User user, @PathParam("date") final String dateString,
                              final String vendor) throws ParseException {
        final DateTime dateTime = DateTime.parse(dateString, DateTimeFormat.forPattern("yyyy-MM-dd"));
        final DateTime next = dateTime.plusDays(1);
        final Query<Order> query = ds.createQuery(Order.class)
                                     .filter("vendor", vendor)
                                     .field("expectedAt").greaterThanOrEq(dateTime.toDate())
                                     .field("expectedAt").lessThan(next.toDate());
        try {
            for (final Order o : query.fetch()) {
                if (o.getClaimedBy() != null) {
                    notifyDelivery(o.getClaimedBy(), o);
                } else if (o.getOrderedBy() != null) {
                    notifyDelivery(o.getOrderedBy(), o);
                }

                o.setDeliveredAt(new Date());
                ds.save(o);
            }
        } catch (EmailException e) {
            notifyAdmin(e);
        }

        return OK_RESPONSE;
    }

    @POST
    @Path("/notify/order")
    @Produces(MediaType.APPLICATION_JSON)
    public String notifyOrder(@Restricted(Authority.ROLE_ADMIN) User user, final String orderId) throws ParseException, EmailException {
        final Order order = ds.createQuery(Order.class)
                              .filter("_id", new ObjectId(orderId)).get();

        if (order != null) {
            if (order.getClaimedBy() != null) {
                notifyDelivery(order.getClaimedBy(), order);
            } else if (order.getOrderedBy() != null) {
                notifyDelivery(order.getOrderedBy(), order);
            }

            order.setDeliveredAt(new Date());
            ds.save(order);
        }

        return OK_RESPONSE;
    }

    @POST
    @Path("/claim")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String claim(@Restricted(Authority.ROLE_PUBLIC) User user, final JsonNode node) throws JsonProcessingException {
        String orderId = node.get("id").textValue();
        String claimant = user.getEmail();
        Query<Order> filter = ds.createQuery(Order.class)
                                .filter("id", new ObjectId(orderId));
        filter.or(
                     filter.and(filter.criteria("upForGrabs").equal(Boolean.TRUE),
                                filter.criteria("claimedBy").doesNotExist()),
                     filter.criteria("claimedBy").equal(claimant)
                 );

        UpdateOperations<Order> updates = ds.createUpdateOperations(Order.class);
        updates.set("claimedBy", claimant);
        updates.set("claimedDate", new Date());

        Order order = ds.findAndModify(filter, updates);


        if (order != null) {
            try {
                notifyClaim(order);
            } catch (EmailException e) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("ok", 0);
                response.put("error", Dibs.error());
                e.printStackTrace();
                mapper.writeValueAsString(response);
            }
        } else {
            order = ds.createQuery(Order.class)
                      .filter("id", new ObjectId(orderId))
                      .get();
        }

        return mapper.writeValueAsString(order);
    }

    private void notifyDelivery(final String email, final Order order) throws EmailException {
        notify(email, Dibs.orderDelivered(order.getVendor()));
    }

    private void notifyClaim(final Order order) throws EmailException {
        notify(order.getClaimedBy(), Dibs.claimSuccessful(order.getOrderedBy()));
        notify(order.getOrderedBy(), Dibs.orderClaimed(order.getClaimedBy()));
    }

    private void notifyAdmin(final EmailException e) {
        try {
            Email email = new SimpleEmail();
            email.setHostName(Smtp.smtpHost());
            email.setSmtpPort(Integer.parseInt(Smtp.smtpPort()));
            email.setAuthenticator(new DefaultAuthenticator(Smtp.notificationsEmailAddress(), Smtp.notificationsEmailPassword()));
            email.setSSLOnConnect(true);
            email.setFrom(Smtp.adminEmailAddress());
            email.addTo(Smtp.adminEmailAddress());
            email.setSubject("MongoDiBs failure");

            StringWriter out = new StringWriter();
            e.printStackTrace(new PrintWriter(out));

            email.setMsg(out.toString());
            email.send();
        } catch (EmailException e1) {
            e1.printStackTrace();
        }
    }

    private void notify(final String emailAddress, final String subject) throws EmailException {
        Email email = new SimpleEmail();
        email.setHostName(Smtp.smtpHost());
        email.setSmtpPort(Integer.parseInt(Smtp.smtpPort()));
        email.setAuthenticator(new DefaultAuthenticator(Smtp.notificationsEmailAddress(), Smtp.notificationsEmailPassword()));
        email.setSSLOnConnect(true);
        email.setFrom(Smtp.notificationsEmailAddress(), "MongoDiBs");
        email.addTo(emailAddress);
        email.setSubject(subject);
        email.send();
    }

    private String findSingleOrders(final Query<Order> query) throws JsonProcessingException {
        MorphiaIterator<Order, Order> iterator = query
                                                     .field("group").equal(false)
                                                     .order("orderedBy")
                                                     .fetch();
        List<Order> orders = new ArrayList<>();
        try {
            for (Order order : iterator) {
                orders.add(order);
            }
        } finally {
            iterator.close();
        }
        return mapper.writeValueAsString(orders);
    }

    private String findUpForGrabs(final Query<Order> query) throws JsonProcessingException {
        MorphiaIterator<Order, Order> iterator = query
                                                     .field("claimedBy").doesNotExist()
                                                     .order("orderedBy")
                                                     .fetch();
        List<Order> orders = new ArrayList<>();
        try {
            for (Order order : iterator) {
                orders.add(order);
            }
        } finally {
            iterator.close();
        }
        return mapper.writeValueAsString(orders);
    }

    private String findGroupOrders(final Query<Order> query) throws JsonProcessingException {
        MorphiaIterator<Order, Order> iterator = query
                                                     .field("group").equal(true)
                                                     .field("upForGrabs").equal(false)
                                                     .fetch();
        Set<String> vendors = new TreeSet<>();
        try {
            for (Order order : iterator) {
                vendors.add(order.getVendor());
            }
        } finally {
            iterator.close();
        }
        return mapper.writeValueAsString(vendors);
    }
}
