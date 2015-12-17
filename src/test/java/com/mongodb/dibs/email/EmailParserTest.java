package com.mongodb.dibs.email;

import com.mongodb.dibs.Smtp;
import com.mongodb.dibs.model.SeamlessConfirmation;
import com.mongodb.dibs.service.BaseDibsTest;
import io.dropwizard.configuration.ConfigurationException;
import org.apache.commons.mail.util.MimeMessageUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

public class EmailParserTest extends BaseDibsTest {
    private static final Logger LOG = getLogger(EmailParserTest.class);
    private SeamlessConfirmationEmailListener listener = new SeamlessConfirmationEmailListener(null);
    private SeamlessConfirmationEmailListener emailListener;

    public EmailParserTest() throws IOException, ConfigurationException {
        emailListener = new SeamlessConfirmationEmailListener(getDatastore());
    }

    @Test
    public void run() throws MessagingException {
        Folder problems = null;
        Store store = null;
        try {
            final Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imaps");

            store = Session.getInstance(props, null).getStore("imaps");
            store.connect(Smtp.imapHost(), Smtp.notificationsEmailAddress(), Smtp.notificationsEmailPassword());

            problems = EmailWatcher.openFolder(store, "Problem Emails");

            for (Message message : problems.getMessages(1, Math.min(50, problems.getMessageCount()))) {
                emailListener.parseMessage(message);
//                message.setFlag(Flag.DELETED, true);
            }
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + ":" + e.getMessage(), e);
        } finally {
            if (problems != null && problems.isOpen()) {
                problems.close(false);
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        }
    }

    @Test
    public void testGroupParse() throws Exception {
        MimeMessage content = MimeMessageUtils.createMimeMessage(null, getClass().getResourceAsStream("/seamless-email.txt"));
        final SeamlessConfirmation confirmation = listener.parseMessage(content);
        Assert.assertEquals("someguy@example.com", confirmation.getFrom());
        Assert.assertEquals(Smtp.notificationsEmailAddress(), confirmation.getEmail());
        Assert.assertTrue(confirmation.getVendor(), confirmation.getVendor().contains("Lenwich"));
    }
}
