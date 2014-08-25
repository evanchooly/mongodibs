package com.mongodb.dibs;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.security.*;
import java.util.ResourceBundle.Control;

import java.util.logging.*;

public class Smtp {
    private static Map<Locale, ResourceBundle> messages = new HashMap<>();
        private static Logger logger = Logger.getLogger(Smtp.class.getName());

    private Smtp() {}

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
            bundle = ResourceBundle.getBundle("smtp", locale );
            messages.put(locale, bundle);
        }
        return bundle;
    }

    private static String getMessageValue(String key, Locale... locale) {
        return (String) getBundle(locale).getObject(key);
    }

    public static String adminEmailAddress(Locale... locale) {
        return getMessageValue("admin.email.address", locale);
    }

    public static String imapHost(Locale... locale) {
        return getMessageValue("imap.host", locale);
    }

    public static String imapPort(Locale... locale) {
        return getMessageValue("imap.port", locale);
    }

    public static String notificationsEmailAddress(Locale... locale) {
        return getMessageValue("notifications.email.address", locale);
    }

    public static String notificationsEmailPassword(Locale... locale) {
        return getMessageValue("notifications.email.password", locale);
    }

    public static String smtpHost(Locale... locale) {
        return getMessageValue("smtp.host", locale);
    }

    public static String smtpPort(Locale... locale) {
        return getMessageValue("smtp.port", locale);
    }

    public static String upForGrabsEmailAddress(Locale... locale) {
        return getMessageValue("up.for.grabs.email.address", locale);
    }

    public static String upForGrabsEmailPassword(Locale... locale) {
        return getMessageValue("up.for.grabs.email.password", locale);
    }


}
