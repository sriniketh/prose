package com.sriniketh.prose.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            configureAndroidCommon(this)
            defaultConfig.targetSdk = libs.version("targetSdkVersion").toInt()
            buildFeatures.buildConfig = true
            val defaultProguardFile = getDefaultProguardFile("proguard-android-optimize.txt")
            buildTypes {
                release {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(defaultProguardFile, "proguard-rules.pro")
                }
            }
        }
        configureKotlin()
        configureAndroidUnitTestJacoco()

        dependencies {
            "androidTestImplementation"(libs.findLibrary("android-junit").get())
            "androidTestImplementation"(libs.findLibrary("android-test-runner").get())
        }
    }
}
