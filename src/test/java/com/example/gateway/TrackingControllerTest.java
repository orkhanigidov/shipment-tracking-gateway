package com.example.gateway;

import com.example.gateway.dto.ShipmentRequest;
import com.example.gateway.dto.TokenRequest;
import com.example.gateway.dto.TrackingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class TrackingControllerTest extends BaseIntegrationTest {

    @Test
    void registerShipment_shouldReturnCreated() {
        ShipmentRequest req = new ShipmentRequest();
        req.setTrackingNumber("TRACK123");
        req.setCarrier("DHL");
        req.setOrigin("Hamburg");
        req.setDestination("Berlin");

        ResponseEntity<TrackingResponse> response = restTemplate.exchange(
                "/shipments", HttpMethod.POST, new HttpEntity<>(req, authHeaders()), TrackingResponse.class);

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
        restTemplate.exchange("/shipments", HttpMethod.POST, new HttpEntity<>(req, authHeaders()), TrackingResponse.class);

        ResponseEntity<TrackingResponse> response = restTemplate.exchange(
                "/shipments/TRACK456", HttpMethod.GET, new HttpEntity<>(authHeaders()), TrackingResponse.class);

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
