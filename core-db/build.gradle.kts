plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

ksp {
	arg("room.schemaLocation", "$projectDir/schemas")
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
	namespace = "com.sriniketh.core_db"

	sourceSets {
		getByName("androidTest") {
			assets.directories.add("$projectDir/schemas")
		}
	}
}

dependencies {
	implementation(libs.android.core.ktx)

	implementation(libs.room.runtime)
	implementation(libs.room.ktx)
	ksp(libs.room.compiler)

	implementation(libs.kotlinx.serialization.json)

	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)

	testImplementation(libs.junit)

	androidTestImplementation(libs.junit)
	androidTestImplementation(libs.android.junit)
	androidTestImplementation(libs.android.test.runner)
	androidTestImplementation(libs.room.testing)
}
