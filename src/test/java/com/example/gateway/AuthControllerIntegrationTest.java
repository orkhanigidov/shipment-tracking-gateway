package com.example.gateway;

import com.example.gateway.dto.TokenRequest;
import com.example.gateway.dto.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void token_ShouldReturnJwt_WhenUsingCorrectPlaintextKeyAgainstDatabaseHash() {
        TokenRequest req = new TokenRequest("alice", "key-alice-001");

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity("/auth/token", req, TokenResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
    }

    @Test
    void token_ShouldReturn401_WhenUsingIncorrectPlaintextKey() {
        TokenRequest req = new TokenRequest("alice", "invalid-key-attempt");

        ResponseEntity<String> response = restTemplate.postForEntity("/auth/token", req, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
