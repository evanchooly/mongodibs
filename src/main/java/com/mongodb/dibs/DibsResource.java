package com.mongodb.dibs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import com.mongodb.dibs.model.Order;
import io.dropwizard.views.View;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.MorphiaIterator;
import org.mongodb.morphia.query.Query;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Response;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public String findOrders(@PathParam("date") String dateString, @PathParam("type") String type) throws IOException, ParseException {
        DateTime dateTime = DateTime.parse(dateString, DateTimeFormat.forPattern("yyyy-MM-dd"));
        DateTime next = dateTime.plusDays(1);

        boolean groupOrder = type.equalsIgnoreCase("group");
        Query<Order> query = ds.createQuery(Order.class)
                               .filter("group", groupOrder)
                               .field("orderDate").greaterThanOrEq(dateTime.toDate())
                               .field("orderDate").lessThan(next.toDate());

        return groupOrder ? findGroupOrders(query) : findSingleOrders(query);
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
    
    private String findSingleOrders(final Query<Order> query) throws JsonProcessingException {
        MorphiaIterator<Order, Order> iterator = query.order("offeredBy").fetch();
        List<Order> orders = new ArrayList<>();
        try {
            for (Order order : iterator) {
                orders.add(order);
            }
        } finally {
            iterator.close();
        }
        return mapper.writeValueAsString(orders);
    }

    private String findGroupOrders(final Query<Order> query) throws JsonProcessingException {
        MorphiaIterator<Order, Order> iterator = query.fetch();
        Map<String, List<Order>> orders = new LinkedHashMap<>();
        try {
            for (Order order : iterator) {
                List<Order> list = orders.get(order.getVendor());
                if(list == null) {
                    list = new ArrayList<>();
                    orders.put(order.getVendor(), list);
                }
                list.add(order);
            }
        } finally {
            iterator.close();
        }
        return mapper.writeValueAsString(orders);
    }

    public static class GroupOrder {
        private String vendor;
        private List<Order> orders = new ArrayList<>();
        
        public void add(Order order) {
            orders.add(order);
        }
    }
}