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

The original Java assignment did **not** include screens to register a mother or book an appointment. Those rows live in the database (`sql/seed.sql`). The program was written for a midwife at the clinic desk: look up today’s list, read the chart, add a note, prescribe a test. Reception / admin work (new mothers, new bookings) was assumed to already be in DB2.

---

## Sample data to try

After a fresh load (`P3.InitDb`), sign in and click **Load** for each date below.

Delete `materna.db` first if you already ran the app once, then `bash scripts/run.sh` so the new rows are loaded.

| Sign in | Date | What you should see |
| --- | --- | --- |
| `MW001` | `2026-03-15` | Alice Smith **P** 09:30, Carol Jones **B** 11:15, Alice **P** 14:00, Dana Patel **P** 16:00 |
| `MW001` | `2026-03-22` | Dana **P** 08:45, Alice **P** 10:00, Carol **B** 13:30, Elena Rossi **B** 15:00 |
| `MW001` | `2026-04-02` | Alice **P** 09:15, Fatima Hassan **P** 11:00, Dana **P** 14:30 |
| `MW001` | `2026-04-10` | Elena **B** 09:45, Carol **B** 11:00, Fatima **P** 13:15 |
| `MW002` | `2026-03-15` | Same four visits, but Alice/Dana are **B** and Carol is **P** |
| `MW002` | `2026-04-10` | Elena **P** 09:45, Carol **P** 11:00, Fatima **B** 13:15 |

Open a visit and check **Notes** / **Tests**:

| Mother | Health card | Useful checks |
| --- | --- | --- |
| Alice Smith | `HCN001` | Several prenatal notes; blood work result “iron slightly low”; glucose still `PENDING` |
| Carol Jones | `HCN002` | Backup check-in note; urine dip “trace protein” on 2026-03-22 |
| Dana Patel | `HCN003` | Intake notes (penicillin allergy); blood type `A+` |
| Elena Rossi | `HCN004` | Transfer-of-care note; GBS swab `PENDING` |
| Fatima Hassan | `HCN005` | First-visit notes; no tests yet — prescribe one with tech `T001` |

When prescribing a test, use technician **`T001`** or **`T002`** and a new test id (for example `TST10`).


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

A window opens. Use the [sample data table](#sample-data-to-try) (start with **`MW001`** and **`2026-03-15`**).

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
