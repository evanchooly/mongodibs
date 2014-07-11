package com.mongodb.dibs.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Version;

import java.util.Date;

@Entity("orders")
@Indexes({
             @Index("expectedAt, vendor"),
             @Index("expectedAt, orderedBy")
         })
public class Order {
    @Id
    private ObjectId id;
    private String vendor;
    private Date expectedAt;
    private Date claimedDate;
    private Date deliveredAt;
    private String claimedBy;
    private String orderedBy;
    private String contents;
    private Boolean group;
    private Boolean upForGrabs = Boolean.FALSE;

    @Version
    private Long version;

    public ObjectId getId() {
        return id;
    }

    public void setId(final ObjectId id) {
        this.id = id;
    }

    public Boolean isGroup() {
        return group;
    }

    public void setGroup(final Boolean group) {
        this.group = group;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(final String vendor) {
        this.vendor = vendor;
    }

    public Date getExpectedAt() {
        return expectedAt;
    }

    public void setExpectedAt(final Date expectedAt) {
        this.expectedAt = expectedAt;
    }

    public Date getClaimedDate() {
        return claimedDate;
    }

    public void setClaimedDate(final Date claimedDate) {
        this.claimedDate = claimedDate;
    }

    public Date getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(final Date deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(final String claimedBy) {
        this.claimedBy = claimedBy;
    }

    public String getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(final String orderedBy) {
        this.orderedBy = orderedBy;
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

    public Boolean getUpForGrabs() {
        return upForGrabs;
    }

    public void setUpForGrabs(final Boolean upForGrabs) {
        this.upForGrabs = upForGrabs;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Order{");
        sb.append("id=").append(id);
        sb.append(", vendor='").append(vendor).append('\'');
        sb.append(", expectedAt=").append(expectedAt);
        sb.append(", claimedDate=").append(claimedDate);
        sb.append(", claimedBy='").append(claimedBy).append('\'');
        sb.append(", orderedBy='").append(orderedBy).append('\'');
        sb.append(", contents='").append(contents).append('\'');
        sb.append(", group=").append(group);
        sb.append(", upForGrabs=").append(upForGrabs);
        sb.append('}');
        return sb.toString();
    }
}
