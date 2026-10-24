plugins {
    id("prose.android.feature")
    id("prose.android.screenshot")
}

android {
    namespace = "com.sriniketh.feature_addhighlight"
}

dependencies {
    implementation(project(":core-platform"))

    implementation(libs.timber)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.activity.compose)

    testImplementation(libs.mockk)
}
