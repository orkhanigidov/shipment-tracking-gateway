package com.example.gateway.dto;

public record TokenRequest(
        String username,
        String apiKey
) {
}
