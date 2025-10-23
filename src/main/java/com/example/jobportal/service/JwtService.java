package com.example.jobportal.service;

import com.example.jobportal.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.jwt.access-expiration}")
    private Long accessTokenExpiration;

    @Value("${spring.jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername(), accessTokenExpiration);
    }

    public String generateAccessToken(User user) {
        return generateToken(user.getEmail(), accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername(), refreshTokenExpiration);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user.getEmail(), refreshTokenExpiration);
    }

    private String generateToken(String subject, Long expiration) {
        Date date = new Date();
        Date expiryDate = new Date(date.getTime() + expiration);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(date)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    public Boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}