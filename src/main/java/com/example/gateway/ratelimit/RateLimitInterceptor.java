package com.example.gateway.ratelimit;

import com.example.gateway.model.Tier;
import com.example.gateway.model.User;
import com.example.gateway.repository.UserRepository;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

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
        Tier tier = userRepository.findByUsername(username)
                .map(User::getTier)
                .orElse(Tier.FREE);

        RateLimitPolicy policy = RateLimitPolicy.resolvePlanFromTier(tier);
        return Bucket.builder().addLimit(policy.getLimit()).build();
    }
}
