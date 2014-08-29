package com.mongodb.dibs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mongodb.dibs.model.OAuthConfig;
import io.dropwizard.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DibsConfiguration extends Configuration {
    public static final String SESSION_TOKEN_NAME = "DibsSession";
    
    @JsonProperty("mongo.uri")
    private String mongoUri;

    @JsonProperty
    private String oauthSuccessUrl;

    @JsonProperty
    private HashMap<String, String> adminUsers = null;

    public String getMongoUri() {
        return mongoUri;
    }

    public String getOAuthSuccessUrl() {
        return oauthSuccessUrl;
    }

    @JsonDeserialize(contentAs = OAuthConfig.class)
    private List<OAuthConfig> oauthCfg;

    public List<OAuthConfig> getOAuthCfg() {
        return oauthCfg;
    }

    @JsonProperty
    private HashMap<String, String> oauthCustomCfg = null;

    public Map<String, String> OAuthCustomCfg() {
        return oauthCustomCfg;
    }

    public Properties getOAuthCfgProperties() {
        Properties properties = new Properties();
        for (OAuthConfig oauth : oauthCfg) {
            properties.put(oauth.getPrefix() + ".consumer_key",
                           oauth.getKey());
            properties.put(oauth.getPrefix() + ".consumer_secret",
                           oauth.getSecret());
            if (oauth.getPermissions() != null) {
                properties.put(oauth.getPrefix() + ".custom_permissions",
                               oauth.getPermissions());
            }
        }
        if (oauthCustomCfg != null) {
            // add any custom config strings
            properties.putAll(oauthCustomCfg);
        }
        return properties;
    }

    public Map<String, String> getAdminUsers() {
        return adminUsers;
    }

}
