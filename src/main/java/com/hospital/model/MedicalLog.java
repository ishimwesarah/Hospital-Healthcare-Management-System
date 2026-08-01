package com.hospital.model;

import java.time.LocalDateTime;

/**
 * Represents an unstructured medical log/note entry for a patient.
 *
 * Deliberately does NOT have fixed fields like "symptoms" or "vitals" — that would defeat
 * the purpose of using JSONB in the first place. logDataJson holds a raw JSON string whose
 * shape can vary freely from one log entry to the next (this is the actual point being
 * demonstrated: schema flexibility for genuinely unstructured clinical data).
 */
public class MedicalLog {

    private int logId;
    private int patientId;
    private LocalDateTime logDate;
    private String logDataJson; // raw JSON text, e.g. {"symptoms": ["cough", "fever"], "severity": "moderate"}

    public MedicalLog() {
    }

    // Use for a NEW log entry (id/date not yet known)
    public MedicalLog(int patientId, String logDataJson) {
        this.patientId = patientId;
        this.logDataJson = logDataJson;
    }

    // Use when reading FROM the database (id and date are known)
    public MedicalLog(int logId, int patientId, LocalDateTime logDate, String logDataJson) {
        this.logId = logId;
        this.patientId = patientId;
        this.logDate = logDate;
        this.logDataJson = logDataJson;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDateTime logDate) {
        this.logDate = logDate;
    }

    public String getLogDataJson() {
        return logDataJson;
    }

    public void setLogDataJson(String logDataJson) {
        this.logDataJson = logDataJson;
    }

    @Override
    public String toString() {
        return "MedicalLog{" +
                "logId=" + logId +
                ", patientId=" + patientId +
                ", logDate=" + logDate +
                ", logDataJson='" + logDataJson + '\'' +
                '}';
    }
}