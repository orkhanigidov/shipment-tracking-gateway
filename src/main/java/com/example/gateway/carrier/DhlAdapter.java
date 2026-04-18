package com.example.gateway.carrier;

import com.example.gateway.model.Carrier;
import com.example.gateway.model.ShipmentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class DhlAdapter implements CarrierAdapter {

    private static final List<ShipmentStatus> STATUSES = List.of(
            ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.OUT_FOR_DELIVERY,
            ShipmentStatus.DELIVERED
    );

    private static final List<String> LOCATIONS = List.of(
            "Berlin, Germany",
            "Hamburg, Germany",
            "Munich, Germany"
    );

    @Override
    public String getCarrierCode() {
        return Carrier.DHL.name();
    }

    @Override
    public TrackingResult track(String trackingNumber) {
        log.debug("DHL mock tracking for {}", trackingNumber);
        int idx = Math.abs(trackingNumber.hashCode()) % STATUSES.size();
        return new TrackingResult(
                trackingNumber,
                Carrier.DHL,
                STATUSES.get(idx),
                LOCATIONS.get(idx),
                LocalDate.now().plusDays(2).toString(),
                LocalDate.now().toString()
        );
    }
}
