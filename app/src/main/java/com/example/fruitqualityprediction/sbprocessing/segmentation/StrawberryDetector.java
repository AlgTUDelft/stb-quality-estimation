package com.example.fruitqualityprediction.sbprocessing.segmentation;

import org.opencv.core.Mat;
import java.util.List;

/**
 * Detects strawberries in an image.
 */
public interface StrawberryDetector {
    /**
     * Finds the bounding boxes of the strawberries in an image.
     *
     * @param image the input image.
     *
     * @return the list of bounding boxes.
     */
    List<StrawberrySegment> detectStrawberries(Mat image);
}
