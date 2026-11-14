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
