package com.mongodb.dibs;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import com.mongodb.dibs.model.Order;
import io.dropwizard.views.View;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.MorphiaIterator;
import org.mongodb.morphia.query.Query;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Response;
import java.io.IOException;
import java.text.*;
import java.util.*;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DibsResource {
    private static final String OK_RESPONSE = "{\"ok\": 1}";

    private final Datastore ds;
    private JacksonMapper mapper = new JacksonMapper();

    public DibsResource(final Datastore ds) {
        this.ds = ds;
    }

    @GET
    @Produces("text/html;charset=ISO-8859-1")
    public View index() {
        return new View("/index.ftl", Charsets.ISO_8859_1) {
        };
    }

    @GET
    @Path("/orders/{date}/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public String findOrders(@PathParam("date") String dateString, @PathParam("type") String type) throws IOException, ParseException {
        DateTime dateTime = DateTime.parse(dateString, DateTimeFormat.forPattern("yyyy-MM-dd"));
        DateTime next = dateTime.plusDays(1);

        boolean groupOrder = type.equalsIgnoreCase("group");
        Query<Order> query = ds.createQuery(Order.class)
                               .filter("group", groupOrder)
                               .field("expectedAt").greaterThanOrEq(dateTime.toDate())
                               .field("expectedAt").lessThan(next.toDate());
        return groupOrder ? findGroupOrders(query) : findSingleOrders(query);
    }

    @POST
    @Path("/notify/{date}/vendor/{vendor}")
    @Produces(MediaType.APPLICATION_JSON)
    public String notifyGroup(
        @PathParam("date") final String dateString,
        @PathParam("vendor") final String vendor)
        throws ParseException
    {
        final DateTime dateTime = DateTime.parse(dateString, DateTimeFormat.forPattern("yyyy-MM-dd"));
        final DateTime next = dateTime.plusDays(1);
        final Query<Order> query = ds.createQuery(Order.class)
                                     .filter("vendor", vendor)
                                     .field("expectedAt").greaterThanOrEq(dateTime)
                                     .field("expectedAt").lessThan(next);
        for (final Order o : query.fetch()) {
            if (o.getClaimedBy() != null) {
                notify(o.getClaimedBy(), o);
                continue;
            }

            if (o.getOrderedBy() != null) {
                notify(o.getOrderedBy(), o);
            }
        }

        return OK_RESPONSE;
    }

    @POST
    @Path("/notify/{date}/order/{order}")
    @Produces(MediaType.APPLICATION_JSON)
    public String notifyOrder(
        @PathParam("date") final String dateString,
        @PathParam("order") final String orderId)
        throws ParseException
    {
        final DateTime dateTime = DateTime.parse(dateString, DateTimeFormat.forPattern("yyyy-MM-dd"));
        final DateTime next = dateTime.plusDays(1);
        final Query<Order> query = ds.createQuery(Order.class)
                                     .filter("_id", new ObjectId(orderId))
                                     .field("expectedAt").greaterThanOrEq(dateTime)
                                     .field("expectedAt").lessThan(next);
        final Order order = query.get();

        if (order != null) {
            if (order.getClaimedBy() != null) {
                notify(order.getClaimedBy(), order);
            } else if (order.getOrderedBy() != null) {
                notify(order.getOrderedBy(), order);
            }
        }

        return OK_RESPONSE;
    }

    @POST
    @Path("/claim/{order}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response claim() {
        return null;
    }

    private void notify(final String email, final Order order) {
        final BasicAWSCredentials creds = new BasicAWSCredentials(
            "asdf", "asdasdf");
        final ClientConfiguration awsConf = new ClientConfiguration();
        awsConf.setConnectionTimeout(30000);
        awsConf.setMaxConnections(200);
        awsConf.setMaxErrorRetry(2);
        awsConf.setSocketTimeout(30000);
        final AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(creds, awsConf);
        final SendEmailRequest request = new SendEmailRequest();
        request.setDestination(new Destination(Collections.singletonList(email)));
        request.setSource("donotreply@10gen.com");
        final Message message = new Message();
        message.withSubject(new Content().withData(order.getVendor() + " delivered. (EOM)"));
        request.setMessage(message);
        client.sendEmail(request);
    }

    private String findSingleOrders(final Query<Order> query) throws JsonProcessingException {
        MorphiaIterator<Order, Order> iterator = query.order("orderedBy").fetch();
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
        MorphiaIterator<Order, Order> iterator = query.fetch();
        Map<String, List<Order>> orders = new LinkedHashMap<>();
        try {
            for (Order order : iterator) {
                List<Order> list = orders.get(order.getVendor());
                if (list == null) {
                    list = new ArrayList<>();
                    orders.put(order.getVendor(), list);
                }
                list.add(order);
            }
        } finally {
            iterator.close();
        }
        return mapper.writeValueAsString(orders);
    }

    public static class GroupOrder {
        private String vendor;
        private List<Order> orders = new ArrayList<>();

        public void add(Order order) {
            orders.add(order);
        }
    }
}
