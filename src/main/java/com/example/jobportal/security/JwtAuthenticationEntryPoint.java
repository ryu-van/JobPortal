package com.example.jobportal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                         AuthenticationException authException) throws IOException, ServletException {
        
        log.warn("Unauthorized access attempt to {} from {}: {}", 
                request.getRequestURI(), 
                request.getRemoteAddr(),
                authException.getMessage());
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", "Access denied. Please login to continue.");
        errorDetails.put("path", request.getRequestURI());
        
        objectMapper.writeValue(response.getOutputStream(), errorDetails);
    }
}
