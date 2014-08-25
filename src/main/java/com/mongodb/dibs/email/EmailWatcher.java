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

    EmailWatcher(final String email, final String password) throws MessagingException {
        this.email = email;
        this.password = password;

        props.setProperty("mail.store.protocol", "imaps");

    }

    @Override
    public void run() {
        try {
            Store store = null;
            try {
                store = Session.getInstance(props, null).getStore("imaps");
                store.connect(Smtp.imapHost(), email, password);
                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE);
                for (Message message : inbox.getMessages()) {
                    System.out.println("message = " + message);
                    process(message);
                }
            } catch (MessagingException e) {
                LOG.error(e.getMessage(), e);
            } finally {
                if (store != null) {
                    store.close();
                }
            }
        } catch (MessagingException e) {
            LOG.error(e.getMessage(), e);
        }

    }

    protected abstract void process(final Message message) throws MessagingException;
}
