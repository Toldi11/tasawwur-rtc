package com.tasawwur.rtc.api.controller;

import com.tasawwur.rtc.api.entity.Project;
import com.tasawwur.rtc.api.repository.ProjectRepository;
import com.tasawwur.rtc.api.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for JWT token generation and management.
 * 
 * This controller provides endpoints for generating signaling tokens
 * that allow SDK clients to authenticate with the signaling server.
 */
@RestController
@RequestMapping("/api/token")
@Tag(name = "Token Management", description = "JWT token generation and validation endpoints")
public class TokenController {

    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    private final TokenService tokenService;
    private final ProjectRepository projectRepository;

    public TokenController(TokenService tokenService, ProjectRepository projectRepository) {
        this.tokenService = tokenService;
        this.projectRepository = projectRepository;
    }

    /**
     * Generate a signaling token for WebRTC communication.
     * 
     * This is the primary endpoint used by SDK clients to obtain authentication
     * tokens for connecting to the signaling server.
     */
    @PostMapping("/generate")
    @Operation(summary = "Generate signaling token", 
               description = "Generate a JWT token for WebRTC signaling server authentication")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Invalid app credentials"),
        @ApiResponse(responseCode = "403", description = "Project disabled or access denied"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> generateToken(@Valid @RequestBody TokenGenerationRequest request) {
        logger.info("Token generation request - appId: {}, channel: {}, user: {}", 
                   request.appId, request.channelName, request.userId);

        try {
            // Validate app credentials
            Optional<Project> projectOpt = projectRepository.findByAppIdAndAppSecret(
                request.appId, request.appSecret);
            
            if (projectOpt.isEmpty()) {
                logger.warn("Invalid app credentials - appId: {}", request.appId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid app credentials", "code", "INVALID_CREDENTIALS"));
            }

            Project project = projectOpt.get();
            
            // Check if project is enabled
            if (!project.getEnabled()) {
                logger.warn("Project disabled - appId: {}", request.appId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Project is disabled", "code", "PROJECT_DISABLED"));
            }

            // Generate token
            String token = tokenService.generateSignalingToken(
                request.appId, 
                request.appSecret, 
                request.channelName, 
                request.userId,
                request.expirationSeconds
            );

            // Calculate expiration time
            long expirationSeconds = request.expirationSeconds != null ? 
                request.expirationSeconds : 86400; // 24 hours default
            Instant expiresAt = Instant.now().plusSeconds(expirationSeconds);

            TokenGenerationResponse response = new TokenGenerationResponse(
                token,
                request.appId,
                request.channelName,
                request.userId,
                expiresAt,
                expirationSeconds
            );

            logger.info("Token generated successfully - appId: {}, channel: {}, user: {}, expires: {}", 
                       request.appId, request.channelName, request.userId, expiresAt);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error generating token - appId: {}, channel: {}, user: {}: {}", 
                        request.appId, request.channelName, request.userId, e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Token generation failed", "code", "GENERATION_ERROR"));
        }
    }

    /**
     * Validate an existing token.
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate token", 
               description = "Validate a JWT token and return its claims")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token is valid"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<?> validateToken(@Valid @RequestBody TokenValidationRequest request) {
        logger.debug("Token validation request");

        try {
            var claims = tokenService.validateToken(request.token);
            
            TokenValidationResponse response = new TokenValidationResponse(
                true,
                claims.getSubject(),
                claims.get("appId", String.class),
                claims.get("channelName", String.class),
                claims.getExpiration().toInstant(),
                tokenService.getRemainingSeconds(request.token)
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid or expired token", "code", "INVALID_TOKEN"));
        }
    }

    /**
     * Get token information without validation (for debugging).
     */
    @PostMapping("/info")
    @Operation(summary = "Get token info", 
               description = "Get token information for debugging (does not validate)")
    public ResponseEntity<?> getTokenInfo(@Valid @RequestBody TokenValidationRequest request) {
        try {
            String summary = tokenService.getTokenSummary(request.token);
            return ResponseEntity.ok(Map.of("tokenInfo", summary));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("tokenInfo", "Invalid token format"));
        }
    }

    // Request/Response DTOs
    public static class TokenGenerationRequest {
        @NotBlank(message = "App ID is required")
        public String appId;

        @NotBlank(message = "App Secret is required")
        public String appSecret;

        @NotBlank(message = "Channel name is required")
        public String channelName;

        @NotBlank(message = "User ID is required")
        public String userId;

        @Positive(message = "Expiration must be positive")
        public Long expirationSeconds;

        // Default constructor for Jackson
        public TokenGenerationRequest() {}

        public TokenGenerationRequest(String appId, String appSecret, String channelName, 
                                    String userId, Long expirationSeconds) {
            this.appId = appId;
            this.appSecret = appSecret;
            this.channelName = channelName;
            this.userId = userId;
            this.expirationSeconds = expirationSeconds;
        }
    }

    public static class TokenGenerationResponse {
        public final String token;
        public final String appId;
        public final String channelName;
        public final String userId;
        public final Instant expiresAt;
        public final long expirationSeconds;

        public TokenGenerationResponse(String token, String appId, String channelName, 
                                     String userId, Instant expiresAt, long expirationSeconds) {
            this.token = token;
            this.appId = appId;
            this.channelName = channelName;
            this.userId = userId;
            this.expiresAt = expiresAt;
            this.expirationSeconds = expirationSeconds;
        }
    }

    public static class TokenValidationRequest {
        @NotBlank(message = "Token is required")
        public String token;

        // Default constructor for Jackson
        public TokenValidationRequest() {}

        public TokenValidationRequest(String token) {
            this.token = token;
        }
    }

    public static class TokenValidationResponse {
        public final boolean valid;
        public final String userId;
        public final String appId;
        public final String channelName;
        public final Instant expiresAt;
        public final long remainingSeconds;

        public TokenValidationResponse(boolean valid, String userId, String appId, 
                                     String channelName, Instant expiresAt, long remainingSeconds) {
            this.valid = valid;
            this.userId = userId;
            this.appId = appId;
            this.channelName = channelName;
            this.expiresAt = expiresAt;
            this.remainingSeconds = remainingSeconds;
        }
    }
}

