package com.example.gateway.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            // unauthenticated request
            return true;
        }

        Bucket bucket = buckets.computeIfAbsent(username, this::buildBucket);

        if (bucket.tryConsume(1)) {
            response.addHeader("X-Rate-Limit-Remaining",
                    String.valueOf(bucket.getAvailableTokens()));
            return true;
        }

        log.warn("Rate limit exceeded for user={}", username);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("Rate limit exceeded. Try again in a minute.");
        return false;
    }

    private Bucket buildBucket(String username) {
        // 20 requests per minute per user
        Bandwidth limit = Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
