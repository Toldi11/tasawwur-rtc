package com.tasawwur.rtc

import android.content.Context
import android.view.SurfaceView
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * Main entry point for the Tasawwur RTC SDK.
 * 
 * This class provides a high-level API for real-time communication features including
 * audio/video calling, channel management, and media controls.
 * 
 * ## Usage Example
 * ```kotlin
 * val config = TasawwurRtcConfig.Builder()
 *     .setAppId("your-app-id")
 *     .setEnvironment(Environment.PRODUCTION)
 *     .build()
 * 
 * val engine = TasawwurRtcEngine.create(context, config)
 * engine.setListener(object : TasawwurRtcListener {
 *     override fun onUserJoined(userId: String) {
 *         // Handle user joined
 *     }
 * })
 * 
 * // Join a channel
 * engine.joinChannel(token, "my-channel", "user-123")
 * ```
 * 
 * @see TasawwurRtcConfig
 * @see TasawwurRtcListener
 */
class TasawwurRtcEngine private constructor(
    private val context: Context,
    private val config: TasawwurRtcConfig
) {
    
    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var nativeEngineHandle: Long = 0L
    private var listener: TasawwurRtcListener? = null
    private var currentChannel: String? = null
    private var currentUserId: String? = null
    private var isDestroyed = false
    
    // Connection state management
    private var _connectionState = ConnectionState.DISCONNECTED
    val connectionState: ConnectionState get() = _connectionState
    
    init {
        if (config.enableLogging) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Initialize native engine
        nativeEngineHandle = nativeCreateEngine(config.toNativeConfig())
        if (nativeEngineHandle == 0L) {
            throw RuntimeException("Failed to initialize native RTC engine")
        }
        
        Timber.d("TasawwurRtcEngine initialized with handle: $nativeEngineHandle")
    }
    
    /**
     * Sets the event listener for RTC events.
     * 
     * @param listener The listener to receive RTC events, or null to remove the current listener
     */
    fun setListener(listener: TasawwurRtcListener?) {
        this.listener = listener
    }
    
    /**
     * Joins a communication channel with the specified token, channel name, and user ID.
     * 
     * @param token Authentication token obtained from your server
     * @param channelName Name of the channel to join
     * @param userId Unique identifier for the local user
     * @return Result indicating success or failure
     * @throws IllegalStateException if the engine is destroyed
     * @throws IllegalArgumentException if parameters are invalid
     */
    suspend fun joinChannel(
        token: String,
        channelName: String,
        userId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        checkNotDestroyed()
        require(token.isNotBlank()) { "Token cannot be empty" }
        require(channelName.isNotBlank()) { "Channel name cannot be empty" }
        require(userId.isNotBlank()) { "User ID cannot be empty" }
        
        try {
            Timber.d("Joining channel: $channelName with user: $userId")
            updateConnectionState(ConnectionState.CONNECTING)
            
            val result = nativeJoinChannel(nativeEngineHandle, token, channelName, userId)
            if (result == 0) {
                currentChannel = channelName
                currentUserId = userId
                updateConnectionState(ConnectionState.CONNECTED)
                Timber.i("Successfully joined channel: $channelName")
                Result.success(Unit)
            } else {
                updateConnectionState(ConnectionState.FAILED)
                val error = RuntimeException("Failed to join channel: $result")
                Timber.e(error, "Join channel failed")
                Result.failure(error)
            }
        } catch (e: Exception) {
            updateConnectionState(ConnectionState.FAILED)
            Timber.e(e, "Exception while joining channel")
            Result.failure(e)
        }
    }
    
    /**
     * Leaves the current channel.
     * 
     * @return Result indicating success or failure
     * @throws IllegalStateException if the engine is destroyed
     */
    suspend fun leaveChannel(): Result<Unit> = withContext(Dispatchers.IO) {
        checkNotDestroyed()
        
        try {
            val channel = currentChannel ?: run {
                Timber.w("Attempted to leave channel but no channel joined")
                return@withContext Result.success(Unit)
            }
            
            Timber.d("Leaving channel: $channel")
            val result = nativeLeaveChannel(nativeEngineHandle)
            
            if (result == 0) {
                currentChannel = null
                currentUserId = null
                updateConnectionState(ConnectionState.DISCONNECTED)
                Timber.i("Successfully left channel")
                Result.success(Unit)
            } else {
                val error = RuntimeException("Failed to leave channel: $result")
                Timber.e(error, "Leave channel failed")
                Result.failure(error)
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception while leaving channel")
            Result.failure(e)
        }
    }
    
    /**
     * Sets up the local video preview on the specified SurfaceView.
     * 
     * @param surfaceView The SurfaceView to render local video
     * @throws IllegalStateException if the engine is destroyed
     */
    fun setupLocalVideo(surfaceView: SurfaceView) {
        checkNotDestroyed()
        Timber.d("Setting up local video")
        nativeSetupLocalVideo(nativeEngineHandle, surfaceView)
    }
    
    /**
     * Sets up remote video rendering for a specific user on the specified SurfaceView.
     * 
     * @param surfaceView The SurfaceView to render remote video
     * @param remoteUserId The ID of the remote user whose video to display
     * @throws IllegalStateException if the engine is destroyed
     */
    fun setupRemoteVideo(surfaceView: SurfaceView, remoteUserId: String) {
        checkNotDestroyed()
        require(remoteUserId.isNotBlank()) { "Remote user ID cannot be empty" }
        Timber.d("Setting up remote video for user: $remoteUserId")
        nativeSetupRemoteVideo(nativeEngineHandle, surfaceView, remoteUserId)
    }
    
    /**
     * Mutes or unmutes the local audio.
     * 
     * @param muted true to mute, false to unmute
     * @throws IllegalStateException if the engine is destroyed
     */
    fun muteLocalAudio(muted: Boolean) {
        checkNotDestroyed()
        Timber.d("Setting local audio muted: $muted")
        nativeMuteLocalAudio(nativeEngineHandle, muted)
    }
    
    /**
     * Enables or disables the local video.
     * 
     * @param enabled true to enable, false to disable
     * @throws IllegalStateException if the engine is destroyed
     */
    fun enableLocalVideo(enabled: Boolean) {
        checkNotDestroyed()
        Timber.d("Setting local video enabled: $enabled")
        nativeEnableLocalVideo(nativeEngineHandle, enabled)
    }
    
    /**
     * Destroys the engine and releases all resources.
     * After calling this method, the engine instance cannot be used anymore.
     */
    fun destroy() {
        if (isDestroyed) {
            Timber.w("Engine already destroyed")
            return
        }
        
        Timber.d("Destroying TasawwurRtcEngine")
        
        // Cancel all coroutines
        engineScope.cancel()
        
        // Leave channel if still connected
        runBlocking {
            if (currentChannel != null) {
                leaveChannel()
            }
        }
        
        // Destroy native engine
        if (nativeEngineHandle != 0L) {
            nativeDestroyEngine(nativeEngineHandle)
            nativeEngineHandle = 0L
        }
        
        listener = null
        isDestroyed = true
        
        Timber.i("TasawwurRtcEngine destroyed")
    }
    
    /**
     * Gets the current channel name if joined to a channel.
     * 
     * @return The current channel name, or null if not in a channel
     */
    fun getCurrentChannel(): String? = currentChannel
    
    /**
     * Gets the current user ID if joined to a channel.
     * 
     * @return The current user ID, or null if not in a channel
     */
    fun getCurrentUserId(): String? = currentUserId
    
    /**
     * Checks if the engine is currently in a channel.
     * 
     * @return true if in a channel, false otherwise
     */
    fun isInChannel(): Boolean = currentChannel != null
    
    private fun updateConnectionState(newState: ConnectionState) {
        if (_connectionState != newState) {
            val oldState = _connectionState
            _connectionState = newState
            
            engineScope.launch {
                listener?.onConnectionStateChanged(newState, oldState)
            }
        }
    }
    
    private fun checkNotDestroyed() {
        if (isDestroyed) {
            throw IllegalStateException("Engine has been destroyed")
        }
    }
    
    // Native callbacks - called from C++
    @Suppress("unused")
    private fun onNativeUserJoined(userId: String) {
        Timber.d("Native callback: user joined - $userId")
        engineScope.launch {
            listener?.onUserJoined(userId)
        }
    }
    
    @Suppress("unused")
    private fun onNativeUserOffline(userId: String, reason: Int) {
        Timber.d("Native callback: user offline - $userId, reason: $reason")
        val offlineReason = UserOfflineReason.fromNative(reason)
        engineScope.launch {
            listener?.onUserOffline(userId, offlineReason)
        }
    }
    
    @Suppress("unused")
    private fun onNativeConnectionStateChanged(state: Int, reason: Int) {
        Timber.d("Native callback: connection state changed - state: $state, reason: $reason")
        val connectionState = ConnectionState.fromNative(state)
        val changeReason = ConnectionChangeReason.fromNative(reason)
        updateConnectionState(connectionState)
        engineScope.launch {
            listener?.onConnectionStateChanged(connectionState, changeReason)
        }
    }
    
    @Suppress("unused")
    private fun onNativeError(errorCode: Int, message: String) {
        Timber.e("Native callback: error - code: $errorCode, message: $message")
        val error = RtcError.fromNative(errorCode, message)
        engineScope.launch {
            listener?.onError(error)
        }
    }
    
    // Native method declarations
    private external fun nativeCreateEngine(config: String): Long
    private external fun nativeDestroyEngine(handle: Long)
    private external fun nativeJoinChannel(handle: Long, token: String, channelName: String, userId: String): Int
    private external fun nativeLeaveChannel(handle: Long): Int
    private external fun nativeSetupLocalVideo(handle: Long, surfaceView: SurfaceView)
    private external fun nativeSetupRemoteVideo(handle: Long, surfaceView: SurfaceView, userId: String)
    private external fun nativeMuteLocalAudio(handle: Long, muted: Boolean)
    private external fun nativeEnableLocalVideo(handle: Long, enabled: Boolean)
    
    companion object {
        private const val NATIVE_LIB_NAME = "tasawwur-rtc"
        
        init {
            try {
                System.loadLibrary(NATIVE_LIB_NAME)
                Timber.d("Native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Timber.e(e, "Failed to load native library: $NATIVE_LIB_NAME")
                throw RuntimeException("Failed to load native RTC library", e)
            }
        }
        
        /**
         * Creates a new instance of TasawwurRtcEngine.
         * 
         * @param context Android application context
         * @param config Configuration for the RTC engine
         * @return A new TasawwurRtcEngine instance
         * @throws RuntimeException if engine creation fails
         */
        @JvmStatic
        fun create(context: Context, config: TasawwurRtcConfig): TasawwurRtcEngine {
            return TasawwurRtcEngine(context.applicationContext, config)
        }
        
        /**
         * Gets the SDK version.
         * 
         * @return The current SDK version string
         */
        @JvmStatic
        fun getSdkVersion(): String = "1.0.0"
    }
}

