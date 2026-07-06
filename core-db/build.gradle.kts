plugins {
	id("prose.android.library")
	id("prose.android.hilt")
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

ksp {
	arg("room.schemaLocation", "$projectDir/schemas")
}

android {
	namespace = "com.sriniketh.core_db"
}

dependencies {
	implementation(libs.android.core.ktx)

	implementation(libs.room.runtime)
	implementation(libs.room.ktx)
	ksp(libs.room.compiler)
}
