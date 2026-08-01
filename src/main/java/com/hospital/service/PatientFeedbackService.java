package com.hospital.service;

import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.PatientFeedbackDAO;
import com.hospital.model.PatientFeedback;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PatientFeedbackService {

    private final PatientFeedbackDAO feedbackDAO = new PatientFeedbackDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    public List<PatientFeedback> getAllFeedback() throws SQLException {
        return feedbackDAO.findAllWithNames();
    }

    public PatientFeedback getFeedbackById(int id) throws SQLException {
        return feedbackDAO.findById(id);
    }

    public int addFeedback(PatientFeedback feedback) throws SQLException {
        validate(feedback);
        return feedbackDAO.create(feedback);
    }

    public boolean updateFeedback(PatientFeedback feedback) throws SQLException {
        validate(feedback);
        return feedbackDAO.update(feedback);
    }

    public boolean deleteFeedback(int id) throws SQLException {
        return feedbackDAO.delete(id);
    }

    private void validate(PatientFeedback feedback) throws SQLException {
        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        if (feedback.getFeedbackDate() == null) {
            throw new IllegalArgumentException("Feedback date is required.");
        }
        if (feedback.getFeedbackDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Feedback date cannot be in the future.");
        }
        if (patientDAO.findById(feedback.getPatientId()) == null) {
            throw new IllegalArgumentException("Selected patient does not exist.");
        }
        // doctorId is optional (nullable) — only validate existence if one was actually selected
        if (feedback.getDoctorId() != null && doctorDAO.findById(feedback.getDoctorId()) == null) {
            throw new IllegalArgumentException("Selected doctor does not exist.");
        }
    }
}