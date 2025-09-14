#ifndef TASAWWUR_LOGGING_H
#define TASAWWUR_LOGGING_H

namespace tasawwur {

enum class LogLevel {
    VERBOSE = 0,
    DEBUG = 1,
    INFO = 2,
    WARN = 3,
    ERROR = 4
};

void InitializeLogging();
void SetLogLevel(LogLevel level);
LogLevel GetLogLevel();
void LogMessage(LogLevel level, const char* file, int line, const char* format, ...);

} // namespace tasawwur

// Logging macros
#define LOG_VERBOSE(...) tasawwur::LogMessage(tasawwur::LogLevel::VERBOSE, __FILE__, __LINE__, __VA_ARGS__)
#define LOG_DEBUG(...) tasawwur::LogMessage(tasawwur::LogLevel::DEBUG, __FILE__, __LINE__, __VA_ARGS__)
#define LOG_INFO(...) tasawwur::LogMessage(tasawwur::LogLevel::INFO, __FILE__, __LINE__, __VA_ARGS__)
#define LOG_WARN(...) tasawwur::LogMessage(tasawwur::LogLevel::WARN, __FILE__, __LINE__, __VA_ARGS__)
#define LOG_ERROR(...) tasawwur::LogMessage(tasawwur::LogLevel::ERROR, __FILE__, __LINE__, __VA_ARGS__)

#endif // TASAWWUR_LOGGING_H
