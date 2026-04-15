package com.example.gateway.service;

import com.example.gateway.dto.TokenResponse;
import com.example.gateway.model.User;
import com.example.gateway.repository.UserRepository;
import com.example.gateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public TokenResponse authenticate(String username, String apiKey) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty() || !userOptional.get().getApiKey().equals(apiKey)) {
            log.warn("Failed auth attempt for username={}", username);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

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
