package com.traffic.view;

import com.traffic.model.PietonModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.effect.Effect; // Import this
import javafx.scene.effect.Glow;   // Import this

public class PietonDashboard extends VBox {

    public PietonDashboard(PietonModel model) {
        super(15);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: #2c3e50; -fx-border-color: #bdc3c7; -fx-border-width: 2; -fx-background-radius: 10;");
        this.setAlignment(Pos.CENTER);

        // Title
        Label title = new Label("🚶 PASSAGE PIÉTON");
        title.setTextFill(Color.WHITE);
        title.setFont(new Font("System Bold", 16));

        // Pedestrian Lights
        HBox lightsBox = new HBox(15);
        lightsBox.setAlignment(Pos.CENTER);

        VBox redBox = createLightBox("STOP", Color.RED, model, "ROUGE");
        VBox greenBox = createLightBox("WALK", Color.LIME, model, "VERT");

        lightsBox.getChildren().addAll(redBox, greenBox);

        // Request Button
        Button btnDemande = new Button("🔘 Demander Passage");
        btnDemande.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnDemande.setOnAction(e -> model.demanderPassage());

        // Disable button if already green
        btnDemande.disableProperty().bind(model.etatPietonProperty().isEqualTo("VERT"));

        // Status Label
        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.YELLOW);
        statusLabel.textProperty().bind(
                javafx.beans.binding.Bindings.when(model.demandePassageProperty())
                        .then("⏳ Attente du feu rouge...")
                        .otherwise("")
        );

        this.getChildren().addAll(title, lightsBox, btnDemande, statusLabel);
    }

    private VBox createLightBox(String text, Color color, PietonModel model, String targetState) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        Circle light = new Circle(15);
        light.setStroke(Color.BLACK);
        light.setStrokeWidth(2);

        // Bind color: Bright when active, Dark when inactive
        light.fillProperty().bind(
                javafx.beans.binding.Bindings.when(model.etatPietonProperty().isEqualTo(targetState))
                        .then(color)
                        .otherwise(color.darker().darker().desaturate())
        );

        // --- FIX IS HERE ---
        // We cast the Glow to (Effect) so the builder knows we want a generic Effect,
        // allowing us to return null in the otherwise() branch.
        light.effectProperty().bind(
                javafx.beans.binding.Bindings.when(model.etatPietonProperty().isEqualTo(targetState))
                        .then((Effect) new Glow(0.8))  // Cast to Effect
                        .otherwise((Effect) null)      // Cast to Effect
        );

        Label lbl = new Label(text);
        lbl.setTextFill(Color.LIGHTGRAY);
        lbl.setFont(new Font(10));

        box.getChildren().addAll(light, lbl);
        return box;
    }
}