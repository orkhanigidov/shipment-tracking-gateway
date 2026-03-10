package com.example.gateway.controller;

import com.example.gateway.dto.TokenRequest;
import com.example.gateway.dto.TokenResponse;
import com.example.gateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    private static final Map<String, String> API_KEYS = Map.of(
            "alice", "key-alice-001",
            "bob", "key-bob-002"
    );

    @PostMapping("/token")
    public TokenResponse token(@RequestBody TokenRequest request) {
        String expectedKey = API_KEYS.get(request.getUsername());
        if (expectedKey == null || !expectedKey.equals(request.getApiKey())) {
            log.warn("Failed auth attempt for username={}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        log.info("Issued token for username={}", request.getUsername());
        return new TokenResponse(jwtUtil.generateToken(request.getUsername()));
    }
}
