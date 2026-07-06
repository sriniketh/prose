plugins {
	id("prose.android.feature")
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

android {
	namespace = "com.sriniketh.feature_viewhighlights"
}

dependencies {

	implementation(project(":core-design"))
	implementation(project(":core-data"))
	implementation(project(":core-models"))

	implementation(libs.android.core.ktx)

	implementation(libs.activity.compose)

	implementation(libs.kotlinx.collections.immutable)

	testImplementation(project(":core-platform"))
	testImplementation(libs.mockk)
}
