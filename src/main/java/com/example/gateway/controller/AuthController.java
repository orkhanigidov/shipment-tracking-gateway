package com.example.gateway.controller;

import com.example.gateway.dto.RefreshTokenRequest;
import com.example.gateway.dto.TokenRequest;
import com.example.gateway.dto.TokenResponse;
import com.example.gateway.model.User;
import com.example.gateway.repository.UserRepository;
import com.example.gateway.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Obtain and refresh JWT tokens")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Operation(summary = "Issue a JWT token", description = "Validates username and API key, returns an access and refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens issued successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/token")
    public TokenResponse token(@RequestBody TokenRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isEmpty() || !userOptional.get().getApiKey().equals(request.getApiKey())) {
            log.warn("Failed auth attempt for username={}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        log.info("Issued tokens for username={}", request.getUsername());
        return new TokenResponse(jwtUtil.generateToken(request.getUsername()), jwtUtil.generateRefreshToken(request.getUsername()));
    }

    @Operation(summary = "Refresh a JWT token", description = "Provides a new access and refresh token pair using a valid refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken != null && jwtUtil.isValid(refreshToken)) {
            String username = jwtUtil.extractUsername(refreshToken);
            log.info("Refreshed tokens for username={}", username);

            return new TokenResponse(jwtUtil.generateToken(username), jwtUtil.generateRefreshToken(username));
        }

        log.warn("Failed token refresh attempt - invalid or expired token");
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }
}
