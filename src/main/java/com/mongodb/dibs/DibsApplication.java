package com.mongodb.dibs;

import com.google.common.base.Optional;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.dibs.auth.OpenIDAuthenticator;
import com.mongodb.dibs.auth.OpenIDRestrictedToProvider;
import com.mongodb.dibs.auth.User;
import com.mongodb.dibs.email.SeamlessConfirmationEmailListener;
import com.mongodb.dibs.model.Order;
import com.mongodb.dibs.resources.DibsResource;
import com.mongodb.dibs.resources.PublicOAuthResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.session.SessionHandler;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public class DibsApplication extends Application<DibsConfiguration> {

    private MongoClientURI mongoUri;
    private MongoClient mongoClient;
    private Datastore datastore;

    @Override
    public void initialize(final Bootstrap<DibsConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle("/assets", "/assets", null, "assets"));
        bootstrap.addBundle(new AssetsBundle("/META-INF/resources/webjars", "/webjars", null, "webjars"));
    }

    @Override
    public void run(final DibsConfiguration configuration, final Environment environment) throws Exception {
        final Morphia morphia = new Morphia();
        morphia.mapPackage(Order.class.getPackage().getName());

        mongoUri = new MongoClientURI(configuration.getMongoUri());
        mongoClient = new MongoClient(mongoUri);
        datastore = morphia.createDatastore(mongoClient, mongoUri.getDatabase());
        datastore.ensureIndexes();

        environment.getApplicationContext().setSessionsEnabled(true);
        environment.getApplicationContext().setSessionHandler(new SessionHandler());
        environment.healthChecks().register("dibs", new DibsHealthCheck());

        environment.jersey().register(new PublicOAuthResource(configuration));
        environment.jersey().register(new DibsResource(configuration, datastore));
        environment.jersey().register(new RuntimeExceptionMapper());

        OpenIDAuthenticator authenticator = new OpenIDAuthenticator();
        environment.jersey().register(new OpenIDRestrictedToProvider<User>(authenticator, "OpenID"));

        SeamlessConfirmationEmailListener emailListener = new SeamlessConfirmationEmailListener(datastore);
        emailListener.start();
    }

    public MongoClientURI getMongoUri() {
        return mongoUri;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public static void main(String[] args) throws Exception {
        new DibsApplication().run(args);
    }

    private static class OathAuthenticator implements io.dropwizard.auth.Authenticator<String, User> {
        @Override
        public Optional<User> authenticate(final String credentials) throws AuthenticationException {
            System.out.println("credentials = " + credentials);
            throw new UnsupportedOperationException("Not implemented yet!");
        }
    }
}
