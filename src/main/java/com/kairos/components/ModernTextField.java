package com.kairos.components;

import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ModernTextField extends VBox {
    private TextField textField;
    private Label floatingLabel;
    private Label errorLabel;
    private Rectangle underline;

    public ModernTextField(String placeholder) {
        setupUI(placeholder);
        setupAnimations();
    }

    private void setupUI(String placeholder) {
        setSpacing(4);
        
        StackPane inputContainer = new StackPane();
        inputContainer.setAlignment(Pos.CENTER_LEFT);

        textField = new TextField();
        textField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 20 8 4 8;");
        
        floatingLabel = new Label(placeholder);
        floatingLabel.setStyle("-fx-text-fill: -color-text-secondary; -fx-font-size: 14px; -fx-padding: 0 0 0 8;");
        floatingLabel.setMouseTransparent(true);
        
        underline = new Rectangle();
        underline.setHeight(2);
        underline.widthProperty().bind(textField.widthProperty());
        underline.setFill(Color.web("#334155")); // border color
        
        StackPane.setAlignment(underline, Pos.BOTTOM_CENTER);
        
        inputContainer.getChildren().addAll(textField, floatingLabel, underline);
        
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: -color-error; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        
        getChildren().addAll(inputContainer, errorLabel);
    }

    private void setupAnimations() {
        TranslateTransition labelUp = new TranslateTransition(Duration.millis(150), floatingLabel);
        labelUp.setToY(-12);
        
        TranslateTransition labelDown = new TranslateTransition(Duration.millis(150), floatingLabel);
        labelDown.setToY(0);

        textField.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.isEmpty() && oldV.isEmpty()) {
                labelUp.playFromStart();
                floatingLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: -color-primary; -fx-padding: 0 0 0 8;");
            } else if (newV.isEmpty()) {
                if (!textField.isFocused()) {
                    labelDown.playFromStart();
                    floatingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-text-secondary; -fx-padding: 0 0 0 8;");
                }
            }
        });

        textField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                underline.setFill(Color.web("#3B82F6")); // primary color
                if (textField.getText().isEmpty()) {
                    labelUp.playFromStart();
                    floatingLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: -color-primary; -fx-padding: 0 0 0 8;");
                }
            } else {
                underline.setFill(Color.web("#334155"));
                if (textField.getText().isEmpty()) {
                    labelDown.playFromStart();
                    floatingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-text-secondary; -fx-padding: 0 0 0 8;");
                }
            }
        });
    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public void setError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        underline.setFill(Color.web("#EF4444")); // error color
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        underline.setFill(textField.isFocused() ? Color.web("#3B82F6") : Color.web("#334155"));
    }
}
