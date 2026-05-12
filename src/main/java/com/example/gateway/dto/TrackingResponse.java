package com.example.gateway.dto;

import com.example.gateway.model.Carrier;
import com.example.gateway.model.ShipmentStatus;

import java.io.Serializable;

public record TrackingResponse(
        String trackingNumber,
        Carrier carrier,
        ShipmentStatus status,
        String currentLocation,
        String estimatedDelivery,
        String lastUpdate,
        String origin,
        String destination
) implements Serializable {
}
