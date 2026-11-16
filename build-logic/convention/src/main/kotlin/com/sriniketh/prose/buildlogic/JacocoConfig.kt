package com.sriniketh.prose.buildlogic

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun Project.configureAndroidUnitTestJacoco() {
    pluginManager.apply("jacoco")
    extensions.configure(JacocoPluginExtension::class.java) {
        toolVersion = libs.version("jacocoVersion")
    }

    // Whether testDebugUnitTest's JaCoCo agent writes to AGP's own path
    // (outputs/unit_test_code_coverage/debugUnitTest/) or the vanilla jacoco plugin's default
    // (jacoco/testDebugUnitTest.exec) depends on plugin-application order, which a convention
    // plugin doesn't control the same way a project's own plugins { } block would - so track
    // both directories as tracked outputs (for correct build-cache behavior) and scan both for
    // the actual exec file, rather than assuming one.
    val jacocoExecDirs = listOf(
        layout.buildDirectory.dir("jacoco"),
        layout.buildDirectory.dir("outputs/unit_test_code_coverage/debugUnitTest")
    )

    tasks.withType<Test>().configureEach {
        if (name == "testDebugUnitTest") {
            jacocoExecDirs.forEach { outputs.dir(it) }
        }
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
            jacocoExecDirs.map { dir -> fileTree(dir) { include("*.exec") } }
        )
    }
}
