plugins {
    id("prose.android.feature")
    id("prose.android.screenshot")
}

android {
    namespace = "com.sriniketh.feature_viewhighlights"
}

dependencies {
    implementation(libs.activity.compose)

    testImplementation(project(":core-platform"))
    testImplementation(libs.mockk)
}
