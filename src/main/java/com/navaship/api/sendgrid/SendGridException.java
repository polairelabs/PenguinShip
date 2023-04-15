package com.navaship.api.sendgrid;

import java.io.IOException;

public class SendGridException extends RuntimeException {
    public SendGridException(String message, IOException e) {
        super(message, e);
    }
}
