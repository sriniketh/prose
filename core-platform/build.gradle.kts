plugins {
    id("prose.android.library")
    id("prose.android.hilt")
}

android {
    namespace = "com.sriniketh.core_platform"
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
