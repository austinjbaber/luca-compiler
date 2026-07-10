#!/bin/bash

PASSED=0
FAILED=0
FAILED_TESTS=()

for test_file in tests/*.luc; do
    testname=$(basename "$test_file" .luc)
    
    echo "=========================================="
    echo "testing ${testname}"
    
    if ./test_parser.sh "${testname}"; then
        ((PASSED++))
    else
        ((FAILED++))
        FAILED_TESTS+=("${testname}")
    fi
    echo ""
done

echo "=========================================="
echo "total passed: ${PASSED}"
echo "total failed: ${FAILED}"

if [ ${FAILED} -gt 0 ]; then
    echo ""
    echo "failed tests:"
    for test in "${FAILED_TESTS[@]}"; do
        echo "$test"
    done
    exit 1
fi

exit 0