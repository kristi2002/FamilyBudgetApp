package it.unicam.cs.mpgc.jbudget120002.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class providing date and time formatting, parsing, and manipulation
 * methods for the Family Budget App. This class centralizes common date/time
 * operations to ensure consistency across the application.
 *
 * Responsibilities:
 * - Format and parse dates and times
 * - Provide date calculations and conversions
 * - Support custom date/time formats for UI and reports
 * - Handle time zone and locale-specific operations
 *
 * Usage:
 * Used throughout the application for consistent date/time handling in
 * models, services, controllers, and UI components.
 */
public class DateTimeUtils {
    public static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Format a LocalDateTime to string using the application's standard format.
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    /**
     * Format a LocalDate to string using the application's standard format.
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    /**
     * Parse a date string in the application's standard format to LocalDate.
     */
    public static LocalDate parseDate(String dateStr) {
        return dateStr != null && !dateStr.isEmpty() ? 
               LocalDate.parse(dateStr, DATE_FORMATTER) : null;
    }

    /**
     * Parse a datetime string in the application's standard format to LocalDateTime.
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr != null && !dateTimeStr.isEmpty() ? 
               LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER) : null;
    }
} 