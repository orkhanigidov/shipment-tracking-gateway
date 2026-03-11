package com.example.gateway.carrier;

import com.example.gateway.model.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrackingResult {
    private String trackingNumber;
    private String carrier;
    private ShipmentStatus status;
    private String currentLocation;
    private String estimatedDelivery;
    private String lastUpdate;
}
