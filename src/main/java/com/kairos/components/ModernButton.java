package com.kairos.components;

import javafx.animation.FillTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class ModernButton extends Button {

    public enum Variant { PRIMARY, SECONDARY, SUCCESS, DANGER, OUTLINE, GHOST }
    private Variant variant = Variant.PRIMARY;

    public ModernButton(String text) {
        super(text);
        setupUI();
        setupAnimations();
    }

    public ModernButton(String text, Variant variant) {
        super(text);
        this.variant = variant;
        setupUI();
        setupAnimations();
    }

    private void setupUI() {
        getStyleClass().add("button");
        getStyleClass().add("button-" + variant.name().toLowerCase());
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(5);
        shadow.setOffsetY(2);
        setEffect(shadow);
    }

    private void setupAnimations() {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), this);
        scaleIn.setToX(1.02);
        scaleIn.setToY(1.02);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), this);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        ScaleTransition press = new ScaleTransition(Duration.millis(100), this);
        press.setToX(0.98);
        press.setToY(0.98);

        setOnMouseEntered(e -> scaleIn.playFromStart());
        setOnMouseExited(e -> {
            scaleIn.stop();
            scaleOut.playFromStart();
        });

        setOnMousePressed(e -> press.playFromStart());
        setOnMouseReleased(e -> {
            press.stop();
            if (isHover()) {
                scaleIn.playFromStart();
            } else {
                scaleOut.playFromStart();
            }
        });
    }

    public void setVariant(Variant variant) {
        getStyleClass().remove("button-" + this.variant.name().toLowerCase());
        this.variant = variant;
        getStyleClass().add("button-" + this.variant.name().toLowerCase());
    }
}
