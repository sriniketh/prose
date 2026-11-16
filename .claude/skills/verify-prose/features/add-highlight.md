# Add or edit a highlight

A user captures a page photo, crops to the passage, lets on-device OCR read it, edits the text, and saves it as a highlight; an existing highlight can be reopened, edited, and saved over.

## Sub-features

- `capture` takes a photo for the book after the camera permission is granted.
- `crop` crops the photo and continues with the `Select` or done-cropping control.
- `ocr` turns the crop into editable text.
- `save` stores the highlight and returns to the book's highlights.
- `edit` reopens an existing highlight with its text, saves changes, and keeps the original saved date.
- `permission-denied` shows `Please allow access to take picture to scan text for highlights.` when the camera permission is refused.

## How to get to it (user POV)

- From a book's highlights screen, choose `Add highlight`.
- For `edit`, open a highlight's `Options Menu` and choose `Edit`.

## Driving it with ui.sh

Preconditions:

- `scripts/launch.sh` has printed `ready:` and `scripts/doctor.sh` passes.
- `Dune` is on the shelf with one injected highlight (see `view-highlights.md`).

- **permission-denied.** `U tap "Dune"`, `U tap "Add highlight"`. When the system dialog appears, `U tap "Don't allow"`. Expect the permission snackbar text; the app stays on `Saved highlights`.
- **edit.** `U tap "Options Menu"`, `U tap "Edit"`, `U wait "Edit highlight"`. The keyboard opens with the field focused and holding the injected text. Replace it with `adb -s emulator-5580 shell input keycombination 113 29` (select all) followed by `U type "Fear is the little death."`, then `U hide-keyboard` so the `Save` button is visible, and `U tap "Save"`. Back on `Saved highlights`, `U wait "Fear is the little death."` succeeds and the old text is gone. `db.sh "select id, text, savedOnTimestamp from HighlightEntity;"` prints the same id (`verify-h1`), the new text, and the original timestamp, and the row count is still `1`.
- **capture, crop, ocr, save: unreachable here.** OCR cannot run on any emulator on this Mac: the API 34 image cannot download the ML Kit module, and an API 36 Play image crashes with `SIGILL` inside the TFLite module. Run these on a real device and report them as skipped with that reason. Do not substitute an injected row for the `save` result.

## Gotchas

- Granting the camera permission opens the camera screen, whose emulator virtual scene is not a book page; OCR output from it is meaningless even on a device that can run OCR.
- Crop and OCR hand a temp image through the cache dir; an abandoned capture deletes it, a completed one is deleted after OCR.
- Rotating the device recreates the Activity and reloads the crop screen, which is how a swapped image gets picked up. Restore `user_rotation` to 0 afterwards.
- Editing keeps the row id and the original `savedOnTimestamp`; a test that sees the timestamp change has found a bug.
