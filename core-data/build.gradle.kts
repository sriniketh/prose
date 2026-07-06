plugins {
	id("prose.android.library")
	id("prose.android.hilt")
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

android {
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

	implementation(libs.kotlinx.serialization.json)

	testImplementation(libs.coroutines.test)
	testImplementation(libs.cashapp.turbine)
	testImplementation(libs.mockk)
}
