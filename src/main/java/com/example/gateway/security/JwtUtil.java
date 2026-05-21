package com.example.gateway.security;

import com.example.gateway.model.Tier;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private Duration expiration;

    @Value("${app.jwt.refresh-expiration}")
    private Duration refreshExpiration;

    private String buildToken(String username, Tier tier, Duration duration) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .subject(username)
                .claim("tier", tier)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + duration.toMillis()))
                .signWith(key)
                .compact();
    }

    public String generateToken(String username, Tier tier) {
        return buildToken(username, tier, expiration);
    }

    public String generateRefreshToken(String username, Tier tier) {
        return buildToken(username, tier, refreshExpiration);
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Tier extractTier(String token) {
        return (Tier) parseClaims(token).get("tier");
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
