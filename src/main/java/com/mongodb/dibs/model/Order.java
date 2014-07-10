package com.mongodb.dibs.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Version;

import java.util.Date;

@Entity("orders")
@Indexes(
            @Index("orderDate, vendor")
)
public class Order {
    @Id
    private ObjectId id;
    private String vendor;
    private Date orderDate;
    private Date claimedDate;
    private String claimedBy;
    private String offeredBy;
    private String contents;
    @Version
    private Long version;

    public ObjectId getId() {
        return id;
    }

    public void setId(final ObjectId id) {
        this.id = id;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(final String vendor) {
        this.vendor = vendor;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(final Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getClaimedDate() {
        return claimedDate;
    }

    public void setClaimedDate(final Date claimedDate) {
        this.claimedDate = claimedDate;
    }

    public String getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(final String claimedBy) {
        this.claimedBy = claimedBy;
    }

    public String getOfferedBy() {
        return offeredBy;
    }

    public void setOfferedBy(final String offeredBy) {
        this.offeredBy = offeredBy;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(final String contents) {
        this.contents = contents;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(final Long version) {
        this.version = version;
    }
}