package com.hospital.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new PatientTab().build(),
                new DoctorTab().build(),
                new DepartmentTab().build(),
                new AppointmentTab().build()
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