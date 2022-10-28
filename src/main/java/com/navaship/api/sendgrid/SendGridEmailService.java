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
public class SendGridEmailService {
    @Value("${navaship.app.sendgrid.apikey}")
    private String sendGridApiKey;
    @Value("${navaship.app.sendgrid.senderEmail}")
    private String senderEmail;
    @Value("${navaship.app.sendgrid.verifyAccountEmailTemplateId}")
    private String verifyAccountEmailTemplateId;
    @Value("${navaship.app.sendgrid.forgotPasswordEmailTemplateId}")
    private String passwordResetEmailTemplateId;


    public void sendVerifyAccountEmail(String to, String emailVerificationToken) {
        try {
            Mail mail = new Mail(new Email(senderEmail), "", new Email(to), new Content("text/html", " "));
            mail.setTemplateId(verifyAccountEmailTemplateId);
            // TODO Build URL for token here
            mail.personalization.get(0).addDynamicTemplateData("{{Verify_Account_Link}}", emailVerificationToken);
            sendEmail(mail);
        } catch (IOException e) {
            throw new SendGridEmailException("A problem occurred while sending email", e);
        }
    }

    public void sendPasswordResetEmail(String to, String firstName, String passwordResetToken) {
        try {
            Mail mail = new Mail(new Email(senderEmail), "", new Email(to), new Content("text/html", " "));
            mail.setTemplateId(passwordResetEmailTemplateId);
            // TODO Build URL for token here
            mail.personalization.get(0).addDynamicTemplateData("{{Reset_Password_Link}}", passwordResetToken);
            mail.personalization.get(0).addDynamicTemplateData("{{Sender_Name}}", firstName);
            sendEmail(mail);
        } catch (IOException e) {
            throw new SendGridEmailException("A problem occurred while sending email", e);
        }
    }

    private Response sendEmail(Mail mail) throws IOException {
        SendGrid sendGridClient = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        return sendGridClient.api(request);
    }
}
