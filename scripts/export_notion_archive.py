#!/usr/bin/env python3
"""Build a Notion-importable Markdown archive of the handwritten lab sources.

The archive inlines text sources and indexes every included file with a sha256.
It records the git commit passed on the command line. It skips SDK, caches,
build outputs, and local.properties.
"""
from __future__ import annotations

import argparse
import hashlib
import subprocess
from pathlib import Path

ROOT_SKIP_DIRS = {
    ".git",
    ".gradle",
    "build",
    ".idea",
    "notion-import",
}
SKIP_NAMES = {"local.properties"}
TEXT_SUFFIXES = {
    ".kt",
    ".kts",
    ".md",
    ".xml",
    ".properties",
    ".toml",
    ".yml",
    ".yaml",
    ".py",
    ".sh",
    ".txt",
    ".json",
    ".csv",
    ".svg",
    ".html",
    ".gitignore",
}


def git_ls(commit: str) -> list[str]:
    out = subprocess.check_output(["git", "ls-tree", "-r", "--name-only", commit], text=True)
    return [line for line in out.splitlines() if line]


def include(path: str) -> bool:
    parts = Path(path).parts
    if not parts:
        return False
    if parts[0] in ROOT_SKIP_DIRS:
        return False
    if any(part in {".gradle", "build"} for part in parts):
        return False
    if Path(path).name in SKIP_NAMES:
        return False
    return True


def bucket(path: str) -> str:
    top = Path(path).parts[0]
    if top in {"app", "checks", "legacy", "legacy-regression"}:
        return top
    if top in {"docs", "experiment", "scripts", "third_party"}:
        return top
    return "config-ci"


def fence(text: str) -> str:
    tick = "```"
    while tick in text:
        tick += "`"
    return tick


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--commit", required=True)
    parser.add_argument("--out", type=Path, required=True)
    args = parser.parse_args()
    commit = args.commit
    files = [path for path in git_ls(commit) if include(path)]
    out = args.out
    out.mkdir(parents=True, exist_ok=True)
    groups: dict[str, list[str]] = {}
    index_lines = ["# 源码索引", "", f"源码快照 commit：`{commit}`", "", "排除：SDK、`.gradle/`、`build/`、`local.properties`、密钥。", "", "| 文件 | sha256 | 字节 | 行 |", "| --- | --- | ---: | ---: |"]
    for path in files:
        raw = subprocess.check_output(["git", "show", f"{commit}:{path}"])
        digest = hashlib.sha256(raw).hexdigest()
        text = None
        suffix = Path(path).suffix
        is_text = suffix in TEXT_SUFFIXES or Path(path).name in {"gradlew", "README", "LICENSE", "NOTICE"} or path.endswith("IssueRegistry")
        if is_text:
            try:
                text = raw.decode("utf-8")
            except UnicodeDecodeError:
                text = None
        line_count = text.count("\n") + (0 if text is None or text.endswith("\n") or text == "" else 1) if text is not None else ""
        index_lines.append(f"| `{path}` | `{digest}` | {len(raw)} | {line_count} |")
        if text is None:
            continue
        groups.setdefault(bucket(path), []).append((path, text, digest))

    (out / "00-源码索引.md").write_text("\n".join(index_lines) + "\n", encoding="utf-8")
    order = ["docs", "experiment", "app", "checks", "legacy", "legacy-regression", "scripts", "third_party", "config-ci"]
    for name in order:
        entries = groups.get(name, [])
        parts = [
            f"# {name}",
            "",
            f"源码快照 commit：`{commit}`",
            "",
            "以下是该目录在该 commit 的完整文本，不是片段。二进制文件只出现在索引里。",
            "",
        ]
        for path, text, digest in entries:
            parts.append(f"## `{path}`")
            parts.append("")
            parts.append(f"sha256 `{digest}`")
            parts.append("")
            bar = fence(text)
            lang = Path(path).suffix.lstrip(".") or "text"
            parts.append(f"{bar}{lang}")
            parts.append(text.rstrip("\n"))
            parts.append(bar)
            parts.append("")
        (out / f"10-{name}.md").write_text("\n".join(parts), encoding="utf-8")
    (out / "SNAPSHOT.txt").write_text(commit + "\n", encoding="utf-8")
    print(f"wrote {out} files={len(files)} commit={commit}")


if __name__ == "__main__":
    main()
