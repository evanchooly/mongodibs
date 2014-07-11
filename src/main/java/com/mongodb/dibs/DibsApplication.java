package com.mongodb.dibs;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.dibs.model.Order;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.session.SessionHandler;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.Random;

public class DibsApplication extends Application<DibsConfiguration> {
    private MongoClient mongo;
    private Morphia morphia;

    @Override
    public void initialize(final Bootstrap<DibsConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle("/assets", "/assets", null, "assets"));
        bootstrap.addBundle(new AssetsBundle("/META-INF/resources/webjars", "/webjars", null, "webjars"));
    }

    @Override
    public void run(final DibsConfiguration configuration, final Environment environment) throws Exception {
        morphia = new Morphia();
        morphia.mapPackage(Order.class.getPackage().getName());

        final MongoClientURI mongoUri = new MongoClientURI(configuration.getMongo());
        mongo = new MongoClient(mongoUri);
        environment.getApplicationContext().setSessionsEnabled(true);
        environment.getApplicationContext().setSessionHandler(new SessionHandler());
        environment.healthChecks().register("dibs", new DibsHealthCheck());

        Datastore datastore = morphia.createDatastore(mongo, mongoUri.getDatabase());
        datastore.ensureIndexes();

        environment.jersey().register(new DibsResource(configuration, datastore));

        String testdata = System.getProperty("testdata");
        if (testdata != null) {
            System.out.println("***  Generating test data ***");
            datastore.getCollection(Order.class).remove(new BasicDBObject());
            Random random = new Random();
            String[] vendors = { "Chopt", "Schnippers", "Food Bucket", "Kosher Deluxe", "Baja Fresh (Broadway)"};
            for (int i = 0; i < 100; i++) {
                String vendor = vendors[i % 5];
                createTestOrder(datastore, i, vendor, true);
            }
            for (int i = 0; i < 10; i++) {
                String vendor = vendors[i % 5];
                
                createTestOrder(datastore, i, vendor, false);
            }
            for (int i = 0; i < 10; i++) {
                Order order = createTestOrder(datastore, i, vendors[i % 5], false);
                order.setUpForGrabs(true);
                datastore.save(order);
            }
        }

    }

    private Order createTestOrder(final Datastore ds, final int count, final String vendor, final boolean group) {
        Order order = new Order();
        order.setVendor(vendor);
        order.setGroup(group);
        order.setExpectedAt(DateTime.now().withTime(11, 45, 0, 0).toDate());
        order.setContents("yum " + count);
        order.setOrderedBy("Employee " + count);
        ds.save(order);
        return order;
    }

    public static void main(String[] args) throws Exception {
        new DibsApplication().run(args);
    }
}
