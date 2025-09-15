package com.tasawwur.rtc

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Configuration class for Tasawwur RTC Engine.
 * 
 * This class contains all the configuration options needed to initialize
 * the RTC engine including app credentials, server URLs, codec settings, etc.
 * 
 * ## Usage Example
 * ```kotlin
 * val config = TasawwurRtcConfig.Builder()
 *     .setAppId("your-app-id")
 *     .setEnvironment(Environment.PRODUCTION)
 *     .setLogLevel(LogLevel.INFO)
 *     .build()
 * ```
 */
@Serializable
data class TasawwurRtcConfig(
    val appId: String,
    val environment: Environment = Environment.PRODUCTION,
    val logLevel: LogLevel = LogLevel.INFO,
    val enableLogging: Boolean = true,
    val signalingServerUrl: String? = null,
    val stunServers: List<String> = defaultStunServers,
    val turnServers: List<TurnServerConfig> = emptyList(),
    val audioCodec: AudioCodec = AudioCodec.OPUS,
    val videoCodec: VideoCodec = VideoCodec.H264,
    val enableHardwareAcceleration: Boolean = true,
    val enableAudioProcessing: Boolean = true,
    val connectionTimeoutMs: Int = 10000,
    val enableStats: Boolean = false
) {
    
    /**
     * Converts the config to JSON string for native engine initialization.
     */
    fun toNativeConfig(): String {
        return Json.encodeToString(serializer(), this)
    }
    
    companion object {
        /**
         * Default STUN servers used for NAT traversal.
         */
        val defaultStunServers = listOf(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
            "stun:stun2.l.google.com:19302",
            "stun:stun3.l.google.com:19302",
            "stun:stun4.l.google.com:19302"
        )
    }
    
    /**
     * Builder class for creating TasawwurRtcConfig instances.
     */
    class Builder {
        private var appId: String = ""
        private var environment: Environment = Environment.PRODUCTION
        private var logLevel: LogLevel = LogLevel.INFO
        private var enableLogging: Boolean = true
        private var signalingServerUrl: String? = null
        private var stunServers: List<String> = TasawwurRtcConfig.defaultStunServers
        private var turnServers: List<TurnServerConfig> = emptyList()
        private var audioCodec: AudioCodec = AudioCodec.OPUS
        private var videoCodec: VideoCodec = VideoCodec.H264
        private var enableHardwareAcceleration: Boolean = true
        private var enableAudioProcessing: Boolean = true
        private var connectionTimeoutMs: Int = 10000
        private var enableStats: Boolean = false
        
        /**
         * Sets the application ID.
         * @param appId The application ID obtained from the dashboard
         */
        fun setAppId(appId: String): Builder {
            require(appId.isNotBlank()) { "App ID cannot be empty or blank" }
            this.appId = appId
            return this
        }
        
        /**
         * Sets the environment (development or production).
         * @param environment The environment to use
         */
        fun setEnvironment(environment: Environment): Builder {
            this.environment = environment
            return this
        }
        
        /**
         * Sets the log level.
         * @param logLevel The log level to use
         */
        fun setLogLevel(logLevel: LogLevel): Builder {
            this.logLevel = logLevel
            return this
        }
        
        /**
         * Enables or disables logging.
         * @param enabled true to enable logging, false to disable
         */
        fun setLoggingEnabled(enabled: Boolean): Builder {
            this.enableLogging = enabled
            return this
        }
        
        /**
         * Sets a custom signaling server URL.
         * @param url The signaling server URL
         */
        fun setSignalingServerUrl(url: String?): Builder {
            this.signalingServerUrl = url
            return this
        }
        
        /**
         * Sets custom STUN servers.
         * @param servers List of STUN server URLs
         */
        fun setStunServers(servers: List<String>): Builder {
            this.stunServers = servers
            return this
        }
        
        /**
         * Sets TURN servers for NAT traversal.
         * @param servers List of TURN server configurations
         */
        fun setTurnServers(servers: List<TurnServerConfig>): Builder {
            this.turnServers = servers
            return this
        }
        
        /**
         * Sets the audio codec.
         * @param codec The audio codec to use
         */
        fun setAudioCodec(codec: AudioCodec): Builder {
            this.audioCodec = codec
            return this
        }
        
        /**
         * Sets the video codec.
         * @param codec The video codec to use
         */
        fun setVideoCodec(codec: VideoCodec): Builder {
            this.videoCodec = codec
            return this
        }
        
        /**
         * Enables or disables hardware acceleration.
         * @param enabled true to enable hardware acceleration, false to disable
         */
        fun setHardwareAccelerationEnabled(enabled: Boolean): Builder {
            this.enableHardwareAcceleration = enabled
            return this
        }
        
        /**
         * Enables or disables audio processing.
         * @param enabled true to enable audio processing, false to disable
         */
        fun setAudioProcessingEnabled(enabled: Boolean): Builder {
            this.enableAudioProcessing = enabled
            return this
        }
        
        /**
         * Sets the connection timeout in milliseconds.
         * @param timeoutMs Timeout in milliseconds (must be > 0)
         */
        fun setConnectionTimeout(timeoutMs: Int): Builder {
            require(timeoutMs > 0) { "Connection timeout must be greater than 0" }
            this.connectionTimeoutMs = timeoutMs
            return this
        }
        
        /**
         * Enables or disables statistics collection.
         * @param enabled true to enable stats, false to disable
         */
        fun setStatsEnabled(enabled: Boolean): Builder {
            this.enableStats = enabled
            return this
        }
        
        /**
         * Builds the TasawwurRtcConfig instance.
         * @return The configured TasawwurRtcConfig instance
         * @throws IllegalArgumentException if required parameters are missing or invalid
         */
        fun build(): TasawwurRtcConfig {
            require(appId.isNotBlank()) { "App ID is required" }
            
            return TasawwurRtcConfig(
                appId = appId,
                environment = environment,
                logLevel = logLevel,
                enableLogging = enableLogging,
                signalingServerUrl = signalingServerUrl ?: environment.signalingUrl,
                stunServers = stunServers,
                turnServers = turnServers,
                audioCodec = audioCodec,
                videoCodec = videoCodec,
                enableHardwareAcceleration = enableHardwareAcceleration,
                enableAudioProcessing = enableAudioProcessing,
                connectionTimeoutMs = connectionTimeoutMs,
                enableStats = enableStats
            )
        }
    }
}

/**
 * Environment configuration for the RTC engine.
 */
enum class Environment(val signalingUrl: String) {
    /**
     * Development environment.
     */
    DEVELOPMENT("wss://dev-signaling.tasawwur-rtc.com/ws"),
    
    /**
     * Production environment.
     */
    PRODUCTION("wss://signaling.tasawwur-rtc.com/ws")
}

/**
 * Log levels for the RTC engine.
 */
enum class LogLevel(val nativeValue: Int) {
    /**
     * Verbose logging.
     */
    VERBOSE(0),
    
    /**
     * Debug logging.
     */
    DEBUG(1),
    
    /**
     * Info logging.
     */
    INFO(2),
    
    /**
     * Warning logging.
     */
    WARN(3),
    
    /**
     * Error logging.
     */
    ERROR(4),
    
    /**
     * No logging.
     */
    NONE(5)
}

/**
 * Audio codec options.
 */
enum class AudioCodec(val mimeType: String) {
    /**
     * OPUS audio codec (recommended).
     */
    OPUS("audio/opus"),
    
    /**
     * PCMU audio codec.
     */
    PCMU("audio/PCMU")
}

/**
 * Video codec options.
 */
enum class VideoCodec(val mimeType: String) {
    /**
     * H.264 video codec (recommended).
     */
    H264("video/H264"),
    
    /**
     * VP8 video codec.
     */
    VP8("video/VP8"),
    
    /**
     * VP9 video codec.
     */
    VP9("video/VP9")
}

/**
 * TURN server configuration.
 */
data class TurnServerConfig(
    val url: String,
    val username: String,
    val password: String
)