# Moshi → kotlinx.serialization Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Moshi with kotlinx.serialization across the codebase so the JSON models and serialization logic compile as pure-Kotlin (Kotlin Multiplatform-ready) code with no Android/JVM-only Moshi dependency.

**Architecture:** Moshi appears in exactly two modules. (1) `core-network` parses Google Books API responses through a Retrofit `MoshiConverterFactory`; this becomes Retrofit 3's official kotlinx.serialization converter backed by a `Json` instance configured to ignore unknown keys (Moshi ignored them silently; kotlinx.serialization throws by default). (2) `core-data` hand-serializes a highlights export with `Moshi.Builder().adapter(...).toJson()`; this becomes `Json.encodeToString(...)`. Model classes swap `@JsonClass(generateAdapter = true)` for `@Serializable`. Moshi uses KSP codegen; kotlinx.serialization uses a Kotlin compiler plugin, so the `moshi-codegen` KSP processor is removed (the KSP plugin itself stays for Hilt).

**Tech Stack:** Kotlin 2.3.10, Retrofit 3.0.0, OkHttp 5.3.2, Hilt, kotlinx.serialization 1.11.0 (`org.jetbrains.kotlin.plugin.serialization` @ 2.3.10), JUnit4 + coroutines-test + Turbine + MockK.

---

## Prerequisites

- `core-network/apikey.properties` must exist (see `AGENTS.md` → "API Key Setup"). Every Gradle invocation configures `core-network`, which reads this file at configuration time; without it, even `./gradlew help` fails.
- Run all commands from the repo root: `/Users/srinikethramachandran/Documents/projects/prose`.

## File Structure

**Modified:**
- `gradle/libs.versions.toml` — add kotlinx.serialization version/libraries/plugin; remove Moshi entries (done last, after no module references them).
- `build.gradle.kts` (root) — declare the serialization plugin with `apply false`.
- `core-network/build.gradle.kts` — swap Moshi deps for serialization deps + apply the plugin.
- `core-network/src/main/java/com/sriniketh/prose/core_network/model/Volumes.kt` — `@JsonClass` → `@Serializable`.
- `core-network/src/main/java/com/sriniketh/prose/core_network/di/NetworkModule.kt` — `MoshiConverterFactory` → kotlinx converter.
- `core-data/build.gradle.kts` — swap Moshi deps for serialization deps + apply the plugin.
- `core-data/src/main/java/com/sriniketh/core_data/models/HighlightsExport.kt` — `@JsonClass` → `@Serializable`.
- `core-data/src/main/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCase.kt` — Moshi adapter → `Json.encodeToString`.
- `core-data/proguard-rules.pro` — add serialization keep rules (consistency with `core-network`).
- `core-data/src/test/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCaseTest.kt` — add a round-trip regression test.

**Created:**
- `core-network/src/main/java/com/sriniketh/prose/core_network/di/BooksApiJson.kt` — the shared `Json` instance (so it is unit-testable).
- `core-network/src/test/java/com/sriniketh/prose/core_network/di/BooksApiJsonTest.kt` — guards `ignoreUnknownKeys` decoding.

**Unchanged regression gates:** `core-network` `BooksRemoteDataSourceImplTest` + `FakeBooksApi`; `core-data` `VolumesTransformersTest`, `BooksRepositoryImplTest`, and the existing methods in `ExportHighlightsUseCaseTest`. These must keep passing.

---

### Task 1: Add kotlinx.serialization to the version catalog and root build

This task only *adds* the new coordinates and plugin (it does **not** remove Moshi yet — the module build files still reference `libs.moshi`, so removing it now would break configuration). Moshi removal happens in Task 4.

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts` (root)

- [ ] **Step 1: Add the runtime version to the `[versions]` block**

In `gradle/libs.versions.toml`, find the line `moshi-version = "1.15.2"` (line ~34) and add a new line directly beneath it:

```toml
moshi-version = "1.15.2"
kotlinx-serialization-version = "1.11.0"
```

- [ ] **Step 2: Add the library coordinates to the `[libraries]` block**

In `gradle/libs.versions.toml`, find the two Moshi library lines (`moshi = ...` and `moshi-codegen = ...`, lines ~69-70) and add the two serialization libraries directly beneath them:

```toml
moshi = { group = "com.squareup.moshi", name = "moshi-kotlin", version.ref = "moshi-version" }
moshi-codegen = { group = "com.squareup.moshi", name = "moshi-kotlin-codegen", version.ref = "moshi-version" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization-version" }
retrofit-kotlinx-serialization-converter = { group = "com.squareup.retrofit2", name = "converter-kotlinx-serialization", version.ref = "retrofit-version" }
```

(The converter's version intentionally tracks `retrofit-version` = `3.0.0` — Square ships `converter-kotlinx-serialization` at the same version as Retrofit itself.)

- [ ] **Step 3: Add the Gradle plugin to the `[plugins]` block**

In `gradle/libs.versions.toml`, find the `kotlin-compose = ...` plugin line (line ~90) and add directly beneath it:

```toml
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin-compose-plugin-version" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin-jvm-plugin-version" }
```

(The serialization compiler plugin version **must** equal the Kotlin version, so it reuses `kotlin-jvm-plugin-version` = `2.3.10`.)

- [ ] **Step 4: Declare the plugin in the root build with `apply false`**

In `build.gradle.kts` (root), find the `plugins { ... }` block and add the serialization line after the `kotlin.compose` line:

```kotlin
plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.kotlin.jvm) apply false
	alias(libs.plugins.kotlin.compose) apply false
	alias(libs.plugins.kotlin.serialization) apply false
	alias(libs.plugins.ksp) apply false
	alias(libs.plugins.hilt) apply false
	alias(libs.plugins.android.navigation.safe.args) apply false
}
```

- [ ] **Step 5: Verify the catalog parses and the root build script compiles**

Run: `./gradlew help -q`
Expected: `BUILD SUCCESSFUL`. (A typo in a catalog accessor or the plugin id surfaces here as a configuration error.)

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts
git commit -m "build: add kotlinx.serialization to version catalog and root build"
```

---

### Task 2: Migrate core-network to kotlinx.serialization

**Files:**
- Modify: `core-network/build.gradle.kts:50-54`
- Modify: `core-network/src/main/java/com/sriniketh/prose/core_network/model/Volumes.kt`
- Create: `core-network/src/main/java/com/sriniketh/prose/core_network/di/BooksApiJson.kt`
- Modify: `core-network/src/main/java/com/sriniketh/prose/core_network/di/NetworkModule.kt`
- Test (create): `core-network/src/test/java/com/sriniketh/prose/core_network/di/BooksApiJsonTest.kt`

- [ ] **Step 1: Swap Moshi for serialization in the module build file**

In `core-network/build.gradle.kts`, add the serialization plugin to the `plugins` block:

```kotlin
plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)
}
```

Then in the `dependencies` block, replace the three Moshi lines (currently lines 52-54):

```kotlin
	implementation(libs.retrofit.moshi.converter)
	implementation(libs.moshi)
	ksp(libs.moshi.codegen)
```

with the two serialization lines:

```kotlin
	implementation(libs.retrofit.kotlinx.serialization.converter)
	implementation(libs.kotlinx.serialization.json)
```

Leave `implementation(libs.retrofit)`, `ksp(libs.hilt.compiler)`, and the `ksp` plugin alias untouched (Hilt still needs KSP).

- [ ] **Step 2: Convert the network models to `@Serializable`**

Replace the entire contents of `core-network/src/main/java/com/sriniketh/prose/core_network/model/Volumes.kt` with:

```kotlin
package com.sriniketh.prose.core_network.model

import kotlinx.serialization.Serializable

@Serializable
data class Volumes(
    val items: List<Volume>
)

@Serializable
data class Volume(
    val id: String,
    val volumeInfo: VolumeInfo
)

@Serializable
data class VolumeInfo(
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val authors: List<String>,
    val imageLinks: ImageLinks? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val pageCount: Int? = null,
    val averageRating: Double? = null,
    val ratingsCount: Int? = null
)

@Serializable
data class ImageLinks(
    val thumbnail: String,
    val smallThumbnail: String? = null,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null
)
```

(Property names already match the Google Books JSON keys exactly — `volumeInfo`, `imageLinks`, `smallThumbnail`, etc. — so no `@SerialName` is needed. Nullability/defaults are kept identical to preserve current decode behavior.)

- [ ] **Step 3: Write the failing test for unknown-key tolerance**

Create `core-network/src/test/java/com/sriniketh/prose/core_network/di/BooksApiJsonTest.kt`:

```kotlin
package com.sriniketh.prose.core_network.di

import com.sriniketh.prose.core_network.model.Volumes
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Test

class BooksApiJsonTest {

    @Test
    fun `decodes google books payload while ignoring unknown keys`() {
        val payload = """
            {
              "kind": "books#volumes",
              "totalItems": 1,
              "items": [
                {
                  "kind": "books#volume",
                  "id": "someId",
                  "etag": "abc123",
                  "selfLink": "https://www.googleapis.com/books/v1/volumes/someId",
                  "volumeInfo": {
                    "title": "Some Title",
                    "subtitle": "Some Subtitle",
                    "authors": ["Author One"],
                    "publisher": "Some Publisher",
                    "publishedDate": "2020",
                    "description": "A description",
                    "industryIdentifiers": [
                      {"type": "ISBN_13", "identifier": "9781234567897"}
                    ],
                    "pageCount": 321,
                    "categories": ["Fiction"],
                    "averageRating": 4.5,
                    "ratingsCount": 12,
                    "imageLinks": {
                      "smallThumbnail": "http://books.google.com/small",
                      "thumbnail": "http://books.google.com/thumb"
                    },
                    "language": "en",
                    "previewLink": "http://books.google.com/preview"
                  },
                  "saleInfo": {"country": "US", "saleability": "NOT_FOR_SALE"},
                  "accessInfo": {"country": "US", "viewability": "PARTIAL"}
                }
              ]
            }
        """.trimIndent()

        val volumes = booksApiJson.decodeFromString<Volumes>(payload)

        assertEquals(1, volumes.items.size)
        val volumeInfo = volumes.items[0].volumeInfo
        assertEquals("someId", volumes.items[0].id)
        assertEquals("Some Title", volumeInfo.title)
        assertEquals("Some Subtitle", volumeInfo.subtitle)
        assertEquals("Author One", volumeInfo.authors[0])
        assertEquals(321, volumeInfo.pageCount)
        assertEquals(4.5, volumeInfo.averageRating)
        assertEquals(12, volumeInfo.ratingsCount)
        assertEquals("http://books.google.com/thumb", volumeInfo.imageLinks?.thumbnail)
    }
}
```

The payload deliberately contains unknown keys at every level (`kind`, `totalItems`, `etag`, `selfLink`, `industryIdentifiers`, `categories`, `language`, `previewLink`, `saleInfo`, `accessInfo`) — exactly the kind of extra data the Google Books API returns. `booksApiJson` does not exist yet, so this is the red state.

- [ ] **Step 4: Run the test to verify it fails**

Run: `./gradlew :core-network:testDebugUnitTest --tests "com.sriniketh.prose.core_network.di.BooksApiJsonTest"`
Expected: compilation FAILS with an unresolved reference: `booksApiJson`.

- [ ] **Step 5: Create the shared `Json` instance**

Create `core-network/src/main/java/com/sriniketh/prose/core_network/di/BooksApiJson.kt`:

```kotlin
package com.sriniketh.prose.core_network.di

import kotlinx.serialization.json.Json

internal val booksApiJson: Json = Json {
    ignoreUnknownKeys = true
}
```

- [ ] **Step 6: Run the test to verify it passes**

Run: `./gradlew :core-network:testDebugUnitTest --tests "com.sriniketh.prose.core_network.di.BooksApiJsonTest"`
Expected: PASS. (If `ignoreUnknownKeys` were missing, this would throw `SerializationException: Encountered an unknown key`.)

- [ ] **Step 7: Point Retrofit at the kotlinx converter**

In `core-network/src/main/java/com/sriniketh/prose/core_network/di/NetworkModule.kt`, replace the Moshi import (line 14):

```kotlin
import retrofit2.converter.moshi.MoshiConverterFactory
```

with these two imports (keep them in alphabetical position among the existing imports):

```kotlin
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.converter.kotlinx.serialization.asConverterFactory
```

Then replace the converter line (line 42):

```kotlin
        .addConverterFactory(MoshiConverterFactory.create())
```

with:

```kotlin
        .addConverterFactory(booksApiJson.asConverterFactory("application/json; charset=utf-8".toMediaType()))
```

(`booksApiJson` is in the same `di` package, so no import is required for it.)

- [ ] **Step 8: Run the full core-network test suite (regression gate)**

Run: `./gradlew :core-network:test`
Expected: PASS — both the new `BooksApiJsonTest` and the existing `BooksRemoteDataSourceImplTest` (which exercises the fake API and all `VolumeInfo` fields) succeed.

- [ ] **Step 9: Commit**

```bash
git add core-network/build.gradle.kts \
  core-network/src/main/java/com/sriniketh/prose/core_network/model/Volumes.kt \
  core-network/src/main/java/com/sriniketh/prose/core_network/di/BooksApiJson.kt \
  core-network/src/main/java/com/sriniketh/prose/core_network/di/NetworkModule.kt \
  core-network/src/test/java/com/sriniketh/prose/core_network/di/BooksApiJsonTest.kt
git commit -m "refactor: migrate core-network JSON parsing from Moshi to kotlinx.serialization"
```

---

### Task 3: Migrate core-data export to kotlinx.serialization

**Files:**
- Modify: `core-data/build.gradle.kts:47-48`
- Modify: `core-data/src/main/java/com/sriniketh/core_data/models/HighlightsExport.kt`
- Modify: `core-data/src/main/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCase.kt`
- Test (modify): `core-data/src/test/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCaseTest.kt`

- [ ] **Step 1: Swap Moshi for serialization in the module build file**

In `core-data/build.gradle.kts`, add the serialization plugin to the `plugins` block:

```kotlin
plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)
}
```

Then in the `dependencies` block, replace the two Moshi lines (currently lines 47-48):

```kotlin
	implementation(libs.moshi)
	ksp(libs.moshi.codegen)
```

with:

```kotlin
	implementation(libs.kotlinx.serialization.json)
```

Leave `ksp(libs.hilt.compiler)` and the `ksp` plugin alias untouched (Hilt still needs KSP).

- [ ] **Step 2: Convert the export models to `@Serializable`**

Replace the entire contents of `core-data/src/main/java/com/sriniketh/core_data/models/HighlightsExport.kt` with:

```kotlin
package com.sriniketh.core_data.models

import kotlinx.serialization.Serializable

@Serializable
data class HighlightsExport(
    val id: String,
    val info: BookInfoExport,
    val highlights: List<HighlightExport>
)

@Serializable
data class BookInfoExport(
    val title: String,
    val subtitle: String?,
    val authors: List<String>,
    val thumbnailLink: String?,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val pageCount: Int?,
    val averageRating: Double?,
    val ratingsCount: Int?
)

@Serializable
data class HighlightExport(
    val id: String,
    val bookId: String,
    val text: String,
    val savedOnTimestamp: String
)
```

- [ ] **Step 3: Add a round-trip regression test (red until the use case is switched)**

In `core-data/src/test/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCaseTest.kt`, add these imports beneath the existing imports:

```kotlin
import com.sriniketh.core_data.models.HighlightsExport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
```

Then add this test method inside the class (e.g., after the existing `when invoked then writes json...` test):

```kotlin
    @Test
    fun `written json round-trips back into HighlightsExport`() = runTest {
        useCase("test-book-id")
        val writtenContent = fakeFileSource.lastWrittenContent!!
        val decoded = Json.decodeFromString<HighlightsExport>(writtenContent)
        assertEquals("Test Title", decoded.info.title)
        assertEquals("Test Author", decoded.info.authors[0])
        assertEquals("Test highlight text", decoded.highlights[0].text)
    }
```

This decodes the file the use case wrote back into the model, proving the serialized field names and structure are stable. (`Test Title` / `Test Author` / `Test highlight text` are the values the existing fakes already produce — see the existing `when invoked then writes json...` test.)

- [ ] **Step 4: Run the test to verify it fails**

Run: `./gradlew :core-data:testDebugUnitTest --tests "com.sriniketh.core_data.usecases.ExportHighlightsUseCaseTest"`
Expected: FAIL — `HighlightsExport` is now `@Serializable` but `ExportHighlightsUseCase` still imports/uses `com.squareup.moshi.Moshi`, so the module no longer compiles (the `moshi` dependency was removed in Step 1). Compilation error: unresolved reference `Moshi`.

- [ ] **Step 5: Switch the use case to `Json.encodeToString`**

In `core-data/src/main/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCase.kt`, remove the Moshi import (line 10):

```kotlin
import com.squareup.moshi.Moshi
```

and add these imports in its place (alphabetical among existing imports):

```kotlin
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
```

Add a private top-level `Json` instance between the imports and the class declaration (i.e., directly above `class ExportHighlightsUseCase`):

```kotlin
@OptIn(ExperimentalSerializationApi::class)
private val exportJson = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}
```

Then replace the three Moshi lines in `invoke` (currently lines 47-49):

```kotlin
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(HighlightsExport::class.java).indent("  ")
        val json = adapter.toJson(export)
```

with:

```kotlin
        val json = exportJson.encodeToString(export)
```

(`prettyPrintIndent = "  "` reproduces Moshi's two-space `.indent("  ")` output. `prettyPrintIndent` is an experimental API, hence the `@OptIn`.)

- [ ] **Step 6: Run the test suite to verify it passes**

Run: `./gradlew :core-data:testDebugUnitTest --tests "com.sriniketh.core_data.usecases.ExportHighlightsUseCaseTest"`
Expected: PASS — the new round-trip test plus all existing `ExportHighlightsUseCaseTest` cases (which assert the JSON contains `"title"`, `"authors"`, `"publisher"`, `"highlights"`, and the expected file name) succeed.

- [ ] **Step 7: Run the full core-data test suite (regression gate)**

Run: `./gradlew :core-data:test`
Expected: PASS — including `VolumesTransformersTest` and `BooksRepositoryImplTest`, confirming the `@Serializable` network models (consumed from `core-network`) still behave correctly across the module boundary.

- [ ] **Step 8: Commit**

```bash
git add core-data/build.gradle.kts \
  core-data/src/main/java/com/sriniketh/core_data/models/HighlightsExport.kt \
  core-data/src/main/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCase.kt \
  core-data/src/test/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCaseTest.kt
git commit -m "refactor: migrate core-data highlight export from Moshi to kotlinx.serialization"
```

---

### Task 4: Remove Moshi from the catalog and finish ProGuard parity

No module references `libs.moshi*` after Tasks 2-3, so the catalog entries can be deleted. Add the kotlinx-serialization keep rules to `core-data` for consistency with `core-network` (minify is currently disabled in all modules, so these rules are precautionary for future release builds).

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `core-data/proguard-rules.pro`

- [ ] **Step 1: Confirm no source or build file still references Moshi**

Run: `grep -rn -i "moshi" --include="*.kt" --include="*.kts" . | grep -v "/build/" | grep -v "/.gradle/" | grep -v "/.worktrees/"`
Expected: only matches inside `gradle/libs.versions.toml` (the catalog entries about to be removed). No `.kt` or module `.gradle.kts` matches.

- [ ] **Step 2: Delete the Moshi version entry**

In `gradle/libs.versions.toml`, delete this line from `[versions]`:

```toml
moshi-version = "1.15.2"
```

- [ ] **Step 3: Delete the Moshi library entries**

In `gradle/libs.versions.toml`, delete these three lines from `[libraries]`:

```toml
retrofit-moshi-converter = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "retrofit-version" }
moshi = { group = "com.squareup.moshi", name = "moshi-kotlin", version.ref = "moshi-version" }
moshi-codegen = { group = "com.squareup.moshi", name = "moshi-kotlin-codegen", version.ref = "moshi-version" }
```

- [ ] **Step 4: Add serialization keep rules to core-data ProGuard config**

Append the following to `core-data/proguard-rules.pro` (mirrors the rules already present in `core-network/proguard-rules.pro`):

```proguard

# kotlinx-serialization rules
# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
```

- [ ] **Step 5: Verify the whole project still compiles and all unit tests pass**

Run: `./gradlew test`
Expected: `BUILD SUCCESSFUL` — every module's unit tests pass with the Moshi catalog entries gone.

- [ ] **Step 6: Verify the debug APK assembles (compiles the app against the new converter)**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add gradle/libs.versions.toml core-data/proguard-rules.pro
git commit -m "build: remove Moshi from version catalog and add core-data serialization keep rules"
```

---

## Notes & Gotchas

- **Unknown keys are the critical behavioral difference.** Moshi silently drops JSON fields not in the model; kotlinx.serialization throws `SerializationException` unless `ignoreUnknownKeys = true`. Task 2 Step 5 sets this, and `BooksApiJsonTest` guards it against regressions. Do **not** drop that flag.
- **Required (non-null, no-default) fields still throw on absence** under both libraries — `VolumeInfo.title`/`authors` behave the same as before, so nullability was intentionally left unchanged.
- **The serialization plugin version tracks Kotlin.** It is wired to `kotlin-jvm-plugin-version` (2.3.10) on purpose; bumping Kotlin automatically bumps the serialization compiler plugin.
- **KSP stays.** It is still used by Hilt (`ksp(libs.hilt.compiler)`) in both modules; only the `moshi-codegen` KSP processor is removed.
- **Minification is disabled** (`isMinifyEnabled = false`) in `app`, `core-network`, and `core-data`, so the ProGuard rules are inert today; they are added for correctness when release minification is eventually enabled.
- **KMP follow-up (out of scope):** these modules are still Android library modules. Making them genuine `commonMain` KMP source sets is a separate effort; this migration removes the JVM-only Moshi blocker so that future move is unobstructed.

## Self-Review

- **Spec coverage:** "move to kotlinx.serialization instead of Moshi" → every Moshi reference found by `grep` is addressed: `Volumes.kt` + `NetworkModule.kt` + `core-network/build.gradle.kts` (Task 2); `HighlightsExport.kt` + `ExportHighlightsUseCase.kt` + `core-data/build.gradle.kts` (Task 3); catalog entries (Tasks 1 & 4). "to better support KMP" → models become annotation-only pure Kotlin with no Moshi/JVM dependency; noted as the enabling step.
- **Type consistency:** `booksApiJson` (network `Json`, `ignoreUnknownKeys`) and `exportJson` (data `Json`, `prettyPrint`) are distinct, each defined once and referenced consistently. Class/field names in models are unchanged, so `FakeBooksApi`, transformers, and the repository keep compiling. Converter coordinate (`converter-kotlinx-serialization`), import (`retrofit2.converter.kotlinx.serialization.asConverterFactory`), and `asConverterFactory(... .toMediaType())` usage are consistent across the plan.
- **No placeholders:** every code/edit step contains the literal content to write and every run step states the exact command and expected outcome.
