package com.example.fruitqualityprediction.preferences;

/**
 * A class that contains the visualisation preferences selected in the 'settings' tab.
 */
public class VisualisationPreferences {
    private final String ripenessFunction; // Ripeness over time
    private final double ripenessMin; // Minimum displayed ripeness value
    private final double ripenessMax; // Maximum displayed ripeness value
    private final double timeMin; // Minimum displayed time value
    private final double timeMax; // Maximum displayed time value
    private final String timeUnit; // Units in which the time value is displayed

    /**
     * Initializes the fields.
     *
     * @param ripenessFunction Ripeness over time
     * @param ripenessMin Minimum displayed ripeness value
     * @param ripenessMax Maximum displayed ripeness value
     * @param timeMin Minimum displayed time value
     * @param timeMax Maximum displayed time value
     * @param timeUnit Units in which the time value is displayed
     */
    public VisualisationPreferences(String ripenessFunction, double ripenessMin, double ripenessMax, double timeMin, double timeMax, String timeUnit) {
        this.ripenessFunction = ripenessFunction;
        this.ripenessMin = ripenessMin;
        this.ripenessMax = ripenessMax;
        this.timeMin = timeMin;
        this.timeMax = timeMax;
        this.timeUnit = timeUnit;
    }

    /**
     * A getter for the ripeness function.
     *
     * @return Ripeness over time
     */
    public String getRipenessFunction() {
        return ripenessFunction;
    }

    /**
     * A getter for the ripeness minimum.
     *
     * @return Minimum displayed ripeness value
     */
    public double getRipenessMin() {
        return ripenessMin;
    }

    /**
     * A getter for the ripeness maximum.
     *
     * @return Maximum displayed ripeness value
     */
    public double getRipenessMax() {
        return ripenessMax;
    }

    /**
     * A getter for the time minimum.
     *
     * @return Minimum displayed time value
     */
    public double getTimeMin() {
        return timeMin;
    }

    /**
     * A getter for the time maximum.
     *
     * @return Maximum displayed time value
     */
    public double getTimeMax() {
        return timeMax;
    }

    /**
     * A getter for the time units.
     *
     * @return Units in which the time value is displayed
     */
    public String getTimeUnit() {
        return timeUnit;
    }
}
