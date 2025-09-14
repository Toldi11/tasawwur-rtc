package com.tasawwur.rtc

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Configuration class for HeliosRtcEngine.
 * 
 * Use the Builder pattern to create instances:
 * ```kotlin
 * val config = HeliosRtcConfig.Builder()
 *     .setAppId("your-app-id")
 *     .setEnvironment(Environment.PRODUCTION)
 *     .setLogLevel(LogLevel.INFO)
 *     .build()
 * ```
 */
@JsonClass(generateAdapter = true)
data class HeliosRtcConfig internal constructor(
    /**
     * Your application ID from the Tasawwur RTC dashboard.
     */
    val appId: String,
    
    /**
     * The environment to connect to.
     */
    val environment: Environment = Environment.PRODUCTION,
    
    /**
     * Log level for the SDK.
     */
    val logLevel: LogLevel = LogLevel.INFO,
    
    /**
     * Whether to enable logging.
     */
    val enableLogging: Boolean = true,
    
    /**
     * Custom signaling server URL (optional, overrides environment setting).
     */
    val signalingServerUrl: String? = null,
    
    /**
     * Custom STUN server URLs.
     */
    val stunServers: List<String> = defaultStunServers,
    
    /**
     * Custom TURN server configurations.
     */
    val turnServers: List<TurnServerConfig> = emptyList(),
    
    /**
     * Audio codec preferences.
     */
    val audioCodec: AudioCodec = AudioCodec.OPUS,
    
    /**
     * Video codec preferences.
     */
    val videoCodec: VideoCodec = VideoCodec.H264,
    
    /**
     * Enable hardware acceleration for video encoding/decoding.
     */
    val enableHardwareAcceleration: Boolean = true,
    
    /**
     * Enable audio processing (echo cancellation, noise suppression).
     */
    val enableAudioProcessing: Boolean = true,
    
    /**
     * Connection timeout in milliseconds.
     */
    val connectionTimeoutMs: Int = 10000,
    
    /**
     * Enable detailed statistics collection.
     */
    val enableStats: Boolean = false
) {
    
    /**
     * Converts this config to a JSON string for native layer consumption.
     */
    internal fun toNativeConfig(): String {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        
        val adapter = moshi.adapter(HeliosRtcConfig::class.java)
        return adapter.toJson(this)
    }
    
    /**
     * Builder class for creating HeliosRtcConfig instances.
     */
    class Builder {
        private var appId: String = ""
        private var environment: Environment = Environment.PRODUCTION
        private var logLevel: LogLevel = LogLevel.INFO
        private var enableLogging: Boolean = true
        private var signalingServerUrl: String? = null
        private var stunServers: List<String> = defaultStunServers
        private var turnServers: List<TurnServerConfig> = emptyList()
        private var audioCodec: AudioCodec = AudioCodec.OPUS
        private var videoCodec: VideoCodec = VideoCodec.H264
        private var enableHardwareAcceleration: Boolean = true
        private var enableAudioProcessing: Boolean = true
        private var connectionTimeoutMs: Int = 10000
        private var enableStats: Boolean = false
        
        /**
         * Sets the application ID.
         * 
         * @param appId Your application ID from the Tasawwur RTC dashboard
         * @return This builder instance
         */
        fun setAppId(appId: String) = apply {
            this.appId = appId
        }
        
        /**
         * Sets the environment.
         * 
         * @param environment The environment to connect to
         * @return This builder instance
         */
        fun setEnvironment(environment: Environment) = apply {
            this.environment = environment
        }
        
        /**
         * Sets the log level.
         * 
         * @param logLevel The log level for the SDK
         * @return This builder instance
         */
        fun setLogLevel(logLevel: LogLevel) = apply {
            this.logLevel = logLevel
        }
        
        /**
         * Enables or disables logging.
         * 
         * @param enabled Whether to enable logging
         * @return This builder instance
         */
        fun setLoggingEnabled(enabled: Boolean) = apply {
            this.enableLogging = enabled
        }
        
        /**
         * Sets a custom signaling server URL.
         * 
         * @param url The signaling server URL
         * @return This builder instance
         */
        fun setSignalingServerUrl(url: String?) = apply {
            this.signalingServerUrl = url
        }
        
        /**
         * Sets custom STUN servers.
         * 
         * @param servers List of STUN server URLs
         * @return This builder instance
         */
        fun setStunServers(servers: List<String>) = apply {
            this.stunServers = servers
        }
        
        /**
         * Sets TURN server configurations.
         * 
         * @param servers List of TURN server configurations
         * @return This builder instance
         */
        fun setTurnServers(servers: List<TurnServerConfig>) = apply {
            this.turnServers = servers
        }
        
        /**
         * Sets the preferred audio codec.
         * 
         * @param codec The audio codec to use
         * @return This builder instance
         */
        fun setAudioCodec(codec: AudioCodec) = apply {
            this.audioCodec = codec
        }
        
        /**
         * Sets the preferred video codec.
         * 
         * @param codec The video codec to use
         * @return This builder instance
         */
        fun setVideoCodec(codec: VideoCodec) = apply {
            this.videoCodec = codec
        }
        
        /**
         * Enables or disables hardware acceleration.
         * 
         * @param enabled Whether to enable hardware acceleration
         * @return This builder instance
         */
        fun setHardwareAccelerationEnabled(enabled: Boolean) = apply {
            this.enableHardwareAcceleration = enabled
        }
        
        /**
         * Enables or disables audio processing.
         * 
         * @param enabled Whether to enable audio processing
         * @return This builder instance
         */
        fun setAudioProcessingEnabled(enabled: Boolean) = apply {
            this.enableAudioProcessing = enabled
        }
        
        /**
         * Sets the connection timeout.
         * 
         * @param timeoutMs Timeout in milliseconds
         * @return This builder instance
         */
        fun setConnectionTimeout(timeoutMs: Int) = apply {
            require(timeoutMs > 0) { "Connection timeout must be positive" }
            this.connectionTimeoutMs = timeoutMs
        }
        
        /**
         * Enables or disables statistics collection.
         * 
         * @param enabled Whether to enable statistics
         * @return This builder instance
         */
        fun setStatsEnabled(enabled: Boolean) = apply {
            this.enableStats = enabled
        }
        
        /**
         * Builds the HeliosRtcConfig instance.
         * 
         * @return A new HeliosRtcConfig instance
         * @throws IllegalArgumentException if required fields are missing or invalid
         */
        fun build(): HeliosRtcConfig {
            require(appId.isNotBlank()) { "App ID is required" }
            
            return HeliosRtcConfig(
                appId = appId,
                environment = environment,
                logLevel = logLevel,
                enableLogging = enableLogging,
                signalingServerUrl = signalingServerUrl,
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
    
    companion object {
        /**
         * Default STUN servers used by the SDK.
         */
        val defaultStunServers = listOf(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
            "stun:stun2.l.google.com:19302"
        )
    }
}

/**
 * Environment configuration for the SDK.
 */
enum class Environment(val signalingUrl: String) {
    /**
     * Development environment for testing.
     */
    DEVELOPMENT("wss://dev-signaling.tasawwur-rtc.com/ws"),
    
    /**
     * Production environment.
     */
    PRODUCTION("wss://signaling.tasawwur-rtc.com/ws")
}

/**
 * Log level configuration.
 */
enum class LogLevel(val nativeValue: Int) {
    VERBOSE(0),
    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4),
    NONE(5)
}

/**
 * Supported audio codecs.
 */
enum class AudioCodec(val mimeType: String) {
    /**
     * Opus codec - recommended for best quality and compression.
     */
    OPUS("audio/opus"),
    
    /**
     * PCMU codec - lower quality but better compatibility.
     */
    PCMU("audio/PCMU")
}

/**
 * Supported video codecs.
 */
enum class VideoCodec(val mimeType: String) {
    /**
     * H.264 codec - recommended for best compatibility and hardware acceleration.
     */
    H264("video/H264"),
    
    /**
     * VP8 codec - good compression and open source.
     */
    VP8("video/VP8"),
    
    /**
     * VP9 codec - better compression than VP8 but requires more CPU.
     */
    VP9("video/VP9")
}

/**
 * TURN server configuration.
 */
@JsonClass(generateAdapter = true)
data class TurnServerConfig(
    /**
     * TURN server URL.
     */
    val url: String,
    
    /**
     * Username for authentication.
     */
    val username: String,
    
    /**
     * Password for authentication.
     */
    val password: String
)
