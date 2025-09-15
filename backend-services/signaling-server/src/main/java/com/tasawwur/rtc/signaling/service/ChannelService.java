package com.tasawwur.rtc.signaling.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing channels and their members.
 * 
 * This service handles channel membership, user presence, and provides
 * horizontal scalability through Redis-based state management.
 */
@Service
public class ChannelService {

    private static final Logger logger = LoggerFactory.getLogger(ChannelService.class);
    
    // Redis key prefixes
    private static final String CHANNEL_MEMBERS_PREFIX = "channel:members:";
    private static final String USER_CHANNELS_PREFIX = "user:channels:";
    private static final String CHANNEL_INFO_PREFIX = "channel:info:";
    
    // Default channel settings
    private static final int MAX_CHANNEL_MEMBERS = 100;
    private static final int CHANNEL_TTL_HOURS = 24;

    private final RedisTemplate<String, String> redisTemplate;

    public ChannelService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Join a user to a channel.
     * 
     * @param channelName The channel name
     * @param sessionId The WebSocket session ID
     * @param userId The user ID
     * @return true if successfully joined, false otherwise
     */
    public boolean joinChannel(String channelName, String sessionId, String userId) {
        try {
            String channelMembersKey = CHANNEL_MEMBERS_PREFIX + channelName;
            String userChannelsKey = USER_CHANNELS_PREFIX + sessionId;
            String channelInfoKey = CHANNEL_INFO_PREFIX + channelName;
            
            // Check if channel is full
            Long memberCount = redisTemplate.opsForSet().size(channelMembersKey);
            if (memberCount != null && memberCount >= MAX_CHANNEL_MEMBERS) {
                logger.warn("Channel {} is full (members: {})", channelName, memberCount);
                return false;
            }
            
            // Add user to channel members
            redisTemplate.opsForSet().add(channelMembersKey, sessionId + ":" + userId);
            redisTemplate.expire(channelMembersKey, CHANNEL_TTL_HOURS, TimeUnit.HOURS);
            
            // Add channel to user's channels
            redisTemplate.opsForSet().add(userChannelsKey, channelName);
            redisTemplate.expire(userChannelsKey, CHANNEL_TTL_HOURS, TimeUnit.HOURS);
            
            // Update channel info
            redisTemplate.opsForHash().put(channelInfoKey, "lastActivity", String.valueOf(System.currentTimeMillis()));
            redisTemplate.opsForHash().put(channelInfoKey, "memberCount", String.valueOf(memberCount != null ? memberCount + 1 : 1));
            redisTemplate.expire(channelInfoKey, CHANNEL_TTL_HOURS, TimeUnit.HOURS);
            
            logger.info("User {} joined channel {} (session: {})", userId, channelName, sessionId);
            return true;
            
        } catch (Exception e) {
            logger.error("Error joining channel {}: {}", channelName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Remove a user from a channel.
     * 
     * @param channelName The channel name
     * @param sessionId The WebSocket session ID
     * @param userId The user ID
     * @return true if successfully left, false otherwise
     */
    public boolean leaveChannel(String channelName, String sessionId, String userId) {
        try {
            String channelMembersKey = CHANNEL_MEMBERS_PREFIX + channelName;
            String userChannelsKey = USER_CHANNELS_PREFIX + sessionId;
            String channelInfoKey = CHANNEL_INFO_PREFIX + channelName;
            
            // Remove user from channel members
            redisTemplate.opsForSet().remove(channelMembersKey, sessionId + ":" + userId);
            
            // Remove channel from user's channels
            redisTemplate.opsForSet().remove(userChannelsKey, channelName);
            
            // Update channel info
            Long memberCount = redisTemplate.opsForSet().size(channelMembersKey);
            if (memberCount != null && memberCount > 0) {
                redisTemplate.opsForHash().put(channelInfoKey, "lastActivity", String.valueOf(System.currentTimeMillis()));
                redisTemplate.opsForHash().put(channelInfoKey, "memberCount", String.valueOf(memberCount));
            } else {
                // Channel is empty, clean up
                redisTemplate.delete(channelMembersKey);
                redisTemplate.delete(channelInfoKey);
            }
            
            logger.info("User {} left channel {} (session: {})", userId, channelName, sessionId);
            return true;
            
        } catch (Exception e) {
            logger.error("Error leaving channel {}: {}", channelName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Remove a user from all channels (typically called on disconnect).
     * 
     * @param sessionId The WebSocket session ID
     * @param userId The user ID
     */
    public void leaveAllChannels(String sessionId, String userId) {
        try {
            String userChannelsKey = USER_CHANNELS_PREFIX + sessionId;
            
            // Get all channels the user is in
            Set<String> channels = redisTemplate.opsForSet().members(userChannelsKey);
            
            if (channels != null) {
                for (String channelName : channels) {
                    leaveChannel(channelName, sessionId, userId);
                }
            }
            
            // Clean up user's channel list
            redisTemplate.delete(userChannelsKey);
            
            logger.info("User {} left all channels (session: {})", userId, sessionId);
            
        } catch (Exception e) {
            logger.error("Error leaving all channels for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Get all members of a channel.
     * 
     * @param channelName The channel name
     * @return Set of session IDs in the channel
     */
    public Set<String> getChannelMembers(String channelName) {
        try {
            String channelMembersKey = CHANNEL_MEMBERS_PREFIX + channelName;
            Set<String> members = redisTemplate.opsForSet().members(channelMembersKey);
            
            // Extract session IDs from "sessionId:userId" format
            if (members != null) {
                return members.stream()
                        .map(member -> member.split(":")[0])
                        .collect(java.util.stream.Collectors.toSet());
            }
            
            return Set.of();
            
        } catch (Exception e) {
            logger.error("Error getting channel members for {}: {}", channelName, e.getMessage(), e);
            return Set.of();
        }
    }

    /**
     * Get the number of members in a channel.
     * 
     * @param channelName The channel name
     * @return Number of members
     */
    public long getChannelMemberCount(String channelName) {
        try {
            String channelMembersKey = CHANNEL_MEMBERS_PREFIX + channelName;
            Long count = redisTemplate.opsForSet().size(channelMembersKey);
            return count != null ? count : 0;
            
        } catch (Exception e) {
            logger.error("Error getting channel member count for {}: {}", channelName, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Check if a user is in a channel.
     * 
     * @param channelName The channel name
     * @param sessionId The WebSocket session ID
     * @param userId The user ID
     * @return true if user is in channel, false otherwise
     */
    public boolean isUserInChannel(String channelName, String sessionId, String userId) {
        try {
            String channelMembersKey = CHANNEL_MEMBERS_PREFIX + channelName;
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(channelMembersKey, sessionId + ":" + userId));
            
        } catch (Exception e) {
            logger.error("Error checking if user {} is in channel {}: {}", userId, channelName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get all channels a user is in.
     * 
     * @param sessionId The WebSocket session ID
     * @return Set of channel names
     */
    public Set<String> getUserChannels(String sessionId) {
        try {
            String userChannelsKey = USER_CHANNELS_PREFIX + sessionId;
            Set<String> channels = redisTemplate.opsForSet().members(userChannelsKey);
            return channels != null ? channels : Set.of();
            
        } catch (Exception e) {
            logger.error("Error getting user channels for session {}: {}", sessionId, e.getMessage(), e);
            return Set.of();
        }
    }

    /**
     * Get channel information.
     * 
     * @param channelName The channel name
     * @return Channel information as a map
     */
    public java.util.Map<Object, Object> getChannelInfo(String channelName) {
        try {
            String channelInfoKey = CHANNEL_INFO_PREFIX + channelName;
            return redisTemplate.opsForHash().entries(channelInfoKey);
            
        } catch (Exception e) {
            logger.error("Error getting channel info for {}: {}", channelName, e.getMessage(), e);
            return java.util.Map.of();
        }
    }

    /**
     * Clean up expired channels (can be called periodically).
     */
    public void cleanupExpiredChannels() {
        try {
            // This would typically be implemented with a scheduled task
            // For now, we rely on Redis TTL for cleanup
            logger.debug("Channel cleanup completed (using Redis TTL)");
            
        } catch (Exception e) {
            logger.error("Error during channel cleanup: {}", e.getMessage(), e);
        }
    }
}

