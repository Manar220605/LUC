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
}
