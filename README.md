# MaternaDB – Midwifery Service Database System

A Java **Swing** desktop app (plus the original console program) that talks to a relational database over JDBC so a midwife can look up appointments, read notes and tests, add an observation, and prescribe a test.

Repo now connects to **SQLite** (local file, no account) or **PostgreSQL** (free hosted databases such as Neon or Supabase).

---

## Features

- Desktop Swing workspace for login, appointments, notes, and tests
- Query midwives and scheduled appointments by date
- View patient-specific appointment details
- Review clinical notes and diagnostic test results
- Add new observations (notes) to appointments
- Prescribe diagnostic tests linked to patient records

---

### Tables this app actually queries

Column names in the Java SQL are the DB2 names (shown in uppercase in queries):

| Table | Role in the UI |
| --- | --- |
| `MIDWIVES` | Login: practitioner id (`pracID`) must exist |
| `ASSIGNEDMW` | Which midwife is primary (`IS_PRIMARY`) vs backup for a pregnancy (`NTHPREG`, `CID`) |
| `SETAPPOINT` | Midwife who booked the appointment (`pracID`) |
| `APPOINTMENTS` | Date (`ADATE`), time (`ATIME`), pregnancy, `APPOINTID` |
| `COUPLE` / `MOTHERS` | Mother name (`MNAME`) and health card (`QCHCN`) |
| `NOTES` | Observation text (`OBSERV`) and note time (`NTIME`) |
| `TESTS` | Prescribed tests (`TESTTYPE`, `PRESCDATE`, `RESULT`) |

### App workflow

1. Sign in with a practitioner id.
2. Load a date `YYYY-MM-DD`. The table lists that midwife’s appointments, ordered by time, with `P` (primary) or `B` (backup), mother name, and health card number.
3. Select a visit to review notes, review tests, add a note, or prescribe a test.

Adding a note inserts `(CURRENT_TIME, APPOINTID, observation)` into `NOTES`. Prescribing a test inserts a row into `TESTS` with today’s date and a `NULL` result (shown as `PENDING`).

---

## Connect to another free database

Credentials are read from the environment (never hard-code passwords):

| Variable | Purpose |
| --- | --- |
| `JDBC_URL` or `DATABASE_URL` | JDBC or `postgres://` URL |
| `JDBC_USER` | User (optional if the URL already contains it) |
| `JDBC_PASSWORD` | Password |

If `JDBC_URL` is unset, the app uses a local file **`materna.db`** (SQLite). That is the fastest free option.

### 1. Local SQLite (no account)

On **Windows**, do not copy-paste `java -cp "lib/*:." ...`. That Unix classpath makes Windows Java report `ClassNotFoundException: P3.InitDb` even when `P3/InitDb.class` exists.

From **Command Prompt** (including if Git Bash dropped you into `C:\Users\...>`):

```bat
scripts\run.cmd
```

From **Git Bash** (stays in bash; does not open `cmd.exe`):

```bash
bash scripts/run.sh
```

On Linux/macOS:

```bash
bash scripts/run.sh
```

Manual commands (Linux/macOS):

```bash
bash scripts/download-drivers.sh
javac -d . InitDb.java goBabbyApp.java MaternaDb.java MaternaApp.java
java -cp "lib/*:." P3.InitDb          # creates materna.db and loads sample rows
java -cp "lib/*:." P3.MaternaApp
```

Manual commands (Windows CMD):

```bat
javac -d . InitDb.java goBabbyApp.java MaternaDb.java MaternaApp.java
java -cp "lib\*;." P3.InitDb
java -cp "lib\*;." P3.MaternaApp
```

A window opens. Sign in as **`MW001`**, keep date **`2026-03-15`**, and click **Load**. When prescribing a test, use technician id **`T001`** and any unused test id (for example `TST03`).

The original console program is still there:

```bash
java -cp "lib/*:." P3.goBabbyApp          # Linux/macOS
java -cp "lib\*;." P3.goBabbyApp          # Windows
```

### 2. Free hosted PostgreSQL (Neon or Supabase)

1. Create a free project at [Neon](https://neon.tech) or [Supabase](https://supabase.com).
2. Copy the connection string. Convert it if needed:

   | Provider string | JDBC URL |
   | --- | --- |
   | `postgres://USER:PASS@HOST/db?sslmode=require` | Accepted as `DATABASE_URL` (the app prefixes `jdbc:`) |
   | Neon Java snippet | `jdbc:postgresql://HOST/db?sslmode=require` plus user/password |

3. Load the schema (from this repo, using `psql` or the provider SQL editor):

   ```bash
   psql "$DATABASE_URL" -f sql/schema.sql -f sql/seed.sql
   ```

   Or with Java:

   ```bash
   export DATABASE_URL='postgres://USER:PASS@HOST/db?sslmode=require'
   java -cp "lib/*:." P3.InitDb
   java -cp "lib/*:." P3.MaternaApp
   ```

4. Example env vars (see `env.example`):

   ```bash
   export JDBC_URL='jdbc:postgresql://ep-xxxx.region.aws.neon.tech/neondb?sslmode=require'
   export JDBC_USER='your_user'
   export JDBC_PASSWORD='your_password'
   ```

### 3. PostgreSQL on your machine

Install Postgres locally, create an empty database, then:

```bash
export JDBC_URL='jdbc:postgresql://localhost:5432/materna'
export JDBC_USER='postgres'
export JDBC_PASSWORD='postgres'
java -cp "lib/*:." P3.InitDb
java -cp "lib/*:." P3.MaternaApp
```
---

## Technologies

- Java (Swing UI, optional console)
- JDBC (PostgreSQL, SQLite, or DB2)
- SQL

Full original model and screenshots: [`info.pdf`](./info.pdf)
