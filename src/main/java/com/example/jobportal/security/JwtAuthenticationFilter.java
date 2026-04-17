package com.example.jobportal.security;

import com.example.jobportal.config.CookieProperties;
import com.example.jobportal.service.JwtService;
import com.example.jobportal.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final CookieProperties cookieProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
        String jwt = extractJwtFromCookies(request);
        
        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(jwt, request, response);
        }
        
        filterChain.doFilter(request, response);
    }


    private String extractJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieProperties.getAccessTokenName().equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }


    private void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(cookieProperties.isHttpOnly())
                .secure(cookieProperties.isSecure())
                .path(cookieProperties.getPath())
                .maxAge(0)
                .sameSite(cookieProperties.getSameSiteValue())
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }


    public void addTokenCookie(HttpServletResponse response, String name, String token, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, token)
                .httpOnly(cookieProperties.isHttpOnly())
                .secure(cookieProperties.isSecure())
                .path(cookieProperties.getPath())
                .maxAge(maxAge)
                .sameSite(cookieProperties.getSameSiteValue())
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        
        log.debug("Added cookie: {} (secure={}, sameSite={})", 
                name, cookieProperties.isSecure(), cookieProperties.getSameSiteValue());
    }


    public static void addTokenCookieStatic(HttpServletResponse response, String name, 
                                            String token, Duration maxAge, boolean secure, 
                                            String sameSite) {
        ResponseCookie cookie = ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }


    private void authenticate(String jwt, HttpServletRequest request, HttpServletResponse response) {
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
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Authenticated user: {}, URI: {}", email, request.getRequestURI());
            } else {
                log.warn("Invalid JWT token for user: {}", email);
                deleteCookie(response, cookieProperties.getAccessTokenName());
            }
            
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
            deleteCookie(response, cookieProperties.getAccessTokenName());
            
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            deleteCookie(response, cookieProperties.getAccessTokenName());
            
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage(), e);
            deleteCookie(response, cookieProperties.getAccessTokenName());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip authentication for public endpoints to avoid unnecessary processing
        String path = request.getRequestURI();
        return path.contains("/auth/") && 
               (path.contains("/login") || path.contains("/register"));
    }
}
