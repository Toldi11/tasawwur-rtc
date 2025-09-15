package com.tasawwur.rtc

/**
 * Overall RTC statistics for the current session.
 */
data class RtcStats(
    /**
     * Total duration of the call in seconds.
     */
    val duration: Int = 0,
    
    /**
     * Total number of bytes sent.
     */
    val txBytes: Long = 0,
    
    /**
     * Total number of bytes received.
     */
    val rxBytes: Long = 0,
    
    /**
     * Total number of audio bytes sent.
     */
    val txAudioBytes: Long = 0,
    
    /**
     * Total number of video bytes sent.
     */
    val txVideoBytes: Long = 0,
    
    /**
     * Total number of audio bytes received.
     */
    val rxAudioBytes: Long = 0,
    
    /**
     * Total number of video bytes received.
     */
    val rxVideoBytes: Long = 0,
    
    /**
     * Transmission bitrate in Kbps.
     */
    val txKBitrate: Int = 0,
    
    /**
     * Reception bitrate in Kbps.
     */
    val rxKBitrate: Int = 0,
    
    /**
     * Audio transmission bitrate in Kbps.
     */
    val txAudioKBitrate: Int = 0,
    
    /**
     * Video transmission bitrate in Kbps.
     */
    val txVideoKBitrate: Int = 0,
    
    /**
     * Audio reception bitrate in Kbps.
     */
    val rxAudioKBitrate: Int = 0,
    
    /**
     * Video reception bitrate in Kbps.
     */
    val rxVideoKBitrate: Int = 0,
    
    /**
     * Round-trip time in milliseconds.
     */
    val lastmileDelay: Int = 0,
    
    /**
     * Number of users in the channel.
     */
    val userCount: Int = 0,
    
    /**
     * Application CPU usage (%).
     */
    val cpuAppUsage: Double = 0.0,
    
    /**
     * Total CPU usage (%).
     */
    val cpuTotalUsage: Double = 0.0,
    
    /**
     * Gateway round-trip time in milliseconds.
     */
    val gatewayRtt: Int = 0,
    
    /**
     * Memory usage of the app in KB.
     */
    val memoryAppUsageKB: Int = 0,
    
    /**
     * Memory usage ratio of the app (%).
     */
    val memoryAppUsageRatio: Double = 0.0,
    
    /**
     * Total memory usage of the system in KB.
     */
    val memoryTotalUsageKB: Int = 0,
    
    /**
     * Total memory usage ratio of the system (%).
     */
    val memoryTotalUsageRatio: Double = 0.0
)

/**
 * Local video statistics.
 */
data class LocalVideoStats(
    /**
     * Bitrate sent in the reported interval (Kbps).
     */
    val sentBitrate: Int = 0,
    
    /**
     * Frame rate sent in the reported interval (fps).
     */
    val sentFrameRate: Int = 0,
    
    /**
     * Encoder output frame rate (fps).
     */
    val encoderOutputFrameRate: Int = 0,
    
    /**
     * Renderer output frame rate (fps).
     */
    val rendererOutputFrameRate: Int = 0,
    
    /**
     * Target bitrate of the current encoder (Kbps).
     */
    val targetBitrate: Int = 0,
    
    /**
     * Target frame rate of the current encoder (fps).
     */
    val targetFrameRate: Int = 0,
    
    /**
     * Quality adapt indication.
     */
    val qualityAdaptIndication: QualityAdaptIndication = QualityAdaptIndication.NONE,
    
    /**
     * Encoded bitrate (Kbps).
     */
    val encodedBitrate: Int = 0,
    
    /**
     * Encoded frame width (px).
     */
    val encodedFrameWidth: Int = 0,
    
    /**
     * Encoded frame height (px).
     */
    val encodedFrameHeight: Int = 0,
    
    /**
     * Encoded frame count.
     */
    val encodedFrameCount: Int = 0,
    
    /**
     * Video codec type.
     */
    val codecType: VideoCodecType = VideoCodecType.H264
)

/**
 * Remote video statistics.
 */
data class RemoteVideoStats(
    /**
     * User ID of the remote user.
     */
    val uid: String = "",
    
    /**
     * Video stream delay (ms).
     */
    val delay: Int = 0,
    
    /**
     * Width of the remote video.
     */
    val width: Int = 0,
    
    /**
     * Height of the remote video.
     */
    val height: Int = 0,
    
    /**
     * Bitrate received in the reported interval (Kbps).
     */
    val receivedBitrate: Int = 0,
    
    /**
     * Decoder output frame rate (fps).
     */
    val decoderOutputFrameRate: Int = 0,
    
    /**
     * Renderer output frame rate (fps).
     */
    val rendererOutputFrameRate: Int = 0,
    
    /**
     * Packet loss rate (%).
     */
    val packetLossRate: Int = 0,
    
    /**
     * Remote video stream type.
     */
    val rxStreamType: VideoStreamType = VideoStreamType.HIGH,
    
    /**
     * Total freeze time (ms).
     */
    val totalFrozenTime: Int = 0,
    
    /**
     * Total video freeze rate (%).
     */
    val frozenRate: Int = 0,
    
    /**
     * Total active time (ms).
     */
    val totalActiveTime: Int = 0
)

/**
 * Local audio statistics.
 */
data class LocalAudioStats(
    /**
     * Number of channels.
     */
    val numChannels: Int = 0,
    
    /**
     * Sample rate (Hz).
     */
    val sentSampleRate: Int = 0,
    
    /**
     * Average sending bitrate (Kbps).
     */
    val sentBitrate: Int = 0,
    
    /**
     * Internal payload codec.
     */
    val internalCodec: Int = 0,
    
    /**
     * Audio packet loss rate from client to server (%).
     */
    val txPacketLossRate: Int = 0
)

/**
 * Remote audio statistics.
 */
data class RemoteAudioStats(
    /**
     * User ID of the remote user.
     */
    val uid: String = "",
    
    /**
     * Audio quality received.
     */
    val quality: NetworkQuality = NetworkQuality.UNKNOWN,
    
    /**
     * Network delay from sender to receiver (ms).
     */
    val networkTransportDelay: Int = 0,
    
    /**
     * Jitter buffer delay from receiver to player (ms).
     */
    val jitterBufferDelay: Int = 0,
    
    /**
     * Audio frame loss rate in the reported interval (%).
     */
    val audioLossRate: Int = 0,
    
    /**
     * Number of channels.
     */
    val numChannels: Int = 0,
    
    /**
     * Sample rate of the received audio stream (Hz).
     */
    val receivedSampleRate: Int = 0,
    
    /**
     * Average bitrate of the received audio stream (Kbps).
     */
    val receivedBitrate: Int = 0,
    
    /**
     * Total freeze time of the remote audio stream (ms).
     */
    val totalFrozenTime: Int = 0,
    
    /**
     * Total freeze rate of the remote audio stream (%).
     */
    val frozenRate: Int = 0,
    
    /**
     * Total active time of the remote audio stream (ms).
     */
    val totalActiveTime: Int = 0
)

/**
 * Quality adaptation indication.
 */
enum class QualityAdaptIndication(val nativeValue: Int) {
    /**
     * The quality of the local video stays the same.
     */
    NONE(0),
    
    /**
     * The quality improves because the network bandwidth increases.
     */
    UP_BANDWIDTH(1),
    
    /**
     * The quality worsens because the network bandwidth decreases.
     */
    DOWN_BANDWIDTH(2);
    
    companion object {
        fun fromNative(value: Int): QualityAdaptIndication {
            return values().find { it.nativeValue == value } ?: NONE
        }
    }
}

/**
 * Video codec types.
 */
enum class VideoCodecType(val nativeValue: Int) {
    /**
     * H.264 codec.
     */
    H264(1),
    
    /**
     * VP8 codec.
     */
    VP8(2),
    
    /**
     * VP9 codec.
     */
    VP9(3),
    
    /**
     * Generic codec.
     */
    GENERIC(4);
    
    companion object {
        fun fromNative(value: Int): VideoCodecType {
            return values().find { it.nativeValue == value } ?: H264
        }
    }
}

/**
 * Video stream types.
 */
enum class VideoStreamType(val nativeValue: Int) {
    /**
     * High-resolution, high-bitrate video stream.
     */
    HIGH(0),
    
    /**
     * Low-resolution, low-bitrate video stream.
     */
    LOW(1);
    
    companion object {
        fun fromNative(value: Int): VideoStreamType {
            return values().find { it.nativeValue == value } ?: HIGH
        }
    }
}

