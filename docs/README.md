# Prose — Engineering Documentation

Prose is a multi-module Android app for capturing and saving highlights from **physical** books.
You search for a book via the Google Books API, save it to a local bookshelf, then point your
camera at a page; on-device OCR (ML Kit) turns the photograph into editable highlight text that is
persisted with Room and can be exported as JSON.

This folder is the canonical reference for how the app is built. It is written to be read by both
humans and coding agents: every component is named with its concrete file path so it can be located
with a single search.

## Documents

| Document | What it covers |
|----------|----------------|
| [architecture.md](architecture.md) | Layering, the unidirectional data-flow (UDF) contract, dependency injection, navigation, and the cross-cutting conventions every module follows. |
| [modules.md](modules.md) | Every Gradle module, what it owns, its public surface, and the full inter-module dependency graph. |
| [flows.md](flows.md) | Step-by-step walkthroughs of each user-facing flow (bootstrap, bookshelf, search, add book, view highlights, capture→OCR→save, export/share) traced from UI down to the data source. |

## Fast facts

- **Language / build:** Kotlin, Gradle (Kotlin DSL), JVM toolchain 17. Versions are centralized in
  [`gradle/libs.versions.toml`](../gradle/libs.versions.toml).
- **UI:** Jetpack Compose, Material 3 with dynamic color, Navigation Compose, shared-element transitions.
- **Async:** Coroutines + Flow. `StateFlow` for screen state, `Channel`/`receiveAsFlow` for one-shot effects.
- **DI:** Hilt, one `@Module` per layer installed in `SingletonComponent`.
- **Persistence:** Room (`book-db`) for books and highlights.
- **Network:** Retrofit 3 + kotlinx.serialization against the Google Books API.
- **OCR:** ML Kit on-device Latin text recognition.
- **Min / target / compile SDK:** 26 / 35 / 36.
- **Entry point:** [`app/.../MainActivity.kt`](../app/src/main/java/com/sriniketh/prose/MainActivity.kt)
  → [`ProseAppScreen.kt`](../app/src/main/java/com/sriniketh/prose/ProseAppScreen.kt).

## Building

1. Create `core-network/apikey.properties` containing `BOOKS_API_KEY="<your-google-books-api-key>"`.
2. `./gradlew assembleDebug`

See [`AGENTS.md`](../AGENTS.md) for build/test commands and agent-specific guidance.
