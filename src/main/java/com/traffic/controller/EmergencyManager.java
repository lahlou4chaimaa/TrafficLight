package com.traffic.controller;

import com.traffic.db.DatabaseHandler;
import com.traffic.model.EmergencyVehicle;
import com.traffic.model.TrafficModel;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import java.util.*;

public class EmergencyManager {
    private List<EmergencyVehicle> vehicles;
    private DatabaseHandler dbHandler;
    private TrafficModel mainModel;

    public EmergencyManager(TrafficModel model) {
        this.mainModel = model;
        this.vehicles = new ArrayList<>();
        this.dbHandler = new DatabaseHandler();
        initializeSampleData();
    }

    private void initializeSampleData() {
        vehicles.add(new EmergencyVehicle("AMB-1", "AMBULANCE"));
        vehicles.add(new EmergencyVehicle("POL-1", "POLICE"));
    }

    public void startEmergency(String vehicleId) {
        EmergencyVehicle v = findVehicleById(vehicleId);
        if (v != null) {
            v.startEmergency();

            // Run UI on FX Thread
            Platform.runLater(() -> {
                List<String> choices = Arrays.asList("Main Road", "Cross Road");
                ChoiceDialog<String> dialog = new ChoiceDialog<>("Main Road", choices);
                dialog.setTitle("🚨 Emergency Override");
                dialog.setHeaderText("Vehicle " + vehicleId + " is approaching!");
                dialog.setContentText("Select incoming direction:");

                Optional<String> result = dialog.showAndWait();

                if (result.isPresent()) {
                    String dir = result.get().equals("Main Road") ? "MAIN" : "CROSS";

                    // Show confirmation
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Priority Granted: " + dir);
                    alert.show();

                    // --- FIX: Trigger Model Interrupt ---
                    if (mainModel != null) {
                        mainModel.triggerEmergency(dir);
                    }
                } else {
                    // Cancelled
                    v.stopEmergency();
                }
            });
        }
    }

    public void stopEmergency(String vehicleId) {
        EmergencyVehicle v = findVehicleById(vehicleId);
        if (v != null) v.stopEmergency();
    }

    public List<EmergencyVehicle> getVehicles() { return vehicles; }
    public void simulateVehicleMovement(String id) {}
    private EmergencyVehicle findVehicleById(String id) {
        return vehicles.stream().filter(v -> v.getId().equals(id)).findFirst().orElse(null);
    }
}