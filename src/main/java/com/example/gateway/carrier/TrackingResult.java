package com.example.gateway.carrier;

import com.example.gateway.model.Carrier;
import com.example.gateway.model.ShipmentStatus;

public record TrackingResult(
        String trackingNumber,
        Carrier carrier,
        ShipmentStatus shipmentStatus,
        String currentLocation,
        String estimatedDelivery,
        String lastUpdate
) {
}
