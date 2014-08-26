package com.mongodb.dibs.email;

import com.mongodb.dibs.Smtp;
import com.sun.mail.imap.IMAPFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

abstract class EmailWatcher implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(EmailWatcher.class);

    private final String email;
    private final String password;
    private final long delay;
    private Properties props = new Properties();
    private boolean running = true;

    EmailWatcher(final String email, final String password) throws MessagingException {
        this.email = email;
        this.password = password;

        props.setProperty("mail.store.protocol", "imaps");
        delay = TimeUnit.MINUTES.toMillis(1);
    }

    @Override
    public void run() {
        Store store = null;
        try {
            store = Session.getInstance(props, null).getStore("imaps");
            store.connect(Smtp.imapHost(), email, password);
            final Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            final Folder problems = store.getFolder("Problem Emails");
            problems.open(Folder.READ_WRITE);
            inbox.addMessageCountListener(new MessageCountAdapter() {
                @Override
                public void messagesAdded(final MessageCountEvent event) {
                    for (Message message : event.getMessages()) {
                        try {
                            process(message);
                        } catch (MessagingException e) {
                            try {
                                inbox.copyMessages(new Message[]{message}, problems);
                            } catch (MessagingException e1) {
                                LOG.error(e.getMessage(), e);
                            }
                        }
                    }

                }
            });
            boolean supportsIdle;
            try {
                ((IMAPFolder) inbox).idle();
                ((IMAPFolder) problems).idle();
                supportsIdle = true;
            } catch (FolderClosedException fex) {
                throw fex;
            } catch (MessagingException mex) {
                supportsIdle = false;
            }
            while (running) {
                if (supportsIdle) {
                    ((IMAPFolder) inbox).idle();
                    ((IMAPFolder) problems).idle();
                } else {
                    Thread.sleep(delay); 
                    inbox.getMessageCount();
                }
            }
        } catch (MessagingException | InterruptedException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    public void stop() {
        running = false;
    }
    protected abstract void process(final Message message) throws MessagingException;
}
