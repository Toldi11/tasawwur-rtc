package com.tasawwur.rtc

/**
 * Represents an error that occurred in the RTC engine.
 */
data class RtcError(
    /**
     * The error code.
     */
    val code: ErrorCode,
    
    /**
     * Human-readable error message.
     */
    val message: String,
    
    /**
     * Additional details about the error (optional).
     */
    val details: String? = null
) {
    
    override fun toString(): String {
        return "RtcError(code=$code, message='$message'${if (details != null) ", details='$details'" else ""})"
    }
    
    companion object {
        /**
         * Creates an RtcError from native error code and message.
         */
        internal fun fromNative(errorCode: Int, message: String): RtcError {
            val code = ErrorCode.fromNative(errorCode)
            return RtcError(code, message)
        }
    }
}

/**
 * Error codes that can be returned by the RTC engine.
 */
enum class ErrorCode(val nativeValue: Int, val description: String) {
    // General errors (0-99)
    NO_ERROR(0, "No error"),
    FAILED(-1, "General failure"),
    INVALID_ARGUMENT(-2, "Invalid argument"),
    NOT_READY(-3, "SDK not ready"),
    NOT_SUPPORTED(-4, "Operation not supported"),
    REFUSED(-5, "Request refused"),
    BUFFER_TOO_SMALL(-6, "Buffer too small"),
    NOT_INITIALIZED(-7, "SDK not initialized"),
    INVALID_STATE(-8, "Invalid state"),
    NO_PERMISSION(-9, "No permission"),
    TIMEDOUT(-10, "Operation timed out"),
    CANCELED(-11, "Operation canceled"),
    TOO_OFTEN(-12, "Operation called too often"),
    BIND_SOCKET(-13, "Failed to bind socket"),
    NET_DOWN(-14, "Network is down"),
    NET_NOBUFS(-15, "No network buffers available"),
    
    // Join channel errors (100-199)
    JOIN_CHANNEL_REJECTED(100, "Join channel rejected"),
    LEAVE_CHANNEL_REJECTED(101, "Leave channel rejected"),
    ALREADY_IN_USE(102, "Resource already in use"),
    ABORT(103, "Operation aborted"),
    INIT_NET_ENGINE(104, "Failed to initialize network engine"),
    RESOURCE_LIMITED(105, "Resource limited"),
    INVALID_APP_ID(106, "Invalid App ID"),
    INVALID_CHANNEL_NAME(107, "Invalid channel name"),
    INVALID_TOKEN(108, "Invalid token"),
    TOKEN_EXPIRED(109, "Token expired"),
    NO_SERVER_RESOURCES(110, "No server resources"),
    
    // Audio/Video errors (200-299)
    INVALID_VENDOR_KEY(200, "Invalid vendor key"),
    INVALID_CHANNEL_PROFILE(201, "Invalid channel profile"),
    CAPTURE_FAILURE(202, "Failed to capture audio/video"),
    INVALID_RENDER_MODE(203, "Invalid render mode"),
    DEVICE_NOT_FOUND(204, "Device not found"),
    DEVICE_BUSY(205, "Device busy"),
    DEVICE_INVALID_ID(206, "Invalid device ID"),
    DEVICE_SYSTEM_NOT_READY(207, "Device system not ready"),
    DEVICE_OCCUPY(208, "Device occupied"),
    
    // Network errors (300-399)
    LOAD_MEDIA_ENGINE(300, "Failed to load media engine"),
    START_CALL(301, "Failed to start call"),
    START_CAMERA(302, "Failed to start camera"),
    START_VIDEO_RENDER(303, "Failed to start video render"),
    ADM_GENERAL_ERROR(304, "Audio device module general error"),
    ADM_JAVA_RESOURCE(305, "Audio device module Java resource error"),
    ADM_SAMPLE_RATE(306, "Audio device module sample rate error"),
    ADM_INIT_PLAYOUT(307, "Failed to initialize audio playout"),
    ADM_INIT_RECORDING(308, "Failed to initialize audio recording"),
    ADM_START_PLAYOUT(309, "Failed to start audio playout"),
    ADM_STOP_PLAYOUT(310, "Failed to stop audio playout"),
    ADM_START_RECORDING(311, "Failed to start audio recording"),
    ADM_STOP_RECORDING(312, "Failed to stop audio recording"),
    ADM_RUNTIME_PLAYOUT_ERROR(313, "Audio playout runtime error"),
    ADM_RUNTIME_RECORDING_ERROR(314, "Audio recording runtime error"),
    ADM_RECORD_AUDIO_FAILED(315, "Failed to record audio"),
    ADM_INIT_LOOPBACK(316, "Failed to initialize audio loopback"),
    ADM_START_LOOPBACK(317, "Failed to start audio loopback"),
    ADM_NO_PERMISSION(318, "No audio permission"),
    
    // Encryption errors (400-499)
    ENCRYPTION_FAILED(400, "Encryption failed"),
    DECRYPTION_FAILED(401, "Decryption failed"),
    INVALID_ENCRYPTION_KEY(402, "Invalid encryption key"),
    
    // Streaming errors (500-599)
    PUBLISH_STREAM_NOT_FOUND(500, "Publish stream not found"),
    PUBLISH_STREAM_FORMAT_NOT_SUPPORTED(501, "Publish stream format not supported"),
    PUBLISH_STREAM_CDN_ERROR(502, "CDN error for publish stream"),
    UNPUBLISH_STREAM_NOT_FOUND(503, "Unpublish stream not found"),
    SUBSCRIBE_STREAM_NOT_FOUND(504, "Subscribe stream not found"),
    SUBSCRIBE_STREAM_FORMAT_NOT_SUPPORTED(505, "Subscribe stream format not supported"),
    
    // Custom/Unknown errors
    UNKNOWN_ERROR(9999, "Unknown error");
    
    companion object {
        /**
         * Converts a native error code to an ErrorCode enum.
         */
        fun fromNative(nativeValue: Int): ErrorCode {
            return values().find { it.nativeValue == nativeValue } ?: UNKNOWN_ERROR
        }
    }
}

/**
 * Exception thrown by the RTC SDK.
 */
class RtcException(
    val error: RtcError,
    cause: Throwable? = null
) : Exception(error.message, cause) {
    
    /**
     * The error code.
     */
    val code: ErrorCode get() = error.code
    
    override fun toString(): String {
        return "RtcException: $error"
    }
    
    companion object {
        /**
         * Creates an RtcException from an error code and message.
         */
        fun create(code: ErrorCode, message: String, cause: Throwable? = null): RtcException {
            val error = RtcError(code, message)
            return RtcException(error, cause)
        }
        
        /**
         * Creates an RtcException from native error code and message.
         */
        internal fun fromNative(errorCode: Int, message: String, cause: Throwable? = null): RtcException {
            val error = RtcError.fromNative(errorCode, message)
            return RtcException(error, cause)
        }
    }
}

