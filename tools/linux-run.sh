#!/bin/sh
# Builds the library and runs every measurement in docs/measurement.md inside
# a Linux container.
#
#   sh tools/linux-run.sh > media/captures/linux-run.txt
#
# The repository is mounted read-only and copied inside the container.
set -eu

REPO=$(cd "$(dirname "$0")/.." && pwd)
IMAGE=maven:3.9-eclipse-temurin-11

docker pull -q "$IMAGE" >/dev/null
docker run --rm -v "$REPO":/repo:ro "$IMAGE" sh -c '
section() { printf "\n=== %s\n" "$*"; }
apt-get -qq update >/dev/null 2>&1 && apt-get -qq install -y python3 >/dev/null 2>&1
cp -r /repo /tmp/sw && cd /tmp/sw

section "environment"
uname -srm
java -version 2>&1 | head -1
mvn -v 2>/dev/null | head -1
python3 --version

section "mvn -q -Dgpg.skip=true package"
mvn -q -B -Dgpg.skip=true package >/tmp/mvn.log 2>&1
echo "exit=$?"
ls target/*.jar | sed "s#^#  #"

section "mvn -q -Dgpg.skip=true install"
mvn -q -B -Dgpg.skip=true install >/tmp/mvn-install.log 2>&1
echo "exit=$?"
ls ~/.m2/repository/ch/bissbert/*/*/*.jar 2>/dev/null | sed "s#.*/repository/#  ~/.m2/repository/#"

section "python3 tools/measure.py"
python3 tools/measure.py
'
