package com.mongodb.dibs.email;

import com.mongodb.dibs.model.Order;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.Date;
import java.util.List;

public class EmailParser {

    public static final String ORDER_CONTENT_QUERY =
        "body > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > table:nth-child" +
        "(1) > " +
        "tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(2) > tbody:nth-child(1) > " +
        "tr:nth-child"
        +
        "(2) > td:nth-child(1) > table:nth-child(1)";
    public static final String VENDOR_QUERY =
        "body > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > table:nth-child(1) > " +
        "tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > " +
        "tr:nth-child(1) > td:nth-child(1) > div:nth-child(1)";
    public static final String DELIVERY_INFO_QUERY =
        "body > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > table:nth-child(1) " +
        "> tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(2) > tbody:nth-child(1) > " +
        "tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > " +
        "td:nth-child(1) > span:nth-child(1) > div:nth-child(1)";

    public Order parse(String from, String body) {
        Order order = new Order();
        order.setOfferedBy(from);
        order.setOrderDate(new Date());

        Document document = Jsoup.parse(body);

        order.setVendor(select(document, VENDOR_QUERY).html());

        order.setContents(select(document, ORDER_CONTENT_QUERY).html());

        order.setGroup(select(document, DELIVERY_INFO_QUERY).text().contains("(GROUP ORDER)"));
        return order;
    }

    private Element select(final Document document, final String query) {
        Element element = document.select(query).get(0);
        trimStyles(element.childNodes());
        return element;
    }

    private void trimStyles(final List<Node> nodes) {
        for (Node node : nodes) {
            node.removeAttr("style");
            trimStyles(node.childNodes());
        }
    }
}