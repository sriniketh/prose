plugins {
	id("prose.android.feature")
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

android {
	namespace = "com.sriniketh.feature_searchbooks"
}

dependencies {

	implementation(project(":core-design"))
	implementation(project(":core-data"))
	implementation(project(":core-models"))
	implementation(project(":core-platform"))

	implementation(libs.android.core.ktx)
	implementation(libs.coil)

	implementation(libs.kotlinx.collections.immutable)
}
