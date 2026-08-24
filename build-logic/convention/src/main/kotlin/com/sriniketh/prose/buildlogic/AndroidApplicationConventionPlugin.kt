package com.sriniketh.prose.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            configureAndroidCommon(this)
            defaultConfig.targetSdk = libs.version("targetSdkVersion").toInt()
            buildFeatures.buildConfig = true
        }
        configureKotlin()

        // AGP pins androidx.concurrent:concurrent-futures for application modules' androidTest
        // classpaths, which conflicts with the newer version androidx.test.ext:junit requires.
        // Library modules don't enforce this pin, only applications do, so this override lives here.
        configurations.matching { it.name.endsWith("AndroidTestRuntimeClasspath") }.configureEach {
            resolutionStrategy.force(
                "androidx.concurrent:concurrent-futures:${libs.version("androidx-concurrent-futures-version")}"
            )
        }
    }
}
