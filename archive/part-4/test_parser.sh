#!/bin/bash

TESTNAME=$1
INPUT_FILE="tests/${TESTNAME}.luc"
OUTPUT_DIR="test_results"
OUTPUT_FILE="${OUTPUT_DIR}/my_${TESTNAME}.xml"
EXPECTED_FILE="tests/${TESTNAME}.xml"

#luca_ast prog1.luc prog1.xml prog1.gv

java parser.Parse "${INPUT_FILE}" "${OUTPUT_FILE}"

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