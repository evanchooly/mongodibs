package com.mongodb.dibs.email;

import com.mongodb.dibs.Smtp;
import com.mongodb.dibs.model.Order;
import com.mongodb.dibs.model.SeamlessConfirmation;
import org.apache.commons.mail.util.MimeMessageUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class EmailParserTest {

    private SeamlessConfirmationEmailListener listener = new SeamlessConfirmationEmailListener(null);

    @Test
    public void testGroupParse() throws Exception {
        MimeMessage content = MimeMessageUtils.createMimeMessage(null, getClass().getResourceAsStream("/seamless-email.txt"));
        final SeamlessConfirmation confirmation = listener.parseMessage(content);
        Assert.assertEquals("someguy@example.com", confirmation.getFrom());
        Assert.assertEquals(Smtp.notificationsEmailAddress(), confirmation.getEmail());
        Assert.assertTrue(confirmation.getVendor(), confirmation.getVendor().contains("Lenwich"));
    }
}
