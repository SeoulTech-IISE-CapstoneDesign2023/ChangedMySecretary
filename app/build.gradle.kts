plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.design"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fastcampus.part3.design"
        minSdk = 30
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("android.arch.lifecycle:livedata-core:1.1.1")
    //retrofit2
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    //okhttp3
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    //coil 이미지처리
    implementation("io.coil-kt:coil:2.2.2")
    // 네이버 지도 SDK
    implementation ("com.naver.maps:map-sdk:3.17.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    //lottie 애니메이션이미지
    implementation ("com.airbnb.android:lottie:6.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //Calendar custom
    implementation ("com.prolificinteractive:material-calendarview:1.4.3")
    //firebase
    implementation ("com.google.firebase:firebase-bom:32.2.3")
    implementation ("com.google.firebase:firebase-auth-ktx:22.1.1")
    implementation ("com.google.firebase:firebase-database-ktx:20.2.2")
}