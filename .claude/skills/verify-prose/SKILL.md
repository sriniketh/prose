---
name: verify-prose
description: Drive the Prose Android app (com.sriniketh.prose) on a headless Pixel_8 emulator the way a user does (search books, add to shelf, view/export highlights) and capture screenshots, UI trees, DB rows and exported files as proof. Use when a change needs to be shown working in the real app, not just in unit or screenshot tests.
---

# Verify Prose

Prose is a single-Activity Compose app. The user surface is touch UI on an Android device. Everything here runs through adb against a throwaway emulator, using scripts in `scripts/` (run them as `.claude/skills/verify-prose/scripts/<name>.sh` from the repo root; they are bash, do not `source` them from zsh).

Read `features/README.md` first, then the feature file that matches the change.

## Launch

```bash
.claude/skills/verify-prose/scripts/launch.sh
```

This boots the `Pixel_8` AVD (API 34) headless with `-read-only -no-snapshot` on port 5580 (serial `emulator-5580`, so it never collides with the user's emulators on 5554/5556), builds `:app:assembleDebug`, installs it, runs `pm clear` for an empty bookshelf, turns the system animation scales off, and launches `MainActivity`. It is ready when it prints `ready: emulator-5580 ...`. The read-only AVD discards all emulator state on shutdown.

It refuses to continue when:
- `core-network/apikey.properties` is missing or empty. Copy it from the main checkout.
- `emulator-5580` is up but this skill did not start it.
- AVD `Pixel_8` is already running elsewhere (a read-only second instance conflicts with it).
- The device is API 36 or newer. Espresso breaks there, and OCR cannot run on API 36 images on this Mac either.

Never use or `pm clear` the user's `Medium_Phone_API_36.1` emulator, which often holds their own Prose data.

## Doctor

```bash
.claude/skills/verify-prose/scripts/doctor.sh
```

Read-only. Checks the API key file, that `emulator-5580` is the Pixel_8 AVD we started, API level, the package is installed and not older than the built APK, the device can reach `www.googleapis.com`, and Prose is in the foreground. Run it first whenever anything looks off. Any `FAIL` line means re-run `launch.sh`.

## Drive

`scripts/ui.sh` wraps `android layout` (JSON of every node with its `center`) and `adb input`. Handles are visible text or `content-desc`, never coordinates.

| Command | Use |
| --- | --- |
| `ui.sh dump` | `[x,y]<TAB>text<TAB>content-desc` for every labelled node. Run it to see what is on screen. |
| `ui.sh wait "<needle>" [secs]` | Poll until a node whose text or content-desc equals (else contains) the needle exists. |
| `ui.sh gone "<needle>" [secs]` | Poll until it disappears. |
| `ui.sh has "<needle>"` | Exit 0 if present. |
| `ui.sh tap "<needle>"` | Tap its centre. Exact matches win over substring matches. |
| `ui.sh type "<text>"` | Type into the focused field. |
| `ui.sh hide-keyboard` | Close the soft keyboard only if it is showing. |
| `ui.sh back` | Back key. |
| `ui.sh shot <name>` | Screenshot to `$EVIDENCE_DIR/<name>.png`. |
| `ui.sh tree <name>` | Layout JSON to `$EVIDENCE_DIR/<name>.json`. |
| `db.sh "<sql>"` | Run SQL against the app's Room DB `databases/book-db` via `run-as` (debug build). |

Stable handles in the app: bookshelf FAB `search for a book button`; search field `Search for a book`; book-info button `Add to shelf`; view-highlights `Share highlights` and `Add highlight`. Strings live in each module's `src/main/res/values/strings.xml`.

Flow-by-flow behaviour is in `docs/flows.md`; the per-feature recipes are in `features/`.

Rules that keep drives honest:
- Call `ui.sh hide-keyboard` after typing a query and before tapping a result. The keyboard's suggestion strip can contain the same word as a result and steals the tap.
- Wait on a node with `ui.sh wait`, never a fixed sleep. Network results arrive from the real Google Books API.
- A tap can silently miss. Confirm the next screen with `ui.sh wait` or `dumpsys activity activities | grep topResumedActivity`.
- Rebuild and reinstall to test a code change: `launch.sh` does both. `adb install -r` alone preserves DB data.

## Evidence

Evidence goes to `$PROSE_VERIFY_OUT/evidence` (default `/tmp/prose-verify/evidence`) and survives cleanup.

A proof contains:
- The action and the resulting state: a screenshot before and after, or a `ui.sh dump` before and after, not only the final screen.
- A side-effect check next to what is visible: the Room row via `db.sh`, or the exported file via `run-as ... cat cache/<title>_export.json`.
- The real user path. Do not call internal setters or write rows for a flow the UI can perform. The sanctioned shortcuts are injecting a highlight row (see `features/view-highlights.md`) and swapping a generated page into the captured photo (see `features/add-highlight.md`), because the emulator camera cannot photograph a real page.

Name artifacts `<feature-id>-<step>` so a reader can tell which sub-feature each one proves, and report any sub-feature you could not reach with the command you attempted and the unmet precondition. Do not report it as verified through another entry point.

## Cleanup

```bash
.claude/skills/verify-prose/scripts/cleanup.sh
```

Runs `pm clear`, then `emu kill` on `emulator-5580`, but only if `launch.sh` started it (marker file); otherwise it stops nothing. It signals the recorded emulator pid only when `emu kill` did not stop it, never by process name. It removes `$STATE_DIR` and leaves the evidence directory, which it lists. Delete the evidence directory yourself once the proof has been reported or copied somewhere durable.

Run cleanup after every failed attempt too.

## Not reachable here

- OCR with the shipped Play-services model: the API 34 image cannot download it, and API 36 crashes with `SIGILL`. Swap in the bundled model locally to drive OCR on API 34 (see `features/add-highlight.md`).
- The `Medium_Phone_API_36.1` image: Espresso-dependent tests fail there, and it is the user's own.
