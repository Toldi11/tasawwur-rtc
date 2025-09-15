package com.tasawwur.rtc

/**
 * Interface for receiving RTC events from the TasawwurRtcEngine.
 * 
 * All callbacks are executed on the main thread.
 * 
 * ## Usage Example
 * ```kotlin
 * engine.setListener(object : TasawwurRtcListener {
 *     override fun onUserJoined(userId: String) {
 *         // Setup remote video view for the new user
 *         engine.setupRemoteVideo(remoteVideoView, userId)
 *     }
 *     
 *     override fun onUserOffline(userId: String, reason: UserOfflineReason) {
 *         // Clean up remote video view
 *         remoteVideoView.visibility = View.GONE
 *     }
 *     
 *     override fun onConnectionStateChanged(state: ConnectionState, reason: ConnectionChangeReason) {
 *         // Update UI based on connection state
 *         when (state) {
 *             ConnectionState.CONNECTED -> showConnectedUI()
 *             ConnectionState.CONNECTING -> showConnectingUI()
 *             ConnectionState.FAILED -> showErrorUI()
 *             else -> showDisconnectedUI()
 *         }
 *     }
 * })
 * ```
 */
interface TasawwurRtcListener {
    
    /**
     * Called when a remote user joins the channel.
     * 
     * @param userId The ID of the user who joined
     */
    fun onUserJoined(userId: String) {}
    
    /**
     * Called when a remote user leaves the channel.
     * 
     * @param userId The ID of the user who left
     * @param reason The reason why the user left
     */
    fun onUserOffline(userId: String, reason: UserOfflineReason) {}
    
    /**
     * Called when the connection state changes.
     * 
     * @param state The new connection state
     * @param reason The reason for the state change
     */
    fun onConnectionStateChanged(state: ConnectionState, reason: ConnectionChangeReason) {}
    
    /**
     * Called when the connection state changes (with previous state).
     * 
     * @param newState The new connection state
     * @param oldState The previous connection state
     */
    fun onConnectionStateChanged(newState: ConnectionState, oldState: ConnectionState) {}
    
    /**
     * Called when an error occurs.
     * 
     * @param error The error that occurred
     */
    fun onError(error: RtcError) {}
    
    /**
     * Called when the local user successfully joins a channel.
     * 
     * @param channelName The name of the joined channel
     * @param userId The local user ID
     * @param elapsed Time elapsed from calling joinChannel to this callback (ms)
     */
    fun onJoinChannelSuccess(channelName: String, userId: String, elapsed: Int) {}
    
    /**
     * Called when the local user leaves a channel.
     * 
     * @param stats Statistics about the call session
     */
    fun onLeaveChannel(stats: RtcStats) {}
    
    /**
     * Called when a remote user's audio is muted or unmuted.
     * 
     * @param userId The user ID
     * @param muted true if muted, false if unmuted
     */
    fun onUserMuteAudio(userId: String, muted: Boolean) {}
    
    /**
     * Called when a remote user's video is enabled or disabled.
     * 
     * @param userId The user ID
     * @param enabled true if enabled, false if disabled
     */
    fun onUserEnableVideo(userId: String, enabled: Boolean) {}
    
    /**
     * Called when the first remote video frame is decoded.
     * 
     * @param userId The user ID
     * @param width Video frame width
     * @param height Video frame height
     * @param elapsed Time elapsed from calling joinChannel to this callback (ms)
     */
    fun onFirstRemoteVideoDecoded(userId: String, width: Int, height: Int, elapsed: Int) {}
    
    /**
     * Called when the first local video frame is displayed.
     * 
     * @param width Video frame width
     * @param height Video frame height
     * @param elapsed Time elapsed from enabling video to this callback (ms)
     */
    fun onFirstLocalVideoFrame(width: Int, height: Int, elapsed: Int) {}
    
    /**
     * Called periodically to report RTC statistics.
     * 
     * @param stats Current RTC statistics
     */
    fun onRtcStats(stats: RtcStats) {}
    
    /**
     * Called to report local video statistics.
     * 
     * @param stats Local video statistics
     */
    fun onLocalVideoStats(stats: LocalVideoStats) {}
    
    /**
     * Called to report remote video statistics.
     * 
     * @param stats Remote video statistics
     */
    fun onRemoteVideoStats(stats: RemoteVideoStats) {}
    
    /**
     * Called to report local audio statistics.
     * 
     * @param stats Local audio statistics
     */
    fun onLocalAudioStats(stats: LocalAudioStats) {}
    
    /**
     * Called to report remote audio statistics.
     * 
     * @param stats Remote audio statistics
     */
    fun onRemoteAudioStats(stats: RemoteAudioStats) {}
    
    /**
     * Called when network quality changes.
     * 
     * @param userId The user ID (empty string for local user)
     * @param txQuality Uplink network quality
     * @param rxQuality Downlink network quality
     */
    fun onNetworkQuality(userId: String, txQuality: NetworkQuality, rxQuality: NetworkQuality) {}
    
    /**
     * Called when the last mile network quality changes.
     * 
     * @param quality The network quality
     */
    fun onLastmileQuality(quality: NetworkQuality) {}
}

/**
 * Connection state of the RTC engine.
 */
enum class ConnectionState(val nativeValue: Int) {
    /**
     * The SDK is disconnected from the server.
     */
    DISCONNECTED(1),
    
    /**
     * The SDK is connecting to the server.
     */
    CONNECTING(2),
    
    /**
     * The SDK is connected to the server.
     */
    CONNECTED(3),
    
    /**
     * The SDK keeps reconnecting to the server.
     */
    RECONNECTING(4),
    
    /**
     * The SDK fails to connect to the server or join the channel.
     */
    FAILED(5);
    
    companion object {
        fun fromNative(value: Int): ConnectionState {
            return values().find { it.nativeValue == value } ?: DISCONNECTED
        }
    }
}

/**
 * Reason for connection state change.
 */
enum class ConnectionChangeReason(val nativeValue: Int) {
    /**
     * The SDK is connecting to the server.
     */
    CONNECTING(1),
    
    /**
     * The SDK has joined the channel successfully.
     */
    JOIN_SUCCESS(2),
    
    /**
     * The connection is interrupted.
     */
    INTERRUPTED(3),
    
    /**
     * The connection is banned by the server.
     */
    BANNED_BY_SERVER(4),
    
    /**
     * The connection failed due to join channel failure.
     */
    JOIN_FAILED(5),
    
    /**
     * The connection failed due to leave channel.
     */
    LEAVE_CHANNEL(6),
    
    /**
     * The connection failed due to invalid App ID.
     */
    INVALID_APP_ID(7),
    
    /**
     * The connection failed due to invalid channel name.
     */
    INVALID_CHANNEL_NAME(8),
    
    /**
     * The connection failed due to invalid token.
     */
    INVALID_TOKEN(9),
    
    /**
     * The token expired.
     */
    TOKEN_EXPIRED(10),
    
    /**
     * The user was rejected by the server.
     */
    REJECTED_BY_SERVER(11),
    
    /**
     * The SDK setting changes before joining the channel.
     */
    SETTING_PROXY_SERVER(12),
    
    /**
     * The token privilege is renewed.
     */
    RENEW_TOKEN(13),
    
    /**
     * The client IP address changed.
     */
    CLIENT_IP_ADDRESS_CHANGED(14),
    
    /**
     * Timeout for the keep-alive of the connection.
     */
    KEEP_ALIVE_TIMEOUT(15);
    
    companion object {
        fun fromNative(value: Int): ConnectionChangeReason {
            return values().find { it.nativeValue == value } ?: CONNECTING
        }
    }
}

/**
 * Reason why a user went offline.
 */
enum class UserOfflineReason(val nativeValue: Int) {
    /**
     * The user left the channel.
     */
    QUIT(1),
    
    /**
     * The SDK timed out and the user dropped offline.
     */
    DROPPED(2),
    
    /**
     * The user switched to an audience role.
     */
    BECAME_AUDIENCE(3);
    
    companion object {
        fun fromNative(value: Int): UserOfflineReason {
            return values().find { it.nativeValue == value } ?: QUIT
        }
    }
}

/**
 * Network quality levels.
 */
enum class NetworkQuality(val nativeValue: Int) {
    /**
     * Network quality is unknown.
     */
    UNKNOWN(0),
    
    /**
     * Excellent network quality.
     */
    EXCELLENT(1),
    
    /**
     * Good network quality.
     */
    GOOD(2),
    
    /**
     * Poor network quality.
     */
    POOR(3),
    
    /**
     * Bad network quality.
     */
    BAD(4),
    
    /**
     * Very bad network quality.
     */
    VERY_BAD(5),
    
    /**
     * Network connection is down.
     */
    DOWN(6);
    
    companion object {
        fun fromNative(value: Int): NetworkQuality {
            return values().find { it.nativeValue == value } ?: UNKNOWN
        }
    }
}

