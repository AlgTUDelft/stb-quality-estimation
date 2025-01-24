package com.example.fruitqualityprediction.sbprocessing.segmentation;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Common utils for different segmentation methods.
 */
public class SegmentationUtils {

    /**
     * Resizes an image so that its largest side is equal to MAX_SIDE
     * while preserving its original proportions.
     *
     * @param image The original image
     * @param maxSide The largest side of the image after resizing
     *
     * @return The resized image
     */
    public static Mat resizeImage(Mat image, int maxSide) {
        int targetHeight;
        int targetWidth;

        if (image.width() >= image.height()) {
            targetWidth = maxSide;
            targetHeight = (int) ((double) targetWidth / image.width() * image.height());
        }
        else {
            targetHeight = maxSide;
            targetWidth = (int) ((double) targetHeight / image.height() * image.width());
        }

        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, new Size(targetWidth, targetHeight), 0, 0, Imgproc.INTER_AREA);
        return resizedImage;
    }
}
