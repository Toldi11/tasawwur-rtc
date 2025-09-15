package com.tasawwur.rtc.signaling.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a signaling message exchanged between WebRTC peers.
 * 
 * This class encapsulates all types of signaling messages including
 * channel management, WebRTC negotiation (offer/answer), and ICE candidates.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignalingMessage {

    /**
     * Types of signaling messages.
     */
    public enum Type {
        // Connection management
        @JsonProperty("connection_ack")
        CONNECTION_ACK,
        
        @JsonProperty("ping")
        PING,
        
        @JsonProperty("pong")
        PONG,
        
        // Channel management
        @JsonProperty("join_channel")
        JOIN_CHANNEL,
        
        @JsonProperty("join_channel_success")
        JOIN_CHANNEL_SUCCESS,
        
        @JsonProperty("leave_channel")
        LEAVE_CHANNEL,
        
        @JsonProperty("leave_channel_success")
        LEAVE_CHANNEL_SUCCESS,
        
        @JsonProperty("user_joined")
        USER_JOINED,
        
        @JsonProperty("user_left")
        USER_LEFT,
        
        // WebRTC signaling
        @JsonProperty("offer")
        OFFER,
        
        @JsonProperty("answer")
        ANSWER,
        
        @JsonProperty("ice_candidate")
        ICE_CANDIDATE,
        
        // Error handling
        @JsonProperty("error")
        ERROR
    }

    @NotNull
    @JsonProperty("type")
    private Type type;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("sender_id")
    private String senderId;
    
    @JsonProperty("target_user_id")
    private String targetUserId;
    
    @JsonProperty("channel_name")
    private String channelName;
    
    @JsonProperty("payload")
    private Object payload;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("timestamp")
    private Long timestamp;

    // Default constructor for Jackson
    public SignalingMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    // Private constructor for builder
    private SignalingMessage(Builder builder) {
        this.type = builder.type;
        this.sessionId = builder.sessionId;
        this.senderId = builder.senderId;
        this.targetUserId = builder.targetUserId;
        this.channelName = builder.channelName;
        this.payload = builder.payload;
        this.error = builder.error;
        this.timestamp = builder.timestamp != null ? builder.timestamp : System.currentTimeMillis();
    }

    // Getters and setters
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    // Utility methods
    public boolean isChannelMessage() {
        return type == Type.JOIN_CHANNEL || 
               type == Type.LEAVE_CHANNEL || 
               type == Type.USER_JOINED || 
               type == Type.USER_LEFT;
    }

    public boolean isWebRTCMessage() {
        return type == Type.OFFER || 
               type == Type.ANSWER || 
               type == Type.ICE_CANDIDATE;
    }

    public boolean isErrorMessage() {
        return type == Type.ERROR;
    }

    public boolean requiresTarget() {
        return isWebRTCMessage();
    }

    public boolean requiresChannel() {
        return isChannelMessage() || isWebRTCMessage();
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Type type;
        private String sessionId;
        private String senderId;
        private String targetUserId;
        private String channelName;
        private Object payload;
        private String error;
        private Long timestamp;

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder senderId(String senderId) {
            this.senderId = senderId;
            return this;
        }

        public Builder targetUserId(String targetUserId) {
            this.targetUserId = targetUserId;
            return this;
        }

        public Builder channelName(String channelName) {
            this.channelName = channelName;
            return this;
        }

        public Builder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SignalingMessage build() {
            return new SignalingMessage(this);
        }
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignalingMessage that = (SignalingMessage) o;
        return type == that.type &&
               Objects.equals(sessionId, that.sessionId) &&
               Objects.equals(senderId, that.senderId) &&
               Objects.equals(targetUserId, that.targetUserId) &&
               Objects.equals(channelName, that.channelName) &&
               Objects.equals(payload, that.payload) &&
               Objects.equals(error, that.error) &&
               Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, sessionId, senderId, targetUserId, channelName, payload, error, timestamp);
    }

    @Override
    public String toString() {
        return "SignalingMessage{" +
               "type=" + type +
               ", sessionId='" + sessionId + '\'' +
               ", senderId='" + senderId + '\'' +
               ", targetUserId='" + targetUserId + '\'' +
               ", channelName='" + channelName + '\'' +
               ", payload=" + payload +
               ", error='" + error + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }
}

