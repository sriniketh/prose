plugins {
	`kotlin-dsl`
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

dependencies {
	compileOnly(libs.plugins.android.application.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
	compileOnly(libs.plugins.android.library.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
	compileOnly(libs.plugins.kotlin.compose.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
	compileOnly(libs.plugins.ksp.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
	compileOnly(libs.plugins.hilt.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
}

gradlePlugin {
	plugins {
		register("proseAndroidLibrary") {
			id = "prose.android.library"
			implementationClass = "ProseAndroidLibraryConventionPlugin"
		}
		register("proseAndroidApplication") {
			id = "prose.android.application"
			implementationClass = "ProseAndroidApplicationConventionPlugin"
		}
		register("proseAndroidCompose") {
			id = "prose.android.compose"
			implementationClass = "ProseAndroidComposeConventionPlugin"
		}
		register("proseAndroidHilt") {
			id = "prose.android.hilt"
			implementationClass = "ProseAndroidHiltConventionPlugin"
		}
		register("proseAndroidFeature") {
			id = "prose.android.feature"
			implementationClass = "ProseAndroidFeatureConventionPlugin"
		}
	}
}
