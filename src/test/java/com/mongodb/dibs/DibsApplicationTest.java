package com.mongodb.dibs;

import com.mongodb.MongoClient;
import com.sun.jersey.api.client.WebResource;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.mongodb.morphia.Morphia;

import java.net.UnknownHostException;

/**
 * Unit test for simple DibsApplication.
 */
public class DibsApplicationTest {
    @ClassRule
    public static final ResourceTestRule resources;

    static {
        try {
            DibsResource resource = new DibsResource(new Morphia().createDatastore(new MongoClient(), "mongo-dibs"));
            resources = ResourceTestRule.builder()
                                        .addResource(resource)
                                        .build();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testGroupOrders() {
        WebResource resource = resources.client().resource("/orders/2014-07-10/group");
        String response = resource.get(String.class);
        System.out.println("response = " + response);
    }
}
