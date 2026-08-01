package com.hospital.ui;

import com.hospital.model.MedicalInventory;
import com.hospital.service.MedicalInventoryService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.util.List;

public class InventoryTab {

    private final MedicalInventoryService inventoryService = new MedicalInventoryService();

    private TableView<MedicalInventory> table;
    private TextField itemNameField;
    private TextField quantityField;
    private TextField unitField;
    private TextField reorderLevelField;
    private DatePicker lastRestockedPicker;
    private Label statusLabel;
    private MedicalInventory selectedItem;

    public Tab build() {
        table = buildTable();
        loadInventory();

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) populateForm(newSel);
        });

        GridPane form = buildForm();
        HBox buttons = buildButtons();
        statusLabel = new Label();

        VBox root = new VBox(12, table, form, buttons, statusLabel);
        root.setPadding(new Insets(15));

        Tab tab = new Tab("Inventory", root);
        tab.setClosable(false);
        return tab;
    }

    private TableView<MedicalInventory> buildTable() {
        TableView<MedicalInventory> tv = new TableView<>();

        TableColumn<MedicalInventory, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getInventoryId()));

        TableColumn<MedicalInventory, String> nameCol = new TableColumn<>("Item");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        TableColumn<MedicalInventory, Number> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()));

        TableColumn<MedicalInventory, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<MedicalInventory, Number> reorderCol = new TableColumn<>("Reorder Level");
        reorderCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getReorderLevel()));

        TableColumn<MedicalInventory, String> restockedCol = new TableColumn<>("Last Restocked");
        restockedCol.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getLastRestocked())));

        TableColumn<MedicalInventory, Boolean> lowStockCol = new TableColumn<>("Low Stock?");
        lowStockCol.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isBelowReorderLevel()));

        tv.getColumns().addAll(List.of(idCol, nameCol, qtyCol, unitCol, reorderCol, restockedCol, lowStockCol));
        return tv;
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        itemNameField = new TextField();
        quantityField = new TextField();
        unitField = new TextField();
        unitField.setPromptText("e.g. boxes, units, ml");
        reorderLevelField = new TextField();
        reorderLevelField.setPromptText("e.g. 10");
        lastRestockedPicker = new DatePicker(LocalDate.now());

        grid.addRow(0, new Label("Item Name:"), itemNameField, new Label("Quantity:"), quantityField);
        grid.addRow(1, new Label("Unit:"), unitField, new Label("Reorder Level:"), reorderLevelField);
        grid.addRow(2, new Label("Last Restocked:"), lastRestockedPicker);

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

    private void loadInventory() {
        try {
            table.setItems(FXCollections.observableArrayList(inventoryService.getAllInventory()));
        } catch (Exception e) {
            showError("Failed to load inventory: " + e.getMessage());
        }
    }

    private void populateForm(MedicalInventory item) {
        selectedItem = item;
        itemNameField.setText(item.getItemName());
        quantityField.setText(String.valueOf(item.getQuantity()));
        unitField.setText(item.getUnit());
        reorderLevelField.setText(String.valueOf(item.getReorderLevel()));
        lastRestockedPicker.setValue(item.getLastRestocked());
    }

    private void clearForm() {
        selectedItem = null;
        itemNameField.clear();
        quantityField.clear();
        unitField.clear();
        reorderLevelField.clear();
        lastRestockedPicker.setValue(LocalDate.now());
        table.getSelectionModel().clearSelection();
    }

    private MedicalInventory buildInventoryFromForm() {
        int quantity;
        int reorderLevel;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Quantity must be a whole number.");
        }
        try {
            reorderLevel = reorderLevelField.getText().trim().isEmpty() ? 10 : Integer.parseInt(reorderLevelField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Reorder level must be a whole number.");
        }

        return new MedicalInventory(
                itemNameField.getText(),
                quantity,
                unitField.getText(),
                reorderLevel,
                lastRestockedPicker.getValue()
        );
    }

    private void handleAdd() {
        try {
            int id = inventoryService.addInventoryItem(buildInventoryFromForm());
            showSuccess("Inventory item added with ID " + id + ".");
            clearForm();
            loadInventory();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to add inventory item: " + e.getMessage());
        }
    }

    private void handleUpdate() {
        if (selectedItem == null) {
            showError("Select an item from the table before updating.");
            return;
        }
        try {
            MedicalInventory updated = buildInventoryFromForm();
            updated.setInventoryId(selectedItem.getInventoryId());
            if (inventoryService.updateInventoryItem(updated)) {
                showSuccess("Inventory item updated successfully.");
                clearForm();
                loadInventory();
            } else {
                showError("Update failed: item not found.");
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Failed to update inventory item: " + e.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedItem == null) {
            showError("Select an item from the table before deleting.");
            return;
        }
        try {
            if (inventoryService.deleteInventoryItem(selectedItem.getInventoryId())) {
                showSuccess("Inventory item deleted.");
                clearForm();
                loadInventory();
            } else {
                showError("Delete failed: item not found.");
            }
        } catch (Exception e) {
            showError("Failed to delete inventory item: " + e.getMessage());
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