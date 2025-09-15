#ifndef TASAWWUR_WEBRTC_WRAPPER_H
#define TASAWWUR_WEBRTC_WRAPPER_H

#include <memory>
#include <string>
#include <vector>
#include <functional>
#include <jni.h>

/**
 * Wrapper around the WebRTC library for easier integration.
 * This class abstracts the complexity of WebRTC and provides a simpler interface.
 */
class WebRTCWrapper {
public:
    /**
     * ICE server configuration.
     */
    struct IceServer {
        std::vector<std::string> urls;
        std::string username;
        std::string password;
    };
    
    /**
     * Configuration for WebRTC.
     */
    struct Config {
        std::vector<IceServer> ice_servers;
        std::string audio_codec = "opus";
        std::string video_codec = "H264";
        bool enable_hardware_acceleration = true;
        bool enable_audio_processing = true;
    };
    
    /**
     * Callback interface for WebRTC events.
     */
    class Observer {
    public:
        virtual ~Observer() = default;
        
        // PeerConnection events
        virtual void OnSignalingChange(int new_state) = 0;
        virtual void OnIceConnectionChange(int new_state) = 0;
        virtual void OnIceGatheringChange(int new_state) = 0;
        virtual void OnIceCandidate(const std::string& candidate, const std::string& sdp_mid, int sdp_mline_index) = 0;
        
        // Media events
        virtual void OnLocalStreamAdded() = 0;
        virtual void OnRemoteStreamAdded(const std::string& stream_id) = 0;
        virtual void OnRemoteStreamRemoved(const std::string& stream_id) = 0;
        
        // Data channel events
        virtual void OnDataChannel() = 0;
        virtual void OnDataChannelMessage(const std::string& message) = 0;
        
        // Error events
        virtual void OnError(const std::string& error) = 0;
    };
    
    /**
     * Constructor.
     */
    explicit WebRTCWrapper(const Config& config);
    
    /**
     * Destructor.
     */
    ~WebRTCWrapper();
    
    /**
     * Initialize WebRTC with the given configuration.
     * @return true on success, false on failure
     */
    bool Initialize();
    
    /**
     * Cleanup WebRTC resources.
     */
    void Cleanup();
    
    /**
     * Set the observer for WebRTC events.
     */
    void SetObserver(std::shared_ptr<Observer> observer);
    
    /**
     * Create a peer connection.
     * @return true on success, false on failure
     */
    bool CreatePeerConnection();
    
    /**
     * Close the peer connection.
     */
    void ClosePeerConnection();
    
    /**
     * Add local audio and video streams.
     * @return true on success, false on failure
     */
    bool AddLocalStreams();
    
    /**
     * Remove local streams.
     */
    void RemoveLocalStreams();
    
    /**
     * Create an offer SDP.
     * @param callback Callback to receive the offer SDP
     */
    void CreateOffer(std::function<void(const std::string& sdp, bool success)> callback);
    
    /**
     * Create an answer SDP.
     * @param callback Callback to receive the answer SDP
     */
    void CreateAnswer(std::function<void(const std::string& sdp, bool success)> callback);
    
    /**
     * Set the local description.
     * @param type SDP type ("offer" or "answer")
     * @param sdp SDP content
     * @param callback Callback to receive the result
     */
    void SetLocalDescription(const std::string& type, const std::string& sdp, 
                           std::function<void(bool success)> callback);
    
    /**
     * Set the remote description.
     * @param type SDP type ("offer" or "answer")
     * @param sdp SDP content
     * @param callback Callback to receive the result
     */
    void SetRemoteDescription(const std::string& type, const std::string& sdp, 
                            std::function<void(bool success)> callback);
    
    /**
     * Add an ICE candidate.
     * @param candidate ICE candidate string
     * @param sdp_mid SDP media ID
     * @param sdp_mline_index SDP media line index
     * @return true on success, false on failure
     */
    bool AddIceCandidate(const std::string& candidate, const std::string& sdp_mid, int sdp_mline_index);
    
    /**
     * Set up local video rendering.
     * @param surface_view Android SurfaceView
     */
    void SetupLocalVideo(jobject surface_view);
    
    /**
     * Set up remote video rendering.
     * @param surface_view Android SurfaceView
     * @param stream_id Remote stream ID
     */
    void SetupRemoteVideo(jobject surface_view, const std::string& stream_id);
    
    /**
     * Mute or unmute local audio.
     * @param muted true to mute, false to unmute
     */
    void MuteLocalAudio(bool muted);
    
    /**
     * Enable or disable local video.
     * @param enabled true to enable, false to disable
     */
    void EnableLocalVideo(bool enabled);
    
    /**
     * Get connection statistics.
     * @param callback Callback to receive stats
     */
    void GetStats(std::function<void(const std::string& stats_json)> callback);
    
    /**
     * Check if peer connection is established.
     */
    bool IsConnected() const;
    
private:
    class Impl;
    std::unique_ptr<Impl> impl_;
    
    Config config_;
    std::shared_ptr<Observer> observer_;
    
    // Disable copy and assignment
    WebRTCWrapper(const WebRTCWrapper&) = delete;
    WebRTCWrapper& operator=(const WebRTCWrapper&) = delete;
};

#endif // TASAWWUR_WEBRTC_WRAPPER_H

