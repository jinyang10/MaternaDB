#!/usr/bin/env bash
# Compile and run the console app.
#
# Git Bash on Windows launches Windows java.exe. A Unix ':' classpath is one
# bogus path, so '.' is dropped and you get ClassNotFoundException: P3.InitDb.
# Git Bash can also rewrite a ';' -cp argument. On Windows we therefore launch
# scripts/run.cmd via cmd.exe, which sets CLASSPATH and does not pass -cp.
set -euo pipefail

# Stop Git Bash from rewriting paths in arguments we do pass through.
export MSYS_NO_PATHCONV=1
export MSYS2_ARG_CONV_EXCL='*'

cd "$(dirname "$0")/.."

windows=0
case "${OSTYPE:-}" in msys*|cygwin*|win32*) windows=1 ;; esac
case "$(uname -s 2>/dev/null || echo unknown)" in
  MINGW*|MSYS*|CYGWIN*|Windows_NT) windows=1 ;;
esac
if [[ -n "${MSYSTEM:-}" && "${MSYSTEM}" == MINGW* ]]; then
  windows=1
fi

if [[ "$windows" -eq 1 ]]; then
  if [[ ! -f scripts/run.cmd ]]; then
    echo "scripts/run.cmd is missing; cannot start Windows Java correctly." >&2
    exit 1
  fi
  echo "Windows Git Bash detected; launching scripts/run.cmd so Java gets a ';' classpath."
  exec cmd.exe //c 'scripts\run.cmd'
fi

if ! command -v javac >/dev/null 2>&1; then
  echo "javac not found. Install a JDK (not only a JRE) and add it to PATH." >&2
  exit 1
fi

if [[ ! -f lib/sqlite-jdbc.jar || ! -f lib/postgresql.jar ]]; then
  bash scripts/download-drivers.sh
fi

# JDBC drivers are runtime-only; javac does not need them on the classpath.
javac -d . InitDb.java goBabbyApp.java

if [[ ! -f P3/InitDb.class || ! -f P3/goBabbyApp.class ]]; then
  echo "javac did not write P3/*.class. The sources use 'package P3;' so -d . must create that folder." >&2
  exit 1
fi

CP="lib/postgresql.jar:lib/sqlite-jdbc.jar:."

if [[ ! -f materna.db && -z "${JDBC_URL:-}" && -z "${DATABASE_URL:-}" ]]; then
  java -cp "$CP" P3.InitDb
fi
exec java -cp "$CP" P3.goBabbyApp
