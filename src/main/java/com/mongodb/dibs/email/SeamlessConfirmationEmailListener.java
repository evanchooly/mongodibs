package com.mongodb.dibs.email;

import com.mongodb.dibs.model.Order;
import com.mongodb.dibs.model.SeamlessConfirmation;
import com.sun.mail.imap.IMAPMessage;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.mail.util.MimeMessageUtils;
import org.mongodb.morphia.Datastore;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SeamlessConfirmationEmailListener {
    public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("h:m a M/d/yy");

    private final EmailParser emailParser = new EmailParser();
    private final Session session;
    private final Datastore ds;
    public static final String ESTIMATED_DELIVERY = "Estimated Delivery: ";

    public SeamlessConfirmationEmailListener(Datastore ds) {
        this.ds = ds;
        session = Session.getInstance(new Properties());
    }

    public void start() throws MessagingException {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(new NotificationsWatcher(this), 1, 10, TimeUnit.SECONDS);
        //                executorService.scheduleAtFixedRate(new UpForGrabsWatcher(), 1, 10, TimeUnit.SECONDS);
    }

    public boolean accept(final String from, final String recipient) {
        return !StringUtils.isBlank(recipient) && recipient.startsWith("seamless");
    }

    public void deliver(final Message message) throws MessagingException {
        final SeamlessConfirmation seamlessConfirmation = parseMessage(message);
        ds.save(seamlessConfirmation);

        InternetAddress address = (InternetAddress) message.getFrom()[0];
        final Order order = emailParser.parse(address.getAddress(), seamlessConfirmation.getBody());

        if (!validate(seamlessConfirmation, order)) {
            throw new NotificationException("Email could not be parsed");
        }

        ds.save(order);
    }

    public boolean validate(final SeamlessConfirmation seamlessConfirmation, final Order order) {
        return seamlessConfirmation.getVendor().equals(order.getVendor()) &&
               seamlessConfirmation.getExpectedAt().equals(order.getExpectedAt());
    }

    public SeamlessConfirmation parseMessage(final Message message) throws MessagingException {
        MimeMessage content;
        final MimeMessageParser parser;
        final Address to;
        final String subject;
        String vendor;
        Date expectedAt;
        try {
            String forward = findForward(((IMAPMessage) message).getRawInputStream());
            content = MimeMessageUtils.createMimeMessage(session, forward);
            parser = new MimeMessageParser(content).parse();
            to = parser.getTo().get(0);
            subject = parser.getSubject();

            vendor = subject.substring(11, subject.indexOf(" received your order"));
            expectedAt = DATE_FORMAT.parse(subject.substring(subject.indexOf(ESTIMATED_DELIVERY) + ESTIMATED_DELIVERY.length())
                                                  .replaceAll("\\.", "").replace(", on", ""));
        } catch (Exception e) {
            throw new MessagingException(e.getMessage(), e);
        }

        return new SeamlessConfirmation(to.toString(), vendor, expectedAt, getHeadersList(content), getHtmlContent(parser));
    }

    private String getHtmlContent(final MimeMessageParser parser) {
        List<String> plainContent = new ArrayList<>(Arrays.asList(parser.getPlainContent().split("\n")));
        while(!plainContent.get(0).startsWith("<")) {
            plainContent.remove(0);
        }
        StringBuilder content = new StringBuilder();
        try {
            InputStream stream = MimeUtility.decode(new ByteArrayInputStream(String.join("\n", plainContent).getBytes()), "quoted-printable");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            while(bufferedReader.ready()) {
                content.append(bufferedReader.readLine())      
                    .append("\n");
            }
            System.out.println("s = " + content);
            return content.toString();
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String findForward(final InputStream inputStream) throws IOException, MessagingException {
        Scanner scanner = new Scanner(inputStream);
        String line;
        StringBuilder builder = new StringBuilder();
        while (!(line = scanner.nextLine()).contains("Forwarded message")) {
        }
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (!line.startsWith("Estimated Delivery")) {
                builder.append("\n");
            } else {
                builder.append(" ");
            }
            builder.append(line);
        }
        return builder.toString().trim();
    }

    private static Map<String, String> getHeadersList(final MimeMessage message) throws MessagingException {
        final Map<String, String> headers = new LinkedHashMap<>();
        final Enumeration allHeaders = message.getAllHeaders();

        while (allHeaders.hasMoreElements()) {
            final Header h = (Header) allHeaders.nextElement();
            headers.put(h.getName(), h.getValue());
        }

        return headers;
    }

}
