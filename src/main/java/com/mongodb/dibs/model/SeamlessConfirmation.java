package com.mongodb.dibs.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.mail.Address;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity("seamless.confirmations")
public class SeamlessConfirmation {
    @Id
    private final ObjectId id;

    @Property("email")
    private final String email;

    @Property("vendor")
    private final String vendor;

    @Property("expectedAt")
    private final Date expectedAt;

    @Property("headers")
    private final List<Map<String, String>> headers;  // list of {name: "From", value: "stephen.lee@10gen.com"}

    @Property("body")
    private final String body;

    public SeamlessConfirmation(
        final String email,
        final String vendor,
        final Date expectedAt,
        final List<Map<String, String>> headers,
        final String body) {
        this.id = new ObjectId();
        this.email = email;
        this.vendor = vendor;
        this.expectedAt = expectedAt;
        this.headers = headers;
        this.body = body;
    }

    public ObjectId getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getVendor() {
        return vendor;
    }

    public Date getExpectedAt() {
        return expectedAt;
    }

    public List<Map<String, String>> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}