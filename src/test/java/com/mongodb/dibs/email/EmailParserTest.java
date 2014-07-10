package com.mongodb.dibs.email;

import com.mongodb.dibs.model.Order;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class EmailParserTest {

    private EmailParser emailParser = new EmailParser();

    @Test
    public void testIndividualParse() throws Exception {
        Assert.assertFalse(emailParser.parse("test@example.com", readBody("seamless-email.html")).isGroup());
    }

    @Test
    public void testGroupParse() throws Exception {
        Assert.assertTrue(emailParser.parse("test@example.com", readBody("seamless-group-email.html")).isGroup());
    }

    @Test
    public void testExpectedAtDateParse() throws Exception {
        final Order order = emailParser.parse("test@example.com", readBody("seamless-group-email.html"));
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2014);
        calendar.set(Calendar.MONDAY, Calendar.JULY);
        calendar.set(Calendar.DAY_OF_MONTH, 2);
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 45);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(order.getExpectedAt(), calendar.getTime());
    }

    private String readBody(final String email) throws IOException {
        String body;
        try(InputStream stream = getClass().getResourceAsStream("/" + email)) {
            byte[] bytes = new byte[8192];
            int read;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((read = stream.read(bytes)) != -1) {
                baos.write(bytes, 0, read);
            }
            body = new String(baos.toByteArray());
        }
        return body;
    }
}
