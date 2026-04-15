package com.example.gateway.service;

import com.example.gateway.carrier.CarrierAdapter;
import com.example.gateway.carrier.CarrierAdapterRegistry;
import com.example.gateway.carrier.TrackingResult;
import com.example.gateway.dto.ShipmentRequest;
import com.example.gateway.dto.TrackingResponse;
import com.example.gateway.model.Carrier;
import com.example.gateway.model.Shipment;
import com.example.gateway.model.ShipmentStatus;
import com.example.gateway.repository.ShipmentRepository;
import com.example.gateway.search.ShipmentDocument;
import com.example.gateway.search.ShipmentIndexer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentIndexer shipmentIndexer;
    private final CarrierAdapterRegistry registry;

    @Transactional
    public TrackingResponse registerAndTrack(ShipmentRequest request) {
        if (shipmentRepository.findByTrackingNumber(request.getTrackingNumber()).isPresent()) {
            throw new ShipmentAlreadyExistsException(request.getTrackingNumber());
        }

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(request.getTrackingNumber());
        shipment.setCarrier(Carrier.valueOf(request.getCarrier().toUpperCase()));
        shipment.setStatus(ShipmentStatus.REGISTERED);
        shipment.setOrigin(request.getOrigin());
        shipment.setDestination(request.getDestination());
        shipmentRepository.save(shipment);

        shipmentIndexer.index(new ShipmentDocument(
                shipment.getTrackingNumber(),
                shipment.getCarrier().name(),
                shipment.getStatus().name(),
                shipment.getOrigin(),
                shipment.getDestination()
        ));

        log.info("Registered shipment {} via {}", request.getTrackingNumber(), request.getCarrier());
        return fetchTracking(request.getTrackingNumber(), request.getCarrier());
    }

    @Cacheable(value = "tracking", key = "#trackingNumber")
    public TrackingResponse getTracking(String trackingNumber) {
        log.debug("Cache miss for trackingNumber={}", trackingNumber);
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException(trackingNumber));
        return fetchTracking(trackingNumber, shipment.getCarrier().name());
    }

    private TrackingResponse fetchTracking(String trackingNumber, String carrierCode) {
        CarrierAdapter adapter = registry.get(carrierCode);
        TrackingResult result = adapter.track(trackingNumber);
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber).orElseThrow();

        shipment.setStatus(result.getStatus());
        shipmentRepository.save(shipment);

        return new TrackingResponse(
                result.getTrackingNumber(), result.getCarrier(), result.getStatus(),
                result.getCurrentLocation(), result.getEstimatedDelivery(),
                result.getLastUpdate(), shipment.getOrigin(), shipment.getDestination()
        );
    }
}
