package com.example.fruitqualityprediction.sbprocessing.marketability;

import android.graphics.Bitmap;
import android.util.Log;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculates the roundness of strawberries.
 */
public class RoundnessCalculator {

    /**
     * Determines the roundness of a strawberry.
     *
     * @param bitmap the strawberry segment image.
     *
     * @return the roundness of the strawberry.
     */
    public double calculateRoundness(Bitmap bitmap) {

        Mat image = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, image);

        // Convert the image to grayscale
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Apply threshold to create a binary image
        Mat binaryImage = new Mat();
        Imgproc.threshold(grayImage, binaryImage, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        // Find contours in the binary image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binaryImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        // Find the largest contour (assuming it is the strawberry)
        double maxArea = 0;
        MatOfPoint largestContour = null;
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                largestContour = contour;
            }
        }


        // Calculate the roundness of the strawberry
        double strawberryArea = maxArea;
        double strawberryRoundness = 0;
        if (largestContour != null) {
            // Find the minimum enclosing circle
            Point center = new Point();
            float[] radius = new float[1];
            Imgproc.minEnclosingCircle(new MatOfPoint2f(largestContour.toArray()), center, radius);
            // Calculate the area of the minimum enclosing circle
            double circleArea = Math.PI * Math.pow(radius[0], 2);
            // Calculate the roundness value
            strawberryRoundness = strawberryArea / circleArea;
        }


        Log.d("roundness", Double.toString(strawberryRoundness));
        return strawberryRoundness;
    }
}