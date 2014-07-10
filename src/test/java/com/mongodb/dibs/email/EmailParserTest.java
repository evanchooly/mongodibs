package com.mongodb.dibs.email;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
