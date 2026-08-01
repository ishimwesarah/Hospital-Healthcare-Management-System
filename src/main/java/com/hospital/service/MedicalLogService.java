package com.hospital.service;

import com.hospital.dao.MedicalLogDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.MedicalLog;

import java.sql.SQLException;
import java.util.List;

public class MedicalLogService {

    private final MedicalLogDAO logDAO = new MedicalLogDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    public List<MedicalLog> getLogsForPatient(int patientId) throws SQLException {
        return logDAO.findByPatientId(patientId);
    }

    public List<MedicalLog> getAllLogs() throws SQLException {
        return logDAO.findAll();
    }

    public List<MedicalLog> searchLogsByJsonField(String jsonFragment) throws SQLException {
        validateJson(jsonFragment);
        return logDAO.findByJsonContains(jsonFragment);
    }

    public int addLog(MedicalLog log) throws SQLException {
        validate(log);
        return logDAO.create(log);
    }

    public boolean deleteLog(int logId) throws SQLException {
        return logDAO.delete(logId);
    }

    private void validate(MedicalLog log) throws SQLException {
        if (patientDAO.findById(log.getPatientId()) == null) {
            throw new IllegalArgumentException("Selected patient does not exist.");
        }
        validateJson(log.getLogDataJson());
    }

    // Basic sanity check — a full JSON parser would be more thorough, but for this scope,
    // catching obviously malformed input before it reaches Postgres is enough.
    private void validateJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("Log data cannot be empty.");
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new IllegalArgumentException("Log data must be a valid JSON object, e.g. {\"key\": \"value\"}.");
        }
    }
}