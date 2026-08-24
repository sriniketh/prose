package com.sriniketh.prose.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // not applying agp - plugins are composable and by not applying agp, this plugin can be used both in library and in app modules
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        // registering a deferred callback enabling to add compose plugin before say a library plugin (library plugin registers LibraryExtension which is a subtype of CommonExtension)
        pluginManager.withPlugin("com.android.base") {
            extensions.getByType<CommonExtension>().buildFeatures.compose = true
        }

        dependencies {
            val bom = platform(libs.findLibrary("compose-bom").get())
            "implementation"(bom)
            "implementation"(libs.findBundle("compose").get())
            "debugImplementation"(libs.findLibrary("compose-ui-tooling").get())
            "androidTestImplementation"(bom)
            "androidTestImplementation"(libs.findLibrary("android-junit").get())
            "androidTestImplementation"(libs.findLibrary("android-test-runner").get())
            "debugImplementation"(libs.findLibrary("compose-test-manifest").get())
        }
    }
}
