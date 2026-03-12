package com.example.gateway;

import com.example.gateway.dto.ShipmentRequest;
import com.example.gateway.dto.TokenRequest;
import com.example.gateway.dto.TokenResponse;
import com.example.gateway.dto.TrackingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class TrackingControllerTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:alpine")
            .withDatabaseName("gatewaydb")
            .withUsername("gateway")
            .withPassword("gateway");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.cache.type", () -> "none");
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
        registry.add("spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    private String bearerToken;

    @BeforeEach
    void obtainToken() {
        TokenRequest req = new TokenRequest();
        req.setUsername("alice");
        req.setApiKey("key-alice-001");
        TokenResponse resp = restTemplate.postForEntity("/auth/token", req, TokenResponse.class).getBody();
        bearerToken = "Bearer " + resp.getToken();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void registerShipment_shouldReturnCreated() {
        ShipmentRequest req = new ShipmentRequest();
        req.setTrackingNumber("TRACK123");
        req.setCarrier("DHL");
        req.setOrigin("Hamburg");
        req.setDestination("Berlin");

        ResponseEntity<TrackingResponse> response = restTemplate.exchange(
                "/shipments", HttpMethod.POST,
                new HttpEntity<>(req, authHeaders()), TrackingResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getTrackingNumber()).isEqualTo("TRACK123");
        assertThat(response.getBody().getStatus()).isNotNull();
    }

    @Test
    void getTracking_shouldReturnShipmentStatus() {
        ShipmentRequest req = new ShipmentRequest();
        req.setTrackingNumber("TRACK456");
        req.setCarrier("UPS");
        req.setOrigin("Munich");
        req.setDestination("Frankfurt");
        restTemplate.exchange("/shipments", HttpMethod.POST,
                new HttpEntity<>(req, authHeaders()), TrackingResponse.class);

        ResponseEntity<TrackingResponse> response = restTemplate.exchange(
                "/shipments/TRACK456", HttpMethod.GET,
                new HttpEntity<>(authHeaders()), TrackingResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCarrier()).isEqualTo("UPS");
    }

    @Test
    void registerShipment_withoutToken_shouldReturn403() {
        ShipmentRequest req = new ShipmentRequest();
        req.setTrackingNumber("TRACK789");
        req.setCarrier("FedEx");

        ResponseEntity<String> response = restTemplate.postForEntity("/shipments", req, String.class);
        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void auth_withWrongApiKey_shouldReturn401() {
        TokenRequest req = new TokenRequest();
        req.setUsername("alice");
        req.setApiKey("wrong-key");

        ResponseEntity<String> response = restTemplate.postForEntity("/auth/token", req, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
