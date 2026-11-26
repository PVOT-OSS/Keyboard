plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "org.prauga.keyboard"
    compileSdk = 35
    ndkVersion = "25.1.8937393"

    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")

    defaultConfig {
        applicationId = "org.prauga.keyboard"
        minSdk = 30
        targetSdk = 34
        versionCode = 30
        versionName = "1.0"

        testApplicationId = "org.prauga.keyboard.tests"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = false
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("shared.keystore")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            isMinifyEnabled = false
        }
        release {
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    externalNativeBuild {
        ndkBuild {
            path = file("src/main/jni/AndroidNdk.mk")
        }
    }

    androidResources {
        noCompress += "dict"
    }

    lint {
        checkReleaseBuilds = false
        lintConfig = file("lint.xml")
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation(libs.jsr305)
    implementation(libs.androidx.viewpager2)
    implementation(libs.material)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.autofill)

    testImplementation(libs.junit)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.dexmaker)
    androidTestImplementation(libs.dexmaker.mockito)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.androidx.annotation)
}
