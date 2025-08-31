package com.milsondev.servus.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component
public class TokenService {

    private final static Logger LOGGER = LoggerFactory.getLogger(TokenService.class);
    private static final String SECRET_KEY = "chave-super-secreta";
    private static final long EXPIRATION_MILLIS = 30 * 60 * 1000;

    public String generateTokenFromEmail(String email) {
        long timestamp = Instant.now().toEpochMilli();
        String payload = email + "|" + timestamp;
        String signature = hmacSha256(payload, SECRET_KEY);

        String data = payload + "|" + signature;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    public String getEmailFromToken(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|");
            if (parts.length != 3) {
                LOGGER.warn("Invalid token format: {}", token);
                throw new IllegalArgumentException("Invalid token format");
            }

            String email = parts[0];
            long timestamp = Long.parseLong(parts[1]);
            String signature = parts[2];

            // Verifica expiração
            long now = Instant.now().toEpochMilli();
            if (now - timestamp > EXPIRATION_MILLIS) {
                LOGGER.warn("Token expired");
                throw new IllegalArgumentException("Token expired");
            }

            // Verifica assinatura
            String expectedSignature = hmacSha256(email + "|" + timestamp, SECRET_KEY);
            if (!expectedSignature.equals(signature)) {
                LOGGER.warn("Invalid signature: {}", signature);
                throw new IllegalArgumentException("Invalid token signature");
            }

            return email;

        } catch (Exception e) {
            LOGGER.error("Error while decoding token", e);
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            LOGGER.error("Error while creating HMAC", e);
            throw new RuntimeException("Error while creating HMAC", e);
        }
    }
}
