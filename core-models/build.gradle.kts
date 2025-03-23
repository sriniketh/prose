plugins {
	alias(libs.plugins.java.library)
	alias(libs.plugins.kotlin.jvm)
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(libs.versions.jvmToolchainVersion.get().toInt())
	}
}
