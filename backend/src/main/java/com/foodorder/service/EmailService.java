package com.foodorder.service;

import com.foodorder.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:no-reply@foodorder.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Async methods receive only plain values. Passing JPA entities into an @Async method
    // would cause lazy-loading on a different thread than the originating transaction, which
    // corrupts the Hibernate session state ("Illegal pop() with non-matching
    // JdbcValuesSourceProcessingState"). Callers must materialise everything needed first.

    @Async
    public void sendRegistrationEmail(String toEmail, String username) {
        sendEmail(toEmail,
                "Welcome to Food Ordering System",
                "Hi " + username + ",\n\nYour account has been created successfully!");
    }

    @Async
    public void sendOrderConfirmation(String toEmail, String username, Long orderId,
                                      BigDecimal total, OrderStatus status) {
        String body = String.format("Hi %s,\n\nYour order #%d has been placed successfully.\nTotal: %s\nStatus: %s\n\nThank you!",
                username, orderId, total, status);
        sendEmail(toEmail, "Order Confirmation #" + orderId, body);
    }

    @Async
    public void sendStatusUpdate(String toEmail, String username, Long orderId, OrderStatus status) {
        String body = String.format("Hi %s,\n\nYour order #%d status has been updated to: %s",
                username, orderId, status);
        sendEmail(toEmail, "Order Status Update #" + orderId, body);
    }

    private void sendEmail(String to, String subject, String body) {
        if (!mailEnabled) {
            logger.info("[MOCK EMAIL] To: {}, Subject: {}\n{}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            logger.info("Email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
