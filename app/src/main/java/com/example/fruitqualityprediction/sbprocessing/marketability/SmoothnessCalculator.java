package com.example.fruitqualityprediction.sbprocessing.marketability;

import android.graphics.Bitmap;
import android.util.Log;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgproc.Imgproc;

/**
 * Calculates the smoothness of strawberries.
 */
public class SmoothnessCalculator {

    /**
     * Deterines the smoothness of a strawberry.
     *
     * @param bitmap the strawberry segment image.
     *
     * @return the smoothness of the strawberry.
     */
    public double calculateSmoothness(Bitmap bitmap) {

        // Load the image
        Mat image = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, image);

        // Convert the image to grayscale
        Mat src = new Mat();
        Imgproc.cvtColor(image, src, Imgproc.COLOR_BGR2GRAY);

        // Creating an empty matrix for destination image
        Mat gradientX = new Mat();
        Mat gradientY = new Mat();

        //applying sobel derivative with x=1, y=0
        Imgproc.Sobel(src, gradientX, CvType.CV_64F, 0, 1);

        // Applying sobel derivative with x=0, y=1
        Imgproc.Sobel(src, gradientY, CvType.CV_64F, 1, 0);

        // Compute gradient magnitude of the image
        Mat gradientMagnitude = new Mat();
        Core.magnitude(gradientX, gradientY, gradientMagnitude);

        // Calculate the mean and standard deviation of the gradient magnitude
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble standardDeviation = new MatOfDouble();
        Core.meanStdDev(gradientMagnitude, mean, standardDeviation);

        double variance = Math.pow(standardDeviation.get(0, 0)[0], 2);

        // Normalize variance to [0, 1]
        double minVariance = 0.0; // Minimum possible variance
        double maxVariance = 255.0 * 255.0; // Maximum possible variance
        double normalizedVariance = (variance - minVariance) / (maxVariance - minVariance);
        Log.d("smoothness", Double.toString(normalizedVariance));

        return normalizedVariance;
    }
}