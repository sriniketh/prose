pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
		maven {
			url = uri("https://jitpack.io")
		}
	}
}
rootProject.name = "prose"
include(":app")
include(":core-network")
include(":core-models")
include(":feature-searchbooks")
include(":feature-bookshelf")
include(":core-data")
include(":core-design")
include(":core-db")
include(":feature-viewhighlights")
include(":feature-addhighlight")
include(":core-platform")
