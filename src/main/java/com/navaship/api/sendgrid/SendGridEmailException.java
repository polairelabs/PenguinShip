package com.navaship.api.sendgrid;

import java.io.IOException;

public class SendGridEmailException extends RuntimeException {
    public SendGridEmailException(String message, IOException e) {
        super(message, e);
    }
}
