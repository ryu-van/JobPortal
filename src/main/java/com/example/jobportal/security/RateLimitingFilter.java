package com.example.jobportal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${app.security.rate-limiting.enabled:false}")
    private boolean rateLimitingEnabled;

    @Value("${app.security.rate-limiting.login.capacity:5}")
    private int loginCapacity;

    @Value("${app.security.rate-limiting.login.refill-period:60}")
    private int loginRefillPeriod;

    @Value("${app.security.rate-limiting.register.capacity:3}")
    private int registerCapacity;

    @Value("${app.security.rate-limiting.register.refill-period:300}")
    private int registerRefillPeriod;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        if (!rateLimitingEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String clientId = getClientIdentifier(request);
        
        if (isAuthEndpoint(path)) {
            Bucket bucket = getBucketForEndpoint(path, clientId);
            
            if (!bucket.tryConsume()) {
                log.warn("Rate limit exceeded for client: {}, path: {}", clientId, path);
                sendRateLimitExceeded(response, bucket.getRetryAfterSeconds());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthEndpoint(String path) {
        return path.contains("/auth/login") || 
               path.contains("/auth/register") ||
               path.contains("/auth/refresh") ||
               path.contains("/auth/resend-verification");
    }

    private Bucket getBucketForEndpoint(String path, String clientId) {
        String key;
        int capacity;
        int refillPeriod;
        
        if (path.contains("/auth/login") || path.contains("/auth/refresh")) {
            key = "login:" + clientId;
            capacity = loginCapacity;
            refillPeriod = loginRefillPeriod;
        } else if (path.contains("/auth/register")) {
            key = "register:" + clientId;
            capacity = registerCapacity;
            refillPeriod = registerRefillPeriod;
        } else {
            key = "auth:" + clientId;
            capacity = loginCapacity;
            refillPeriod = loginRefillPeriod;
        }
        
        return buckets.computeIfAbsent(key, k -> new Bucket(capacity, refillPeriod));
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        if (userAgent != null) {
            return ip + ":" + Math.abs(userAgent.hashCode() % 10000);
        }
        return ip;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private void sendRateLimitExceeded(HttpServletResponse response, int retryAfterSeconds) 
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        
        String jsonResponse = String.format(
            "{\"success\":false,\"message\":\"Rate limit exceeded. Please try again in %d seconds.\",\"retryAfter\":%d}",
            retryAfterSeconds, retryAfterSeconds
        );
        
        response.getWriter().write(jsonResponse);
    }


    private static class Bucket {
        private final int capacity;
        private final int refillPeriod;
        private double tokens;
        private Instant lastRefill;

        Bucket(int capacity, int refillPeriod) {
            this.capacity = capacity;
            this.refillPeriod = refillPeriod;
            this.tokens = capacity;
            this.lastRefill = Instant.now();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1) {
                tokens--;
                return true;
            }
            return false;
        }

        synchronized int getRetryAfterSeconds() {
            refill();
            if (tokens >= 1) {
                return 0;
            }
            double tokensNeeded = 1 - tokens;
            double secondsPerToken = (double) refillPeriod / capacity;
            return (int) Math.ceil(tokensNeeded * secondsPerToken);
        }

        private void refill() {
            Instant now = Instant.now();
            long secondsSinceLastRefill = Duration.between(lastRefill, now).getSeconds();
            
            if (secondsSinceLastRefill > 0) {
                double tokensToAdd = (double) secondsSinceLastRefill * capacity / refillPeriod;
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefill = now;
            }
        }
    }
}
