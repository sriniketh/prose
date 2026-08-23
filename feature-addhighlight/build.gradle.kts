plugins {
    id("prose.android.feature")
}

android {
    namespace = "com.sriniketh.feature_addhighlight"
}

dependencies {
    implementation(project(":core-platform"))

    implementation(libs.timber)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.cropify)
    implementation(libs.activity.compose)

    testImplementation(libs.mockk)
}
