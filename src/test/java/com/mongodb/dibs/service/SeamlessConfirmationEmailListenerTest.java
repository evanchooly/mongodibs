package com.mongodb.dibs.service;

import com.mongodb.dibs.Smtp;
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

public class SeamlessConfirmationEmailListenerTest extends BaseDibsTest {
    private final static String TEST_DATETIME = "11:45 am 12/15/15";
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
    public void parseEmail() throws Exception {
        final Session session = Session.getInstance(new Properties());
        final SeamlessConfirmation confirmation = emailListener.parseMessage(new MimeMessage(session, emailInput));
        assertEquals(Smtp.notificationsEmailAddress(), confirmation.getEmail());
        assertEquals("Lenwich by Lenny’s (43rd Street)", confirmation.getVendor());
        assertEquals(SeamlessConfirmationEmailListener.DATE_FORMAT.parse(TEST_DATETIME), confirmation.getExpectedAt());
        // TODO body?
    }
}
