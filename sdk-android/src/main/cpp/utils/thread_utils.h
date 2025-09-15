#ifndef TASAWWUR_THREAD_UTILS_H
#define TASAWWUR_THREAD_UTILS_H

#include <string>

namespace tasawwur {

/**
 * Thread priority levels.
 */
enum class ThreadPriority {
    LOW,
    NORMAL,
    HIGH,
    REALTIME
};

/**
 * Sets the name of the current thread.
 * @param name The thread name (max 15 characters on Linux)
 */
void SetThreadName(const std::string& name);

/**
 * Sets the priority of the current thread.
 * @param priority The desired thread priority
 */
void SetThreadPriority(ThreadPriority priority);

/**
 * Gets the current thread ID.
 * @return The current thread ID
 */
long GetCurrentThreadId();

/**
 * Sleep for the specified number of milliseconds.
 * @param milliseconds Number of milliseconds to sleep
 */
void SleepMs(int milliseconds);

} // namespace tasawwur

#endif // TASAWWUR_THREAD_UTILS_H

