plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ir.srp.webrtc"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        version = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    // WebRTC
    val webrtc_version = "1.0.23995"
    implementation("org.webrtc:google-webrtc:$webrtc_version")

    // okhttp3
    val okhttp3_version = "4.12.0"
    implementation("com.squareup.okhttp3:okhttp:$okhttp3_version")

    // Gson
    val gson_version = "2.10.1"
    implementation("com.google.code.gson:gson:$gson_version")

    // Coroutines
    val coroutines_version = "1.7.3"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
}