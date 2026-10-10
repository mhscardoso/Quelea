#!/usr/bin/env bash
set -euo pipefail
QUELEA_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
export JAVA_HOME="$QUELEA_ROOT/.local-tools/jdk-25"
export GRADLE_USER_HOME="$QUELEA_ROOT/.local-tools/gradle"
export PATH="$JAVA_HOME/bin:$PATH"
if [[ ! -x "$JAVA_HOME/bin/java" ]]; then
    echo "JDK 25 não encontrado em $JAVA_HOME" >&2
    exit 1
fi
cd "$QUELEA_ROOT/Quelea"
if [[ $# -eq 0 ]]; then
    set -- run
fi
exec bash ./gradlew -x dependencyUpdates "$@"
