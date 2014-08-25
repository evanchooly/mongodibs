package com.mongodb.dibs.email;

import com.mongodb.dibs.Smtp;

import javax.mail.Message;
import javax.mail.MessagingException;

class UpForGrabsWatcher extends EmailWatcher {

    UpForGrabsWatcher() throws MessagingException {
        super(Smtp.upForGrabsEmailAddress(), Smtp.upForGrabsEmailPassword());
    }

    @Override
    protected void process(final Message message) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

}
