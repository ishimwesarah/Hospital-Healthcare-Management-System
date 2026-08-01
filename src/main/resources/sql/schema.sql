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

CREATE TABLE departments (
                             department_id   SERIAL PRIMARY KEY,
                             name             VARCHAR(100) NOT NULL UNIQUE,
                             location         VARCHAR(100),
                             phone_number     VARCHAR(20),
                             created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_departments_name ON departments(name);

CREATE TABLE doctors (
                         doctor_id       SERIAL PRIMARY KEY,
                         first_name      VARCHAR(50)  NOT NULL,
                         last_name       VARCHAR(50)  NOT NULL,
                         specialization  VARCHAR(100),
                         phone_number    VARCHAR(20)  UNIQUE,
                         email           VARCHAR(100) UNIQUE,
                         department_id   INT NOT NULL REFERENCES departments(department_id),
                         created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_doctors_last_name ON doctors(last_name);
CREATE INDEX idx_doctors_department_id ON doctors(department_id);