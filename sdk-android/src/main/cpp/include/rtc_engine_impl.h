#ifndef TASAWWUR_RTC_ENGINE_IMPL_H
#define TASAWWUR_RTC_ENGINE_IMPL_H

#include <jni.h>
#include <memory>
#include <string>
#include <atomic>
#include <mutex>
#include <thread>
#include <functional>

// Forward declarations
class WebRTCWrapper;
class SignalingClient;
class MediaManager;

/**
 * Core implementation of the RTC engine.
 * This class manages the WebRTC peer connection, signaling, and media streams.
 */
class RtcEngineImpl {
public:
    /**
     * Configuration structure for the RTC engine.
     */
    struct Config {
        std::string app_id;
        std::string environment;
        std::string signaling_url;
        std::vector<std::string> stun_servers;
        std::vector<std::string> turn_servers;
        std::string audio_codec = "opus";
        std::string video_codec = "H264";
        bool enable_hardware_acceleration = true;
        bool enable_audio_processing = true;
        int connection_timeout_ms = 10000;
        bool enable_stats = false;
        int log_level = 2; // INFO level
        
        // Parse from JSON string
        static Config FromJson(const std::string& json);
    };
    
    /**
     * Callback interface for engine events.
     */
    class Callback {
    public:
        virtual ~Callback() = default;
        virtual void OnUserJoined(const std::string& user_id) = 0;
        virtual void OnUserOffline(const std::string& user_id, int reason) = 0;
        virtual void OnConnectionStateChanged(int state, int reason) = 0;
        virtual void OnError(int error_code, const std::string& message) = 0;
        virtual void OnJoinChannelSuccess(const std::string& channel, const std::string& user_id, int elapsed) = 0;
        virtual void OnLeaveChannel() = 0;
        virtual void OnFirstRemoteVideoDecoded(const std::string& user_id, int width, int height, int elapsed) = 0;
        virtual void OnFirstLocalVideoFrame(int width, int height, int elapsed) = 0;
        virtual void OnRtcStats(const std::string& stats_json) = 0;
    };
    
    /**
     * Connection states.
     */
    enum class ConnectionState {
        DISCONNECTED = 1,
        CONNECTING = 2,
        CONNECTED = 3,
        RECONNECTING = 4,
        FAILED = 5
    };
    
    /**
     * Constructor.
     */
    explicit RtcEngineImpl(const Config& config);
    
    /**
     * Destructor.
     */
    ~RtcEngineImpl();
    
    /**
     * Sets the callback for engine events.
     */
    void SetCallback(std::shared_ptr<Callback> callback);
    
    /**
     * Joins a channel.
     * @param token Authentication token
     * @param channel_name Channel name to join
     * @param user_id Local user ID
     * @return 0 on success, error code on failure
     */
    int JoinChannel(const std::string& token, const std::string& channel_name, const std::string& user_id);
    
    /**
     * Leaves the current channel.
     * @return 0 on success, error code on failure
     */
    int LeaveChannel();
    
    /**
     * Sets up local video rendering.
     * @param surface_view Android SurfaceView for rendering
     */
    void SetupLocalVideo(jobject surface_view);
    
    /**
     * Sets up remote video rendering.
     * @param surface_view Android SurfaceView for rendering
     * @param user_id Remote user ID
     */
    void SetupRemoteVideo(jobject surface_view, const std::string& user_id);
    
    /**
     * Mutes or unmutes local audio.
     * @param muted true to mute, false to unmute
     */
    void MuteLocalAudio(bool muted);
    
    /**
     * Enables or disables local video.
     * @param enabled true to enable, false to disable
     */
    void EnableLocalVideo(bool enabled);
    
    /**
     * Gets the current connection state.
     */
    ConnectionState GetConnectionState() const;
    
    /**
     * Gets the current channel name.
     */
    std::string GetCurrentChannel() const;
    
    /**
     * Gets the current user ID.
     */
    std::string GetCurrentUserId() const;
    
    /**
     * Checks if currently in a channel.
     */
    bool IsInChannel() const;
    
private:
    // Configuration
    Config config_;
    
    // Core components
    std::unique_ptr<WebRTCWrapper> webrtc_wrapper_;
    std::unique_ptr<SignalingClient> signaling_client_;
    std::unique_ptr<MediaManager> media_manager_;
    
    // State management
    std::atomic<ConnectionState> connection_state_{ConnectionState::DISCONNECTED};
    std::string current_channel_;
    std::string current_user_id_;
    std::string current_token_;
    
    // Threading
    std::thread worker_thread_;
    std::atomic<bool> should_stop_{false};
    
    // Synchronization
    mutable std::mutex state_mutex_;
    
    // Callback
    std::shared_ptr<Callback> callback_;
    
    // JNI environment (stored for callbacks)
    JavaVM* jvm_ = nullptr;
    
    // Private methods
    void WorkerThreadMain();
    void SetConnectionState(ConnectionState new_state, int reason = 0);
    void HandleSignalingMessage(const std::string& message);
    void InitializeWebRTC();
    void CleanupWebRTC();
    
    // Callback helpers
    void InvokeCallback(std::function<void(Callback*)> callback_func);
};

#endif // TASAWWUR_RTC_ENGINE_IMPL_H
