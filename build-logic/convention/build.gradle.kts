plugins {
    `kotlin-dsl`
}

group = "com.sriniketh.prose.buildlogic"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.jvmToolchainVersion.get().toInt())
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "prose.android.application"
            implementationClass =
                "com.sriniketh.prose.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "prose.android.library"
            implementationClass = "com.sriniketh.prose.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "prose.android.compose"
            implementationClass = "com.sriniketh.prose.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "prose.android.hilt"
            implementationClass = "com.sriniketh.prose.buildlogic.AndroidHiltConventionPlugin"
        }
        register("androidFeature") {
            id = "prose.android.feature"
            implementationClass = "com.sriniketh.prose.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("jvmLibrary") {
            id = "prose.jvm.library"
            implementationClass = "com.sriniketh.prose.buildlogic.JvmLibraryConventionPlugin"
        }
    }
}
