package com.traffic.view;

import com.traffic.controller.EmergencyManager;
import com.traffic.model.EmergencyVehicle;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EmergencyDashboard extends VBox {

    public EmergencyDashboard(EmergencyManager manager) {
        super(15);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc;");

        Label title = new Label("🚑 FLOTTE D'URGENCE (Colleague's Part)");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        this.getChildren().add(title);

        for (EmergencyVehicle v : manager.getVehicles()) {
            this.getChildren().add(createCard(v, manager));
        }
    }

    private HBox createCard(EmergencyVehicle v, EmergencyManager manager) {
        HBox card = new HBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd;");

        VBox info = new VBox(5);
        Label name = new Label(v.getId() + " (" + v.getType() + ")");
        name.setStyle("-fx-font-weight: bold;");
        Label status = new Label();
        status.textProperty().bind(v.currentPositionProperty());

        info.getChildren().addAll(name, status);

        VBox buttons = new VBox(5);
        Button btnEmergency = new Button("Siren ON/OFF");
        btnEmergency.setOnAction(e -> {
            if (v.isEmergencyActive()) manager.stopEmergency(v.getId());
            else manager.startEmergency(v.getId());
        });

        Button btnMove = new Button("Go to Light");
        btnMove.setOnAction(e -> manager.simulateVehicleMovement(v.getId()));

        buttons.getChildren().addAll(btnEmergency, btnMove);
        card.getChildren().addAll(info, buttons);

        return card;
    }
}