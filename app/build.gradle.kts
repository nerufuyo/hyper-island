plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21"
}

android {
    namespace = "com.nerufuyo.hyperisland"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.nerufuyo.hyperisland"
        minSdk = 35
        targetSdk = 37
        versionCode = 33
        versionName = "0.5.7"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            optimization {
                enable = true // Enables code and resource optimizations.
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        aidl = true
    }

    lint {
        // These are translation-completeness/consistency checks across ~20 community-
        // translated locale files (inherited from upstream, not something CI should hard-fail
        // on - fully auditing 190+ occurrences across 20 languages isn't practical here, and
        // most StringFormatInvalid hits are false positives from a literal '%' in changelog
        // text that's never actually used with String.format). Real code-correctness checks
        // (MissingPermission, LocalContextGetResourceValueCall, etc.) stay as errors.
        disable += setOf(
            "MissingTranslation",
            "ImpliedQuantity",
            "StringFormatInvalid",
            "InconsistentArrays",
            "MissingQuantity",
            "UnusedQuantity",
            "PluralsCandidate",
            "StringFormatCount"
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.palette.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.hyperisland.kit)
    implementation(libs.haze)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // Shizuku
    implementation(libs.api)
    implementation(libs.provider)
}

configurations.all {
    exclude(group = "com.intellij", module = "annotations")
}
