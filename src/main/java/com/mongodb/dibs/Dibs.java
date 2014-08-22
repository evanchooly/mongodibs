package com.mongodb.dibs;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.security.*;
import java.util.ResourceBundle.Control;

import java.util.logging.*;

public class Dibs {
    private static Map<Locale, ResourceBundle> messages = new HashMap<>();
        private static Logger logger = Logger.getLogger(Dibs.class.getName());

    private Dibs() {}

    private static ResourceBundle getBundle(Locale... localeList) {
        Locale locale = localeList.length == 0 ? Locale.getDefault() : localeList[0];
        ResourceBundle labels = loadBundle(locale);
        if(labels == null) {
            labels = loadBundle(Locale.ROOT);
        }
        return labels;
    }

    private static ResourceBundle loadBundle(Locale locale) {
        ResourceBundle bundle = messages.get(locale);
        if(bundle == null) {
            bundle = ResourceBundle.getBundle("dibs", locale );
            messages.put(locale, bundle);
        }
        return bundle;
    }

    private static String getMessageValue(String key, Locale... locale) {
        return (String) getBundle(locale).getObject(key);
    }

    public static String claimSuccessful(Object arg0, Locale... locale) {
        return MessageFormat.format(getMessageValue("claim.successful", locale), arg0);
    }

    public static String claimedBy(Object arg0, Locale... locale) {
        return MessageFormat.format(getMessageValue("claimed.by", locale), arg0);
    }

    public static String mongoMongodibs(Locale... locale) {
        return getMessageValue("mongo.mongodibs", locale);
    }

    public static String orderClaimed(Object arg0, Locale... locale) {
        return MessageFormat.format(getMessageValue("order.claimed", locale), arg0);
    }

    public static String orderDelivered(Object arg0, Locale... locale) {
        return MessageFormat.format(getMessageValue("order.delivered", locale), arg0);
    }

    public static String smtpServicePort(Locale... locale) {
        return getMessageValue("smtp.service.port", locale);
    }


}
