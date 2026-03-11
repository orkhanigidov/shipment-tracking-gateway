package com.example.gateway.service;

public class ShipmentAlreadyExistsException extends RuntimeException {
    public ShipmentAlreadyExistsException(String trackingNumber) {
        super("Shipment already registered: " + trackingNumber);
    }
}
