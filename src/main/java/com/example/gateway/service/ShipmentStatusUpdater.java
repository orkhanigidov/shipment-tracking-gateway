package com.example.gateway.service;

import com.example.gateway.model.Shipment;
import com.example.gateway.model.ShipmentStatus;
import com.example.gateway.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipmentStatusUpdater {

    private final ShipmentRepository shipmentRepository;

    @Transactional
    protected void updateShipmentStatus(Shipment shipment, ShipmentStatus status) {
        shipment.setStatus(status);
        shipmentRepository.save(shipment);
    }
}
