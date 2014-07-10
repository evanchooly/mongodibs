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
            @Index("expectedAt, vendor")
)
public class Order {
    @Id
    private ObjectId id;
    private String vendor;
    private Date expectedAt;
    private Date claimedDate;
    private String claimedBy;
    private String offeredBy;
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

    public Boolean getGroup() {
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

    public Boolean getUpForGrabs() {
        return upForGrabs;
    }

    public void setUpForGrabs(final Boolean upForGrabs) {
        this.upForGrabs = upForGrabs;
    }
}
