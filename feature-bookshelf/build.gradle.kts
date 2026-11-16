plugins {
    id("prose.android.feature")
    id("prose.android.screenshot")
}

android {
    namespace = "com.sriniketh.feature_bookshelf"
}

dependencies {
    implementation(project(":core-platform"))
    implementation(libs.coil)
}
