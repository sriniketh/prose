plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)
	alias(libs.plugins.android.navigation.safe.args)
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

	namespace = "com.sriniketh.feature_viewhighlights"
}

dependencies {

	implementation(project(":core-design"))
	implementation(project(":core-data"))
	implementation(project(":core-models"))

	implementation(libs.android.core.ktx)

	val composeBom = platform(libs.compose.bom)
	implementation(composeBom)
	implementation(libs.bundles.compose)
	debugImplementation(libs.compose.ui.tooling)

	implementation(libs.lifecycle.runtime.compose)
	implementation(libs.lifecycle.viewmodel.compose)
	implementation(libs.hilt.navigation.compose)
	implementation(libs.activity.compose)

	androidTestImplementation(composeBom)
	androidTestImplementation(libs.compose.junit)
	debugImplementation(libs.compose.test.manifest)

	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)

	testImplementation(libs.junit)
	testImplementation(libs.coroutines.test)
	testImplementation(libs.cashapp.turbine)
	androidTestImplementation(libs.android.junit)
	androidTestImplementation(libs.compose.junit)
	debugImplementation(libs.compose.test.manifest)
}
