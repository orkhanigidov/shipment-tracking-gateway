package com.example.gateway;

import com.example.gateway.carrier.CarrierAdapter;
import com.example.gateway.carrier.CarrierAdapterRegistry;
import com.example.gateway.carrier.TrackingResult;
import com.example.gateway.dto.ShipmentRequest;
import com.example.gateway.dto.TrackingResponse;
import com.example.gateway.model.Carrier;
import com.example.gateway.model.Shipment;
import com.example.gateway.model.ShipmentStatus;
import com.example.gateway.repository.ShipmentRepository;
import com.example.gateway.search.ShipmentSearchService;
import com.example.gateway.service.ShipmentAlreadyExistsException;
import com.example.gateway.service.ShipmentNotFoundException;
import com.example.gateway.service.TrackingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrackingServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private ShipmentSearchService searchService;
    @Mock
    private CarrierAdapterRegistry registry;
    @Mock
    private CarrierAdapter carrierAdapter;

    @InjectMocks
    private TrackingService trackingService;

    @Test
    void registerAndTrack_ShouldSaveAndReturnResponse() {
        ShipmentRequest request = new ShipmentRequest();
        request.setTrackingNumber("DHL123");
        request.setCarrier("DHL");
        request.setOrigin("Hamburg");
        request.setDestination("Berlin");

        Shipment savedShipment = new Shipment();
        savedShipment.setTrackingNumber("DHL123");
        savedShipment.setCarrier(Carrier.DHL);
        savedShipment.setOrigin("Hamburg");
        savedShipment.setDestination("Berlin");

        when(shipmentRepository.findByTrackingNumber("DHL123"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(savedShipment));

        when(registry.get("DHL")).thenReturn(carrierAdapter);
        when(carrierAdapter.track("DHL123"))
                .thenReturn(new TrackingResult("DHL123", "DHL", ShipmentStatus.IN_TRANSIT, "Hamburg", LocalDate.now().toString(), LocalDate.now().toString()));

        TrackingResponse response = trackingService.registerAndTrack(request);

        assertThat(response).isNotNull();
        assertThat(response.getTrackingNumber()).isEqualTo("DHL123");
        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);

        verify(shipmentRepository, times(2)).save(any(Shipment.class));
        verify(searchService, times(1)).index(any());
    }

    @Test
    void registerAndTrack_ShouldThrowExceptionWhenAlreadyExists() {
        ShipmentRequest request = new ShipmentRequest();
        request.setTrackingNumber("DHL123");

        when(shipmentRepository.findByTrackingNumber("DHL123")).thenReturn(Optional.of(new Shipment()));

        assertThrows(ShipmentAlreadyExistsException.class, () -> trackingService.registerAndTrack(request));
        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void registerAndTrack_ShouldThrowExceptionWhenNotFound() {
        when(shipmentRepository.findByTrackingNumber("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ShipmentNotFoundException.class, () -> trackingService.getTracking("UNKNOWN"));
    }
}
