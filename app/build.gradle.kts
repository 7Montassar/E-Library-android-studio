



plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.e_library"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.e_library"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/*.SF"
            excludes += "META-INF/*.DSA"
            excludes += "META-INF/*.RSA"
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.glide)
    implementation(libs.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    //Gson for JSON parsing
    implementation(libs.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
