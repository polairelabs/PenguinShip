package com.navaship.api.sendgrid;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendGridEmailService implements EmailService {
    public static final String EMAIL_TEXT_PLAIN_CONTENT_TYPE = "text/plain";
    public static final String EMAIL_TEXT_HTML_CONTENT_TYPE = "text/html";

    @Value("${navaship.app.senderEmail}")
    private String senderEmail;


    @Override
    public void sendText(String to, String subject, String body) throws IOException {
        try {
            sendEmail(senderEmail, to, subject, new Content(EMAIL_TEXT_PLAIN_CONTENT_TYPE, body));
        } catch (IOException ex) {
            throw new IOException("Email failure", ex);
        }
    }

    @Override
    public void sendHTML(String to, String subject, String body) throws IOException {
        try {
            sendEmail(senderEmail, to, subject, new Content(EMAIL_TEXT_HTML_CONTENT_TYPE, body));
        } catch (IOException ex) {
            throw new IOException("Email failure", ex);
        }
    }

    private Response sendEmail(String from, String to, String subject, Content content) throws IOException {
        SendGrid sendGridClient = new SendGrid(System.getenv("SENDGRID_API_KEY"));
        Mail mail = new Mail(new Email(from), subject, new Email(to), content);
        Request request = new Request();
        Response response = null;
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            response = sendGridClient.api(request);
        } catch (IOException ex) {
            throw new IOException("Email failure", ex);
        }
        return response;
    }
}
