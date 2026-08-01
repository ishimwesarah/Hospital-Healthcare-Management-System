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


CREATE TABLE appointments (
                              appointment_id    SERIAL PRIMARY KEY,
                              patient_id        INT NOT NULL REFERENCES patients(patient_id),
                              doctor_id         INT NOT NULL REFERENCES doctors(doctor_id),
                              appointment_date  DATE NOT NULL,
                              appointment_time  TIME NOT NULL,
                              reason            VARCHAR(200),
                              status            VARCHAR(20) NOT NULL DEFAULT 'Scheduled',
                              created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_appointments_date ON appointments(appointment_date);
CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX idx_appointments_doctor_id ON appointments(doctor_id);


CREATE INDEX idx_patients_lower_last_name ON patients (LOWER(last_name));
CREATE INDEX idx_patients_lower_first_name ON patients (LOWER(first_name));

CREATE TABLE prescriptions (
                               prescription_id   SERIAL PRIMARY KEY,
                               patient_id        INT NOT NULL REFERENCES patients(patient_id),
                               doctor_id         INT NOT NULL REFERENCES doctors(doctor_id),
                               date_prescribed   DATE NOT NULL DEFAULT CURRENT_DATE,
                               notes             VARCHAR(300),
                               created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE prescription_items (
                                    prescription_item_id  SERIAL PRIMARY KEY,
                                    prescription_id       INT NOT NULL REFERENCES prescriptions(prescription_id) ON DELETE CASCADE,
                                    medication_name       VARCHAR(150) NOT NULL,
                                    dosage                VARCHAR(50),
                                    frequency             VARCHAR(50),
                                    duration              VARCHAR(50),
                                    quantity              INT
);

CREATE INDEX idx_prescriptions_patient_id ON prescriptions(patient_id);
CREATE INDEX idx_prescriptions_doctor_id ON prescriptions(doctor_id);
CREATE INDEX idx_prescription_items_prescription_id ON prescription_items(prescription_id);