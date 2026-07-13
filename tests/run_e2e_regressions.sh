#!/bin/bash

set -uo pipefail

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)

tmp_dir=$(mktemp -d 2>/dev/null || mktemp -d -t luca-e2e-tests)
trap 'rm -rf "$tmp_dir"' EXIT

passed=0
failed=0
for source in "$ROOT"/tests/e2e/*.luc; do
    name=$(basename "${source%.luc}")
    expected="${source%.luc}.out"
    actual="$tmp_dir/$name.out"
    normalized_expected="$tmp_dir/$name.expected"
    normalized_actual="$tmp_dir/$name.actual"
    status=0

    sh "$ROOT/bin/lucac" run "$source" >"$actual" 2>&1 || status=$?
    sed 's/\r$//' "$expected" >"$normalized_expected"
    sed 's/\r$//' "$actual" >"$normalized_actual"

    if [[ $status -eq 0 ]] && diff -u "$normalized_expected" "$normalized_actual" >/dev/null; then
        passed=$((passed + 1))
    else
        echo "" >&2
        echo "--- tests/e2e/$name.luc ---" >&2
        if [[ $status -ne 0 ]]; then
            echo "process exited with status $status" >&2
        fi
        diff -u --label "tests/e2e/$name.out" --label "actual output" \
            "$normalized_expected" "$normalized_actual" >&2 || true
        failed=$((failed + 1))
    fi
done

echo "e2e: $passed/$((passed + failed)) passed"
[[ $failed -eq 0 ]]
