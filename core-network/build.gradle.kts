plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
	compilerOptions {
		optIn.add("kotlin.RequiresOptIn")
	}
}

android {
	compileSdk = libs.versions.compileSdkVersion.get().toInt()

	defaultConfig {
		minSdk = libs.versions.minSdkVersion.get().toInt()

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
	namespace = "com.sriniketh.prose.core_network"
}

dependencies {
	implementation(libs.android.core.ktx)
	implementation(libs.coroutines.android)

	implementation(libs.okhttp.logging.interceptor)
	implementation(libs.retrofit)
	implementation(libs.retrofit.kotlinx.serialization.converter)
	implementation(libs.kotlinx.serialization.json)

	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)

	testImplementation(libs.junit)
	testImplementation(libs.coroutines.test)
}
