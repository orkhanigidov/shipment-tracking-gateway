package com.example.gateway.controller;

import com.example.gateway.dto.RefreshTokenRequest;
import com.example.gateway.dto.TokenRequest;
import com.example.gateway.dto.TokenResponse;
import com.example.gateway.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Obtain and refresh JWT tokens")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Issue a JWT token", description = "Validates username and API key, returns an access and refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens issued successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/token")
    public TokenResponse token(@RequestBody TokenRequest request) {
        return authService.authenticate(request.getUsername(), request.getApiKey());
    }

    @Operation(summary = "Refresh a JWT token", description = "Provides a new access and refresh token pair using a valid refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request.getRefreshToken());
    }
}
