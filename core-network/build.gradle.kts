import java.util.Properties
import java.io.FileInputStream

plugins {
    id("prose.android.library")
    id("prose.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

val apikeyPropertiesFile = file("apikey.properties")
val apikeyProperties = Properties()
apikeyProperties.load(FileInputStream(apikeyPropertiesFile))

kotlin {
    compilerOptions {
        optIn.add("kotlin.RequiresOptIn")
    }
}

android {
    namespace = "com.sriniketh.prose.core_network"

    defaultConfig {
        buildConfigField("String", "BOOKS_API_KEY", apikeyProperties["BOOKS_API_KEY"] as String)
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.coroutines.android)

    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
