package com.example.gateway.carrier;

import com.example.gateway.model.ShipmentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class UpsAdapter implements CarrierAdapter {

    private static final List<ShipmentStatus> STATUSES = List.of(
            ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.EXCEPTION,
            ShipmentStatus.OUT_FOR_DELIVERY
    );

    @Override
    public String getCarrierCode() {
        return "UPS";
    }

    @Override
    public TrackingResult track(String trackingNumber) {
        log.debug("UPS mock tracking for {}", trackingNumber);
        int idx = Math.abs(trackingNumber.hashCode()) % STATUSES.size();
        return new TrackingResult(
                trackingNumber,
                "UPS",
                STATUSES.get(idx),
                "Atlanta, USA",
                LocalDate.now().plusDays(3).toString(),
                LocalDate.now().toString()
        );
    }
}
