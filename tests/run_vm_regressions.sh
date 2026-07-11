#!/bin/bash

set -uo pipefail

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
RUNTIME="$ROOT/runtime/build"
EXE=
if [[ -f "$RUNTIME/luca_vm_switch.exe" ]]; then
    EXE=.exe
fi

switch_vm="$RUNTIME/luca_vm_switch$EXE"
indirect_vm="$RUNTIME/luca_vm_indirect$EXE"
if [[ ! -x "$switch_vm" || ! -x "$indirect_vm" ]]; then
    echo "VM executables not found; run 'make build' first." >&2
    exit 2
fi

tmp_dir=$(mktemp -d 2>/dev/null || mktemp -d -t luca-vm-tests)
trap 'rm -rf "$tmp_dir"' EXIT

passed=0
failed=0

run_case() {
    local name=$1 runtime=$2 program=$3 expected=$4
    local actual="$tmp_dir/actual"
    local normalized_expected="$tmp_dir/expected"
    local normalized_actual="$tmp_dir/actual-normalized"
    local status=0

    "$runtime" "$program" >"$actual" 2>&1 || status=$?
    sed 's/\r$//' "$expected" >"$normalized_expected"
    sed 's/\r$//' "$actual" >"$normalized_actual"

    if [[ $status -eq 0 ]] && diff -u "$normalized_expected" "$normalized_actual" >/dev/null; then
        passed=$((passed + 1))
        return
    fi

    echo "" >&2
    echo "--- $name ---" >&2
    if [[ $status -ne 0 ]]; then
        echo "process exited with status $status" >&2
    fi
    diff -u --label "${expected#"$ROOT/"}" --label "actual output" \
        "$normalized_expected" "$normalized_actual" >&2 || true
    failed=$((failed + 1))
}

run_case "switch arithmetic" "$switch_vm" \
    "$ROOT/tests/runtime/arithmetic.vm" "$ROOT/tests/runtime/arithmetic.switch.out"
run_case "indirect arithmetic" "$indirect_vm" \
    "$ROOT/tests/runtime/arithmetic.vm" "$ROOT/tests/runtime/arithmetic.indirect.out"
run_case "switch procedure frames" "$switch_vm" \
    "$ROOT/tests/runtime/procedure.vm" "$ROOT/tests/runtime/procedure.switch.out"

echo "runtime: $passed/$((passed + failed)) passed"
[[ $failed -eq 0 ]]
