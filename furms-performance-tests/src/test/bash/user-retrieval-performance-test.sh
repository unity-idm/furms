#!/bin/bash

#
# Copyright (c) 2021 Bixbit s.c. All rights reserved.
#  See LICENSE file for licensing information.
#

FENIX_ID=""
MAX_ALLOWED_TIME=1
TOTAL_WARMUP=10
TOTAL_TESTS=50

function send_request() {
  curl \
    -u cidp:cidppass \
    -w "%{time_total}" \
    -o /dev/null \
    -ks \
    https://localhost:3443/rest-api/v1/cidp/user/$FENIX_ID
}

# WARMUP
echo "Warmup..."
for i in $(seq 0 $((TOTAL_WARMUP-1)))
do
  send_request $i >> /dev/null
done

# PERFORMANCE TEST
echo "Begin performance test..."
PASSED=0
TIME_TOTAL=0
for i in $(seq 0 $((TOTAL_TESTS-1)))
do
  TIME=$(send_request $i)
  if [[ 1 -eq "$(echo "$TIME < $MAX_ALLOWED_TIME" | bc)" ]]
  then
    echo "[$i] $TIME [s] - PASSED"
    PASSED=$((PASSED+1))
  else
    echo "[$i] $TIME [s] - FAILED"
  fi
  TIME_TOTAL=$(bc <<< "scale=2; ${TIME_TOTAL}+${TIME}")
done
echo "End performance test..."
echo "Passed tests: $PASSED/$TOTAL_TESTS"
echo "Average time: $(bc <<< "scale=6; ${TIME_TOTAL}/${TOTAL_TESTS}" | awk '{printf "%f [s]", $0}')"