package com.traffic.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TrafficLight {
    private String id;
    private StringProperty state; // "RED", "GREEN", "EMERGENCY_GREEN"
    private boolean emergencyOverride;
    private String emergencyVehicleId;

    public TrafficLight(String id) {
        this.id = id;
        this.state = new SimpleStringProperty("RED");
        this.emergencyOverride = false;
        this.emergencyVehicleId = null;
    }

    public void activateEmergencyGreen(String vehicleId) {
        this.state.set("EMERGENCY_GREEN");
        this.emergencyOverride = true;
        this.emergencyVehicleId = vehicleId;
        System.out.println("Feu " + id + ": VERT URGENCE pour " + vehicleId);
    }

    public void deactivateEmergency() {
        this.state.set("RED");
        this.emergencyOverride = false;
        this.emergencyVehicleId = null;
        System.out.println("Feu " + id + ": retour à ROUGE");
    }

    // Getters
    public String getId() { return id; }
    public StringProperty stateProperty() { return state; }
    public String getState() { return state.get(); }
    public boolean isEmergencyOverride() { return emergencyOverride; }
    public String getEmergencyVehicleId() { return emergencyVehicleId; }
}