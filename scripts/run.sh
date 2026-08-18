#!/usr/bin/env bash
# Compile and run the console app.
#
# Git Bash on Windows launches Windows java.exe, which needs ';' in -cp.
# A Unix ':' classpath is treated as one bogus path, so Java never sees '.'
# and fails with: Could not find or load main class P3.InitDb
set -euo pipefail

# Stop Git Bash from rewriting classpath arguments into Windows paths.
export MSYS_NO_PATHCONV=1
export MSYS2_ARG_CONV_EXCL='*'

cd "$(dirname "$0")/.."

if ! command -v javac >/dev/null 2>&1; then
  echo "javac not found. Install a JDK (not only a JRE) and add it to PATH." >&2
  exit 1
fi

if [[ ! -f lib/sqlite-jdbc.jar || ! -f lib/postgresql.jar ]]; then
  bash scripts/download-drivers.sh
fi

# Windows Java (Git Bash / MSYS / Cygwin) uses ';' ; Linux and macOS use ':'.
case "${OSTYPE:-}" in
  msys*|cygwin*|win32*) SEP=';' ;;
  *)
    case "$(uname -s 2>/dev/null || echo unknown)" in
      MINGW*|MSYS*|CYGWIN*|Windows_NT) SEP=';' ;;
      *) SEP=':' ;;
    esac
    ;;
esac

CP="lib/postgresql.jar${SEP}lib/sqlite-jdbc.jar${SEP}."

javac -cp "$CP" -d . InitDb.java goBabbyApp.java

if [[ ! -f P3/InitDb.class || ! -f P3/goBabbyApp.class ]]; then
  echo "javac did not write P3/*.class. The sources use 'package P3;' so -d . must create that folder." >&2
  exit 1
fi

if [[ ! -f materna.db && -z "${JDBC_URL:-}" && -z "${DATABASE_URL:-}" ]]; then
  java -cp "$CP" P3.InitDb
fi
exec java -cp "$CP" P3.goBabbyApp
