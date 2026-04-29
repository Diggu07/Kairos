package com.kairos.model;

import com.kairos.model.Entry.EntryType;
import com.kairos.model.Entry.Priority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of the NLP Intent Detection analysis.
 */
public class DetectedIntent {

    private EntryType suggestedType;
    private double confidenceScore;
    private String extractedTitle;
    private String extractedContent;
    private Priority suggestedPriority;
    private LocalDateTime suggestedReminderTime;
    private List<String> extractedTags = new ArrayList<>();
    private Map<String, String> entities = new HashMap<>();

    public DetectedIntent() {
        this.suggestedType = EntryType.NOTE;
        this.confidenceScore = 0.0;
        this.suggestedPriority = Priority.MEDIUM;
    }

    public Entry toEntry() {
        Entry e = new Entry();
        e.setType(this.suggestedType != null ? this.suggestedType : EntryType.NOTE);
        e.setPriority(this.suggestedPriority != null ? this.suggestedPriority : Priority.MEDIUM);
        e.setTitle(this.extractedTitle != null ? this.extractedTitle : "New " + e.getType().name());
        e.setContent(this.extractedContent != null ? this.extractedContent : "");
        e.setReminderTime(this.suggestedReminderTime);
        
        if (!this.extractedTags.isEmpty()) {
            e.setTags(String.join(",", this.extractedTags));
        } else {
            e.setTags("");
        }
        
        return e;
    }

    public String getConfidencePercentage() {
        return String.format("%.0f%%", this.confidenceScore * 100);
    }

    // Getters and Setters
    public EntryType getSuggestedType() { return suggestedType; }
    public void setSuggestedType(EntryType suggestedType) { this.suggestedType = suggestedType; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = Math.max(0.0, Math.min(1.0, confidenceScore)); }

    public String getExtractedTitle() { return extractedTitle; }
    public void setExtractedTitle(String extractedTitle) { this.extractedTitle = extractedTitle; }

    public String getExtractedContent() { return extractedContent; }
    public void setExtractedContent(String extractedContent) { this.extractedContent = extractedContent; }

    public Priority getSuggestedPriority() { return suggestedPriority; }
    public void setSuggestedPriority(Priority suggestedPriority) { this.suggestedPriority = suggestedPriority; }

    public LocalDateTime getSuggestedReminderTime() { return suggestedReminderTime; }
    public void setSuggestedReminderTime(LocalDateTime suggestedReminderTime) { this.suggestedReminderTime = suggestedReminderTime; }

    public List<String> getExtractedTags() { return extractedTags; }
    public void setExtractedTags(List<String> extractedTags) { this.extractedTags = extractedTags; }
    public void addTag(String tag) { if(!this.extractedTags.contains(tag)) this.extractedTags.add(tag); }

    public Map<String, String> getEntities() { return entities; }
    public void setEntities(Map<String, String> entities) { this.entities = entities; }
    public void addEntity(String type, String value) { this.entities.put(type, value); }
}
