package com.kairos.service;

import com.kairos.dao.EntryDAO;
import com.kairos.model.Entry;
import com.kairos.model.Entry.EntryType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProcrastinationDetector {

    public enum ProcrastinationLevel { LOW, MODERATE, HIGH }

    private final EntryDAO entryDAO;

    public ProcrastinationDetector() {
        this.entryDAO = new EntryDAO();
    }

    /**
     * Finds tasks/reminders that were created more than {@code thresholdDays} ago
     * but are still incomplete.
     */
    public List<Entry> identifyProcrastinatedTasks() {
        int thresholdDays = 3; // tasks older than 3 days and not done
        LocalDateTime cutoff = LocalDateTime.now().minusDays(thresholdDays);

        List<Entry> allTasks = entryDAO.getEntriesByType(EntryType.TASK);
        List<Entry> allReminders = entryDAO.getEntriesByType(EntryType.REMINDER);

        List<Entry> combined = new ArrayList<>();
        combined.addAll(allTasks);
        combined.addAll(allReminders);

        return combined.stream()
                .filter(e -> !e.isCompleted())
                .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().isBefore(cutoff))
                .collect(Collectors.toList());
    }

    /**
     * Returns the user's procrastination level based on the ratio of stalled tasks.
     */
    public ProcrastinationLevel getProcrastinationLevel() {
        List<Entry> allTasks = entryDAO.getEntriesByType(EntryType.TASK);
        if (allTasks.isEmpty()) return ProcrastinationLevel.LOW;

        long stalled = identifyProcrastinatedTasks().size();
        double ratio = (double) stalled / allTasks.size();

        if (ratio > 0.5) return ProcrastinationLevel.HIGH;
        if (ratio > 0.25) return ProcrastinationLevel.MODERATE;
        return ProcrastinationLevel.LOW;
    }

    /**
     * Suggests an immediate action based on the oldest stalled task.
     */
    public String suggestImmediateAction() {
        List<Entry> stalled = identifyProcrastinatedTasks();
        if (stalled.isEmpty()) {
            return "You're all caught up! Great work. 🎉";
        }

        Entry oldest = stalled.stream()
                .min((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .orElse(stalled.get(0));

        long daysOld = ChronoUnit.DAYS.between(oldest.getCreatedAt(), LocalDateTime.now());
        return String.format("Start with \"%s\" — it's been waiting %d days. Break it into a 5-minute step!",
                oldest.getTitle(), daysOld);
    }

    /**
     * Analyzes postpone patterns.
     */
    public String analyzePostponePattern() {
        List<Entry> stalled = identifyProcrastinatedTasks();
        if (stalled.isEmpty()) {
            return "No procrastination patterns detected. Keep it up!";
        }
        return String.format("You have %d stalled items. Procrastination level: %s. %s",
                stalled.size(), getProcrastinationLevel(), suggestImmediateAction());
    }
}
