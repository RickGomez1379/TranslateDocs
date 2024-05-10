plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.translatedocs"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.translatedocs"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    // Use this dependency to bundle the model with your app
    implementation ("com.google.mlkit:language-id:17.0.5")

    // Use this dependency to use the dynamically downloaded model in Google Play Services
    implementation ("com.google.android.gms:play-services-mlkit-language-id:17.0.0")

    // Use this dependency to use the translate text
    implementation ("com.google.mlkit:translate:17.0.2")

    // To recognize Latin script
    implementation ("com.google.mlkit:text-recognition:16.0.0")
    
    implementation(libs.play.services.vision)
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}