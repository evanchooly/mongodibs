package com.mongodb.dibs;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
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
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DibsResource {
    private static final String OK_RESPONSE = "{\"ok\": 1}";

    private final Datastore ds;
    private DibsConfiguration configuration;
    private AmazonSimpleEmailServiceClient sesClient;
    private JacksonMapper mapper = new JacksonMapper();

    public DibsResource(final DibsConfiguration configuration, final Datastore ds) {
        this.ds = ds;

        if (configuration != null) {
            this.configuration = configuration;

            if (configuration.getAwsCredentials().getAccessKey() != null &&
                configuration.getAwsCredentials().getSecretKey() != null) {
                final BasicAWSCredentials creds = new BasicAWSCredentials(
                    configuration.getAwsCredentials().getAccessKey(),
                    configuration.getAwsCredentials().getSecretKey());
                final ClientConfiguration awsConf = new ClientConfiguration();
                awsConf.setConnectionTimeout(30000);
                awsConf.setMaxConnections(200);
                awsConf.setMaxErrorRetry(2);
                awsConf.setSocketTimeout(30000);
                this.sesClient = new AmazonSimpleEmailServiceClient(creds, awsConf);
            }
        }
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
        Query<Order> query = ds.createQuery(Order.class)
                               .filter("group", groupOrder)
                               .field("expectedAt").greaterThanOrEq(dateTime.toDate())
                               .field("expectedAt").lessThan(next.toDate());
        return groupOrder ? findGroupOrders(query) : findSingleOrders(query);
    }

    @POST
    @Path("/notify/vendor/")
    @Produces(MediaType.APPLICATION_JSON)
    public String notifyGroup(final String dateString, final String vendor) throws ParseException {
        final DateTime dateTime = DateTime.parse(dateString, DateTimeFormat.forPattern("yyyy-MM-dd"));
        final DateTime next = dateTime.plusDays(1);
        final Query<Order> query = ds.createQuery(Order.class)
                                     .filter("vendor", vendor)
                                     .field("expectedAt").greaterThanOrEq(dateTime)
                                     .field("expectedAt").lessThan(next);
        for (final Order o : query.fetch()) {
            if (o.getClaimedBy() != null) {
                notifyDelivery(o.getClaimedBy(), o);
            } else if (o.getOrderedBy() != null) {
                notifyDelivery(o.getOrderedBy(), o);
            }

            o.setDeliveredAt(new Date());
        }

        return OK_RESPONSE;
    }

    @POST
    @Path("/notify/order")
    @Produces(MediaType.APPLICATION_JSON)
    public String notifyOrder(final String orderId) throws ParseException {
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
    @Consumes(MediaType.TEXT_PLAIN)
    public String claim(final String orderId) {
        final Order order = ds.createQuery(Order.class)
                              .filter("_id", new ObjectId(orderId)).get();

        if (order != null) {
            if (order.getUpForGrabs() && order.getClaimedBy() != null) {
                order.setClaimedBy("" /* FIXME */);
                notifyClaim(order.getClaimedBy(), order);
                ds.save(order);
            } else {
                // TODO throw exception for order being claimed or not being up for grabs
            }
        }

        return OK_RESPONSE;
    }

    private void notifyDelivery(final String email, final Order order) {
        notify(email, order.getVendor() + " delivered. (EOM)");
    }

    private void notifyClaim(final String email, final Order order) {
        notify(email, "You have claimed the order for " + order.getOrderedBy() + ". (EOM)");
    }

    private void notify(final String email, final String subject) {
        final SendEmailRequest request = new SendEmailRequest();
        request.setDestination(new Destination(Collections.singletonList(email)));
        request.setSource("donotreply@10gen.com");
        final Message message = new Message();
        message.withSubject(new Content().withData(subject));
        request.setMessage(message);

        if (sesClient != null) {
            sesClient.sendEmail(request);
        }
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
