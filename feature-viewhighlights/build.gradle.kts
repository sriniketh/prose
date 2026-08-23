plugins {
    id("prose.android.feature")
}

android {
    namespace = "com.sriniketh.feature_viewhighlights"
}

dependencies {
    implementation(libs.activity.compose)

    testImplementation(project(":core-platform"))
    testImplementation(libs.mockk)
}
