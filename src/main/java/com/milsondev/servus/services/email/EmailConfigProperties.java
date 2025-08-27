package com.milsondev.servus.services.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "email.config")
@Data
public class EmailConfigProperties {
    private String host;
    private String port;
    private String username;
    private String password;
    private String auth;
    private String starttls;
    private String noReply;
}