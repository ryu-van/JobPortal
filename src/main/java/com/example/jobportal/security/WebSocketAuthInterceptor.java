package com.example.jobportal.security;

import com.example.jobportal.service.JwtService;
import com.example.jobportal.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

/**
 * WebSocket authentication interceptor
 * Validates JWT token during WebSocket handshake
 * Prevents unauthorized WebSocket connections
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String jwt = extractJwtFromCookies(httpRequest);
            
            if (jwt == null) {
                log.warn("WebSocket connection attempt without token from: {}", 
                        httpRequest.getRemoteAddr());
                return false;
            }
            
            try {
                String email = jwtService.extractUsername(jwt);
                CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
                
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    attributes.put("user", authentication);
                    attributes.put("userId", userDetails.getId());
                    
                    log.debug("WebSocket authenticated for user: {}", email);
                    return true;  // Allow connection
                } else {
                    log.warn("Invalid JWT token for WebSocket connection from: {}", 
                            httpRequest.getRemoteAddr());
                    return false;
                }
                
            } catch (Exception e) {
                log.warn("WebSocket authentication failed: {}", e.getMessage());
                return false;  // Reject connection on any error
            }
        }
        
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private String extractJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        
        return Arrays.stream(request.getCookies())
                .filter(cookie -> ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
