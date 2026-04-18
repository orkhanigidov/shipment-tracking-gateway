package com.example.gateway;

import com.example.gateway.dto.ShipmentRequest;
import com.example.gateway.model.Carrier;
import com.example.gateway.search.ShipmentDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchControllerTest extends BaseIntegrationTest {

    @BeforeEach
    void setupData() throws InterruptedException {
        ShipmentRequest shipmentReq = new ShipmentRequest("ES-SEARCH-001", Carrier.DHL, "Hamburg", "Berlin");

        restTemplate.exchange("/shipments", HttpMethod.POST, new HttpEntity<>(shipmentReq, authHeaders()), String.class);

        // Short delay to allow Elasticsearch to index the data (Near Real-Time)
        Thread.sleep(1500);
    }

    @Test
    void shouldSearchByLocation() {
        ResponseEntity<ShipmentDocument[]> response = restTemplate.exchange(
                "/shipments/search/location?q=Hamburg", HttpMethod.GET, new HttpEntity<>(authHeaders()), ShipmentDocument[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(1);
        assertThat(response.getBody()[0].getOrigin()).contains("Hamburg");
    }

    @Test
    void shouldSearchByCarrier() {
        ResponseEntity<ShipmentDocument[]> response = restTemplate.exchange(
                "/shipments/search/carrier?q=DHL", HttpMethod.GET, new HttpEntity<>(authHeaders()), ShipmentDocument[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(1);
        assertThat(response.getBody()[0].getCarrier()).isEqualTo("DHL");
    }

    @Test
    void shouldSearchByStatus() {
        ResponseEntity<ShipmentDocument[]> response = restTemplate.exchange(
                "/shipments/search/status?q=REGISTERED", HttpMethod.GET, new HttpEntity<>(authHeaders()), ShipmentDocument[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(1);
        assertThat(response.getBody()[0].getStatus()).isEqualTo("REGISTERED");
    }
}
