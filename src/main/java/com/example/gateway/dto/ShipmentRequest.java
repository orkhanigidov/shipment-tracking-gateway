package com.example.gateway.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentRequest {
    private String trackingNumber;
    private String carrier;
    private String origin;
    private String destination;
}
