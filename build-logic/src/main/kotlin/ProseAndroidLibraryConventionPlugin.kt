import com.android.build.api.dsl.LibraryExtension
import com.sriniketh.prose.buildlogic.library
import com.sriniketh.prose.buildlogic.libs
import com.sriniketh.prose.buildlogic.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

abstract class ProseAndroidLibraryConventionPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		with(target) {
			apply(plugin = "com.android.library")

			extensions.configure<LibraryExtension> {
				compileSdk = libs.version("compileSdkVersion").toInt()

				defaultConfig {
					minSdk = libs.version("minSdkVersion").toInt()
					consumerProguardFiles("consumer-rules.pro")
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
