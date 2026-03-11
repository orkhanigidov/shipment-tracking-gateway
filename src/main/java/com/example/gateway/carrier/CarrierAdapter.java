package com.example.gateway.carrier;

public interface CarrierAdapter {
    String getCarrierCode();
    TrackingResult track(String trackingNumber);
}
