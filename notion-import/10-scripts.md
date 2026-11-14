# scripts

源码快照 commit：`9441209aadafb1f33c3694643eed5880fa715177`

以下是该目录在该 commit 的完整文本，不是片段。二进制文件只出现在索引里。

## `scripts/chart_detector_results.py`

sha256 `f9fdb783173472b46d2992d7232ced0ef0686fa8ef7c2a8829038879307fd026`

```py
#!/usr/bin/env python3
"""Bar chart of JUnit gold-corpus results. Refuses to draw if the XML is missing.

Numbers are read from the test report. Nothing is hardcoded.
The chart is detector validation, not a model A/B comparison.
"""
from __future__ import annotations

import argparse
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def load(paths: list[Path]) -> list[dict]:
    rows = []
    for path in paths:
        root = ET.parse(path).getroot()
        suites = [root] if root.tag == "testsuite" else list(root.findall("testsuite"))
        for suite in suites:
            name = suite.attrib.get("name", path.stem).split(".")[-1]
            rows.append(
                {
                    "name": name,
                    "tests": int(suite.attrib.get("tests", 0)),
                    "failures": int(suite.attrib.get("failures", 0)),
                    "errors": int(suite.attrib.get("errors", 0)),
                    "skipped": int(suite.attrib.get("skipped", 0)),
                }
            )
    return rows


def svg(rows: list[dict]) -> str:
    width = 760
    bar_h = 28
    gap = 16
    left = 280
    top = 56
    height = top + len(rows) * (bar_h + gap) + 40
    max_tests = max((row["tests"] for row in rows), default=1) or 1
    usable = width - left - 40
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
        '<rect width="100%" height="100%" fill="#f7f4ee"/>',
        '<text x="24" y="32" font-family="sans-serif" font-size="18" fill="#1c1915">检测器金标准用例（JUnit，不是模型通过率）</text>',
    ]
    for index, row in enumerate(rows):
        y = top + index * (bar_h + gap)
        passed = row["tests"] - row["failures"] - row["errors"] - row["skipped"]
        scale = usable / max_tests
        parts.append(
            f'<text x="16" y="{y + 20}" font-family="sans-serif" font-size="13" fill="#1c1915">{row["name"]}</text>'
        )
        parts.append(
            f'<rect x="{left}" y="{y}" width="{max(passed, 0) * scale:.1f}" height="{bar_h}" fill="#2f6f4e"/>'
        )
        if row["failures"] or row["errors"]:
            bad = row["failures"] + row["errors"]
            parts.append(
                f'<rect x="{left + passed * scale:.1f}" y="{y}" width="{bad * scale:.1f}" height="{bar_h}" fill="#8c2f2f"/>'
            )
        parts.append(
            f'<text x="{left + 8}" y="{y + 19}" font-family="sans-serif" font-size="12" fill="#f7f4ee">{passed} passed / {row["tests"]} tests</text>'
        )
    parts.append("</svg>")
    return "\n".join(parts)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("xml", nargs="+", type=Path)
    parser.add_argument("--out", type=Path, required=True)
    args = parser.parse_args()
    missing = [str(path) for path in args.xml if not path.exists()]
    if missing:
        print("missing junit xml: " + ", ".join(missing), file=sys.stderr)
        return 2
    rows = load(args.xml)
    if not rows:
        print("no testsuites", file=sys.stderr)
        return 2
    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(svg(rows), encoding="utf-8")
    csv = args.out.with_suffix(".csv")
    lines = ["suite,tests,failures,errors,skipped"]
    for row in rows:
        lines.append(f'{row["name"]},{row["tests"]},{row["failures"]},{row["errors"]},{row["skipped"]}')
    csv.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(args.out)
    print(csv)
    return 0


if __name__ == "__main__":
    sys.exit(main())
```

## `scripts/export_notion_archive.py`

sha256 `1526f76b9f094d041dcbc3c6ea93ef37ac94edd730e8c3a511a384b456c1da0a`

````py
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
````

## `scripts/score_run.py`

sha256 `f9263e9b7319d199feccd2da23885635159361343c432076cbefe83c77cdf0f6`

```py
#!/usr/bin/env python3
"""Score a lint XML plus an optional meta file.

This is a parser for later coordinated runs. It does not call a model.
sample_kind comes from the meta file. If meta is missing, the kind is unknown
and must not be reported as a model A/B result.
"""
from __future__ import annotations

import argparse
import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

TRACKED = [
    "AdaptiveScreenOrientationLock",
    "AdaptiveConfigChangesOptOut",
    "UnsaveableScrollRemember",
    "AdaptivePaneScaffoldBanned",
    "MissingEnableEdgeToEdge",
]


def lint_counts(path: Path) -> dict:
    root = ET.parse(path).getroot()
    counts = {issue: 0 for issue in TRACKED}
    other = 0
    severities: dict[str, int] = {}
    for issue in root.findall("issue"):
        issue_id = issue.attrib.get("id", "")
        severity = issue.attrib.get("severity", "")
        severities[severity] = severities.get(severity, 0) + 1
        if issue_id in counts:
            counts[issue_id] += 1
        else:
            other += 1
    return {
        "lint_xml": str(path),
        "tracked_issue_counts": counts,
        "tracked_total": sum(counts.values()),
        "other_issue_count": other,
        "severity_counts": severities,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--lint-xml", type=Path, required=True)
    parser.add_argument("--meta", type=Path)
    parser.add_argument("--out", type=Path, required=True)
    args = parser.parse_args()
    meta = {}
    if args.meta and args.meta.exists():
        meta = json.loads(args.meta.read_text(encoding="utf-8"))
    kind = meta.get("sample_kind", "unknown")
    report = {
        "sample_kind": kind,
        "is_model_ab": kind in {"model_arm_a", "model_arm_b", "model_arm_a_self_review"},
        "counts": lint_counts(args.lint_xml),
        "meta": meta,
        "limit": "Functional, visual, and security results are not inferred from lint XML.",
    }
    if kind == "unknown":
        report["warning"] = "sample_kind missing; do not cite this file as a model result"
    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(args.out)
    return 0


if __name__ == "__main__":
    sys.exit(main())
```

## `scripts/verify-legacy-gate.sh`

sha256 `293130cd5d238c57d8577c6f2aaf8b57536eefa475cb8efd31f6465b9cd70237`

```sh
#!/usr/bin/env bash
# Proves two gates with process exit codes:
#   legacy lint (historical debt baselined) exits 0
#   legacy-regression lint (new debt, no baseline) exits non-zero
#   removing the legacy baseline makes the same historical module fail
#   a temporary new violation in the legacy module fails, then the file is removed
# Does not update the baseline. Restores the tree on exit.
set -u
cd "$(dirname "$0")/.."
mkdir -p evidence
OUT=evidence/legacy-gate.json
LOG=evidence/legacy-gate.log
BASELINE=legacy/lint-baseline.xml
TMP=legacy/src/main/java/com/example/movielab/legacy/TempNewDebt.kt
: > "$LOG"

cleanup() {
  if [[ -f "${BASELINE}.gatebak" ]]; then
    mv "${BASELINE}.gatebak" "$BASELINE"
  fi
  rm -f "$TMP"
}
trap cleanup EXIT

run() {
  local name="$1"
  shift
  local start end code
  start=$(date +%s)
  set +e
  "$@" >>"$LOG" 2>&1
  code=$?
  set -e
  end=$(date +%s)
  printf '%s\n' "$name exit=$code seconds=$((end - start))" | tee -a "$LOG" >&2
  echo "$code"
}

if [[ ! -f "$BASELINE" ]]; then
  echo "missing $BASELINE; generate it with ./gradlew :legacy:updateLintBaseline before this gate" | tee -a "$LOG"
  exit 2
fi

set -e
pos=$(run legacy_with_baseline ./gradlew :legacy:lintDebug --console=plain)
# offline may fail if cache cold; retry online when the first attempt is not 0 for infrastructure
if [[ "$pos" -ne 0 ]]; then
  echo "retry legacy_with_baseline online" >>"$LOG"
  pos=$(run legacy_with_baseline_online ./gradlew :legacy:lintDebug --console=plain)
fi
neg=$(run legacy_regression ./gradlew :legacy-regression:lintDebug --console=plain)

mv "$BASELINE" "${BASELINE}.gatebak"
nobase=$(run legacy_without_baseline ./gradlew :legacy:lintDebug --console=plain)
mv "${BASELINE}.gatebak" "$BASELINE"

cat > "$TMP" <<'EOF'
package com.example.movielab.legacy

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun TempNewDebt() {
    val state = remember { ScrollState(0) }
    androidx.compose.material3.Text("TEMP_NEW_DEBT ${state.value}")
}
EOF
fresh=$(run legacy_with_temp_new_debt ./gradlew :legacy:lintDebug --console=plain)
rm -f "$TMP"
after=$(run legacy_restored ./gradlew :legacy:lintDebug --console=plain)

python3 - "$OUT" "$pos" "$neg" "$nobase" "$fresh" "$after" <<'PY'
import json, sys
out, pos, neg, nobase, fresh, after = sys.argv[1:]
def code(v):
    return int(v)
report = {
    "sample_kind": "hand_constructed_baseline_gate",
    "not_a_model_ab": True,
    "commands": [
        {"name": "legacy_with_baseline", "exit_code": code(pos), "expect": 0},
        {"name": "legacy_regression", "exit_code": code(neg), "expect": "nonzero"},
        {"name": "legacy_without_baseline", "exit_code": code(nobase), "expect": "nonzero"},
        {"name": "legacy_with_temp_new_debt", "exit_code": code(fresh), "expect": "nonzero"},
        {"name": "legacy_restored", "exit_code": code(after), "expect": 0},
    ],
}
ok = (
    code(pos) == 0
    and code(neg) != 0
    and code(nobase) != 0
    and code(fresh) != 0
    and code(after) == 0
)
report["gate_ok"] = ok
with open(out, "w", encoding="utf-8") as f:
    json.dump(report, f, ensure_ascii=False, indent=2)
    f.write("\n")
print(json.dumps(report, ensure_ascii=False, indent=2))
sys.exit(0 if ok else 1)
PY
```
