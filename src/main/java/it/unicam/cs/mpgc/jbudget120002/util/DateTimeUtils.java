package it.unicam.cs.mpgc.jbudget120002.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Utility class providing date and time formatting, parsing, and manipulation
 * methods for the Family Budget App.
 * 
 * <p>This class centralizes common date/time operations to ensure consistency
 * across the application. It provides standardized formatting patterns and
 * utility methods for date calculations, conversions, and validations.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Format and parse dates and times</li>
 *   <li>Provide date calculations and conversions</li>
 *   <li>Support custom date/time formats for UI and reports</li>
 *   <li>Handle time zone and locale-specific operations</li>
 *   <li>Validate date/time inputs</li>
 *   <li>Calculate date ranges and periods</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Format dates for display
 * String formattedDate = DateTimeUtils.formatDate(LocalDate.now());
 * 
 * // Parse user input
 * LocalDate date = DateTimeUtils.parseDate("25/12/2023");
 * 
 * // Calculate date differences
 * long daysBetween = DateTimeUtils.daysBetween(startDate, endDate);
 * 
 * // Get month boundaries
 * LocalDate monthStart = DateTimeUtils.getMonthStart(date);
 * LocalDate monthEnd = DateTimeUtils.getMonthEnd(date);
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
public class DateTimeUtils {
    
    /** Standard date formatter for dd/MM/yyyy format */
    public static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /** Standard date-time formatter for dd/MM/yyyy HH:mm:ss format */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    /** ISO date formatter for yyyy-MM-dd format */
    public static final DateTimeFormatter ISO_DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /** Month-year formatter for MMMM yyyy format */
    public static final DateTimeFormatter MONTH_YEAR_FORMATTER = 
            DateTimeFormatter.ofPattern("MMMM yyyy");
    
    /** Short month-year formatter for MMM yyyy format */
    public static final DateTimeFormatter SHORT_MONTH_YEAR_FORMATTER = 
            DateTimeFormatter.ofPattern("MMM yyyy");

    // ==================== FORMATTING METHODS ====================

    /**
     * Format a LocalDateTime to string using the application's standard format.
     * 
     * @param dateTime the LocalDateTime to format
     * @return the formatted string, or empty string if dateTime is null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    /**
     * Format a LocalDate to string using the application's standard format.
     * 
     * @param date the LocalDate to format
     * @return the formatted string, or empty string if date is null
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    /**
     * Format a LocalDate to ISO format string.
     * 
     * @param date the LocalDate to format
     * @return the ISO formatted string, or empty string if date is null
     */
    public static String formatDateISO(LocalDate date) {
        return date != null ? date.format(ISO_DATE_FORMATTER) : "";
    }

    /**
     * Format a LocalDate to month-year format.
     * 
     * @param date the LocalDate to format
     * @return the month-year formatted string, or empty string if date is null
     */
    public static String formatMonthYear(LocalDate date) {
        return date != null ? date.format(MONTH_YEAR_FORMATTER) : "";
    }

    /**
     * Format a LocalDate to short month-year format.
     * 
     * @param date the LocalDate to format
     * @return the short month-year formatted string, or empty string if date is null
     */
    public static String formatShortMonthYear(LocalDate date) {
        return date != null ? date.format(SHORT_MONTH_YEAR_FORMATTER) : "";
    }

    // ==================== PARSING METHODS ====================

    /**
     * Parse a date string in the application's standard format to LocalDate.
     * 
     * @param dateStr the date string to parse
     * @return the parsed LocalDate, or null if dateStr is null or empty
     * @throws DateTimeParseException if the string cannot be parsed
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
    }

    /**
     * Parse a datetime string in the application's standard format to LocalDateTime.
     * 
     * @param dateTimeStr the datetime string to parse
     * @return the parsed LocalDateTime, or null if dateTimeStr is null or empty
     * @throws DateTimeParseException if the string cannot be parsed
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr.trim(), DATE_TIME_FORMATTER);
    }

    /**
     * Parse a date string in ISO format to LocalDate.
     * 
     * @param dateStr the ISO date string to parse
     * @return the parsed LocalDate, or null if dateStr is null or empty
     * @throws DateTimeParseException if the string cannot be parsed
     */
    public static LocalDate parseDateISO(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr.trim(), ISO_DATE_FORMATTER);
    }

    /**
     * Safely parse a date string, returning null if parsing fails.
     * 
     * @param dateStr the date string to parse
     * @return the parsed LocalDate, or null if parsing fails
     */
    public static LocalDate parseDateSafe(String dateStr) {
        try {
            return parseDate(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Safely parse a datetime string, returning null if parsing fails.
     * 
     * @param dateTimeStr the datetime string to parse
     * @return the parsed LocalDateTime, or null if parsing fails
     */
    public static LocalDateTime parseDateTimeSafe(String dateTimeStr) {
        try {
            return parseDateTime(dateTimeStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ==================== DATE CALCULATION METHODS ====================

    /**
     * Calculate the number of days between two dates.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return the number of days between the dates
     * @throws IllegalArgumentException if either date is null
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculate the number of months between two dates.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return the number of months between the dates
     * @throws IllegalArgumentException if either date is null
     */
    public static long monthsBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

    /**
     * Calculate the number of years between two dates.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return the number of years between the dates
     * @throws IllegalArgumentException if either date is null
     */
    public static long yearsBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        return ChronoUnit.YEARS.between(startDate, endDate);
    }

    // ==================== DATE RANGE METHODS ====================

    /**
     * Get the start of the month for a given date.
     * 
     * @param date the date
     * @return the first day of the month
     * @throws IllegalArgumentException if date is null
     */
    public static LocalDate getMonthStart(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.withDayOfMonth(1);
    }

    /**
     * Get the end of the month for a given date.
     * 
     * @param date the date
     * @return the last day of the month
     * @throws IllegalArgumentException if date is null
     */
    public static LocalDate getMonthEnd(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * Get the start of the year for a given date.
     * 
     * @param date the date
     * @return the first day of the year
     * @throws IllegalArgumentException if date is null
     */
    public static LocalDate getYearStart(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.withDayOfYear(1);
    }

    /**
     * Get the end of the year for a given date.
     * 
     * @param date the date
     * @return the last day of the year
     * @throws IllegalArgumentException if date is null
     */
    public static LocalDate getYearEnd(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.withDayOfYear(date.lengthOfYear());
    }

    /**
     * Get the start of the week (Monday) for a given date.
     * 
     * @param date the date
     * @return the Monday of the week
     * @throws IllegalArgumentException if date is null
     */
    public static LocalDate getWeekStart(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.with(java.time.DayOfWeek.MONDAY);
    }

    /**
     * Get the end of the week (Sunday) for a given date.
     * 
     * @param date the date
     * @return the Sunday of the week
     * @throws IllegalArgumentException if date is null
     */
    public static LocalDate getWeekEnd(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.with(java.time.DayOfWeek.SUNDAY);
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Check if a date is in the past.
     * 
     * @param date the date to check
     * @return true if the date is in the past, false otherwise
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Check if a date is in the future.
     * 
     * @param date the date to check
     * @return true if the date is in the future, false otherwise
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Check if a date is today.
     * 
     * @param date the date to check
     * @return true if the date is today, false otherwise
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * Check if a date range is valid (start date is before or equal to end date).
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return true if the date range is valid, false otherwise
     */
    public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Get the current date.
     * 
     * @return the current date
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Get the current date and time.
     * 
     * @return the current date and time
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Get a date representing the beginning of time (far in the past).
     * 
     * @return a date far in the past
     */
    public static LocalDate beginningOfTime() {
        return LocalDate.of(1900, 1, 1);
    }

    /**
     * Get a date representing the end of time (far in the future).
     * 
     * @return a date far in the future
     */
    public static LocalDate endOfTime() {
        return LocalDate.of(2100, 12, 31);
    }

    /**
     * Check if two dates are equal, handling null values.
     * 
     * @param date1 the first date
     * @param date2 the second date
     * @return true if the dates are equal, false otherwise
     */
    public static boolean equals(LocalDate date1, LocalDate date2) {
        return Objects.equals(date1, date2);
    }

    /**
     * Get the minimum of two dates, handling null values.
     * 
     * @param date1 the first date
     * @param date2 the second date
     * @return the minimum date, or null if both dates are null
     */
    public static LocalDate min(LocalDate date1, LocalDate date2) {
        if (date1 == null) return date2;
        if (date2 == null) return date1;
        return date1.isBefore(date2) ? date1 : date2;
    }

    /**
     * Get the maximum of two dates, handling null values.
     * 
     * @param date1 the first date
     * @param date2 the second date
     * @return the maximum date, or null if both dates are null
     */
    public static LocalDate max(LocalDate date1, LocalDate date2) {
        if (date1 == null) return date2;
        if (date2 == null) return date1;
        return date1.isAfter(date2) ? date1 : date2;
    }
} 