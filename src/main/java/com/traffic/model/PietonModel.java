package com.traffic.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PietonModel {
    private StringProperty etatPieton = new SimpleStringProperty("ROUGE");
    private BooleanProperty demandePassage = new SimpleBooleanProperty(false);

    // Link to the main traffic light state
    private StringProperty mainLightState;

    public PietonModel() {
        // Default state
    }

    public void demanderPassage() {
        if (etatPieton.get().equals("ROUGE")) {
            demandePassage.set(true);
            System.out.println("🚶 Piéton a appuyé sur le bouton !");
        }
    }

    public void setMainLightState(String state) {
        // Logic: Pedestrians can walk ONLY if Cars are RED
        if (state.equals("RED")) {
            etatPieton.set("VERT");
            demandePassage.set(false); // Request fulfilled
        } else {
            etatPieton.set("ROUGE");
        }
    }

    // Getters
    public StringProperty etatPietonProperty() { return etatPieton; }
    public BooleanProperty demandePassageProperty() { return demandePassage; }
}