import java.util.Properties

plugins {
    alias(libs.plugins.pvot.android.application)
    alias(libs.plugins.pvot.android.compose)
}

val releaseKeystore = rootProject.file("keystore.properties")

android {
    namespace = "org.prauga.keyboard"
    ndkVersion = "25.1.8937393"

    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")

    defaultConfig {
        applicationId = "org.prauga.keyboard"
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String

        testApplicationId = "org.prauga.keyboard.tests"
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
            signingConfig =
                signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
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

    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.dexmaker)
    androidTestImplementation(libs.dexmaker.mockito)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.androidx.annotation)
}

val injectedSigning = providers.gradleProperty("android.injected.signing.store.file").isPresent

tasks
    .matching { it.name == "bundleRelease" }
    .configureEach {
        doFirst {
            check(releaseKeystore.exists() || injectedSigning) {
                "No upload key: sign from Android Studio or add keystore.properties"
            }
        }
    }
