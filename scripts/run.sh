#!/usr/bin/env bash
# Compile and run the console app.
#
# Git Bash on Windows launches Windows java.exe:
#   - ':' in -cp is not a classpath separator, so P3.InitDb is not found
#   - ';' in -cp is often rewritten by MSYS path conversion
#   - exec cmd.exe //c opens an interactive Command Prompt (Git Bash eats /c)
# So on Windows we set CLASSPATH and call java with no -cp and no cmd.exe.
set -euo pipefail

export MSYS_NO_PATHCONV=1
export MSYS2_ARG_CONV_EXCL='*'
export MSYS2_ENV_CONV_EXCL="${MSYS2_ENV_CONV_EXCL:+${MSYS2_ENV_CONV_EXCL};}CLASSPATH"

cd "$(dirname "$0")/.."

windows=0
case "${OSTYPE:-}" in msys*|cygwin*|win32*) windows=1 ;; esac
case "$(uname -s 2>/dev/null || echo unknown)" in
  MINGW*|MSYS*|CYGWIN*|Windows_NT) windows=1 ;;
esac
if [[ -n "${MSYSTEM:-}" && "${MSYSTEM}" == MINGW* ]]; then
  windows=1
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

run_java() {
  local main=$1
  if java "$main"; then
    return 0
  fi
  echo "Failed to run ${main}." >&2
  echo "CLASSPATH=${CLASSPATH:-}" >&2
  ls -l P3 lib 2>/dev/null || true
  return 1
}

if [[ "$windows" -eq 1 ]]; then
  if command -v cygpath >/dev/null 2>&1; then
    root="$(cygpath -w "$PWD")"
    export CLASSPATH="${root}\\lib\\postgresql.jar;${root}\\lib\\sqlite-jdbc.jar;${root}"
  else
    export CLASSPATH="lib/postgresql.jar;lib/sqlite-jdbc.jar;."
  fi
  echo "Windows Git Bash: using CLASSPATH (not cmd.exe, not java -cp)."
  if [[ ! -f materna.db && -z "${JDBC_URL:-}" && -z "${DATABASE_URL:-}" ]]; then
    run_java P3.InitDb
  fi
  exec java P3.goBabbyApp
fi

CP="lib/postgresql.jar:lib/sqlite-jdbc.jar:."
if [[ ! -f materna.db && -z "${JDBC_URL:-}" && -z "${DATABASE_URL:-}" ]]; then
  java -cp "$CP" P3.InitDb
fi
exec java -cp "$CP" P3.goBabbyApp
