# Data Export Format

This document defines the JSON structure and logic used to export book highlights from the local Room database to shareable files on Android.

---

## Serialization & Formatting

The export pipeline is managed by [ExportHighlightsUseCase](../core-data/src/main/java/com/sriniketh/core_data/usecases/ExportHighlightsUseCase.kt).

* **JSON Library**: Kotlinx Serialization is used to format outputs.
* **Formatting Details**: Outputs are configured to output human-readable, pretty-printed JSON structure:
  ```kotlin
  private val exportJson = Json {
      prettyPrint = true
      prettyPrintIndent = "  "
      explicitNulls = false // Exclude properties with null values from the JSON output
  }
  ```

---

## JSON Schema

The top-level structure is [HighlightsExport](../core-data/src/main/java/com/sriniketh/core_data/models/HighlightsExport.kt), matching the format:

```json
{
  "id": "book_volume_id",
  "info": {
    "title": "Book Title",
    "subtitle": "Book Subtitle (optional)",
    "authors": [
      "Author Name 1",
      "Author Name 2"
    ],
    "thumbnailLink": "https://books.google.com/...",
    "publisher": "Publisher Name (optional)",
    "publishedDate": "YYYY-MM-DD (optional)",
    "description": "Book synopsis text...",
    "pageCount": 350,
    "averageRating": 4.5,
    "ratingsCount": 20
  },
  "highlights": [
    {
      "id": "highlight_uuid_1",
      "bookId": "book_volume_id",
      "text": "The first extracted highlight text.",
      "savedOnTimestamp": "YYYY-MM-DD HH:mm:ss"
    },
    {
      "id": "highlight_uuid_2",
      "bookId": "book_volume_id",
      "text": "The second extracted highlight text.",
      "savedOnTimestamp": "YYYY-MM-DD HH:mm:ss"
    }
  ]
}
```

---

## File Lifecycle & Naming

1. **Naming Strategy**:
   The exported filename is formatted by converting the book's title to lowercase and replacing all spaces with underscores, appended with `_export.json`.
   ```kotlin
   val fileName = "${book.info.title.lowercase().replace(" ", "_")}_export.json"
   ```
2. **File Creation**:
   The `ExportHighlightsUseCase` calls [FileSource.writeToFile(fileName, json)](../core-platform/src/main/java/com/sriniketh/core_platform/FileSource.kt) to write the serialized string to Android's storage. It returns the file's sharing `Uri`.
3. **Usage in UI**:
   The sharing `Uri` returned by the UseCase is handed to the operating system's sharing intent provider (Share Sheet) in `ViewHighlightsScreen` to enable copying, email sending, or cloud saving.
