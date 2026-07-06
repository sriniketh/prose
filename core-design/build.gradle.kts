plugins {
	id("prose.android.library")
	id("prose.android.compose")
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

android {
	namespace = "com.sriniketh.core_design"
}

dependencies {
	implementation(libs.google.fonts)
}
