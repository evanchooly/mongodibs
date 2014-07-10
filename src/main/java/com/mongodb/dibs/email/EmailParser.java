package com.mongodb.dibs.email;

import com.mongodb.dibs.model.Order;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.List;

public class EmailParser {
    public Order parse(String from, String body) {
        Order order = new Order();
        order.setOfferedBy(from);
        order.setOrderDate(new Date());
        
        Document document = Jsoup.parse(body);
        Elements elements
            = document.select("body > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > table:nth-child(1) > " +
                              "tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > " +
                              "tr:nth-child(1) > td:nth-child(1) > div:nth-child(1)");
        order.setVendor(elements.get(0).html()); 
        
        elements = document.select("body > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > table:nth-child" +
                                  "(1) > " +
                        "tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(2) > tbody:nth-child(1) > tr:nth-child" +
                        "(2) > td:nth-child(1) > table:nth-child(1)");
        Element element = elements.get(0);
        trimStyles(element.childNodes());
        order.setContents(element.html()); 
        
        return order;
    }

    private void trimStyles(final List<Node> nodes) {
        for (Node node : nodes) {
            node.removeAttr("style");
            trimStyles(node.childNodes());
        }
    }
}