package com.kairos.main;

import com.kairos.dao.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX application entry point for Kairos.
 *
 * <p>Bootstraps the database, loads the dashboard FXML, and presents
 * the primary stage to the user.
 *
 * @author Kairos
 * @version 1.0.0
 */
public class MainApp extends Application {

    /** Application window title. */
    public static final String APP_TITLE = "Kairos — Master the Opportune";

    /** Preferred initial window width in pixels. */
    public static final double PREF_WIDTH = 1100;

    /** Preferred initial window height in pixels. */
    public static final double PREF_HEIGHT = 700;

    /** Minimum window width — prevents layout from collapsing. */
    public static final double MIN_WIDTH = 900;

    /** Minimum window height. */
    public static final double MIN_HEIGHT = 600;

    // ─────────────────────────────────────────────────────────────────────────
    // Application Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * JavaFX application start method.
     *
     * <p>Loads {@code dashboard.fxml}, configures the stage dimensions,
     * applies the global stylesheet, and shows the window.
     *
     * @param primaryStage the primary window provided by the JavaFX runtime
     * @throws IOException if the FXML resource cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Initialise database eagerly so first-run table creation happens before UI
        DatabaseManager.getInstance();

        // Start background scheduling (Notifications/Audio)
        com.kairos.service.ReminderService.start();

        // Load the root layout from FXML
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(
                        getClass().getResource("/com/kairos/dashboard.fxml"),
                        "dashboard.fxml not found on classpath"
                )
        );

        Scene scene = new Scene(loader.load(), PREF_WIDTH, PREF_HEIGHT);

        // Apply global dark stylesheet
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource("/com/kairos/styles.css"),
                        "styles.css not found on classpath"
                ).toExternalForm()
        );

        // Configure the primary stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth (MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setWidth (PREF_WIDTH);
        primaryStage.setHeight(PREF_HEIGHT);

        // Close DB connection gracefully when the window is closed
        primaryStage.setOnCloseRequest(event -> {
            com.kairos.service.ReminderService.stop();
            DatabaseManager.getInstance().closeConnection();
            System.out.println("[MainApp] Application closed.");
        });

        primaryStage.show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main Entry Point
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Standard Java entry point. Delegates to {@link Application#launch(String...)}.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
