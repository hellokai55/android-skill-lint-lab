#!/usr/bin/env python3
"""Score one generated sample commit with the frozen detectors.

This does not call a model. It refuses known non-samples: the reference
implementation, the empty template, and any tree that still contains a
reference-app blob. Rules are not edited here.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ISSUE_IDS = [
    "AdaptiveScreenOrientationLock",
    "AdaptiveConfigChangesOptOut",
    "UnsaveableScrollRemember",
    "AdaptivePaneScaffoldBanned",
    "MissingEnableEdgeToEdge",
]

DETECTOR_HASHES = {
    "checks/src/main/java/com/example/movielab/lint/ManifestAdaptationDetector.kt":
        "6a021fcd3913b6dfb4566ad9bfd382254aa6e78f9a35654f6e826c697dc74f55",
    "checks/src/main/java/com/example/movielab/lint/UnsaveableScrollRememberDetector.kt":
        "5597ea1abac5026d47dcd54dce2e56c852f3ba4c485ddd69b94519c7254b7e50",
    "checks/src/main/java/com/example/movielab/lint/AdaptivePaneScaffoldDetector.kt":
        "b73aad2c22c0ed35de93d502079a4c20e3b66678a118402ce6c721fbfb5a8d80",
    "checks/src/main/java/com/example/movielab/lint/MissingEnableEdgeToEdgeDetector.kt":
        "9a6b6877f50bf25f6c7417f0fc02a62eb40e59d5829456bb2dda913d9ac14614",
    "checks/src/main/java/com/example/movielab/lint/MovieLabIssues.kt":
        "797e09eea5c50d4639b1cc0c11214e6a8febe2f9ee3f5122235be49f87076ccb",
    "checks/src/main/java/com/example/movielab/lint/MovieLabIssueRegistry.kt":
        "e68e711ec30f178a8fddd387943a19c352b76560be62454c3126bfc91729fda3",
}

EXCLUDED_COMMITS = {
    "fe85943416ab8da24fc08c844b05c00a0e377c3f",
    "9441209aadafb1f33c3694643eed5880fa715177",
    "85d704ee50ffbac15cfd8ad9b4e07cd369814659",
    "f7a580a50bdcc7a5d040a682107e3f8463a04560",
}

REFERENCE_BLOBS = {
    "app/src/main/java/com/example/movielab/data/Catalog.kt":
        "c17750622572eb77759380ba13125cad9e934d1492a0e0c7cb807f4704b42237",
    "app/src/main/java/com/example/movielab/ui/MovieApp.kt":
        "78b4bedbcb901cca0ab07f72cb0687a3268b0f9c16b2d5db9902332a69970f60",
    "app/src/main/java/com/example/movielab/MainActivity.kt":
        "a9662fd7aa462639c476c7e97765f360de2ec45b7971ec6b68fc9daeab121b8e",
    "app/src/main/java/com/example/movielab/ui/PlaceholderPoster.kt":
        "55e8ca5265d9df5b6d8ae848438988c439d9fdeaa102ff435ba8895a6247ab59",
    "app/src/main/java/com/example/movielab/ui/Theme.kt":
        "2dbc78a54dc5b30c9f774d5676f9e300afca09c1882fdff8faf409dc18e303a3",
}

ALLOWED_KINDS = {"model_arm_a", "model_arm_b", "model_arm_a_self_review"}
SCROLL_MARKERS = (
    "LazyColumn",
    "LazyRow",
    "LazyVerticalGrid",
    "LazyHorizontalGrid",
    "LazyVerticalStaggeredGrid",
    "verticalScroll",
    "horizontalScroll",
)
PANE_MARKERS = (
    "NavDisplay",
    "ListDetailSceneStrategy",
    "ListDetailPaneScaffold",
    "SupportingPaneScaffold",
)
ACTIVITY_SUPERTYPES = (
    "ComponentActivity",
    "AppCompatActivity",
    "FragmentActivity",
    "Activity",
)


def run(cmd: list[str], cwd: Path | None = None, check: bool = True) -> subprocess.CompletedProcess[str]:
    return subprocess.run(cmd, cwd=cwd, text=True, capture_output=True, check=check)


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    digest.update(path.read_bytes())
    return digest.hexdigest()


def git_out(repo: Path, *args: str) -> str:
    result = run(["git", *args], cwd=repo)
    return result.stdout.strip()


def verify_detectors(repo: Path) -> None:
    for relative, expected in DETECTOR_HASHES.items():
        path = repo / relative
        actual = sha256_file(path)
        if actual != expected:
            raise SystemExit(f"detector hash mismatch: {relative}\nexpected {expected}\nactual   {actual}")


def full_sha(repo: Path, sample: str) -> str:
    return git_out(repo, "rev-parse", sample)


def sample_file_bytes(repo: Path, sample: str, path: str) -> bytes | None:
    result = run(["git", "cat-file", "-e", f"{sample}:{path}"], cwd=repo, check=False)
    if result.returncode != 0:
        return None
    data = subprocess.run(
        ["git", "show", f"{sample}:{path}"],
        cwd=repo,
        capture_output=True,
        check=True,
    ).stdout
    return data


def reference_blob_hits(repo: Path, sample: str) -> list[str]:
    hits = []
    for path, expected in REFERENCE_BLOBS.items():
        data = sample_file_bytes(repo, sample, path)
        if data is not None and hashlib.sha256(data).hexdigest() == expected:
            hits.append(path)
    return hits


def kotlin_texts(root: Path) -> list[tuple[Path, str]]:
    files = []
    src = root / "app" / "src" / "main"
    if not src.exists():
        return files
    for path in src.rglob("*"):
        if path.suffix in {".kt", ".kts", ".java"} and path.is_file():
            files.append((path, path.read_text(encoding="utf-8", errors="replace")))
    return files


def manifest_text(root: Path) -> str:
    path = root / "app" / "src" / "main" / "AndroidManifest.xml"
    if not path.exists():
        return ""
    return path.read_text(encoding="utf-8", errors="replace")


def activity_elements(manifest: str) -> list[str]:
    return re.findall(r"<(activity|activity-alias)\b([^>]*)>", manifest, flags=re.I | re.S)


def concrete_activity_names(texts: list[tuple[Path, str]]) -> list[str]:
    names = []
    pattern = re.compile(
        r"(?m)^(?!.*\babstract\b)\s*(?:public\s+|internal\s+|open\s+|private\s+)*class\s+(\w+)\b[^{:\n]*:\s*([^{\n]+)"
    )
    for _, text in texts:
        for match in pattern.finditer(text):
            supers = match.group(2)
            if any(re.search(rf"\b{name}\b", supers) for name in ACTIVITY_SUPERTYPES):
                names.append(match.group(1))
    return names


def applicability(root: Path) -> dict:
    texts = kotlin_texts(root)
    joined = "\n".join(text for _, text in texts)
    manifest = manifest_text(root)
    elements = activity_elements(manifest)
    activities = concrete_activity_names(texts)
    scroll = any(marker in joined for marker in SCROLL_MARKERS)
    pane = any(marker in joined for marker in PANE_MARKERS)
    return {
        "AdaptiveScreenOrientationLock": {
            "applicable": len(elements) > 0,
            "denominator": len(elements),
            "unit": "activity_or_alias_in_main_manifest",
        },
        "AdaptiveConfigChangesOptOut": {
            "applicable": len(elements) > 0,
            "denominator": len(elements),
            "unit": "activity_or_alias_in_main_manifest",
        },
        "UnsaveableScrollRemember": {
            "applicable": scroll,
            "denominator": 1 if scroll else 0,
            "unit": "module_has_lazy_or_scroll_modifier",
        },
        "AdaptivePaneScaffoldBanned": {
            "applicable": pane,
            "denominator": 1 if pane else 0,
            "unit": "module_mentions_navdisplay_scene_or_banned_scaffold",
        },
        "MissingEnableEdgeToEdge": {
            "applicable": len(activities) > 0,
            "denominator": len(activities),
            "unit": "concrete_activity_class_regex",
            "names": activities,
        },
    }


def string_literals(blob: str) -> list[str]:
    return re.findall(r'"([^"\\]*(?:\\.[^"\\]*)*)"', blob)


def suppressions(root: Path) -> list[dict]:
    records = []
    annotation = re.compile(r"@(file:\s*)?Suppress(?:Lint)?\s*\((.*?)\)", re.S)
    for path, text in kotlin_texts(root):
        for match in annotation.finditer(text):
            ids = [item for item in string_literals(match.group(2)) if item in ISSUE_IDS]
            if not ids:
                continue
            line = text[: match.start()].count("\n") + 1
            tail = text[match.end() : match.end() + 400]
            kind = "file" if match.group(1) else "unknown"
            name = ""
            if kind != "file":
                found = re.search(r"\b(class|object|fun)\s+(\w+)", tail)
                if found:
                    kind = "function" if found.group(1) == "fun" else "class"
                    name = found.group(2)
            for issue_id in ids:
                records.append(
                    {
                        "issue_id": issue_id,
                        "path": str(path.relative_to(root)),
                        "line": line,
                        "declaration_kind": kind,
                        "declaration_name": name,
                    }
                )
    manifest = manifest_text(root)
    if manifest:
        for match in re.finditer(r"<(activity|activity-alias)\b([^>]*)>", manifest, flags=re.I | re.S):
            body = match.group(2)
            ignore = re.search(r"tools:ignore\s*=\s*\"([^\"]*)\"", body)
            if not ignore:
                continue
            ids = [item.strip() for item in ignore.group(1).split(",") if item.strip() in ISSUE_IDS]
            name_match = re.search(r"android:name\s*=\s*\"([^\"]+)\"", body)
            line = manifest[: match.start()].count("\n") + 1
            for issue_id in ids:
                records.append(
                    {
                        "issue_id": issue_id,
                        "path": "app/src/main/AndroidManifest.xml",
                        "line": line,
                        "declaration_kind": "manifest_element",
                        "declaration_name": name_match.group(1) if name_match else "",
                    }
                )
    return records


def lint_counts(xml_path: Path) -> dict[str, int]:
    counts = {issue_id: 0 for issue_id in ISSUE_IDS}
    if not xml_path.exists():
        return counts
    root = ET.parse(xml_path).getroot()
    for issue in root.findall("issue"):
        issue_id = issue.attrib.get("id", "")
        if issue_id in counts:
            counts[issue_id] += 1
    return counts


def rule_status(applicable: bool, unsuppressed: int, suppressed: int) -> str:
    if not applicable:
        return "not_applicable"
    if unsuppressed > 0:
        return "fail"
    if suppressed > 0:
        return "suppressed"
    return "pass"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--sample", required=True)
    parser.add_argument("--sample-kind", required=True)
    parser.add_argument("--out", type=Path)
    parser.add_argument("--repo", type=Path, default=Path(__file__).resolve().parents[1])
    args = parser.parse_args()
    repo = args.repo
    if args.sample_kind not in ALLOWED_KINDS:
        raise SystemExit(f"sample-kind must be one of {sorted(ALLOWED_KINDS)}")
    verify_detectors(repo)
    sample = full_sha(repo, args.sample)
    if sample in EXCLUDED_COMMITS or args.sample in EXCLUDED_COMMITS:
        raise SystemExit(f"refusing non-sample commit {sample}")
    hits = reference_blob_hits(repo, sample)
    if hits:
        raise SystemExit("refusing commit that still contains reference-app blobs: " + ", ".join(hits))
    listed = run(["git", "ls-tree", "-r", "--name-only", sample, "app/src"], cwd=repo, check=False)
    if listed.returncode != 0 or not listed.stdout.strip():
        raise SystemExit("sample commit has no app/src")

    work = Path(f"/tmp/score-{sample[:12]}")
    if work.exists():
        run(["git", "worktree", "remove", "--force", str(work)], cwd=repo, check=False)
        shutil.rmtree(work, ignore_errors=True)
    run(["git", "worktree", "add", "--detach", str(work), "HEAD"], cwd=repo)
    try:
        src = work / "app" / "src"
        shutil.rmtree(src)
        archive = subprocess.run(
            ["git", "archive", sample, "app/src"],
            cwd=repo,
            check=True,
            capture_output=True,
        )
        subprocess.run(["tar", "-x", "-C", str(work)], input=archive.stdout, check=True)
        catalog = git_out(repo, "rev-parse", "HEAD:gradle/libs.versions.toml")
        sample_catalog = run(
            ["git", "rev-parse", f"{sample}:gradle/libs.versions.toml"],
            cwd=repo,
            check=False,
        )
        toolchain_drift = sample_catalog.returncode != 0 or sample_catalog.stdout.strip() != catalog
        gradle = subprocess.run(
            ["./gradlew", ":app:assembleDebug", ":app:testDebugUnitTest", ":app:lintDebug", "--console=plain"],
            cwd=work,
        )
        lint_xml = work / "app" / "build" / "reports" / "lint-results-debug.xml"
        counts = lint_counts(lint_xml)
        suppressed = suppressions(work)
        applicable = applicability(work)
        rules = {}
        for issue_id in ISSUE_IDS:
            suppressed_n = sum(1 for record in suppressed if record["issue_id"] == issue_id)
            rules[issue_id] = {
                "applicability": applicable[issue_id],
                "unsuppressed_lint_count": counts[issue_id],
                "suppression_count": suppressed_n,
                "suppressions": [record for record in suppressed if record["issue_id"] == issue_id],
                "status": rule_status(applicable[issue_id]["applicable"], counts[issue_id], suppressed_n),
            }
        report = {
            "sample_commit": sample,
            "sample_kind": args.sample_kind,
            "is_model_sample": True,
            "reference_app_is_not_this_sample": True,
            "detector_hashes_verified": True,
            "toolchain_drift": toolchain_drift,
            "gradle_exit_code": gradle.returncode,
            "rules": rules,
            "functional_checklist": "not_run_by_this_script",
            "visual_checklist": "not_run_by_this_script",
        }
        text = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
        if args.out:
            args.out.parent.mkdir(parents=True, exist_ok=True)
            args.out.write_text(text, encoding="utf-8")
        sys.stdout.write(text)
        return 0 if gradle.returncode == 0 else 1
    finally:
        run(["git", "worktree", "remove", "--force", str(work)], cwd=repo, check=False)
        shutil.rmtree(work, ignore_errors=True)


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except subprocess.CalledProcessError as error:
        sys.stderr.write(error.stdout or "")
        sys.stderr.write(error.stderr or "")
        raise
