#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ ! -f lib/sqlite-jdbc.jar || ! -f lib/postgresql.jar ]]; then
  bash scripts/download-drivers.sh
fi
CP="lib/postgresql.jar:lib/sqlite-jdbc.jar:."
javac -cp "$CP" -d . InitDb.java goBabbyApp.java
if [[ ! -f materna.db && -z "${JDBC_URL:-}" && -z "${DATABASE_URL:-}" ]]; then
  java -cp "$CP" P3.InitDb
fi
exec java -cp "$CP" P3.goBabbyApp
