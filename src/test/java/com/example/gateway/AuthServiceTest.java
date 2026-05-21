package com.example.gateway;

import com.example.gateway.dto.TokenResponse;
import com.example.gateway.model.Tier;
import com.example.gateway.model.User;
import com.example.gateway.repository.UserRepository;
import com.example.gateway.security.JwtUtil;
import com.example.gateway.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticate_ShouldReturnTokens_WhenCryptographicMatchSucceeds() {
        User user = new User();
        user.setUsername("alice");
        user.setApiKey("$2a$12$mocked_bcrypt_hash_string");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("raw-secret-key", "$2a$12$mocked_bcrypt_hash_string")).thenReturn(true);
        when(jwtUtil.generateToken("alice", any(Tier.class))).thenReturn("mock-access-token");
        when(jwtUtil.generateRefreshToken("alice", any(Tier.class))).thenReturn("mock-refresh-token");

        TokenResponse response = authService.authenticate("alice", "raw-secret-key");

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mock-access-token");
        verify(passwordEncoder, times(1)).matches("raw-secret-key", "$2a$12$mocked_bcrypt_hash_string");
    }

    @Test
    void authenticate_ShouldThrowUnauthorized_WhenCryptographicMatchFails() {
        User user = new User();
        user.setUsername("alice");
        user.setApiKey("$2a$12$mocked_bcrypt_hash_string");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong-key", "$2a$12$mocked_bcrypt_hash_string")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> authService.authenticate("alice", "wrong-key"));

        verify(jwtUtil, never()).generateToken(anyString(), any(Tier.class));
    }
}
