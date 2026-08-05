package com.hospital.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppData appData = new AppData();
        appData.refreshAll(); // populate shared Patients/Doctors/Departments lists once, up front

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new PatientTab(appData).build(),
                new DoctorTab(appData).build(),
                new DepartmentTab(appData).build(),
                new AppointmentTab(appData).build(),
                new PrescriptionTab(appData).build(),
                new FeedbackTab(appData).build(),
                new InventoryTab().build() // no cross-tab shared data needed here
        );

        Scene scene = new Scene(tabPane, 850, 650);
        primaryStage.setTitle("Hospital Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}