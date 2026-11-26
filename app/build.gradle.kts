import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.pvot.android.compose)
}

val releaseKeystore = rootProject.file("keystore.properties")

android {
    namespace = "org.prauga.keyboard"
    compileSdk { version = release(37) { minorApiLevel = 1 } }
    ndkVersion = "25.1.8937393"

    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")

    defaultConfig {
        applicationId = "org.prauga.keyboard"
        minSdk = 30
        targetSdk = 36
        versionCode = rootProject.extra["computedVersionCode"] as Int
        versionName = rootProject.extra["computedVersionName"] as String

        testApplicationId = "org.prauga.keyboard.tests"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = false
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("shared.keystore")
        }
        if (releaseKeystore.exists()) {
            val keystore = Properties().apply { releaseKeystore.inputStream().use { load(it) } }
            create("release") {
                storeFile = rootProject.file(keystore.getProperty("storeFile"))
                storePassword = keystore.getProperty("storePassword")
                keyAlias = keystore.getProperty("keyAlias")
                keyPassword = keystore.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
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

    bundle {
        language {
            enableSplit = false
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
    implementation(libs.pvot.design.system)
    implementation(libs.pvot.design.system.views)
    implementation(libs.pvot.core.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.dexmaker)
    androidTestImplementation(libs.dexmaker.mockito)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.androidx.annotation)
}

val injectedSigning = providers.gradleProperty("android.injected.signing.store.file").isPresent

tasks.matching { it.name == "bundleRelease" }.configureEach {
    doFirst {
        check(releaseKeystore.exists() || injectedSigning) {
            "No upload key: sign from Android Studio or add keystore.properties"
        }
    }
}
