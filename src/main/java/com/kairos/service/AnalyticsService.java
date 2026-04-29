package com.kairos.service;

import com.kairos.dao.EntryDAO;
import com.kairos.model.Entry;
import com.kairos.model.Entry.EntryType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AnalyticsService {
    private static AnalyticsService instance;
    private final EntryDAO entryDAO;

    private AnalyticsService() {
        this.entryDAO = new EntryDAO();
    }

    public static synchronized AnalyticsService getInstance() {
        if (instance == null) {
            instance = new AnalyticsService();
        }
        return instance;
    }

    /**
     * Calculates the task completion rate between two dates.
     */
    public double getTaskCompletionRate(LocalDate from, LocalDate to) {
        List<Entry> tasks = entryDAO.getEntriesByType(EntryType.TASK);
        if (tasks.isEmpty()) return 0.0;

        long total = tasks.stream()
                .filter(e -> e.getCreatedAt() != null)
                .filter(e -> {
                    LocalDate created = e.getCreatedAt().toLocalDate();
                    return !created.isBefore(from) && !created.isAfter(to);
                })
                .count();

        if (total == 0) return 0.0;

        long completed = tasks.stream()
                .filter(Entry::isCompleted)
                .filter(e -> e.getCreatedAt() != null)
                .filter(e -> {
                    LocalDate created = e.getCreatedAt().toLocalDate();
                    return !created.isBefore(from) && !created.isAfter(to);
                })
                .count();

        return (double) completed / total;
    }

    /**
     * Finds the hour of day when most tasks are completed.
     */
    public LocalTime getMostProductiveHour() {
        List<Entry> completed = entryDAO.getAllEntries().stream()
                .filter(Entry::isCompleted)
                .filter(e -> e.getUpdatedAt() != null)
                .collect(Collectors.toList());

        if (completed.isEmpty()) return LocalTime.of(10, 0);

        Map<Integer, Long> hourCounts = completed.stream()
                .collect(Collectors.groupingBy(e -> e.getUpdatedAt().getHour(), Collectors.counting()));

        int bestHour = hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(10);

        return LocalTime.of(bestHour, 0);
    }

    /**
     * Finds the day of week when most tasks are completed.
     */
    public DayOfWeek getMostProductiveDay() {
        List<Entry> completed = entryDAO.getAllEntries().stream()
                .filter(Entry::isCompleted)
                .filter(e -> e.getUpdatedAt() != null)
                .collect(Collectors.toList());

        if (completed.isEmpty()) return DayOfWeek.MONDAY;

        Map<DayOfWeek, Long> dayCounts = completed.stream()
                .collect(Collectors.groupingBy(e -> e.getUpdatedAt().getDayOfWeek(), Collectors.counting()));

        return dayCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DayOfWeek.MONDAY);
    }

    /**
     * Calculates consecutive days with at least one completed task (streak).
     */
    public int getCurrentStreak() {
        List<Entry> completed = entryDAO.getAllEntries().stream()
                .filter(Entry::isCompleted)
                .filter(e -> e.getUpdatedAt() != null)
                .collect(Collectors.toList());

        if (completed.isEmpty()) return 0;

        // Get unique dates with completions
        java.util.Set<LocalDate> completionDates = completed.stream()
                .map(e -> e.getUpdatedAt().toLocalDate())
                .collect(Collectors.toSet());

        int streak = 0;
        LocalDate day = LocalDate.now();

        while (completionDates.contains(day)) {
            streak++;
            day = day.minusDays(1);
        }

        return streak;
    }

    /**
     * Calculates a 0-100 habit score based on recent consistency.
     */
    public int getHabitScore() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        List<Entry> allEntries = entryDAO.getAllEntries();
        
        long createdThisWeek = allEntries.stream()
                .filter(e -> e.getCreatedAt() != null)
                .filter(e -> !e.getCreatedAt().toLocalDate().isBefore(weekAgo))
                .count();

        long completedThisWeek = allEntries.stream()
                .filter(Entry::isCompleted)
                .filter(e -> e.getUpdatedAt() != null)
                .filter(e -> !e.getUpdatedAt().toLocalDate().isBefore(weekAgo))
                .count();

        // Score formula: combination of activity and completion
        int activityScore = Math.min(50, (int)(createdThisWeek * 7));  // max 50 for creating stuff
        int completionScore = Math.min(50, (int)(completedThisWeek * 10)); // max 50 for completing

        return Math.min(100, activityScore + completionScore);
    }

    /**
     * Returns a map of entry type -> count for distribution charts.
     */
    public Map<String, Integer> getEntryDistribution() {
        Map<String, Integer> dist = new HashMap<>();
        dist.put("Notes", entryDAO.getEntriesByType(EntryType.NOTE).size());
        dist.put("Tasks", entryDAO.getEntriesByType(EntryType.TASK).size());
        dist.put("Reminders", entryDAO.getEntriesByType(EntryType.REMINDER).size());
        return dist;
    }

    /**
     * Returns daily completion counts for the last 7 days.
     */
    public Map<String, Integer> getWeeklyCompletionTrend() {
        Map<String, Integer> trend = new HashMap<>();
        LocalDate today = LocalDate.now();

        List<Entry> completed = entryDAO.getAllEntries().stream()
                .filter(Entry::isCompleted)
                .filter(e -> e.getUpdatedAt() != null)
                .collect(Collectors.toList());

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            String label = day.getDayOfWeek().toString().substring(0, 3);
            long count = completed.stream()
                    .filter(e -> e.getUpdatedAt().toLocalDate().equals(day))
                    .count();
            trend.put(label, (int) count);
        }
        return trend;
    }
}
