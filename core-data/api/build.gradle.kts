plugins {
    id("prose.android.library")
    id("prose.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sriniketh.core_data"
}

dependencies {
    implementation(project(":core-platform"))
    implementation(project(":core-models"))

    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}
