# NoSQL Design — Medical Logs (Optional)

## 1. Use Case

Clinical notes and medical logs are inherently **variable in shape**. A routine checkup
might record blood pressure and temperature. A specialist consultation might record a list
of symptoms, a severity rating, and free-text observations. A follow-up visit might record
none of those and instead reference a previous log ID with a status update. Unlike Patients,
Doctors, or Appointments — which have a fixed, predictable set of fields — medical logs do
not have a single "correct" schema that fits every entry.

## 2. Why NoSQL (Document Storage) Fits Better Than a Rigid Relational Table

Forcing this data into a traditional relational table would require one of two approaches,
both flawed:

1. **A wide table with many nullable columns** (`blood_pressure`, `temperature`, `symptoms`,
   `severity`, `follow_up_notes`, ...) — most rows would have most columns `NULL`, the table
   would need constant schema migrations every time a new type of note is needed, and
   queries would be full of `WHERE column IS NOT NULL` noise.
2. **An EAV (Entity-Attribute-Value) table** (`log_id`, `attribute_name`, `attribute_value`)
   — technically flexible, but every read requires reconstructing the object from many rows,
   losing type information, and making even simple queries expensive and awkward.

A **document-oriented** approach — storing each log entry as a self-contained JSON document —
avoids both problems: each entry carries exactly the fields relevant to that particular visit,
new "shapes" of note can be introduced without any schema migration, and the whole document
is stored and retrieved as one unit.

## 3. Implementation Choice: PostgreSQL `JSONB`

Rather than introducing a separate NoSQL database (e.g. MongoDB) purely for this one optional
feature, this project uses PostgreSQL's native `JSONB` column type. This is a legitimate,
production-used pattern — many real systems use Postgres as a hybrid relational + document
store rather than running two separate databases.

```sql
CREATE TABLE medical_logs (
    log_id      SERIAL PRIMARY KEY,
    patient_id  INT NOT NULL REFERENCES patients(patient_id),
    log_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    log_data    JSONB NOT NULL
);

CREATE INDEX idx_medical_logs_data ON medical_logs USING GIN (log_data);
```

`JSONB` (as opposed to plain `JSON`) stores the data in a decomposed binary format, which
Postgres can index (via a GIN index) and query efficiently — this is what makes it
meaningfully different from just storing a text blob.

### Example — two structurally different logs for the same patient

```json
// Log 1: routine checkup
{"type": "checkup", "vitals": {"bp": "120/80", "temp": 36.6}}

// Log 2: symptom report — completely different fields
{"type": "symptom_report", "symptoms": ["cough", "fatigue"], "severity": "moderate"}
```

Both rows live in the same table, same column, with no schema change required between them.

### Querying inside the flexible structure

PostgreSQL's `@>` (containment) operator lets us query *inside* the JSON structure directly:

```sql
SELECT * FROM medical_logs WHERE log_data @> '{"severity": "high"}'::jsonb;
```

This finds every log where `severity` is `"high"`, regardless of what other fields that
particular entry happens to have — demonstrated in `MedicalLogDAOTest.findByJsonContains_...`.

## 4. Comparison: Relational vs. Document Approach

| Aspect | Rigid Relational Table | JSONB Document Column |
|---|---|---|
| Adding a new note "type" | Requires `ALTER TABLE` migration | No schema change needed |
| Storage efficiency | Many `NULL` columns for unused fields | Only stores fields that exist |
| Query flexibility | Simple, but limited to existing columns | Can query any nested field via `@>`, `->`, `->>` |
| Type safety / validation | Enforced strictly by column types | Looser — validated at the application layer instead |
| Best fit for | Patients, Doctors, Appointments (fixed, predictable structure) | Clinical notes, logs (variable, evolving structure) |

## 5. Conclusion

This project uses relational tables (with foreign keys, indexes, and 3NF normalization) for
all core entities where the structure is fixed and predictable — Patients, Doctors,
Departments, Appointments, Prescriptions, Feedback, and Inventory. For medical logs
specifically, where the shape of the data genuinely varies entry-to-entry, a `JSONB`
document column is used instead. This is a deliberate, justified choice per entity type,
not a wholesale replacement of the relational model — reflecting the reality that different
data has different structural needs, even within a single application.