import com.sriniketh.prose.buildlogic.library
import com.sriniketh.prose.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

abstract class ProseAndroidHiltConventionPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		with(target) {
			apply(plugin = "com.google.dagger.hilt.android")
			apply(plugin = "com.google.devtools.ksp")

			dependencies {
				"implementation"(libs.library("hilt-android"))
				"ksp"(libs.library("hilt-compiler"))
			}
		}
	}
}
