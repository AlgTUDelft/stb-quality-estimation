package com.example.fruitqualityprediction.feedback;

import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * This class serves as a template for the json object
 */
public class JsonData {
    @SerializedName("image_date")
    public String timeStamp;
    public List<StrawberrySegment> strawberrySegments;

    /**
     * Initializes the fields.
     *
     * @param timeStamp of when the image was taken
     * @param strawberrySegments is the list of segmented strawberries in the image
     */
    public JsonData(String timeStamp, List<StrawberrySegment> strawberrySegments) {
        this.timeStamp = timeStamp;
        this.strawberrySegments = strawberrySegments;
    }
}
