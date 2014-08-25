package com.mongodb.dibs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
    public View index() {
        return new View("/index.ftl", Charsets.ISO_8859_1) {
        };
    }

    @GET
    @Path("dibs")
    @Produces("text/html;charset=ISO-8859-1")
    public View dibs() {
        return new View("/dibs.ftl", Charsets.ISO_8859_1) {
        };
    }

    @GET
    @Path("/orders/{date}/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public String findOrders(@PathParam("date") String dateString, @PathParam("type") String type) throws IOException, ParseException {
        DateTime dateTime = DateTime.parse(dateString, DateTimeFormat.forPattern("yyyy-MM-dd"));
        DateTime next = dateTime.plusDays(1);

        boolean groupOrder = type.equalsIgnoreCase("group");
        boolean upForGrabs = type.equalsIgnoreCase("upForGrabs");
        Query<Order> query = ds.createQuery(Order.class)
                               .filter("group", groupOrder)
                               .field("expectedAt").greaterThanOrEq(dateTime.toDate())
                               .field("expectedAt").lessThan(next.toDate());
        return groupOrder ? findGroupOrders(query) : (upForGrabs ? findUpForGrabs(query) : findSingleOrders(query));
    }

    @POST
    @Path("/notify/{date}/vendor/")
    @Produces(MediaType.APPLICATION_JSON)
    public String notifyGroup(@PathParam("date") final String dateString, final String vendor) throws ParseException {
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
            }
        } catch (EmailException e) {
            notifyAdmin(e);
        }

        return OK_RESPONSE;
    }

    @POST
    @Path("/notify/order")
    @Produces(MediaType.APPLICATION_JSON)
    public String notifyOrder(final String orderId) throws ParseException, EmailException {
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
    public String claim(final JsonNode node) throws JsonProcessingException {
        String orderId = node.get("orderId").textValue();
        String claimant = node.get("email").textValue();
        Query<Order> filter = ds.createQuery(Order.class)
                                .filter("id", new ObjectId(orderId));
        filter.or(
                     filter.criteria("upForGrabs").equal(Boolean.TRUE),
                     filter.criteria("claimedBy").doesNotExist(),
                     filter.criteria("claimedBy").equal(claimant)
                 );

        UpdateOperations<Order> updates = ds.createUpdateOperations(Order.class);
        updates.set("upForGrabs", Boolean.FALSE);
        updates.set("claimedBy", claimant);
        updates.set("claimedDate", new Date());

        Order order = ds.findAndModify(filter, updates);

        Map<String, Object> response = new LinkedHashMap<>();

        if (order != null) {
            try {
                notifyClaim(order);
                response.put("ok", 1);
                //                response.put("message", "You have successfully claimed this order.");
            } catch (EmailException e) {
                response.put("ok", 0);
                response.put("error", Dibs.error());
                e.printStackTrace();
            }
        } else {
            response.put("ok", 0);
            order = ds.createQuery(Order.class)
                      .filter("id", new ObjectId(orderId))
                      .get();
            response.put("claimedBy", Dibs.claimedBy(order.getClaimedBy()));
        }

        return mapper.writeValueAsString(response);
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


/*
        final SendEmailRequest request = new SendEmailRequest();
        request.setDestination(new Destination(Collections.singletonList(emailAddress)));
        request.setSource("donotreply@10gen.com");
        final Message message = new Message();
        message.withSubject(new Content().withData(subject));
        request.setMessage(message);

        if (sesClient != null) {
            sesClient.sendEmail(request);
        }
*/
    }

    private String findSingleOrders(final Query<Order> query) throws JsonProcessingException {
        MorphiaIterator<Order, Order> iterator = query
                                                     .field("group").equal(false)
                                                     .field("upForGrabs").equal(false)
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
                                                     .field("upForGrabs").equal(true)
                                                     .field("claimedBy").doesNotExist()
                                                     .order("orderedBy").fetch();
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
