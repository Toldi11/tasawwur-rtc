package com.tasawwur.rtc

import android.content.Context
import android.view.SurfaceView
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for TasawwurRtcEngine.
 * 
 * These tests verify the core functionality of the RTC engine API
 * without requiring actual WebRTC connections.
 */
@RunWith(RobolectricTestRunner::class)
class TasawwurRtcEngineTest {
    
    private lateinit var context: Context
    private lateinit var config: TasawwurRtcConfig
    private lateinit var engine: TasawwurRtcEngine
    private val mockListener = mockk<TasawwurRtcListener>(relaxed = true)
    
    @Before
    fun setUp() {
        // Mock the native library loading
        mockkStatic(System::class)
        every { System.loadLibrary(any()) } returns Unit
        
        context = RuntimeEnvironment.getApplication()
        config = TasawwurRtcConfig.Builder()
            .setAppId("test-app-id")
            .setEnvironment(Environment.DEVELOPMENT)
            .setLoggingEnabled(false) // Disable logging for tests
            .build()
        
        engine = TasawwurRtcEngine.create(context, config)
        engine.setListener(mockListener)
    }
    
    @After
    fun tearDown() {
        engine.destroy()
        unmockkStatic(System::class)
    }
    
    @Test
    fun `create engine with valid config should succeed`() {
        val testConfig = TasawwurRtcConfig.Builder()
            .setAppId("test-app")
            .setEnvironment(Environment.PRODUCTION)
            .build()
        
        val testEngine = TasawwurRtcEngine.create(context, testConfig)
        
        assertEquals(ConnectionState.DISCONNECTED, testEngine.connectionState)
        assertFalse(testEngine.isInChannel())
        
        testEngine.destroy()
    }
    
    @Test
    fun `create engine with invalid config should fail`() {
        assertFailsWith<IllegalArgumentException> {
            TasawwurRtcConfig.Builder()
                .setAppId("") // Empty app ID should fail
                .build()
        }
    }
    
    @Test
    fun `join channel with valid parameters should succeed`() = runTest {
        val result = engine.joinChannel("test-token", "test-channel", "user-123")
        
        assertTrue(result.isSuccess)
        assertTrue(engine.isInChannel())
        assertEquals("test-channel", engine.getCurrentChannel())
        assertEquals("user-123", engine.getCurrentUserId())
    }
    
    @Test
    fun `join channel with invalid parameters should fail`() = runTest {
        // Test empty token
        assertFailsWith<IllegalArgumentException> {
            engine.joinChannel("", "test-channel", "user-123")
        }
        
        // Test empty channel name
        assertFailsWith<IllegalArgumentException> {
            engine.joinChannel("test-token", "", "user-123")
        }
        
        // Test empty user ID
        assertFailsWith<IllegalArgumentException> {
            engine.joinChannel("test-token", "test-channel", "")
        }
    }
    
    @Test
    fun `join channel twice should fail`() = runTest {
        // First join should succeed
        val firstResult = engine.joinChannel("test-token", "test-channel", "user-123")
        assertTrue(firstResult.isSuccess)
        
        // Second join should fail
        val secondResult = engine.joinChannel("test-token", "another-channel", "user-456")
        assertTrue(secondResult.isFailure)
    }
    
    @Test
    fun `leave channel when not in channel should succeed`() = runTest {
        val result = engine.leaveChannel()
        assertTrue(result.isSuccess)
        assertFalse(engine.isInChannel())
    }
    
    @Test
    fun `leave channel after joining should succeed`() = runTest {
        // Join first
        engine.joinChannel("test-token", "test-channel", "user-123")
        assertTrue(engine.isInChannel())
        
        // Then leave
        val result = engine.leaveChannel()
        assertTrue(result.isSuccess)
        assertFalse(engine.isInChannel())
        assertEquals(null, engine.getCurrentChannel())
        assertEquals(null, engine.getCurrentUserId())
    }
    
    @Test
    fun `setup local video should not throw`() {
        val mockSurfaceView = mockk<SurfaceView>()
        
        // Should not throw even if not in channel
        engine.setupLocalVideo(mockSurfaceView)
        
        // Verify no exceptions
        assertTrue(true)
    }
    
    @Test
    fun `setup remote video should not throw`() {
        val mockSurfaceView = mockk<SurfaceView>()
        
        // Should not throw even if not in channel
        engine.setupRemoteVideo(mockSurfaceView, "remote-user-123")
        
        // Verify no exceptions
        assertTrue(true)
    }
    
    @Test
    fun `setup remote video with empty user ID should fail`() {
        val mockSurfaceView = mockk<SurfaceView>()
        
        assertFailsWith<IllegalArgumentException> {
            engine.setupRemoteVideo(mockSurfaceView, "")
        }
    }
    
    @Test
    fun `mute local audio should not throw`() {
        // Should work regardless of channel state
        engine.muteLocalAudio(true)
        engine.muteLocalAudio(false)
        
        // Verify no exceptions
        assertTrue(true)
    }
    
    @Test
    fun `enable local video should not throw`() {
        // Should work regardless of channel state
        engine.enableLocalVideo(true)
        engine.enableLocalVideo(false)
        
        // Verify no exceptions
        assertTrue(true)
    }
    
    @Test
    fun `operations after destroy should fail`() {
        engine.destroy()
        
        assertFailsWith<IllegalStateException> {
            runTest {
                engine.joinChannel("token", "channel", "user")
            }
        }
        
        assertFailsWith<IllegalStateException> {
            engine.muteLocalAudio(true)
        }
        
        assertFailsWith<IllegalStateException> {
            engine.enableLocalVideo(true)
        }
    }
    
    @Test
    fun `double destroy should not throw`() {
        engine.destroy()
        engine.destroy() // Should not throw
        
        // Verify no exceptions
        assertTrue(true)
    }
    
    @Test
    fun `SDK version should be valid`() {
        val version = TasawwurRtcEngine.getSdkVersion()
        assertTrue(version.isNotBlank())
        assertTrue(version.matches(Regex("\\d+\\.\\d+\\.\\d+")))
    }
    
    @Test
    fun `connection state should be initially disconnected`() {
        assertEquals(ConnectionState.DISCONNECTED, engine.connectionState)
    }
    
    @Test
    fun `listener can be set and cleared`() {
        val newListener = mockk<TasawwurRtcListener>(relaxed = true)
        
        engine.setListener(newListener)
        engine.setListener(null)
        
        // Should not throw
        assertTrue(true)
    }
    
    @Test
    fun `config should serialize to JSON correctly`() {
        val json = config.toNativeConfig()
        
        assertTrue(json.contains("test-app-id"))
        assertTrue(json.contains("DEVELOPMENT"))
        assertTrue(json.isNotBlank())
    }
}

