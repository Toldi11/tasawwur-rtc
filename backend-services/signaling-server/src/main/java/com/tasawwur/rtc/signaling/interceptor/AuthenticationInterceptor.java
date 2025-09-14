package com.tasawwur.rtc.signaling.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * WebSocket handshake interceptor for JWT-based authentication.
 * 
 * This interceptor validates JWT tokens during the WebSocket handshake
 * and extracts user information for session management.
 */
@Component
public class AuthenticationInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);
    
    private static final String TOKEN_PARAM = "token";
    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String APP_ID_ATTRIBUTE = "appId";
    private static final String CHANNEL_ATTRIBUTE = "channelName";

    @Value("${tasawwur.rtc.jwt.secret:tasawwur-rtc-default-secret-key-change-in-production}")
    private String jwtSecret;

    @Value("${tasawwur.rtc.auth.enabled:true}")
    private boolean authEnabled;

    /**
     * Called before the WebSocket handshake.
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, 
                                 ServerHttpResponse response,
                                 WebSocketHandler wsHandler, 
                                 Map<String, Object> attributes) throws Exception {
        
        if (!authEnabled) {
            // Authentication disabled for development
            attributes.put(USER_ID_ATTRIBUTE, "dev-user-" + System.currentTimeMillis());
            attributes.put(APP_ID_ATTRIBUTE, "dev-app");
            logger.debug("Authentication disabled, allowing connection");
            return true;
        }

        try {
            // Extract token from query parameters
            String token = extractTokenFromRequest(request);
            
            if (token == null || token.trim().isEmpty()) {
                logger.warn("Missing authentication token in WebSocket handshake");
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Validate and parse JWT token
            Claims claims = validateToken(token);
            
            if (claims == null) {
                logger.warn("Invalid authentication token in WebSocket handshake");
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Extract user information from token
            String userId = claims.getSubject();
            String appId = claims.get("appId", String.class);
            String channelName = claims.get("channelName", String.class);
            
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("Missing user ID in authentication token");
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }
            
            if (appId == null || appId.trim().isEmpty()) {
                logger.warn("Missing app ID in authentication token");
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Store user information in session attributes
            attributes.put(USER_ID_ATTRIBUTE, userId);
            attributes.put(APP_ID_ATTRIBUTE, appId);
            
            if (channelName != null && !channelName.trim().isEmpty()) {
                attributes.put(CHANNEL_ATTRIBUTE, channelName);
            }

            logger.info("WebSocket authentication successful: userId={}, appId={}, channel={}", 
                       userId, appId, channelName);
            
            return true;

        } catch (Exception e) {
            logger.error("Error during WebSocket authentication: {}", e.getMessage(), e);
            response.setStatusCode(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    /**
     * Called after the WebSocket handshake.
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, 
                             ServerHttpResponse response,
                             WebSocketHandler wsHandler, 
                             Exception exception) {
        
        if (exception != null) {
            logger.error("WebSocket handshake failed: {}", exception.getMessage(), exception);
        } else {
            logger.debug("WebSocket handshake completed successfully");
        }
    }

    /**
     * Extract JWT token from the request.
     */
    private String extractTokenFromRequest(ServerHttpRequest request) {
        // Try query parameter first
        String query = request.getURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith(TOKEN_PARAM + "=")) {
                    return param.substring((TOKEN_PARAM + "=").length());
                }
            }
        }

        // Try Authorization header
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }

        return null;
    }

    /**
     * Validate JWT token and return claims.
     */
    private Claims validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                    
        } catch (Exception e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
            return null;
        }
    }
}
