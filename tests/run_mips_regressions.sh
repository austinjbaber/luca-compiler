#!/bin/bash

set -uo pipefail

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
EXE=
if [[ -f "$ROOT/backend/mips/build/luca_mips.exe" ]]; then
    EXE=.exe
fi

translator="$ROOT/backend/mips/build/luca_mips$EXE"
if [[ ! -x "$translator" ]]; then
    echo "MIPS translator not found; run 'make build' first." >&2
    exit 2
fi

tmp_dir=$(mktemp -d 2>/dev/null || mktemp -d -t luca-mips-tests)
trap 'rm -rf "$tmp_dir"' EXIT

passed=0
failed=0

run_case() {
    local name=$1 expected=$2
    shift 2
    local actual="$tmp_dir/actual.s"
    local normalized_expected="$tmp_dir/expected"
    local normalized_actual="$tmp_dir/actual-normalized"
    local status=0

    "$translator" "$ROOT/tests/backend/mips.quad" "$actual" "$@" || status=$?
    sed 's/\r$//' "$expected" >"$normalized_expected"
    sed 's/\r$//' "$actual" >"$normalized_actual"

    if [[ $status -eq 0 ]] && diff -u "$normalized_expected" "$normalized_actual" >/dev/null; then
        passed=$((passed + 1))
        return
    fi

    echo "" >&2
    echo "--- $name ---" >&2
    if [[ $status -ne 0 ]]; then
        echo "translator exited with status $status" >&2
    fi
    diff -u --label "${expected#"$ROOT/"}" --label "actual output" \
        "$normalized_expected" "$normalized_actual" >&2 || true
    failed=$((failed + 1))
}

run_case "MIPS translation" "$ROOT/tests/backend/mips.s"
run_case "optimized MIPS translation" "$ROOT/tests/backend/mips.optimized.s" -O

echo "mips backend: $passed/$((passed + failed)) passed"
[[ $failed -eq 0 ]]
