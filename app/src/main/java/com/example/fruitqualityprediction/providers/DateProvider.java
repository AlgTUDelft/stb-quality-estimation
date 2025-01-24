package com.example.fruitqualityprediction.providers;

import java.util.Date;

/**
 * A provider for the current date.
 */
public class DateProvider {

    /**
     * Provides the current date.
     *
     * @return The current date.
     */
    public Date getDate() {
        return new Date();
    }
}
