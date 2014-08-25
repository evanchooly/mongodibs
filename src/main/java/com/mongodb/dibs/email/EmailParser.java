package com.mongodb.dibs.email;

import com.mongodb.dibs.model.Order;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class EmailParser {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE, MMMM d, yyyy k:mm aa");

    public static final String ORDER_CONTENT_QUERY = ".gmail_quote > div:nth-child(12) > table:nth-child(1) > tbody:nth-child(1) > " +
                                                     "tr:nth-child(2) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > " +
                                                     "tr:nth-child(1) > td:nth-child(1) > table:nth-child(2) > tbody:nth-child(1) > " +
                                                     "tr:nth-child(2) > td:nth-child(1) > table:nth-child(1)";
    public static final String VENDOR_QUERY =
        ".gmail_quote > div:nth-child(12) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > table:nth-child" +
        "(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > " +
        "td:nth-child(1) > div:nth-child(1)";
    public static final String DELIVERY_INFO_QUERY =
        ".gmail_quote > div:nth-child(12) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > table:nth-child" +
        "(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(2) > tbody:nth-child(1) > tr:nth-child(1) > " +
        "td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > span:nth-child(1)";
    public static final String EXPECTED_AT_DATE_QUERY = DELIVERY_INFO_QUERY + " > span:nth-child(2)"; // Friday, June 27, 2014
    public static final String EXPECTED_AT_TIME_QUERY = DELIVERY_INFO_QUERY + " > span:nth-child(3)"; // 11:45 AM

    public Order parse(String from, String body) {
        Order order = new Order();
        order.setOrderedBy(from);

        Document document = Jsoup.parse(body);

        try {
            order.setExpectedAt(DATE_FORMAT.parse(
                                                     select(document, EXPECTED_AT_DATE_QUERY).text() + " " +
                                                     select(document, EXPECTED_AT_TIME_QUERY).text()));
        } catch (final ParseException pe) {
            throw new IllegalArgumentException("Couldn't parse expectedAt date in HTML email body: " + pe.getMessage());
        }

        order.setVendor(select(document, VENDOR_QUERY).text());
        order.setContents(select(document, ORDER_CONTENT_QUERY).html());
        order.setGroup(select(document, DELIVERY_INFO_QUERY).text().contains("(GROUP ORDER)"));
        return order;
    }

    private Element select(final Element root, final String query) {
        Element element = root.select(query).get(0);
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
