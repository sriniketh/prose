
This file provides guidance to AI coding agents like Claude Code (claude.ai/code), Cursor AI, Codex, Gemini CLI, GitHub Copilot, and other AI coding assistants when working with code in this repository.

## Documentation

In-depth engineering docs live under [`docs/`](docs/) and are written to be both human- and agent-readable:

- [`docs/README.md`](docs/README.md) — index and fast facts.
- [`docs/architecture.md`](docs/architecture.md) — layering, the UDF contract, Hilt DI map, navigation, and conventions.
- [`docs/modules.md`](docs/modules.md) — per-module breakdown, dependency graph, and use-case index.
- [`docs/flows.md`](docs/flows.md) — end-to-end walkthroughs of every user flow with source paths.
- [`docs/convention-plugins.md`](docs/convention-plugins.md) — the `build-logic` convention plugins, AGP 9 constraints, and how to add a module.

Consult these before making non-trivial changes; the summary below is a quick reference.

**Keep them in sync.** These docs name exact file paths, the module graph, and concrete flows, so they drift the moment code moves. When a change does any of the following, update the relevant `docs/` file in the same change set:

- Adds, removes, or renames a Gradle module → `docs/modules.md` (per-module entry + dependency graph) and the fast facts in `docs/README.md`.
- Alters Hilt wiring, the UDF/`Result<T>` contract, navigation routes, or a cross-cutting convention → `docs/architecture.md`.
- Changes a convention plugin, adds a new one, or changes which plugins a module applies →
  `docs/convention-plugins.md` (plugin reference + per-module table) and the plugin table in
  `docs/modules.md`.
- Changes a user-facing flow or the UI→data→UI path of one → `docs/flows.md`.
- Moves or renames any file referenced by path in a doc → fix the path everywhere it appears.

If a change touches none of the above (e.g. an internal refactor with no structural or flow impact), the docs need no edit. When unsure, run the audit described in `.claude/commands/audit-docs.md`.

## Build Commands

```bash
# Build the project (requires API key setup first)
./gradlew assembleDebug

# Run all unit tests
./gradlew test

# Run unit tests for a specific module
./gradlew :feature-bookshelf:test

# Run a specific test class (must use testDebugUnitTest, not test, with --tests)
./gradlew :feature-bookshelf:testDebugUnitTest --tests "com.sriniketh.feature_bookshelf.SomeTest"

# Run instrumented/UI tests (requires emulator or device)
./gradlew connectedDebugAndroidTest

# Run UI tests for specific feature modules
./gradlew :feature-bookshelf:connectedDebugAndroidTest

# Compile the convention plugins only (fast check after editing build-logic)
./gradlew -p build-logic :convention:compileKotlin
```

## API Key Setup

Before building, create `core-network/apikey.properties` with:
```
BOOKS_API_KEY="your-google-books-api-key"
```

## Architecture Overview

Prose is a multi-module Android app for capturing book highlights from physical books using OCR (ML Kit). It follows unidirectional data flow (UDF) with Jetpack Compose UI. Uses JVM toolchain 17.

### Module Structure

**Core Modules:**
- `core-network` - Retrofit API client for Google Books API (kotlinx.serialization for JSON)
- `core-db` - Room database with `BookEntity` and `HighlightEntity`
- `core-data` - Repositories and UseCases that combine network/db operations
- `core-models` - Domain models (`Book`, `Highlight`, `BookSearch`) — pure Kotlin, no Android deps
- `core-design` - Shared Compose theme and components
- `core-platform` - Platform utilities (file operations, URI encoding)

**Build Logic:**
- `build-logic` - included Gradle build publishing the convention plugins (not an app module; ships nothing into the APK)

**Feature Modules:**
- `feature-bookshelf` - Main screen showing saved books
- `feature-searchbooks` - Book search and info screens
- `feature-viewhighlights` - Display highlights for a book
- `feature-addhighlight` - Camera capture, crop, OCR, and save highlight

### Build Configuration

Shared Gradle configuration lives in convention plugins under `build-logic/`, **never** in module
build files. A module's `build.gradle.kts` declares its `namespace`, anything genuinely
module-specific, and its `dependencies` — nothing else.

| Plugin | Applies |
|--------|---------|
| `prose.android.application` | AGP application + shared Android/Kotlin config + `targetSdk` |
| `prose.android.library` | AGP library + shared Android/Kotlin config + `consumerProguardFiles` |
| `prose.android.compose` | Compose compiler plugin, `buildFeatures.compose`, Compose dependency set |
| `prose.android.hilt` | KSP + Hilt plugin and dependencies |
| `prose.android.feature` | library + compose + hilt + the shared feature dependency set |
| `prose.jvm.library` | `java-library` + Kotlin JVM + toolchain |

Rules when editing the build:
- Do **not** add `compileSdk`, `minSdk`, `jvmToolchain`, `buildTypes`, or `buildFeatures.buildConfig`
  to a module file — they come from a convention plugin. If a module genuinely needs different
  behaviour, change the convention rather than overriding locally.
- Do **not** apply `org.jetbrains.kotlin.android` anywhere. AGP 9 has built-in Kotlin support and
  applying it fails the build. (`org.jetbrains.kotlin.jvm` is still required for `core-models`.)
- Keep the root `build.gradle.kts` `plugins { … apply false }` block — it puts AGP on the
  buildscript classpath so `build-logic` can declare it `compileOnly`.
- Convention plugins cannot use type-safe catalog accessors; use `libs.version("…")` /
  `libs.findLibrary("…")` from `ProjectExtensions.kt`.
- After changing a convention plugin: `./gradlew -p build-logic :convention:compileKotlin`, then
  `./gradlew assembleDebug test`.

See [`docs/convention-plugins.md`](docs/convention-plugins.md) for the full reference.

### Data Flow Pattern

```
Network/Database → Repository → UseCase → ViewModel → Compose UI
```

- **Repositories** (`BooksRepository`, `HighlightsRepository`) abstract data sources
- **UseCases** are single-purpose classes with `operator fun invoke()` (e.g., `GetAllSavedBooksUseCase`)
- **ViewModels** expose `StateFlow<UIState>` to Compose screens
- Results are wrapped in Kotlin `Result<T>` for error handling

### Dependency Injection

Hilt is used throughout. Each module has a DI package (`di/` or `dagger/`) with `@Module` classes:
- `NetworkModule` provides Retrofit/OkHttp instances
- `DatabaseModule` provides Room database and DAOs
- `DataModule` binds repository implementations

### Navigation

Navigation Compose with routes defined in `app/src/main/java/com/sriniketh/prose/Navigation.kt`. The `ProseAppScreen` composable sets up the NavHost with all destinations. Screen arguments are passed via route paths (e.g., `view_highlights/{bookId}`).

### Key Libraries

- **Compose BOM** for UI with Material 3 + DynamicColors
- **Retrofit 3 + kotlinx.serialization** for networking
- **Room** for local persistence
- **Coil** for image loading
- **Cropify** for image cropping
- **Timber** for logging

### Testing Patterns

- ViewModels are tested with **Turbine** for Flow assertions and fake repositories
- Use `StandardTestDispatcher()` with `Dispatchers.setMain()` for coroutine tests
- Fake implementations live in `src/test/.../fakes/` directories
- **MockK** is available for mocking

### Version Catalog

All dependency versions are managed in `gradle/libs.versions.toml`. Use version catalog references (`libs.versions.*`, `libs.plugins.*`, `libs.*`) rather than hardcoded versions.
