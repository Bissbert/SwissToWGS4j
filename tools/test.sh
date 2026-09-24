#!/bin/sh
# Run the JUnit suite in a Linux container with the working tree as it is now.
#
#   sh tools/test.sh
set -eu

REPO=$(cd "$(dirname "$0")/.." && pwd)
docker run --rm -v "$REPO":/repo:ro maven:3.9-eclipse-temurin-11 sh -c '
cp -r /repo /tmp/sw && cd /tmp/sw && rm -rf target && mvn -B -q -Dgpg.skip=true test
'
