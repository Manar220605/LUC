package com.luc.qa.common.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${luc.mail.from:LUC <noreply@luc.local>}")
    private String fromAddress;

    @Value("${luc.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public void sendStudentCredentials(String toEmail, String fullName, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your Lebanese University Connect account");
        message.setText("""
            Hello %s,

            Your LUC student account has been created and verified.

            Sign in at: %s/auth/signin
            Email: %s
            Temporary password: %s

            Please sign in and change your password after your first login.

            If you did not request this account, you can ignore this email.
            """.formatted(fullName, frontendUrl, toEmail, password));
        mailSender.send(message);
    }
}
