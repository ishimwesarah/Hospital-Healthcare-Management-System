package com.hospital.ui;

import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.DepartmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;

/**
 * Shared, live data store for entities referenced by more than one tab
 * (e.g. Patients and Doctors are needed by Appointments, Prescriptions, and Feedback tabs
 * as dropdown options; Departments are needed by the Doctors tab).
 *
 * Each ObservableList here is created ONCE, in this class, and handed out BY REFERENCE to
 * every tab that needs it (via getPatients(), getDoctors(), getDepartments()). Because
 * JavaFX's TableView and ComboBox automatically listen for changes on whatever ObservableList
 * they're given via setItems(...), calling refreshPatients() (etc.) here — which updates the
 * SAME list object's contents via setAll(...) — causes every UI control bound to it to update
 * immediately, in every tab, with no manual reload or app restart needed.
 */
public class AppData {

    private final PatientService patientService = new PatientService();
    private final DoctorService doctorService = new DoctorService();
    private final DepartmentService departmentService = new DepartmentService();

    private final ObservableList<Patient> patients = FXCollections.observableArrayList();
    private final ObservableList<Doctor> doctors = FXCollections.observableArrayList();
    private final ObservableList<Department> departments = FXCollections.observableArrayList();

    public ObservableList<Patient> getPatients() {
        return patients;
    }

    public ObservableList<Doctor> getDoctors() {
        return doctors;
    }

    public ObservableList<Department> getDepartments() {
        return departments;
    }

    // setAll() replaces the CONTENTS of the list in place, rather than creating a new list —
    // this is what lets every tab already pointing at this list see the update automatically.
    public void refreshPatients() throws SQLException {
        patients.setAll(patientService.getAllPatients());
    }

    public void refreshDoctors() throws SQLException {
        doctors.setAll(doctorService.getAllDoctors());
    }

    public void refreshDepartments() throws SQLException {
        departments.setAll(departmentService.getAllDepartments());
    }

    // Called once at app startup to populate everything before any tab is built.
    public void refreshAll() throws SQLException {
        refreshPatients();
        refreshDoctors();
        refreshDepartments();
    }
}