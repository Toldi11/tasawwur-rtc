package com.tasawwur.rtc.signaling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for the Tasawwur RTC Signaling Server.
 * 
 * This server handles WebSocket-based signaling for WebRTC peer connections,
 * managing channel membership, message routing, and connection state.
 * 
 * Key Features:
 * - WebSocket signaling for WebRTC
 * - Horizontal scalability with Redis
 * - JWT-based authentication
 * - Comprehensive monitoring and metrics
 * - Production-ready configuration
 * 
 * @author Tasawwur RTC Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan
public class SignalingServerApplication {

    /**
     * Main entry point for the signaling server.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set system properties for optimal performance
        System.setProperty("spring.jmx.enabled", "true");
        System.setProperty("management.endpoint.health.probes.enabled", "true");
        
        SpringApplication app = new SpringApplication(SignalingServerApplication.class);
        
        // Configure default profiles
        app.setDefaultProperties(java.util.Map.of(
            "spring.profiles.default", "dev",
            "logging.level.com.tasawwur.rtc", "INFO"
        ));
        
        app.run(args);
    }
}
