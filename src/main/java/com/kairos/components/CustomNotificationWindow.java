package com.kairos.components;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class CustomNotificationWindow {

    public enum NotificationType { SUCCESS, ERROR, WARNING, INFO }

    // Hardcoded colors — popup Stage doesn't inherit the app's looked-up colors
    private static final String BG_COLOR = "#1E293B";
    private static final String TEXT_PRIMARY = "#F1F5F9";
    private static final String TEXT_SECONDARY = "#94A3B8";

    public static void showNotification(String title, String message, NotificationType type) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);

        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        root.setPrefWidth(380);
        root.setMinHeight(100);

        String accentColor;
        String iconEmoji;
        switch (type) {
            case SUCCESS: accentColor = "#10B981"; iconEmoji = "✅"; break;
            case ERROR:   accentColor = "#EF4444"; iconEmoji = "❌"; break;
            case WARNING: accentColor = "#F59E0B"; iconEmoji = "⚠"; break;
            default:      accentColor = "#3B82F6"; iconEmoji = "ℹ"; break;
        }

        root.setStyle(
            "-fx-background-color: " + BG_COLOR + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + accentColor + ";" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 0 0 0 5;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(20);
        shadow.setOffsetY(8);
        root.setEffect(shadow);

        // ── Header ──
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(iconEmoji);
        iconLabel.setStyle("-fx-font-size: 18px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: " + TEXT_PRIMARY + ";"
        );

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("X");
        closeBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: " + TEXT_SECONDARY + ";" +
            "-fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 0 4;"
        );
        closeBtn.setOnAction(e -> closeWithAnimation(stage, root));

        header.getChildren().addAll(iconLabel, titleLabel, spacer, closeBtn);

        // ── Message ──
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(340);
        messageLabel.setStyle(
            "-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px;"
        );

        // ── Accent bar at bottom ──
        Rectangle accentBar = new Rectangle(348, 3);
        accentBar.setArcWidth(6);
        accentBar.setArcHeight(6);
        accentBar.setFill(Color.web(accentColor));

        root.getChildren().addAll(header, messageLabel, accentBar);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        // Position bottom-right
        javafx.geometry.Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMaxX() - 400 - 24);
        stage.setY(bounds.getMaxY() - 140);

        // Entrance animation
        root.setOpacity(0);
        root.setTranslateX(400);
        stage.show();

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), root);
        slideIn.setFromX(400);
        slideIn.setToX(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(350), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        slideIn.play();
        fadeIn.play();

        // Auto close after 6 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(6));
        delay.setOnFinished(e -> closeWithAnimation(stage, root));
        delay.play();
    }

    private static void closeWithAnimation(Stage stage, VBox root) {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), root);
        slideOut.setToX(400);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), root);
        fadeOut.setToValue(0);

        slideOut.setOnFinished(e -> stage.close());
        slideOut.play();
        fadeOut.play();
    }
}
