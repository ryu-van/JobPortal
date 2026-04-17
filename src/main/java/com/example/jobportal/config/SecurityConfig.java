package com.example.jobportal.config;

import com.example.jobportal.constant.AppConstants;
import com.example.jobportal.security.JwtAuthenticationEntryPoint;
import com.example.jobportal.security.JwtAuthenticationFilter;
import com.example.jobportal.security.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Duration;

/**
 * Security configuration with profile-aware settings
 * Production-ready security headers and CORS configuration
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final RateLimitingFilter rateLimitingFilter;
    private final CorsProperties corsProperties;
    private final CookieProperties cookieProperties;

    @Value("${spring.base-url:/api}")
    private String apiPrefix;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * Main security filter chain for API endpoints
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        String authPath = apiPrefix + "/auth/**";
        String userPath = apiPrefix + "/users/**";
        String jobPath = apiPrefix + "/jobs/**";
        String categoryPath = apiPrefix + "/categories/**";
        String companyPath = apiPrefix + "/companies/**";
        String applicationPath = apiPrefix + "/applications/**";
        String resumePath = apiPrefix + "/resumes/**";
        String notificationPath = apiPrefix + "/notifications/**";

        return http
                .securityMatcher(apiPrefix + "/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .headers(headers -> {
                    // Content Security Policy
                    headers.contentSecurityPolicy(csp -> csp
                            .policyDirectives("default-src 'self'; " +
                                    "script-src 'self' 'unsafe-inline'; " +
                                    "style-src 'self' 'unsafe-inline'; " +
                                    "img-src 'self' data: https:; " +
                                    "font-src 'self'; " +
                                    "connect-src 'self';")
                    );
                    headers.contentTypeOptions(contentTypeOptions -> {});
                    headers.frameOptions(frameOptions -> frameOptions.deny());
                    headers.xssProtection(xss -> xss.disable());  // CSP is preferred
                    if (isProduction()) {
                        headers.httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)  // 1 year
                                .includeSubDomains(true)
                                .preload(true)
                        );
                    }
                    headers.referrerPolicy(referrer -> referrer
                            .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                    );
                    headers.permissionsPolicyHeader(permissions -> permissions
                            .policy("geolocation=(), microphone=(), camera=(), payment=(), usb=(), vr=()")
                    );
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(authPath).permitAll()
                        
                        .requestMatchers(HttpMethod.GET, jobPath).permitAll()
                        .requestMatchers(HttpMethod.GET, categoryPath).permitAll()
                        .requestMatchers(HttpMethod.GET, companyPath).permitAll()
                        
                        .requestMatchers(HttpMethod.POST, applicationPath).hasAnyRole(
                                AppConstants.ROLE_CANDIDATE, AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, applicationPath + "/me/**").hasAnyRole(
                                AppConstants.ROLE_CANDIDATE, AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.POST, resumePath + "/**").hasAnyRole(
                                AppConstants.ROLE_CANDIDATE, AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, resumePath + "/**").hasAnyRole(
                                AppConstants.ROLE_CANDIDATE, AppConstants.ROLE_ADMIN)
                        
                        .requestMatchers(HttpMethod.POST, jobPath).hasAnyRole(
                                AppConstants.ROLE_HR, AppConstants.ROLE_COMPANY_ADMIN, AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, jobPath).hasAnyRole(
                                AppConstants.ROLE_HR, AppConstants.ROLE_COMPANY_ADMIN, AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, jobPath).hasAnyRole(
                                AppConstants.ROLE_HR, AppConstants.ROLE_COMPANY_ADMIN, AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, applicationPath + "/job/**").hasAnyRole(
                                AppConstants.ROLE_HR, AppConstants.ROLE_COMPANY_ADMIN, AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, applicationPath + "/**/status").hasAnyRole(
                                AppConstants.ROLE_HR, AppConstants.ROLE_COMPANY_ADMIN, AppConstants.ROLE_ADMIN)
                        
                        .requestMatchers(apiPrefix + "/companies/**/hr/**").hasAnyRole(
                                AppConstants.ROLE_COMPANY_ADMIN, AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, apiPrefix + "/companies/**").hasAnyRole(
                                AppConstants.ROLE_COMPANY_ADMIN, AppConstants.ROLE_ADMIN)
                        
                        .requestMatchers(apiPrefix + "/admin/**").hasRole(AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, userPath).hasRole(AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, userPath + "/**").hasRole(AppConstants.ROLE_ADMIN)
                        
                        .requestMatchers(HttpMethod.GET, userPath + "/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, userPath + "/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, userPath + "/avatar").authenticated()
                        
                        .requestMatchers(notificationPath + "/**").authenticated()
                        
                        .anyRequest().authenticated()
                )
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = 
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsProperties.toCorsConfiguration());
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private boolean isProduction() {
        return "prod".equals(activeProfile);
    }
}
