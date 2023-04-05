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
    @Value("${sendgrid.apikey}")
    private String sendGridApiKey;
    @Value("${sendgrid.senderEmail}")
    private String senderEmail;
    @Value("${sendgrid.verifyEmailTemplateId}")
    private String verifyAccountEmailTemplateId;
    @Value("${sendgrid.forgotPasswordEmailTemplateId}")
    private String passwordResetEmailTemplateId;
    @Value("${navaship.webapp.url}")
    private String webAppUrl;


    public void sendVerifyAccountEmail(String to, String emailVerificationJwt) {
        try {
            Mail mail = new Mail(new Email(senderEmail), "", new Email(to), new Content("text/html", " "));
            mail.setTemplateId(verifyAccountEmailTemplateId);
            String verifyEmailLink = webAppUrl + "/verify-email/" + emailVerificationJwt;
            mail.personalization.get(0).addDynamicTemplateData("{{Verify_Account_Link}}", verifyEmailLink);
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
