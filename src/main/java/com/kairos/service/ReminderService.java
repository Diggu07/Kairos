package com.kairos.service;

import com.kairos.dao.EntryDAO;
import com.kairos.model.Entry;
import com.kairos.model.Entry.EntryType;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background service that periodically checks for overdue reminders
 * and triggers visual/audio alerts.
 */
public class ReminderService {

    private static ScheduledExecutorService scheduler;
    private static final Set<Integer> alertedIds = new HashSet<>();
    private static final EntryDAO entryDAO = new EntryDAO();

    /**
     * Starts the background polling thread.
     */
    public static void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ReminderService-Thread");
            t.setDaemon(true);
            return t;
        });

        // Run the checker every 10 seconds
        scheduler.scheduleAtFixedRate(ReminderService::checkReminders, 5, 10, TimeUnit.SECONDS);
        System.out.println("[ReminderService] Started background scheduler.");
    }

    /**
     * Stops the background polling thread cleanly.
     */
    public static void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
            System.out.println("[ReminderService] Stopped background scheduler.");
        }
    }

    /**
     * Polling logic logic executed by the scheduled thread.
     */
    private static void checkReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Entry> reminders = entryDAO.getEntriesByType(EntryType.REMINDER);

            for (Entry r : reminders) {
                if (r.isCompleted() || r.getReminderTime() == null) {
                    continue;
                }

                if (!r.getReminderTime().isAfter(now)) {
                    if (!alertedIds.contains(r.getId())) {
                        
                        alertedIds.add(r.getId());
                        
                        // Fire audio
                        AudioService.playNotification();

                        // Fire UI Alert on the JavaFX Application Thread
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Reminder Alert");
                            alert.setHeaderText("⏰ Reminder Due!");
                            alert.setContentText(r.getTitle() + "\n\n" + (r.getContent() == null ? "" : r.getContent()));
                            // Custom buttons
                            javafx.scene.control.ButtonType btnDone = new javafx.scene.control.ButtonType("Mark as Done", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                            javafx.scene.control.ButtonType btnClose = new javafx.scene.control.ButtonType("Close", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
                            alert.getButtonTypes().setAll(btnDone, btnClose);

                            // Keep it on top of other windows so user doesn't miss it
                            javafx.stage.Stage stage = (javafx.stage.Stage) alert.getDialogPane().getScene().getWindow();
                            stage.setAlwaysOnTop(true);

                            alert.setOnHidden(evt -> {
                                if (alert.getResult() == btnDone) {
                                    entryDAO.markAsCompleted(r.getId());
                                }
                            });
                            
                            alert.show();
                        });
                    }
                } else {
                    // Reminder is in the future. If it was previously alerted, clear it so it can ring again later!
                    alertedIds.remove(r.getId());
                }
            }
        } catch (Exception e) {
             System.err.println("[ReminderService] Error during database poll: " + e.getMessage());
             e.printStackTrace();
        }
    }
}
