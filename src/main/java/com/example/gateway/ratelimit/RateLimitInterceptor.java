package com.example.gateway.ratelimit;

import com.example.gateway.model.Tier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            // unauthenticated request
            return true;
        }

        String tierStr = (String) request.getAttribute("tier");
        Tier tier = tierStr != null ? Tier.valueOf(tierStr) : Tier.FREE;

        long limit = getLimitForTier(tier);

        long currentMinuteWindow = Instant.now().getEpochSecond() / 60;
        String redisKey = "ratelimit:" + username + ":" + currentMinuteWindow;

        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write("Rate limiting service unavailable.");
            return false;
        }

        if (currentCount == 1) {
            redisTemplate.expire(redisKey, Duration.ofMinutes(2));
        } else {
            Long ttl = redisTemplate.getExpire(redisKey);
            if (ttl == -1) {
                redisTemplate.expire(redisKey, Duration.ofMinutes(2));
            }
        }

        long remainingTokens = limit - currentCount;

        if (remainingTokens < 0) {
            log.warn("Rate limit exceeded for user={}", username);
            response.addHeader("X-Rate-Limit-Remaining", "0");
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Try again in a minute.");
            return false;
        }

        response.addHeader("X-Rate-Limit-Remaining", String.valueOf(remainingTokens));
        return true;
    }

    private long getLimitForTier(Tier tier) {
        return switch (tier) {
            case PREMIUM -> 100;
            case ENTERPRISE -> 1000;
            default -> 10;
        };
    }
}
