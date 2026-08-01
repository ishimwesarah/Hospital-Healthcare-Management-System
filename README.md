# Hospital Management System

A JavaFX + PostgreSQL desktop application for managing hospital operations: patients, doctors,
departments, appointments, prescriptions, feedback, and inventory — built with a layered
Controller → Service → DAO architecture, JDBC persistence, and application-level caching and
sorting.

## Features

- **Patient management** — registration, search (case-insensitive, indexed), full CRUD
- **Doctor management** — CRUD with department assignment
- **Department management** — CRUD, referenced by doctors
- **Appointment scheduling** — links patients + doctors, with joined queries showing names instead of raw IDs
- **Prescriptions & prescription items** — one prescription can hold multiple medication items (parent/child relationship with cascade delete)
- **Patient feedback** — optional doctor association (general or doctor-specific feedback)
- **Medical inventory** — stock tracking with low-stock flagging based on reorder level
- **Indexed search** — case-insensitive prefix search on patient name, backed by `text_pattern_ops` Postgres indexes
- **In-memory caching** — `HashMap`-based patient cache with explicit invalidation on any write
- **Manual merge sort** — patient listings sorted via a hand-written merge sort implementation (not a library call)
- **Performance benchmarking tools** — standalone tools measuring indexing and caching gains, see [`docs/performance_report.md`](docs/performance_report.md)

## Tech Stack

- Java 17
- JavaFX 21 (UI)
- PostgreSQL (database)
- JDBC (`org.postgresql:postgresql:42.7.7`)
- Maven (build tool)
- JUnit 5 (testing)

## Architecture

Controller (JavaFX UI, src/main/java/com/hospital/ui)
↓
Service (validation, business logic, src/main/java/com/hospital/service)
↓
DAO (pure SQL/JDBC, src/main/java/com/hospital/dao)
↓
PostgreSQL database


Supporting layers:
- `com.hospital.model` — plain data classes (Patient, Doctor, Department, Appointment, Prescription, PrescriptionItem, PatientFeedback, MedicalInventory)
- `com.hospital.cache` — in-memory caching (`PatientCache`)
- `com.hospital.util` — algorithms (`PatientSorter` — merge sort)
- `com.hospital.performance` — standalone benchmarking tools (not part of the JUnit suite; these modify schema/insert bulk data)
- `com.hospital.db` — `DatabaseConnection`, the single source of the JDBC connection

## Prerequisites

- **Java 17+** (JDK)
- **PostgreSQL** installed locally, with a database created (see setup below)
- **IntelliJ IDEA** (or any IDE with Maven support)
- **Maven** (bundled with IntelliJ — no separate install needed if using IntelliJ's Maven tool window)

## Setup

### 1. Clone the repository
```bash
git clone https://github.com/ishimwesarah/Hospital-Healthcare-Management-System.git
cd Hospital-Healthcare-Management-System
```

### 2. Create the database
Using pgAdmin 4 (or `psql`):
```sql
CREATE DATABASE hospital_db;
```

### 3. Run the schema
Open `src/main/resources/sql/schema.sql` and execute it against `hospital_db` — either:
- **pgAdmin**: right-click `hospital_db` → Query Tool → paste the script → execute (F5)
- **IntelliJ Database tool window**: add a PostgreSQL data source pointing at `hospital_db`, open `schema.sql`, associate it with that data source, then execute

This creates all tables (patients, doctors, departments, appointments, prescriptions,
prescription_items, patient_feedback, medical_inventory), their indexes, and foreign key
constraints.

### 4. Configure the database connection
Edit `src/main/java/com/hospital/db/DatabaseConnection.java` with your local Postgres credentials:
```java
private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
private static final String USER = "postgres";
private static final String PASSWORD = "your_password_here";
```

### 5. Install dependencies
In IntelliJ: right-click `pom.xml` → **Maven → Reload Project**. This pulls JavaFX, the
PostgreSQL JDBC driver, and JUnit 5.

## Running the Application

Use IntelliJ's **Maven tool window**: expand the project → **Plugins → javafx → javafx:run**,
then double-click it.

(Running `MainApp.main()` directly via IntelliJ's green Run button will fail with
`Error: JavaFX runtime components are missing` — JavaFX must be launched via the Maven
plugin, which handles the module path correctly.)

## Running Tests

Right-click `src/test/java` → **Run 'All Tests'**, or run individual test classes
(e.g. `PatientDAOTest`, `AppointmentDAOTest`, `PrescriptionDAOTest`) directly. These are
integration tests — they connect to your real `hospital_db` and clean up after themselves
via `@AfterEach`.

## Running Performance Benchmarks

These are standalone tools, not JUnit tests (they insert bulk synthetic data and, in the
search case, temporarily drop/recreate indexes):

- `src/main/java/com/hospital/performance/SearchPerformanceTest.java` — measures indexed vs. non-indexed patient search
- `src/main/java/com/hospital/performance/CachePerformanceTest.java` — measures cached vs. uncached patient list retrieval

Right-click either → Run. Both clean up all synthetic data on completion. See
[`docs/performance_report.md`](docs/performance_report.md) for recorded results and methodology.

## Project Structure

src/main/java/com/hospital/
├── cache/ PatientCache (in-memory caching)
├── dao/ JDBC data access — one DAO per entity
├── db/ DatabaseConnection
├── model/ Plain data classes — one per entity
├── performance/ Standalone benchmarking tools
├── service/ Validation + business logic — one per entity
├── ui/ JavaFX tabs — one per entity, assembled in MainApp
└── util/ PatientSorter (merge sort)

src/main/resources/sql/
└── schema.sql All table definitions, indexes, and constraints

src/test/java/com/hospital/
├── cache/ PatientCache unit tests
├── dao/ DAO integration tests (one per entity)
└── util/ PatientSorter unit tests

docs/
└── performance_report.md


## Git Workflow

This project follows a git-flow-style branching model:
- `main` — stable, always-working; receives merges only from `develop`
- `develop` — integration branch; receives merges only from `feature/*` branches
- `feature/*` — one branch per slice of functionality (e.g. `feature/patient-crud`,
  `feature/appointment-crud`), merged into `develop` once its tests pass

[//]: # (## Known Limitations)

[//]: # ()
[//]: # (- `PrescriptionService.addPrescriptionWithItems` is not wrapped in a single database)

[//]: # (  transaction — if an item insert fails partway through, the parent prescription row could)

[//]: # (  be left with only some of its items. A production version would wrap the parent + child)

[//]: # (  inserts in one transaction &#40;`conn.setAutoCommit&#40;false&#41;` shared across both DAOs&#41;.)

[//]: # (- Database credentials are stored directly in `DatabaseConnection.java` rather than an)

[//]: # (  environment variable or config file — acceptable for local development, not for)

[//]: # (  production deployment.)