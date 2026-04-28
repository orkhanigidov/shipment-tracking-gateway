package com.example.gateway.carrier;

import com.example.gateway.model.Carrier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CarrierAdapterRegistry {

    private final DhlAdapter dhlAdapter;
    private final FedExAdapter fedExAdapter;
    private final UpsAdapter upsAdapter;

    public CarrierAdapter get(Carrier carrier) {
        return switch (carrier) {
            case DHL -> dhlAdapter;
            case FedEx -> fedExAdapter;
            case UPS -> upsAdapter;
        };
    }
}
