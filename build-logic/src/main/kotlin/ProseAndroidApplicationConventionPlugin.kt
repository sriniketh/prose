import com.android.build.api.dsl.ApplicationExtension
import com.sriniketh.prose.buildlogic.library
import com.sriniketh.prose.buildlogic.libs
import com.sriniketh.prose.buildlogic.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

abstract class ProseAndroidApplicationConventionPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		with(target) {
			apply(plugin = "com.android.application")

			extensions.configure<ApplicationExtension> {
				compileSdk = libs.version("compileSdkVersion").toInt()

				defaultConfig {
					minSdk = libs.version("minSdkVersion").toInt()
					targetSdk = libs.version("targetSdkVersion").toInt()
				}

				buildTypes {
					release {
						isMinifyEnabled = false
						proguardFiles(
							getDefaultProguardFile("proguard-android-optimize.txt"),
							"proguard-rules.pro"
						)
					}
				}
			}

			dependencies {
				"testImplementation"(libs.library("junit"))
			}
		}
	}
}
