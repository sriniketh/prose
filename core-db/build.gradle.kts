plugins {
    id("prose.android.library")
    id("prose.android.hilt")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.sriniketh.core_db"
}

dependencies {
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
}
