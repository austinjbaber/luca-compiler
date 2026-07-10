#!/bin/bash

TESTNAME=$1
INPUT_FILE="tests/${TESTNAME}.luc"
OUTPUT_DIR="test_results"
OUTPUT_FILE="${OUTPUT_DIR}/my_${TESTNAME}.out"
EXPECTED_FILE="test_results/${TESTNAME}.out"

java lexer.Lex "${INPUT_FILE}" > "${OUTPUT_FILE}"

echo ""
echo "running diff ${EXPECTED_FILE} with ${OUTPUT_FILE}"
echo ""

if diff "${EXPECTED_FILE}" "${OUTPUT_FILE}"; then
    echo "passed"
    exit 0
else
    echo "failed"
    exit 1
fi