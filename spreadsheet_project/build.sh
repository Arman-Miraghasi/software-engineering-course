#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out/main out/test
find src -name '*.java' -print > out/sources.txt
javac -encoding UTF-8 -cp lib/expression-factory.jar -d out/main @out/sources.txt
if [[ ${1:-} == test ]]; then
  "${MAVEN_CMD:-mvn}" test
fi
