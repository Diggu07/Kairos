package com.kairos.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date/time formatting and parsing in Kairos.
 *
 * <p>Centralises all date formatting so that display strings and database
 * storage strings are consistent across the entire application.
 *
 * @author Kairos
 * @version 1.0.0
 */
public final class DateUtil {

    // ─────────────────────────────────────────────────────────────────────────
    // Formatters
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Human-readable display format: {@code "Mar 21, 2026 · 10:30 AM"}.
     */
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy · hh:mm a");

    /**
     * ISO-8601 storage format used in SQLite: {@code "2026-03-21T10:30:00"}.
     */
    private static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Short date-only display: {@code "Mar 21, 2026"}.
     */
    private static final DateTimeFormatter SHORT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor (private — utility class)
    // ─────────────────────────────────────────────────────────────────────────

    /** Prevent instantiation of this utility class. */
    private DateUtil() {}

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Formats a {@link LocalDateTime} for human-readable display.
     *
     * <p>Example output: {@code "Mar 21, 2026 · 10:30 AM"}
     *
     * @param dt the date/time to format; {@code null} returns {@code "—"}
     * @return formatted display string
     */
    public static String formatDisplay(LocalDateTime dt) {
        if (dt == null) return "—";
        return dt.format(DISPLAY_FORMATTER);
    }

    /**
     * Formats a {@link LocalDateTime} as an ISO-8601 string suitable for
     * storage in a SQLite TEXT column.
     *
     * <p>Example output: {@code "2026-03-21T10:30:00"}
     *
     * @param dt the date/time to format; {@code null} returns {@code null}
     * @return ISO string, or {@code null}
     */
    public static String formatForDB(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.format(DB_FORMATTER);
    }

    /**
     * Parses an ISO-8601 string (as stored in SQLite) back to a
     * {@link LocalDateTime}.
     *
     * @param s the ISO string from the database
     * @return the parsed {@link LocalDateTime}, or {@code null} if blank/invalid
     */
    public static LocalDateTime parseFromDB(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s, DB_FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("[DateUtil] Could not parse date from DB: '" + s + "'");
            return null;
        }
    }

    /**
     * Returns {@code true} if the given date/time falls on today's calendar date.
     *
     * @param dt the date/time to test; {@code null} returns {@code false}
     * @return {@code true} iff {@code dt} is today
     */
    public static boolean isToday(LocalDateTime dt) {
        if (dt == null) return false;
        return dt.toLocalDate().isEqual(LocalDate.now());
    }

    /**
     * Returns {@code true} if the given date/time is strictly in the past
     * (before the current moment).
     *
     * @param dt the date/time to test; {@code null} returns {@code false}
     * @return {@code true} iff {@code dt} is before {@link LocalDateTime#now()}
     */
    public static boolean isPast(LocalDateTime dt) {
        if (dt == null) return false;
        return dt.isBefore(LocalDateTime.now());
    }

    /**
     * Returns {@code true} if the given date/time is strictly in the future.
     *
     * @param dt the date/time to test; {@code null} returns {@code false}
     * @return {@code true} iff {@code dt} is after {@link LocalDateTime#now()}
     */
    public static boolean isFuture(LocalDateTime dt) {
        if (dt == null) return false;
        return dt.isAfter(LocalDateTime.now());
    }

    /**
     * Returns a short date-only string for compact display.
     *
     * <p>Example: {@code "Mar 21, 2026"}
     *
     * @param dt the date/time; {@code null} returns {@code "—"}
     * @return short date string
     */
    public static String formatShortDate(LocalDateTime dt) {
        if (dt == null) return "—";
        return dt.format(SHORT_DATE_FORMATTER);
    }
}
