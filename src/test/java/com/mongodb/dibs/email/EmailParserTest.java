package com.mongodb.dibs.email;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EmailParserTest {

    @Test
    public void testParse() throws Exception {
        String body;
        try(InputStream stream = getClass().getResourceAsStream("/seamless-email.html")) {
            byte[] bytes = new byte[8192];
            int read;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((read = stream.read(bytes)) != -1) {
                baos.write(bytes, 0, read);
            }
            body = new String(baos.toByteArray());
        }
        
        new EmailParser().parse("test@example.com", body);
    }
}