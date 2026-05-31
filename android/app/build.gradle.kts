plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
}

android {
    namespace = "co.theportman.way_of_the_goat"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        // MUST match the live Play listing to ship updates to existing users.
        applicationId = "co.theportman.way_of_the_goat"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            // TODO(Phase 6): wire the existing Play upload/signing keystore here.
            //   The same signing key as the current listing is REQUIRED to publish updates.
            //   Read keystore path + credentials from local.properties or env vars and set
            //   signingConfig = signingConfigs.getByName("release"). Until then, release
            //   builds are unsigned and debug builds use the default debug key.
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    // Persistence — Room (ADR 002). Wired in Phase 4 once ratified.
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Compose tooling (debug only)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Unit tests
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // Detekt Compose rules
    detektPlugins(libs.detekt.compose)
}

detekt {
    config.setFrom(files("$rootDir/../detekt.yml"))
    source.setFrom("src/main/kotlin", "src/test/kotlin")
}
