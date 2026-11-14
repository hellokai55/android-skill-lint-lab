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
