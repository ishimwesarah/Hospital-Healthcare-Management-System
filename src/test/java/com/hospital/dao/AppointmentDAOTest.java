package com.hospital.dao;

import com.hospital.model.Appointment;
import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentDAOTest {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    private int testDepartmentId;
    private int testDoctorId;
    private int testPatientId;
    private Integer createdAppointmentId;

    // Appointments need a real patient AND doctor (which needs a department), so set up the full chain
    @BeforeEach
    void setUpDependencies() throws Exception {
        testDepartmentId = departmentDAO.create(new Department("Test Dept", "Block Z", "0788000999"));
        testDoctorId = doctorDAO.create(new Doctor("Test", "Doctor", "General",
                "0788111000", "test.doctor@example.com", testDepartmentId));
        testPatientId = patientDAO.create(new Patient("Test", "Patient",
                LocalDate.of(1999, 1, 1), "Other", "0788222000", "test.patient@example.com", "Kigali"));
    }

    @Test
    void create_shouldInsertAppointmentAndReturnGeneratedId() throws Exception {
        Appointment appt = new Appointment(testPatientId, testDoctorId,
                LocalDate.now().plusDays(1), LocalTime.of(10, 30), "Checkup", Appointment.STATUS_SCHEDULED);

        int id = appointmentDAO.create(appt);
        createdAppointmentId = id;

        assertTrue(id > 0);
    }

    @Test
    void findById_shouldReturnCorrectAppointment() throws Exception {
        Appointment appt = new Appointment(testPatientId, testDoctorId,
                LocalDate.now().plusDays(2), LocalTime.of(14, 0), "Follow-up", Appointment.STATUS_SCHEDULED);
        int id = appointmentDAO.create(appt);
        createdAppointmentId = id;

        Appointment fetched = appointmentDAO.findById(id);

        assertNotNull(fetched);
        assertEquals(testPatientId, fetched.getPatientId());
        assertEquals(testDoctorId, fetched.getDoctorId());
        assertEquals("Follow-up", fetched.getReason());
    }

    @Test
    void update_shouldModifyExistingAppointment() throws Exception {
        Appointment appt = new Appointment(testPatientId, testDoctorId,
                LocalDate.now().plusDays(3), LocalTime.of(9, 0), "Initial reason", Appointment.STATUS_SCHEDULED);
        int id = appointmentDAO.create(appt);
        createdAppointmentId = id;

        Appointment toUpdate = appointmentDAO.findById(id);
        toUpdate.setStatus(Appointment.STATUS_COMPLETED);

        boolean updated = appointmentDAO.update(toUpdate);
        Appointment afterUpdate = appointmentDAO.findById(id);

        assertTrue(updated);
        assertEquals(Appointment.STATUS_COMPLETED, afterUpdate.getStatus());
    }

    @Test
    void delete_shouldRemoveAppointment() throws Exception {
        Appointment appt = new Appointment(testPatientId, testDoctorId,
                LocalDate.now().plusDays(4), LocalTime.of(11, 0), "To be deleted", Appointment.STATUS_SCHEDULED);
        int id = appointmentDAO.create(appt);

        boolean deleted = appointmentDAO.delete(id);
        Appointment afterDelete = appointmentDAO.findById(id);

        assertTrue(deleted);
        assertNull(afterDelete);

        createdAppointmentId = null;
    }

    @Test
    void findAllWithNames_shouldReturnJoinedPatientAndDoctorNames() throws Exception {
        Appointment appt = new Appointment(testPatientId, testDoctorId,
                LocalDate.now().plusDays(5), LocalTime.of(15, 30), "Consultation", Appointment.STATUS_SCHEDULED);
        int id = appointmentDAO.create(appt);
        createdAppointmentId = id;

        List<Appointment> all = appointmentDAO.findAllWithNames();

        Appointment found = all.stream().filter(a -> a.getAppointmentId() == id).findFirst().orElse(null);

        assertNotNull(found);
        assertEquals("Test Patient", found.getPatientName());
        assertEquals("Test Doctor", found.getDoctorName());
    }

    @AfterEach
    void cleanUp() throws Exception {
        if (createdAppointmentId != null) {
            appointmentDAO.delete(createdAppointmentId);
        }
        patientDAO.delete(testPatientId);
        doctorDAO.delete(testDoctorId);
        departmentDAO.delete(testDepartmentId);
    }
}