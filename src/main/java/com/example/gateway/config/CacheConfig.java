package com.example.gateway.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // TTL is configured in application.yml (spring.cache.redis.time-to-live = 5 minutes)
}
