package com.tasawwur.rtc.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot application class for the Tasawwur RTC REST API Server.
 * 
 * This server provides REST APIs for:
 * - User registration and authentication
 * - Project management (CRUD operations)
 * - JWT token generation for signaling server access
 * - Usage analytics and reporting
 * - Developer dashboard backend services
 * 
 * Key Features:
 * - JWT-based authentication and authorization
 * - PostgreSQL database with JPA/Hibernate
 * - Comprehensive API documentation with OpenAPI
 * - Production-ready monitoring and metrics
 * - Horizontal scalability support
 * 
 * @author Tasawwur RTC Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan
public class RestApiServerApplication {

    /**
     * Main entry point for the REST API server.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set system properties for optimal performance
        System.setProperty("spring.jmx.enabled", "true");
        System.setProperty("management.endpoint.health.probes.enabled", "true");
        
        SpringApplication app = new SpringApplication(RestApiServerApplication.class);
        
        // Configure default profiles
        app.setDefaultProperties(java.util.Map.of(
            "spring.profiles.default", "dev",
            "logging.level.com.tasawwur.rtc", "INFO",
            "server.port", "8081"
        ));
        
        app.run(args);
    }
}

