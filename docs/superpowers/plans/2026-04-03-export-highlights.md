# Export Highlights Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a share icon button to the Saved Highlights top app bar that exports book details + highlights to a JSON file in the cache directory and opens the Android share sheet.

**Architecture:** Refactor `FileSource.createNewFile` to accept a `fileName` parameter and rename `CreateFileUseCase` to `CreateTempImageFileUseCase`. Add a `writeToFile` method to `FileSource` for writing string content. Add a `getBookById` query to BookDao/BooksRepository. Create a `HighlightsExport` model in core-data with Moshi codegen. Create an `ExportHighlightsUseCase` that fetches the full `Book` + `Highlight` models, wraps them in `HighlightsExport`, serializes to JSON via Moshi codegen, writes to cache via `FileSource`, and returns an `android.net.Uri`. The ViewModel exposes a `Uri?` in UI state. The Compose screen uses `ActivityResultContracts.StartActivityForResult` to launch the share sheet when the URI is emitted, cleared on callback.

**Tech Stack:** Moshi with codegen (already in project), Room, FileProvider (already configured), ActivityResultContracts

---

## Context

The app captures book highlights via OCR but has no way to export them. Users need a way to share their highlights externally. This adds a JSON export via the Android share sheet, accessed from a share icon in the highlights screen top app bar. The export includes all fields from `Book`/`BookInfo` and `Highlight` domain models, wrapped in a `HighlightsExport` model in core-data with Moshi codegen for compile-time safe serialization.

---

### Task 1: Refactor FileSource.createNewFile to accept fileName and rename CreateFileUseCase ✅

**Files:**
- Modify: `core-platform/src/main/java/com/sriniketh/core_platform/FileSource.kt`
- Modify: `app/src/main/java/com/sriniketh/prose/files/FileSourceImpl.kt`
- Rename: `core-data/src/main/java/com/sriniketh/core_data/usecases/CreateFileUseCase.kt` → `CreateTempImageFileUseCase.kt`
- Rename: `core-data/src/test/java/com/sriniketh/core_data/usecases/CreateFileUseCaseTest.kt` → `CreateTempImageFileUseCaseTest.kt`
- Modify: `feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/CaptureAndCropImageViewModel.kt`
- Modify: `core-data/src/test/java/com/sriniketh/core_data/fakes/FakeFileSource.kt`
- Modify: `feature-addhighlight/src/test/java/com/sriniketh/feature_addhighlight/fakes/FakeFileSource.kt`

- [x] **Step 1: Update FileSource interface to accept fileName**

In `core-platform/src/main/java/com/sriniketh/core_platform/FileSource.kt`:

```kotlin
package com.sriniketh.core_platform

import android.net.Uri

interface FileSource {
    fun createNewFile(fileName: String): Uri
    fun writeToFile(fileName: String, content: String): Uri
    fun deleteFile(uri: Uri): Boolean
}
```

- [x] **Step 2: Update FileSourceImpl**

In `app/src/main/java/com/sriniketh/prose/files/FileSourceImpl.kt`:

```kotlin
package com.sriniketh.prose.files

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider.getUriForFile
import com.sriniketh.core_platform.FileSource
import com.sriniketh.core_platform.logTag
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class FileSourceImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : FileSource {

    override fun createNewFile(fileName: String): Uri {
        val newFile = File(appContext.cacheDir, fileName)
        val contentUri = getFileProviderUri(newFile)
        Timber.d("${this.logTag()}: Created file $contentUri")
        return contentUri
    }

    override fun writeToFile(fileName: String, content: String): Uri {
        val file = File(appContext.cacheDir, fileName)
        file.writeText(content)
        val contentUri = getFileProviderUri(file)
        Timber.d("${this.logTag()}: Wrote file $contentUri")
        return contentUri
    }

    override fun deleteFile(uri: Uri): Boolean {
        val result = appContext.contentResolver.delete(uri, null, null)
        Timber.d("${this.logTag()}: Deleted rows $result")
        return result > 0
    }

    private fun getFileProviderUri(file: File): Uri {
        val authority = "${appContext.packageName}.fileProvider"
        return getUriForFile(appContext, authority, file)
    }
}
```

- [x] **Step 3: Update FakeFileSource in core-data test fakes**

In `core-data/src/test/java/com/sriniketh/core_data/fakes/FakeFileSource.kt`:

```kotlin
package com.sriniketh.core_data.fakes

import android.net.Uri
import com.sriniketh.core_platform.FileSource
import io.mockk.every
import io.mockk.mockk
import java.io.IOException

class FakeFileSource : FileSource {

    val deletedUris = mutableListOf<Uri>()
    var shouldDeleteFail = false

    var lastWrittenFileName: String? = null
    var lastWrittenContent: String? = null
    var shouldWriteToFileFail = false

    override fun createNewFile(fileName: String): Uri {
        val mockUri = mockk<Uri>()
        every { mockUri.toString() } returns "file://some_path/$fileName"
        return mockUri
    }

    override fun writeToFile(fileName: String, content: String): Uri {
        if (shouldWriteToFileFail) {
            throw IOException("Failed to write file")
        }
        lastWrittenFileName = fileName
        lastWrittenContent = content
        return Uri.parse("content://com.test.fileProvider/cache/$fileName")
    }

    override fun deleteFile(uri: Uri): Boolean {
        deletedUris.add(uri)
        return !shouldDeleteFail
    }
}
```

- [x] **Step 4: Update FakeFileSource in feature-addhighlight test fakes**

In `feature-addhighlight/src/test/java/com/sriniketh/feature_addhighlight/fakes/FakeFileSource.kt`:

```kotlin
package com.sriniketh.feature_addhighlight.fakes

import android.net.Uri
import com.sriniketh.core_platform.FileSource
import io.mockk.mockk

class FakeFileSource : FileSource {

    var deletedUris = mutableListOf<Uri>()
    var shouldDeleteFail = false

    override fun createNewFile(fileName: String): Uri {
        return mockk<Uri>()
    }

    override fun writeToFile(fileName: String, content: String): Uri {
        return Uri.parse("content://com.test.fileProvider/cache/$fileName")
    }

    override fun deleteFile(uri: Uri): Boolean {
        deletedUris.add(uri)
        return !shouldDeleteFail
    }
}
```

- [x] **Step 5: Rename CreateFileUseCase to CreateTempImageFileUseCase**

Delete `core-data/src/main/java/com/sriniketh/core_data/usecases/CreateFileUseCase.kt` and create `core-data/src/main/java/com/sriniketh/core_data/usecases/CreateTempImageFileUseCase.kt`:

```kotlin
package com.sriniketh.core_data.usecases

import android.net.Uri
import com.sriniketh.core_platform.FileSource
import java.util.UUID
import javax.inject.Inject

class CreateTempImageFileUseCase @Inject constructor(
    private val fileSource: FileSource
) {
    operator fun invoke(): Uri = fileSource.createNewFile("${UUID.randomUUID()}.jpg")
}
```

- [x] **Step 6: Rename and update CreateFileUseCaseTest**

Delete `core-data/src/test/java/com/sriniketh/core_data/usecases/CreateFileUseCaseTest.kt` and create `core-data/src/test/java/com/sriniketh/core_data/usecases/CreateTempImageFileUseCaseTest.kt`:

```kotlin
package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeFileSource
import com.sriniketh.core_platform.FileSource
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateTempImageFileUseCaseTest {

    private val fileSource: FileSource = FakeFileSource()
    private val createTempImageFileUseCase = CreateTempImageFileUseCase(fileSource)

    @Test
    fun `when invoked then creates new file using file source`() {
        val file = createTempImageFileUseCase()
        assertTrue(file.toString().contains("file://some_path/"))
    }
}
```

- [x] **Step 7: Update CaptureAndCropImageViewModel to use renamed use case**

In `feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/CaptureAndCropImageViewModel.kt`, update the import and constructor:

Change:
```kotlin
import com.sriniketh.core_data.usecases.CreateFileUseCase
```
to:
```kotlin
import com.sriniketh.core_data.usecases.CreateTempImageFileUseCase
```

Change constructor:
```kotlin
private val createFileUseCase: CreateFileUseCase,
```
to:
```kotlin
private val createTempImageFileUseCase: CreateTempImageFileUseCase,
```

Change usage:
```kotlin
savedStateHandle.get<Uri>("imageUri") ?: createFileUseCase().also {
```
to:
```kotlin
savedStateHandle.get<Uri>("imageUri") ?: createTempImageFileUseCase().also {
```

- [x] **Step 8: Verify the project compiles and existing tests pass**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

Run: `./gradlew :core-data:test --tests "com.sriniketh.core_data.usecases.CreateTempImageFileUseCaseTest" --info`
Expected: PASS

- [x] **Step 9: Commit**

```bash
git add core-platform/src/main/java/com/sriniketh/core_platform/FileSource.kt \
       app/src/main/java/com/sriniketh/prose/files/FileSourceImpl.kt \
       core-data/src/main/java/com/sriniketh/core_data/usecases/CreateTempImageFileUseCase.kt \
       core-data/src/test/java/com/sriniketh/core_data/usecases/CreateTempImageFileUseCaseTest.kt \
       core-data/src/test/java/com/sriniketh/core_data/fakes/FakeFileSource.kt \
       feature-addhighlight/src/test/java/com/sriniketh/feature_addhighlight/fakes/FakeFileSource.kt \
       feature-addhighlight/src/main/java/com/sriniketh/feature_addhighlight/CaptureAndCropImageViewModel.kt
git rm core-data/src/main/java/com/sriniketh/core_data/usecases/CreateFileUseCase.kt \
       core-data/src/test/java/com/sriniketh/core_data/usecases/CreateFileUseCaseTest.kt
git commit -m "refactor: rename CreateFileUseCase to CreateTempImageFileUseCase and add fileName param to FileSource"
```

---

### Task 2: Add `getBookById` to BookDao and FakeBookDao ✅

**Files:**
- Modify: `core-db/src/main/java/com/sriniketh/core_db/dao/BookDao.kt`
- Modify: `core-data/src/test/java/com/sriniketh/core_data/fakes/FakeBookDao.kt`

- [x] **Step 1: Add the query method to BookDao**

In `core-db/src/main/java/com/sriniketh/core_db/dao/BookDao.kt`, add after the `doesBookExist` method:

```kotlin
@Query("SELECT * FROM bookEntity WHERE id = :bookId")
suspend fun getBookById(bookId: String): BookEntity?
```

- [x] **Step 2: Add the method to FakeBookDao**

In `core-data/src/test/java/com/sriniketh/core_data/fakes/FakeBookDao.kt`, add:

```kotlin
var shouldGetBookByIdThrowException = false
override suspend fun getBookById(bookId: String): BookEntity? {
    if (shouldGetBookByIdThrowException) {
        throw RuntimeException("some error fetching book by id")
    }
    return booksInDb.find { it.id == bookId }
}
```

- [x] **Step 3: Verify the project compiles**

Run: `./gradlew :core-db:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [x] **Step 4: Commit**

```bash
git add core-db/src/main/java/com/sriniketh/core_db/dao/BookDao.kt \
       core-data/src/test/java/com/sriniketh/core_data/fakes/FakeBookDao.kt
git commit -m "feat: add getBookById query to BookDao and FakeBookDao"
```

---

### Task 3: Add `getBookByIdFromDb` to BooksRepository with fakes and tests ✅

**Files:**
- Modify: `core-data/src/main/java/com/sriniketh/core_data/BooksRepository.kt`
- Modify: `core-data/src/main/java/com/sriniketh/core_data/BooksRepositoryImpl.kt`
- Modify: `core-data/src/test/java/com/sriniketh/core_data/BooksRepositoryImplTest.kt`
- Modify: `core-data/src/test/java/com/sriniketh/core_data/fakes/FakeBooksRepository.kt`

- [x] **Step 1: Write failing tests for getBookByIdFromDb**

Add to `core-data/src/test/java/com/sriniketh/core_data/BooksRepositoryImplTest.kt`:

```kotlin
@Test
fun `getBookByIdFromDb returns success Result when book exists in db`() = runTest {
    bookDao.booksInDb.add(fakeBookEntity("someId"))
    val result = booksRepositoryImpl.getBookByIdFromDb("someId")
    assertTrue(result.isSuccess)
    val book = result.getOrNull()!!
    assertEquals("someId", book.id)
    assertEquals("some title", book.info.title)
}

@Test
fun `getBookByIdFromDb returns failure Result when book does not exist in db`() = runTest {
    val result = booksRepositoryImpl.getBookByIdFromDb("nonexistent")
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is NoSuchElementException)
}

@Test
fun `getBookByIdFromDb returns failure Result when exception occurs`() = runTest {
    bookDao.shouldGetBookByIdThrowException = true
    val result = booksRepositoryImpl.getBookByIdFromDb("someId")
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is RuntimeException)
    assertEquals("some error fetching book by id", result.exceptionOrNull()?.message)
}
```

- [x] **Step 2: Run tests to verify they fail**

Run: `./gradlew :core-data:test --tests "com.sriniketh.core_data.BooksRepositoryImplTest" --info`
Expected: FAIL — `getBookByIdFromDb` does not exist yet.

- [x] **Step 3: Add the method to the BooksRepository interface**

In `core-data/src/main/java/com/sriniketh/core_data/BooksRepository.kt`, add after the `doesBookExistInDb` method:

```kotlin
suspend fun getBookByIdFromDb(bookId: String): Result<Book>
```

- [x] **Step 4: Implement in BooksRepositoryImpl**

In `core-data/src/main/java/com/sriniketh/core_data/BooksRepositoryImpl.kt`, add after the `doesBookExistInDb` method:

```kotlin
override suspend fun getBookByIdFromDb(bookId: String): Result<Book> =
    try {
        val entity = localBookDataSource.getBookById(bookId)
        if (entity != null) {
            Result.success(entity.asBook())
        } else {
            Result.failure(NoSuchElementException("Book not found: $bookId"))
        }
    } catch (exception: Exception) {
        Timber.e(exception, this.logTag())
        Result.failure(exception)
    }
```

Note: `asBook()` extension is defined in `core-data/src/main/java/com/sriniketh/core_data/transformers/Book.kt`.

- [x] **Step 5: Update FakeBooksRepository**

In `core-data/src/test/java/com/sriniketh/core_data/fakes/FakeBooksRepository.kt`, add the flag and method:

```kotlin
var shouldGetBookByIdFromDbThrowException = false
```

And the implementation:

```kotlin
override suspend fun getBookByIdFromDb(bookId: String): Result<Book> {
    return if (shouldGetBookByIdFromDbThrowException) {
        Result.failure(NoSuchElementException("Book not found"))
    } else {
        Result.success(fakeBook)
    }
}
```

- [x] **Step 6: Run tests to verify they pass**

Run: `./gradlew :core-data:test --tests "com.sriniketh.core_data.BooksRepositoryImplTest" --info`
Expected: All tests PASS (both existing and new)

- [x] **Step 7: Commit**

```bash
git add core-data/src/main/java/com/sriniketh/core_data/BooksRepository.kt \
       core-data/src/main/java/com/sriniketh/core_data/BooksRepositoryImpl.kt \
       core-data/src/test/java/com/sriniketh/core_data/BooksRepositoryImplTest.kt \
       core-data/src/test/java/com/sriniketh/core_data/fakes/FakeBooksRepository.kt
git commit -m "feat: add getBookByIdFromDb to BooksRepository with tests"
```

---

### Task 4: Create HighlightsExport model and ExportHighlightsUseCase ✅

**Files:**
- Create: `core-data/src/main/java/com/sriniketh/core_data/models/HighlightsExport.kt`
- Create: `core-data/src/main/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCase.kt`
- Create: `core-data/src/test/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCaseTest.kt`
- Modify: `core-data/build.gradle.kts`

The use case fetches the full `Book` and `List<Highlight>` domain models, wraps them in a `HighlightsExport` model (Moshi codegen), serializes to JSON, writes to cache via `FileSource.writeToFile`, and returns a `Uri`.

- [x] **Step 1: Add Moshi dependencies to core-data**

In `core-data/build.gradle.kts`, add to the dependencies block:

```kotlin
implementation(libs.moshi)
ksp(libs.moshi.codegen)
```

- [x] **Step 2: Create the HighlightsExport model**

Create: `core-data/src/main/java/com/sriniketh/core_data/models/HighlightsExport.kt`

```kotlin
package com.sriniketh.core_data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HighlightsExport(
    val id: String,
    val info: BookInfoExport,
    val highlights: List<HighlightExport>
)

@JsonClass(generateAdapter = true)
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

@JsonClass(generateAdapter = true)
data class HighlightExport(
    val id: String,
    val bookId: String,
    val text: String,
    val savedOnTimestamp: String
)
```

Note: We need separate `@JsonClass`-annotated export copies of `BookInfo` and `Highlight` because the originals in `core-models` have no Moshi annotations and core-models has no Moshi dependency. The use case maps domain models to these export models before serialization.

- [x] **Step 3: Write the failing test**

Create: `core-data/src/test/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCaseTest.kt`

```kotlin
package com.sriniketh.core_data.usecases

import com.sriniketh.core_data.fakes.FakeBooksRepository
import com.sriniketh.core_data.fakes.FakeFileSource
import com.sriniketh.core_data.fakes.FakeHighlightsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportHighlightsUseCaseTest {

    private lateinit var fakeBooksRepository: FakeBooksRepository
    private lateinit var fakeHighlightsRepository: FakeHighlightsRepository
    private lateinit var fakeFileSource: FakeFileSource
    private lateinit var useCase: ExportHighlightsUseCase

    @Before
    fun setup() {
        fakeBooksRepository = FakeBooksRepository()
        fakeHighlightsRepository = FakeHighlightsRepository()
        fakeFileSource = FakeFileSource()
        useCase = ExportHighlightsUseCase(fakeBooksRepository, fakeHighlightsRepository, fakeFileSource)
    }

    @Test
    fun `when invoked with valid bookId then returns success with uri`() = runTest {
        val result = useCase("test-book-id")
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrThrow())
    }

    @Test
    fun `when invoked then writes json with all book fields and highlights to cache`() = runTest {
        useCase("test-book-id")
        val writtenContent = fakeFileSource.lastWrittenContent!!
        assertTrue(writtenContent.contains("\"title\""))
        assertTrue(writtenContent.contains("Test Title"))
        assertTrue(writtenContent.contains("\"authors\""))
        assertTrue(writtenContent.contains("Test Author"))
        assertTrue(writtenContent.contains("\"publisher\""))
        assertTrue(writtenContent.contains("\"highlights\""))
        assertTrue(writtenContent.contains("Test highlight text"))
        assertEquals("highlights_export.json", fakeFileSource.lastWrittenFileName)
    }

    @Test
    fun `when book not found then returns failure`() = runTest {
        fakeBooksRepository.shouldGetBookByIdFromDbThrowException = true
        val result = useCase("nonexistent-id")
        assertTrue(result.isFailure)
    }

    @Test
    fun `when highlights retrieval fails then returns failure`() = runTest {
        fakeHighlightsRepository.shouldGetAllHighlightsForBookFromDbThrowException = true
        val result = useCase("test-book-id")
        assertTrue(result.isFailure)
    }

    @Test
    fun `when file write fails then returns failure`() = runTest {
        fakeFileSource.shouldWriteToFileFail = true
        val result = useCase("test-book-id")
        assertTrue(result.isFailure)
    }
}
```

- [x] **Step 4: Run the test to verify it fails**

Run: `./gradlew :core-data:test --tests "com.sriniketh.core_data.usecases.ExportHighlightsUseCaseTest" --info`
Expected: FAIL — `ExportHighlightsUseCase` does not exist yet.

- [x] **Step 5: Create the ExportHighlightsUseCase**

Create: `core-data/src/main/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCase.kt`

```kotlin
package com.sriniketh.core_data.usecases

import android.net.Uri
import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_data.HighlightsRepository
import com.sriniketh.core_data.models.BookInfoExport
import com.sriniketh.core_data.models.HighlightExport
import com.sriniketh.core_data.models.HighlightsExport
import com.sriniketh.core_platform.FileSource
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExportHighlightsUseCase @Inject constructor(
    private val booksRepository: BooksRepository,
    private val highlightsRepository: HighlightsRepository,
    private val fileSource: FileSource
) {
    suspend operator fun invoke(bookId: String): Result<Uri> = try {
        val book = booksRepository.getBookByIdFromDb(bookId).getOrThrow()
        val highlights = highlightsRepository.getAllHighlightsForBookFromDb(bookId).first().getOrThrow()

        val export = HighlightsExport(
            id = book.id,
            info = BookInfoExport(
                title = book.info.title,
                subtitle = book.info.subtitle,
                authors = book.info.authors,
                thumbnailLink = book.info.thumbnailLink,
                publisher = book.info.publisher,
                publishedDate = book.info.publishedDate,
                description = book.info.description,
                pageCount = book.info.pageCount,
                averageRating = book.info.averageRating,
                ratingsCount = book.info.ratingsCount
            ),
            highlights = highlights.map { highlight ->
                HighlightExport(
                    id = highlight.id,
                    bookId = highlight.bookId,
                    text = highlight.text,
                    savedOnTimestamp = highlight.savedOnTimestamp
                )
            }
        )

        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(HighlightsExport::class.java).indent("  ")
        val json = adapter.toJson(export)

        val uri = fileSource.writeToFile("highlights_export.json", json)
        Result.success(uri)
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
```

- [x] **Step 6: Run the tests to verify they pass**

Run: `./gradlew :core-data:test --tests "com.sriniketh.core_data.usecases.ExportHighlightsUseCaseTest" --info`
Expected: All 5 tests PASS

- [x] **Step 7: Commit**

```bash
git add core-data/src/main/java/com/sriniketh/core_data/models/HighlightsExport.kt \
       core-data/src/main/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCase.kt \
       core-data/src/test/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCaseTest.kt \
       core-data/build.gradle.kts
git commit -m "feat: add HighlightsExport model and ExportHighlightsUseCase with Moshi codegen"
```

---

### Task 5: Add export action to ViewHighlightsViewModel ✅

**Files:**
- Modify: `feature-viewhighlights/src/main/java/com/sriniketh/feature_viewhighlights/ViewHighlightsViewModel.kt`
- Modify: `feature-viewhighlights/src/main/res/values/strings.xml`
- Modify: `feature-viewhighlights/src/test/java/com/sriniketh/feature_viewhighlights/ViewHighlightsViewModelTest.kt`
- Create: `feature-viewhighlights/src/test/java/com/sriniketh/feature_viewhighlights/fakes/FakeBooksRepository.kt`
- Create: `feature-viewhighlights/src/test/java/com/sriniketh/feature_viewhighlights/fakes/FakeFileSource.kt`

- [x] **Step 1: Write failing tests for export functionality**

Add to `ViewHighlightsViewModelTest.kt`:

```kotlin
@Test
fun `when exportHighlights is called then sets loading state to true`() = runTest {
    viewModel.highlightsUIStateFlow.test {
        awaitItem()

        viewModel.exportHighlights("test-book-id")

        val loadingState = awaitItem()
        assertTrue(loadingState.isLoading)

        cancelAndIgnoreRemainingEvents()
    }
}

@Test
fun `when exportHighlights succeeds then emits export uri and clears loading`() = runTest {
    viewModel.highlightsUIStateFlow.test {
        awaitItem()

        viewModel.exportHighlights("test-book-id")

        awaitItem() // loading
        val successState = awaitItem()
        assertFalse(successState.isLoading)
        assertNotNull(successState.exportUri)
    }
}

@Test
fun `when exportHighlights fails then shows error and clears loading`() = runTest {
    fakeBooksRepository.shouldGetBookByIdFromDbThrowException = true

    viewModel.highlightsUIStateFlow.test {
        awaitItem()

        viewModel.exportHighlights("test-book-id")

        awaitItem() // loading
        val errorState = awaitItem()
        assertFalse(errorState.isLoading)
        assertEquals(R.string.export_error_message, errorState.snackBarText)
    }
}

@Test
fun `when clearExportUri is called then clears the uri`() = runTest {
    viewModel.highlightsUIStateFlow.test {
        awaitItem()

        viewModel.exportHighlights("test-book-id")
        awaitItem() // loading
        awaitItem() // success with uri

        viewModel.clearExportUri()
        val clearedState = awaitItem()
        assertNull(clearedState.exportUri)
    }
}
```

- [x] **Step 2: Run tests to verify they fail**

Run: `./gradlew :feature-viewhighlights:test --tests "com.sriniketh.feature_viewhighlights.ViewHighlightsViewModelTest" --info`
Expected: FAIL — `exportHighlights` method and `exportUri` field don't exist.

- [x] **Step 3: Create FakeBooksRepository in feature-viewhighlights test fakes**

Create: `feature-viewhighlights/src/test/java/com/sriniketh/feature_viewhighlights/fakes/FakeBooksRepository.kt`

```kotlin
package com.sriniketh.feature_viewhighlights.fakes

import com.sriniketh.core_data.BooksRepository
import com.sriniketh.core_models.book.Book
import com.sriniketh.core_models.book.BookInfo
import com.sriniketh.core_models.search.BookSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeBooksRepository : BooksRepository {

    var shouldGetBookByIdFromDbThrowException = false

    override suspend fun searchForBooks(searchQuery: String): Result<BookSearch> {
        return Result.failure(NotImplementedError())
    }

    override suspend fun fetchBookInfo(volumeId: String): Result<Book> {
        return Result.failure(NotImplementedError())
    }

    override suspend fun insertBookIntoDb(book: Book): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun doesBookExistInDb(bookId: String): Boolean = true

    override fun getAllSavedBooksFromDb(): Flow<Result<List<Book>>> = flow {
        emit(Result.success(emptyList()))
    }

    override suspend fun deleteBookFromDb(book: Book): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getBookByIdFromDb(bookId: String): Result<Book> {
        return if (shouldGetBookByIdFromDbThrowException) {
            Result.failure(NoSuchElementException("Book not found"))
        } else {
            Result.success(
                Book(
                    id = bookId,
                    info = BookInfo(
                        title = "Test Book Title",
                        subtitle = null,
                        authors = listOf("Test Author"),
                        thumbnailLink = null,
                        publisher = null,
                        publishedDate = null,
                        description = null,
                        pageCount = null,
                        averageRating = null,
                        ratingsCount = null
                    )
                )
            )
        }
    }
}
```

- [x] **Step 4: Create FakeFileSource in feature-viewhighlights test fakes**

Create: `feature-viewhighlights/src/test/java/com/sriniketh/feature_viewhighlights/fakes/FakeFileSource.kt`

```kotlin
package com.sriniketh.feature_viewhighlights.fakes

import android.net.Uri
import com.sriniketh.core_platform.FileSource

class FakeFileSource : FileSource {

    override fun createNewFile(fileName: String): Uri {
        return Uri.parse("content://com.test.fileProvider/cache/$fileName")
    }

    override fun writeToFile(fileName: String, content: String): Uri {
        return Uri.parse("content://com.test.fileProvider/cache/$fileName")
    }

    override fun deleteFile(uri: Uri): Boolean = true
}
```

- [x] **Step 5: Add `OnExportHighlights` event to ViewHighlightsEvent**

In `ViewHighlightsViewModel.kt`, add to `ViewHighlightsEvent`:

```kotlin
data object OnExportHighlights : ViewHighlightsEvent
```

- [x] **Step 6: Update ViewHighlightsUIState to include exportUri**

In `ViewHighlightsViewModel.kt`, update the data class:

```kotlin
internal data class ViewHighlightsUIState(
    val isLoading: Boolean = false,
    val highlights: List<HighlightUIState> = emptyList(),
    @StringRes val snackBarText: Int? = null,
    val exportUri: Uri? = null
)
```

Add import: `import android.net.Uri`

- [x] **Step 7: Add ExportHighlightsUseCase to ViewModel and implement export methods**

Update constructor:

```kotlin
@HiltViewModel
class ViewHighlightsViewModel @Inject constructor(
    private val getAllSavedHighlightsUseCase: GetAllSavedHighlightsUseCase,
    private val deleteHighlightUseCase: DeleteHighlightUseCase,
    private val exportHighlightsUseCase: ExportHighlightsUseCase
) : ViewModel() {
```

Add import: `import com.sriniketh.core_data.usecases.ExportHighlightsUseCase`

Add methods:

```kotlin
internal fun exportHighlights(bookId: String) {
    viewModelScope.launch {
        _highlightsUIStateFlow.update { state ->
            state.copy(isLoading = true)
        }
        val result = exportHighlightsUseCase(bookId)
        if (result.isSuccess) {
            _highlightsUIStateFlow.update { state ->
                state.copy(
                    isLoading = false,
                    exportUri = result.getOrThrow()
                )
            }
        } else {
            _highlightsUIStateFlow.update { state ->
                state.copy(
                    isLoading = false,
                    snackBarText = R.string.export_error_message
                )
            }
        }
    }
}

internal fun clearExportUri() {
    _highlightsUIStateFlow.update { state ->
        state.copy(exportUri = null)
    }
}
```

- [x] **Step 8: Add string resources**

In `feature-viewhighlights/src/main/res/values/strings.xml`, add before `</resources>`:

```xml
<string name="export_error_message">Unable to export highlights.</string>
<string name="share_button_cont_desc">Share highlights</string>
```

- [x] **Step 9: Update test setup to include new dependencies**

In `ViewHighlightsViewModelTest.kt`, add fields:

```kotlin
private lateinit var fakeBooksRepository: FakeBooksRepository
private lateinit var fakeFileSource: FakeFileSource
private lateinit var exportHighlightsUseCase: ExportHighlightsUseCase
```

Update `setup()`:

```kotlin
@Before
fun setup() {
    Dispatchers.setMain(StandardTestDispatcher())
    fakeHighlightsRepository = FakeHighlightsRepository()
    fakeBooksRepository = FakeBooksRepository()
    fakeFileSource = FakeFileSource()
    getAllSavedHighlightsUseCase = GetAllSavedHighlightsUseCase(fakeHighlightsRepository)
    deleteHighlightUseCase = DeleteHighlightUseCase(fakeHighlightsRepository)
    exportHighlightsUseCase = ExportHighlightsUseCase(fakeBooksRepository, fakeHighlightsRepository, fakeFileSource)
    viewModel = ViewHighlightsViewModel(
        getAllSavedHighlightsUseCase,
        deleteHighlightUseCase,
        exportHighlightsUseCase
    )
}
```

Add imports:

```kotlin
import com.sriniketh.core_data.usecases.ExportHighlightsUseCase
import com.sriniketh.feature_viewhighlights.fakes.FakeBooksRepository
import com.sriniketh.feature_viewhighlights.fakes.FakeFileSource
import org.junit.Assert.assertNotNull
```

- [x] **Step 10: Run the tests to verify they pass**

Run: `./gradlew :feature-viewhighlights:test --tests "com.sriniketh.feature_viewhighlights.ViewHighlightsViewModelTest" --info`
Expected: All tests PASS (both existing and new)

- [x] **Step 11: Commit**

```bash
git add feature-viewhighlights/src/main/java/com/sriniketh/feature_viewhighlights/ViewHighlightsViewModel.kt \
       feature-viewhighlights/src/main/res/values/strings.xml \
       feature-viewhighlights/src/test/java/com/sriniketh/feature_viewhighlights/ViewHighlightsViewModelTest.kt \
       feature-viewhighlights/src/test/java/com/sriniketh/feature_viewhighlights/fakes/FakeBooksRepository.kt \
       feature-viewhighlights/src/test/java/com/sriniketh/feature_viewhighlights/fakes/FakeFileSource.kt
git commit -m "feat: add export action to ViewHighlightsViewModel"
```

---

### Task 6: Add share icon button and share sheet to ViewHighlightsScreen ✅

**Files:**
- Modify: `feature-viewhighlights/src/main/java/com/sriniketh/feature_viewhighlights/ViewHighlightsScreen.kt`

- [x] **Step 1: Add the share icon button in the ProseTopAppBar actions slot**

In `ViewHighlightsScreen.kt`, update the `ProseTopAppBar` to include the `actions` parameter:

```kotlin
ProseTopAppBar(
    title = {
        Text(
            text = stringResource(id = R.string.highlights_pagetitle),
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    },
    navigationIcon = { NavigationBack { onEvent(ViewHighlightsEvent.OnBackPressed) } },
    actions = {
        IconButton(onClick = { onEvent(ViewHighlightsEvent.OnExportHighlights) }) {
            Icon(
                painter = painterResource(com.sriniketh.core_design.R.drawable.ic_share),
                contentDescription = stringResource(id = R.string.share_button_cont_desc)
            )
        }
    },
    scrollBehavior = scrollBehavior,
)
```

- [x] **Step 2: Add the share sheet launcher and export URI handling in ViewHighlightsScreen**

In the outer `ViewHighlightsScreen` composable, add after `collectAsStateWithLifecycle()`:

```kotlin
val shareLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) {
    viewModel.clearExportUri()
}

uiState.exportUri?.let { uri ->
    LaunchedEffect(uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        shareLauncher.launch(Intent.createChooser(shareIntent, null))
    }
}
```

Update the `ViewHighlights` `onEvent` handler to include the new event:

```kotlin
is ViewHighlightsEvent.OnExportHighlights -> viewModel.exportHighlights(bookId)
```

Add import: `import android.content.Intent`

- [x] **Step 3: Verify the project compiles**

Run: `./gradlew :feature-viewhighlights:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [x] **Step 4: Commit**

```bash
git add feature-viewhighlights/src/main/java/com/sriniketh/feature_viewhighlights/ViewHighlightsScreen.kt
git commit -m "feat: add share icon button with share sheet for highlight export"
```

---

### Task 7: Run all tests and verify ✅

**Files:** No changes — verification only.

- [x] **Step 1: Run all unit tests**

Run: `./gradlew test`
Expected: All tests PASS

- [x] **Step 2: Run the full debug build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [x] **Step 3: Final commit if any fixes were needed**

If any fixes were required, commit them with an appropriate message.

---

## Verification

1. **Unit tests**: `./gradlew test` — all existing + new tests pass
2. **Build**: `./gradlew assembleDebug` — compiles successfully
3. **Manual test**: Install on device/emulator, navigate to a book's highlights, tap the share icon in the top app bar, verify the share sheet appears with JSON content containing all book fields and highlights
