package com.example.fruitqualityprediction.preferences;

import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberryDetector;
import java.util.Set;

/**
 * A class that contains the processing preferences selected in the 'settings' tab.
 */
public class ProcessingPreferences {
    private final StrawberryDetector strawberryDetector; // The strawberry detector to be used
    private final int targetRipeness; // The target ripeness percentage
    private final String boundingBoxColorPreference; // Feature used for bounding box color
    private final boolean displayText; // Whether to display text alongside the annotations
    private final Set<String> selectedAttributes; // Selected attributes to display

    /**
     * Initializes the fields
     *
     * @param strawberryDetector The strawberry detector to be used
     * @param targetRipeness The target ripeness percentage
     * @param boundingBoxColorPreference Feature used for bounding box color
     * @param displayText Whether to display text alongside the annotations
     * @param selectedAttributes Selected attributes to display
     */
    public ProcessingPreferences(StrawberryDetector strawberryDetector, int targetRipeness, String boundingBoxColorPreference, boolean displayText, Set<String> selectedAttributes) {
        this.strawberryDetector = strawberryDetector;
        this.targetRipeness = targetRipeness;
        this.boundingBoxColorPreference = boundingBoxColorPreference;
        this.displayText = displayText;
        this.selectedAttributes = selectedAttributes;
    }

    /**
     * A getter for the strawberry detector.
     *
     * @return The strawberry detector to be used
     */
    public StrawberryDetector getStrawberryDetector() {
        return strawberryDetector;
    }

    /**
     * A getter for the target ripeness.
     *
     * @return The target ripeness percentage
     */
    public int getTargetRipeness() {
        return targetRipeness;
    }

    /**
     * A getter for the bounding box color preference.
     *
     * @return Feature used for bounding box color
     */
    public String getBoundingBoxColorPreference() {
        return boundingBoxColorPreference;
    }

    /**
     * A getter for the displayText.
     *
     * @return Whether to display text alongside the annotations
     */
    public boolean getDisplayText() {
        return displayText;
    }

    /**
     * A getter for the selectedAttributes.
     *
     * @return Selected attributes to display
     */
    public Set<String> getSelectedAttributes() {
        return selectedAttributes;
    }
}
