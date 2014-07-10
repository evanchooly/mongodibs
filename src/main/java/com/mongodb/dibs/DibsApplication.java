package com.mongodb.dibs;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
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

        mongo = new MongoClient();
        environment.getApplicationContext().setSessionsEnabled(true);
        environment.getApplicationContext().setSessionHandler(new SessionHandler());

        Datastore datastore = morphia.createDatastore(mongo, "mongo-dibs");
        datastore.ensureIndexes();

        environment.jersey().register(new DibsResource(datastore));

        String testdata = System.getProperty("testdata");
        if (testdata != null) {
            System.out.println("***  Generating test data ***");
            datastore.getCollection(Order.class).remove(new BasicDBObject());
            for (int i = 0; i < 100; i++) {
                createTestOrder(datastore, i, "Vendor " + (i % 5), true);
            }
            for (int i = 0; i < 10; i++) {
                createTestOrder(datastore, i, "Awesome Vendor", false);
            }
        }

    }

    private void createTestOrder(final Datastore ds, final int count, final String vendor, final boolean group) {
        Order order = new Order();
        order.setVendor(vendor);
        order.setGroup(group);
        order.setExpectedAt(new DateTime(2014, 7, 10, 11, 45).toDate());
        order.setContents("yum " + count);
        ds.save(order);
    }

    public static void main(String[] args) throws Exception {
        new DibsApplication().run(args);
    }
}
