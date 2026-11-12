plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
    alias(libs.plugins.dagger.hilt)
}

android {
    namespace = "com.codelabs.gardahijau"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.codelabs.gardahijau"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Mode demo: true = semua request API dijawab data contoh (lihat model/remote/mock).
        buildConfigField("boolean", "USE_MOCK_API", "false")
    }

    buildTypes {
        debug {
            // Server smartmaggot.my.id sedang mati -> build debug memakai data contoh.
            // Ubah ke "false" kalau server sudah menyala.
            buildConfigField("boolean", "USE_MOCK_API", "true")
        }
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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)

    //SplashScreen
    implementation(libs.androidx.core.splashscreen)

    //TimeLineView
    implementation(libs.timelineview)


    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Room
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Grafik - Data2viz
    implementation("com.diogobernardino:williamchart:3.10.1")

    // data store
    implementation(libs.datastore.preferences)

}