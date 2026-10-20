# View and export highlights

For a saved book a user sees its highlights with their saved dates, can delete one, and can export all of them as a JSON file through the system share sheet.

## Sub-features

- `highlights-list` shows each highlight's text and `Saved on: <timestamp>`.
- `highlights-empty` shows `No saved highlights` for a book without any.
- `highlights-export` writes `<title>_export.json` and opens the system share sheet.
- `highlights-delete` removes one highlight after a confirmation dialog.

## How to get to it (user POV)

- Choose a book card on the bookshelf.

## Driving it with ui.sh

Preconditions:

- `scripts/launch.sh` has printed `ready:` and `scripts/doctor.sh` passes.
- `Dune` is on the shelf, added through `search-and-add-book.md`.
- Highlights are created in the app only through camera OCR of a page photo, which takes the setup in `add-highlight.md`. To test listing alone, inject one row instead:

```bash
adb -s emulator-5580 shell am force-stop com.sriniketh.prose
.claude/skills/verify-prose/scripts/db.sh "insert into HighlightEntity values ('verify-h1','B1hSG45JCX4C','Fear is the mind-killer.','10-18-2026 12:35 AM');"
adb -s emulator-5580 shell am start -W -n com.sriniketh.prose/.MainActivity
```

  `bookId` must equal the stored `BookEntity.id` (`db.sh "select id from BookEntity;"`). The app writes timestamps as `MM-dd-yyyy hh:mm a`; the column is plain text, so use that shape.

- **highlights-empty.** Before injecting, `U tap "Dune"`, `U wait "No saved highlights"`.
- **highlights-list.** After injecting and relaunching, `U wait "Dune"`, `U tap "Dune"`, `U wait "Fear is the mind-killer." 15`. The screen also lists `Saved on: ` followed by the stored timestamp. Capture `U shot view-highlights-dune`.
- **highlights-export.** `U tap "Share highlights"` and wait about 3 seconds. The foreground activity is the system chooser: `adb -s emulator-5580 shell dumpsys activity activities | grep topResumedActivity` names `com.android.intentresolver/.ChooserActivity`. Capture `U shot export-chooser`. Read the file back with `adb -s emulator-5580 shell run-as com.sriniketh.prose cat cache/dune_export.json`; it contains `"id": "B1hSG45JCX4C"`, `info.title` `Dune`, and a `highlights` array with `verify-h1` and the injected text. Then `U back` returns to the app.
- **highlights-delete.** With one injected highlight listed: `U tap "Options Menu"`, `U wait "Delete"`; the menu lists `Copy`, `Edit`, `Delete`. `U tap "Delete"` opens a dialog titled `Delete highlight` with the text `Are you sure you want to remove this highlight from your device?` and buttons `Cancel` and `Delete`. Capture `U shot delete-dialog`.
  - Cancel: `U tap "Cancel"`, `U gone "Delete highlight"`. The highlight is still listed and `db.sh "select count(*) from HighlightEntity;"` prints `1`.
  - Confirm: open the menu and `Delete` again, then tap the dialog's `Delete` button (in the proof run this was `U tap-xy 779 1367`, because `U tap "Delete"` also matches the dialog title `Delete highlight` by substring). `U wait "No saved highlights"` succeeds, the count prints `0`, and the `Dune` book row is still there.

## Gotchas

- Force-stop the app before injecting, or Room can keep serving its cached view and the row never appears.
- The file name is the lower-cased title with underscores plus `_export.json`; a title with spaces becomes `the_odyssey_export.json`.
- The exported JSON omits null fields (`explicitNulls = false`), and `description` carries raw HTML from the API.
- The share sheet is a system activity: nothing of Prose is visible while it is up. Press back to return.
- `Add highlight` asks for the CAMERA permission first. On a fresh install a permission dialog appears; denying it shows `Please allow access to take picture to scan text for highlights.`
