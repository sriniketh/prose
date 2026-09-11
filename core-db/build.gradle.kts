plugins {
    id("prose.android.library")
    id("prose.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.sriniketh.core_db"

    sourceSets {
        getByName("androidTest") {
            assets.directories.add("$projectDir/schemas")
        }
    }
}

dependencies {
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.android.junit)
    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.room.testing)
}
