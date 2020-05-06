#!/bin/sh
set -euf

JAR_PATH=$1
shift

if [ -n "${JAVA_OPTS:-}" ]; then
    exec java $JAVA_OPTS -jar "$JAR_PATH" "$@"
else
    exec java -jar "$JAR_PATH" "$@"
fi
