package com.sriniketh.prose.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun Project.configureAndroidUnitTestJacoco() {
    pluginManager.apply("jacoco")
    extensions.configure(JacocoPluginExtension::class.java) {
        toolVersion = libs.version("jacocoVersion")
    }

    tasks.register("jacocoTestReport", JacocoReport::class.java) {
        dependsOn("testDebugUnitTest")
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
        sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
        classDirectories.setFrom(
            fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes"))
        )
        executionData.setFrom(
            fileTree(layout.buildDirectory.dir("outputs/unit_test_code_coverage/debugUnitTest")) {
                include("*.exec")
            }
        )
    }
}
