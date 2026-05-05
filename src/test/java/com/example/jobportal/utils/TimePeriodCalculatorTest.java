package com.example.jobportal.utils;

import com.example.jobportal.exception.InvalidPeriodException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for TimePeriodCalculator utility class
 * Tests time period calculation and validation for dashboard statistics filtering
 */
@DisplayName("TimePeriodCalculator Tests")
class TimePeriodCalculatorTest {

    @Test
    @DisplayName("calculateStartDate with 'today' should return start of current day")
    void calculateStartDate_today_returnsStartOfCurrentDay() {
        // When
        LocalDateTime result = TimePeriodCalculator.calculateStartDate("today");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.toLocalDate()).isEqualTo(LocalDateTime.now().toLocalDate());
        assertThat(result.getHour()).isEqualTo(0);
        assertThat(result.getMinute()).isEqualTo(0);
        assertThat(result.getSecond()).isEqualTo(0);
        assertThat(result.getNano()).isEqualTo(0);
    }

    @Test
    @DisplayName("calculateStartDate with 'this_week' should return start of current week (Monday)")
    void calculateStartDate_thisWeek_returnsStartOfCurrentWeek() {
        // When
        LocalDateTime result = TimePeriodCalculator.calculateStartDate("this_week");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result.getHour()).isEqualTo(0);
        assertThat(result.getMinute()).isEqualTo(0);
        assertThat(result.getSecond()).isEqualTo(0);
        assertThat(result.getNano()).isEqualTo(0);
        
        // Verify it's the Monday of the current week
        LocalDateTime expectedMonday = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate()
                .atStartOfDay();
        assertThat(result).isEqualTo(expectedMonday);
    }

    @Test
    @DisplayName("calculateStartDate with 'this_month' should return start of current month")
    void calculateStartDate_thisMonth_returnsStartOfCurrentMonth() {
        // When
        LocalDateTime result = TimePeriodCalculator.calculateStartDate("this_month");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDayOfMonth()).isEqualTo(1);
        assertThat(result.getHour()).isEqualTo(0);
        assertThat(result.getMinute()).isEqualTo(0);
        assertThat(result.getSecond()).isEqualTo(0);
        assertThat(result.getNano()).isEqualTo(0);
        
        // Verify it's the first day of the current month
        LocalDateTime expectedFirstDay = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .toLocalDate()
                .atStartOfDay();
        assertThat(result).isEqualTo(expectedFirstDay);
    }

    @Test
    @DisplayName("calculateStartDate with 'this_year' should return start of current year")
    void calculateStartDate_thisYear_returnsStartOfCurrentYear() {
        // When
        LocalDateTime result = TimePeriodCalculator.calculateStartDate("this_year");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMonthValue()).isEqualTo(1);
        assertThat(result.getDayOfMonth()).isEqualTo(1);
        assertThat(result.getHour()).isEqualTo(0);
        assertThat(result.getMinute()).isEqualTo(0);
        assertThat(result.getSecond()).isEqualTo(0);
        assertThat(result.getNano()).isEqualTo(0);
        
        // Verify it's January 1st of the current year
        LocalDateTime expectedFirstDay = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfYear())
                .toLocalDate()
                .atStartOfDay();
        assertThat(result).isEqualTo(expectedFirstDay);
    }

    @Test
    @DisplayName("calculateStartDate with 'all' should return null")
    void calculateStartDate_all_returnsNull() {
        // When
        LocalDateTime result = TimePeriodCalculator.calculateStartDate("all");
        
        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("calculateStartDate with null should return null")
    void calculateStartDate_null_returnsNull() {
        // When
        LocalDateTime result = TimePeriodCalculator.calculateStartDate(null);
        
        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("calculateStartDate with invalid period should throw InvalidPeriodException")
    void calculateStartDate_invalidPeriod_throwsException() {
        // When & Then
        assertThatThrownBy(() -> TimePeriodCalculator.calculateStartDate("invalid_period"))
                .isInstanceOf(InvalidPeriodException.class)
                .hasMessageContaining("Invalid time period: invalid_period");
    }

    @Test
    @DisplayName("calculateStartDate should be case-insensitive")
    void calculateStartDate_caseInsensitive_worksCorrectly() {
        // When
        LocalDateTime todayLower = TimePeriodCalculator.calculateStartDate("today");
        LocalDateTime todayUpper = TimePeriodCalculator.calculateStartDate("TODAY");
        LocalDateTime todayMixed = TimePeriodCalculator.calculateStartDate("ToDay");
        
        // Then
        assertThat(todayLower).isNotNull();
        assertThat(todayUpper).isNotNull();
        assertThat(todayMixed).isNotNull();
        assertThat(todayLower).isEqualTo(todayUpper);
        assertThat(todayLower).isEqualTo(todayMixed);
    }

    @Test
    @DisplayName("isValidPeriod should return true for valid periods")
    void isValidPeriod_validPeriods_returnsTrue() {
        // When & Then
        assertThat(TimePeriodCalculator.isValidPeriod("today")).isTrue();
        assertThat(TimePeriodCalculator.isValidPeriod("this_week")).isTrue();
        assertThat(TimePeriodCalculator.isValidPeriod("this_month")).isTrue();
        assertThat(TimePeriodCalculator.isValidPeriod("this_year")).isTrue();
        assertThat(TimePeriodCalculator.isValidPeriod("all")).isTrue();
    }

    @Test
    @DisplayName("isValidPeriod should return true for null")
    void isValidPeriod_null_returnsTrue() {
        // When & Then
        assertThat(TimePeriodCalculator.isValidPeriod(null)).isTrue();
    }

    @Test
    @DisplayName("isValidPeriod should return false for invalid periods")
    void isValidPeriod_invalidPeriods_returnsFalse() {
        // When & Then
        assertThat(TimePeriodCalculator.isValidPeriod("invalid")).isFalse();
        assertThat(TimePeriodCalculator.isValidPeriod("yesterday")).isFalse();
        assertThat(TimePeriodCalculator.isValidPeriod("last_week")).isFalse();
        assertThat(TimePeriodCalculator.isValidPeriod("")).isFalse();
        assertThat(TimePeriodCalculator.isValidPeriod("   ")).isFalse();
    }

    @Test
    @DisplayName("isValidPeriod should be case-insensitive")
    void isValidPeriod_caseInsensitive_returnsTrue() {
        // When & Then
        assertThat(TimePeriodCalculator.isValidPeriod("TODAY")).isTrue();
        assertThat(TimePeriodCalculator.isValidPeriod("This_Week")).isTrue();
        assertThat(TimePeriodCalculator.isValidPeriod("THIS_MONTH")).isTrue();
        assertThat(TimePeriodCalculator.isValidPeriod("this_YEAR")).isTrue();
        assertThat(TimePeriodCalculator.isValidPeriod("ALL")).isTrue();
    }

    @Test
    @DisplayName("calculateStartDate should handle edge case: first day of week")
    void calculateStartDate_onMonday_returnsCurrentDayStart() {
        // This test verifies that when today is Monday, this_week returns today at 00:00:00
        LocalDateTime now = LocalDateTime.now();
        
        // When
        LocalDateTime result = TimePeriodCalculator.calculateStartDate("this_week");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        
        // If today is Monday, result should be today at 00:00:00
        if (now.getDayOfWeek() == DayOfWeek.MONDAY) {
            assertThat(result.toLocalDate()).isEqualTo(now.toLocalDate());
        }
    }

    @Test
    @DisplayName("calculateStartDate should handle edge case: first day of month")
    void calculateStartDate_onFirstDayOfMonth_returnsCurrentDayStart() {
        // This test verifies that when today is the 1st, this_month returns today at 00:00:00
        LocalDateTime now = LocalDateTime.now();
        
        // When
        LocalDateTime result = TimePeriodCalculator.calculateStartDate("this_month");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDayOfMonth()).isEqualTo(1);
        
        // If today is the 1st, result should be today at 00:00:00
        if (now.getDayOfMonth() == 1) {
            assertThat(result.toLocalDate()).isEqualTo(now.toLocalDate());
        }
    }

    @Test
    @DisplayName("calculateStartDate should handle edge case: first day of year")
    void calculateStartDate_onFirstDayOfYear_returnsCurrentDayStart() {
        // This test verifies that when today is January 1st, this_year returns today at 00:00:00
        LocalDateTime now = LocalDateTime.now();
        
        // When
        LocalDateTime result = TimePeriodCalculator.calculateStartDate("this_year");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMonthValue()).isEqualTo(1);
        assertThat(result.getDayOfMonth()).isEqualTo(1);
        
        // If today is January 1st, result should be today at 00:00:00
        if (now.getMonthValue() == 1 && now.getDayOfMonth() == 1) {
            assertThat(result.toLocalDate()).isEqualTo(now.toLocalDate());
        }
    }

    @Test
    @DisplayName("TimePeriodCalculator constructor should throw UnsupportedOperationException")
    void constructor_shouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> {
            // Use reflection to access private constructor
            var constructor = TimePeriodCalculator.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        })
        .hasCauseInstanceOf(UnsupportedOperationException.class)
        .hasStackTraceContaining("Utility class cannot be instantiated");
    }
}
