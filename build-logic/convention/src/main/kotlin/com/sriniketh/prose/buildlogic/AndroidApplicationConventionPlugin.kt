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
        }
        configureKotlin()
        configureAndroidUnitTestJacoco()

        dependencies {
            "androidTestImplementation"(libs.findLibrary("android-junit").get())
            "androidTestImplementation"(libs.findLibrary("android-test-runner").get())
        }
    }
}
