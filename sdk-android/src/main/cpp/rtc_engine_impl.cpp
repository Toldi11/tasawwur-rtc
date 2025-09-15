#include "include/rtc_engine_impl.h"
#include "include/webrtc_wrapper.h"
#include "utils/logging.h"
#include "utils/json_helper.h"
#include <thread>
#include <chrono>

using namespace tasawwur;

// RtcEngineImpl::Config implementation
RtcEngineImpl::Config RtcEngineImpl::Config::FromJson(const std::string& json) {
    Config config;
    
    try {
        JsonValue root = JsonParser::Parse(json);
        
        config.app_id = root.GetString("appId");
        config.environment = root.GetString("environment", "PRODUCTION");
        config.signaling_url = root.GetString("signalingServerUrl");
        config.stun_servers = root.GetStringArray("stunServers");
        config.audio_codec = root.GetString("audioCodec", "opus");
        config.video_codec = root.GetString("videoCodec", "H264");
        config.enable_hardware_acceleration = root.GetBool("enableHardwareAcceleration", true);
        config.enable_audio_processing = root.GetBool("enableAudioProcessing", true);
        config.connection_timeout_ms = root.GetInt("connectionTimeoutMs", 10000);
        config.enable_stats = root.GetBool("enableStats", false);
        config.log_level = root.GetInt("logLevel", 2);
        
        // Set default signaling URL based on environment if not provided
        if (config.signaling_url.empty()) {
            if (config.environment == "DEVELOPMENT") {
                config.signaling_url = "wss://dev-signaling.tasawwur-rtc.com/ws";
            } else {
                config.signaling_url = "wss://signaling.tasawwur-rtc.com/ws";
            }
        }
        
        // Set default STUN servers if not provided
        if (config.stun_servers.empty()) {
            config.stun_servers = {
                "stun:stun.l.google.com:19302",
                "stun:stun1.l.google.com:19302",
                "stun:stun2.l.google.com:19302"
            };
        }
        
    } catch (const std::exception& e) {
        LOG_ERROR("Failed to parse config JSON: %s", e.what());
        // Return default config
    }
    
    return config;
}

// RtcEngineImpl implementation
RtcEngineImpl::RtcEngineImpl(const Config& config) 
    : config_(config) {
    
    LOG_INFO("Creating RTC engine with app_id: %s", config_.app_id.c_str());
    
    // Set log level
    SetLogLevel(static_cast<LogLevel>(config_.log_level));
    
    // Initialize WebRTC wrapper
    WebRTCWrapper::Config webrtc_config;
    
    // Configure ICE servers
    for (const auto& stun_url : config_.stun_servers) {
        WebRTCWrapper::IceServer ice_server;
        ice_server.urls.push_back(stun_url);
        webrtc_config.ice_servers.push_back(ice_server);
    }
    
    for (const auto& turn_url : config_.turn_servers) {
        WebRTCWrapper::IceServer ice_server;
        ice_server.urls.push_back(turn_url);
        // In a real implementation, you would parse username/password from config
        webrtc_config.ice_servers.push_back(ice_server);
    }
    
    webrtc_config.audio_codec = config_.audio_codec;
    webrtc_config.video_codec = config_.video_codec;
    webrtc_config.enable_hardware_acceleration = config_.enable_hardware_acceleration;
    webrtc_config.enable_audio_processing = config_.enable_audio_processing;
    
    try {
        webrtc_wrapper_ = std::make_unique<WebRTCWrapper>(webrtc_config);
        if (!webrtc_wrapper_->Initialize()) {
            throw std::runtime_error("Failed to initialize WebRTC");
        }
        
        LOG_INFO("RTC engine created successfully");
    } catch (const std::exception& e) {
        LOG_ERROR("Failed to create RTC engine: %s", e.what());
        throw;
    }
}

RtcEngineImpl::~RtcEngineImpl() {
    LOG_INFO("Destroying RTC engine");
    
    // Stop worker thread
    should_stop_ = true;
    if (worker_thread_.joinable()) {
        worker_thread_.join();
    }
    
    // Leave channel if still connected
    if (IsInChannel()) {
        LeaveChannel();
    }
    
    // Cleanup WebRTC
    if (webrtc_wrapper_) {
        webrtc_wrapper_->Cleanup();
    }
    
    LOG_INFO("RTC engine destroyed");
}

void RtcEngineImpl::SetCallback(std::shared_ptr<Callback> callback) {
    std::lock_guard<std::mutex> lock(state_mutex_);
    callback_ = callback;
    LOG_DEBUG("Callback set");
}

int RtcEngineImpl::JoinChannel(const std::string& token, const std::string& channel_name, const std::string& user_id) {
    LOG_INFO("Joining channel: %s with user: %s", channel_name.c_str(), user_id.c_str());
    
    std::lock_guard<std::mutex> lock(state_mutex_);
    
    if (IsInChannel()) {
        LOG_WARN("Already in channel: %s", current_channel_.c_str());
        return -1;
    }
    
    if (!webrtc_wrapper_) {
        LOG_ERROR("WebRTC wrapper not initialized");
        return -2;
    }
    
    // Validate parameters
    if (token.empty() || channel_name.empty() || user_id.empty()) {
        LOG_ERROR("Invalid parameters for join channel");
        return -3;
    }
    
    try {
        // Set connection state to connecting
        SetConnectionState(ConnectionState::CONNECTING, 1);
        
        // Store channel info
        current_channel_ = channel_name;
        current_user_id_ = user_id;
        current_token_ = token;
        
        // Create peer connection
        if (!webrtc_wrapper_->CreatePeerConnection()) {
            LOG_ERROR("Failed to create peer connection");
            SetConnectionState(ConnectionState::FAILED, 5);
            return -4;
        }
        
        // Add local streams
        if (!webrtc_wrapper_->AddLocalStreams()) {
            LOG_ERROR("Failed to add local streams");
            SetConnectionState(ConnectionState::FAILED, 5);
            return -5;
        }
        
        // Start worker thread for signaling
        should_stop_ = false;
        worker_thread_ = std::thread(&RtcEngineImpl::WorkerThreadMain, this);
        
        // In a real implementation, we would connect to signaling server here
        // For now, simulate successful connection
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
        SetConnectionState(ConnectionState::CONNECTED, 2);
        
        // Notify callback of successful join
        InvokeCallback([channel_name, user_id](Callback* callback) {
            callback->OnJoinChannelSuccess(channel_name, user_id, 100);
        });
        
        LOG_INFO("Successfully joined channel: %s", channel_name.c_str());
        return 0;
        
    } catch (const std::exception& e) {
        LOG_ERROR("Exception while joining channel: %s", e.what());
        SetConnectionState(ConnectionState::FAILED, 5);
        current_channel_.clear();
        current_user_id_.clear();
        current_token_.clear();
        return -6;
    }
}

int RtcEngineImpl::LeaveChannel() {
    LOG_INFO("Leaving channel");
    
    std::lock_guard<std::mutex> lock(state_mutex_);
    
    if (!IsInChannel()) {
        LOG_WARN("Not in any channel");
        return 0;
    }
    
    try {
        // Stop worker thread
        should_stop_ = true;
        if (worker_thread_.joinable()) {
            worker_thread_.join();
        }
        
        // Close peer connection
        if (webrtc_wrapper_) {
            webrtc_wrapper_->ClosePeerConnection();
        }
        
        // Clear state
        current_channel_.clear();
        current_user_id_.clear();
        current_token_.clear();
        
        // Set connection state
        SetConnectionState(ConnectionState::DISCONNECTED, 6);
        
        // Notify callback
        InvokeCallback([](Callback* callback) {
            callback->OnLeaveChannel();
        });
        
        LOG_INFO("Successfully left channel");
        return 0;
        
    } catch (const std::exception& e) {
        LOG_ERROR("Exception while leaving channel: %s", e.what());
        return -1;
    }
}

void RtcEngineImpl::SetupLocalVideo(jobject surface_view) {
    LOG_DEBUG("Setting up local video");
    
    if (!webrtc_wrapper_) {
        LOG_ERROR("WebRTC wrapper not initialized");
        return;
    }
    
    webrtc_wrapper_->SetupLocalVideo(surface_view);
}

void RtcEngineImpl::SetupRemoteVideo(jobject surface_view, const std::string& user_id) {
    LOG_DEBUG("Setting up remote video for user: %s", user_id.c_str());
    
    if (!webrtc_wrapper_) {
        LOG_ERROR("WebRTC wrapper not initialized");
        return;
    }
    
    webrtc_wrapper_->SetupRemoteVideo(surface_view, user_id);
}

void RtcEngineImpl::MuteLocalAudio(bool muted) {
    LOG_DEBUG("Setting local audio muted: %s", muted ? "true" : "false");
    
    if (!webrtc_wrapper_) {
        LOG_ERROR("WebRTC wrapper not initialized");
        return;
    }
    
    webrtc_wrapper_->MuteLocalAudio(muted);
}

void RtcEngineImpl::EnableLocalVideo(bool enabled) {
    LOG_DEBUG("Setting local video enabled: %s", enabled ? "true" : "false");
    
    if (!webrtc_wrapper_) {
        LOG_ERROR("WebRTC wrapper not initialized");
        return;
    }
    
    webrtc_wrapper_->EnableLocalVideo(enabled);
}

RtcEngineImpl::ConnectionState RtcEngineImpl::GetConnectionState() const {
    return connection_state_.load();
}

std::string RtcEngineImpl::GetCurrentChannel() const {
    std::lock_guard<std::mutex> lock(state_mutex_);
    return current_channel_;
}

std::string RtcEngineImpl::GetCurrentUserId() const {
    std::lock_guard<std::mutex> lock(state_mutex_);
    return current_user_id_;
}

bool RtcEngineImpl::IsInChannel() const {
    std::lock_guard<std::mutex> lock(state_mutex_);
    return !current_channel_.empty();
}

void RtcEngineImpl::WorkerThreadMain() {
    LOG_DEBUG("Worker thread started");
    
    while (!should_stop_) {
        // In a real implementation, this would handle:
        // - Signaling server connection
        // - WebRTC signaling messages
        // - Statistics collection
        // - Connection monitoring
        
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
        
        // Simulate periodic stats reporting
        if (config_.enable_stats && IsInChannel()) {
            static int stats_counter = 0;
            if (++stats_counter % 50 == 0) { // Every 5 seconds
                InvokeCallback([](Callback* callback) {
                    callback->OnRtcStats("{\"duration\": 30, \"txBytes\": 1024, \"rxBytes\": 2048}");
                });
            }
        }
    }
    
    LOG_DEBUG("Worker thread stopped");
}

void RtcEngineImpl::SetConnectionState(ConnectionState new_state, int reason) {
    ConnectionState old_state = connection_state_.exchange(new_state);
    
    if (old_state != new_state) {
        LOG_INFO("Connection state changed: %d -> %d (reason: %d)", 
                static_cast<int>(old_state), static_cast<int>(new_state), reason);
        
        InvokeCallback([new_state, reason](Callback* callback) {
            callback->OnConnectionStateChanged(static_cast<int>(new_state), reason);
        });
    }
}

void RtcEngineImpl::InvokeCallback(std::function<void(Callback*)> callback_func) {
    auto callback = callback_;
    if (callback) {
        try {
            callback_func(callback.get());
        } catch (const std::exception& e) {
            LOG_ERROR("Exception in callback: %s", e.what());
        }
    }
}

