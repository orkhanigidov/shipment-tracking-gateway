package com.example.gateway.dto;

import com.example.gateway.model.Carrier;

public record ShipmentRequest(
        String trackingNumber,
        Carrier carrier,
        String origin,
        String destination
) {
}
