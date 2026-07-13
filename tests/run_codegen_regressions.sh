#!/bin/bash

set -uo pipefail

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
CLASSES="$ROOT/compiler/build/classes"
RUNTIME="$ROOT/runtime/build/luca_vm_switch"
if [[ -f "$RUNTIME.exe" ]]; then
    RUNTIME="$RUNTIME.exe"
fi

if [[ ! -d "$CLASSES" || ! -x "$RUNTIME" ]]; then
    echo "Compiler or supported VM not built; run 'make build' first." >&2
    exit 2
fi

tmp_dir=$(mktemp -d 2>/dev/null || mktemp -d -t luca-codegen-tests)
trap 'rm -rf "$tmp_dir"' EXIT

passed=0
failed=0
for source in "$ROOT"/tests/codegen/*.luc; do
    name=$(basename "${source%.luc}")
    expected="${source%.luc}.out"
    program="$tmp_dir/$name.vm"
    actual="$tmp_dir/$name.out"
    normalized_expected="$tmp_dir/$name.expected"
    normalized_actual="$tmp_dir/$name.actual"
    status=0

    java -cp "$CLASSES" cli.Lucac compile "$source" "$program" >"$tmp_dir/compiler.out" 2>&1 || status=$?
    if [[ $status -eq 0 ]]; then
        "$RUNTIME" "$program" >"$actual" 2>&1 || status=$?
    fi
    sed 's/\r$//' "$expected" >"$normalized_expected"
    if [[ -f "$actual" ]]; then
        sed 's/\r$//' "$actual" >"$normalized_actual"
    else
        sed 's/\r$//' "$tmp_dir/compiler.out" >"$normalized_actual"
    fi

    if [[ $status -eq 0 ]] && diff -u "$normalized_expected" "$normalized_actual" >/dev/null; then
        passed=$((passed + 1))
    else
        echo "" >&2
        echo "--- tests/codegen/$name.luc ---" >&2
        if [[ $status -ne 0 ]]; then
            echo "process exited with status $status" >&2
        fi
        diff -u --label "tests/codegen/$name.out" --label "actual output" \
            "$normalized_expected" "$normalized_actual" >&2 || true
        failed=$((failed + 1))
    fi
done

echo "codegen: $passed/$((passed + failed)) passed"
[[ $failed -eq 0 ]]
