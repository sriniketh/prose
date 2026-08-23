# Build Logic & Convention Plugins

Prose's Gradle configuration lives in [`build-logic/`](../build-logic), an **included build** that
publishes six convention plugins into the main build. Module `build.gradle.kts` files declare
*identity* (namespace, dependencies) and nothing else; everything shared — SDK levels, the JDK
toolchain, ProGuard wiring, Compose, Hilt — comes from a plugin.

This document is the reference for that setup: what each plugin does, why the set is shaped this
way, and the AGP 9 constraints that dictate how the plugins are written.

> **Toolchain:** AGP 9.2.1, Kotlin 2.4.0, KSP 2.3.9, Hilt 2.60, Gradle 9.6.1, JDK 17.
> The AGP 9 notes in [Constraints](#agp-9-constraints) matter — most convention-plugin material
> online (Now in Android included) targets AGP 8 and **will not compile here**.

---

## Contents

- [Layout](#layout)
- [The plugins](#the-plugins)
- [Which module applies what](#which-module-applies-what)
- [AGP 9 constraints](#agp-9-constraints)
- [How the pieces fit](#how-the-pieces-fit)
- [Adding a new module](#adding-a-new-module)
- [Changing a convention](#changing-a-convention)
- [Troubleshooting](#troubleshooting)
- [Known remaining cleanups](#known-remaining-cleanups)

---

## Layout

```
build-logic/
├── settings.gradle.kts                  # separate build; points at the shared version catalog
└── convention/
    ├── build.gradle.kts                 # kotlin-dsl, plugin deps, plugin ID registration
    └── src/main/kotlin/com/sriniketh/prose/buildlogic/
        ├── ProjectExtensions.kt         # version-catalog access helper
        ├── KotlinConfig.kt              # shared Kotlin config (toolchain, compiler args)
        ├── AndroidConfig.kt             # shared Android config (SDKs, ProGuard)
        ├── AndroidApplicationConventionPlugin.kt
        ├── AndroidLibraryConventionPlugin.kt
        ├── AndroidComposeConventionPlugin.kt
        ├── AndroidHiltConventionPlugin.kt
        ├── AndroidFeatureConventionPlugin.kt
        └── JvmLibraryConventionPlugin.kt
```

`build-logic` is a **separate Gradle build**, not a `buildSrc` directory, so editing one convention
plugin does not invalidate the whole main build's configuration cache. It is wired in from the root
[`settings.gradle.kts`](../settings.gradle.kts):

```kotlin
pluginManagement {
	includeBuild("build-logic")
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}
}
```

`includeBuild` must be inside `pluginManagement`. A top-level `includeBuild` composites builds for
*dependency* substitution, not plugin resolution, and the plugin IDs will not be found.

`build-logic/settings.gradle.kts` declares its own repositories (an included build inherits none)
and points at the same catalog file the modules use, so plugin versions can never drift:

```kotlin
dependencyResolutionManagement {
	versionCatalogs {
		create("libs") {
			from(files("../gradle/libs.versions.toml"))
		}
	}
}
```

The catalog carries the plugins' own JAR coordinates under `[libraries]` — separate from the
`[plugins]` markers, which only work in a `plugins { }` block:

```toml
# Gradle plugin artifacts, for the build-logic compile classpath only
android-gradlePlugin = { group = "com.android.tools.build", name = "gradle", version.ref = "android-application-plugin-version" }
kotlin-gradlePlugin = { group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version.ref = "kotlin-jvm-plugin-version" }
compose-gradlePlugin = { group = "org.jetbrains.kotlin", name = "compose-compiler-gradle-plugin", version.ref = "kotlin-compose-plugin-version" }
ksp-gradlePlugin = { group = "com.google.devtools.ksp", name = "symbol-processing-gradle-plugin", version.ref = "ksp-plugin-version" }
hilt-gradlePlugin = { group = "com.google.dagger", name = "hilt-android-gradle-plugin", version.ref = "hilt-plugin-version" }
```

These are `compileOnly` in `build-logic/convention/build.gradle.kts` — the convention plugins need
AGP's *types* to compile, but AGP is already on the buildscript classpath at runtime via the root
`plugins { … apply false }` block. That root block is load-bearing; see
[constraint 4](#4-agp-as-compileonly-depends-on-the-root-plugins-block).

---

## The plugins

Six plugins, deliberately **composable** — a module applies the two or three it needs rather than
one monolith. Each encodes *a decision*, not *a module*.

### `prose.jvm.library`

[`JvmLibraryConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/JvmLibraryConventionPlugin.kt)

```kotlin
pluginManager.apply("java-library")
pluginManager.apply("org.jetbrains.kotlin.jvm")
configureKotlin()
```

Pure Kotlin/JVM module. `org.jetbrains.kotlin.jvm` **is** still required here — AGP 9's built-in
Kotlin support covers Android modules only. `configureKotlin()` sets the JDK toolchain, which also
configures the Java toolchain, so no separate `java { toolchain { … } }` block is needed.

### `prose.android.library`

[`AndroidLibraryConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/AndroidLibraryConventionPlugin.kt)

```kotlin
pluginManager.apply("com.android.library")

extensions.configure<LibraryExtension> {
	configureAndroidCommon(this)
	defaultConfig.consumerProguardFiles("consumer-rules.pro")
}
configureKotlin()
```

Applies AGP's library plugin and the shared Android + Kotlin configuration. `consumerProguardFiles`
is library-only (it lives on `LibraryVariantDimension`, not `CommonExtension`), so it sits here
rather than in the shared helper. Every library module already has a `consumer-rules.pro`; a new one
without it fails at `mergeDebugConsumerProguardFiles` with a clear message.

Note there is **no** `org.jetbrains.kotlin.android` — see [constraint 1](#1-do-not-apply-orgjetbrainskotlinandroid).

### `prose.android.application`

[`AndroidApplicationConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/AndroidApplicationConventionPlugin.kt)

```kotlin
pluginManager.apply("com.android.application")

extensions.configure<ApplicationExtension> {
	configureAndroidCommon(this)
	defaultConfig.targetSdk = libs.version("targetSdkVersion").toInt()
	buildFeatures.buildConfig = true
}
configureKotlin()
```

Structurally identical to the library plugin, plus `targetSdk` (an application-only property) and
`buildFeatures.buildConfig = true` — `app` is the only module that gets `BuildConfig` generation for
free; every other module opts in per-module (`core-network` does, in its own `build.gradle.kts`).
`applicationId`, `versionCode`, and `versionName` deliberately stay in `app/build.gradle.kts` —
they are identity, not convention.

### `prose.android.compose`

[`AndroidComposeConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/AndroidComposeConventionPlugin.kt)

```kotlin
pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

pluginManager.withPlugin("com.android.base") {
	extensions.getByType<CommonExtension>().buildFeatures.compose = true
}

dependencies {
	val bom = platform(libs.findLibrary("compose-bom").get())
	"implementation"(bom)
	"implementation"(libs.findBundle("compose").get())
	"debugImplementation"(libs.findLibrary("compose-ui-tooling").get())
	"androidTestImplementation"(bom)
	"debugImplementation"(libs.findLibrary("compose-test-manifest").get())
}
```

It applies *only* the Compose compiler plugin and never AGP — that is what makes it composable
across `app` and library modules alike. `CommonExtension` is the supertype of both
`ApplicationExtension` and `LibraryExtension`, so one lookup serves both.

The `pluginManager.withPlugin("com.android.base")` callback is what makes plugin **order in the
module's `plugins { }` block irrelevant**. `getByType` is eager, so a direct call would require the
Android plugin to be applied first; deferring until AGP's base plugin lands removes that trap.

### `prose.android.hilt`

[`AndroidHiltConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/AndroidHiltConventionPlugin.kt)

```kotlin
pluginManager.apply("com.google.devtools.ksp")
pluginManager.apply("com.google.dagger.hilt.android")

dependencies {
	"implementation"(libs.findLibrary("hilt-android").get())
	"ksp"(libs.findLibrary("hilt-compiler").get())
}
```

KSP is applied first because the Hilt plugin looks for an annotation processor to attach to, and
because the `"ksp"` configuration only exists once the KSP plugin is applied.

Bundling KSP with Hilt is a judgement call: in this repo every KSP consumer is also a Hilt consumer
(`core-db` uses both Room and Hilt). If a module ever needs KSP without Hilt, split out a
`prose.android.ksp` plugin.

### `prose.android.feature`

[`AndroidFeatureConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/AndroidFeatureConventionPlugin.kt)

```kotlin
pluginManager.apply("prose.android.library")
pluginManager.apply("prose.android.compose")
pluginManager.apply("prose.android.hilt")

dependencies {
	"implementation"(project(":core-design"))
	"implementation"(project(":core-data"))
	"implementation"(project(":core-models"))

	"implementation"(libs.findLibrary("lifecycle-runtime-compose").get())
	"implementation"(libs.findLibrary("lifecycle-viewmodel-compose").get())
	"implementation"(libs.findLibrary("hilt-lifecycle-viewmodel-compose").get())
	"implementation"(libs.findLibrary("kotlinx-collections-immutable").get())

	"testImplementation"(libs.findLibrary("junit").get())
	"testImplementation"(libs.findLibrary("coroutines-test").get())
	"testImplementation"(libs.findLibrary("cashapp-turbine").get())

	"androidTestImplementation"(libs.findLibrary("android-junit").get())
	"androidTestImplementation"(libs.findLibrary("compose-junit").get())
}
```

The one deliberate bundle: "this is a Prose feature module". A convention plugin can apply other
convention plugins by ID, which is the payoff of the composable design.

What is **not** here, and why:

- `core-platform` — `feature-viewhighlights` needs it only on `testImplementation`, so it stays a
  per-module line.
- `mockk` — only two of the four features use it. Two explicit lines beat two unused dependencies.

### Shared helpers

**[`ProjectExtensions.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/ProjectExtensions.kt)** —
Gradle does not generate type-safe catalog accessors for code compiled inside another build, so
`libs.versions.minSdkVersion` does not exist in plugin source. These helpers reach the catalog
through its runtime API instead:

```kotlin
internal val Project.libs: VersionCatalog
	get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(name: String): String =
	findVersion(name).get().requiredVersion
```

The trade-off is that catalog keys become strings — a typo is a configuration-time
`NoSuchElementException` rather than a compile error. It still fails on the first build after the
mistake. Keys keep their dashes in this form: `findLibrary("cashapp-turbine")`.

**[`KotlinConfig.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/KotlinConfig.kt)** —
the shared `kotlin { }` configuration for both Android and JVM modules:

```kotlin
extensions.configure<KotlinBaseExtension> {
	jvmToolchain(libs.version("jvmToolchainVersion").toInt())
}
tasks.withType<KotlinCompile>().configureEach {
	compilerOptions {
		freeCompilerArgs.add("-Xannotation-default-target=param-property")
	}
}
```

`KotlinBaseExtension` is the common supertype of the Android and JVM Kotlin extensions, and Gradle's
`getByType` matches on subtype — so one function serves `prose.android.library` and
`prose.jvm.library` both. `tasks.withType<KotlinCompile>` replaced the old root `subprojects { }`
block, and does match AGP 9's built-in Kotlin compile tasks even though no Kotlin Android plugin is
applied.

**[`AndroidConfig.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/AndroidConfig.kt)** —
the shared `android { }` configuration, taking a plain `CommonExtension` so it serves both `app` and
every library:

```kotlin
internal fun Project.configureAndroidCommon(extension: CommonExtension) {
	with(extension) {
		compileSdk = libs.version("compileSdkVersion").toInt()

		defaultConfig.apply {
			minSdk = libs.version("minSdkVersion").toInt()
			testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		}

		buildTypes.getByName("release") {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
}
```

The property-access style (`defaultConfig.apply { }`, `buildTypes.getByName(…)`) is required, not
stylistic — see [constraint 3](#3-commonextension-exposes-defaultconfig-and-buildtypes-as-properties-only).

`buildConfig` generation is **not** part of this shared helper — only `app` and `core-network` read
a generated `BuildConfig` class, so `prose.android.application` sets
`buildFeatures.buildConfig = true` itself, and `core-network/build.gradle.kts` opts in explicitly.
The other seven Android modules get no `BuildConfig` class or compile task at all.

---

## Which module applies what

| Module | Plugins | Module-local configuration |
|---|---|---|
| `app` | `prose.android.application`, `prose.android.compose`, `prose.android.hilt` | `applicationId`, `versionCode`, `versionName` |
| `core-models` | `prose.jvm.library` | — |
| `core-platform` | `prose.android.library`, `prose.android.hilt` | — |
| `core-db` | `prose.android.library`, `prose.android.hilt` | `ksp { arg("room.schemaLocation", …) }` |
| `core-network` | `prose.android.library`, `prose.android.hilt`, `kotlin.serialization` | `apikey.properties` loading, `buildFeatures.buildConfig = true`, `buildConfigField`, `optIn` |
| `core-data` | `prose.android.library`, `prose.android.hilt`, `kotlin.serialization` | — |
| `core-design` | `prose.android.library`, `prose.android.compose` | — |
| `feature-bookshelf` | `prose.android.feature` | — |
| `feature-searchbooks` | `prose.android.feature` | — |
| `feature-viewhighlights` | `prose.android.feature` | — |
| `feature-addhighlight` | `prose.android.feature` | — |

`kotlin.serialization` stays a catalog alias rather than a convention plugin — only two modules use
it, so a plugin would not earn its keep. Applying it *after* a convention plugin works fine.

Every module additionally declares its `namespace` and its own `dependencies { }` block.

---

## AGP 9 constraints

Four AGP 9 behaviours differ from every AGP 8 tutorial, and each produces a confusing error.

### 1. Do **not** apply `org.jetbrains.kotlin.android`

AGP 9 has built-in Kotlin support. Applying the Kotlin Android plugin now fails hard:

```
> Failed to apply plugin 'org.jetbrains.kotlin.android'.
   > The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin
     support since AGP 9.0.
```

This is why the Android convention plugins apply only AGP, yet modules still get a working
`kotlin { }` block. Only the *Android* Kotlin plugin is subsumed — `org.jetbrains.kotlin.jvm` is
still needed for `core-models`.

### 2. `CommonExtension` is no longer generic

In AGP 8 it was `CommonExtension<*, *, *, *, *, *>`. In AGP 9.2.1:

```kotlin
interface CommonExtension : ExtensionAware
interface LibraryExtension : CommonExtension, TestedExtension
interface ApplicationExtension : CommonExtension, ApkExtension, TestedExtension
```

Shared helpers take a plain `CommonExtension` — no star projections. Copying the NiA signature will
not compile.

### 3. `CommonExtension` exposes `defaultConfig` and `buildTypes` as properties only

The *action* overloads (`defaultConfig { … }`, `buildTypes { release { … } }`) are declared on the
concrete `LibraryExtension` / `ApplicationExtension` interfaces, not on `CommonExtension`. Inside a
helper typed as `CommonExtension` this fails with `Unresolved reference 'defaultConfig'`:

```kotlin
// ✗
defaultConfig {
	minSdk = 26
}

// ✓
defaultConfig.apply {
	minSdk = 26
}

// ✓ — the release { } / debug { } shortcuts are LibraryExtension member extensions
buildTypes.getByName("release") { … }
```

### 4. AGP as `compileOnly` depends on the root plugins block

`build-logic` declares AGP as `compileOnly`, which keeps it off the convention plugins' runtime
classpath. That works **only** because the root [`build.gradle.kts`](../build.gradle.kts) declares
every plugin with `apply false`, putting them on the shared buildscript classpath. Do not delete
that block — the failure is opaque:

```
> Could not generate a decorated class for type AndroidLibraryConventionPlugin.
   > com/android/build/api/dsl/LibraryExtension
```

---

## How the pieces fit

```
gradle/libs.versions.toml ──┬─→ build-logic (compileOnly plugin JARs, versions via runtime API)
                            │        │
                            │        ├─ prose.jvm.library ─────────→ core-models
                            │        │
                            │        ├─ prose.android.library ──┬──→ core-platform, core-db,
                            │        │                          │    core-network, core-data,
                            │        │                          │    core-design
                            │        ├─ prose.android.compose ──┤
                            │        ├─ prose.android.hilt ─────┤
                            │        │                          │
                            │        ├─ prose.android.feature ──┴──→ the 4 feature-* modules
                            │        │   (= library + compose + hilt + shared deps)
                            │        │
                            │        └─ prose.android.application ─→ app
                            │
                            └─→ modules (libs.* accessors in build.gradle.kts)
```

---

## Adding a new module

1. `include(":new-module")` in the root [`settings.gradle.kts`](../settings.gradle.kts).
2. `touch new-module/consumer-rules.pro` (Android library modules only).
3. Write the build file — normally four lines:

```kotlin
plugins {
	id("prose.android.library")
}

android {
	namespace = "com.sriniketh.new_module"
}
```

Add `id("prose.android.compose")` if it has Compose UI, `id("prose.android.hilt")` if it has
injection, or replace all of them with `id("prose.android.feature")` if it is a feature module.

4. Update [`modules.md`](modules.md) — per-module entry, dependency graph, and the plugin table.

Do **not** re-add `compileSdk`, `minSdk`, `jvmToolchain`, `buildTypes`, or `buildFeatures.buildConfig`
to a module file. If you find yourself needing one of those, that is a signal the convention should
change instead — see below.

## Changing a convention

Anything that should apply to *every* module of a kind goes in `build-logic`, never copy-pasted
into module files. After editing:

```bash
./gradlew -p build-logic :convention:compileKotlin   # plugins compile
./gradlew assembleDebug                              # modules still configure and build
./gradlew test
./gradlew assembleDebug --configuration-cache        # run twice; second must say "reused"
```

To prove a dependency change did what you meant, diff the resolved graph:

```bash
./gradlew :feature-bookshelf:dependencies --configuration debugRuntimeClasspath
```

---

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `Plugin with id 'prose.android.library' not found` | `includeBuild("build-logic")` missing or outside `pluginManagement` | [Layout](#layout) |
| `Could not generate a decorated class … com/android/build/api/dsl/LibraryExtension` | AGP not on the runtime classpath | Keep the root `plugins { … apply false }` block ([4](#4-agp-as-compileonly-depends-on-the-root-plugins-block)) |
| `The 'org.jetbrains.kotlin.android' plugin is no longer required … since AGP 9.0` | A convention plugin applies the Kotlin Android plugin | Remove it ([1](#1-do-not-apply-orgjetbrainskotlinandroid)) |
| `Unresolved reference 'defaultConfig'` in a helper | Using the action form on `CommonExtension` | `defaultConfig.apply { }` ([3](#3-commonextension-exposes-defaultconfig-and-buildtypes-as-properties-only)) |
| `CommonExtension` "expects 6 type arguments" | Copied an AGP 8 snippet | Drop the star projections ([2](#2-commonextension-is-no-longer-generic)) |
| `Extension of type 'CommonExtension' does not exist` | Eager `getByType` before AGP is applied | Defer with `pluginManager.withPlugin("com.android.base")` |
| `libs` unresolved in `build-logic/convention/build.gradle.kts` | `versionCatalogs` block missing from `build-logic/settings.gradle.kts` | [Layout](#layout) |
| `libs.versions.minSdkVersion` unresolved in plugin *source* | Type-safe accessors are not generated for plugin projects | Use the `Project.libs` helper |
| `NoSuchElementException` at configuration time | Typo in a `findLibrary` / `findVersion` key | Check the key against `gradle/libs.versions.toml` |
| `Supplied consumer proguard configuration does not exist` | New library module without `consumer-rules.pro` | `touch <module>/consumer-rules.pro` |

---

## Known remaining cleanups

Two pieces of dead or redundant configuration survive the migration. Each is a small, independent
follow-up.

**1. The Safe Args catalog entry is unused.** The plugin was dropped from the root
`build.gradle.kts` and from the three feature modules that applied it — it generates code from XML
navigation graphs, and there are none:

```bash
find . -type d -name navigation -not -path "*/build/*"    # no results
```

`android-navigation-safe-args-plugin-version` and the `android-navigation-safe-args` plugin alias
are still in `gradle/libs.versions.toml` and can go.

**2. `-Xannotation-default-target=param-property` is redundant on Kotlin 2.4.** Every compile task
now warns:

```
w: The argument '-Xannotation-default-target=param-property' is redundant for the current language version 2.4.
```

It is already the default. The `tasks.withType<KotlinCompile>` block in `KotlinConfig.kt` can be
deleted, leaving `configureKotlin()` as just the toolchain call.

---

## History

The `viewBinding = true` flag was also dropped during the migration — `app` and all four feature
modules enabled it despite the repo having no `res/layout` directory and no reference to a generated
binding class.

The migration landed as one commit per module (`ab2b200` through `5eb5c89`), each verified with
`./gradlew assembleDebug` before moving on. Module build files went from 617 lines total to 190,
against 259 lines of `build-logic`. The line count is roughly flat; the win is that `compileSdk`,
the Compose BOM, and the Hilt wiring each now have exactly one definition site instead of ten, six,
and nine.
