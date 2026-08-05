package com.hospital.ui;

import com.hospital.model.Department;
import com.hospital.model.Doctor;
import com.hospital.service.DoctorService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

public class DoctorTab {

    private final DoctorService doctorService = new DoctorService();
    private final AppData appData;

    private TableView<Doctor> table;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField specializationField;
    private TextField phoneField;
    private TextField emailField;
    private ComboBox<Department> departmentBox;
    private Label statusLabel;
    private Doctor selectedDoctor;

    public DoctorTab(AppData appData) {
        this.appData = appData;
    }

    public Tab build() {
        table = buildTable();
        table.setItems(appData.getDoctors()); // shared, live list

        departmentBox = new ComboBox<>();
        setupDepartmentBoxDisplay();
        departmentBox.setItems(appData.getDepartments()); // shared, live list — updates from Departments tab automatically

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) populateForm(newSel);
        });

        GridPane form = buildForm();
        HBox buttons = buildButtons();
        statusLabel = new Label();

        VBox root = new VBox(12, table, form, buttons, statusLabel);
        root.setPadding(new Insets(15));

        Tab tab = new Tab("Doctors", root);
        tab.setClosable(false);
        return tab;
    }

    private TableView<Doctor> buildTable() {
        TableView<Doctor> tv = new TableView<>();

        TableColumn<Doctor, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getDoctorId()));

        TableColumn<Doctor, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<Doctor, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<Doctor, String> specCol = new TableColumn<>("Specialization");
        specCol.setCellValueFactory(new PropertyValueFactory<>("specialization"));

        TableColumn<Doctor, Number> deptCol = new TableColumn<>("Dept ID");
        deptCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getDepartmentId()));

        tv.getColumns().addAll(List.of(idCol, firstNameCol, lastNameCol, specCol, deptCol));
        return tv;
    }

    private void setupDepartmentBoxDisplay() {
        departmentBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Department d) {
                return d == null ? "" : d.getName();
            }

            @Override
            public Department fromString(String s) {
                return null;
            }
        });
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        firstNameField = new TextField();
        lastNameField = new TextField();
        specializationField = new TextField();
        phoneField = new TextField();
        emailField = new TextField();

        grid.addRow(0, new Label("First Name:"), firstNameField, new Label("Last Name:"), lastNameField);
        grid.addRow(1, new Label("Specialization:"), specializationField, new Label("Department:"), departmentBox);
        grid.addRow(2, new Label("Phone:"), phoneField, new Label("Email:"), emailField);

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

    private void populateForm(Doctor d) {
        selectedDoctor = d;
        firstNameField.setText(d.getFirstName());
        lastNameField.setText(d.getLastName());
        specializationField.setText(d.getSpecialization());
        phoneField.setText(d.getPhoneNumber());
        emailField.setText(d.getEmail());

        departmentBox.getItems().stream()
                .filter(dept -> dept.getDepartmentId() == d.getDepartmentId())
                .findFirst()
                .ifPresent(departmentBox::setValue);
    }

    private void clearForm() {
        selectedDoctor = null;
        firstNameField.clear();
        lastNameField.clear();
        specializationField.clear();
        phoneField.clear();
        emailField.clear();
        departmentBox.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private Doctor buildDoctorFromForm() {
        Department selectedDept = departmentBox.getValue();
        int deptId = selectedDept != null ? selectedDept.getDepartmentId() : 0;

        return new Doctor(
                firstNameField.getText(),
                lastNameField.getText(),
                specializationField.getText(),
                phoneField.getText(),
                emailField.getText(),
                deptId
        );
    }

    private void handleAdd() {
        try {
            int id = doctorService.addDoctor(buildDoctorFromForm());
            showSuccess("Doctor added with ID " + id + ".");
            clearForm();
            appData.refreshDoctors();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to add doctor: " + e.getMessage());
        }
    }

    private void handleUpdate() {
        if (selectedDoctor == null) {
            showError("Select a doctor from the table before updating.");
            return;
        }
        try {
            Doctor updated = buildDoctorFromForm();
            updated.setDoctorId(selectedDoctor.getDoctorId());
            if (doctorService.updateDoctor(updated)) {
                showSuccess("Doctor updated successfully.");
                clearForm();
                appData.refreshDoctors();
            } else {
                showError("Update failed: doctor not found.");
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to update doctor: " + e.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedDoctor == null) {
            showError("Select a doctor from the table before deleting.");
            return;
        }
        try {
            if (doctorService.deleteDoctor(selectedDoctor.getDoctorId())) {
                showSuccess("Doctor deleted.");
                clearForm();
                appData.refreshDoctors();
            } else {
                showError("Delete failed: doctor not found.");
            }
        } catch (Exception e) {
            showError("Failed to delete doctor: " + e.getMessage());
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