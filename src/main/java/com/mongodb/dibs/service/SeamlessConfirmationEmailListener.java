package com.mongodb.dibs.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.dibs.email.EmailParser;
import com.mongodb.dibs.model.Order;
import com.mongodb.dibs.model.SeamlessConfirmation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeamlessConfirmationEmailListener implements SimpleMessageListener {
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(SeamlessConfirmationEmailListener.class.getResourceAsStream("/dibs.properties"));
        } catch (IOException e) {
            System.err.println("Failed to load dibs.properties: " + e.getMessage());
            System.exit(-1);
        }
    }

    private final static Pattern subjectRegex = Pattern.compile("Confirmed!\\s+(.*?)\\s+received your order. Estimated Delivery:\\s+(.*)$");
    public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("h:m a M/d/yy");

    private final EmailParser emailParser = new EmailParser();
    private final Session session;
    private final Datastore ds;

    public SeamlessConfirmationEmailListener() {
        session = Session.getInstance(properties);
        MongoClient mongo = null;

        try {
            mongo = new MongoClient(new MongoClientURI(properties.getProperty("mongo.mongodibs")));
        } catch (final UnknownHostException uhe) {
            System.err.println("Could not resolve host specified in mongo.mongodibs property");
            System.exit(-1);
        }

        final Morphia morphia = new Morphia();
        morphia.map(SeamlessConfirmation.class);
        morphia.map(Order.class);
        ds = morphia.createDatastore(mongo, "mongodibs");
        ds.ensureIndexes();
    }

    @Override
    public boolean accept(final String from, final String recipient) {
        return !StringUtils.isBlank(recipient) && recipient.startsWith("seamless");
    }

    @Override
    public void deliver(
        final String from,
        final String recipient,
        final InputStream data)
        throws TooMuchDataException, IOException
    {
        try {
            final SeamlessConfirmation seamlessConfirmation = parseMessage(new MimeMessage(session, data));
            ds.save(seamlessConfirmation);

            final Order order = emailParser.parse(from, seamlessConfirmation.getBody());

            if (!validate(seamlessConfirmation, order)) {
                throw new Exception("Email could not be parsed");
            }

            ds.save(order);
        } catch (final Exception e) {
            // TODO send failure email
            e.printStackTrace();
        }
    }

    public boolean validate(final SeamlessConfirmation seamlessConfirmation, final Order order) {
        return seamlessConfirmation.getVendor().equals(order.getVendor()) &&
            seamlessConfirmation.getExpectedAt().equals(order.getExpectedAt());
    }

    public static SeamlessConfirmation parseMessage(final MimeMessage message) throws Exception {
        final MimeMessageParser parser = new MimeMessageParser(message).parse();
        final Address to = parser.getTo().get(0);
        final String subject = parser.getSubject();
        final Matcher matcher = subjectRegex.matcher(subject);

        if (!matcher.matches()) {
            throw new ParseException("Unable to parse subject", 0);
        }

        final String vendor = matcher.group(1);
        final Date expectedAt = DATE_FORMAT.parse(matcher.group(2).replaceAll("\\.", "").replace(", on", ""));
        final List<Map<String, String>> headers = getHeadersList(message);
        return new SeamlessConfirmation(to.toString(), vendor, expectedAt, headers, parser.getHtmlContent());
    }

    private static List<Map<String, String>> getHeadersList(final MimeMessage message) throws MessagingException {
        final List<Map<String, String>> headersList = new LinkedList<>();
        final Enumeration headers = message.getAllHeaders();

        while (headers.hasMoreElements()) {
            final Header h = (Header)headers.nextElement();
            Map<String, String> header = new HashMap<>();
            header.put("name", h.getName());
            header.put("value", h.getValue());
            headersList.add(header);
        }

        return headersList;
    }

    public static void main(final String ... args) throws Exception {
        final SMTPServer server = new SMTPServer(new SimpleMessageListenerAdapter(new SeamlessConfirmationEmailListener()));
        server.setPort(Integer.parseInt(properties.getProperty("smtp.service.port")));
        server.start();
    }
}
