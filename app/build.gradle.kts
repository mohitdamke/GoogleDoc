plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.googledoc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.googledoc"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField(
            "String",
            "ServerClientID",
            "\"${project.findProperty("ServerClientID")}\"")
        buildFeatures {
            buildConfig = true
        }
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    // Loading Bar
    implementation("androidx.compose.material3:material3:1.2.0-rc01")

    // Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.0-beta05")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))

    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("com.google.android.gms:play-services-auth:20.5.0")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.0")

    // Rich Text Editor
    implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc06")

    // Pdf File
    implementation("com.itextpdf:itext7-core:7.2.6")

    // Word File
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    implementation("androidx.room:room-runtime:2.5.0")  // Ensure you have the latest Room dependency
    kapt("androidx.room:room-compiler:2.5.0")  // Add the kapt annotation processor for Room
    implementation("androidx.room:room-ktx:2.5.0")  // Op

    implementation("androidx.appcompat:appcompat:1.7.0") // Check for the latest version
}