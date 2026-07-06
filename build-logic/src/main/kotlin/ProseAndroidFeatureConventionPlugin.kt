import com.android.build.api.dsl.LibraryExtension
import com.sriniketh.prose.buildlogic.library
import com.sriniketh.prose.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

abstract class ProseAndroidFeatureConventionPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		with(target) {
			apply(plugin = "prose.android.library")
			apply(plugin = "prose.android.compose")
			apply(plugin = "prose.android.hilt")

			extensions.configure<LibraryExtension> {
				defaultConfig {
					testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
				}
			}

			dependencies {
				"implementation"(libs.library("lifecycle-runtime-compose"))
				"implementation"(libs.library("lifecycle-viewmodel-compose"))
				"implementation"(libs.library("hilt-navigation-compose"))

				val composeBom = platform(libs.library("compose-bom"))
				"androidTestImplementation"(composeBom)
				"androidTestImplementation"(libs.library("compose-junit"))
				"debugImplementation"(libs.library("compose-test-manifest"))
				"androidTestImplementation"(libs.library("android-junit"))

				"testImplementation"(libs.library("coroutines-test"))
				"testImplementation"(libs.library("cashapp-turbine"))
			}
		}
	}
}
