package com.tasawwur.rtc.signaling.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing WebSocket sessions and user presence.
 * 
 * This service tracks active sessions, maps users to sessions, and provides
 * session lookup capabilities for message routing.
 */
@Service
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    
    // Redis key prefixes
    private static final String SESSION_USER_PREFIX = "session:user:";
    private static final String USER_SESSION_PREFIX = "user:session:";
    private static final String SESSION_INFO_PREFIX = "session:info:";
    
    // Session TTL
    private static final int SESSION_TTL_HOURS = 24;

    private final RedisTemplate<String, String> redisTemplate;
    
    // Local cache for active sessions (for performance)
    private final Map<String, WebSocketSession> localSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();
    private final Map<String, String> userToSession = new ConcurrentHashMap<>();

    public SessionService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Register a new WebSocket session.
     * 
     * @param sessionId The WebSocket session ID
     * @param userId The user ID
     * @param session The WebSocket session
     */
    public void registerSession(String sessionId, String userId, WebSocketSession session) {
        try {
            // Store in local cache
            localSessions.put(sessionId, session);
            sessionToUser.put(sessionId, userId);
            userToSession.put(userId, sessionId);
            
            // Store in Redis for cross-server communication
            String sessionUserKey = SESSION_USER_PREFIX + sessionId;
            String userSessionKey = USER_SESSION_PREFIX + userId;
            String sessionInfoKey = SESSION_INFO_PREFIX + sessionId;
            
            redisTemplate.opsForValue().set(sessionUserKey, userId, SESSION_TTL_HOURS, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(userSessionKey, sessionId, SESSION_TTL_HOURS, TimeUnit.HOURS);
            
            // Store session metadata
            Map<String, String> sessionInfo = Map.of(
                "userId", userId,
                "connectedAt", String.valueOf(System.currentTimeMillis()),
                "serverInstance", getServerInstanceId(),
                "lastActivity", String.valueOf(System.currentTimeMillis())
            );
            
            redisTemplate.opsForHash().putAll(sessionInfoKey, sessionInfo);
            redisTemplate.expire(sessionInfoKey, SESSION_TTL_HOURS, TimeUnit.HOURS);
            
            logger.info("Session registered: sessionId={}, userId={}", sessionId, userId);
            
        } catch (Exception e) {
            logger.error("Error registering session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * Unregister a WebSocket session.
     * 
     * @param sessionId The WebSocket session ID
     */
    public void unregisterSession(String sessionId) {
        try {
            // Get user ID before removing
            String userId = sessionToUser.get(sessionId);
            
            // Remove from local cache
            localSessions.remove(sessionId);
            sessionToUser.remove(sessionId);
            if (userId != null) {
                userToSession.remove(userId);
            }
            
            // Remove from Redis
            String sessionUserKey = SESSION_USER_PREFIX + sessionId;
            String userSessionKey = USER_SESSION_PREFIX + userId;
            String sessionInfoKey = SESSION_INFO_PREFIX + sessionId;
            
            redisTemplate.delete(sessionUserKey);
            if (userId != null) {
                redisTemplate.delete(userSessionKey);
            }
            redisTemplate.delete(sessionInfoKey);
            
            logger.info("Session unregistered: sessionId={}, userId={}", sessionId, userId);
            
        } catch (Exception e) {
            logger.error("Error unregistering session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * Get WebSocket session by session ID.
     * 
     * @param sessionId The session ID
     * @return The WebSocket session, or null if not found
     */
    public WebSocketSession getSession(String sessionId) {
        return localSessions.get(sessionId);
    }

    /**
     * Get user ID by session ID.
     * 
     * @param sessionId The session ID
     * @return The user ID, or null if not found
     */
    public String getUserId(String sessionId) {
        // Try local cache first
        String userId = sessionToUser.get(sessionId);
        if (userId != null) {
            return userId;
        }
        
        // Fallback to Redis
        try {
            String sessionUserKey = SESSION_USER_PREFIX + sessionId;
            return redisTemplate.opsForValue().get(sessionUserKey);
        } catch (Exception e) {
            logger.error("Error getting user ID for session {}: {}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get session ID by user ID.
     * 
     * @param userId The user ID
     * @return The session ID, or null if not found
     */
    public String getSessionIdByUserId(String userId) {
        // Try local cache first
        String sessionId = userToSession.get(userId);
        if (sessionId != null) {
            return sessionId;
        }
        
        // Fallback to Redis
        try {
            String userSessionKey = USER_SESSION_PREFIX + userId;
            return redisTemplate.opsForValue().get(userSessionKey);
        } catch (Exception e) {
            logger.error("Error getting session ID for user {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Check if a session is active.
     * 
     * @param sessionId The session ID
     * @return true if session is active, false otherwise
     */
    public boolean isSessionActive(String sessionId) {
        WebSocketSession session = localSessions.get(sessionId);
        return session != null && session.isOpen();
    }

    /**
     * Check if a user is online.
     * 
     * @param userId The user ID
     * @return true if user is online, false otherwise
     */
    public boolean isUserOnline(String userId) {
        String sessionId = getSessionIdByUserId(userId);
        return sessionId != null && isSessionActive(sessionId);
    }

    /**
     * Update last activity for a session.
     * 
     * @param sessionId The session ID
     */
    public void updateLastActivity(String sessionId) {
        try {
            String sessionInfoKey = SESSION_INFO_PREFIX + sessionId;
            redisTemplate.opsForHash().put(sessionInfoKey, "lastActivity", String.valueOf(System.currentTimeMillis()));
            
        } catch (Exception e) {
            logger.error("Error updating last activity for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * Get session information.
     * 
     * @param sessionId The session ID
     * @return Session information as a map
     */
    public Map<Object, Object> getSessionInfo(String sessionId) {
        try {
            String sessionInfoKey = SESSION_INFO_PREFIX + sessionId;
            return redisTemplate.opsForHash().entries(sessionInfoKey);
            
        } catch (Exception e) {
            logger.error("Error getting session info for {}: {}", sessionId, e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * Get all active sessions count.
     * 
     * @return Number of active sessions
     */
    public int getActiveSessionCount() {
        return localSessions.size();
    }

    /**
     * Get all active user IDs.
     * 
     * @return Set of active user IDs
     */
    public java.util.Set<String> getActiveUserIds() {
        return java.util.Set.copyOf(userToSession.keySet());
    }

    /**
     * Clean up expired sessions.
     * This should be called periodically to remove stale sessions.
     */
    public void cleanupExpiredSessions() {
        try {
            // Remove sessions that are no longer open
            localSessions.entrySet().removeIf(entry -> {
                WebSocketSession session = entry.getValue();
                if (!session.isOpen()) {
                    String sessionId = entry.getKey();
                    String userId = sessionToUser.remove(sessionId);
                    if (userId != null) {
                        userToSession.remove(userId);
                    }
                    logger.debug("Cleaned up expired session: {}", sessionId);
                    return true;
                }
                return false;
            });
            
            logger.debug("Session cleanup completed. Active sessions: {}", localSessions.size());
            
        } catch (Exception e) {
            logger.error("Error during session cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Get server instance ID for distributed deployment.
     * 
     * @return Server instance identifier
     */
    private String getServerInstanceId() {
        // In a real deployment, this would be the actual server/pod ID
        return System.getProperty("server.instance.id", "default-instance");
    }

    /**
     * Get statistics about sessions.
     * 
     * @return Session statistics
     */
    public SessionStats getSessionStats() {
        return new SessionStats(
            localSessions.size(),
            sessionToUser.size(),
            userToSession.size()
        );
    }

    /**
     * Session statistics data class.
     */
    public static class SessionStats {
        private final int activeSessions;
        private final int sessionUserMappings;
        private final int userSessionMappings;

        public SessionStats(int activeSessions, int sessionUserMappings, int userSessionMappings) {
            this.activeSessions = activeSessions;
            this.sessionUserMappings = sessionUserMappings;
            this.userSessionMappings = userSessionMappings;
        }

        public int getActiveSessions() {
            return activeSessions;
        }

        public int getSessionUserMappings() {
            return sessionUserMappings;
        }

        public int getUserSessionMappings() {
            return userSessionMappings;
        }

        @Override
        public String toString() {
            return "SessionStats{" +
                   "activeSessions=" + activeSessions +
                   ", sessionUserMappings=" + sessionUserMappings +
                   ", userSessionMappings=" + userSessionMappings +
                   '}';
        }
    }
}

