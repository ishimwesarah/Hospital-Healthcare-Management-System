CREATE TABLE patients (
                          patient_id      SERIAL PRIMARY KEY,
                          first_name      VARCHAR(50)  NOT NULL,
                          last_name       VARCHAR(50)  NOT NULL,
                          date_of_birth   DATE         NOT NULL,
                          gender          VARCHAR(10)  NOT NULL,
                          phone_number    VARCHAR(20)  UNIQUE,
                          email           VARCHAR(100) UNIQUE,
                          address         VARCHAR(200),
                          created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_patients_last_name ON patients(last_name);