package com.mongodb.dibs;

import com.fasterxml.jackson.core.JsonFactory;
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

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Unit test for simple DibsApplication.
 */
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
            DibsResource resource = new DibsResource(datastore);
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
        WebResource resource = resources.client().resource("/orders/2014-07-10/group");
        String response = resource.get(String.class);
        JsonNode json = parseResponse(response);
        Assert.assertEquals(5, json.size());
        for (JsonNode jsonNode : json) {
            Assert.assertEquals(20, jsonNode.size());
        }
    }

    private JsonNode parseResponse(final String response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        return mapper.readTree(factory.createParser(response));
    }

    @Test
    public void testSingleOrders() throws IOException {
        collection.remove(new BasicDBObject());
        for (int i = 0; i < 10; i++) {
            createOrder(i, "Awesome Vendor", false);
        }
        WebResource resource = resources.client().resource("/orders/2014-07-10/single");
        String response = resource.get(String.class);
        JsonNode json = parseResponse(response);
        Assert.assertEquals(10, json.size());
    }

    private void createOrder(final int count, final String vendor, final boolean group) {
        Order order = new Order();
        order.setVendor(vendor);
        order.setGroup(group);
        order.setOrderDate(new DateTime(2014, 7, 10, 11, 45).toDate());
        order.setContents("yum " + count);
        datastore.save(order);
    }
}
