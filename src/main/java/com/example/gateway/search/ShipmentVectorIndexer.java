package com.example.gateway.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentVectorIndexer {

    private final VectorStore vectorStore;

    public void index(ShipmentDocument shipment) {
        // Build a natural-language sentence that will be embedded.
        // The more descriptive this text, the better semantic search works.
        String content = String.format(
                "Shipment %s via %s from %s to %s, status: %s",
                shipment.trackingNumber(),
                shipment.carrier(),
                shipment.origin(),
                shipment.destination(),
                shipment.status()
        );

        Document doc = new Document(content, Map.of(
                "trackingNumber", shipment.trackingNumber(),
                "carrier", shipment.carrier(),
                "origin", shipment.origin(),
                "destination", shipment.destination(),
                "status", shipment.status())
        );

        vectorStore.add(List.of(doc));
        log.debug("Vector-indexed shipment {}", shipment.trackingNumber());
    }
}
