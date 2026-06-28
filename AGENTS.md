
This file provides guidance to AI coding agents like Claude Code (claude.ai/code), Cursor AI, Codex, Gemini CLI, GitHub Copilot, and other AI coding assistants when working with code in this repository.

## Documentation

In-depth engineering docs live under [`docs/`](docs/) and are written to be both human- and agent-readable:

- [`docs/README.md`](docs/README.md) — index and fast facts.
- [`docs/architecture.md`](docs/architecture.md) — layering, the UDF contract, Hilt DI map, navigation, and conventions.
- [`docs/modules.md`](docs/modules.md) — per-module breakdown, dependency graph, and use-case index.
- [`docs/flows.md`](docs/flows.md) — end-to-end walkthroughs of every user flow with source paths.

Consult these before making non-trivial changes; the summary below is a quick reference.

**Keep them in sync.** These docs name exact file paths, the module graph, and concrete flows, so they drift the moment code moves. When a change does any of the following, update the relevant `docs/` file in the same change set:

- Adds, removes, or renames a Gradle module → `docs/modules.md` (per-module entry + dependency graph) and the fast facts in `docs/README.md`.
- Alters Hilt wiring, the UDF/`Result<T>` contract, navigation routes, or a cross-cutting convention → `docs/architecture.md`.
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

**Feature Modules:**
- `feature-bookshelf` - Main screen showing saved books
- `feature-searchbooks` - Book search and info screens
- `feature-viewhighlights` - Display highlights for a book
- `feature-addhighlight` - Camera capture, crop, OCR, and save highlight

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
