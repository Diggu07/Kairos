package com.kairos.service;

import com.kairos.dao.EntryDAO;
import com.kairos.model.Entry;
import com.kairos.model.Entry.EntryType;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Singleton service that checks for overdue reminders and triggers notifications.
 */
public class ReminderScheduler {

    private static ReminderScheduler instance;
    private ScheduledExecutorService scheduler;
    private final EntryDAO entryDAO;

    private ReminderScheduler() {
        entryDAO = new EntryDAO();
    }

    public static synchronized ReminderScheduler getInstance() {
        if (instance == null) {
            instance = new ReminderScheduler();
        }
        return instance;
    }

    public void startScheduling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "ReminderScheduler-Thread");
            t.setDaemon(true);
            return t;
        });

        // Run the checker every 1 minute
        scheduler.scheduleAtFixedRate(this::checkAndTriggerReminders, 5, 60, TimeUnit.SECONDS);
        System.out.println("[ReminderScheduler] Started background scheduler.");
    }

    public void stopScheduling() {
        if (scheduler != null) {
            scheduler.shutdown();
            System.out.println("[ReminderScheduler] Stopped background scheduler.");
        }
    }

    public boolean isRunning() {
        return scheduler != null && !scheduler.isShutdown();
    }

    public void scheduleReminder(Entry reminder) {
        // Here we could schedule an individual Future, but polling handles logic dynamically.
        // The polling loop will pick it up, so no immediate timer needed unless we want exact second precision.
    }

    public void cancelReminder(int entryId) {
        // Individual canceling is handled by simply marking entry completed or changing its date.
    }

    private void checkAndTriggerReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Entry> reminders = entryDAO.getEntriesByType(EntryType.REMINDER);

            for (Entry r : reminders) {
                if (r.isCompleted() || r.getReminderTime() == null) {
                    continue;
                }

                // If reminder time has passed or is now
                if (!r.getReminderTime().isAfter(now)) {
                    if (!NotificationManager.hasBeenNotified(r)) {
                        
                        NotificationManager.recordNotification(r.getId(), now);
                        
                        // Action: trigger audio and UI
                        NotificationService.playAlertSound(NotificationService.AlertType.REMINDER_SOFT);
                        NotificationService.showSystemNotification("⏰ Reminder Due!", r.getTitle(), String.valueOf(r.getId()));

                        // Fire UI Alert on the JavaFX Application Thread
                        Platform.runLater(() -> createNotificationPopup(r));
                    }
                }
            }
        } catch (Exception e) {
             System.err.println("[ReminderScheduler] Error during database poll: " + e.getMessage());
             e.printStackTrace();
        }
    }

    private void createNotificationPopup(Entry r) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reminder Alert");
        alert.setHeaderText("⏰ " + r.getTitle());
        alert.setContentText(r.getContent() == null ? "" : r.getContent());
        
        ButtonType btnDone = new ButtonType("Complete", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnSnooze = new ButtonType("Snooze 15m", ButtonBar.ButtonData.OTHER);
        ButtonType btnClose = new ButtonType("Dismiss", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(btnDone, btnSnooze, btnClose);

        javafx.stage.Stage stage = (javafx.stage.Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        alert.setOnHidden(evt -> {
            if (alert.getResult() == btnDone) {
                entryDAO.markAsCompleted(r.getId());
                NotificationService.showToastNotification("Reminder completed!", 3);
            } else if (alert.getResult() == btnSnooze) {
                NotificationManager.snoozeReminder(r.getId(), 15);
                NotificationService.showToastNotification("Snoozed for 15 minutes.", 3);
            }
        });
        
        alert.show();
    }
}
