import java.util.Properties
import java.io.FileInputStream

plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)
}

val apikeyPropertiesFile = file("apikey.properties")
val apikeyProperties = Properties()
apikeyProperties.load(FileInputStream(apikeyPropertiesFile))

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

android {
	compileSdk = libs.versions.compileSdkVersion.get().toInt()

	defaultConfig {
		minSdk = libs.versions.minSdkVersion.get().toInt()

		buildConfigField("String", "BOOKS_API_KEY", apikeyProperties["BOOKS_API_KEY"] as String)
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
	kotlinOptions {
		freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
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
	implementation(libs.retrofit.moshi.converter)
	implementation(libs.moshi)
	ksp(libs.moshi.codegen)

	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)

	testImplementation(libs.junit)
	testImplementation(libs.coroutines.test)
}
