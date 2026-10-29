#!/bin/sh
set -eu
cd "$(dirname "$0")"
if [ -f dist/releaseflow.jar ]; then
  exec java -jar dist/releaseflow.jar --spring.profiles.active=demo "$@"
fi
if [ -f target/releaseflow-0.0.1-SNAPSHOT.jar ]; then
  exec java -jar target/releaseflow-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo "$@"
fi
printf '%s\n' 'Build first: ./mvnw verify' >&2
exit 1
