package com.example.gateway.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentSearchService implements ShipmentIndexer {

    private final ShipmentSearchRepository searchRepository;

    @Override
    public void index(ShipmentDocument document) {
        searchRepository.save(document);
        log.debug("Indexed shipment {} in Elasticsearch", document.getTrackingNumber());
    }

    public List<ShipmentDocument> searchByLocation(String query) {
        return searchRepository.findByOriginContainingOrDestinationContaining(query, query);
    }

    public List<ShipmentDocument> searchByCarrier(String carrier) {
        return searchRepository.findByCarrier(carrier.toUpperCase());
    }

    public List<ShipmentDocument> searchByStatus(String status) {
        return searchRepository.findByStatus(status.toUpperCase());
    }
}
