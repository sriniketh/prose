#!/usr/bin/env python3
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
DOCS_DIR = REPO_ROOT / "docs"

LINK_PATTERN = re.compile(r"\(\.\./[^)]+\)")
CODE_PATH_PATTERN = re.compile(r"`[a-zA-Z0-9_./-]+\.(?:kt|kts|toml|properties)`")


def resolve_elided(prefix: str, suffix: str) -> Path | None:
    base = REPO_ROOT / prefix
    if not base.exists():
        return None
    matches = list(base.glob(f"**/{suffix}"))
    return matches[0] if matches else None


def check_markdown_link(raw: str) -> Path | None:
    target = raw[1:-1]
    resolved = (DOCS_DIR / target).resolve()
    return resolved if resolved.exists() else None


def check_code_path(raw: str) -> tuple[Path | None, bool]:
    target = raw.strip("`")
    if "/" not in target:
        return None, True
    if "/.../" in target:
        prefix, suffix = target.split("/.../", 1)
        return resolve_elided(prefix, suffix), False
    direct = (REPO_ROOT / target).resolve()
    if direct.exists():
        return direct, False
    via_docs = (DOCS_DIR / target).resolve()
    if via_docs.exists():
        return via_docs, False
    return None, False


def main() -> int:
    if not DOCS_DIR.is_dir():
        print(f"No docs/ directory found at {DOCS_DIR}", file=sys.stderr)
        return 1

    dead_paths: list[str] = []
    md_files = sorted(DOCS_DIR.glob("*.md"))
    for md_file in md_files:
        lines = md_file.read_text(encoding="utf-8").splitlines()
        for lineno, line in enumerate(lines, start=1):
            for raw in LINK_PATTERN.findall(line):
                if check_markdown_link(raw) is None:
                    dead_paths.append(f"{md_file.relative_to(REPO_ROOT)}:{lineno}: dead link target {raw}")
            for match in re.finditer(CODE_PATH_PATTERN, line):
                raw = match.group(0)
                resolved, is_bare = check_code_path(raw)
                if is_bare:
                    continue
                if resolved is None:
                    dead_paths.append(f"{md_file.relative_to(REPO_ROOT)}:{lineno}: dead path reference {raw}")

    if dead_paths:
        print("Docs path audit found dead/unresolvable path references:\n", file=sys.stderr)
        for entry in dead_paths:
            print(f"  - {entry}", file=sys.stderr)
        print(
            "\nUpdate the referencing doc(s), or if the code moved intentionally, "
            "fix the path. See .claude/commands/audit-docs.md Pass 1.",
            file=sys.stderr,
        )
        return 1

    print(f"Docs path audit: checked {len(md_files)} file(s) under docs/, no dead paths found.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
