#include "thread_utils.h"
#include "logging.h"
#include <pthread.h>
#include <unistd.h>
#include <sys/prctl.h>

namespace tasawwur {

void SetThreadName(const std::string& name) {
    if (name.length() > 15) {
        // Linux thread names are limited to 16 characters (including null terminator)
        std::string truncated_name = name.substr(0, 15);
        prctl(PR_SET_NAME, truncated_name.c_str(), 0, 0, 0);
        LOG_DEBUG("Thread name set to: %s (truncated from %s)", truncated_name.c_str(), name.c_str());
    } else {
        prctl(PR_SET_NAME, name.c_str(), 0, 0, 0);
        LOG_DEBUG("Thread name set to: %s", name.c_str());
    }
}

void SetThreadPriority(ThreadPriority priority) {
    int policy;
    struct sched_param param;
    
    switch (priority) {
        case ThreadPriority::LOW:
            policy = SCHED_OTHER;
            param.sched_priority = 0;
            setpriority(PRIO_PROCESS, 0, 10); // Lower priority
            break;
            
        case ThreadPriority::NORMAL:
            policy = SCHED_OTHER;
            param.sched_priority = 0;
            setpriority(PRIO_PROCESS, 0, 0); // Normal priority
            break;
            
        case ThreadPriority::HIGH:
            policy = SCHED_OTHER;
            param.sched_priority = 0;
            setpriority(PRIO_PROCESS, 0, -10); // Higher priority
            break;
            
        case ThreadPriority::REALTIME:
            policy = SCHED_FIFO;
            param.sched_priority = 1; // Minimum real-time priority
            break;
    }
    
    pthread_t current_thread = pthread_self();
    int result = pthread_setschedparam(current_thread, policy, &param);
    
    if (result == 0) {
        LOG_DEBUG("Thread priority set successfully");
    } else {
        LOG_WARN("Failed to set thread priority: %d", result);
    }
}

long GetCurrentThreadId() {
    return static_cast<long>(pthread_self());
}

void SleepMs(int milliseconds) {
    usleep(milliseconds * 1000);
}

} // namespace tasawwur

