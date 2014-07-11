package com.mongodb.dibs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.dibs.model.Order;
import com.sun.jersey.api.client.WebResource;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DibsResourceTest {
    @ClassRule
    public static final ResourceTestRule resources;

    private static Datastore datastore;

    private static final DBCollection collection;

    static {
        try {
            datastore = new Morphia().createDatastore(new MongoClient(), "mongo-dibs");
            datastore.ensureIndexes();
            collection = datastore.getCollection(Order.class);
            
            DibsResource resource = new DibsResource(null, datastore);
            resources = ResourceTestRule.builder()
                                        .addResource(resource)
                                        .build();
            
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGroupOrders() throws IOException {
        collection.remove(new BasicDBObject());
        for (int i = 0; i < 100; i++) {
            createOrder(i, "Vendor " + (i % 5), true);
        }
        String response = resources.client().resource("/orders/2014-07-10/group").get(String.class);
        JsonNode json = parseResponse(response);
        Assert.assertEquals(5, json.size());
    }

    private JsonNode parseResponse(final String response) throws IOException {
        return new ObjectMapper().readTree(response);
    }

    @Test
    public void testSingleOrders() throws IOException {
        collection.remove(new BasicDBObject());
        for (int i = 0; i < 10; i++) {
            createOrder(i, "Awesome Vendor", false);
        }
        String response = resources.client().resource("/orders/2014-07-10/single").get(String.class);
        JsonNode json = parseResponse(response);
        Assert.assertEquals(10, json.size());
    }

//    @Test
    public void testClaim() throws MessagingException {
        collection.remove(new BasicDBObject());
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Order order = createOrder(i, "Awesome Vendor", false);
            order.setUpForGrabs(true);
            datastore.save();
            orders.add(order);
        }
        Map<String, String> formParams=new LinkedHashMap<>();
        formParams.put("orderId", orders.get(0).getId().toString());
        formParams.put("email", "test@example.com");
        
        WebResource resource = resources.client().resource("/claim/");
        String response = resource.post(String.class, formParams);
    }

    private Order createOrder(final int count, final String vendor, final boolean group) {
        Order order = new Order();
        order.setVendor(vendor);
        order.setGroup(group);
        order.setExpectedAt(new DateTime(2014, 7, 10, 11, 45).toDate());
        order.setContents("yum " + count);
        datastore.save(order);
        return order;
    }
}
