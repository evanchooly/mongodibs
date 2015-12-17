package com.mongodb.dibs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.dibs.auth.Restricted;
import com.mongodb.dibs.auth.User;
import com.mongodb.dibs.model.Order;
import com.mongodb.dibs.resources.DibsResource;
import com.mongodb.dibs.test.Sofia;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

public class DibsResourceTest {
    @ClassRule
    public static final ResourceTestRule resources;
    private static final DBCollection collection;
    private static Datastore datastore;

    static {
        try {
            datastore = new Morphia().createDatastore(new MongoClient(), "mongo-dibs");
            datastore.ensureIndexes();
            collection = datastore.getCollection(Order.class);

            DibsResource resource = new DibsResource(null, datastore);
            resources = ResourceTestRule.builder()
                                        .addResource(resource)
                                        .addProvider(new TestInjectableProvider())
                                        .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final JacksonMapper mapper = new JacksonMapper();

    @Test
    public void testClaim() throws MessagingException, IOException {
        List<Order> orders = generateData();
        Map<String, String> formParams = new HashMap<>();
        formParams.put("id", orders.get(0).getId().toString());
        formParams.put("email", Sofia.testEmail1());

        WebResource resource = resources.client().resource("/claim/");
        Map map = mapper.readValue(resource.type(MediaType.APPLICATION_JSON)
                                           .post(String.class, mapper.writeValueAsString(formParams)), LinkedHashMap.class);
        System.out.println("************ map = " + map);

        Assert.assertEquals(format("Should get find order\n%s", map), orders.get(0).getId().toString(), map.get("id"));
        Assert.assertEquals(format("Should be claimed by %s:\n%s", Sofia.testEmail1(), map), Sofia.testEmail1(), map.get("claimedBy"));
    }

    @Test
    public void testDoubleClaim() throws MessagingException, IOException {
        WebResource resource = resources.client().resource("/claim/");

        List<Order> orders = generateData();
        Map<String, String> formParams = new HashMap<>();
        formParams.put("id", orders.get(0).getId().toString());
        formParams.put("email", Sofia.testEmail1());

        Map map = mapper.readValue(resource.type(MediaType.APPLICATION_JSON)
                                           .post(String.class, mapper.writeValueAsString(formParams)), LinkedHashMap.class);
        Assert.assertEquals(format("Should get find order\n%s", map), orders.get(0).getId().toString(), map.get("id"));
        Assert.assertEquals(format("Should be claimed by %s:\n%s", Sofia.testEmail1(), map), Sofia.testEmail1(), map.get("claimedBy"));

        map = mapper.readValue(resource.type(MediaType.APPLICATION_JSON)
                                       .post(String.class, mapper.writeValueAsString(formParams)), LinkedHashMap.class);
        Assert.assertEquals(format("Should get find order\n%s", map), orders.get(0).getId().toString(), map.get("id"));
        Assert.assertEquals(format("Should be claimed by %s:\n%s", Sofia.testEmail1(), map), Sofia.testEmail1(), map.get("claimedBy"));

        formParams.put("email", Sofia.testEmail2());
        map = mapper.readValue(resource
                                   .type(MediaType.APPLICATION_JSON)
                                   .post(String.class, mapper.writeValueAsString(formParams)), LinkedHashMap.class);
        Assert.assertEquals(format("Should get find ok:0\n%s", map), 0, map.get("ok"));
        Assert.assertEquals(Dibs.claimedBy(Sofia.testEmail1()), map.get("claimedBy"));
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

    @Test
    public void testSingleOrders() throws IOException {
        collection.remove(new BasicDBObject());
        for (int i = 0; i < 10; i++) {
            createOrder(i, "Awesome Vendor", false);
        }
        String response = resources.client().resource("/orders/2014-07-10/single").get(String.class);
        Assert.assertEquals(10, parseResponse(response).size());
    }

    private Order createOrder(final int count, final String vendor, final boolean group) {
        Order order = new Order();
        order.setVendor(vendor);
        order.setGroup(group);
        order.setExpectedAt(new DateTime(2014, 7, 10, 11, 45).toDate());
        order.setContents("yum " + count);
        order.setOrderedBy(Sofia.orderedByEmail());
        datastore.save(order);
        return order;
    }

    private List<Order> generateData() {
        collection.remove(new BasicDBObject());
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Order order = createOrder(i, "Awesome Vendor", false);
            order.setUpForGrabs(true);
            datastore.save(order);
            orders.add(order);
        }
        return orders;
    }

    private JsonNode parseResponse(final String response) throws IOException {
        return new ObjectMapper().readTree(response);
    }

    private static class TestInjectableProvider implements InjectableProvider<Restricted, Parameter> {
        private User user;

        public TestInjectableProvider() {
            user = new User(UUID.randomUUID());
            user.setEmail(Sofia.testEmail1());
        }

        @Override
        public ComponentScope getScope() {
            return ComponentScope.PerRequest;
        }

        @Override
        public Injectable getInjectable(final ComponentContext ic, final Restricted restricted,
                                        final Parameter parameter) {
            return () -> user;
        }
    }
}
