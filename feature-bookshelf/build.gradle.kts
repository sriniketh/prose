plugins {
    id("prose.android.feature")
}

android {
    namespace = "com.sriniketh.feature_bookshelf"
}

dependencies {
    implementation(project(":core-platform"))
    implementation(libs.coil)
}
