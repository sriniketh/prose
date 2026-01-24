
This file provides guidance to AI coding agents like Claude Code (claude.ai/code), Cursor AI, Codex, Gemini CLI, GitHub Copilot, and other AI coding assistants when working with code in this repository.

## Build Commands

```bash
# Build the project (requires API key setup first)
./gradlew assembleDebug

# Run all unit tests
./gradlew test

# Run unit tests for a specific module
./gradlew :feature-bookshelf:test

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

Prose is a multi-module Android app for capturing book highlights using OCR. It follows unidirectional data flow (UDF) with Jetpack Compose UI.

### Module Structure

**Core Modules:**
- `core-network` - Retrofit API client for Google Books API
- `core-db` - Room database with `BookEntity` and `HighlightEntity`
- `core-data` - Repositories and UseCases that combine network/db operations
- `core-models` - Domain models (`Book`, `Highlight`, `BookSearch`)
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

Hilt is used throughout. Each module has a `di/` package with `@Module` classes:
- `NetworkModule` provides Retrofit/OkHttp instances
- `DatabaseModule` provides Room database and DAOs
- `DataModule` binds repository implementations

### Navigation

Navigation Compose with routes defined in `app/.../Navigation.kt`. The `ProseAppScreen` composable sets up the NavHost with all destinations. Screen arguments are passed via route paths (e.g., `view_highlights/{bookId}`).

### Testing Patterns

- ViewModels are tested with Turbine for Flow assertions and fake repositories
- Use `StandardTestDispatcher()` with `Dispatchers.setMain()` for coroutine tests
- Fake implementations live in `src/test/.../fakes/` directories
