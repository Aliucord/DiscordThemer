plugins {
    id("com.android.application")
}

android {
    compileSdk = 30

    defaultConfig {
        applicationId = "com.aliucord.themer"
        minSdk = 21
        targetSdk = 30
        versionCode = 4
        versionName = "0.0.3"
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
    implementation("androidx.preference:preference:1.1.1")
    implementation("com.google.android.material:material:1.4.0")

    compileOnly("de.robv.android.xposed:api:82")
    implementation("com.github.discord:ColorPicker:1.1.2")
    implementation("com.google.code.gson:gson:2.8.7")
}
