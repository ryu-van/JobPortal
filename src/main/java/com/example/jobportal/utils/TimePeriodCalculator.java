package com.example.jobportal.utils;

import com.example.jobportal.exception.InvalidPeriodException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for calculating time period start dates for dashboard statistics filtering.
 * All calculations use UTC timezone for consistency.
 */
public class TimePeriodCalculator {

    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final List<String> VALID_PERIODS = Arrays.asList(
            "today", "this_week", "this_month", "this_year", "all"
    );

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private TimePeriodCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Calculates the start date based on the specified time period.
     *
     * @param period The time period filter. Valid values: "today", "this_week", "this_month", "this_year", "all", or null
     * @return LocalDateTime representing the start of the period, or null for "all" or null period
     * @throws InvalidPeriodException if the period value is invalid
     */
    public static LocalDateTime calculateStartDate(String period) {
        // Handle null or "all" - no filtering
        if (period == null || "all".equalsIgnoreCase(period)) {
            return null;
        }

        // Validate period
        if (!isValidPeriod(period)) {
            throw new InvalidPeriodException(period);
        }

        LocalDateTime now = LocalDateTime.now(UTC);

        switch (period.toLowerCase()) {
            case "today":
                // Start of current day (00:00:00)
                return now.toLocalDate().atStartOfDay();

            case "this_week":
                // Start of current week (Monday 00:00:00)
                return now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .toLocalDate()
                        .atStartOfDay();

            case "this_month":
                // Start of current month (1st day 00:00:00)
                return now.with(TemporalAdjusters.firstDayOfMonth())
                        .toLocalDate()
                        .atStartOfDay();

            case "this_year":
                // Start of current year (January 1st 00:00:00)
                return now.with(TemporalAdjusters.firstDayOfYear())
                        .toLocalDate()
                        .atStartOfDay();

            default:
                // This should never happen due to validation, but included for safety
                throw new InvalidPeriodException(period);
        }
    }

    /**
     * Validates if the provided period value is valid.
     *
     * @param period The time period to validate
     * @return true if the period is valid, false otherwise
     */
    public static boolean isValidPeriod(String period) {
        if (period == null) {
            return true; // null is valid (treated as "all")
        }
        return VALID_PERIODS.stream()
                .anyMatch(validPeriod -> validPeriod.equalsIgnoreCase(period));
    }
}
