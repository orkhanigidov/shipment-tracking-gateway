package com.example.gateway.controller;

import com.example.gateway.dto.ShipmentRequest;
import com.example.gateway.dto.TrackingResponse;
import com.example.gateway.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Shipment Tracking", description = "Register shipments and retrieve tracking information")
public class TrackingController {

    private final TrackingService trackingService;

    @Operation(summary = "Register a shipment", description = "Creates a new shipment record and returns initial tracking info")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Shipment registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded (20 req/min per user)")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/shipments")
    @ResponseStatus(HttpStatus.CREATED)
    public TrackingResponse register(@RequestBody ShipmentRequest request) {
        return trackingService.registerAndTrack(request);
    }

    @Operation(summary = "Track a shipment", description = "Returns cached (Redis, TTL 5 min) tracking status for the given tracking number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tracking info returned"),
            @ApiResponse(responseCode = "404", description = "Tracking number not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded (20 req/min per user)")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/shipments/{trackingNumber}")
    public TrackingResponse track(
            @Parameter(description = "Carrier-specific tracking number, e.g. DHL123456789")
            @PathVariable String trackingNumber) {
        return trackingService.getTracking(trackingNumber);
    }
}
