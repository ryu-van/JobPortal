package com.example.jobportal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;


@Configuration
@ConfigurationProperties(prefix = "app.security.cors")
@Getter
@Setter
public class CorsProperties {
    

    private List<String> allowedOrigins = List.of("http://localhost:5173");

    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
    

    private List<String> allowedHeaders = List.of(
        "Authorization",
        "Content-Type",
        "Accept",
        "Origin",
        "X-Requested-With",
        "Access-Control-Request-Method",
        "Access-Control-Request-Headers"
    );
    

    private List<String> exposedHeaders = List.of("Authorization", "Content-Disposition");
    

    private boolean allowCredentials = true;
    

    private long maxAge = 3600;
    

    public CorsConfiguration toCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(allowedMethods);
        config.setAllowedHeaders(allowedHeaders);
        config.setExposedHeaders(exposedHeaders);
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAge);
        return config;
    }
}
