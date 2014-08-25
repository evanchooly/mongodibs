package com.mongodb.dibs.service;

import com.mongodb.dibs.email.SeamlessConfirmationEmailListener;
import com.mongodb.dibs.model.SeamlessConfirmation;
import io.dropwizard.configuration.ConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SeamlessConfirmationEmailListenerTest extends BaseDibsTest {
    private final static String TEST_DATETIME = "11:45 am 7/2/14";
    private InputStream emailInput;
    private SeamlessConfirmationEmailListener emailListener;

    public SeamlessConfirmationEmailListenerTest() throws IOException, ConfigurationException {
        emailListener = new SeamlessConfirmationEmailListener(getDatastore());
    }

    @Before
    public void setUpListener() {
        emailInput = SeamlessConfirmationEmailListenerTest.class.getResourceAsStream("/seamless-email.txt");
    }

    @After
    public void tearDown() throws IOException {
        if (emailInput != null) {
            emailInput.close();
        }
    }

    @Test
    public void disallowRecipient() {
        assertFalse(emailListener.accept(null, "asdfasdf"));
    }

    @Test
    public void allowRecipient() {
        assertTrue(emailListener.accept(null, "seamless@helloworld"));
    }

    @Test
    public void parseEmail() throws Exception {
        final Session session = Session.getInstance(new Properties());
        final SeamlessConfirmation confirmation = SeamlessConfirmationEmailListener.parseMessage(new MimeMessage(session, emailInput));
        assertEquals(confirmation.getEmail(), "stephen.lee@10gen.com");
        assertEquals(confirmation.getVendor(), "Chop't Creative Salad Co. (Times Square)");
        assertEquals(confirmation.getExpectedAt(), SeamlessConfirmationEmailListener.DATE_FORMAT.parse(TEST_DATETIME));
        // TODO body?
    }
}
