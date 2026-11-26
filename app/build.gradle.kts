plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.gyaanpath.playstorebrowser"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gyaanpath.playstorebrowser"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}
