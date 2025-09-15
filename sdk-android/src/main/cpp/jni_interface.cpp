#include <jni.h>
#include <android/log.h>
#include <string>
#include <memory>
#include <unordered_map>
#include <mutex>

#include "include/rtc_engine_impl.h"
#include "utils/logging.h"

// Logging macros
#define TAG "TasawwurRTC-JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace {
    // Global state management
    std::mutex g_engines_mutex;
    std::unordered_map<jlong, std::shared_ptr<RtcEngineImpl>> g_engines;
    jlong g_next_handle = 1;
    
    JavaVM* g_jvm = nullptr;
    
    // JNI callback class
    class JniCallback : public RtcEngineImpl::Callback {
    public:
        JniCallback(JavaVM* jvm, jobject java_engine_obj) 
            : jvm_(jvm), java_engine_obj_(java_engine_obj) {
            // Get global reference to the Java object
            JNIEnv* env = nullptr;
            if (jvm_->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                java_engine_obj_ = env->NewGlobalRef(java_engine_obj);
                
                // Cache method IDs for callbacks
                jclass engine_class = env->GetObjectClass(java_engine_obj_);
                
                on_user_joined_method_ = env->GetMethodID(engine_class, "onNativeUserJoined", "(Ljava/lang/String;)V");
                on_user_offline_method_ = env->GetMethodID(engine_class, "onNativeUserOffline", "(Ljava/lang/String;I)V");
                on_connection_state_changed_method_ = env->GetMethodID(engine_class, "onNativeConnectionStateChanged", "(II)V");
                on_error_method_ = env->GetMethodID(engine_class, "onNativeError", "(ILjava/lang/String;)V");
                
                env->DeleteLocalRef(engine_class);
            }
        }
        
        ~JniCallback() {
            if (java_engine_obj_ && jvm_) {
                JNIEnv* env = nullptr;
                if (jvm_->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
                    env->DeleteGlobalRef(java_engine_obj_);
                }
            }
        }
        
        void OnUserJoined(const std::string& user_id) override {
            CallJavaMethod([this, user_id](JNIEnv* env) {
                if (on_user_joined_method_) {
                    jstring j_user_id = env->NewStringUTF(user_id.c_str());
                    env->CallVoidMethod(java_engine_obj_, on_user_joined_method_, j_user_id);
                    env->DeleteLocalRef(j_user_id);
                }
            });
        }
        
        void OnUserOffline(const std::string& user_id, int reason) override {
            CallJavaMethod([this, user_id, reason](JNIEnv* env) {
                if (on_user_offline_method_) {
                    jstring j_user_id = env->NewStringUTF(user_id.c_str());
                    env->CallVoidMethod(java_engine_obj_, on_user_offline_method_, j_user_id, reason);
                    env->DeleteLocalRef(j_user_id);
                }
            });
        }
        
        void OnConnectionStateChanged(int state, int reason) override {
            CallJavaMethod([this, state, reason](JNIEnv* env) {
                if (on_connection_state_changed_method_) {
                    env->CallVoidMethod(java_engine_obj_, on_connection_state_changed_method_, state, reason);
                }
            });
        }
        
        void OnError(int error_code, const std::string& message) override {
            CallJavaMethod([this, error_code, message](JNIEnv* env) {
                if (on_error_method_) {
                    jstring j_message = env->NewStringUTF(message.c_str());
                    env->CallVoidMethod(java_engine_obj_, on_error_method_, error_code, j_message);
                    env->DeleteLocalRef(j_message);
                }
            });
        }
        
        void OnJoinChannelSuccess(const std::string& channel, const std::string& user_id, int elapsed) override {
            // Implementation for join channel success callback
            LOGI("Join channel success: %s, user: %s, elapsed: %d", channel.c_str(), user_id.c_str(), elapsed);
        }
        
        void OnLeaveChannel() override {
            // Implementation for leave channel callback
            LOGI("Leave channel");
        }
        
        void OnFirstRemoteVideoDecoded(const std::string& user_id, int width, int height, int elapsed) override {
            // Implementation for first remote video decoded callback
            LOGI("First remote video decoded: %s, %dx%d, elapsed: %d", user_id.c_str(), width, height, elapsed);
        }
        
        void OnFirstLocalVideoFrame(int width, int height, int elapsed) override {
            // Implementation for first local video frame callback
            LOGI("First local video frame: %dx%d, elapsed: %d", width, height, elapsed);
        }
        
        void OnRtcStats(const std::string& stats_json) override {
            // Implementation for RTC stats callback
            LOGD("RTC Stats: %s", stats_json.c_str());
        }
        
    private:
        JavaVM* jvm_;
        jobject java_engine_obj_;
        
        // Cached method IDs
        jmethodID on_user_joined_method_ = nullptr;
        jmethodID on_user_offline_method_ = nullptr;
        jmethodID on_connection_state_changed_method_ = nullptr;
        jmethodID on_error_method_ = nullptr;
        
        void CallJavaMethod(std::function<void(JNIEnv*)> method_call) {
            if (!jvm_ || !java_engine_obj_) return;
            
            JNIEnv* env = nullptr;
            bool attached = false;
            
            int status = jvm_->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
            if (status == JNI_EDETACHED) {
                status = jvm_->AttachCurrentThread(&env, nullptr);
                attached = (status == JNI_OK);
            }
            
            if (status == JNI_OK && env) {
                method_call(env);
                
                if (env->ExceptionCheck()) {
                    env->ExceptionDescribe();
                    env->ExceptionClear();
                }
                
                if (attached) {
                    jvm_->DetachCurrentThread();
                }
            }
        }
    };
    
    // Helper functions
    std::string jstring_to_string(JNIEnv* env, jstring jstr) {
        if (!jstr) return "";
        
        const char* chars = env->GetStringUTFChars(jstr, nullptr);
        std::string result(chars);
        env->ReleaseStringUTFChars(jstr, chars);
        return result;
    }
    
    std::shared_ptr<RtcEngineImpl> get_engine(jlong handle) {
        std::lock_guard<std::mutex> lock(g_engines_mutex);
        auto it = g_engines.find(handle);
        return (it != g_engines.end()) ? it->second : nullptr;
    }
    
} // anonymous namespace

extern "C" {

// JNI_OnLoad - called when the library is loaded
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("JNI_OnLoad called");
    g_jvm = vm;
    
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        LOGE("Failed to get JNI environment");
        return JNI_ERR;
    }
    
    // Initialize logging
    tasawwur::InitializeLogging();
    
    LOGI("Tasawwur RTC native library loaded successfully");
    return JNI_VERSION_1_6;
}

// JNI_OnUnload - called when the library is unloaded
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    LOGI("JNI_OnUnload called");
    
    // Clean up all engines
    {
        std::lock_guard<std::mutex> lock(g_engines_mutex);
        g_engines.clear();
    }
    
    g_jvm = nullptr;
    LOGI("Tasawwur RTC native library unloaded");
}

// Create RTC Engine
JNIEXPORT jlong JNICALL
Java_com_tasawwur_rtc_TasawwurRtcEngine_nativeCreateEngine(JNIEnv* env, jobject thiz, jstring config_json) {
    LOGI("Creating native RTC engine");
    
    try {
        std::string config_str = jstring_to_string(env, config_json);
        LOGD("Engine config: %s", config_str.c_str());
        
        // Parse configuration
        auto config = RtcEngineImpl::Config::FromJson(config_str);
        
        // Create engine instance
        auto engine = std::make_shared<RtcEngineImpl>(config);
        
        // Create and set callback
        auto callback = std::make_shared<JniCallback>(g_jvm, thiz);
        engine->SetCallback(callback);
        
        // Store engine with handle
        jlong handle;
        {
            std::lock_guard<std::mutex> lock(g_engines_mutex);
            handle = g_next_handle++;
            g_engines[handle] = engine;
        }
        
        LOGI("Native RTC engine created with handle: %lld", handle);
        return handle;
        
    } catch (const std::exception& e) {
        LOGE("Failed to create RTC engine: %s", e.what());
        return 0;
    }
}

// Destroy RTC Engine
JNIEXPORT void JNICALL
Java_com_tasawwur_rtc_TasawwurRtcEngine_nativeDestroyEngine(JNIEnv* env, jobject thiz, jlong handle) {
    LOGI("Destroying native RTC engine with handle: %lld", handle);
    
    std::lock_guard<std::mutex> lock(g_engines_mutex);
    auto it = g_engines.find(handle);
    if (it != g_engines.end()) {
        g_engines.erase(it);
        LOGI("Native RTC engine destroyed");
    } else {
        LOGW("Attempted to destroy non-existent engine with handle: %lld", handle);
    }
}

// Join Channel
JNIEXPORT jint JNICALL
Java_com_tasawwur_rtc_TasawwurRtcEngine_nativeJoinChannel(JNIEnv* env, jobject thiz, 
                                                        jlong handle, jstring token, 
                                                        jstring channel_name, jstring user_id) {
    LOGD("Join channel called for handle: %lld", handle);
    
    auto engine = get_engine(handle);
    if (!engine) {
        LOGE("Invalid engine handle: %lld", handle);
        return -1;
    }
    
    std::string token_str = jstring_to_string(env, token);
    std::string channel_str = jstring_to_string(env, channel_name);
    std::string user_id_str = jstring_to_string(env, user_id);
    
    LOGI("Joining channel: %s with user: %s", channel_str.c_str(), user_id_str.c_str());
    
    return engine->JoinChannel(token_str, channel_str, user_id_str);
}

// Leave Channel
JNIEXPORT jint JNICALL
Java_com_tasawwur_rtc_TasawwurRtcEngine_nativeLeaveChannel(JNIEnv* env, jobject thiz, jlong handle) {
    LOGD("Leave channel called for handle: %lld", handle);
    
    auto engine = get_engine(handle);
    if (!engine) {
        LOGE("Invalid engine handle: %lld", handle);
        return -1;
    }
    
    return engine->LeaveChannel();
}

// Setup Local Video
JNIEXPORT void JNICALL
Java_com_tasawwur_rtc_TasawwurRtcEngine_nativeSetupLocalVideo(JNIEnv* env, jobject thiz, 
                                                            jlong handle, jobject surface_view) {
    LOGD("Setup local video called for handle: %lld", handle);
    
    auto engine = get_engine(handle);
    if (!engine) {
        LOGE("Invalid engine handle: %lld", handle);
        return;
    }
    
    engine->SetupLocalVideo(surface_view);
}

// Setup Remote Video
JNIEXPORT void JNICALL
Java_com_tasawwur_rtc_TasawwurRtcEngine_nativeSetupRemoteVideo(JNIEnv* env, jobject thiz, 
                                                             jlong handle, jobject surface_view, 
                                                             jstring user_id) {
    LOGD("Setup remote video called for handle: %lld", handle);
    
    auto engine = get_engine(handle);
    if (!engine) {
        LOGE("Invalid engine handle: %lld", handle);
        return;
    }
    
    std::string user_id_str = jstring_to_string(env, user_id);
    engine->SetupRemoteVideo(surface_view, user_id_str);
}

// Mute Local Audio
JNIEXPORT void JNICALL
Java_com_tasawwur_rtc_TasawwurRtcEngine_nativeMuteLocalAudio(JNIEnv* env, jobject thiz, 
                                                           jlong handle, jboolean muted) {
    LOGD("Mute local audio called for handle: %lld, muted: %s", handle, muted ? "true" : "false");
    
    auto engine = get_engine(handle);
    if (!engine) {
        LOGE("Invalid engine handle: %lld", handle);
        return;
    }
    
    engine->MuteLocalAudio(muted == JNI_TRUE);
}

// Enable Local Video
JNIEXPORT void JNICALL
Java_com_tasawwur_rtc_TasawwurRtcEngine_nativeEnableLocalVideo(JNIEnv* env, jobject thiz, 
                                                             jlong handle, jboolean enabled) {
    LOGD("Enable local video called for handle: %lld, enabled: %s", handle, enabled ? "true" : "false");
    
    auto engine = get_engine(handle);
    if (!engine) {
        LOGE("Invalid engine handle: %lld", handle);
        return;
    }
    
    engine->EnableLocalVideo(enabled == JNI_TRUE);
}

} // extern "C"

