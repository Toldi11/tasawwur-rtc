#include "logging.h"
#include <android/log.h>
#include <cstdarg>

namespace tasawwur {

static const char* TAG = "TasawwurRTC";
static LogLevel g_log_level = LogLevel::INFO;

void InitializeLogging() {
    __android_log_print(ANDROID_LOG_INFO, TAG, "Logging initialized");
}

void SetLogLevel(LogLevel level) {
    g_log_level = level;
}

LogLevel GetLogLevel() {
    return g_log_level;
}

void LogMessage(LogLevel level, const char* file, int line, const char* format, ...) {
    if (level < g_log_level) {
        return;
    }
    
    android_LogPriority priority;
    switch (level) {
        case LogLevel::VERBOSE:
            priority = ANDROID_LOG_VERBOSE;
            break;
        case LogLevel::DEBUG:
            priority = ANDROID_LOG_DEBUG;
            break;
        case LogLevel::INFO:
            priority = ANDROID_LOG_INFO;
            break;
        case LogLevel::WARN:
            priority = ANDROID_LOG_WARN;
            break;
        case LogLevel::ERROR:
            priority = ANDROID_LOG_ERROR;
            break;
        default:
            priority = ANDROID_LOG_INFO;
            break;
    }
    
    char buffer[1024];
    va_list args;
    va_start(args, format);
    vsnprintf(buffer, sizeof(buffer), format, args);
    va_end(args);
    
    // Extract filename from full path
    const char* filename = strrchr(file, '/');
    if (filename) {
        filename++;
    } else {
        filename = file;
    }
    
    __android_log_print(priority, TAG, "[%s:%d] %s", filename, line, buffer);
}

} // namespace tasawwur

