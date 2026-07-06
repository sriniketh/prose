import com.android.build.api.dsl.CommonExtension
import com.sriniketh.prose.buildlogic.bundle
import com.sriniketh.prose.buildlogic.library
import com.sriniketh.prose.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

abstract class ProseAndroidComposeConventionPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		with(target) {
			apply(plugin = "org.jetbrains.kotlin.plugin.compose")

			extensions.configure<CommonExtension> {
				buildFeatures.compose = true
			}

			dependencies {
				val composeBom = platform(libs.library("compose-bom"))
				"implementation"(composeBom)
				"implementation"(libs.bundle("compose"))
				"debugImplementation"(libs.library("compose-ui-tooling"))
			}
		}
	}
}
