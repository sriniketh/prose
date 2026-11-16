# Bookshelf

The bookshelf is the start screen. It lists saved books as cards, shows an empty-state message when there are none, and opens a book's highlights when a card is chosen.

## Sub-features

- `shelf-empty` shows `Go ahead, grab a book!` with no cards.
- `shelf-list` lists every saved book with title and author, updating when books are added.
- `shelf-open` opens the saved-highlights screen for the chosen book.

## How to get to it (user POV)

- Launch the app. The bookshelf is the first screen.
- Return to it from search or book info by adding a book to the shelf, or by pressing back.

## Driving it with ui.sh

Preconditions:

- `scripts/launch.sh` has printed `ready:` and `scripts/doctor.sh` passes.
- For `shelf-list` and `shelf-open`, at least one book has been added through `search-and-add-book.md`.

- **shelf-empty.** Right after launch, `U wait "Go ahead, grab a book!"`. Capture `U shot shelf-empty`. `db.sh "select count(*) from BookEntity;"` prints `0`.
- **shelf-list.** After adding `Dune`, `U has "Dune"` and `U has "Frank Herbert"` succeed on the bookshelf. `db.sh "select count(*) from BookEntity;"` equals the number of cards.
- **shelf-open.** `U tap "Dune"`, then `U wait "Saved highlights"`. The screen shows the title area plus the `Share highlights` and `Add highlight` controls. With no highlights it shows `No saved highlights`.

## Gotchas

- The bookshelf reads Room as a stream, so a book inserted through the database while the app is open can appear without a relaunch, but a highlight row injected the same way is only picked up after `am force-stop` and a relaunch. Always force-stop before injecting.
- `pm clear` returns the app to the empty state, but the first launch afterwards can take a few seconds. Wait on a node.
- The `Bookshelf` heading is also a text node on other screens' back stacks; wait on a card title or the empty-state text to know which state you are in.
