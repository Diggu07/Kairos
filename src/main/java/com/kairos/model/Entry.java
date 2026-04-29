package com.kairos.model;

import java.time.LocalDateTime;

/**
 * Universal Entry model representing a Note, Task, or Reminder in Kairos.
 * All productivity items share this common data structure to simplify
 * storage, retrieval, and type conversion across the application.
 *
 * @author Kairos
 * @version 1.0.0
 */
public class Entry {

    // ─────────────────────────────────────────────────────────────────────────
    // Enums
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Represents the type of a Kairos entry.
     */
    public enum EntryType {
        NOTE, TASK, REMINDER
    }

    /**
     * Represents the priority level of a Kairos entry.
     */
    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fields
    // ─────────────────────────────────────────────────────────────────────────

    /** Unique database identifier. 0 means not yet persisted. */
    private int id;

    /** Short descriptive title of the entry (required). */
    private String title;

    /** Full plain-text content of the entry. */
    private String content;

    /** Category of this entry: NOTE, TASK, or REMINDER. */
    private EntryType type;

    /** Importance level: LOW, MEDIUM, or HIGH. */
    private Priority priority;

    /** Comma-separated tags for flexible categorisation, e.g. "work,urgent". */
    private String tags;

    /** Timestamp when this entry was first created. */
    private LocalDateTime createdAt;

    /** Timestamp of the most recent update to this entry. */
    private LocalDateTime updatedAt;

    /**
     * Optional date/time at which a reminder should fire.
     * Only meaningful when {@code type == EntryType.REMINDER}.
     * May be {@code null} for NOTE and TASK entries.
     */
    private LocalDateTime reminderTime;

    /** Whether this task/reminder has been completed or dismissed. */
    private boolean isCompleted;

    /**
     * AES-256 encrypted version of {@code content}, stored as a
     * Base64-encoded string. Populated by {@link com.kairos.service.EncryptionService}.
     */
    private String encryptedContent;

    /** Timestamp of when the user was notified of this reminder. */
    private LocalDateTime notifiedAt;

    /** Number of times the user has been notified. */
    private int notificationCount;

    /** Timestamp of the last time this reminder was snoozed. */
    private LocalDateTime lastSnoozedAt;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Full constructor used when loading an existing entry from the database
     * (includes the persisted {@code id}).
     *
     * @param id               database primary key
     * @param title            entry title
     * @param content          plain-text content
     * @param type             entry type (NOTE / TASK / REMINDER)
     * @param priority         importance level
     * @param tags             comma-separated tag string
     * @param createdAt        creation timestamp
     * @param updatedAt        last-update timestamp
     * @param reminderTime     optional reminder fire time (nullable)
     * @param isCompleted      completion status
     * @param encryptedContent AES-encrypted content string
     * @param notifiedAt       Timestamp of when the user was notified
     * @param notificationCount Number of times notified
     * @param lastSnoozedAt    Timestamp of last snooze
     */
    public Entry(int id, String title, String content, EntryType type,
                 Priority priority, String tags, LocalDateTime createdAt,
                 LocalDateTime updatedAt, LocalDateTime reminderTime,
                 boolean isCompleted, String encryptedContent,
                 LocalDateTime notifiedAt, int notificationCount, LocalDateTime lastSnoozedAt) {
        this.id               = id;
        this.title            = title;
        this.content          = content;
        this.type             = type;
        this.priority         = priority;
        this.tags             = tags;
        this.createdAt        = createdAt;
        this.updatedAt        = updatedAt;
        this.reminderTime     = reminderTime;
        this.isCompleted      = isCompleted;
        this.encryptedContent = encryptedContent;
        this.notifiedAt       = notifiedAt;
        this.notificationCount = notificationCount;
        this.lastSnoozedAt    = lastSnoozedAt;
    }

    /**
     * Legacy constructor used when loading an existing entry from the database
     * without notification fields.
     */
    public Entry(int id, String title, String content, EntryType type,
                 Priority priority, String tags, LocalDateTime createdAt,
                 LocalDateTime updatedAt, LocalDateTime reminderTime,
                 boolean isCompleted, String encryptedContent) {
        this(id, title, content, type, priority, tags, createdAt, updatedAt, 
             reminderTime, isCompleted, encryptedContent, null, 0, null);
    }

    /**
     * Constructor for creating a new entry before it has been persisted
     * (no {@code id} — the DB will assign one on insert).
     *
     * @param title        entry title
     * @param content      plain-text content
     * @param type         entry type
     * @param priority     importance level
     * @param tags         comma-separated tag string
     * @param reminderTime optional reminder fire time (nullable)
     */
    public Entry(String title, String content, EntryType type,
                 Priority priority, String tags, LocalDateTime reminderTime) {
        this.title        = title;
        this.content      = content;
        this.type         = type;
        this.priority     = priority;
        this.tags         = tags;
        this.reminderTime = reminderTime;
        this.createdAt    = LocalDateTime.now();
        this.updatedAt    = LocalDateTime.now();
        this.isCompleted  = false;
    }

    /** Default no-arg constructor for JavaFX property binding and frameworks. */
    public Entry() {
        this.createdAt   = LocalDateTime.now();
        this.updatedAt   = LocalDateTime.now();
        this.isCompleted = false;
        this.priority    = Priority.MEDIUM;
        this.type        = EntryType.NOTE;
        this.notificationCount = 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Business Methods
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a shallow copy of this entry converted to the given
     * {@code newType}. The {@code id} is reset to 0 so the converted
     * entry is treated as a new record if persisted.
     *
     * @param newType the desired {@link EntryType} for the copy
     * @return a new {@code Entry} instance with {@code type} set to {@code newType}
     */
    public Entry convertTo(EntryType newType) {
        Entry copy = new Entry(
                this.title,
                this.content,
                newType,
                this.priority,
                this.tags,
                this.reminderTime
        );
        copy.setEncryptedContent(this.encryptedContent);
        copy.setCompleted(this.isCompleted);
        copy.setCreatedAt(this.createdAt);
        copy.setUpdatedAt(LocalDateTime.now());
        copy.setNotifiedAt(this.notifiedAt);
        copy.setNotificationCount(this.notificationCount);
        copy.setLastSnoozedAt(this.lastSnoozedAt);
        return copy;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public EntryType getType() { return type; }
    public void setType(EntryType type) { this.type = type; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalDateTime reminderTime) { this.reminderTime = reminderTime; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getEncryptedContent() { return encryptedContent; }
    public void setEncryptedContent(String encryptedContent) { this.encryptedContent = encryptedContent; }

    public LocalDateTime getNotifiedAt() { return notifiedAt; }
    public void setNotifiedAt(LocalDateTime notifiedAt) { this.notifiedAt = notifiedAt; }

    public int getNotificationCount() { return notificationCount; }
    public void setNotificationCount(int notificationCount) { this.notificationCount = notificationCount; }

    public LocalDateTime getLastSnoozedAt() { return lastSnoozedAt; }
    public void setLastSnoozedAt(LocalDateTime lastSnoozedAt) { this.lastSnoozedAt = lastSnoozedAt; }

    // ─────────────────────────────────────────────────────────────────────────
    // toString
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a human-readable summary of this entry, useful for logging
     * and debugging.
     */
    @Override
    public String toString() {
        return "Entry{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", priority=" + priority +
                ", tags='" + tags + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", reminderTime=" + reminderTime +
                ", isCompleted=" + isCompleted +
                '}';
    }
}
