package com.kairos.service;

import com.kairos.model.Entry;
import com.kairos.components.CustomNotificationWindow;
import com.kairos.components.CustomNotificationWindow.NotificationType;

public class AlertManager {
    private static AlertManager instance;

    public enum AlertType {
        REMINDER_DUE, TASK_OVERDUE, MOTIVATION_PROMPT, DAILY_SUMMARY, MILESTONE_REACHED
    }

    private AlertManager() {}

    public static synchronized AlertManager getInstance() {
        if (instance == null) {
            instance = new AlertManager();
        }
        return instance;
    }

    public void triggerAlert(AlertType type, Entry entry) {
        String title = "";
        String message = "";
        NotificationType notifType = NotificationType.INFO;

        switch (type) {
            case REMINDER_DUE:
                title = "Reminder Due";
                message = entry != null ? entry.getTitle() : "You have a reminder.";
                notifType = NotificationType.WARNING;
                break;
            case TASK_OVERDUE:
                title = "Task Overdue";
                message = entry != null ? entry.getTitle() + " is overdue!" : "A task is overdue.";
                notifType = NotificationType.ERROR;
                break;
            case MOTIVATION_PROMPT:
                title = "Keep Going!";
                message = "You're doing great. Stay focused.";
                notifType = NotificationType.INFO;
                break;
            case DAILY_SUMMARY:
                title = "Daily Summary";
                message = "Here is your summary for today.";
                notifType = NotificationType.INFO;
                break;
            case MILESTONE_REACHED:
                title = "Milestone Reached! 🎉";
                message = "Congratulations on hitting a new milestone!";
                notifType = NotificationType.SUCCESS;
                break;
        }

        CustomNotificationWindow.showNotification(title, message, notifType);
        playAlertSound(type);
    }

    private void playAlertSound(AlertType type) {
        // Assume AudioService plays standard alerts
        // AudioService.playNotificationSound();
    }
}
