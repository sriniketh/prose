import java.io.FileInputStream
import java.util.Properties

plugins {
	id("prose.android.library")
	id("prose.android.hilt")
	alias(libs.plugins.kotlin.serialization)
}

val apikeyPropertiesFile = file("apikey.properties")
val apikeyProperties = Properties()
if (!apikeyPropertiesFile.exists()) {
	throw GradleException(
		"core-network/apikey.properties is missing. Create it with a BOOKS_API_KEY entry, " +
			"e.g. BOOKS_API_KEY=\"your-google-books-api-key\" " +
			"(see AGENTS.md's \"API Key Setup\" section)."
	)
}
apikeyProperties.load(FileInputStream(apikeyPropertiesFile))

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
	compilerOptions {
		optIn.add("kotlin.RequiresOptIn")
	}
}

android {
	defaultConfig {
		buildConfigField("String", "BOOKS_API_KEY", apikeyProperties["BOOKS_API_KEY"] as String)
	}

	buildFeatures {
		buildConfig = true
	}

	namespace = "com.sriniketh.prose.core_network"
}

dependencies {
	implementation(libs.android.core.ktx)
	implementation(libs.coroutines.android)

	implementation(libs.okhttp.logging.interceptor)
	implementation(libs.retrofit)
	implementation(libs.retrofit.kotlinx.serialization.converter)
	implementation(libs.kotlinx.serialization.json)

	testImplementation(libs.coroutines.test)
}
