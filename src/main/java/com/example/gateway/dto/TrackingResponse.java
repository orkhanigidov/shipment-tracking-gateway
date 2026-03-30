package com.example.gateway.dto;

import com.example.gateway.model.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrackingResponse {
    private String trackingNumber;
    private String carrier;
    private ShipmentStatus status;
    private String currentLocation;
    private String estimatedDelivery;
    private String lastUpdate;
    private String origin;
    private String destination;
}
