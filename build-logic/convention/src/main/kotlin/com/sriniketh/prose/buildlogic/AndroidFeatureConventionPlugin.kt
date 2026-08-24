package com.sriniketh.prose.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("prose.android.library")
        pluginManager.apply("prose.android.compose")
        pluginManager.apply("prose.android.hilt")

        dependencies {
            "implementation"(project(":core-design"))
            "implementation"(project(":core-data"))
            "implementation"(project(":core-models"))

            "implementation"(libs.findLibrary("lifecycle-runtime-compose").get())
            "implementation"(libs.findLibrary("lifecycle-viewmodel-compose").get())
            "implementation"(libs.findLibrary("hilt-lifecycle-viewmodel-compose").get())
            "implementation"(libs.findLibrary("kotlinx-collections-immutable").get())

            "testImplementation"(libs.findLibrary("junit").get())
            "testImplementation"(libs.findLibrary("coroutines-test").get())
            "testImplementation"(libs.findLibrary("cashapp-turbine").get())
        }
    }
}
