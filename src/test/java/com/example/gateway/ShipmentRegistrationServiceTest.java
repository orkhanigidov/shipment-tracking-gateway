package com.example.gateway;

import com.example.gateway.dto.ShipmentRequest;
import com.example.gateway.model.Carrier;
import com.example.gateway.model.Shipment;
import com.example.gateway.model.ShipmentStatus;
import com.example.gateway.repository.ShipmentRepository;
import com.example.gateway.search.ShipmentIndexer;
import com.example.gateway.service.ShipmentAlreadyExistsException;
import com.example.gateway.service.ShipmentRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShipmentRegistrationServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentIndexer shipmentIndexer;

    @InjectMocks
    private ShipmentRegistrationService registrationService;

    @Test
    void register_ShouldSaveAndIndex() {
        ShipmentRequest request = new ShipmentRequest("DHL123", Carrier.DHL, "Hamburg", "Berlin");

        when(shipmentRepository.existsByTrackingNumber("DHL123")).thenReturn(false);

        Shipment response = registrationService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getTrackingNumber()).isEqualTo("DHL123");
        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.REGISTERED);

        verify(shipmentRepository, times(1)).save(any(Shipment.class));
        verify(shipmentIndexer, times(1)).index(any());
    }

    @Test
    void register_ShouldThrowExceptionWhenAlreadyExists() {
        ShipmentRequest request = new ShipmentRequest("DHL123", Carrier.DHL, null, null);

        when(shipmentRepository.existsByTrackingNumber("DHL123")).thenReturn(true);

        assertThrows(ShipmentAlreadyExistsException.class, () -> registrationService.register(request));

        verify(shipmentRepository, never()).save(any());
        verify(shipmentIndexer, never()).index(any());
    }
}
