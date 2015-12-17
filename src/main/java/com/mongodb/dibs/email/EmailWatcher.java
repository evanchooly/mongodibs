package com.mongodb.dibs.email;

import com.mongodb.dibs.Smtp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

abstract class EmailWatcher implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(EmailWatcher.class);

    private final String email;
    private final String password;
    private Properties props = new Properties();
    private Folder inbox;
    private Folder problems;
    private Store store;

    EmailWatcher(final String email, final String password) throws MessagingException {
        this.email = email;
        this.password = password;

        props.setProperty("mail.store.protocol", "imaps");
    }

    @Override
    public void run() {
        try {
            cleanUp();

            store = Session.getInstance(props, null).getStore("imaps");
            store.connect(Smtp.imapHost(), email, password);

            inbox = openFolder(store, "INBOX");
            problems = openFolder(store, "Problem Emails");

            for (Message message : inbox.getMessages()) {
                System.out.println("message = " + message);
                try {
                    process(message);
                } catch (Exception e) {
                    try {
                        LOG.error(e.getMessage(), e);
                        inbox.copyMessages(new Message[]{message}, problems);
                    } catch (MessagingException e1) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + ":" + e.getMessage(), e);
        } finally {
            try {
                cleanUp();
            } catch (MessagingException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void cleanUp() throws MessagingException {
        if (inbox != null && inbox.isOpen()) {
            inbox.close(false);
        }
        if (problems != null && problems.isOpen()) {
            problems.close(false);
        }
        if (store != null && store.isConnected()) {
            store.close();
        }
    }

    public static Folder openFolder(final Store store, final String name) throws MessagingException {
        Folder folder = store.getFolder(name);
        folder.open(Folder.READ_WRITE);
        return folder;
    }

    protected abstract void process(final Message message) throws MessagingException;
}
