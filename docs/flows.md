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
      → BooksRepository.getAllSavedBooksFromDb(): Flow<Result<List<Book>>>   (core-data)
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
      .flatMapLatest { BooksRepository.searchForBooks(query) }     ← cancels the previous search
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
  → BooksRepository.fetchBookInfo
       → BooksRemoteDataSource.getVolume(volumeId)  → Volume.asBook()
  → BooksRepository.doesBookExistInDb → BookDao.doesBookExist(bookId)   → canAddToShelf = !isInDb

[user taps "Add to shelf"]
  → BooksRepository.insertBookIntoDb
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
  → HighlightsRepository.getAllHighlightsForBookFromDb(bookId): Flow<...>
      → HighlightDao.getAllHighlightsForBook(bookId): Flow<List<HighlightEntity>>
  ← sortedBy { savedOnTimestamp } → HighlightUIState list
```

Actions are dispatched through `processAction(ViewHighlightsAction)`
([`ViewHighlightsViewModel`](../feature-viewhighlights/src/main/java/com/sriniketh/feature_viewhighlights/ViewHighlightsViewModel.kt)):
- **Delete** → `HighlightsRepository.deleteHighlightFromDb` → `HighlightDao.deleteHighlightById(id)`.
- **Export** → see flow 6.
- **Camera permission denied** → `permission_denied` snackbar. The screen's camera-permission
  launcher requests `CAMERA`; on grant it navigates into the capture flow, on deny it dispatches the
  denied action.
- Adding a highlight navigates to `capture_and_select_text/{bookId}`; editing one navigates to
  `save_highlight_from_highlight_id/{bookId}/{highlightId}`.

---

## 5. Add a highlight — capture → select text → save

This flow spans two ViewModels and two navigation destinations. The user photographs a page, the
app recognizes its text on-device, and the user picks the passage by tapping or swiping over the
recognized lines drawn on top of the photo.

### 5a. Capture & select text

```
capture_and_select_text/{bookId}
  → CaptureAndSelectTextScreen + CaptureAndSelectTextViewModel
      photoUri = FileSource.createNewFile("<uuid>.jpg")
                 (cacheDir file, exposed via FileProvider; stored in SavedStateHandle)
      state: OpenCamera → CameraOpen → Reading → Selecting
                                              ↘ Failed (no text / unreadable) → Retake → OpenCamera
             CameraOpen → Abandoned (first photo cancelled) → goBack()
```

Steps ([`CaptureAndSelectTextViewModel`](../feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/CaptureAndSelectTextViewModel.kt),
[`CaptureAndSelectTextScreen`](../feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/CaptureAndSelectTextScreen.kt)):
1. **OpenCamera / CameraOpen** — a `TakePicture` activity-result launcher writes the photo into the
   temp FileProvider URI. The ViewModel records in `SavedStateHandle` that the camera is open and
   whether a photo exists, so a config change or process death never opens the camera twice and a
   restored screen re-reads the photo it already has.
2. **Reading** — [`PageScanner`](../feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/PageScanner.kt)
   decodes the photo once (EXIF-rotated, downsampled to a 2048 px long edge by `ImageRotater`) and
   runs ML Kit on that same bitmap, so every recognized line's corner points are in the coordinates
   of the image on screen. ML Kit's `Text` is mapped to an
   [`OcrPage`](../feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/ocr/OcrPage.kt)
   of lines tagged with their block.
3. **Selecting** — the photo is drawn with each recognized line as a faint tilted highlight.
   Selection is a [`LineSelection`](../feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/ocr/LineSelection.kt)
   reducer over line indices in reading order: tapping a block toggles all its lines; a drag
   selects (or, when it starts on a selected line, deselects) every line between the anchor and the
   line nearest the finger, and dragging back shrinks the range. Each block is also an accessibility
   node with its text as the content description and a click action. `OcrPage.textOf` assembles the
   selection: lines join with spaces, a word hyphenated across a line break is rejoined, and a new
   block starts a new line only when the previous one ended a sentence.
4. **Failed** — no text found or the photo could not be read; the user can retake. Cancelling a
   retake re-reads the existing photo.
5. **Continue** — the screen passes the selected text to `onTextSelected`; the navigation layer
   ([`ProseAppScreen.kt`](../app/src/main/java/com/sriniketh/prose/ProseAppScreen.kt)) URL-encodes it
   and navigates to `save_highlight_from_text/{bookId}/{text}`. The selection screen stays on the
   back stack, so back from the editor returns to the same selection.
6. **Cleanup** — `onCleared()` deletes the temp photo, which happens when the flow is abandoned or
   saving pops back to `view_highlights/{bookId}`.

### 5b. Edit & save

```
save_highlight_from_text/{bookId}/{text}
  → EditAndSaveNewHighlightScreen(highlightText) + EditAndSaveHighlightViewModel
      startWithText(text)                       (seeds the field once; edits survive rotation)

[user edits text, taps save]
  → saveHighlight(bookId, text)
      → HighlightsRepository.insertHighlightIntoDb
          → HighlightDao.insertHighlight(highlight.asHighlightEntity())  (onConflict = REPLACE)
      timestamp = FormatCurrentDateTimeUseCase(DateTimeSource.now())
  → Effect.HighlightSaved → goBack to view_highlights/{bookId}
```

Details:
- `PageScanner` wraps the callback-based ML Kit recognizer in `suspendCancellableCoroutine` and
  closes the recognizer on success, failure, and cancellation. OCR runs fully on-device.
- A new highlight gets a random `UUID` and the current formatted timestamp.

### 5c. Edit an existing highlight

```
save_highlight_from_highlight_id/{bookId}/{highlightId}
  → EditAndSaveHighlightViewModel.loadHighlightText(highlightId)
      → HighlightsRepository.loadHighlightFromDb → HighlightDao.getHighlightById
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
- [`ExportHighlightsUseCase`](../core-data/api/src/main/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCase.kt)
  is the only use case that coordinates **both** repositories. It takes a one-shot snapshot of
  the highlights `Flow` via `.first()`.
- The serialized shape is the `*Export` DTO set in
  [`HighlightsExport.kt`](../core-data/api/src/main/java/com/sriniketh/core_data/models/HighlightsExport.kt)
  (kept separate from domain models so the on-disk format is independent of internal types).
  `explicitNulls = false` omits null fields from the JSON.
- The file lands in the app cache dir and is shared through the `${applicationId}.fileProvider`
  declared in [`AndroidManifest.xml`](../app/src/main/AndroidManifest.xml); the share intent grants
  temporary read permission to the receiving app.
- Export failure → `export` error snackbar.

---

## Flow ↔ source map (quick index)

| Flow | Screen / ViewModel | Use case / repository call(s) | Data source |
|------|--------------------|-------------|-------------|
| Bookshelf | `feature-bookshelf` | `BooksRepository.getAllSavedBooksFromDb` | Room `BookDao` (Flow) |
| Search | `feature-searchbooks` (`SearchBookViewModel`) | `BooksRepository.searchForBooks` | Books API `getVolumes` |
| Book info / add | `feature-searchbooks` (`BookInfoViewModel`) | `BooksRepository.fetchBookInfo`, `BooksRepository.doesBookExistInDb`, `BooksRepository.insertBookIntoDb` | API `getVolume` + `BookDao` |
| View highlights | `feature-viewhighlights` | `HighlightsRepository.getAllHighlightsForBookFromDb`, `HighlightsRepository.deleteHighlightFromDb` | Room `HighlightDao` (Flow) |
| Capture / select text | `feature-addhighlight` (`CaptureAndSelectTextViewModel`) | `FileSource.createNewFile`, `FileSource.deleteFile`, `PageScanner` | `FileSource` (cacheDir) + ML Kit |
| Edit / save | `feature-addhighlight` (`EditAndSaveHighlightViewModel`) | `HighlightsRepository.insertHighlightIntoDb`, `HighlightsRepository.loadHighlightFromDb`, `FormatCurrentDateTimeUseCase` | `HighlightDao` |
| Export / share | `feature-viewhighlights` | `ExportHighlightsUseCase` | both repos + `FileSource` |
