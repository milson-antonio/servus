package com.milsondev.servus.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Service
public class EmailSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSenderService.class);
    private final EmailConfigProperties config;

    public EmailSenderService(EmailConfigProperties config) {
        this.config = config;
    }

    private JavaMailSenderImpl createMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getHost());
        sender.setPort(Integer.parseInt(config.getPort()));
        sender.setUsername(config.getUsername());
        sender.setPassword(config.getPassword());

        Properties props = new Properties();
        props.setProperty("mail.smtp.auth", config.getAuth());
        props.setProperty("mail.smtp.starttls.enable", config.getStarttls());
        sender.setJavaMailProperties(props);

        return sender;
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        JavaMailSenderImpl sender = createMailSender();
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            String from = (config.getNoReply() != null && !config.getNoReply().isBlank()) ? config.getNoReply() : config.getUsername();
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            sender.send(message);
            LOGGER.info("Email sent to [{}]", to);
        } catch (MessagingException e) {
            LOGGER.error("Error sending email to [{}]: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}

