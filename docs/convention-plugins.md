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
- [Coverage reporting](#coverage-reporting)
- [AGP 9 constraints](#agp-9-constraints)
- [How the pieces fit](#how-the-pieces-fit)
- [Adding a new module](#adding-a-new-module)
- [Changing a convention](#changing-a-convention)
- [Troubleshooting](#troubleshooting)

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
        ├── JacocoConfig.kt              # shared unit-test JaCoCo wiring (Android side)
        ├── AndroidApplicationConventionPlugin.kt
        ├── AndroidLibraryConventionPlugin.kt
        ├── AndroidComposeConventionPlugin.kt
        ├── AndroidHiltConventionPlugin.kt
        ├── AndroidFeatureConventionPlugin.kt
        └── JvmLibraryConventionPlugin.kt
```

`build-logic` is a **separate Gradle build**, not a `buildSrc` directory, so editing one convention
plugin does not invalidate the whole main build's configuration cache. It is wired in from the root
[`settings.gradle.kts`](../settings.gradle.kts), whose `pluginManagement { }` block calls
`includeBuild("build-logic")` and declares the repositories (`google()`, `mavenCentral()`,
`gradlePluginPortal()`) an included build needs for itself, since it inherits none from the root.

`includeBuild` must be inside `pluginManagement`. A top-level `includeBuild` composites builds for
*dependency* substitution, not plugin resolution, and the plugin IDs will not be found.

[`build-logic/settings.gradle.kts`](../build-logic/settings.gradle.kts) declares its own repositories
(an included build inherits none) and its `dependencyResolutionManagement { versionCatalogs { } }`
block points a catalog also named `"libs"` at the very same `../gradle/libs.versions.toml` the
modules use, so plugin versions can never drift between the two builds.

The catalog carries the plugins' own JAR coordinates under `[libraries]` in
[`gradle/libs.versions.toml`](../gradle/libs.versions.toml) — separate from the `[plugins]` markers,
which only work in a `plugins { }` block. Look for `android-gradlePlugin`, `kotlin-gradlePlugin`,
`compose-gradlePlugin`, `ksp-gradlePlugin`, and `hilt-gradlePlugin` there.

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

Pure Kotlin/JVM module: applies `java-library` and `org.jetbrains.kotlin.jvm` — the latter **is**
still required here, since AGP 9's built-in Kotlin support covers Android modules only — then
`configureKotlin()`, which sets the JDK toolchain and with it the Java toolchain, so no separate
`java { toolchain { … } }` block is needed. It also applies `jacoco` and turns on XML + HTML reports
for the plugin's own default `jacocoTestReport` task (see [Coverage reporting](#coverage-reporting)).

### `prose.android.library`

[`AndroidLibraryConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/AndroidLibraryConventionPlugin.kt)

Applies AGP's library plugin, the shared Android + Kotlin configuration (`configureAndroidCommon`,
`configureKotlin`), and the shared unit-test JaCoCo wiring (`configureAndroidUnitTestJacoco`, see
[Coverage reporting](#coverage-reporting)), plus `consumerProguardFiles("consumer-rules.pro")` and a
`dependencies { }` block covering `junit`, `android-junit`, and `android-test-runner`.
`consumerProguardFiles`
is library-only (it lives on `LibraryVariantDimension`, not `CommonExtension`), so it sits here
rather than in the shared helper. Every library module already has a `consumer-rules.pro`; a new one
without it fails at `mergeDebugConsumerProguardFiles` with a clear message.

Note there is **no** `org.jetbrains.kotlin.android` — see [constraint 1](#1-do-not-apply-orgjetbrainskotlinandroid).

The dependency block gives every library module the JVM-unit-test basics (`junit`) and the two
things any `androidTest` needs regardless of Compose: `android-junit` (`androidx.test.ext:junit`,
the `AndroidJUnit4` class behind `@RunWith(AndroidJUnit4::class)`) and `android-test-runner`
(`androidx.test:runner`, the `AndroidJUnitRunner` *instrumentation* class every module's
`testInstrumentationRunner` — set in [`AndroidConfig.kt`](#shared-helpers) — names). Both are
declared explicitly rather than left to arrive transitively through `compose-junit`, so a module
gets them the moment it applies this plugin, whether or not it also has Compose.

### `prose.android.application`

[`AndroidApplicationConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/AndroidApplicationConventionPlugin.kt)

Structurally identical to the library plugin (same `configureAndroidCommon`, `configureKotlin`,
`configureAndroidUnitTestJacoco`, and an `android-junit`/`android-test-runner` dependencies block),
plus `targetSdk` (an application-only property) and
`buildFeatures.buildConfig = true` — `app` is the only module that gets `BuildConfig` generation for
free; every other module opts in per-module (`core-network` does, in its own `build.gradle.kts`).
`applicationId`, `versionCode`, and `versionName` deliberately stay in `app/build.gradle.kts` —
they are identity, not convention.

The `android-junit`/`android-test-runner` pair is duplicated here rather than shared with the
library plugin because `app` applies `com.android.application`, not `com.android.library` — the two
plugins are siblings, not one built on the other, so each needs its own copy.

### `prose.android.compose`

[`AndroidComposeConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/AndroidComposeConventionPlugin.kt)

Applies *only* the Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`) and never AGP —
that is what makes it composable across `app` and library modules alike — then, deferred behind
`pluginManager.withPlugin("com.android.base")`, turns on `buildFeatures.compose` via a plain
`CommonExtension` lookup (the supertype of both `ApplicationExtension` and `LibraryExtension`, so one
lookup serves both). Its `dependencies { }` block adds the Compose BOM + bundle + tooling to
`implementation`/`debugImplementation`, and the BOM + `compose-junit` to `androidTestImplementation`.

The `pluginManager.withPlugin("com.android.base")` callback is what makes plugin **order in the
module's `plugins { }` block irrelevant**. `getByType` is eager, so a direct call would require the
Android plugin to be applied first; deferring until AGP's base plugin lands removes that trap.

`compose-junit` (`androidx.compose.ui:ui-test-junit4`, `createComposeRule()` and the
`onNodeWith*`/`assert*` semantics-tree API) is the **only** androidTest dependency this plugin adds —
`android-junit`/`android-test-runner` live in the library/application plugins instead, because those
two are useful to *any* module with `androidTest`, Compose or not (`core-db`'s DAO tests,
`core-platform`'s `UriExtensionsTest`), while `compose-junit` only matters to a module that actually
drives a `ComposeTestRule`.

### `prose.android.hilt`

[`AndroidHiltConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/AndroidHiltConventionPlugin.kt)

Applies KSP (`com.google.devtools.ksp`) then Hilt (`com.google.dagger.hilt.android`), and adds
`hilt-android` on `implementation` plus `hilt-compiler` on `ksp`. KSP is applied first because the
Hilt plugin looks for an annotation processor to attach to, and
because the `"ksp"` configuration only exists once the KSP plugin is applied.

Bundling KSP with Hilt is a judgement call: in this repo every KSP consumer is also a Hilt consumer
(`core-db` uses both Room and Hilt). If a module ever needs KSP without Hilt, split out a
`prose.android.ksp` plugin.

### `prose.android.feature`

[`AndroidFeatureConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/AndroidFeatureConventionPlugin.kt)

Applies `prose.android.library` + `prose.android.compose` + `prose.android.hilt`, then a
`dependencies { }` block: `implementation` on `core-design`, `core-data`, `core-models`, the
lifecycle/Compose-ViewModel libraries, and immutable collections; `testImplementation` on
`coroutines-test` and Turbine.

The one deliberate bundle: "this is a Prose feature module". A convention plugin can apply other
convention plugins by ID, which is the payoff of the composable design.

What is **not** here, and why:

- `junit` (`testImplementation`) and `android-junit`/`android-test-runner`/`compose-junit`
  (`androidTestImplementation`) — inherited transitively from `prose.android.library` and
  `prose.android.compose`, which this plugin already applies. Declaring them again here would be a
  pure duplicate.
- `core-platform` — `feature-viewhighlights` needs it only on `testImplementation`, so it stays a
  per-module line.
- `mockk` — only two of the four features use it. Two explicit lines beat two unused dependencies.

### Shared helpers

**[`ProjectExtensions.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/ProjectExtensions.kt)** —
Gradle does not generate type-safe catalog accessors for code compiled inside another build, so
`libs.versions.minSdkVersion` does not exist in plugin source. It reaches the catalog through its
runtime API instead: a `Project.libs` extension property (`VersionCatalogsExtension` lookup by name)
and a `VersionCatalog.version(name)` helper (`findVersion(name).get().requiredVersion`) — these are
what every `libs.version("…")` / `libs.findLibrary("…")` call in the other plugins resolves to.

The trade-off is that catalog keys become strings — a typo is a configuration-time
`NoSuchElementException` rather than a compile error. It still fails on the first build after the
mistake. Keys keep their dashes in this form: `findLibrary("cashapp-turbine")`.

**[`KotlinConfig.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/KotlinConfig.kt)** —
the shared `kotlin { }` configuration for both Android and JVM modules: a `configureKotlin()`
extension function that does one thing, `jvmToolchain(libs.version("jvmToolchainVersion").toInt())`,
against a plain `KotlinBaseExtension` lookup.

`KotlinBaseExtension` is the common supertype of the Android and JVM Kotlin extensions, and Gradle's
`getByType` matches on subtype — so one function serves `prose.android.library` and
`prose.jvm.library` both.

**[`AndroidConfig.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/AndroidConfig.kt)** —
the shared `android { }` configuration, taking a plain `CommonExtension` so it serves both `app` and
every library. `configureAndroidCommon(extension)` sets `compileSdk`, `defaultConfig.minSdk` and
`testInstrumentationRunner`, `buildTypes["debug"].enableAndroidTestCoverage = true`, and
`buildTypes["release"]`'s `isMinifyEnabled = false` + ProGuard files
(`proguard-android-optimize.txt` + the module's own `proguard-rules.pro`).

The property-access style (`defaultConfig.apply { }`, `buildTypes.getByName(…)`) is required, not
stylistic — see [constraint 3](#3-commonextension-exposes-defaultconfig-and-buildtypes-as-properties-only).

`buildConfig` generation is **not** part of this shared helper — only `app` and `core-network` read
a generated `BuildConfig` class, so `prose.android.application` sets
`buildFeatures.buildConfig = true` itself, and `core-network/build.gradle.kts` opts in explicitly.
The other seven Android modules get no `BuildConfig` class or compile task at all.

`enableAndroidTestCoverage` makes AGP instrument the debug variant so `connectedDebugAndroidTest`
produces its own JaCoCo data, surfaced through the auto-generated `createDebugAndroidTestCoverageReport`
task (HTML + XML, no custom task needed) — see [Coverage reporting](#coverage-reporting) below.

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

## Coverage reporting

Every module gets a `jacocoTestReport` task producing HTML + XML from its unit tests — Android
modules via [`JacocoConfig.kt`](../build-logic/convention/src/main/kotlin/com/sriniketh/prose/buildlogic/JacocoConfig.kt)'s
`configureAndroidUnitTestJacoco()` (called from `prose.android.library` and
`prose.android.application`), JVM modules via the `jacoco` plugin's own default wiring to the plain
`test` task (`prose.jvm.library`). The task name is deliberately the same in both cases, so
`./gradlew jacocoTestReport` runs it project-wide regardless of module type.

Instrumented-test coverage needs no custom task at all: `enableAndroidTestCoverage = true` on the
debug build type (set in `AndroidConfig.kt`, see [above](#shared-helpers)) makes AGP instrument that
variant, and `connectedDebugAndroidTest` gets a sibling task, `createDebugAndroidTestCoverageReport`,
that runs the tests and writes `build/reports/coverage/androidTest/debug/connected/report.xml`.

CI (`.github/workflows/build.yml`) uploads both to Codecov as separate flags — `unittests` from the
`unit-tests` job, `androidtests` from the `ui-tests` job (the job that already has a device to run
`connectedDebugAndroidTest` on) — which Codecov merges into one project view. The `jacocoVersion`
catalog entry is the single source of truth both wiring paths read from, so it never drifts between
the Android and JVM cases. [`codecov.yml`](../codecov.yml) at the repo root excludes generated code
(Hilt, `R`, `BuildConfig`, DI modules) from the numbers and has both status checks (`project`,
`patch`) turned off — reporting only, no merge gating, until real thresholds are chosen.

### Excluding `@Preview` functions from coverage

JaCoCo ships a built-in bytecode filter (`AnnotationGeneratedFilter`, part of `org.jacoco.core`
itself, so it applies to both `jacocoTestReport` and AGP's `createDebugAndroidTestCoverageReport`
without any extra wiring) that drops a class or method from analysis if it carries an annotation
with `CLASS` or `RUNTIME` retention whose simple name **contains** `"Generated"`. The rule started
as an exact-match on `Generated` in JaCoCo 0.8.2 and was loosened to a contains-match in 0.8.3; the
`jacocoVersion` this repo pins (0.8.12) and the newer version AGP bundles for the instrumented-test
report path (0.8.14, visible in the HTML report footer) both carry the loosened rule.

[`GeneratedPreview`](../core-design/src/main/kotlin/com/sriniketh/core_design/ui/PreviewAnnotations.kt)
is a marker annotation (`@Retention(AnnotationRetention.BINARY)`, `@Target(FUNCTION)`) declared in
`core-design` that leans on this filter. It is applied directly on every `@Preview`/`@PreviewLightDark`
function repo-wide, alongside the existing preview annotation:

```kotlin
@GeneratedPreview
@PreviewLightDark
@Composable
internal fun BookshelfScreenSuccessPreview() { … }
```

This was chosen over `classDirectories.exclude()` glob patterns: excludes operate at the whole-class
level, and a preview function's `FooKt` class is frequently shared with a real production composable
in the same file (`ProseTopAppBarKt` holds both `ProseTopAppBar` and `ProseTopAppBarPreview`) — a
glob exclude would hide that production composable's real coverage too. The annotation filter instead
removes only the annotated *method*, verified empirically (before/after `jacocoTestReport`/
`createDebugAndroidTestCoverageReport` HTML+XML) on `Typography.kt` first: `TypographyKt` disappeared
from the report entirely (it had exactly one method, and it was the annotated preview), while in a
mixed file like `ProseTopAppBar.kt`, `ProseTopAppBarKt` kept its production `ProseTopAppBar` method
at full coverage and only lost the `ProseTopAppBarPreview` method.

**Known residual gap:** the Compose compiler hoists a composable's non-capturing trailing lambdas
(e.g. `AppTheme { Surface { Column { Text(…) } } }`) into a synthetic `ComposableSingletons$FooKt`
class, shared by every composable in that file. That class's methods are never themselves annotated
with `@GeneratedPreview` — only the enclosing source function is — so a preview that builds UI
inline (rather than delegating to an already-tested production composable with sample data) still
leaves that inline content counted as missed in `ComposableSingletons$FooKt`. `Typography.kt`'s
preview *is* the UI, so this
doesn't apply there; `ProseTopAppBarPreview`'s one `title = { Text(…) }` lambda is a small instance
of it. The other five preview functions across the repo delegate to a production composable with
plain data + no-op callbacks and contribute no new singleton-lambda content, so they have no residual
at all. Excluding `ComposableSingletons$FooKt` wholesale via `classDirectories.exclude()` was
considered and rejected for the same reason as above: that class also holds real, tested production
lambdas (default parameter values, etc.) in every one of these files.

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

## History

The `viewBinding = true` flag was also dropped during the migration — `app` and all four feature
modules enabled it despite the repo having no `res/layout` directory and no reference to a generated
binding class.

The migration landed as one commit per module (`ab2b200` through `5eb5c89`), each verified with
`./gradlew assembleDebug` before moving on. Module build files went from 617 lines total to 190,
against 259 lines of `build-logic`. The line count is roughly flat; the win is that `compileSdk`,
the Compose BOM, and the Hilt wiring each now have exactly one definition site instead of ten, six,
and nine.
