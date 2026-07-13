#!/bin/bash

set -uo pipefail

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
CLASSES="$ROOT/compiler/build/classes"

if [[ ! -d "$CLASSES" ]]; then
    echo "Compiler classes not found; run 'make build' first." >&2
    exit 2
fi

if [[ $# -eq 0 ]]; then
    groups=(lexer parser semantics)
else
    groups=("$@")
fi

for group in "${groups[@]}"; do
    case "$group" in
        lexer|parser|semantics) ;;
        *)
            echo "Unknown test group: $group" >&2
            echo "Usage: $0 [lexer] [parser] [semantics]" >&2
            exit 2
            ;;
    esac
done

tmp_dir=$(mktemp -d 2>/dev/null || mktemp -d -t lucac-tests)
trap 'rm -rf "$tmp_dir"' EXIT

total=0
failed=0
failed_tests=()

for group in "${groups[@]}"; do
    case "$group" in
        lexer)
            command=lex
            suffix=.out
            ;;
        parser)
            command=parse
            suffix=.xml
            ;;
        semantics)
            command=check
            suffix=.out
            ;;
    esac

    directory="$ROOT/tests/$group"
    mapfile -t sources < <(find "$directory" -type f -name '*.luc' | sort)
    group_passed=0

    for source in "${sources[@]}"; do
        relative=${source#"$ROOT/"}
        expected="${source%.luc}${suffix}"
        actual="$tmp_dir/actual"
        normalized_expected="$tmp_dir/expected"
        normalized_actual="$tmp_dir/actual-normalized"
        total=$((total + 1))

        if [[ ! -f "$expected" ]]; then
            echo "FAIL $relative: missing ${expected#"$ROOT/"}" >&2
            failed=$((failed + 1))
            failed_tests+=("$relative")
            continue
        fi

        status=0
        java -cp "$CLASSES" cli.Lucac "$command" "$source" >"$actual" 2>&1 || status=$?
        sed 's/\r$//' "$expected" >"$normalized_expected"
        sed 's/\r$//' "$actual" >"$normalized_actual"

        if [[ $status -eq 0 ]] && diff -u "$normalized_expected" "$normalized_actual" >/dev/null; then
            group_passed=$((group_passed + 1))
        else
            echo "" >&2
            echo "--- $relative ---" >&2
            if [[ $status -ne 0 ]]; then
                echo "process exited with status $status" >&2
            fi
            diff -u --label "${expected#"$ROOT/"}" --label "actual output" \
                "$normalized_expected" "$normalized_actual" >&2 || true
            failed=$((failed + 1))
            failed_tests+=("$relative")
        fi
    done

    echo "$group: $group_passed/${#sources[@]} passed"
done

if [[ $failed -ne 0 ]]; then
    echo "" >&2
    echo "$failed of $total regressions failed:" >&2
    printf '  %s\n' "${failed_tests[@]}" >&2
    exit 1
fi

echo "all $total regressions passed"
