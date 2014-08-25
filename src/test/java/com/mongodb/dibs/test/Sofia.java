package com.mongodb.dibs.test;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.security.*;
import java.util.ResourceBundle.Control;

import java.util.logging.*;

public class Sofia {
    private static Map<Locale, ResourceBundle> messages = new HashMap<>();
        private static Logger logger = Logger.getLogger(Sofia.class.getName());

    private Sofia() {}

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
            bundle = ResourceBundle.getBundle("sofia", locale );
            messages.put(locale, bundle);
        }
        return bundle;
    }

    private static String getMessageValue(String key, Locale... locale) {
        return (String) getBundle(locale).getObject(key);
    }

    public static String orderedByEmail(Locale... locale) {
        return getMessageValue("ordered.by.email", locale);
    }

    public static String testEmail1(Locale... locale) {
        return getMessageValue("test.email.1", locale);
    }

    public static String testEmail2(Locale... locale) {
        return getMessageValue("test.email.2", locale);
    }


}
