@echo off
setlocal EnableExtensions
cd /d "%~dp0\.."

where javac >nul 2>&1
if errorlevel 1 (
  echo javac not found. Install a JDK ^(not only a JRE^) and add it to PATH.
  exit /b 1
)

if exist lib\sqlite-jdbc.jar if exist lib\postgresql.jar goto :compile
where bash >nul 2>&1
if errorlevel 1 (
  echo Missing JDBC jars in lib\ and bash is not on PATH.
  echo From Git Bash run: bash scripts/download-drivers.sh
  exit /b 1
)
bash scripts/download-drivers.sh
if errorlevel 1 exit /b 1

:compile
rem JDBC jars are needed at runtime only.
javac -d . InitDb.java goBabbyApp.java
if errorlevel 1 exit /b 1

if not exist P3\InitDb.class (
  echo javac did not write P3\InitDb.class
  exit /b 1
)

set "CLASSPATH=lib\postgresql.jar;lib\sqlite-jdbc.jar;."

if exist materna.db goto :runapp
if defined JDBC_URL goto :runapp
if defined DATABASE_URL goto :runapp
java P3.InitDb
if errorlevel 1 (
  echo Failed to run P3.InitDb. Expected P3\InitDb.class and jars in lib\.
  dir P3
  dir lib
  exit /b 1
)

:runapp
java P3.goBabbyApp
exit /b %ERRORLEVEL%
