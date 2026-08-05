package com.hospital.ui;

import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AppointmentTab {

    private final AppointmentService appointmentService = new AppointmentService();
    private final AppData appData;

    private TableView<Appointment> table;
    private ComboBox<Patient> patientBox;
    private ComboBox<Doctor> doctorBox;
    private DatePicker datePicker;
    private TextField timeField;
    private TextField reasonField;
    private ComboBox<String> statusBox;
    private Label statusLabel;
    private Appointment selectedAppointment;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public AppointmentTab(AppData appData) {
        this.appData = appData;
    }

    public Tab build() {
        table = buildTable();
        patientBox = new ComboBox<>();
        doctorBox = new ComboBox<>();
        setupComboDisplays();

        patientBox.setItems(appData.getPatients()); // shared, live list
        doctorBox.setItems(appData.getDoctors());   // shared, live list

        loadAppointments(); // appointments themselves aren't shared with any other tab

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) populateForm(newSel);
        });

        GridPane form = buildForm();
        HBox buttons = buildButtons();
        statusLabel = new Label();

        VBox root = new VBox(12, table, form, buttons, statusLabel);
        root.setPadding(new Insets(15));

        Tab tab = new Tab("Appointments", root);
        tab.setClosable(false);
        return tab;
    }

    private TableView<Appointment> buildTable() {
        TableView<Appointment> tv = new TableView<>();

        TableColumn<Appointment, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getAppointmentId()));

        TableColumn<Appointment, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatientName()));

        TableColumn<Appointment, String> doctorCol = new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDoctorName()));

        TableColumn<Appointment, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getAppointmentDate())));

        TableColumn<Appointment, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getAppointmentTime().format(TIME_FORMAT)));

        TableColumn<Appointment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        tv.getColumns().addAll(List.of(idCol, patientCol, doctorCol, dateCol, timeCol, statusCol));
        return tv;
    }

    private void setupComboDisplays() {
        patientBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Patient p) {
                return p == null ? "" : p.getFirstName() + " " + p.getLastName();
            }

            @Override
            public Patient fromString(String s) {
                return null;
            }
        });

        doctorBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Doctor d) {
                return d == null ? "" : "Dr. " + d.getFirstName() + " " + d.getLastName();
            }

            @Override
            public Doctor fromString(String s) {
                return null;
            }
        });
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        datePicker = new DatePicker();
        timeField = new TextField();
        timeField.setPromptText("HH:mm e.g. 14:30");
        reasonField = new TextField();
        statusBox = new ComboBox<>(FXCollections.observableArrayList(
                Appointment.STATUS_SCHEDULED, Appointment.STATUS_COMPLETED, Appointment.STATUS_CANCELLED));

        grid.addRow(0, new Label("Patient:"), patientBox, new Label("Doctor:"), doctorBox);
        grid.addRow(1, new Label("Date:"), datePicker, new Label("Time:"), timeField);
        grid.addRow(2, new Label("Reason:"), reasonField, new Label("Status:"), statusBox);

        return grid;
    }

    private HBox buildButtons() {
        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");

        addBtn.setOnAction(e -> handleAdd());
        updateBtn.setOnAction(e -> handleUpdate());
        deleteBtn.setOnAction(e -> handleDelete());
        clearBtn.setOnAction(e -> clearForm());

        return new HBox(10, addBtn, updateBtn, deleteBtn, clearBtn);
    }

    private void loadAppointments() {
        try {
            ObservableList<Appointment> list = FXCollections.observableArrayList(appointmentService.getAllAppointments());
            table.setItems(list);
        } catch (Exception e) {
            showError("Failed to load appointments: " + e.getMessage());
        }
    }

    private void populateForm(Appointment appt) {
        selectedAppointment = appt;

        patientBox.getItems().stream()
                .filter(p -> p.getPatientId() == appt.getPatientId())
                .findFirst().ifPresent(patientBox::setValue);

        doctorBox.getItems().stream()
                .filter(d -> d.getDoctorId() == appt.getDoctorId())
                .findFirst().ifPresent(doctorBox::setValue);

        datePicker.setValue(appt.getAppointmentDate());
        timeField.setText(appt.getAppointmentTime().format(TIME_FORMAT));
        reasonField.setText(appt.getReason());
        statusBox.setValue(appt.getStatus());
    }

    private void clearForm() {
        selectedAppointment = null;
        patientBox.setValue(null);
        doctorBox.setValue(null);
        datePicker.setValue(null);
        timeField.clear();
        reasonField.clear();
        statusBox.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private Appointment buildAppointmentFromForm() {
        Patient p = patientBox.getValue();
        Doctor d = doctorBox.getValue();

        LocalTime time;
        try {
            time = LocalTime.parse(timeField.getText().trim(), TIME_FORMAT);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Time must be in HH:mm format, e.g. 14:30.");
        }

        return new Appointment(
                p != null ? p.getPatientId() : 0,
                d != null ? d.getDoctorId() : 0,
                datePicker.getValue(),
                time,
                reasonField.getText(),
                statusBox.getValue()
        );
    }

    private void handleAdd() {
        try {
            int id = appointmentService.addAppointment(buildAppointmentFromForm());
            showSuccess("Appointment added with ID " + id + ".");
            clearForm();
            loadAppointments();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to add appointment: " + e.getMessage());
        }
    }

    private void handleUpdate() {
        if (selectedAppointment == null) {
            showError("Select an appointment from the table before updating.");
            return;
        }
        try {
            Appointment updated = buildAppointmentFromForm();
            updated.setAppointmentId(selectedAppointment.getAppointmentId());
            if (appointmentService.updateAppointment(updated)) {
                showSuccess("Appointment updated successfully.");
                clearForm();
                loadAppointments();
            } else {
                showError("Update failed: appointment not found.");
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to update appointment: " + e.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedAppointment == null) {
            showError("Select an appointment from the table before deleting.");
            return;
        }
        try {
            if (appointmentService.deleteAppointment(selectedAppointment.getAppointmentId())) {
                showSuccess("Appointment deleted.");
                clearForm();
                loadAppointments();
            } else {
                showError("Delete failed: appointment not found.");
            }
        } catch (Exception e) {
            showError("Failed to delete appointment: " + e.getMessage());
        }
    }

    private void showSuccess(String message) {
        statusLabel.setTextFill(Color.GREEN);
        statusLabel.setText(message);
    }

    private void showError(String message) {
        statusLabel.setTextFill(Color.RED);
        statusLabel.setText(message);
    }
}