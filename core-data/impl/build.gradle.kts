plugins {
    id("prose.android.library")
    id("prose.android.hilt")
}

android {
    namespace = "com.sriniketh.core_data.impl"
}

dependencies {
    implementation(project(":core-data:api"))
    implementation(project(":core-network"))
    implementation(project(":core-platform"))
    implementation(project(":core-db"))
    implementation(project(":core-models"))

    implementation(libs.coroutines.android)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.cashapp.turbine)
}
