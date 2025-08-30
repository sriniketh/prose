plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.android)
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
		buildConfig = true
	}
	namespace = "com.sriniketh.core_data"
}

dependencies {

	implementation(project(":core-network"))
	implementation(project(":core-platform"))
	implementation(project(":core-db"))
	implementation(project(":core-models"))

	implementation(libs.android.core.ktx)
	implementation(libs.coroutines.android)
	implementation(libs.timber)

	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)

	testImplementation(libs.junit)
	testImplementation(libs.coroutines.test)
	testImplementation(libs.cashapp.turbine)
	testImplementation(libs.mockk)
}
