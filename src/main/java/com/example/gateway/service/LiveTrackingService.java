package com.example.gateway.service;

import com.example.gateway.carrier.CarrierAdapter;
import com.example.gateway.carrier.CarrierAdapterRegistry;
import com.example.gateway.carrier.TrackingResult;
import com.example.gateway.dto.TrackingResponse;
import com.example.gateway.model.Shipment;
import com.example.gateway.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveTrackingService {

    private final ShipmentRepository shipmentRepository;
    private final CarrierAdapterRegistry registry;

    @Cacheable(value = "tracking", key = "#trackingNumber")
    public TrackingResponse getTracking(String trackingNumber) {
        log.debug("Cache miss for trackingNumber={}", trackingNumber);

        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException(trackingNumber));

        CarrierAdapter adapter = registry.get(shipment.getCarrier().name());
        TrackingResult result = adapter.track(trackingNumber);

        shipment.setStatus(result.shipmentStatus());
        shipmentRepository.save(shipment);

        return new TrackingResponse(
                result.trackingNumber(), result.carrier(), result.shipmentStatus(),
                result.currentLocation(), result.estimatedDelivery(),
                result.lastUpdate(), shipment.getOrigin(), shipment.getDestination()
        );
    }
}
