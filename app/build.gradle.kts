plugins {
    id("com.android.application")
}

android {
    namespace = "com.velocityrush.game"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.velocityrush.game"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }

        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
