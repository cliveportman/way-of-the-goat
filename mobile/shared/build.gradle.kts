plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    // iOS targets - disabled due to Kotlin Native dependency issues
    // To enable iOS: fix Xcode setup and Kotlin Native repository access
    val enableIosTargets = true // Set to true when environment issues are resolved
    if (enableIosTargets) {
        iosSimulatorArm64() // Start with just simulator target
        // iosX64()  // Uncomment when needed
        // iosArm64()  // Uncomment when needed
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }

        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.8.2")
        }

        if (enableIosTargets) {
            iosMain.dependencies {
            }
        }
    }
}

android {
    namespace = "co.cliveportman.wayofthegoat.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}