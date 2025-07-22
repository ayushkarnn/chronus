plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.devtools.ksp)
    id("com.google.gms.google-services")
}

android {
    namespace = "ayush.chronos"
    compileSdk = 36

    defaultConfig {
        applicationId = "ayush.chronos"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":prefs"))
    implementation(project(":network"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("io.coil-kt:coil-compose:2.5.0")

// Kotlin Coroutines & Flow
    implementation(libs.kotlinx.coroutines.android)

// ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.9.0")

// Optional: Animation support for navigation
    implementation("androidx.compose.animation:animation:1.8.2")

// If you need navigation with ViewModel support
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation(libs.lottie)
    implementation(libs.retrofit)

    // Hilt dependencies
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")


    // Google Maps for Compose
    implementation("com.google.maps.android:maps-compose:2.15.0")
    // Google Identity API for sign-in
    implementation("com.google.android.gms:play-services-identity:18.0.1")
    implementation("com.google.android.libraries.places:places:3.3.0")
    // Accompanist permissions for location permissions
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    // Play services maps and location
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.33.2-alpha")

    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.gms:play-services-auth:21.1.0")

    implementation("androidx.compose.material3:material3:1.3.2")

    implementation("androidx.work:work-runtime-ktx:2.9.0")
}