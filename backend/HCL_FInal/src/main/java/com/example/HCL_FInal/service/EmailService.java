package com.example.HCL_FInal.service;

import com.example.HCL_FInal.entity.Order;
import com.example.HCL_FInal.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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

    @Async
    public void sendRegistrationEmail(User user) {
        sendEmail(user.getEmail(),
                "Welcome to Food Ordering System",
                "Hi " + user.getUsername() + ",\n\nYour account has been created successfully!");
    }

    @Async
    public void sendOrderConfirmation(Order order) {
        String body = String.format("Hi %s,\n\nYour order #%d has been placed successfully.\nTotal: ₹%s\nStatus: %s\n\nThank you!",
                order.getUser().getUsername(), order.getId(), order.getTotalAmount(), order.getStatus());
        sendEmail(order.getUser().getEmail(), "Order Confirmation #" + order.getId(), body);
    }

    @Async
    public void sendStatusUpdate(Order order) {
        String body = String.format("Hi %s,\n\nYour order #%d status has been updated to: %s",
                order.getUser().getUsername(), order.getId(), order.getStatus());
        sendEmail(order.getUser().getEmail(), "Order Status Update #" + order.getId(), body);
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
