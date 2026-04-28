package com.example.gateway.service;

import com.example.gateway.dto.TokenResponse;
import com.example.gateway.repository.UserRepository;
import com.example.gateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse authenticate(String username, String apiKey) {
        userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(apiKey, user.getApiKey()))
                .orElseThrow(() -> {
                    log.warn("Failed auth attempt for username={}", username);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        log.info("Issued tokens for username={}", username);
        return new TokenResponse(jwtUtil.generateToken(username), jwtUtil.generateRefreshToken(username));
    }

    public TokenResponse refreshToken(String refreshToken) {
        if (refreshToken != null && jwtUtil.isValid(refreshToken)) {
            String username = jwtUtil.extractUsername(refreshToken);
            log.info("Refreshed tokens for username={}", username);
            return new TokenResponse(jwtUtil.generateToken(username), jwtUtil.generateRefreshToken(username));
        }

        log.warn("Failed token refresh attempt - invalid or expired token");
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }
}
