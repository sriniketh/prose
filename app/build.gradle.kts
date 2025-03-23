plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.ksp)
	alias(libs.plugins.hilt)
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

android {
	compileSdk = libs.versions.compileSdkVersion.get().toInt()

	defaultConfig {
		applicationId = "com.sriniketh.prose"
		minSdk = libs.versions.minSdkVersion.get().toInt()
		targetSdk = libs.versions.targetSdkVersion.get().toInt()
		versionCode = 3
		versionName = "1.2"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

	namespace = "com.sriniketh.prose"
}

dependencies {

	implementation(project(":core-design"))
	implementation(project(":core-platform"))
	implementation(project(":feature-bookshelf"))
	implementation(project(":feature-searchbooks"))
	implementation(project(":feature-viewhighlights"))
	implementation(project(":feature-addhighlight"))

	implementation(libs.android.core.ktx)
	implementation(libs.android.appcompat)
	implementation(libs.coroutines.android)
	implementation(libs.timber)

	val composeBom = platform(libs.compose.bom)
	implementation(composeBom)
	implementation(libs.bundles.compose)
	debugImplementation(libs.compose.ui.tooling)
	implementation(libs.navigation.compose)

	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)

	testImplementation(libs.junit)
	androidTestImplementation(libs.android.junit)
	androidTestImplementation(libs.espresso.core)
}
