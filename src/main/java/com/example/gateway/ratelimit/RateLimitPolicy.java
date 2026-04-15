package com.example.gateway.ratelimit;

import com.example.gateway.model.Tier;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import lombok.Getter;

import java.time.Duration;

@Getter
public enum RateLimitPolicy {

    // 10 requests per minute
    FREE(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)))),

    // 100 requests per minute
    PREMIUM(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)))),

    // 1000 requests per minute
    ENTERPRISE(Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofMinutes(1))));

    private final Bandwidth limit;

    RateLimitPolicy(Bandwidth limit) {
        this.limit = limit;
    }

    public static RateLimitPolicy resolvePlanFromTier(Tier tier) {
        return switch (tier) {
            case PREMIUM -> PREMIUM;
            case ENTERPRISE -> ENTERPRISE;
            default -> FREE;
        };
    }
}
