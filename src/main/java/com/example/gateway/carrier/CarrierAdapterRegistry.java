package com.example.gateway.carrier;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CarrierAdapterRegistry {

    private final Map<String, CarrierAdapter> adapters;

    public CarrierAdapterRegistry(List<CarrierAdapter> adapterList) {
        this.adapters = adapterList.stream()
                .collect(Collectors.toMap(CarrierAdapter::getCarrierCode, Function.identity()));
    }

    public CarrierAdapter get(String carrierCode) {
        CarrierAdapter adapter = adapters.get(carrierCode.toUpperCase());
        if (adapter == null) {
            throw new IllegalArgumentException("Unknown carrier: " + carrierCode);
        }
        return adapter;
    }
}
