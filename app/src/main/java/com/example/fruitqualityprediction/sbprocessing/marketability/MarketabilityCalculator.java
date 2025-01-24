package com.example.fruitqualityprediction.sbprocessing.marketability;

import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;

/**
 * Calculates the marketability of strawberries.
 */
public class MarketabilityCalculator {

    // The smoothness threshold after which strawberries are too smooth, and not marketable.
    private static final double SMOOTHNESS_THRESHOLD = 0.15;

    // The roundness threshold from which strawberries are considered marketable.
    private static final double ROUNDNESS_THRESHOLD = 0.1;

    // The ripeness threshold from which strawberries are considered marketable.
    private static final double RIPENESS_THRESHOLD = 0.6;


    /**
     * Determines whether the strawberry is marketable.
     *
     * @param strawberrySegment A strawberry segment
     *
     * @return whether the strawberry is marketable.
     */
    public boolean isMarketable(StrawberrySegment strawberrySegment) {
        if ((strawberrySegment.getRoundness() == null) ||
            (strawberrySegment.getSmoothness() == null) ||
            (strawberrySegment.getRipeness() == null)) {
            return false;
        }

        return strawberrySegment.getRoundness() >= ROUNDNESS_THRESHOLD &&
                strawberrySegment.getSmoothness() <= SMOOTHNESS_THRESHOLD &&
                strawberrySegment.getRipeness() >= RIPENESS_THRESHOLD;
    }
}