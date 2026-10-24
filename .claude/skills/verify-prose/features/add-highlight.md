# Add or edit a highlight

A user photographs a page, on-device OCR draws each recognized line over the photo, the user taps a paragraph or swipes across lines to pick the passage, edits the text, and saves it as a highlight; an existing highlight can be reopened, edited, and saved over.

## Sub-features

- `capture` takes a photo for the book after the camera permission is granted.
- `no-text` shows `No text found. Retake the photo with the page in focus.` and a `Retake` button for a photo without text.
- `select-tap` toggles a whole paragraph; the bottom bar previews the selected text and enables `Continue`.
- `select-swipe` selects every line between the start and the finger; dragging back shrinks the range, and a swipe that starts on a selected line deselects.
- `retake` reopens the camera; cancelling it re-reads the existing photo.
- `save` opens `Save highlight` with the selected text, stores it, and returns to the book's highlights. Back from `Save highlight` returns to the same selection.
- `edit` reopens an existing highlight with its text, saves changes, and keeps the original saved date.
- `permission-denied` shows `Please allow access to take picture to scan text for highlights.` when the camera permission is refused.

## How to get to it (user POV)

- From a book's highlights screen, choose `Add highlight`.
- For `edit`, open a highlight's `Options Menu` and choose `Edit`.

## Making OCR run on this Mac's emulators

The app ships the Play-services ML Kit model, which the API 34 image cannot download. For a local check only, swap in the bundled model, which runs on the API 34 arm64 emulator (proven 2026-10-19). Never commit the swap:

```bash
sed -i '' 's/^mlkit-text-recognition = .*/mlkit-text-recognition = { group = "com.google.mlkit", name = "text-recognition", version = "16.0.1" }/' gradle/libs.versions.toml
scripts/launch.sh
git checkout gradle/libs.versions.toml
```

The emulator camera photographs a virtual room, not a page. Generate a page photo (4000x3000 with EXIF rotation, so it also exercises the downsample and rotation path) with `python3 scripts/make-page.py /tmp/page.jpg` (needs Pillow, for example from a throwaway venv) and push it with `adb -s emulator-5580 push /tmp/page.jpg /data/local/tmp/page.jpg`.

## Driving it with ui.sh

Preconditions:

- `scripts/launch.sh` has printed `ready:` with the bundled model swapped in, and `scripts/doctor.sh` passes.
- A book is on the shelf (see `search-and-add-book.md`). For `edit`, inject one highlight (see `view-highlights.md`).

- **capture, no-text.** `U tap "Add highlight"`, allow the permission, press the shutter (`adb -s emulator-5580 shell input tap 540 2174`), then the check mark (`... input tap 540 2178`). Expect `No text found…` and `Retake`.
- **retake.** `U tap "Retake"`. While the camera is open, overwrite the photo: `F=$(adb -s emulator-5580 shell run-as com.sriniketh.prose ls -t cache | grep jpg | head -1 | tr -d '\r')`, then `adb -s emulator-5580 shell "cat /data/local/tmp/page.jpg | run-as com.sriniketh.prose sh -c 'cat > cache/$F'"` (keep the whole pipe inside one quoted `adb shell` string). Cancel the camera with `U back`. Expect `Select text` with one content-desc node per paragraph holding its recognized text.
- **select-tap.** Tap a paragraph node's centre from `U dump`. The bottom bar text changes to that paragraph.
- **select-swipe.** `adb -s emulator-5580 shell input swipe <x1> <y1> <x2> <y2> 800` across lines of two paragraphs. For a drag back, send `input motionevent DOWN/MOVE/UP` steps down and then up again. Read the bottom bar from `U dump` before and after.
- **save.** `U tap "Continue"`, `U wait "Save highlight"`, `U tap "Save"`, `U wait "Saved highlights"`. `db.sh "select text from HighlightEntity;"` prints the selected text, and `run-as … ls cache` no longer lists the photo.
- **permission-denied.** `U tap "Add highlight"`. When the system dialog appears, `U tap "Don't allow"`. Expect the permission snackbar text; the app stays on `Saved highlights`.
- **edit.** `U tap "Options Menu"`, `U tap "Edit"`, `U wait "Edit highlight"`. The keyboard opens with the field focused and holding the injected text. Replace it with `adb -s emulator-5580 shell input keycombination 113 29` (select all) followed by `U type "Fear is the little death."`, then `U hide-keyboard` so the `Save` button is visible, and `U tap "Save"`. Back on `Saved highlights`, `U wait "Fear is the little death."` succeeds and the old text is gone. `db.sh "select id, text, savedOnTimestamp from HighlightEntity;"` prints the same id (`verify-h1`), the new text, and the original timestamp, and the row count is still `1`.

## Gotchas

- The selection flow owns the temp photo in the cache dir and deletes it when the flow leaves the back stack. A process killed by `adb install -r` mid-flow leaves its photo behind; `pm clear` removes it.
- Sending the app home and running `am kill com.sriniketh.prose`, then relaunching, restores `Select text` by re-reading the saved photo; the selection itself starts empty again.
- Rotating the device (`settings put system accelerometer_rotation 0`, then `user_rotation 1`) keeps the selection. Restore `user_rotation` to 0 afterwards.
- Editing keeps the row id and the original `savedOnTimestamp`; a test that sees the timestamp change has found a bug.
