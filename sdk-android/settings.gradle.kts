pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // For WebRTC native libraries
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "tasawwur-rtc-sdk"

