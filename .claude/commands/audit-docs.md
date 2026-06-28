---
description: Use when docs/ or AGENTS.md may have drifted from the code (after refactors, renames, module or dependency changes) to reconcile them against the source and report or fix drift
---

Audit the engineering docs under `docs/` **and the `AGENTS.md` quick reference** against the current state of the repo and report every place they have drifted. Do **not** change code to match the docs — the code is the source of truth; the docs follow it.

Work through these passes in order. For each finding, cite the doc file + line and the contradicting source file.

## 1. Path references (deterministic — do this first)

Every component in the docs is named by a concrete path. Extract every file path referenced in `docs/README.md`, `docs/architecture.md`, `docs/modules.md`, and `docs/flows.md` — both markdown links (`[`Foo`](../path/Foo.kt)`) and inline code paths — and verify each target exists. Run:

```bash
grep -oE '\(\.\./[^)]+\)|`[a-zA-Z0-9_./-]+\.(kt|kts|toml|properties)`' docs/*.md
```

then resolve each path (links are relative to `docs/`, so `../` is repo root) and flag any that no longer exist or have moved. Dead paths are the highest-priority finding.

The grep over-matches; do not report a hit as missing until you confirm it. Two forms are not literal paths: an elided segment like `app/.../MainActivity.kt` (the `.../` is an abbreviation — resolve it to the real file before judging) and a bare filename like `` `Navigation.kt` `` used inline (illustrative, not a link). Only a fully-spelled path that resolves nowhere is a dead path.

## 2. Module graph — `docs/modules.md`

- List actual Gradle modules from `settings.gradle.kts`. Compare against the per-module entries and the Mermaid + text dependency graph. Flag added/removed/renamed modules and any module missing an entry.
- For each module, read its `build.gradle.kts` `dependencies {}` block and verify the documented inter-module edges (`implementation(project(":core-data"))` etc.) match the graph. A feature module depending on anything other than `core-data` for data is a doc-worthy violation of the stated rule.
- Verify the use-case → repository → data-source index lists every `*UseCase` and `*Repository` actually present in `core-data`.

## 3. Architecture contract — `docs/architecture.md`

- Hilt DI map: confirm each documented `@Module` still exists with the stated bindings. Modules keep them in `di/` or `dagger/` packages — search both, not just `di/`.
- Navigation routes: compare the documented routes against `app/.../Navigation.kt` and `ProseAppScreen.kt`. Flag renamed/added/removed routes and changed arguments.
- UDF/`Result<T>` contract, effect `Channel`, and cross-cutting conventions: spot-check one ViewModel per feature to confirm the described shape still holds.

## 4. Flows — `docs/flows.md`

For each of the documented flows (bootstrap, bookshelf, search, add book, view highlights, capture→OCR→save, export/share), trace the described UI→data→UI path against the real code. Flag any step where the named class/function no longer exists or the path through the layers has changed (e.g. a UseCase inserted/removed, a different repository call).

## 5. Fast facts — `docs/README.md`

Verify min/target/compile SDK, JVM toolchain, and key-library claims against `gradle/libs.versions.toml` and the module `build.gradle.kts` files.

## 6. Agent quick reference — `AGENTS.md`

`AGENTS.md` carries a condensed "Architecture Overview" that duplicates parts of `docs/`, so it drifts the same way. Cross-check its concrete claims against the code, not against `docs/`:

- **Module Structure** list — every module present and described correctly (responsibilities, named entities/types).
- **Key Libraries** — the named stack (networking + JSON converter, image loading, cropping, logging, OCR) matches `gradle/libs.versions.toml`. The JSON library is the known drift point: confirm it against the actual `core-network` imports and converter dependency, not the prose.
- **Dependency Injection** — the named `@Module` classes exist and the stated DI-package convention matches reality.
- **Navigation** — the `Navigation.kt` path and any example route still hold.
- **Testing Patterns** — the named test libraries and dispatcher pattern are still what the suites use.
- **Build Commands / API Key Setup** — the Gradle invocations and `core-network/apikey.properties` key name are current.

Flag any claim that contradicts the code. Keep this section a true *summary* — if a fix would mean adding deep detail, correct the fact tersely and note that the depth belongs in `docs/`.

## Output

Produce a prioritized list: **(P1)** dead/moved paths, **(P2)** missing or wrong module/route/flow structure, **(P3)** stale fast facts or wording. For each, give the exact doc edit needed.

If invoked with `--fix`, apply the edits to the `docs/` files and `AGENTS.md` after presenting the list. Otherwise report only and let the user decide. Never invent paths — every corrected path must point at a file you confirmed exists.
