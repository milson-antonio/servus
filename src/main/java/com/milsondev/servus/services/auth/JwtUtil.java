package com.milsondev.servus.services.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    // NOTE: For production, externalize and use at least 256-bit random secret.
    private static final String SECRET = "servusSecretKey-servusSecretKey-servusSecretKey-256bit";
    private static final long EXPIRATION_MS = 1000 * 60 * 60; // 1 hour

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String generateToken(UserDetails userDetails, int tokenVersion) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRATION_MS);
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
                .claim("ver", tokenVersion)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails, int currentTokenVersion) {
        String username = extractUsername(token);
        if (!username.equals(userDetails.getUsername()) || isTokenExpired(token)) {
            return false;
        }
        Integer ver = (Integer) Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("ver", Integer.class);
        return ver != null && ver == currentTokenVersion;
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getExpiration();
        return expiration.before(new Date());
    }
}
