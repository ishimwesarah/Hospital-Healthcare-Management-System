package com.hospital.ui;

import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

public class PatientTab {

    private final PatientService patientService = new PatientService();
    private final AppData appData;

    private TableView<Patient> table;
    private TextField searchField;
    private TextField firstNameField;
    private TextField lastNameField;
    private DatePicker dobPicker;
    private ComboBox<String> genderBox;
    private TextField phoneField;
    private TextField emailField;
    private TextField addressField;
    private Label statusLabel;
    private Patient selectedPatient;

    public PatientTab(AppData appData) {
        this.appData = appData;
    }

    public Tab build() {
        table = buildTable();
        table.setItems(appData.getPatients()); // shared, live list — updates automatically from ANY tab

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) populateForm(newSel);
        });

        searchField = new TextField();
        searchField.setPromptText("Search by name or ID...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch(newVal));
        HBox searchBar = new HBox(10, new Label("Search:"), searchField);

        GridPane form = buildForm();
        HBox buttons = buildButtons();
        statusLabel = new Label();

        VBox root = new VBox(12, searchBar, table, form, buttons, statusLabel);
        root.setPadding(new Insets(15));

        Tab tab = new Tab("Patients", root);
        tab.setClosable(false);
        return tab;
    }

    private TableView<Patient> buildTable() {
        TableView<Patient> tv = new TableView<>();

        TableColumn<Patient, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPatientId()));

        TableColumn<Patient, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<Patient, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<Patient, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        TableColumn<Patient, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        tv.getColumns().addAll(List.of(idCol, firstNameCol, lastNameCol, phoneCol, emailCol));
        return tv;
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        firstNameField = new TextField();
        lastNameField = new TextField();
        dobPicker = new DatePicker();
        genderBox = new ComboBox<>(FXCollections.observableArrayList("Male", "Female", "Other"));
        phoneField = new TextField();
        emailField = new TextField();
        addressField = new TextField();

        grid.addRow(0, new Label("First Name:"), firstNameField, new Label("Last Name:"), lastNameField);
        grid.addRow(1, new Label("Date of Birth:"), dobPicker, new Label("Gender:"), genderBox);
        grid.addRow(2, new Label("Phone:"), phoneField, new Label("Email:"), emailField);
        grid.addRow(3, new Label("Address:"), addressField);

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

    // Search bypasses the shared list — filtered results are temporary, so we show a fresh
    // one-off list on the table without disturbing appData.getPatients() itself.
    // Clearing the search box restores the live shared list.
    private void handleSearch(String term) {
        try {
            if (term == null || term.trim().isEmpty()) {
                table.setItems(appData.getPatients());
            } else {
                table.setItems(FXCollections.observableArrayList(patientService.searchPatients(term)));
            }
        } catch (Exception e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    private void populateForm(Patient p) {
        selectedPatient = p;
        firstNameField.setText(p.getFirstName());
        lastNameField.setText(p.getLastName());
        dobPicker.setValue(p.getDateOfBirth());
        genderBox.setValue(p.getGender());
        phoneField.setText(p.getPhoneNumber());
        emailField.setText(p.getEmail());
        addressField.setText(p.getAddress());
    }

    private void clearForm() {
        selectedPatient = null;
        firstNameField.clear();
        lastNameField.clear();
        dobPicker.setValue(null);
        genderBox.setValue(null);
        phoneField.clear();
        emailField.clear();
        addressField.clear();
        table.getSelectionModel().clearSelection();
    }

    private Patient buildPatientFromForm() {
        return new Patient(
                firstNameField.getText(),
                lastNameField.getText(),
                dobPicker.getValue(),
                genderBox.getValue(),
                phoneField.getText(),
                emailField.getText(),
                addressField.getText()
        );
    }

    private void handleAdd() {
        try {
            int id = patientService.addPatient(buildPatientFromForm());
            showSuccess("Patient added with ID " + id + ".");
            clearForm();
            appData.refreshPatients(); // updates the SHARED list -> every tab using it sees this instantly
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to add patient: " + e.getMessage());
        }
    }

    private void handleUpdate() {
        if (selectedPatient == null) {
            showError("Select a patient from the table before updating.");
            return;
        }
        try {
            Patient updated = buildPatientFromForm();
            updated.setPatientId(selectedPatient.getPatientId());
            if (patientService.updatePatient(updated)) {
                showSuccess("Patient updated successfully.");
                clearForm();
                appData.refreshPatients();
            } else {
                showError("Update failed: patient not found.");
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to update patient: " + e.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedPatient == null) {
            showError("Select a patient from the table before deleting.");
            return;
        }
        try {
            if (patientService.deletePatient(selectedPatient.getPatientId())) {
                showSuccess("Patient deleted.");
                clearForm();
                appData.refreshPatients();
            } else {
                showError("Delete failed: patient not found.");
            }
        } catch (Exception e) {
            showError("Failed to delete patient: " + e.getMessage());
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