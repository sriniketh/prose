plugins {
	id("prose.android.library")
	id("prose.android.hilt")
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

android {
	namespace = "com.sriniketh.core_platform"
}

dependencies {
	implementation(libs.android.core.ktx)
	implementation(libs.timber)
}
