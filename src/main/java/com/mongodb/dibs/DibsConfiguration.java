package com.mongodb.dibs;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class DibsConfiguration extends Configuration {
    @JsonProperty("mongo.uri")
    private String mongoUri;


    public String getMongoUri() {
        return mongoUri;
    }
}
