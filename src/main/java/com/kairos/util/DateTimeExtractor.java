package com.kairos.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting dates/times from natural language.
 */
public class DateTimeExtractor {

    /**
     * Attempts to extract a LocalDateTime from natural language text.
     */
    public static LocalDateTime extractDateTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        
        String lower = text.toLowerCase();
        
        // 1. Try relative dates
        LocalDateTime relative = parseRelativeDates(lower);
        if (relative != null) return relative;
        
        // 2. Try simple time matching (e.g. 5pm)
        LocalDateTime timeMatch = parseSimpleTime(lower);
        if (timeMatch != null) return timeMatch;

        return null;
    }

    private static LocalDateTime parseRelativeDates(String text) {
        LocalDateTime now = LocalDateTime.now();
        
        if (text.contains("tomorrow")) {
            return now.plusDays(1).withHour(9).withMinute(0).withSecond(0);
        } else if (text.contains("next week")) {
            return now.plusWeeks(1).withHour(9).withMinute(0).withSecond(0);
        } else if (text.contains("someday")) {
            return now.plusMonths(1);
        }
        
        Pattern inHoursPattern = Pattern.compile("in (\\d+) hours?");
        Matcher m1 = inHoursPattern.matcher(text);
        if (m1.find()) {
            int hours = Integer.parseInt(m1.group(1));
            return now.plusHours(hours);
        }
        
        Pattern inMinsPattern = Pattern.compile("in (\\d+) mins?");
        Matcher m2 = inMinsPattern.matcher(text);
        if (m2.find()) {
            int mins = Integer.parseInt(m2.group(1));
            return now.plusMinutes(mins);
        }
        
        Pattern nextDayPattern = Pattern.compile("next (monday|tuesday|wednesday|thursday|friday|saturday|sunday)");
        Matcher m3 = nextDayPattern.matcher(text);
        if (m3.find()) {
            String day = m3.group(1).toUpperCase();
            return now.with(TemporalAdjusters.next(DayOfWeek.valueOf(day))).withHour(9).withMinute(0).withSecond(0);
        }

        return null;
    }

    private static LocalDateTime parseSimpleTime(String text) {
        LocalDateTime now = LocalDateTime.now();
        
        // Match things like 5pm, 5 pm, 10:30am, 10:30 am
        Pattern timePattern = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)");
        Matcher matcher = timePattern.matcher(text);
        
        if (matcher.find()) {
            int hour = Integer.parseInt(matcher.group(1));
            int minute = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
            String ampm = matcher.group(3);
            
            if (hour == 12 && ampm.equals("am")) {
                hour = 0;
            } else if (hour < 12 && ampm.equals("pm")) {
                hour += 12;
            }
            
            LocalDateTime matchedTime = now.withHour(hour).withMinute(minute).withSecond(0);
            
            // If the time already passed today, assume tomorrow
            if (matchedTime.isBefore(now)) {
                return matchedTime.plusDays(1);
            }
            return matchedTime;
        }
        
        return null;
    }
}
