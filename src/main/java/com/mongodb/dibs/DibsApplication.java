package com.mongodb.dibs;

import com.mongodb.MongoClient;
import com.mongodb.dibs.model.Order;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.session.SessionHandler;
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
    }

    public static void main(String[] args) throws Exception {
        new DibsApplication().run(args);
    }
}
