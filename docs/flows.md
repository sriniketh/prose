# Important Flows

Each flow is traced from the UI down to the data source and back. File paths are given so any step
can be opened directly. The shared UDF mechanics (StateFlow + effect Channel) are described once in
[architecture.md](architecture.md#the-udf-contract); this document focuses on what is specific to each
flow.

---

## 0. App bootstrap

```
ProseApplication (@HiltAndroidApp, plants Timber in debug)
  → MainActivity (@AndroidEntryPoint, enableEdgeToEdge, AppTheme)
    → ProseAppScreen  (SharedTransitionLayout + NavHost, start = "bookshelf")
```

- [`ProseApplication.kt`](../app/src/main/java/com/sriniketh/prose/ProseApplication.kt) initializes
  Hilt and logging.
- [`MainActivity.kt`](../app/src/main/java/com/sriniketh/prose/MainActivity.kt) sets the Compose
  content inside `AppTheme`.
- [`ProseAppScreen.kt`](../app/src/main/java/com/sriniketh/prose/ProseAppScreen.kt) builds the
  `NavHost` and provides the shared-transition composition locals. Start destination is the bookshelf.

---

## 1. Bookshelf — list saved books

**Reads the local DB as a stream; UI updates automatically when books change.**

```
BookshelfScreen
  → BookshelfViewModel.init
      → GetAllSavedBooksUseCase()                       (core-data)
        → BooksRepository.getAllSavedBooksFromDb(): Flow<Result<List<Book>>>
          → BookDao.getAllBooks(): Flow<List<BookEntity>>   (Room, core-db)
      ← entities.map { it.asBook() }  → map to BookUIState (ImmutableList)
```

Details:
- The ViewModel collects the DB `Flow` in `init`, so the list is reactive — inserting or deleting a
  book elsewhere re-emits here. See
  [`BookshelfViewModel`](../feature-bookshelf/src/main/java/com/sriniketh/feature_bookshelf/BookshelfViewModel.kt).
- A second `init` collector watches `savedStateHandle[BOOKSHELF_SHOW_ADDED_MESSAGE]`; when the
  add-book flow sets it `true`, a `ShowMessage` effect fires a confirmation snackbar and the flag is
  reset.
- DB errors map to `Result.failure` (caught in the repository's `.catch`) → `getallbooks` error
  snackbar.
- Tapping a book navigates to `view_highlights/{bookId}`; the FAB navigates to `search`.

---

## 2. Search books — debounced search-as-you-type

**Type a query; results refresh after a short pause, and stale in-flight responses are discarded.**

```
SearchBookScreen (text field) → SearchBookViewModel.searchForBook(query)
  → queryFlow (MutableStateFlow)
      .filter { length > 3 }
      .debounce(300ms)
      .distinctUntilChanged()
      .flatMapLatest { SearchForBookUseCase(query) }     ← cancels the previous search
        → BooksRepository.searchForBooks
          → BooksRemoteDataSource.getVolumes(query)  (projection = "lite")
          ← Volumes.asBookSearchResult()  → BookSearch  → BookUiState list
```

Details ([`SearchBookViewModel`](../feature-searchbooks/src/main/java/com/sriniketh/feature_searchbooks/SearchBookViewModel.kt)):
- Query and reset are modeled as a `SearchAction` sealed type and `merge`d into one pipeline.
- `flatMapLatest` is the key to correctness: when a newer query arrives, the in-flight request for the
  older query is cancelled, so a slow earlier response can never overwrite newer results (the
  search-as-you-type race condition this design fixes).
- `resetSearch()` emits `SearchAction.Clear`, restoring the empty state.
- Tapping a result navigates to `book_info/{bookId}`.

---

## 3. Book info & add to shelf

```
BookInfoScreen → BookInfoViewModel.getBookDetail(volumeId)
  → FetchBookInfoUseCase → BooksRepository.fetchBookInfo
       → BooksRemoteDataSource.getVolume(volumeId)  → Volume.asBook()
  → IsBookInDbUseCase → BookDao.doesBookExist(bookId)   → canAddToShelf = !isInDb

[user taps "Add to shelf"]
  → AddBookToShelfUseCase → BooksRepository.insertBookIntoDb
       → BookDao.insertBook(book.asBookEntity())   (onConflict = IGNORE)
  → Effect.NavigateToBookshelf
```

Details ([`BookInfoViewModel`](../feature-searchbooks/src/main/java/com/sriniketh/feature_searchbooks/BookInfoViewModel.kt)):
- `canAddToShelf` is computed up front so an already-saved book can't be added twice (DB insert also
  uses `IGNORE` as a safety net).
- On success the screen emits `NavigateToBookshelf`. The navigation layer
  ([`ProseAppScreen.kt`](../app/src/main/java/com/sriniketh/prose/ProseAppScreen.kt)) sets
  `BOOKSHELF_SHOW_ADDED_MESSAGE = true` on the bookshelf back-stack entry and pops back to it — that
  is what triggers the bookshelf's "added" snackbar (flow 1).

---

## 4. View highlights

```
ViewHighlightsScreen → ViewHighlightsViewModel.getHighlights(bookId)
  → GetAllSavedHighlightsUseCase(bookId)
      → HighlightsRepository.getAllHighlightsForBookFromDb(bookId): Flow<...>
        → HighlightDao.getAllHighlightsForBook(bookId): Flow<List<HighlightEntity>>
  ← sortedBy { savedOnTimestamp } → HighlightUIState list
```

Actions are dispatched through `processAction(ViewHighlightsAction)`
([`ViewHighlightsViewModel`](../feature-viewhighlights/src/main/java/com/sriniketh/feature_viewhighlights/ViewHighlightsViewModel.kt)):
- **Delete** → `DeleteHighlightUseCase` → `HighlightDao.deleteHighlightById(id)`.
- **Export** → see flow 6.
- **Camera permission denied** → `permission_denied` snackbar. The screen's camera-permission
  launcher requests `CAMERA`; on grant it navigates into the capture flow, on deny it dispatches the
  denied action.
- Adding a highlight navigates to `capture_and_crop_image/{bookId}`; editing one navigates to
  `save_highlight_from_highlight_id/{bookId}/{highlightId}`.

---

## 5. Add a highlight — capture → crop → OCR → save

This is the most involved flow and spans two ViewModels and three navigation destinations.

### 5a. Capture & crop

```
capture_and_crop_image/{bookId}
  → CaptureAndCropImageScreen + CaptureAndCropImageViewModel
      imageUri = CreateTempImageFileUseCase()  → FileSource.createNewFile("<uuid>.jpg")
                 (cacheDir file, exposed via FileProvider; stored in SavedStateHandle)
      state: CaptureImage → CropImage → ImageCapturedAndCropped
```

Steps ([`CaptureAndCropImageViewModel`](../feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/CaptureAndCropImageViewModel.kt),
[`CaptureAndCropImageScreen`](../feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/CaptureAndCropImageScreen.kt)):
1. **CaptureImage** — a `TakePicture` activity-result launcher writes the photo into the temp
   FileProvider URI. On cancel/failure the screen calls `goBack()`.
2. **CropImage** — `CropImageScreen` (Cropify) lets the user crop; `onImageCropped()` advances state.
3. **ImageCapturedAndCropped** — the screen calls `onImageCaptured(uri)`, which URL-encodes the URI
   and navigates to `save_highlight_from_uri/{bookId}/{encodedUri}`.
4. **Cleanup** — `onCleared()` deletes the temp file *unless* the flow completed
   (`ImageCapturedAndCropped`), so abandoned captures don't leak files. The completed file is handed
   off to the next screen, which deletes it after OCR.

### 5b. OCR & save

```
save_highlight_from_uri/{bookId}/{uri}
  → EditAndSaveHighlightScreen(uri) + EditAndSaveHighlightViewModel
      processImageForHighlightText(uri):
        → TextAnalyzer.analyzeImage(uri)        (ML Kit on-device Latin OCR)
        → visionText.text.replace("\n", " ")    → editable highlightText
        finally → DeleteFileUseCase(uri)         (delete the temp image)

[user edits text, taps save]
  → saveHighlight(bookId, text)
      → SaveHighlightUseCase → HighlightsRepository.insertHighlightIntoDb
          → HighlightDao.insertHighlight(highlight.asHighlightEntity())  (onConflict = REPLACE)
      timestamp = FormatCurrentDateTimeUseCase(DateTimeSource.now())
  → Effect.HighlightSaved → goBack to view_highlights/{bookId}
```

Details:
- [`TextAnalyzer`](../feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/TextAnalyzer.kt)
  wraps the callback-based ML Kit recognizer in `suspendCancellableCoroutine` and closes the
  recognizer on cancellation. OCR runs fully on-device.
- OCR failure → `image_processing_failure` snackbar; the temp file is deleted in `finally` either way.
- A new highlight gets a random `UUID` and the current formatted timestamp.

### 5c. Edit an existing highlight

```
save_highlight_from_highlight_id/{bookId}/{highlightId}
  → EditAndSaveHighlightViewModel.loadHighlightText(highlightId)
      → LoadHighlightUseCase → HighlightsRepository.loadHighlightFromDb → HighlightDao.getHighlightById
  ← prefilled text + screen title switches to "edit"; original savedOnTimestamp is preserved
[save] → updateHighlight(bookId, text, highlightId)   (same UUID → REPLACE updates the row)
```

The same [`EditAndSaveHighlightViewModel`](../feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/EditAndSaveHighlightViewModel.kt)
serves both new and edit cases. For edits, `savedOnTimestamp` is retained so the original capture time
is not overwritten, and the existing `highlightId` makes the REPLACE insert an update.

---

## 6. Export & share highlights

```
ViewHighlights "share" → ViewHighlightsViewModel.processAction(OnExportHighlights(bookId))
  → ExportHighlightsUseCase(bookId)
       book       = BooksRepository.getBookByIdFromDb(bookId).getOrThrow()
       highlights = HighlightsRepository.getAllHighlightsForBookFromDb(bookId).first().getOrThrow()
       → build HighlightsExport (book info + highlights)
       → Json { prettyPrint; explicitNulls = false }.encodeToString(...)
       → FileSource.writeToFile("<title>_export.json", json)   (cacheDir + FileProvider URI)
  → Effect.ShareHighlights(uri)
  → screen launches Intent.ACTION_SEND chooser
       (type application/json, EXTRA_STREAM = uri, FLAG_GRANT_READ_URI_PERMISSION)
```

Details:
- [`ExportHighlightsUseCase`](../core-data/src/main/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCase.kt)
  is one of the few use cases that coordinates **both** repositories. It takes a one-shot snapshot of
  the highlights `Flow` via `.first()`.
- The serialized shape is the `*Export` DTO set in
  [`HighlightsExport.kt`](../core-data/src/main/java/com/sriniketh/core_data/models/HighlightsExport.kt)
  (kept separate from domain models so the on-disk format is independent of internal types).
  `explicitNulls = false` omits null fields from the JSON.
- The file lands in the app cache dir and is shared through the `${applicationId}.fileProvider`
  declared in [`AndroidManifest.xml`](../app/src/main/AndroidManifest.xml); the share intent grants
  temporary read permission to the receiving app.
- Export failure → `export` error snackbar.

---

## Flow ↔ source map (quick index)

| Flow | Screen / ViewModel | Use case(s) | Data source |
|------|--------------------|-------------|-------------|
| Bookshelf | `feature-bookshelf` | `GetAllSavedBooksUseCase` | Room `BookDao` (Flow) |
| Search | `feature-searchbooks` (`SearchBookViewModel`) | `SearchForBookUseCase` | Books API `getVolumes` |
| Book info / add | `feature-searchbooks` (`BookInfoViewModel`) | `FetchBookInfoUseCase`, `IsBookInDbUseCase`, `AddBookToShelfUseCase` | API `getVolume` + `BookDao` |
| View highlights | `feature-viewhighlights` | `GetAllSavedHighlightsUseCase`, `DeleteHighlightUseCase` | Room `HighlightDao` (Flow) |
| Capture / crop | `feature-addhighlight` (`CaptureAndCropImageViewModel`) | `CreateTempImageFileUseCase`, `DeleteFileUseCase` | `FileSource` (cacheDir) |
| OCR / save | `feature-addhighlight` (`EditAndSaveHighlightViewModel`) | `SaveHighlightUseCase`, `LoadHighlightUseCase`, `FormatCurrentDateTimeUseCase`, `DeleteFileUseCase` | ML Kit + `HighlightDao` |
| Export / share | `feature-viewhighlights` | `ExportHighlightsUseCase` | both repos + `FileSource` |
