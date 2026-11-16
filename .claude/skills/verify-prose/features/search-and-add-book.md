# Search and add a book

A user searches the Google Books catalogue as they type, opens a result to see its details, and adds it to the bookshelf, which returns them to the shelf with the book listed.

## Sub-features

- `search-open` opens the search screen from the bookshelf.
- `search-results` shows results with title and author after a query of at least 4 characters.
- `book-info` opens a result and shows title, author, publisher, description, and ratings.
- `add-to-shelf` saves the book and returns to the bookshelf with it listed.
- `add-persisted` stores the book row in the local database.
- `add-message` shows the snackbar `Book has been added to shelf!` on the bookshelf after adding. Currently failing, see Gotchas.

## How to get to it (user POV)

- Choose the search button (`search for a book button`) at the bottom right of the bookshelf.

## Driving it with ui.sh

Preconditions:

- `scripts/launch.sh` has printed `ready:` and `scripts/doctor.sh` passes.
- The bookshelf is empty (`Go ahead, grab a book!` is shown).
- All commands below are `.claude/skills/verify-prose/scripts/<name>` from the repo root; `U` means `ui.sh`.

- **search-open.** `U tap "search for a book button"`, then `U wait "Search for a book"`. The search screen shows the field `Search for a book`, a search icon, and a close icon.
- **search-results.** `U tap "Search for a book"`, `U type "dune"`, `U wait "Frank Herbert" 20`, `U hide-keyboard`. The list contains a `Dune` card with author `Frank Herbert`. Capture `U shot search-results-dune` and `U tree search-results-dune`.
- **book-info.** `U tap "Dune"`, `U wait "Add to shelf" 20`. The screen shows the title `Dune`, `Frank Herbert`, publisher `Penguin`, a description, and `Ratings: <n>/5.0 out of <m> reviews`. Capture `U shot book-info-dune`.
- **add-to-shelf.** `U tap "Add to shelf"`, then `U wait "Bookshelf"`. The bookshelf lists a `Dune` card with `Frank Herbert`. Capture `U shot bookshelf-after-add`.
- **add-persisted.** `db.sh "select id, title, authors from BookEntity;"` prints `B1hSG45JCX4C|Dune|Frank Herbert`. Confirm the row id matches the book the UI showed.
- **add-message.** Right after `U tap "Add to shelf"`, run `U wait "Book has been added to shelf!" 6`. It should succeed on the bookshelf. As of 2026-10-18 it times out; this sub-feature fails (see Gotchas). Do not weaken it to pass.
- **Second book.** Repeat from `search-open` with `hamlet` and wait for `William Shakespeare`; the bookshelf then lists both books and `select title from BookEntity order by title;` returns both.

## Gotchas

- The soft keyboard's suggestion strip can offer the typed word (for example `Emma`). Tapping the result by text then lands on the suggestion, which rewrites the field. Always run `U hide-keyboard` after typing.
- The search field contains the query (`dune`), so exact-match taps on the capitalized title hit the result, but a short lowercase substring might hit the field.
- Several results can share a title (three `Emma` cards). `U tap` picks the first exact match; assert on author as well.
- Search ignores queries of 3 characters or fewer and waits about 300 ms, so wait on a result node, not a sleep.
- Results and covers come from the live API and vary over time. Cover images may still be loading in screenshots.
- The `Book has been added to shelf!` snackbar is not shown. It is raised by `BookshelfViewModel` (`BOOKSHELF_SHOW_ADDED_MESSAGE` in its `SavedStateHandle`), which `ProseAppScreen.kt` sets in `onBookAddedToShelf` before popping to the bookshelf; `BookInfoScreen`'s own snackbar only carries the load and add error messages. Observed on 2026-10-18 (API 34, fresh install, animations both off and on): no snackbar in on-device screenshot bursts from about 0.2 s to 2 s after the tap, in 0.5 s-spaced captures, or in `ui.sh dump`, though a Short snackbar lasts 4 s. The book is added and listed correctly. `BookshelfViewModelTest` and `BookshelfScreenTest` cover the ViewModel and the screen with a hand-made `SavedStateHandle`; nothing covers the navigation wiring between them, which is where the break most likely is.
- To catch a transient UI element, do not run `ui.sh` after the tap (each layout call takes 1-2 s). Fetch the tap coordinates first, then do the tap and a burst of `screencap` inside one `adb shell`. `screenrecord` started before the tap ended at the tap in this environment and was not usable.
- Search results and book info need network. If `doctor.sh` fails the Books API check, nothing in this recipe can pass.
