package com.mongodb.dibs;

import com.mongodb.BasicDBObject;
import com.mongodb.dibs.model.Order;
import com.mongodb.dibs.test.Sofia;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;

public class TestDibsApplication extends DibsApplication {

    @Override
    public void initialize(final Bootstrap<DibsConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle("/assets", "/assets", null, "assets"));
        bootstrap.addBundle(new AssetsBundle("/META-INF/resources/webjars", "/webjars", null, "webjars"));
    }

    @Override
    public void run(final DibsConfiguration configuration, final Environment environment) throws Exception {
        super.run(configuration, environment);

        generateTestData(getDatastore());
    }

    private void generateTestData(final Datastore datastore) {
        System.out.println("***  Generating test data ***");
        datastore.getCollection(Order.class).remove(new BasicDBObject());
        String[] vendors = {"Chopt", "Schnippers", "Food Bucket", "Kosher Deluxe", "Baja Fresh (Broadway)"};
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

    private Order createTestOrder(final Datastore ds, final int count, final String vendor, final boolean group) {
        Order order = new Order();
        order.setVendor(vendor);
        order.setGroup(group);
        order.setExpectedAt(DateTime.now().withTime(11, 45, 0, 0).toDate());
        order.setContents("yum " + count);
        order.setOrderedBy(count % 2 == 0 ? Sofia.testEmail1() : Sofia.testEmail2());
        ds.save(order);
        return order;
    }

    public static void main(String[] args) throws Exception {
        new TestDibsApplication().run(args);
    }
}
