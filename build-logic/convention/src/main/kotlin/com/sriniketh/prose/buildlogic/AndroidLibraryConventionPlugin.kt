package com.sriniketh.prose.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            configureAndroidCommon(this)
            defaultConfig.consumerProguardFiles("consumer-rules.pro")
        }
        configureKotlin()

        dependencies {
            "androidTestImplementation"(libs.findLibrary("android-junit").get())
            "androidTestImplementation"(libs.findLibrary("android-test-runner").get())
        }
    }
}
