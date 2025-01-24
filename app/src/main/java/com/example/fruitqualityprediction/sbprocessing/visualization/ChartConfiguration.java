package com.example.fruitqualityprediction.sbprocessing.visualization;

import com.example.fruitqualityprediction.sbprocessing.FeatureRange;
import org.opencv.core.Size;
import java.util.Objects;

/**
 * A class that contains the chart configuration details.
 */
public class ChartConfiguration {
    private final String functionString;
    private final Size imageSize;
    private final FeatureRange<Double> xRange;
    private final FeatureRange<Double> yRange;
    private final String xLabel;
    private final String yLabel;

    /**
     * Initializes the configuration.
     *
     * @param functionString A string which describes a mathematical function.
     *                       The only variable must be 'x'
     * @param imageSize The size of the generated image
     * @param xRange The range of x values to be displayed
     * @param yRange The range of y values to be displayed
     * @param xLabel The label of the x axis
     * @param yLabel The label of the y axis
     */
    public ChartConfiguration(String functionString,
                              Size imageSize,
                              FeatureRange<Double> xRange,
                              FeatureRange<Double> yRange, String xLabel, String yLabel) {
        this.functionString = functionString;
        this.imageSize = imageSize;
        this.xRange = xRange;
        this.yRange = yRange;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
    }

    /**
     * A getter for the functionString.
     *
     * @return A string which describes a mathematical function.
     */
    public String getFunctionString() {
        return functionString;
    }

    /**
     * A getter for the imageSize.
     *
     * @return The size of the generated image
     */
    public Size getImageSize() {
        return imageSize;
    }

    /**
     * A getter for the xRange.
     *
     * @return The range of x values to be displayed
     */
    public FeatureRange<Double> getXRange() {
        return xRange;
    }

    /**
     * A getter for the yRange.
     *
     * @return The range of y values to be displayed
     */
    public FeatureRange<Double> getYRange() {
        return yRange;
    }

    /**
     * A getter for the xLabel.
     *
     * @return The label of the x axis
     */
    public String getXLabel() {
        return xLabel;
    }

    /**
     * A getter for the yLabel.
     *
     * @return The label of the y axis
     */
    public String getYLabel() {
        return yLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChartConfiguration that = (ChartConfiguration) o;
        return functionString.equals(that.functionString) && imageSize.equals(that.imageSize) && xRange.equals(that.xRange) && yRange.equals(that.yRange) && xLabel.equals(that.xLabel) && yLabel.equals(that.yLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionString, imageSize, xRange, yRange, xLabel, yLabel);
    }
}
