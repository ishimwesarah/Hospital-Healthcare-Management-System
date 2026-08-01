package com.hospital.service;

import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Appointment;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class AppointmentService {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private static final Set<String> VALID_STATUSES = Set.of(
            Appointment.STATUS_SCHEDULED, Appointment.STATUS_COMPLETED, Appointment.STATUS_CANCELLED
    );

    public List<Appointment> getAllAppointments() throws SQLException {
        return appointmentDAO.findAllWithNames();
    }

    public Appointment getAppointmentById(int id) throws SQLException {
        return appointmentDAO.findById(id);
    }

    public int addAppointment(Appointment appt) throws SQLException {
        validate(appt);
        return appointmentDAO.create(appt);
    }

    public boolean updateAppointment(Appointment appt) throws SQLException {
        validate(appt);
        return appointmentDAO.update(appt);
    }

    public boolean deleteAppointment(int id) throws SQLException {
        return appointmentDAO.delete(id);
    }

    private void validate(Appointment appt) throws SQLException {
        if (appt.getAppointmentDate() == null) {
            throw new IllegalArgumentException("Appointment date is required.");
        }
        if (appt.getAppointmentTime() == null) {
            throw new IllegalArgumentException("Appointment time is required.");
        }
        if (appt.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Appointment date cannot be in the past.");
        }
        if (appt.getStatus() == null || !VALID_STATUSES.contains(appt.getStatus())) {
            throw new IllegalArgumentException("Status must be one of: Scheduled, Completed, Cancelled.");
        }
        // Referential integrity checks at the app level, for clearer error messages
        // than a raw SQL foreign key violation would give.
        if (patientDAO.findById(appt.getPatientId()) == null) {
            throw new IllegalArgumentException("Selected patient does not exist.");
        }
        if (doctorDAO.findById(appt.getDoctorId()) == null) {
            throw new IllegalArgumentException("Selected doctor does not exist.");
        }
    }
}