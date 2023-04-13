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
    private String sourceEmail;
    @Value("${navaship.webapp.url}")
    private String webAppUrl;


    public void sendVerificationEmail(String userEmail, String userFirstname, String emailVerificationJwt) throws IOException {
        // Frontend link
        String verifyEmailLink = webAppUrl + "/confirm-account?token=" + emailVerificationJwt;
        Email from = new Email(sourceEmail);
        Email to = new Email(userEmail);
        Content content = new Content("text/html", createVerificationEmailBody(userFirstname, verifyEmailLink));
        Mail mail = new Mail(from, "Welcome to Navaship - Complete Your Registration", to, content);
        sendEmail(mail);
    }

    public void sendPasswordResetEmail(String userEmail, String userFirstname, String passwordResetJwt) throws IOException {
        // Frontend link
        String passwordResetLink = webAppUrl + "/change-password?token=" + passwordResetJwt;
        Email from = new Email(sourceEmail);
        Email to = new Email(userEmail);
        Content content = new Content("text/html", createPasswordResetEmailBody(userFirstname, passwordResetLink));
        Mail mail = new Mail(from, "Navaship Password Reset - Reset Your Password", to, content);
        sendEmail(mail);
    }

    private String createVerificationEmailBody(String userFirstname, String verifyEmailLink) {
        return "Hello " + userFirstname + ",<br><br>"
                + "Thank you for signing up with Navaship. Please click the link below to verify your email address and complete your registration:<br><br>"
                + "<a href=\"" + verifyEmailLink + "\">Verify Your Email</a><br><br>"
                + "Best regards,<br>"
                + "Navaship Team";
    }

    private String createPasswordResetEmailBody(String userFirstname, String passwordResetLink) {
        return "Hello " + userFirstname + ",<br><br>"
                + "We have received a request to reset your password for your Navaship account. Please click the link below to create a new password:<br><br>"
                + "<a href=\"" + passwordResetLink + "\">Reset Your Password</a><br><br>"
                + "If you did not request a password reset, please ignore this email<br><br>"
                + "Best regards,<br>"
                + "Navaship Team";
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
