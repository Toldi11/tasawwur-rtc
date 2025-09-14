package com.tasawwur.rtc.signaling.config;

import com.tasawwur.rtc.signaling.handler.SignalingWebSocketHandler;
import com.tasawwur.rtc.signaling.interceptor.AuthenticationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * WebSocket configuration for the signaling server.
 * 
 * This configuration sets up WebSocket endpoints for real-time communication
 * between clients, handling authentication, and managing connection lifecycle.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SignalingWebSocketHandler signalingHandler;
    private final AuthenticationInterceptor authInterceptor;

    public WebSocketConfig(SignalingWebSocketHandler signalingHandler, 
                          AuthenticationInterceptor authInterceptor) {
        this.signalingHandler = signalingHandler;
        this.authInterceptor = authInterceptor;
    }

    /**
     * Register WebSocket handlers and configure endpoints.
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Main signaling endpoint
        registry.addHandler(signalingHandler, "/ws")
                .setAllowedOriginPatterns("*") // Configure properly for production
                .addInterceptors(
                    new HttpSessionHandshakeInterceptor(),
                    authInterceptor
                );
        
        // Health check endpoint (no authentication required)
        registry.addHandler(new HealthCheckWebSocketHandler(), "/ws/health")
                .setAllowedOriginPatterns("*");
    }

    /**
     * Simple health check WebSocket handler.
     */
    private static class HealthCheckWebSocketHandler extends org.springframework.web.socket.WebSocketHandler {
        @Override
        public void afterConnectionEstablished(org.springframework.web.socket.WebSocketSession session) {
            try {
                session.sendMessage(new org.springframework.web.socket.TextMessage("{\"status\":\"ok\"}"));
                session.close();
            } catch (Exception e) {
                // Ignore errors in health check
            }
        }

        @Override
        public void handleMessage(org.springframework.web.socket.WebSocketSession session, 
                                org.springframework.web.socket.WebSocketMessage<?> message) {
            // No-op
        }

        @Override
        public void handleTransportError(org.springframework.web.socket.WebSocketSession session, 
                                       Throwable exception) {
            // No-op
        }

        @Override
        public void afterConnectionClosed(org.springframework.web.socket.WebSocketSession session, 
                                        org.springframework.web.socket.CloseStatus closeStatus) {
            // No-op
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
    }
}
