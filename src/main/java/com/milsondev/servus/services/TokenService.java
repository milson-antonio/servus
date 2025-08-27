package com.milsondev.servus.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.zip.CRC32;

@Component
public class TokenService {

    private final static Logger LOGGER = LoggerFactory.getLogger(TokenService.class);

    public String generateTokenFromEmail(String email) {
        CRC32 crc = new CRC32();
        crc.update(email.getBytes());
        String checksum = Long.toHexString(crc.getValue());

        String data = email + "|" + checksum;
        return Base64.getUrlEncoder()
                .encodeToString(data.getBytes())
                .replace("=", "");
    }

    public String getEmailFromToken(String token) {
        String padded = token;
        switch (token.length() % 4) {
            case 2: padded += "=="; break;
            case 3: padded += "="; break;
        }

        String data = new String(Base64.getUrlDecoder().decode(padded));
        String[] parts = data.split("\\|");
        if (parts.length != 2) {
            LOGGER.error("Invalid Token");
        }

        CRC32 crc = new CRC32();
        crc.update(parts[0].getBytes());
        String checksum = Long.toHexString(crc.getValue());

        if (!checksum.equals(parts[1])) {
            LOGGER.error("Corrupted Token");
        }

        return parts[0];
    }

}
