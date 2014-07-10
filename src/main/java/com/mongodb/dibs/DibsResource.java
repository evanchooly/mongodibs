package com.mongodb.dibs;

import com.google.common.base.Charsets;
import com.mongodb.dibs.model.Order;
import io.dropwizard.views.View;
import org.mongodb.morphia.Datastore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DibsResource {
    private final Datastore ds;
    private JacksonMapper mapper = new JacksonMapper();

    public DibsResource(final Datastore ds) {
        this.ds = ds;
    }

    @GET
    @Produces("text/html;charset=ISO-8859-1")
    public View index() {
        return new View("/index.ftl", Charsets.ISO_8859_1) {
        };
    }

    @GET
    @Path("/orders/{date}/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public String groupOrders(@PathParam("date") String date, @PathParam("type") String type) throws IOException {
        ds.createQuery(Order.class)
          .filter("group", type.equalsIgnoreCase("group"))
          .order("vendor");
        return mapper.writeValueAsString("");
    }
}
