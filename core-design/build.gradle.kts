plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

android {
	compileSdk = libs.versions.compileSdkVersion.get().toInt()

	defaultConfig {
		minSdk = libs.versions.minSdkVersion.get().toInt()

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
	buildFeatures {
		buildConfig = true
		compose = true
	}

	namespace = "com.sriniketh.core_design"
}

dependencies {
	val composeBom = platform(libs.compose.bom)
	implementation(composeBom)
	implementation(libs.bundles.compose)
	debugImplementation(libs.compose.ui.tooling)
	implementation(libs.google.fonts)

	androidTestImplementation(composeBom)
	androidTestImplementation(libs.compose.junit)
	debugImplementation(libs.compose.test.manifest)

	testImplementation(libs.junit)
}
