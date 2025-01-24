package com.example.fruitqualityprediction.sbprocessing.visualization;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Common utils for image visualisation.
 */
public class VisualizationUtils {

    /**
     * Used for calculating the color of the bounding box. Strawberries with a low ripeness score
     * are given a more red border, while better strawberries with a higher score get a border that
     * turns green.
     *
     * @param qualityAttribute the ripeness score ([0,1])
     * @param min The minimum qualityAttribute value
     * @param max The maximum qualityAttribute value
     *
     * @return corresponding hsv color for the border
     */
    public static Scalar interpolateColor(double qualityAttribute, double min, double max) {
        qualityAttribute = (qualityAttribute - min) / (max - min); // Normalize
        double H = qualityAttribute * 60; // Hue ranges from 120 (green) to 0 (red)
        if (H < 0) {
            H = 0; // Set hue to 0 for fully ripe strawberries
        }
        double S = 255;
        double V = 255;

        Mat hsv = new Mat(1,1, CvType.CV_8UC3, new Scalar(H,S,V));
        Mat rgb = new Mat();
        Imgproc.cvtColor(hsv, rgb, Imgproc.COLOR_HSV2RGB);

        return new Scalar(rgb.get(0, 0));
    }
}
