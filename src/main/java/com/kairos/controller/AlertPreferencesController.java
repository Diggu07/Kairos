package com.kairos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AlertPreferencesController {
    
    @FXML private CheckBox enableNotificationsCheck;
    @FXML private Slider volumeSlider;
    @FXML private ComboBox<String> alertTimingCombo;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @FXML
    public void initialize() {
        alertTimingCombo.getItems().addAll(
            "Exact time", "5 mins before", "15 mins before", "1 hour before"
        );
        alertTimingCombo.getSelectionModel().selectFirst();

        saveButton.setOnAction(e -> savePreferences());
        cancelButton.setOnAction(e -> closeDialog());
    }

    private void savePreferences() {
        // Save logic to database via DAO
        // e.g., DatabaseManager.savePreference("notifications_enabled", enableNotificationsCheck.isSelected());
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
