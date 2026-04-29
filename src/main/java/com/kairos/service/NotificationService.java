package com.kairos.service;

import com.kairos.model.Entry;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

import java.awt.*;

/**
 * Handles different types of notifications: System tray, Toasts, and Alert Popups.
 */
public class NotificationService {

    public enum AlertType {
        REMINDER_SOFT, REMINDER_LOUD, TASK_ALERT
    }

    /**
     * Shows a desktop system notification via SystemTray if supported.
     */
    public static void showSystemNotification(String title, String message, String entryId) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                
                // Try to use existing tray icon if available, else create temporary one
                Image image = Toolkit.getDefaultToolkit().createImage(NotificationService.class.getResource("/com/kairos/logo.png"));
                TrayIcon trayIcon = new TrayIcon(image, "Kairos");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
                
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
                
                // Remove it after a few seconds so it doesn't pile up (if not persistent)
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        tray.remove(trayIcon);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                
            } catch (Exception e) {
                System.err.println("[NotificationService] Failed to show system tray notification: " + e.getMessage());
            }
        }
    }

    /**
     * Plays the appropriate alert sound based on AlertType.
     * Delegates to AudioService.
     */
    public static void playAlertSound(AlertType type) {
        // In a full implementation we would check the type and play different wav files.
        // For now, AudioService plays the default notify.wav
        AudioService.playNotification();
    }

    /**
     * Shows a custom JavaFX toast notification that auto-dismisses.
     */
    public static void showToastNotification(String message, int durationSeconds) {
        Platform.runLater(() -> {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);

            Label text = new Label(message);
            text.setStyle("-fx-background-color: rgba(59, 130, 246, 0.9); -fx-text-fill: white; -fx-padding: 15px; -fx-background-radius: 8px; -fx-font-size: 14px;");

            VBox root = new VBox(text);
            root.setAlignment(Pos.CENTER);
            root.setStyle("-fx-background-color: transparent; -fx-padding: 20px;");

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);

            // Position at bottom right roughly
            // In a real app, calculate based on Screen bounds
            toastStage.show();

            PauseTransition delay = new PauseTransition(Duration.seconds(durationSeconds));
            delay.setOnFinished(e -> toastStage.close());
            delay.play();
        });
    }
}
