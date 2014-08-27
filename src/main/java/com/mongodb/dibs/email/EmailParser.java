package com.mongodb.dibs.email;

import com.mongodb.dibs.model.Order;
import com.mongodb.dibs.model.SeamlessConfirmation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.List;

public class EmailParser {

    public Order parse(SeamlessConfirmation seamlessConfirmation) {
        Order order = new Order();
        order.setOrderedBy(seamlessConfirmation.getEmail());
        order.setExpectedAt(seamlessConfirmation.getExpectedAt());
        order.setVendor(seamlessConfirmation.getVendor());
        order.setGroup(seamlessConfirmation.getBody().contains("(GROUP ORDER)"));

        Document document = Jsoup.parse(seamlessConfirmation.getBody());

        order.setContents(select(document, ".gmail_quote > div:nth-child(12)").html());
        order.setGroup(seamlessConfirmation.getBody().contains("(GROUP ORDER)"));
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
