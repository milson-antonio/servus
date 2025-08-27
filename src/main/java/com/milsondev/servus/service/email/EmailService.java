package com.milsondev.servus.service.email;

import com.milsondev.servus.enu.EmailTemplateType;
import com.milsondev.servus.service.TokenService;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final TokenService tokenService;
    private final EmailTemplateBuilder templateBuilder;
    private final EmailSenderService senderService;
    private final String baseUrl;
    private final String brandName;

    public EmailService(TokenService tokenService,
                        EmailTemplateBuilder templateBuilder,
                        EmailSenderService senderService,
                        Environment env) {
        this.tokenService = tokenService;
        this.templateBuilder = templateBuilder;
        this.senderService = senderService;
        this.baseUrl = env.getProperty("app.base-url", "http://localhost:8080");
        this.brandName = env.getProperty("app.brand-name", "Embassy Appointments");
    }

    @Async
    public void sendEmail(String recipient, EmailTemplateType type) {
        String token = tokenService.generateTokenFromEmail(recipient);
        EmailTemplateBuilder.EmailContent content = templateBuilder.build(type, token, baseUrl, brandName);
        senderService.sendHtmlEmail(recipient, content.getSubject(), content.getHtmlBody());
    }

}
