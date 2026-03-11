package com.example.gateway.service;

public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(String trackingNumber) {
        super("Shipment not found: " + trackingNumber);
    }
}
