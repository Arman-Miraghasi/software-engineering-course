#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
bash build.sh
java -cp out/main:lib/expression-factory.jar Main "$@"
