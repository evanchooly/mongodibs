package com.mongodb.dibs;

import com.mongodb.dibs.model.AWSCredentials;
import io.dropwizard.Configuration;

public class DibsConfiguration extends Configuration {
    private AWSCredentials awsCredentials;
    private String mongo;

    public void setAwsCredentials(final AWSCredentials awsCredentials) {
        this.awsCredentials = awsCredentials;
    }

    public AWSCredentials getAwsCredentials() {
        return awsCredentials;
    }

    public void setMongo(final String mongo) {
        this.mongo = mongo;
    }

    public String getMongo() {
        return mongo;
    }
}
