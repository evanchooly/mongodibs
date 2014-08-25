package com.mongodb.dibs.email;

import com.mongodb.dibs.Smtp;

import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;

class NotificationsWatcher extends EmailWatcher {
    private final SeamlessConfirmationEmailListener emailListener;

    NotificationsWatcher(final SeamlessConfirmationEmailListener emailListener) throws MessagingException {
        super(Smtp.notificationsEmailAddress(), Smtp.notificationsEmailPassword());
        this.emailListener = emailListener;
    }

    @Override
    protected void process(final Message message) throws MessagingException {
        emailListener.deliver(message);
        message.setFlag(Flag.DELETED, true);
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
