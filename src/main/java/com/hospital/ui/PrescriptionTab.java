package com.hospital.ui;

import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionItem;
import com.hospital.service.PrescriptionService;
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
import java.util.ArrayList;
import java.util.List;

public class PrescriptionTab {

    private final PrescriptionService prescriptionService = new PrescriptionService();
    private final AppData appData;

    private TableView<Prescription> prescriptionTable;
    private TableView<PrescriptionItem> itemTable;
    private final ObservableList<PrescriptionItem> currentItems = FXCollections.observableArrayList();

    private ComboBox<Patient> patientBox;
    private ComboBox<Doctor> doctorBox;
    private DatePicker datePicker;
    private TextField notesField;

    private TextField medicationField;
    private TextField dosageField;
    private TextField frequencyField;
    private TextField durationField;
    private TextField quantityField;

    private Label statusLabel;
    private Prescription selectedPrescription;

    public PrescriptionTab(AppData appData) {
        this.appData = appData;
    }

    public Tab build() {
        prescriptionTable = buildPrescriptionTable();
        itemTable = buildItemTable();
        patientBox = new ComboBox<>();
        doctorBox = new ComboBox<>();
        setupComboDisplays();

        patientBox.setItems(appData.getPatients()); // shared, live list
        doctorBox.setItems(appData.getDoctors());   // shared, live list

        loadPrescriptions(); // prescriptions themselves aren't shared with any other tab

        prescriptionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) populateForm(newSel);
        });

        GridPane prescriptionForm = buildPrescriptionForm();
        GridPane itemForm = buildItemForm();
        HBox itemButtons = buildItemButtons();
        HBox mainButtons = buildMainButtons();
        statusLabel = new Label();

        VBox root = new VBox(10,
                new Label("Prescriptions"), prescriptionTable, prescriptionForm,
                new Separator(),
                new Label("Medication Items for this Prescription"), itemTable, itemForm, itemButtons,
                new Separator(),
                mainButtons, statusLabel
        );
        root.setPadding(new Insets(15));

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);

        Tab tab = new Tab("Prescriptions", scrollPane);
        tab.setClosable(false);
        return tab;
    }

    private TableView<Prescription> buildPrescriptionTable() {
        TableView<Prescription> tv = new TableView<>();

        TableColumn<Prescription, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPrescriptionId()));

        TableColumn<Prescription, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatientName()));

        TableColumn<Prescription, String> doctorCol = new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDoctorName()));

        TableColumn<Prescription, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getDatePrescribed())));

        TableColumn<Prescription, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNotes()));

        tv.getColumns().addAll(List.of(idCol, patientCol, doctorCol, dateCol, notesCol));
        tv.setPrefHeight(180);
        return tv;
    }

    private TableView<PrescriptionItem> buildItemTable() {
        TableView<PrescriptionItem> tv = new TableView<>(currentItems);

        TableColumn<PrescriptionItem, String> medCol = new TableColumn<>("Medication");
        medCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMedicationName()));

        TableColumn<PrescriptionItem, String> dosageCol = new TableColumn<>("Dosage");
        dosageCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDosage()));

        TableColumn<PrescriptionItem, String> freqCol = new TableColumn<>("Frequency");
        freqCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFrequency()));

        TableColumn<PrescriptionItem, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDuration()));

        TableColumn<PrescriptionItem, Number> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()));

        tv.getColumns().addAll(List.of(medCol, dosageCol, freqCol, durationCol, qtyCol));
        tv.setPrefHeight(120);
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

    private GridPane buildPrescriptionForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        datePicker = new DatePicker(LocalDate.now());
        notesField = new TextField();

        grid.addRow(0, new Label("Patient:"), patientBox, new Label("Doctor:"), doctorBox);
        grid.addRow(1, new Label("Date:"), datePicker, new Label("Notes:"), notesField);

        return grid;
    }

    private GridPane buildItemForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        medicationField = new TextField();
        medicationField.setPromptText("Medication name");
        dosageField = new TextField();
        dosageField.setPromptText("e.g. 500mg");
        frequencyField = new TextField();
        frequencyField.setPromptText("e.g. Twice daily");
        durationField = new TextField();
        durationField.setPromptText("e.g. 7 days");
        quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        grid.addRow(0, new Label("Medication:"), medicationField, new Label("Dosage:"), dosageField);
        grid.addRow(1, new Label("Frequency:"), frequencyField, new Label("Duration:"), durationField);
        grid.addRow(2, new Label("Quantity:"), quantityField);

        return grid;
    }

    private HBox buildItemButtons() {
        Button addItemBtn = new Button("Add Item to List");
        Button removeItemBtn = new Button("Remove Selected Item");

        addItemBtn.setOnAction(e -> handleAddItem());
        removeItemBtn.setOnAction(e -> handleRemoveItem());

        return new HBox(10, addItemBtn, removeItemBtn);
    }

    private HBox buildMainButtons() {
        Button saveBtn = new Button("Save New Prescription");
        Button updateBtn = new Button("Update Selected Prescription");
        Button deleteBtn = new Button("Delete Selected Prescription");
        Button clearBtn = new Button("Clear");

        saveBtn.setOnAction(e -> handleSave());
        updateBtn.setOnAction(e -> handleUpdate());
        deleteBtn.setOnAction(e -> handleDelete());
        clearBtn.setOnAction(e -> clearForm());

        return new HBox(10, saveBtn, updateBtn, deleteBtn, clearBtn);
    }

    private void loadPrescriptions() {
        try {
            prescriptionTable.setItems(FXCollections.observableArrayList(prescriptionService.getAllPrescriptions()));
        } catch (Exception e) {
            showError("Failed to load prescriptions: " + e.getMessage());
        }
    }

    private void handleAddItem() {
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            PrescriptionItem item = new PrescriptionItem(
                    medicationField.getText(), dosageField.getText(),
                    frequencyField.getText(), durationField.getText(), quantity);

            if (item.getMedicationName() == null || item.getMedicationName().trim().isEmpty()) {
                showError("Medication name is required before adding an item.");
                return;
            }

            currentItems.add(item);
            medicationField.clear();
            dosageField.clear();
            frequencyField.clear();
            durationField.clear();
            quantityField.clear();
            showSuccess("Item added to the list below. Click 'Save' once all items are added.");
        } catch (NumberFormatException e) {
            showError("Quantity must be a whole number.");
        }
    }

    private void handleRemoveItem() {
        PrescriptionItem selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an item from the list to remove it.");
            return;
        }
        currentItems.remove(selected);
    }

    private void populateForm(Prescription prescription) {
        try {
            Prescription full = prescriptionService.getPrescriptionWithItems(prescription.getPrescriptionId());
            selectedPrescription = full;

            patientBox.getItems().stream()
                    .filter(p -> p.getPatientId() == full.getPatientId())
                    .findFirst().ifPresent(patientBox::setValue);

            doctorBox.getItems().stream()
                    .filter(d -> d.getDoctorId() == full.getDoctorId())
                    .findFirst().ifPresent(doctorBox::setValue);

            datePicker.setValue(full.getDatePrescribed());
            notesField.setText(full.getNotes());

            currentItems.setAll(full.getItems());
        } catch (Exception e) {
            showError("Failed to load prescription details: " + e.getMessage());
        }
    }

    private void clearForm() {
        selectedPrescription = null;
        patientBox.setValue(null);
        doctorBox.setValue(null);
        datePicker.setValue(LocalDate.now());
        notesField.clear();
        currentItems.clear();
        prescriptionTable.getSelectionModel().clearSelection();
    }

    private Prescription buildPrescriptionFromForm() {
        Patient p = patientBox.getValue();
        Doctor d = doctorBox.getValue();

        Prescription prescription = new Prescription(
                p != null ? p.getPatientId() : 0,
                d != null ? d.getDoctorId() : 0,
                datePicker.getValue(),
                notesField.getText()
        );
        prescription.setItems(new ArrayList<>(currentItems));
        return prescription;
    }

    private void handleSave() {
        try {
            int id = prescriptionService.addPrescriptionWithItems(buildPrescriptionFromForm());
            showSuccess("Prescription saved with ID " + id + " (" + currentItems.size() + " item(s)).");
            clearForm();
            loadPrescriptions();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to save prescription: " + e.getMessage());
        }
    }

    private void handleUpdate() {
        if (selectedPrescription == null) {
            showError("Select a prescription from the table before updating.");
            return;
        }
        try {
            Prescription updated = buildPrescriptionFromForm();
            updated.setPrescriptionId(selectedPrescription.getPrescriptionId());
            if (prescriptionService.updatePrescriptionWithItems(updated)) {
                showSuccess("Prescription updated successfully.");
                clearForm();
                loadPrescriptions();
            } else {
                showError("Update failed: prescription not found.");
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to update prescription: " + e.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedPrescription == null) {
            showError("Select a prescription from the table before deleting.");
            return;
        }
        try {
            if (prescriptionService.deletePrescription(selectedPrescription.getPrescriptionId())) {
                showSuccess("Prescription and its items deleted.");
                clearForm();
                loadPrescriptions();
            } else {
                showError("Delete failed: prescription not found.");
            }
        } catch (Exception e) {
            showError("Failed to delete prescription: " + e.getMessage());
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