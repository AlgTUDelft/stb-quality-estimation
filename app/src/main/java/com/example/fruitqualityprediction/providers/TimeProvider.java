package com.example.fruitqualityprediction.providers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Wrapper around LocalDateTime to allow for mocking and limit functionality.
 */
public class TimeProvider {

    private LocalDateTime dateTime; // The internal LocalDateTime value.

    /**
     * Creates a new TimeProvider based on LocalDateTime.now()
     */
    public TimeProvider() {
        dateTime = LocalDateTime.now();
    }

    /**
     * Sets the year of this time provider.
     *
     * @param year the year to set the time provider to.
     */
    public void setYear(int year) {
        dateTime = dateTime.withYear(year);
    }

    /**
     * Sets the month of this time provider.
     *
     * @param month the month to set the time provider to.
     */
    public void setMonth(int month) {
        dateTime = dateTime.withMonth(month);
    }

    /**
     * Sets the day of this time provider.
     *
     * @param day the day to set the time provider to.
     */
    public void setDay(int day) {
        dateTime = dateTime.withDayOfMonth(day);
    }

    /**
     * Sets the hour of this time provider.
     *
     * @param hour the hour to set the time provider to.
     */
    public void setHour(int hour) {
        dateTime = dateTime.withHour(hour);
    }

    /**
     * Sets the minute of this time provider.
     *
     * @param minute the minute to set the time provider to.
     */
    public void setMinute(int minute) {
        dateTime = dateTime.withMinute(minute);
    }

    /**
     * Sets the second of this time provider.
     *
     * @param second the second to set the time provider to.
     */
    public void setSecond(int second) {
        dateTime = dateTime.withSecond(second);
    }

    /**
     * Rounds the dateTime to the nearest hour, either rounding up or down, depending on which
     * time it is closest to. Used in order to allow the date to be queried in the environmental
     * data.
     */
    public void roundToNearestHour() {
        int minutes = dateTime.getMinute();
        if (minutes >= 30) {
            dateTime = dateTime.plusHours(1).withMinute(0).withSecond(0);
        } else {
            dateTime = dateTime.withMinute(0).withSecond(0);
        }
    }

    /**
     * Formats the given pattern into the correct format that is to be used in the environmental
     * data lookup.
     *
     * @param pattern the pattern which the dateTime should be formatted to.
     *
     * @return formatted dateTime.
     */
    public String format(String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
}