package com.sriniketh.prose.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/* Gradle does not generate type-safe catalog accessors for code compiled inside another build.
We're using these extensions to get the catalog using the runtime API instead.

Note: catalog keys with dashes become dots in lookups — cashapp-turbine in the TOML is findLibrary("cashapp-turbine") */

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(name: String): String =
    findVersion(name).get().requiredVersion
