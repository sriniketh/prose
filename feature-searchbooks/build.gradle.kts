plugins {
    id("prose.android.feature")
    id("prose.android.screenshot")
}

android {
    namespace = "com.sriniketh.feature_searchbooks"
}

dependencies {
    implementation(project(":core-platform"))
    implementation(libs.coil)
}
