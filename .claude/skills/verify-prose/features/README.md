# Prose verification map

This directory is the maintained source for verifying the user-facing behavior of Prose. Read the index before driving the app, then use the matching feature file as the recipe.

## Baseline preconditions

- Run `scripts/launch.sh`, then `scripts/doctor.sh`, and require `doctor: instance is worth driving`.
- The app starts from `pm clear`: empty bookshelf, empty database.
- The device has network access; search and book info call the real Google Books API.
- Never drive an emulator that this run did not start.

## Driving conventions

- Start every recipe from the baseline state unless its preconditions say otherwise.
- Prefer visible text and `content-desc` over coordinates. Use `scripts/ui.sh dump` to find them.
- Hide the soft keyboard before tapping a search result.
- Search needs a query of at least 4 characters and waits about 300 ms before the request goes out.
- Book titles and results come from the live API, so pick assertions on a specific title and author, not on result counts.
- Do not remove proof artifacts during cleanup.

## Proof and skip reporting

- Capture the user action and the resulting state, not only the final screen.
- UI proof includes a screenshot and a `ui.sh dump` of the end state.
- Persistence proof includes a `scripts/db.sh` query of the stored rows.
- Export proof includes the exported JSON read back from the app cache.
- Record the feature ID and entry point used with every artifact.
- Report an unreachable path with the attempted command and the unmet precondition.
- Do not report a skipped entry point as verified through a different path.

## Feature entry contract

Each feature file starts with an H1 title and one paragraph describing the user-visible behavior. It then uses exactly four H2 sections in this order: `Sub-features`, `How to get to it (user POV)`, `Driving it with ui.sh`, `Gotchas`.

## Features

Status of each recipe: `proven` means the commands below were run end to end against a fresh install on 2026-10-18.

- [Search and add a book](./search-and-add-book.md) covers search-as-you-type, book info, and adding to the shelf. Proven, except `add-message`, which fails: the "added" snackbar is not shown.
- [Bookshelf](./bookshelf.md) covers the saved-books list, empty state, and opening a book. Proven.
- [View and export highlights](./view-highlights.md) covers listing, share/export, and delete. Proven.
- [Add or edit a highlight](./add-highlight.md) covers capture, crop, OCR, save, and edit. `edit` is proven. Capture, crop, OCR, and save are not runnable on this Mac's emulators; `permission-denied` is written from source and not yet run.
