package com.hospital.ui;

import com.hospital.model.Department;
import com.hospital.service.DepartmentService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

public class DepartmentTab {

    private final DepartmentService departmentService = new DepartmentService();
    private final AppData appData;

    private TableView<Department> table;
    private TextField nameField;
    private TextField locationField;
    private TextField phoneField;
    private Label statusLabel;
    private Department selectedDepartment;

    public DepartmentTab(AppData appData) {
        this.appData = appData;
    }

    public Tab build() {
        table = buildTable();
        table.setItems(appData.getDepartments()); // shared, live list

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) populateForm(newSel);
        });

        GridPane form = buildForm();
        HBox buttons = buildButtons();
        statusLabel = new Label();

        VBox root = new VBox(12, table, form, buttons, statusLabel);
        root.setPadding(new Insets(15));

        Tab tab = new Tab("Departments", root);
        tab.setClosable(false);
        return tab;
    }

    private TableView<Department> buildTable() {
        TableView<Department> tv = new TableView<>();

        TableColumn<Department, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getDepartmentId()));

        TableColumn<Department, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Department, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Department, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        tv.getColumns().addAll(List.of(idCol, nameCol, locationCol, phoneCol));
        return tv;
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        nameField = new TextField();
        locationField = new TextField();
        phoneField = new TextField();

        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Location:"), locationField);
        grid.addRow(2, new Label("Phone:"), phoneField);

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

    private void populateForm(Department d) {
        selectedDepartment = d;
        nameField.setText(d.getName());
        locationField.setText(d.getLocation());
        phoneField.setText(d.getPhoneNumber());
    }

    private void clearForm() {
        selectedDepartment = null;
        nameField.clear();
        locationField.clear();
        phoneField.clear();
        table.getSelectionModel().clearSelection();
    }

    private Department buildDepartmentFromForm() {
        return new Department(nameField.getText(), locationField.getText(), phoneField.getText());
    }

    private void handleAdd() {
        try {
            int id = departmentService.addDepartment(buildDepartmentFromForm());
            showSuccess("Department added with ID " + id + ".");
            clearForm();
            appData.refreshDepartments(); // Doctor tab's dropdown updates instantly too — same shared list
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to add department: " + e.getMessage());
        }
    }

    private void handleUpdate() {
        if (selectedDepartment == null) {
            showError("Select a department from the table before updating.");
            return;
        }
        try {
            Department updated = buildDepartmentFromForm();
            updated.setDepartmentId(selectedDepartment.getDepartmentId());
            if (departmentService.updateDepartment(updated)) {
                showSuccess("Department updated successfully.");
                clearForm();
                appData.refreshDepartments();
            } else {
                showError("Update failed: department not found.");
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to update department: " + e.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedDepartment == null) {
            showError("Select a department from the table before deleting.");
            return;
        }
        try {
            if (departmentService.deleteDepartment(selectedDepartment.getDepartmentId())) {
                showSuccess("Department deleted.");
                clearForm();
                appData.refreshDepartments();
            } else {
                showError("Delete failed: department not found.");
            }
        } catch (Exception e) {
            showError("Failed to delete department: " + e.getMessage());
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