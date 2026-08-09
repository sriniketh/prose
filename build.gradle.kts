// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.kotlin.jvm) apply false
	alias(libs.plugins.kotlin.compose) apply false
	alias(libs.plugins.kotlin.serialization) apply false
	alias(libs.plugins.ksp) apply false
	alias(libs.plugins.hilt) apply false
	alias(libs.plugins.android.navigation.safe.args) apply false
	alias(libs.plugins.ktlint) apply false
	alias(libs.plugins.detekt) apply false
}

val ktlintToolVersion = libs.versions.ktlintToolVersion.get()

subprojects {
	tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
		compilerOptions {
			freeCompilerArgs.add("-Xannotation-default-target=param-property")
		}
	}

	apply(plugin = "org.jlleitschuh.gradle.ktlint")
	apply(plugin = "io.gitlab.arturbosch.detekt")

	extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
		version.set(ktlintToolVersion)
		android.set(true)
		baseline.set(file("$projectDir/config/ktlint/baseline.xml"))
	}

	extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
		buildUponDefaultConfig = true
		parallel = true
		config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
		baseline = file("$projectDir/config/detekt/baseline.xml")
	}

	tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
		reports {
			xml.required.set(true)
			html.required.set(true)
			sarif.required.set(true)
			txt.required.set(false)
		}
	}
}

tasks.register<Delete>("clean") {
	group = "Build"
	delete(rootProject.layout.buildDirectory)
}
