package com.example.fruitqualityprediction.sbprocessing;

import java.util.Objects;

/**
 * A class that scores the allowed range for a given feature.
 *
 * @param <T> the numeric/comparable type to use.
 */
public class FeatureRange<T extends Comparable<T>> {

    private final T min; // The minimum value for this feature.
    private final T max; // The maximum value for this feature.

    /**
     * A constructor that sets the limits of the range.
     *
     * @param min the minimum value.
     * @param max the maximum value.
     */
    public FeatureRange(T min, T max) {
        this.min = min;
        this.max = max;
    }

    /**
     * A getter for the minimum value.
     *
     * @return the minimum value.
     */
    public T getMin() {
        return min;
    }

    /**
     * A getter for the maximum value.
     *
     * @return the maximum value.
     */
    public T getMax() {
        return max;
    }

    /**
     * Checks of a given value is in range.
     *
     * @param val the value to test for.
     *
     * @return whether the value is in range.
     */
    public boolean contains(T val) {
        return min.compareTo(val) <= 0 && max.compareTo(val) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureRange<?> that = (FeatureRange<?>) o;
        return min.equals(that.min) && max.equals(that.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }
}
