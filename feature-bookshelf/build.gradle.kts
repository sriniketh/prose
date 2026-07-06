plugins {
	id("prose.android.feature")
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

android {
	namespace = "com.sriniketh.feature_bookshelf"
}

dependencies {

	implementation(project(":core-design"))
	implementation(project(":core-data"))
	implementation(project(":core-models"))
	implementation(project(":core-platform"))

	implementation(libs.android.core.ktx)
	implementation(libs.kotlinx.collections.immutable)
	implementation(libs.coil)
}
