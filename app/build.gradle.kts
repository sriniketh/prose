plugins {
    id("prose.android.application")
    id("prose.android.compose")
    id("prose.android.hilt")
}

android {
    namespace = "com.sriniketh.prose"

    defaultConfig {
        applicationId = "com.sriniketh.prose"
        versionCode = 3
        versionName = "1.2"
    }
}

dependencies {
    implementation(project(":core-design"))
    implementation(project(":core-platform"))
    implementation(project(":feature-bookshelf"))
    implementation(project(":feature-searchbooks"))
    implementation(project(":feature-viewhighlights"))
    implementation(project(":feature-addhighlight"))

    implementation(libs.android.appcompat)
    implementation(libs.coroutines.android)
    implementation(libs.timber)
    implementation(libs.navigation.compose)

    androidTestImplementation(libs.android.junit)
}
