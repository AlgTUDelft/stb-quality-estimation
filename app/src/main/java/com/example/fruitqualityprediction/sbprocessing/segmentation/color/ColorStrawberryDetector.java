package com.example.fruitqualityprediction.sbprocessing.segmentation.color;

import android.util.Log;
import com.example.fruitqualityprediction.sbprocessing.segmentation.SegmentationUtils;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberryDetector;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that extracts strawberries from an image using color segmentation.
 */
public class ColorStrawberryDetector implements StrawberryDetector {

    private static final int MAX_SIDE = 1024; // The maximum size for a segment.
    private static final double MIN_RATIO = 0.5; // Minimum shape ratio.
    private static final double MAX_RATIO = 1.5; // Maximum shape ratio.
    private static final double MIN_AREA = 10000; // Minimum shape area.
    private static final double MIN_AREA_RATIO = 0.5; // Minimum shape area ratio.

    /**
     * Detects red strawberries in an image using color segmentation.
     *
     * @param image an image containing strawberries.
     *
     * @return a list of strawberries.
     */
    @Override
    public List<StrawberrySegment> detectStrawberries(Mat image) {
        Mat resizedImage = SegmentationUtils.resizeImage(image, MAX_SIDE);

        Mat mask = generateMask(resizedImage);
        mask = filterMask(mask);
        return extractStrawberries(image, mask);
    }

    /**
     * Extracts strawberry-shaped segments from an image using a mask.
     *
     * @param image the image to extract from.
     * @param mask the mask to apply.
     *
     * @return list of cropped strawberries with bounding boxes.
     */
    public List<StrawberrySegment> extractStrawberries(Mat image, Mat mask) {
        Mat resizedMask = new Mat();
        Imgproc.resize(mask, resizedMask, new Size(image.width(), image.height()), 0, 0, Imgproc.INTER_AREA);
        List<Mat> segments = segmentBinaryImage(resizedMask);

        ArrayList<StrawberrySegment> strawberries = new ArrayList<>();
        for (Mat segment : segments) {
            if (Core.sumElems(segment).val[0] == 0) {
                continue;
            }
            MatOfPoint indices = new MatOfPoint();
            Core.findNonZero(segment, indices);
            Rect boundingRect = Imgproc.boundingRect(indices);
            Mat croppedMask = new Mat(segment, boundingRect);

            double height = croppedMask.height(), width = croppedMask.width();
            double ratio = height / width;
            double area = Core.sumElems(segment).val[0] / 255;

            if (ratio > MIN_RATIO && ratio < MAX_RATIO && area > MIN_AREA && area > MIN_AREA_RATIO * width * height) {
                strawberries.add(new StrawberrySegment(boundingRect));
            }
        }
        Log.d("Number of strawberries: ", String.valueOf(strawberries.size()));
        return strawberries;
    }

    /**
     * Filters out noise from the mask.
     *
     * @param inputMask the initial mask as a binary image.
     *
     * @return the filtered mask.
     */
    public Mat filterMask(Mat inputMask) {
        Mat mask = inputMask.clone();
        Mat mask_reduced = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(30, 30));
        Imgproc.morphologyEx(mask, mask_reduced, Imgproc.MORPH_OPEN, kernel);
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(50, 50));
        Imgproc.morphologyEx(mask_reduced, mask_reduced, Imgproc.MORPH_DILATE, kernel);
        Core.bitwise_and(mask, mask_reduced, mask);

        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(20, 20));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(mask, contours, i, new Scalar(255), Core.FILLED);
        }

        return mask;
    }

    /**
     * Generates mask to extract red strawberries from an image.
     *
     * @param image the input image.
     *
     * @return the corresponding mask.
     */
    public Mat generateMask(Mat image) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_RGB2HSV);

        Scalar cmin_low = new Scalar(0, 60, 100);
        Scalar cmax_low = new Scalar(10, 255, 255);

        Scalar cmin_high = new Scalar(165, 60, 100);
        Scalar cmax_high = new Scalar(180, 255, 255);

        Mat mask1 = new Mat();
        Core.inRange(hsv, cmin_low, cmax_low, mask1);

        Mat mask2 = new Mat();
        Core.inRange(hsv, cmin_high, cmax_high, mask2);

        Mat mask = new Mat();
        Core.bitwise_or(mask1, mask2, mask);
        return mask;
    }

    /**
     * Transforms a binary image into a list of disconnected segments.
     *
     * @param binaryImage the binary image.
     *
     * @return all disconnected segments within the image as separate images.
     */
    public List<Mat> segmentBinaryImage(Mat binaryImage) {
        Mat img = binaryImage.clone();
        List<Mat> segments = new ArrayList<>();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(img, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {
            Mat segmentMask = Mat.zeros(img.size(), img.type());
            Imgproc.drawContours(segmentMask, contours, i, new Scalar(255), Core.FILLED);
            Mat segment = new Mat();
            Core.bitwise_and(img, segmentMask, segment);
            Core.bitwise_xor(img, segment, img);
            segments.add(segment);
        }
        return segments;
    }
}