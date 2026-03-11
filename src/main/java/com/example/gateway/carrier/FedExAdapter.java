package com.example.gateway.carrier;

import com.example.gateway.model.ShipmentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class FedExAdapter implements CarrierAdapter {

    private static final List<ShipmentStatus> STATUSES = List.of(
            ShipmentStatus.REGISTERED,
            ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.DELIVERED
    );

    @Override
    public String getCarrierCode() {
        return "FedEx";
    }

    @Override
    public TrackingResult track(String trackingNumber) {
        log.debug("FedEx mock tracking for {}", trackingNumber);
        int idx = Math.abs(trackingNumber.hashCode()) % STATUSES.size();
        return new TrackingResult(
                trackingNumber,
                "FedEx",
                STATUSES.get(idx),
                "New York, USA",
                LocalDate.now().plusDays(1).toString(),
                LocalDate.now().toString()
        );
    }
}
