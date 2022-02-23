val accompanistVersion = "0.23.0"
val composeVersion = "1.1.0"
val composeDestinationsVersion = "1.3.1-beta"

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.devtools.ksp") version "1.6.10-1.0.3"
}

kotlin {
    sourceSets {
        debug {
            kotlin.srcDir("build/generated/ksp/debug/kotlin")
        }
        release {
            kotlin.srcDir("build/generated/ksp/release/kotlin")
        }
    }
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.aliucord.themer"
        minSdk = 21
        targetSdk = 29
        versionCode = 5
        versionName = "1.0.0-alpha1"

        buildConfigField("String", "TAG", "\"DiscordThemer\"")
        buildConfigField("String", "SUPPORT_SERVER", "\"EsNDvBaHVU\"")
        buildConfigField("String", "PREFERENCES_NAME", "\"themer_preferences\"")
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
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
}

dependencies {
    // core dependencies
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("com.google.android.material:material:1.5.0")

    // compose dependencies
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")

    // accompanist dependencies
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")

    // navigation
    implementation("io.github.raamcosta.compose-destinations:animations-core:$composeDestinationsVersion")
    ksp("io.github.raamcosta.compose-destinations:ksp:$composeDestinationsVersion")

    // other dependencies
    compileOnly("de.robv.android.xposed:api:82")
    implementation("com.github.discord:ColorPicker:1.1.2")
    implementation("com.github.topjohnwu.libsu:core:3.2.1")
}
