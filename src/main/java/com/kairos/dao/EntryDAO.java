package com.kairos.dao;

import com.kairos.model.Entry;
import com.kairos.model.Entry.EntryType;
import com.kairos.model.Entry.Priority;
import com.kairos.service.EncryptionService;
import com.kairos.util.DateUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@link Entry} entities.
 *
 * <p>All database interactions for the {@code entries} table flow through
 * this class. Content is transparently encrypted/decrypted using
 * {@link EncryptionService} on every read and write.
 *
 * @author Kairos
 * @version 1.0.0
 */
public class EntryDAO {

    // ─────────────────────────────────────────────────────────────────────────
    // SQL Constants
    // ─────────────────────────────────────────────────────────────────────────

    private static final String INSERT_SQL =
            "INSERT INTO entries (title, content, type, priority, tags, " +
            "created_at, updated_at, reminder_time, is_completed, encrypted_content) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE entries SET title=?, content=?, type=?, priority=?, tags=?, " +
            "updated_at=?, reminder_time=?, is_completed=?, encrypted_content=? " +
            "WHERE id=?";

    private static final String DELETE_SQL =
            "DELETE FROM entries WHERE id=?";

    private static final String SELECT_BY_ID_SQL =
            "SELECT * FROM entries WHERE id=?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM entries ORDER BY updated_at DESC";

    private static final String SELECT_BY_TYPE_SQL =
            "SELECT * FROM entries WHERE type=? ORDER BY updated_at DESC";

    private static final String SEARCH_SQL =
            "SELECT * FROM entries WHERE " +
            "LOWER(title) LIKE ? OR LOWER(content) LIKE ? OR LOWER(tags) LIKE ? " +
            "ORDER BY updated_at DESC";

    private static final String SELECT_BY_PRIORITY_SQL =
            "SELECT * FROM entries WHERE priority=? ORDER BY updated_at DESC";

    private static final String MARK_COMPLETED_SQL =
            "UPDATE entries SET is_completed=1, updated_at=? WHERE id=?";

    // ─────────────────────────────────────────────────────────────────────────
    // Dependencies
    // ─────────────────────────────────────────────────────────────────────────

    private final DatabaseManager dbManager;
    private final EncryptionService encryptionService;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates an {@code EntryDAO} using the application-wide singletons.
     */
    public EntryDAO() {
        this.dbManager         = DatabaseManager.getInstance();
        this.encryptionService = new EncryptionService();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Write Operations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Persists a new {@link Entry} to the database and returns the generated
     * primary key.
     *
     * @param entry the entry to insert (id is ignored; assigned by DB)
     * @return the generated {@code id}, or {@code -1} on failure
     */
    public int insertEntry(Entry entry) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {

            String encrypted = encryptionService.encrypt(entry.getContent());
            entry.setEncryptedContent(encrypted);

            ps.setString(1, entry.getTitle());
            ps.setString(2, entry.getContent());
            ps.setString(3, entry.getType().name());
            ps.setString(4, entry.getPriority().name());
            ps.setString(5, entry.getTags());
            ps.setString(6, DateUtil.formatForDB(entry.getCreatedAt()));
            ps.setString(7, DateUtil.formatForDB(entry.getUpdatedAt()));
            ps.setString(8, entry.getReminderTime() != null
                    ? DateUtil.formatForDB(entry.getReminderTime()) : null);
            ps.setInt   (9, entry.isCompleted() ? 1 : 0);
            ps.setString(10, encrypted);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    entry.setId(generatedId);
                    return generatedId;
                }
            }
        } catch (Exception e) {
            System.err.println("[EntryDAO] insertEntry failed: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates all mutable fields of an existing {@link Entry}.
     *
     * @param entry the entry to update (must have a valid {@code id})
     * @return {@code true} if at least one row was updated
     */
    public boolean updateEntry(Entry entry) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            entry.setUpdatedAt(LocalDateTime.now());
            String encrypted = encryptionService.encrypt(entry.getContent());
            entry.setEncryptedContent(encrypted);

            ps.setString(1, entry.getTitle());
            ps.setString(2, entry.getContent());
            ps.setString(3, entry.getType().name());
            ps.setString(4, entry.getPriority().name());
            ps.setString(5, entry.getTags());
            ps.setString(6, DateUtil.formatForDB(entry.getUpdatedAt()));
            ps.setString(7, entry.getReminderTime() != null
                    ? DateUtil.formatForDB(entry.getReminderTime()) : null);
            ps.setInt   (8, entry.isCompleted() ? 1 : 0);
            ps.setString(9, encrypted);
            ps.setInt   (10, entry.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[EntryDAO] updateEntry failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes the entry with the given {@code id}.
     *
     * @param id primary key of the entry to remove
     * @return {@code true} if the row was deleted
     */
    public boolean deleteEntry(int id) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[EntryDAO] deleteEntry failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Marks the entry with the given {@code id} as completed.
     *
     * @param id primary key of the entry to complete
     * @return {@code true} if the row was updated
     */
    public boolean markAsCompleted(int id) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(MARK_COMPLETED_SQL)) {

            ps.setString(1, DateUtil.formatForDB(LocalDateTime.now()));
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[EntryDAO] markAsCompleted failed: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Read Operations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves a single entry by its primary key.
     *
     * @param id the entry's database id
     * @return the matching {@link Entry}, or {@code null} if not found
     */
    public Entry getEntryById(int id) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            System.err.println("[EntryDAO] getEntryById failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all entries ordered by most-recently updated first.
     *
     * @return list of all {@link Entry} objects; empty list on error
     */
    public List<Entry> getAllEntries() {
        List<Entry> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (Exception e) {
            System.err.println("[EntryDAO] getAllEntries failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all entries of a given {@link EntryType}.
     *
     * @param type the entry type filter
     * @return filtered list; empty list on error
     */
    public List<Entry> getEntriesByType(EntryType type) {
        List<Entry> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_TYPE_SQL)) {

            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.err.println("[EntryDAO] getEntriesByType failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Full-text search across {@code title}, {@code content}, and {@code tags}.
     * The search is case-insensitive.
     *
     * @param keyword the search term
     * @return matching entries; empty list if none found or on error
     */
    public List<Entry> searchEntries(String keyword) {
        List<Entry> list = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) return getAllEntries();

        String pattern = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SEARCH_SQL)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.err.println("[EntryDAO] searchEntries failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all entries with the given {@link Priority}.
     *
     * @param priority the priority filter
     * @return filtered list; empty list on error
     */
    public List<Entry> getEntriesByPriority(Priority priority) {
        List<Entry> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_PRIORITY_SQL)) {

            ps.setString(1, priority.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.err.println("[EntryDAO] getEntriesByPriority failed: " + e.getMessage());
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mapping Helper
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Maps the current row of a {@link ResultSet} to an {@link Entry} object.
     * Decrypts the stored content on the way out.
     *
     * @param rs an open {@link ResultSet} positioned at the desired row
     * @return a populated {@link Entry}
     * @throws SQLException if any column cannot be read
     */
    private Entry mapRow(ResultSet rs) throws Exception {
        Entry entry = new Entry();

        entry.setId       (rs.getInt   ("id"));
        entry.setTitle    (rs.getString("title"));
        entry.setType     (EntryType.valueOf(rs.getString("type")));
        entry.setPriority (Priority.valueOf(rs.getString("priority")));
        entry.setTags     (rs.getString("tags"));
        entry.setCompleted(rs.getInt   ("is_completed") == 1);

        entry.setCreatedAt(DateUtil.parseFromDB(rs.getString("created_at")));
        entry.setUpdatedAt(DateUtil.parseFromDB(rs.getString("updated_at")));

        String reminderStr = rs.getString("reminder_time");
        if (reminderStr != null && !reminderStr.isEmpty()) {
            entry.setReminderTime(DateUtil.parseFromDB(reminderStr));
        }

        // Decrypt stored content; fall back to raw content column on failure
        String encryptedContent = rs.getString("encrypted_content");
        entry.setEncryptedContent(encryptedContent);
        if (encryptedContent != null && !encryptedContent.isEmpty()) {
            try {
                entry.setContent(encryptionService.decrypt(encryptedContent));
            } catch (Exception ex) {
                // Decryption failed — use plain content column as fallback
                entry.setContent(rs.getString("content"));
            }
        } else {
            entry.setContent(rs.getString("content"));
        }

        return entry;
    }
}
