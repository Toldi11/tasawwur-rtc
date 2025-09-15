package com.tasawwur.rtc.api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * Service for generating and validating JWT tokens for RTC signaling authentication.
 * 
 * This service creates secure, time-limited tokens that allow SDK users to connect
 * to the signaling server and join specific channels.
 */
@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${tasawwur.rtc.jwt.secret}")
    private String jwtSecret;

    @Value("${tasawwur.rtc.jwt.expiration:86400}") // 24 hours default
    private long jwtExpirationSeconds;

    /**
     * Generate a signaling token for a specific user, app, and channel.
     * 
     * @param appId The application ID
     * @param appSecret The application secret (for validation)
     * @param channelName The channel name the user wants to join
     * @param userId The user ID
     * @param expirationSeconds Custom expiration time (optional)
     * @return JWT token string
     */
    public String generateSignalingToken(String appId, String appSecret, 
                                       String channelName, String userId, 
                                       Long expirationSeconds) {
        try {
            long expiration = expirationSeconds != null ? expirationSeconds : jwtExpirationSeconds;
            Instant expiryTime = Instant.now().plusSeconds(expiration);
            
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            String token = Jwts.builder()
                    .setSubject(userId)
                    .setIssuer("tasawwur-rtc-api")
                    .setAudience("tasawwur-rtc-signaling")
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(expiryTime))
                    .addClaims(Map.of(
                        "appId", appId,
                        "channelName", channelName,
                        "tokenType", "signaling"
                    ))
                    .signWith(key)
                    .compact();
            
            logger.info("Generated signaling token for appId: {}, channel: {}, user: {}, expires: {}", 
                       appId, channelName, userId, expiryTime);
            
            return token;
            
        } catch (Exception e) {
            logger.error("Error generating signaling token for appId: {}, channel: {}, user: {}: {}", 
                        appId, channelName, userId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate signaling token", e);
        }
    }

    /**
     * Generate a general API access token for REST API authentication.
     * 
     * @param userId The user ID
     * @param userEmail The user email
     * @param expirationSeconds Custom expiration time (optional)
     * @return JWT token string
     */
    public String generateApiToken(Long userId, String userEmail, Long expirationSeconds) {
        try {
            long expiration = expirationSeconds != null ? expirationSeconds : jwtExpirationSeconds;
            Instant expiryTime = Instant.now().plusSeconds(expiration);
            
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            String token = Jwts.builder()
                    .setSubject(userId.toString())
                    .setIssuer("tasawwur-rtc-api")
                    .setAudience("tasawwur-rtc-api")
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(expiryTime))
                    .addClaims(Map.of(
                        "email", userEmail,
                        "tokenType", "api"
                    ))
                    .signWith(key)
                    .compact();
            
            logger.info("Generated API token for user: {}, expires: {}", userId, expiryTime);
            
            return token;
            
        } catch (Exception e) {
            logger.error("Error generating API token for user: {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate API token", e);
        }
    }

    /**
     * Validate and parse a JWT token.
     * 
     * @param token The JWT token string
     * @return Claims if token is valid
     * @throws RuntimeException if token is invalid or expired
     */
    public Claims validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            logger.debug("Token validated successfully for subject: {}", claims.getSubject());
            return claims;
            
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired token", e);
        }
    }

    /**
     * Extract user ID from a token.
     * 
     * @param token The JWT token string
     * @return User ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }

    /**
     * Extract app ID from a signaling token.
     * 
     * @param token The JWT token string
     * @return App ID
     */
    public String getAppIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("appId", String.class);
    }

    /**
     * Extract channel name from a signaling token.
     * 
     * @param token The JWT token string
     * @return Channel name
     */
    public String getChannelNameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("channelName", String.class);
    }

    /**
     * Check if a token is expired.
     * 
     * @param token The JWT token string
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true; // Consider invalid tokens as expired
        }
    }

    /**
     * Get token expiration time.
     * 
     * @param token The JWT token string
     * @return Expiration time as Instant
     */
    public Instant getTokenExpiration(String token) {
        Claims claims = validateToken(token);
        return claims.getExpiration().toInstant();
    }

    /**
     * Get remaining time until token expires.
     * 
     * @param token The JWT token string
     * @return Remaining seconds until expiration
     */
    public long getRemainingSeconds(String token) {
        Instant expiration = getTokenExpiration(token);
        return java.time.Duration.between(Instant.now(), expiration).getSeconds();
    }

    /**
     * Check if token is a signaling token.
     * 
     * @param token The JWT token string
     * @return true if it's a signaling token, false otherwise
     */
    public boolean isSignalingToken(String token) {
        try {
            Claims claims = validateToken(token);
            return "signaling".equals(claims.get("tokenType", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is an API token.
     * 
     * @param token The JWT token string
     * @return true if it's an API token, false otherwise
     */
    public boolean isApiToken(String token) {
        try {
            Claims claims = validateToken(token);
            return "api".equals(claims.get("tokenType", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Create a token summary for logging/debugging.
     * 
     * @param token The JWT token string
     * @return Token summary string
     */
    public String getTokenSummary(String token) {
        try {
            Claims claims = validateToken(token);
            return String.format("Token{subject=%s, type=%s, expires=%s, remaining=%ds}",
                               claims.getSubject(),
                               claims.get("tokenType", String.class),
                               claims.getExpiration(),
                               getRemainingSeconds(token));
        } catch (Exception e) {
            return "Token{invalid}";
        }
    }
}

