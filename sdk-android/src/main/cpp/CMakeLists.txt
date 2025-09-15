# CMakeLists.txt for Tasawwur RTC Android SDK
cmake_minimum_required(VERSION 3.22.1)

project("tasawwur-rtc")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Enable all warnings and treat them as errors in debug builds
if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -Werror")
endif()

# Add optimization flags for release builds
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -DNDEBUG -ffast-math")
endif()

# Find required packages
find_library(log-lib log)
find_library(android-lib android)

# WebRTC library configuration
# Note: In a real implementation, you would download and link the prebuilt WebRTC library
# For this example, we'll create placeholder functions
set(WEBRTC_ROOT_DIR ${CMAKE_CURRENT_SOURCE_DIR}/webrtc)
set(WEBRTC_INCLUDE_DIR ${WEBRTC_ROOT_DIR}/include)
set(WEBRTC_LIB_DIR ${WEBRTC_ROOT_DIR}/lib/${ANDROID_ABI})

# Include directories
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${WEBRTC_INCLUDE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

# Source files
set(NATIVE_SOURCES
    # JNI interface
    jni_interface.cpp
    
    # Core engine
    rtc_engine_impl.cpp
    
    # WebRTC wrapper
    webrtc_wrapper.cpp
    
    # Signaling client
    signaling_client.cpp
    
    # Media management
    media_manager.cpp
    
    # Utilities
    utils/logging.cpp
    utils/json_helper.cpp
    utils/thread_utils.cpp
)

# Create the native library
add_library(tasawwur-rtc SHARED ${NATIVE_SOURCES})

# Link libraries
target_link_libraries(tasawwur-rtc
    ${log-lib}
    ${android-lib}
    # WebRTC libraries would be linked here
    # ${WEBRTC_LIB_DIR}/libwebrtc.a
)

# Compiler definitions
target_compile_definitions(tasawwur-rtc PRIVATE
    WEBRTC_ANDROID
    WEBRTC_POSIX
    ANDROID
)

# Set output directory for different ABIs
set_target_properties(tasawwur-rtc PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
)

# Copy WebRTC native libraries (if they exist)
# This would typically copy prebuilt WebRTC libraries to the output directory

