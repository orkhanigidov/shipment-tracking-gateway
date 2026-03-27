package com.example.gateway.controller;

import com.example.gateway.search.ShipmentDocument;
import com.example.gateway.search.ShipmentSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shipments/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Full-text shipment search powered by Elasticsearch")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

    private final ShipmentSearchService searchService;

    @Operation(summary = "Search by location")
    @GetMapping("/location")
    public List<ShipmentDocument> byLocation(@RequestParam String q) {
        return searchService.searchByLocation(q);
    }

    @Operation(summary = "Search by carrier")
    @GetMapping("/carrier")
    public List<ShipmentDocument> byCarrier(@RequestParam String q) {
        return searchService.searchByCarrier(q);
    }

    @Operation(summary = "Search by status")
    @GetMapping("/status")
    public List<ShipmentDocument> byStatus(@RequestParam String q) {
        return searchService.searchByStatus(q);
    }
}
