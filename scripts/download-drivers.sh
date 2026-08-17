#!/usr/bin/env bash
# Download free JDBC drivers so the app can talk to PostgreSQL or SQLite.
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p lib
curl -fsSL -o lib/postgresql.jar \
  https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar
curl -fsSL -o lib/sqlite-jdbc.jar \
  https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.47.1.0/sqlite-jdbc-3.47.1.0.jar
echo "Drivers saved in lib/"
ls -l lib/*.jar
