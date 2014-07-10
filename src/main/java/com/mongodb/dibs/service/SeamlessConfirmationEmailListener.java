package com.mongodb.dibs.service;

import org.apache.commons.mail.util.MimeMessageParser;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeamlessConfirmationEmailListener implements SimpleMessageListener {
    private final static Pattern subjectRegex = Pattern.compile("Confirmed!\\s+(.*?)\\s+received your order. Estimated Delivery:\\s+(.*)$");
    private final static SimpleDateFormat sdf = new SimpleDateFormat("h:m a M/d/yy");

    private final Session session;

    public SeamlessConfirmationEmailListener() {
        session = Session.getInstance(new Properties() /* FIXME */);
    }

    @Override
    public boolean accept(final String from, final String recipient) {
        return true;
    }

    @Override
    public void deliver(
        final String from,
        final String recipient,
        final InputStream data)
        throws TooMuchDataException, IOException
    {
        try {
            final MimeMessage message = new MimeMessage(session, data);
            final MimeMessageParser parser = new MimeMessageParser(message).parse();
            final Address to = parser.getTo().get(0);
            final String subject = parser.getSubject();
            final Matcher matcher = subjectRegex.matcher(subject);

            if (matcher.matches()) {
                final String vendor = matcher.group(1);
                final Date expectedAt = sdf.parse(matcher.group(2).replaceAll("\\.", "").replace(", on", ""));
            }
        } catch (Exception e) {
            // TODO send failure email
            e.printStackTrace();
        }
    }

    public static void main(final String ... args) {
        final SMTPServer server = new SMTPServer(new SimpleMessageListenerAdapter(new SeamlessConfirmationEmailListener()));
        server.setPort(2500);
        server.start();
    }
}
