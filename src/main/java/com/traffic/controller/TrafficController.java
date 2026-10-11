package com.traffic.controller;

import com.traffic.model.LightColor;
import com.traffic.model.PietonModel;
import com.traffic.model.TrafficModel;
import com.traffic.view.EmergencyDashboard;
import com.traffic.view.PietonDashboard;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class TrafficController {

    @FXML private Circle mainRed, mainYellow, mainGreen;
    @FXML private Circle crossRed, crossYellow, crossGreen;
    @FXML private ProgressBar mainDensityBar, crossDensityBar;
    @FXML private Label mainDensityLabel, crossDensityLabel;
    @FXML private Button startButton, tramButton;
    @FXML private Label statusLabel;
    @FXML private HBox dashboardContainer;

    private TrafficModel model;
    private EmergencyManager emergencyManager;
    private PietonModel pietonModel;

    // Keep track of current colors so we don't lose them during density updates
    private LightColor currentMain = LightColor.RED;
    private LightColor currentCross = LightColor.RED;

    @FXML
    public void initialize() {
        model = new TrafficModel(this::updateIntersection);
        emergencyManager = new EmergencyManager(model);
        pietonModel = new PietonModel();

        EmergencyDashboard emergencyDash = new EmergencyDashboard(emergencyManager);
        PietonDashboard pietonDash = new PietonDashboard(pietonModel);
        HBox.setHgrow(emergencyDash, Priority.ALWAYS);
        HBox.setHgrow(pietonDash, Priority.ALWAYS);

        if (dashboardContainer != null) dashboardContainer.getChildren().addAll(emergencyDash, pietonDash);
        resetLights();
    }

    @FXML
    public void onStartClick() {
        startButton.setDisable(true);
        statusLabel.setText("System: Smart Dynamic Cycle");
        model.startSimulation();
    }

    @FXML
    public void onTramClick() {
        statusLabel.setText("System: Tram Priority (Main Road)");
        model.triggerEmergency("MAIN");
    }

    // Called frequently (every 0.5s)
    private void updateIntersection(LightColor main, LightColor cross, double mainDens, double crossDens) {
        Platform.runLater(() -> {
            // If colors are null, it means only density changed. Use last known colors.
            if (main != null) currentMain = main;
            if (cross != null) currentCross = cross;

            setLightGroup(mainRed, mainYellow, mainGreen, currentMain);
            setLightGroup(crossRed, crossYellow, crossGreen, currentCross);

            // Update Bars (Dynamic!)
            mainDensityBar.setProgress(mainDens);
            crossDensityBar.setProgress(crossDens);
            setStyleForBar(mainDensityBar, mainDens);
            setStyleForBar(crossDensityBar, crossDens);

            // Update Labels
            mainDensityLabel.setText(String.format("Traffic: %.0f%%", mainDens * 100));
            crossDensityLabel.setText(String.format("Traffic: %.0f%%", crossDens * 100));

            // Pedestrian Logic
            if (currentMain == LightColor.RED && currentCross == LightColor.RED) {
                pietonModel.setMainLightState("GREEN"); // Stop Peds
            } else if (currentMain == LightColor.RED) {
                pietonModel.setMainLightState("RED"); // Walk
            } else {
                pietonModel.setMainLightState("GREEN"); // Stop Peds
            }
        });
    }

    private void setStyleForBar(ProgressBar bar, double density) {
        if (density < 0.3) bar.setStyle("-fx-accent: #2ecc71;");
        else if (density < 0.7) bar.setStyle("-fx-accent: #f1c40f;");
        else bar.setStyle("-fx-accent: #e74c3c;");
    }

    private void setLightGroup(Circle r, Circle y, Circle g, LightColor color) {
        r.setFill(Color.DARKRED.darker()); r.setEffect(null);
        y.setFill(Color.GOLDENROD.darker()); y.setEffect(null);
        g.setFill(Color.DARKGREEN.darker()); g.setEffect(null);
        switch (color) {
            case RED -> { r.setFill(Color.RED); r.setEffect(new javafx.scene.effect.Glow(0.8)); }
            case YELLOW -> { y.setFill(Color.YELLOW); y.setEffect(new javafx.scene.effect.Glow(0.8)); }
            case GREEN -> { g.setFill(Color.LIME); g.setEffect(new javafx.scene.effect.Glow(0.8)); }
        }
    }

    private void resetLights() {
        setLightGroup(mainRed, mainYellow, mainGreen, LightColor.RED);
        setLightGroup(crossRed, crossYellow, crossGreen, LightColor.RED);
        mainDensityBar.setProgress(0);
        crossDensityBar.setProgress(0);
    }
}