package com.example.gateway.dto;

public record TokenResponse(
        String token,
        String refreshToken,
        String type
) {
    public TokenResponse(String token, String refreshToken) {
        this(token, refreshToken, "Bearer");
    }
}
