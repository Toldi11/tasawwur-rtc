package com.tasawwur.rtc.signaling.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasawwur.rtc.signaling.model.SignalingMessage;
import com.tasawwur.rtc.signaling.service.ChannelService;
import com.tasawwur.rtc.signaling.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for signaling messages.
 * 
 * This handler manages WebSocket connections for real-time signaling between
 * WebRTC peers, handling message routing, channel management, and connection lifecycle.
 */
@Component
public class SignalingWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(SignalingWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final ChannelService channelService;
    private final SessionService sessionService;
    
    // Thread-safe map to store active sessions
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public SignalingWebSocketHandler(ObjectMapper objectMapper,
                                   ChannelService channelService,
                                   SessionService sessionService) {
        this.objectMapper = objectMapper;
        this.channelService = channelService;
        this.sessionService = sessionService;
    }

    /**
     * Called when a new WebSocket connection is established.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        String userId = getUserId(session);
        
        logger.info("WebSocket connection established: sessionId={}, userId={}", sessionId, userId);
        
        // Store session
        sessions.put(sessionId, session);
        
        // Register session with session service
        sessionService.registerSession(sessionId, userId, session);
        
        // Send connection acknowledgment
        sendMessage(session, SignalingMessage.builder()
                .type(SignalingMessage.Type.CONNECTION_ACK)
                .sessionId(sessionId)
                .build());
    }

    /**
     * Called when a WebSocket connection is closed.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        String userId = getUserId(session);
        
        logger.info("WebSocket connection closed: sessionId={}, userId={}, status={}", 
                   sessionId, userId, status);
        
        // Remove from active sessions
        sessions.remove(sessionId);
        
        // Leave all channels and cleanup
        channelService.leaveAllChannels(sessionId, userId);
        sessionService.unregisterSession(sessionId);
        
        super.afterConnectionClosed(session, status);
    }

    /**
     * Called when a text message is received.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String userId = getUserId(session);
        
        try {
            SignalingMessage signalingMessage = objectMapper.readValue(message.getPayload(), SignalingMessage.class);
            signalingMessage.setSessionId(sessionId);
            signalingMessage.setSenderId(userId);
            
            logger.debug("Received signaling message: type={}, channel={}, from={}", 
                        signalingMessage.getType(), signalingMessage.getChannelName(), userId);
            
            handleSignalingMessage(session, signalingMessage);
            
        } catch (Exception e) {
            logger.error("Error processing signaling message from session {}: {}", sessionId, e.getMessage(), e);
            
            // Send error response
            sendError(session, "Invalid message format", e.getMessage());
        }
    }

    /**
     * Called when a transport error occurs.
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        String userId = getUserId(session);
        
        logger.error("WebSocket transport error: sessionId={}, userId={}", sessionId, userId, exception);
        
        // Cleanup and close connection
        sessions.remove(sessionId);
        channelService.leaveAllChannels(sessionId, userId);
        sessionService.unregisterSession(sessionId);
        
        super.handleTransportError(session, exception);
    }

    /**
     * Handle different types of signaling messages.
     */
    private void handleSignalingMessage(WebSocketSession session, SignalingMessage message) {
        switch (message.getType()) {
            case JOIN_CHANNEL:
                handleJoinChannel(session, message);
                break;
                
            case LEAVE_CHANNEL:
                handleLeaveChannel(session, message);
                break;
                
            case OFFER:
                handleOffer(session, message);
                break;
                
            case ANSWER:
                handleAnswer(session, message);
                break;
                
            case ICE_CANDIDATE:
                handleIceCandidate(session, message);
                break;
                
            case PING:
                handlePing(session, message);
                break;
                
            default:
                logger.warn("Unknown message type: {} from session {}", message.getType(), session.getId());
                sendError(session, "Unknown message type", message.getType().toString());
        }
    }

    /**
     * Handle join channel request.
     */
    private void handleJoinChannel(WebSocketSession session, SignalingMessage message) {
        String channelName = message.getChannelName();
        String userId = message.getSenderId();
        String sessionId = session.getId();
        
        if (channelName == null || channelName.trim().isEmpty()) {
            sendError(session, "Invalid channel name", "Channel name is required");
            return;
        }
        
        try {
            // Join the channel
            boolean joined = channelService.joinChannel(channelName, sessionId, userId);
            
            if (joined) {
                // Send success response
                sendMessage(session, SignalingMessage.builder()
                        .type(SignalingMessage.Type.JOIN_CHANNEL_SUCCESS)
                        .channelName(channelName)
                        .sessionId(sessionId)
                        .build());
                
                // Notify other users in the channel
                notifyChannelMembers(channelName, SignalingMessage.builder()
                        .type(SignalingMessage.Type.USER_JOINED)
                        .channelName(channelName)
                        .senderId(userId)
                        .build(), sessionId);
                
                logger.info("User {} joined channel {} (session: {})", userId, channelName, sessionId);
            } else {
                sendError(session, "Failed to join channel", "Channel may be full or restricted");
            }
            
        } catch (Exception e) {
            logger.error("Error joining channel {}: {}", channelName, e.getMessage(), e);
            sendError(session, "Failed to join channel", e.getMessage());
        }
    }

    /**
     * Handle leave channel request.
     */
    private void handleLeaveChannel(WebSocketSession session, SignalingMessage message) {
        String channelName = message.getChannelName();
        String userId = message.getSenderId();
        String sessionId = session.getId();
        
        if (channelName == null || channelName.trim().isEmpty()) {
            sendError(session, "Invalid channel name", "Channel name is required");
            return;
        }
        
        try {
            // Leave the channel
            boolean left = channelService.leaveChannel(channelName, sessionId, userId);
            
            if (left) {
                // Send success response
                sendMessage(session, SignalingMessage.builder()
                        .type(SignalingMessage.Type.LEAVE_CHANNEL_SUCCESS)
                        .channelName(channelName)
                        .sessionId(sessionId)
                        .build());
                
                // Notify other users in the channel
                notifyChannelMembers(channelName, SignalingMessage.builder()
                        .type(SignalingMessage.Type.USER_LEFT)
                        .channelName(channelName)
                        .senderId(userId)
                        .build(), sessionId);
                
                logger.info("User {} left channel {} (session: {})", userId, channelName, sessionId);
            }
            
        } catch (Exception e) {
            logger.error("Error leaving channel {}: {}", channelName, e.getMessage(), e);
            sendError(session, "Failed to leave channel", e.getMessage());
        }
    }

    /**
     * Handle WebRTC offer message.
     */
    private void handleOffer(WebSocketSession session, SignalingMessage message) {
        routeMessageToTarget(session, message, SignalingMessage.Type.OFFER);
    }

    /**
     * Handle WebRTC answer message.
     */
    private void handleAnswer(WebSocketSession session, SignalingMessage message) {
        routeMessageToTarget(session, message, SignalingMessage.Type.ANSWER);
    }

    /**
     * Handle ICE candidate message.
     */
    private void handleIceCandidate(WebSocketSession session, SignalingMessage message) {
        routeMessageToTarget(session, message, SignalingMessage.Type.ICE_CANDIDATE);
    }

    /**
     * Handle ping message for connection keep-alive.
     */
    private void handlePing(WebSocketSession session, SignalingMessage message) {
        sendMessage(session, SignalingMessage.builder()
                .type(SignalingMessage.Type.PONG)
                .sessionId(session.getId())
                .timestamp(System.currentTimeMillis())
                .build());
    }

    /**
     * Route a message to a specific target user.
     */
    private void routeMessageToTarget(WebSocketSession senderSession, SignalingMessage message, SignalingMessage.Type type) {
        String targetUserId = message.getTargetUserId();
        String channelName = message.getChannelName();
        
        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            sendError(senderSession, "Invalid target user", "Target user ID is required");
            return;
        }
        
        if (channelName == null || channelName.trim().isEmpty()) {
            sendError(senderSession, "Invalid channel name", "Channel name is required");
            return;
        }
        
        try {
            // Find target session
            String targetSessionId = sessionService.getSessionIdByUserId(targetUserId);
            
            if (targetSessionId != null) {
                WebSocketSession targetSession = sessions.get(targetSessionId);
                
                if (targetSession != null && targetSession.isOpen()) {
                    // Forward the message
                    SignalingMessage forwardedMessage = SignalingMessage.builder()
                            .type(type)
                            .channelName(channelName)
                            .senderId(message.getSenderId())
                            .targetUserId(targetUserId)
                            .payload(message.getPayload())
                            .timestamp(System.currentTimeMillis())
                            .build();
                    
                    sendMessage(targetSession, forwardedMessage);
                    
                    logger.debug("Routed {} message from {} to {} in channel {}", 
                               type, message.getSenderId(), targetUserId, channelName);
                } else {
                    sendError(senderSession, "Target user not available", "User is not connected");
                }
            } else {
                sendError(senderSession, "Target user not found", "User is not in any channel");
            }
            
        } catch (Exception e) {
            logger.error("Error routing message to {}: {}", targetUserId, e.getMessage(), e);
            sendError(senderSession, "Failed to route message", e.getMessage());
        }
    }

    /**
     * Notify all members of a channel except the sender.
     */
    private void notifyChannelMembers(String channelName, SignalingMessage message, String excludeSessionId) {
        try {
            var channelMembers = channelService.getChannelMembers(channelName);
            
            for (String sessionId : channelMembers) {
                if (!sessionId.equals(excludeSessionId)) {
                    WebSocketSession session = sessions.get(sessionId);
                    if (session != null && session.isOpen()) {
                        sendMessage(session, message);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error notifying channel members for channel {}: {}", channelName, e.getMessage(), e);
        }
    }

    /**
     * Send a message to a WebSocket session.
     */
    private void sendMessage(WebSocketSession session, SignalingMessage message) {
        try {
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            logger.error("Error sending message to session {}: {}", session.getId(), e.getMessage(), e);
        }
    }

    /**
     * Send an error message to a WebSocket session.
     */
    private void sendError(WebSocketSession session, String error, String details) {
        sendMessage(session, SignalingMessage.builder()
                .type(SignalingMessage.Type.ERROR)
                .error(error)
                .payload(details)
                .sessionId(session.getId())
                .timestamp(System.currentTimeMillis())
                .build());
    }

    /**
     * Extract user ID from WebSocket session attributes.
     */
    private String getUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? userId.toString() : "anonymous-" + session.getId();
    }
}

