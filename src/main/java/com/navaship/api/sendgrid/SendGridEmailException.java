package com.navaship.api.sendgrid;

public class SendGridEmailException extends RuntimeException {

    public SendGridEmailException(String message) {
        super(message);
    }
}
