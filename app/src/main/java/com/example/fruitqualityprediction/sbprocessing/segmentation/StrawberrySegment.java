package com.example.fruitqualityprediction.sbprocessing.segmentation;

import android.graphics.Bitmap;
import org.opencv.core.Rect;
import java.io.Serializable;
import java.util.Objects;

/**
 * A class to contain a detected strawberry.
 */
public class StrawberrySegment implements Serializable {

    private final Rect boundingBox; // The bounding box of this segment.

    private transient  Bitmap bitmap; // The bitmap of this segment.
    private Double ripeness; // The ripeness of this segment.
    private Float brix; // The Brix of this segment.
    private Float firmness; // The firmness of this segment.
    private Double roundness; // The roundness of this segment.
    private Boolean marketability; // The marketability of this segment.
    private Double smoothness; // The smoothness of this segment.

    /**
     * A constructor that creates a new strawberry segment.
     *
     * @param boundingBox the bounding box of the segment.
     */
    public StrawberrySegment(Rect boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     * A getter for the strawberry image.
     *
     * @return a bitmap containing the cropped strawberry.
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /**
     * A setter for the strawberry bitmap.
     *
     * @param strawberryBitmap the strawberry bitmap.
     */
    public void setBitmap(Bitmap strawberryBitmap) {
        this.bitmap = strawberryBitmap;
    }

    /**
     * A getter for the bounding box.
     *
     * @return the bounding box showing the location of the strawberry in the original image.
     */
    public Rect getBoundingBox() {
        return boundingBox;
    }

    /**
     * A getter for the ripeness value.
     *
     * @return the ripeness value.
     */
    public Double getRipeness() {
        return ripeness;
    }

    /**
     * A setter for the ripeness value.
     *
     * @param ripeness the ripeness value.
     */
    public void setRipeness(Double ripeness) {
        this.ripeness = ripeness;
    }

    /**
     * A getter for the brix value.
     *
     * @return the brix value.
     */
    public Float getBrix() {
        return brix;
    }

    /**
     * A setter for the brix value.
     *
     * @param brix the brix value.
     */
    public void setBrix(Float brix) {
        this.brix = brix;
    }

    /**
     * A getter for the firmness value.
     *
     * @return the firmness value.
     */
    public Float getFirmness() {
        return firmness;
    }

    /**
     * A setter for the firmness value.
     *
     * @param firmness the firmness value.
     */
    public void setFirmness(Float firmness) {
        this.firmness = firmness;
    }

    /**
     * A getter for the roundness value.
     *
     * @return the roundness value.
     */
    public Double getRoundness() {
        return roundness;
    }

    /**
     * A setter for the roundness value.
     *
     * @param roundness the roundness value.
     */
    public void setRoundness(double roundness) {
        this.roundness = roundness;
    }

    /**
     * A getter for the marketability value.
     *
     * @return the marketability.
     */
    public Boolean getMarketability() {
        return marketability;
    }

    /**
     * Converts the marketability to yes/no.
     *
     * @return a String representation of the marketability.
     */
    public String getMarketabilityAsString() {
        return this.marketability ? "Yes" : "No";
    }

    /**
     * Converts the marketability to 0 or 1.
     *
     * @return the marketability as an integer.
     */
    public int getMarketabilityAsInteger() {
        return this.marketability ? 1 : 0;
    }

    /**
     * A setter for the marketability value.
     * @param marketability the marketability.
     */
    public void setMarketability(boolean marketability) {
        this.marketability = marketability;
    }

    /**
     * A getter for the smoothness value.
     *
     * @return the smoothness value.
     */
    public Double getSmoothness() {
        return smoothness;
    }

    /**
     * A setter for the smoothness value.
     *
     * @param smoothness the smoothness value.
     */
    public void setSmoothness(Double smoothness) {
        this.smoothness = smoothness;
    }

    /**
     * Whether two segments are equal.
     *
     * @param o the other segment to compare against.
     *
     * @return whether the segments are one and the same.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StrawberrySegment segment = (StrawberrySegment) o;
        return Objects.equals(boundingBox, segment.boundingBox) &&
                Objects.equals(bitmap, segment.bitmap) &&
                Objects.equals(ripeness, segment.ripeness) &&
                Objects.equals(brix, segment.brix) &&
                Objects.equals(firmness, segment.firmness) &&
                Objects.equals(roundness, segment.roundness) &&
                Objects.equals(marketability, segment.marketability) &&
                Objects.equals(smoothness, segment.smoothness);
    }

    /**
     * Generates a unique hash code for this segment.
     *
     * @return an integer hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(boundingBox, bitmap, ripeness, brix, firmness, roundness, marketability, smoothness);
    }
}
