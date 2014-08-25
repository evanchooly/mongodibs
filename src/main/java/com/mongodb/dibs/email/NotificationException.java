package com.mongodb.dibs.email;

public class NotificationException extends RuntimeException {
    public NotificationException(final String message) {
        super(message);
    }

    public NotificationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NotificationException(final Throwable cause) {
        super(cause);
    }

    public NotificationException(final String message, final Throwable cause, final boolean enableSuppression,
                                 final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
