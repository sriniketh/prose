plugins {
	id("prose.android.feature")
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

android {
	namespace = "com.sriniketh.feature_addhighlight"
}

dependencies {

	implementation(project(":core-design"))
	implementation(project(":core-platform"))
	implementation(project(":core-data"))
	implementation(project(":core-models"))

	implementation(libs.android.core.ktx)
	implementation(libs.timber)

	implementation(libs.mlkit.text.recognition)

	implementation(libs.activity.compose)

	implementation(libs.cropify)

	testImplementation(libs.mockk)
}
