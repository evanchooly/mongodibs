package com.mongodb.dibs;

import com.google.common.base.Charsets;
import com.mongodb.BasicDBObject;
import com.mongodb.dibs.model.Order;
import io.dropwizard.views.View;
import org.mongodb.morphia.Datastore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    public String groupOrders(@PathParam("date") String dateString, @PathParam("type") String type) throws IOException, ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
//        DateTime.parse()
        ds.createQuery(Order.class)
          .filter("group", type.equalsIgnoreCase("group"))
          .order("vendor");
        return mapper.writeValueAsString(new BasicDBObject("bob",date));
    }

    @POST
    @Path("/notify/{date}/vendor/{vendor}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifyGroup() {
        return null;
    }

    @POST
    @Path("/notify/{date}/order/{order}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifyOrder() {
        return null;
    }
}
