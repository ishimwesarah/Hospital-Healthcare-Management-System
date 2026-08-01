package com.hospital.ui;

import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

// Minimal shell: one window, one table, reads patients from the DB.
// No add/edit/delete from the UI yet — that's the next slice.
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        TableView<Patient> table = new TableView<>();

        TableColumn<Patient, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getPatientId()));

        TableColumn<Patient, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<Patient, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<Patient, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        TableColumn<Patient, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        table.getColumns().addAll(List.of(idCol, firstNameCol, lastNameCol, phoneCol, emailCol));

        Label statusLabel = new Label();

        try {
            PatientDAO dao = new PatientDAO();
            ObservableList<Patient> patients = FXCollections.observableArrayList(dao.findAll());
            table.setItems(patients);
            statusLabel.setText("Loaded " + patients.size() + " patient(s) from database.");
        } catch (Exception e) {
            statusLabel.setText("Failed to load patients: " + e.getMessage());
            e.printStackTrace();
        }

        VBox root = new VBox(10, statusLabel, table);
        root.setStyle("-fx-padding: 15;");

        Scene scene = new Scene(root, 700, 400);
        primaryStage.setTitle("Hospital Management System - Patients");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}