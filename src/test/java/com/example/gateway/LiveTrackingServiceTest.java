package com.example.gateway;

import com.example.gateway.carrier.CarrierAdapter;
import com.example.gateway.carrier.CarrierAdapterRegistry;
import com.example.gateway.carrier.TrackingResult;
import com.example.gateway.dto.TrackingResponse;
import com.example.gateway.model.Carrier;
import com.example.gateway.model.Shipment;
import com.example.gateway.model.ShipmentStatus;
import com.example.gateway.repository.ShipmentRepository;
import com.example.gateway.service.LiveTrackingService;
import com.example.gateway.service.ShipmentNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LiveTrackingServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private CarrierAdapterRegistry registry;
    @Mock
    private CarrierAdapter carrierAdapter;

    @InjectMocks
    private LiveTrackingService trackingService;

    @Test
    void getTracking_ShouldReturnResponseAndUpdateStatus() {
        Shipment savedShipment = new Shipment();
        savedShipment.setTrackingNumber("DHL123");
        savedShipment.setCarrier(Carrier.DHL);
        savedShipment.setOrigin("Hamburg");
        savedShipment.setDestination("Berlin");
        savedShipment.setStatus(ShipmentStatus.REGISTERED);

        when(shipmentRepository.findByTrackingNumber("DHL123")).thenReturn(Optional.of(savedShipment));
        when(registry.get("DHL")).thenReturn(carrierAdapter);
        when(carrierAdapter.track("DHL123"))
                .thenReturn(new TrackingResult("DHL123", "DHL", ShipmentStatus.IN_TRANSIT, "Frankfurt", LocalDate.now().toString(), LocalDate.now().toString()));

        TrackingResponse response = trackingService.getTracking("DHL123");

        assertThat(response).isNotNull();
        assertThat(response.getTrackingNumber()).isEqualTo("DHL123");
        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);

        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    void getTracking_ShouldThrowExceptionWhenNotFound() {
        when(shipmentRepository.findByTrackingNumber("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ShipmentNotFoundException.class, () -> trackingService.getTracking("UNKNOWN"));
    }
}
