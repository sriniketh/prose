# Architecture

Prose follows **unidirectional data flow (UDF)** with a clean, layered, multi-module structure. Data
moves in one direction — from data sources up to the UI — and user intent moves back down as method
calls. The same shape repeats in every feature, so once you understand one screen you understand all
of them.

```
 ┌─────────────────────────────────────────────────────────────┐
 │  Presentation (feature-* modules)                            │
 │  Compose screen  ⇄  ViewModel (StateFlow + effect Channel)   │
 └───────────────▲───────────────────────────┬─────────────────┘
                 │ UiState / Effect           │ UseCase()
 ┌───────────────┴───────────────────────────▼─────────────────┐
 │  Domain + Data (core-data)                                   │
 │  UseCase  →  Repository  →  transformers (DTO/Entity ↔ Model)│
 └───────────────▲───────────────────────────┬─────────────────┘
                 │ Result<DomainModel>        │
 ┌───────────────┴──────────┬────────────────▼─────────────────┐
 │ core-network (Retrofit)  │ core-db (Room)   │ core-platform  │
 │ Google Books API         │ book-db          │ files, time    │
 └──────────────────────────┴──────────────────┴────────────────┘
```

The canonical pipeline (also stated in `AGENTS.md`):

```
Network / Database → Repository → UseCase → ViewModel → Compose UI
```

A feature module **never** talks to `core-network` or `core-db` directly. Its only data dependency
is `core-data`, which hides the data sources behind repositories and exposes use cases.

---

## Layers

### Domain models — `core-models`

Pure Kotlin (`java-library` + `kotlin-jvm`, no Android dependencies). Holds the three domain types
that flow through the whole app:

- [`Book` / `BookInfo`](../core-models/src/main/java/com/sriniketh/core_models/book/Book.kt)
- [`Highlight`](../core-models/src/main/java/com/sriniketh/core_models/book/Highlight.kt)
- [`BookSearch`](../core-models/src/main/java/com/sriniketh/core_models/search/BookSearch.kt)

These are the lingua franca: network DTOs and Room entities are mapped to/from them at the
`core-data` boundary, so nothing above `core-data` ever sees a Retrofit or Room type.

### Data sources

| Module | Responsibility | Key types |
|--------|----------------|-----------|
| [`core-network`](../core-network) | Google Books API client. | [`BooksApi`](../core-network/src/main/java/com/sriniketh/prose/core_network/retrofit/BooksApi.kt), [`BooksRemoteDataSource`](../core-network/src/main/java/com/sriniketh/prose/core_network/BooksRemoteDataSource.kt), [`Volumes`](../core-network/src/main/java/com/sriniketh/prose/core_network/model/Volumes.kt) DTOs |
| [`core-db`](../core-db) | Room database `book-db`. | [`BookDatabase`](../core-db/src/main/java/com/sriniketh/core_db/BookDatabase.kt), [`BookDao`](../core-db/src/main/java/com/sriniketh/core_db/dao/BookDao.kt), [`HighlightDao`](../core-db/src/main/java/com/sriniketh/core_db/dao/HighlightDao.kt), entities |
| [`core-platform`](../core-platform) | OS abstractions that keep `core-data` testable. | [`FileSource`](../core-platform/src/main/java/com/sriniketh/core_platform/FileSource.kt), [`DateTimeSource`](../core-platform/src/main/java/com/sriniketh/core_platform/DateTimeSource.kt), URI/log extensions |

### Repository + domain — `core-data`

The single module that combines the data sources. It contains:

- **Repositories** — [`BooksRepository`](../core-data/src/main/java/com/sriniketh/core_data/BooksRepository.kt)
  and [`HighlightsRepository`](../core-data/src/main/java/com/sriniketh/core_data/HighlightsRepository.kt)
  (interfaces) with `*Impl` classes that orchestrate network + db and map DTO/entity ↔ domain model.
- **Use cases** — single-purpose classes in
  [`usecases/`](../core-data/src/main/java/com/sriniketh/core_data/usecases) with an
  `operator fun invoke()`. ViewModels depend on these, not on repositories directly. Most are thin
  pass-throughs (e.g. `SaveHighlightUseCase`); a few coordinate multiple sources (e.g.
  `ExportHighlightsUseCase` reads both repositories).
- **Transformers** — extension functions in
  [`transformers/`](../core-data/src/main/java/com/sriniketh/core_data/transformers) such as
  `Volume.asBook()`, `Book.asBookEntity()`, `BookEntity.asBook()` that cross the model boundaries.

### Design system — `core-design`

Shared Compose theme ([`Theme.kt`](../core-design/src/main/kotlin/com/sriniketh/core_design/ui/theme/Theme.kt),
dynamic color), reusable components (`ProseTopAppBar`, `NavigationBack`, `Placeholder`), typography,
animation specs, and the
[`CompositionLocals`](../core-design/src/main/kotlin/com/sriniketh/core_design/ui/CompositionLocals.kt)
(`LocalSharedTransitionScope`, `LocalAnimatedVisibilityScope`) used for shared-element transitions.

### Presentation — `feature-*` and `app`

Each feature module owns its Compose screens, ViewModels, and feature-local DI. The `app` module
hosts the single Activity, the navigation graph, and app-level bindings.

---

## The UDF contract

Every ViewModel in the codebase follows the same pattern. Use it as the template when adding a screen.

**State** — an immutable data class exposed as a read-only `StateFlow`:

```kotlin
private val _uiState = MutableStateFlow(SomeUiState())
internal val uiState: StateFlow<SomeUiState> = _uiState.asStateFlow()
```

**Effects** — one-shot events (snackbars, navigation, share sheet) exposed via a buffered `Channel`
so they fire exactly once and are not replayed on recomposition / config change:

```kotlin
private val _effects = Channel<SomeEffect>(Channel.BUFFERED)
internal val effects: Flow<SomeEffect> = _effects.receiveAsFlow()
```

**Actions** — user intent. Simpler screens expose plain functions (`viewModel.saveHighlight(...)`);
[`ViewHighlightsViewModel`](../feature-viewhighlights/src/main/java/com/sriniketh/feature_viewhighlights/ViewHighlightsViewModel.kt)
uses a sealed `ViewHighlightsAction` dispatched through a single `processAction(...)`.

**Collection in Compose** — screens read state with `collectAsStateWithLifecycle()` and drain effects
inside `repeatOnLifecycle(STARTED)`.

**State design conventions:**
- UI state classes hold only what the screen renders. Domain models are mapped to per-screen
  `*UiState` types inside the ViewModel (e.g. `Book.asBookshelfUIState()`).
- List fields use `kotlinx.collections.immutable` (`ImmutableList`, `persistentListOf()`) so Compose
  treats them as stable and skips needless recomposition.
- `@StringRes Int` is carried in effects/state rather than resolved strings, keeping ViewModels free
  of `Context`.

**Error handling** — repositories and use cases return Kotlin `Result<T>`. Failures are logged with
Timber using the `T.logTag()` extension
([`LogExtensions.kt`](../core-platform/src/main/java/com/sriniketh/core_platform/LogExtensions.kt))
and surfaced to the user as a `ShowMessage` effect. ViewModels branch on `result.isSuccess` /
`result.isFailure`.

---

## Dependency injection (Hilt)

Hilt wires everything. Conventions:

- One `@Module object`/`abstract class` per layer, installed in `SingletonComponent`.
- Interface → implementation bindings use `@Binds` (abstract module) or `@Provides` (object module).
- ViewModels are `@HiltViewModel` and obtained in Compose with `hiltViewModel()`.

| Module file | Binds / provides |
|-------------|------------------|
| [`NetworkModule`](../core-network/src/main/java/com/sriniketh/prose/core_network/di/NetworkModule.kt) | `Retrofit`/`BooksApi` (singleton), `BooksRemoteDataSource` |
| [`DatabaseModule`](../core-db/src/main/java/com/sriniketh/core_db/dagger/DatabaseModule.kt) | `BookDatabase` (singleton), `BookDao`, `HighlightDao` |
| [`DataModule`](../core-data/src/main/java/com/sriniketh/core_data/di/DataModule.kt) | `BooksRepository`, `HighlightsRepository`, IO `CoroutineDispatcher` |
| [`PlatformModule`](../core-platform/src/main/java/com/sriniketh/core_platform/dagger/PlatformModule.kt) | `DateTimeSource` |
| [`AppModule`](../app/src/main/java/com/sriniketh/prose/dagger/AppModule.kt) | `FileSource` → `FileSourceImpl` |
| [`TextAnalysisModule`](../feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/dagger/TextAnalysisModule.kt) | `TextAnalyzer` → `TextAnalyzerImpl` |

> **Note — split interface/impl.** `FileSource` is *declared* in `core-platform` but *implemented*
> in `app` ([`FileSourceImpl`](../app/src/main/java/com/sriniketh/prose/files/FileSourceImpl.kt)),
> because the implementation needs `FileProvider` + the app's `packageName`/authority. `core-data`
> depends only on the interface. `DateTimeSource`, by contrast, is implemented and bound inside
> `core-platform`.

The application class [`ProseApplication`](../app/src/main/java/com/sriniketh/prose/ProseApplication.kt)
is `@HiltAndroidApp` and plants a Timber `DebugTree` in debug builds.
[`MainActivity`](../app/src/main/java/com/sriniketh/prose/MainActivity.kt) is `@AndroidEntryPoint`.

---

## Navigation

Navigation Compose with **string routes**, defined as a sealed interface in
[`Navigation.kt`](../app/src/main/java/com/sriniketh/prose/Navigation.kt) and wired in
[`ProseAppScreen.kt`](../app/src/main/java/com/sriniketh/prose/ProseAppScreen.kt). The start
destination is `bookshelf`. Arguments are passed as path segments (`view_highlights/{bookId}`).

| Route | Screen | Args |
|-------|--------|------|
| `bookshelf` | Bookshelf (start) | — |
| `search` | Book search | — |
| `book_info/{bookId}` | Book detail / add-to-shelf | `bookId` |
| `view_highlights/{bookId}` | Highlights list | `bookId` |
| `capture_and_crop_image/{bookId}` | Camera + crop | `bookId` |
| `save_highlight_from_uri/{bookId}/{uri}` | OCR + save new highlight | `bookId`, encoded `uri` |
| `save_highlight_from_highlight_id/{bookId}/{highlightId}` | Edit existing highlight | `bookId`, `highlightId` |

Notes:
- The image `uri` is URL-encoded/decoded across the route boundary via
  [`UriExtensions`](../core-platform/src/main/java/com/sriniketh/core_platform/UriExtensions.kt)
  (`encodeUri()` / `decodeUri()`).
- Cross-screen results use `savedStateHandle` on a back-stack entry. Adding a book sets
  `BOOKSHELF_SHOW_ADDED_MESSAGE` on the bookshelf entry, then pops back to it; the bookshelf
  ViewModel observes that handle and shows a confirmation snackbar.
- The whole graph is wrapped in a `SharedTransitionLayout`; the shared scopes are passed down through
  the `core-design` composition locals to animate book covers between screens.

---

## Cross-cutting conventions

- **No code comments.** The codebase is intentionally comment-free; names and small functions carry
  the meaning. Match this when contributing.
- **`Result<T>` everywhere** across the data/domain boundary; never throw across it.
- **Timber + `logTag()`** for logging; tags are prefixed `PROSE_DEBUG_LOG:`.
- **Immutable Compose inputs** via `kotlinx.collections.immutable`.
- **Version catalog only** — reference `libs.*` / `libs.versions.*` / `libs.plugins.*`, never
  hardcode versions ([`gradle/libs.versions.toml`](../gradle/libs.versions.toml)).
- **Testing** — ViewModels are unit-tested with Turbine + fake repositories and a
  `StandardTestDispatcher`; fakes live under each module's `src/test/.../fakes/`. UI tests use the
  Compose test rule. See `AGENTS.md` for the full testing notes.
