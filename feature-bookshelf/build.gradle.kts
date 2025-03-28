plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)
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
		viewBinding = true
		buildConfig = true
		compose = true
	}

	namespace = "com.sriniketh.feature_bookshelf"
}

dependencies {

	implementation(project(":core-design"))
	implementation(project(":core-data"))
	implementation(project(":core-models"))
	implementation(project(":core-platform"))

	implementation(libs.android.core.ktx)
	implementation(libs.coil)

	val composeBom = platform(libs.compose.bom)
	implementation(composeBom)
	implementation(libs.bundles.compose)
	debugImplementation(libs.compose.ui.tooling)

	implementation(libs.lifecycle.runtime.compose)
	implementation(libs.lifecycle.viewmodel.compose)
	implementation(libs.hilt.navigation.compose)

	androidTestImplementation(composeBom)
	androidTestImplementation(libs.compose.junit)
	debugImplementation(libs.compose.test.manifest)

	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)

	testImplementation(libs.junit)
	androidTestImplementation(libs.android.junit)
}
