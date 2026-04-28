package com.example.gateway.carrier;

public sealed interface CarrierAdapter permits DhlAdapter, FedExAdapter, UpsAdapter {
    String getCarrierCode();

    TrackingResult track(String trackingNumber);
}
