package com.sriniketh.prose.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("java-library")
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        configureKotlin()

        pluginManager.apply("jacoco")
        extensions.configure(JacocoPluginExtension::class.java) {
            toolVersion = libs.version("jacocoVersion")
        }
        tasks.withType<JacocoReport>().configureEach {
            reports {
                xml.required.set(true)
                html.required.set(true)
            }
        }
    }
}
