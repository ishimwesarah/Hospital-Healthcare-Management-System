package com.hospital.ui;

import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.PatientFeedback;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientFeedbackService;
import com.hospital.service.PatientService;
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

import java.time.LocalDate;
import java.util.List;

public class FeedbackTab {

    private final PatientFeedbackService feedbackService = new PatientFeedbackService();
    private final PatientService patientService = new PatientService();
    private final DoctorService doctorService = new DoctorService();

    private TableView<PatientFeedback> table;
    private ComboBox<Patient> patientBox;
    private ComboBox<Doctor> doctorBox; // optional — user may leave unselected for general feedback
    private ComboBox<Integer> ratingBox;
    private TextField commentsField;
    private DatePicker datePicker;
    private Label statusLabel;
    private PatientFeedback selectedFeedback;

    public Tab build() {
        table = buildTable();
        patientBox = new ComboBox<>();
        doctorBox = new ComboBox<>();
        setupComboDisplays();
        loadPatientsIntoBox();
        loadDoctorsIntoBox();
        loadFeedback();

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) populateForm(newSel);
        });

        GridPane form = buildForm();
        HBox buttons = buildButtons();
        statusLabel = new Label();

        VBox root = new VBox(12, table, form, buttons, statusLabel);
        root.setPadding(new Insets(15));

        Tab tab = new Tab("Feedback", root);
        tab.setClosable(false);
        return tab;
    }

    private TableView<PatientFeedback> buildTable() {
        TableView<PatientFeedback> tv = new TableView<>();

        TableColumn<PatientFeedback, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getFeedbackId()));

        TableColumn<PatientFeedback, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatientName()));

        TableColumn<PatientFeedback, String> doctorCol = new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDoctorName()));

        TableColumn<PatientFeedback, Number> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRating()));

        TableColumn<PatientFeedback, String> commentsCol = new TableColumn<>("Comments");
        commentsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getComments()));

        TableColumn<PatientFeedback, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getFeedbackDate())));

        tv.getColumns().addAll(List.of(idCol, patientCol, doctorCol, ratingCol, commentsCol, dateCol));
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
                return d == null ? "(General feedback, no doctor)" : "Dr. " + d.getFirstName() + " " + d.getLastName();
            }

            @Override
            public Doctor fromString(String s) {
                return null;
            }
        });
    }

    private void loadPatientsIntoBox() {
        try {
            patientBox.setItems(FXCollections.observableArrayList(patientService.getAllPatients()));
        } catch (Exception e) {
            showError("Failed to load patients: " + e.getMessage());
        }
    }

    private void loadDoctorsIntoBox() {
        try {
            doctorBox.setItems(FXCollections.observableArrayList(doctorService.getAllDoctors()));
        } catch (Exception e) {
            showError("Failed to load doctors: " + e.getMessage());
        }
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        ratingBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        commentsField = new TextField();
        datePicker = new DatePicker(LocalDate.now());

        grid.addRow(0, new Label("Patient:"), patientBox, new Label("Doctor (optional):"), doctorBox);
        grid.addRow(1, new Label("Rating (1-5):"), ratingBox, new Label("Date:"), datePicker);
        grid.addRow(2, new Label("Comments:"), commentsField);

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

    private void loadFeedback() {
        try {
            table.setItems(FXCollections.observableArrayList(feedbackService.getAllFeedback()));
        } catch (Exception e) {
            showError("Failed to load feedback: " + e.getMessage());
        }
    }

    private void populateForm(PatientFeedback fb) {
        selectedFeedback = fb;

        patientBox.getItems().stream()
                .filter(p -> p.getPatientId() == fb.getPatientId())
                .findFirst().ifPresent(patientBox::setValue);

        if (fb.getDoctorId() != null) {
            doctorBox.getItems().stream()
                    .filter(d -> d.getDoctorId() == fb.getDoctorId())
                    .findFirst().ifPresent(doctorBox::setValue);
        } else {
            doctorBox.setValue(null);
        }

        ratingBox.setValue(fb.getRating());
        commentsField.setText(fb.getComments());
        datePicker.setValue(fb.getFeedbackDate());
    }

    private void clearForm() {
        selectedFeedback = null;
        patientBox.setValue(null);
        doctorBox.setValue(null);
        ratingBox.setValue(null);
        commentsField.clear();
        datePicker.setValue(LocalDate.now());
        table.getSelectionModel().clearSelection();
    }

    private PatientFeedback buildFeedbackFromForm() {
        Patient p = patientBox.getValue();
        Doctor d = doctorBox.getValue();

        return new PatientFeedback(
                p != null ? p.getPatientId() : 0,
                d != null ? d.getDoctorId() : null,
                ratingBox.getValue() != null ? ratingBox.getValue() : 0,
                commentsField.getText(),
                datePicker.getValue()
        );
    }

    private void handleAdd() {
        try {
            int id = feedbackService.addFeedback(buildFeedbackFromForm());
            showSuccess("Feedback added with ID " + id + ".");
            clearForm();
            loadFeedback();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to add feedback: " + e.getMessage());
        }
    }

    private void handleUpdate() {
        if (selectedFeedback == null) {
            showError("Select a feedback entry from the table before updating.");
            return;
        }
        try {
            PatientFeedback updated = buildFeedbackFromForm();
            updated.setFeedbackId(selectedFeedback.getFeedbackId());
            if (feedbackService.updateFeedback(updated)) {
                showSuccess("Feedback updated successfully.");
                clearForm();
                loadFeedback();
            } else {
                showError("Update failed: feedback not found.");
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to update feedback: " + e.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedFeedback == null) {
            showError("Select a feedback entry from the table before deleting.");
            return;
        }
        try {
            if (feedbackService.deleteFeedback(selectedFeedback.getFeedbackId())) {
                showSuccess("Feedback deleted.");
                clearForm();
                loadFeedback();
            } else {
                showError("Delete failed: feedback not found.");
            }
        } catch (Exception e) {
            showError("Failed to delete feedback: " + e.getMessage());
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