package com.example.gateway.controller;

import com.example.gateway.dto.ShipmentRequest;
import com.example.gateway.dto.TrackingResponse;
import com.example.gateway.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/shipments")
    @ResponseStatus(HttpStatus.CREATED)
    public TrackingResponse register(@RequestBody ShipmentRequest request) {
        return trackingService.registerAndTrack(request);
    }

    @GetMapping("/shipments/{trackingNumber}")
    public TrackingResponse track(@PathVariable String trackingNumber) {
        return trackingService.getTracking(trackingNumber);
    }
}
