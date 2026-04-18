package com.example.gateway.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentSearchRepository extends ElasticsearchRepository<ShipmentDocument, String> {
    List<ShipmentDocument> findByOriginContainingOrDestinationContaining(String origin, String destination);

    List<ShipmentDocument> findByCarrier(String carrier);

    List<ShipmentDocument> findByStatus(String status);
}
