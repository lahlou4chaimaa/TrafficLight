package com.traffic.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EmergencyVehicle {
    private String id;
    private String type;
    private StringProperty currentPosition;
    private BooleanProperty emergencyActive;
    private TrafficLight currentTrafficLight;

    public EmergencyVehicle(String id, String type) {
        this.id = id;
        this.type = type;
        this.currentPosition = new SimpleStringProperty("Garage");
        this.emergencyActive = new SimpleBooleanProperty(false);
        this.currentTrafficLight = null;
    }

    public void arriveAtTrafficLight(TrafficLight light) {
        this.currentTrafficLight = light;
        this.currentPosition.set("Devant " + light.getId());
        if (emergencyActive.get()) {
            light.activateEmergencyGreen(id);
        }
    }

    public void passTrafficLight() {
        if (currentTrafficLight != null) {
            if (currentTrafficLight.isEmergencyOverride() &&
                    currentTrafficLight.getEmergencyVehicleId().equals(id)) {
                currentTrafficLight.deactivateEmergency();
            }
            currentTrafficLight = null;
            currentPosition.set("En route");
        }
    }

    public void startEmergency() { emergencyActive.set(true); }
    public void stopEmergency() { emergencyActive.set(false); }

    public String getId() { return id; }
    public String getType() { return type; }
    public StringProperty currentPositionProperty() { return currentPosition; }
    public BooleanProperty emergencyActiveProperty() { return emergencyActive; }
    public boolean isEmergencyActive() { return emergencyActive.get(); }
    public TrafficLight getCurrentTrafficLight() { return currentTrafficLight; }
}