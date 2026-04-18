package com.example.gateway.service;

import com.example.gateway.dto.ShipmentRequest;
import com.example.gateway.model.Carrier;
import com.example.gateway.model.Shipment;
import com.example.gateway.model.ShipmentStatus;
import com.example.gateway.repository.ShipmentRepository;
import com.example.gateway.search.ShipmentDocument;
import com.example.gateway.search.ShipmentIndexer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentRegistrationService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentIndexer shipmentIndexer;

    @Transactional
    public Shipment register(ShipmentRequest request) {
        if (shipmentRepository.existsByTrackingNumber(request.trackingNumber())) {
            throw new ShipmentAlreadyExistsException(request.trackingNumber());
        }

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(request.trackingNumber());
        shipment.setCarrier(Carrier.valueOf(request.carrier().name()));
        shipment.setStatus(ShipmentStatus.REGISTERED);
        shipment.setOrigin(request.origin());
        shipment.setDestination(request.destination());
        shipmentRepository.save(shipment);

        shipmentIndexer.index(new ShipmentDocument(
                shipment.getTrackingNumber(),
                shipment.getCarrier().name(),
                shipment.getStatus().name(),
                shipment.getOrigin(),
                shipment.getDestination()
        ));

        log.info("Registered shipment {} via {}", request.trackingNumber(), request.carrier());
        return shipment;
    }
}
