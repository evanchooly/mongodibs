package com.mongodb.dibs.email;

import com.mongodb.dibs.Smtp;

import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;

class UpForGrabsWatcher extends EmailWatcher {

    private final SeamlessConfirmationEmailListener emailListener;

    UpForGrabsWatcher(final SeamlessConfirmationEmailListener emailListener) throws MessagingException {
        super(Smtp.upForGrabsEmailAddress(), Smtp.upForGrabsEmailPassword());
        this.emailListener = emailListener;
    }

    @Override
    protected void process(final Message message) throws MessagingException {
        emailListener.deliver(message, true);
        message.setFlag(Flag.DELETED, true);
    }

}
