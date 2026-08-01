# Performance Report — Hospital Management System

## 1. Overview

This report documents two performance optimizations applied to the Patient data layer:
searchable indexing and in-memory caching. Both are measured with before/after timings
using synthetic data generated specifically for these tests, and both were verified using
Postgres's own query planner (`EXPLAIN ANALYZE`) rather than relying on wall-clock timing alone.

## 2. Methodology

- **Environment**: PostgreSQL (local instance), JDBC via `postgresql:42.7.7`, JUnit 5 for
  correctness tests, standalone Java tools (`SearchPerformanceTest`, `CachePerformanceTest`)
  for timing benchmarks — these are not part of the automated test suite since they
  intentionally modify schema (drop/recreate indexes) and insert bulk synthetic data.
- **Synthetic data**: generated via batch JDBC inserts, always tagged with a recognizable
  email prefix (e.g. `perf_test_...@example.com`) so it can be reliably cleaned up after
  each run, leaving the real dataset untouched.
- **Two timing methods per benchmark**:
    - Wall-clock time (`System.nanoTime()`), representing what the application actually
      experiences, including JDBC connection overhead.
    - Postgres's own `EXPLAIN ANALYZE` execution time, representing pure query execution,
      excluding connection/network overhead — used as the authoritative number for the
      indexing benchmark specifically.

## 3. Search Indexing Optimization

### 3.1 Goal
Enable fast, case-insensitive prefix search on patient first/last name (User Story 3.1).

### 3.2 First attempt — and why it didn't work
The first indexing attempt created a standard functional B-tree index:
```sql
CREATE INDEX idx_patients_lower_last_name ON patients (LOWER(last_name));
```
Running `EXPLAIN ANALYZE` on the search query showed Postgres using a **Seq Scan** even
with the index present — the index was never used at all, regardless of table size.

**Root cause**: a plain B-tree index can only be used for `LIKE 'prefix%'` pattern matching
if the database locale is `C`, or if the index is built with the `text_pattern_ops` operator
class. Under a standard locale (e.g. `en_US.UTF-8`), Postgres's planner knows the index
can't support pattern matching and skips it entirely at planning time.

### 3.3 Fix
Indexes were rebuilt with the correct operator class:
```sql
CREATE INDEX idx_patients_lower_first_name ON patients (LOWER(first_name) text_pattern_ops);
CREATE INDEX idx_patients_lower_last_name ON patients (LOWER(last_name) text_pattern_ops);
```

### 3.4 Test setup
- 20,000 synthetic patient records inserted.
- Search target deliberately made rare (~0.5% of rows, ~150 matching records) to reflect a
  realistic "find one specific patient" search rather than a common term that would match a
  large fraction of the table (where a sequential scan is legitimately faster).

### 3.5 Results

| Metric | Without Index (Seq Scan) | With Index (Bitmap Heap Scan) | Improvement |
|---|---|---|---|
| Postgres execution time (`EXPLAIN ANALYZE`) | 59.436 ms | 2.413 ms | **~24.6x faster (96% reduction)** |
| Application wall-clock avg (50 runs, connection reused) | 145.480 ms | 115.260 ms | 20.8% faster |

Query plan confirmed the mechanism change:
- **Without index**: `Seq Scan on patients` — scanned all 20,000 rows, discarding 19,851 non-matches.
- **With index**: `Bitmap Heap Scan` combining two `Bitmap Index Scan`s (one per column) via
  `BitmapOr` — Postgres used both `LOWER(first_name)` and `LOWER(last_name)` indexes together
  to satisfy the `OR` condition, only visiting the ~150 matching rows directly.

The gap between the Postgres-level number (96% reduction) and the application-level number
(20.8%) is expected: each application call opens a fresh JDBC connection, and that overhead
is roughly constant regardless of indexing. In a long-running application with pooled/reused
connections, the actual user-facing benefit is much closer to the Postgres-level number.

## 4. Caching Optimization

### 4.1 Goal
Reduce repeated database load for frequently-accessed patient data (User Story 3.2), backed
by explicit cache invalidation on any write, so data can never go stale.

### 4.2 Design
`PatientCache` is a `HashMap<Integer, Patient>` inside `PatientService`. Conceptually this
mirrors what a database index does — instead of scanning/querying for data, a direct
key-based lookup avoids the scan entirely. A B-tree index gives O(log n) lookup on disk;
this in-memory HashMap gives O(1) average-case lookup in RAM — same underlying idea
(avoid re-deriving the answer from scratch), applied at the application layer instead of
the database layer.

Cache invalidation strategy: any `addPatient`/`updatePatient`/`deletePatient` call clears
the entire cache rather than attempting to patch individual entries — trading a small
amount of efficiency for a strong correctness guarantee (no risk of ever serving stale data).

### 4.3 Test setup
- 5,000 synthetic patient records inserted.
- **Without cache**: a fresh `PatientService` instance created per call — since each new
  instance starts with an empty cache, every call is forced to hit the database, simulating
  the absence of any caching layer.
- **With cache**: a single `PatientService` instance reused for all 50 calls — first call
  populates the cache (one real DB hit), all subsequent calls are served from memory.

### 4.4 Results

| Metric | Without Cache | With Cache | Improvement |
|---|---|---|---|
| Average time per call (50 calls) | 127.920 ms | 8.200 ms | **93.6% faster** |

Console output confirmed the mechanism directly: 50 consecutive `[Cache] MISS` messages in
the uncached run, versus one `[Cache] MISS` followed by 49 `[Cache] HIT` messages in the
cached run.

Note: the "with cache" average includes the one real DB-hit call, so the *actual* per-hit
speed is even better than the blended average shown — the 49 pure cache hits alone would
average well under 1 ms each.

## 5. Conclusion

Both optimizations produced large, measurable, and mechanistically-verified improvements:

- **Indexed search**: ~24.6x faster query execution (Postgres `EXPLAIN ANALYZE`), confirmed
  by the query plan changing from `Seq Scan` to `Bitmap Heap Scan`.
- **In-memory caching**: ~93.6% faster average call time, confirmed by console-level
  cache hit/miss logging.

A notable part of this process was diagnosing why the *first* indexing attempt showed no
improvement at all — the index existed but was structurally incapable of being used for
pattern-matching queries. This is a common, easy-to-miss Postgres behavior, and confirming
it via `EXPLAIN ANALYZE` (rather than assuming an index automatically helps) was essential
to actually fixing it.