package com.sriniketh.prose.buildlogic

import com.android.build.api.dsl.CommonExtension
import com.android.compose.screenshot.tasks.PreviewScreenshotValidationTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

class AndroidScreenshotConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.withPlugin("com.android.base") {
            pluginManager.apply("com.android.compose.screenshot")
            extensions.getByType<CommonExtension>()
                .experimentalProperties["android.experimental.enableScreenshotTest"] = true

            tasks.withType<PreviewScreenshotValidationTask>().configureEach {
                testEngineInput.threshold.set(SCREENSHOT_DIFFERENCE_THRESHOLD)
            }

            dependencies {
                "screenshotTestImplementation"(platform(libs.findLibrary("compose-bom").get()))
                "screenshotTestImplementation"(libs.findLibrary("compose-ui-tooling").get())
                "screenshotTestImplementation"(libs.findLibrary("screenshot-validation-api").get())
            }
        }
    }

    private companion object {
        const val SCREENSHOT_DIFFERENCE_THRESHOLD = 0.00001f
    }
}
