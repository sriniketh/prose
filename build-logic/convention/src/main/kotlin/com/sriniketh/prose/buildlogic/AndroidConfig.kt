package com.sriniketh.prose.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

internal fun Project.configureAndroidCommon(extension: CommonExtension) {
    with(extension) {
        compileSdk = libs.version("compileSdkVersion").toInt()

        defaultConfig.apply {
            minSdk = libs.version("minSdkVersion").toInt()
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildTypes.getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        buildFeatures.buildConfig = true
    }
}
