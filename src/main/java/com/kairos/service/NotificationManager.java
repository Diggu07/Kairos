package com.kairos.service;

import com.kairos.dao.EntryDAO;
import com.kairos.model.Entry;
import java.time.LocalDateTime;

/**
 * Tracks notification history, prevents duplicate notifications,
 * and manages snooze functionality.
 */
public class NotificationManager {

    private static final EntryDAO entryDAO = new EntryDAO();

    /**
     * Records that a notification was sent for a given entry.
     * Updates notified_at and increments notification_count.
     */
    public static void recordNotification(int entryId, LocalDateTime notifiedAt) {
        Entry entry = entryDAO.getEntryById(entryId);
        if (entry != null) {
            entry.setNotifiedAt(notifiedAt);
            entry.setNotificationCount(entry.getNotificationCount() + 1);
            entryDAO.updateEntry(entry);
        }
    }

    /**
     * Checks if a notification has recently been sent to prevent duplicates.
     * Logic: If already notified, we won't notify again unless snoozed time is reached.
     */
    public static boolean hasBeenNotified(Entry entry) {
        if (entry.getNotifiedAt() == null) {
            return false;
        }
        
        // If it was snoozed, and current time is past snooze time, we should notify again
        if (entry.getLastSnoozedAt() != null) {
            return !LocalDateTime.now().isAfter(entry.getLastSnoozedAt());
        }

        // Default behavior: if notifiedAt is set, it has been notified.
        return true;
    }

    /**
     * Snoozes a reminder for a specific number of minutes.
     * Updates last_snoozed_at to current time + minutes.
     */
    public static void snoozeReminder(int entryId, int minutes) {
        Entry entry = entryDAO.getEntryById(entryId);
        if (entry != null) {
            entry.setLastSnoozedAt(LocalDateTime.now().plusMinutes(minutes));
            // Reset notifiedAt to allow the next trigger
            entry.setNotifiedAt(null);
            entryDAO.updateEntry(entry);
        }
    }
}
