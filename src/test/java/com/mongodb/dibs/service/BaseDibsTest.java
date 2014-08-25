package com.mongodb.dibs.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.dibs.DibsApplication;
import com.mongodb.dibs.DibsConfiguration;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import io.dropwizard.setup.Bootstrap;
import org.junit.After;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.io.IOException;

public class BaseDibsTest {
    private DibsConfiguration dibsConfiguration;
    private Datastore datastore;
    private MongoClient mongoClient;

    public BaseDibsTest() throws IOException, ConfigurationException {
        final Bootstrap<DibsConfiguration> bootstrap = new Bootstrap<>(new DibsApplication());

        ConfigurationFactoryFactory<DibsConfiguration> factoryFactory = bootstrap.getConfigurationFactoryFactory();
        final ConfigurationFactory<DibsConfiguration> configurationFactory =
            factoryFactory.create(DibsConfiguration.class,
                                  bootstrap.getValidatorFactory().getValidator(),
                                  bootstrap.getObjectMapper(),
                                  "dw");
        dibsConfiguration = configurationFactory.build(bootstrap.getConfigurationSourceProvider(), "dibs.yml");

        MongoClientURI mongoClientURI = new MongoClientURI(dibsConfiguration.getMongoUri());
        mongoClient = new MongoClient(mongoClientURI);
        datastore = new Morphia().createDatastore(mongoClient, mongoClientURI.getDatabase());
        datastore.ensureIndexes();
    }

    @After
    public void tearDown() throws IOException {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public DibsConfiguration getDibsConfiguration() {
        return dibsConfiguration;
    }

    public Datastore getDatastore() {
        return datastore;
    }
}
