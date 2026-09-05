package com.luc.qa.common.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${luc.mail.from:LUC <noreply@luc.local>}")
    private String fromAddress;

    @Value("${luc.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public void sendStudentLookupCode(String toEmail, String firstName, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your LUC account activation code");
        message.setText("""
            Hello %s,

            Your LUC account activation code is: %s

            It expires in 10 minutes. If you did not request this, you can ignore this email.
            """.formatted(firstName, code));
        mailSender.send(message);
    }

    public void sendAlumniVerificationApproved(String toEmail, String displayName) {
        String verifyUrl = frontendUrl.replaceAll("/$", "") + "/alumni/verify";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your LUC alumni verification was approved");
        message.setText("""
            Hello %s,

            Your alumni verification request on Lebanese University Connect has been approved.

            Your profile now shows the Alumni badge. You can review your status here:
            %s

            Welcome to the LUC alumni community.
            """.formatted(displayName, verifyUrl));
        mailSender.send(message);
    }

    public void sendAlumniVerificationRejected(String toEmail, String displayName, String reason) {
        String verifyUrl = frontendUrl.replaceAll("/$", "") + "/alumni/verify";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your LUC alumni verification was not approved");
        message.setText("""
            Hello %s,

            Your alumni verification request on Lebanese University Connect was not approved.

            Reason from the reviewer:
            %s

            You can update your details and submit again here:
            %s
            """.formatted(displayName, reason, verifyUrl));
        mailSender.send(message);
    }
}
