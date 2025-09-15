package com.tasawwur.rtc

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for TasawwurRtcConfig and related classes.
 */
class TasawwurRtcConfigTest {
    
    @Test
    fun `builder with minimal config should work`() {
        val config = TasawwurRtcConfig.Builder()
            .setAppId("test-app-id")
            .build()
        
        assertEquals("test-app-id", config.appId)
        assertEquals(Environment.PRODUCTION, config.environment)
        assertEquals(LogLevel.INFO, config.logLevel)
        assertTrue(config.enableLogging)
        assertEquals(AudioCodec.OPUS, config.audioCodec)
        assertEquals(VideoCodec.H264, config.videoCodec)
        assertTrue(config.enableHardwareAcceleration)
        assertTrue(config.enableAudioProcessing)
        assertEquals(10000, config.connectionTimeoutMs)
        assertFalse(config.enableStats)
    }
    
    @Test
    fun `builder with full config should work`() {
        val turnServers = listOf(
            TurnServerConfig("turn:server1.com", "user1", "pass1"),
            TurnServerConfig("turn:server2.com", "user2", "pass2")
        )
        
        val config = TasawwurRtcConfig.Builder()
            .setAppId("full-app-id")
            .setEnvironment(Environment.DEVELOPMENT)
            .setLogLevel(LogLevel.DEBUG)
            .setLoggingEnabled(false)
            .setSignalingServerUrl("wss://custom.server.com/ws")
            .setStunServers(listOf("stun:custom.stun.com:3478"))
            .setTurnServers(turnServers)
            .setAudioCodec(AudioCodec.PCMU)
            .setVideoCodec(VideoCodec.VP8)
            .setHardwareAccelerationEnabled(false)
            .setAudioProcessingEnabled(false)
            .setConnectionTimeout(5000)
            .setStatsEnabled(true)
            .build()
        
        assertEquals("full-app-id", config.appId)
        assertEquals(Environment.DEVELOPMENT, config.environment)
        assertEquals(LogLevel.DEBUG, config.logLevel)
        assertFalse(config.enableLogging)
        assertEquals("wss://custom.server.com/ws", config.signalingServerUrl)
        assertEquals(listOf("stun:custom.stun.com:3478"), config.stunServers)
        assertEquals(turnServers, config.turnServers)
        assertEquals(AudioCodec.PCMU, config.audioCodec)
        assertEquals(VideoCodec.VP8, config.videoCodec)
        assertFalse(config.enableHardwareAcceleration)
        assertFalse(config.enableAudioProcessing)
        assertEquals(5000, config.connectionTimeoutMs)
        assertTrue(config.enableStats)
    }
    
    @Test
    fun `builder without app ID should fail`() {
        assertFailsWith<IllegalArgumentException> {
            TasawwurRtcConfig.Builder().build()
        }
    }
    
    @Test
    fun `builder with empty app ID should fail`() {
        assertFailsWith<IllegalArgumentException> {
            TasawwurRtcConfig.Builder()
                .setAppId("")
                .build()
        }
    }
    
    @Test
    fun `builder with blank app ID should fail`() {
        assertFailsWith<IllegalArgumentException> {
            TasawwurRtcConfig.Builder()
                .setAppId("   ")
                .build()
        }
    }
    
    @Test
    fun `builder with invalid timeout should fail`() {
        assertFailsWith<IllegalArgumentException> {
            TasawwurRtcConfig.Builder()
                .setAppId("test-app")
                .setConnectionTimeout(0)
                .build()
        }
        
        assertFailsWith<IllegalArgumentException> {
            TasawwurRtcConfig.Builder()
                .setAppId("test-app")
                .setConnectionTimeout(-1000)
                .build()
        }
    }
    
    @Test
    fun `config should serialize to JSON`() {
        val config = TasawwurRtcConfig.Builder()
            .setAppId("json-test-app")
            .setEnvironment(Environment.DEVELOPMENT)
            .build()
        
        val json = config.toNativeConfig()
        
        assertTrue(json.contains("json-test-app"))
        assertTrue(json.contains("DEVELOPMENT"))
        assertTrue(json.startsWith("{"))
        assertTrue(json.endsWith("}"))
    }
    
    @Test
    fun `default STUN servers should be provided`() {
        val defaultServers = TasawwurRtcConfig.defaultStunServers
        
        assertTrue(defaultServers.isNotEmpty())
        assertTrue(defaultServers.all { it.startsWith("stun:") })
        assertTrue(defaultServers.any { it.contains("google.com") })
    }
    
    @Test
    fun `environment enum should have correct signaling URLs`() {
        assertEquals("wss://dev-signaling.tasawwur-rtc.com/ws", Environment.DEVELOPMENT.signalingUrl)
        assertEquals("wss://signaling.tasawwur-rtc.com/ws", Environment.PRODUCTION.signalingUrl)
    }
    
    @Test
    fun `log level enum should have correct native values`() {
        assertEquals(0, LogLevel.VERBOSE.nativeValue)
        assertEquals(1, LogLevel.DEBUG.nativeValue)
        assertEquals(2, LogLevel.INFO.nativeValue)
        assertEquals(3, LogLevel.WARN.nativeValue)
        assertEquals(4, LogLevel.ERROR.nativeValue)
        assertEquals(5, LogLevel.NONE.nativeValue)
    }
    
    @Test
    fun `audio codec enum should have correct MIME types`() {
        assertEquals("audio/opus", AudioCodec.OPUS.mimeType)
        assertEquals("audio/PCMU", AudioCodec.PCMU.mimeType)
    }
    
    @Test
    fun `video codec enum should have correct MIME types`() {
        assertEquals("video/H264", VideoCodec.H264.mimeType)
        assertEquals("video/VP8", VideoCodec.VP8.mimeType)
        assertEquals("video/VP9", VideoCodec.VP9.mimeType)
    }
    
    @Test
    fun `TURN server config should store all fields`() {
        val turnConfig = TurnServerConfig(
            url = "turn:example.com:3478",
            username = "testuser",
            password = "testpass"
        )
        
        assertEquals("turn:example.com:3478", turnConfig.url)
        assertEquals("testuser", turnConfig.username)
        assertEquals("testpass", turnConfig.password)
    }
    
    @Test
    fun `builder should be reusable`() {
        val builder = TasawwurRtcConfig.Builder()
            .setAppId("reusable-app")
            .setEnvironment(Environment.PRODUCTION)
        
        val config1 = builder.build()
        val config2 = builder
            .setEnvironment(Environment.DEVELOPMENT)
            .build()
        
        assertEquals(Environment.PRODUCTION, config1.environment)
        assertEquals(Environment.DEVELOPMENT, config2.environment)
        assertEquals("reusable-app", config1.appId)
        assertEquals("reusable-app", config2.appId)
    }
}
