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
