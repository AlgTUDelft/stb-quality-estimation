package com.example.fruitqualityprediction.sbprocessing.ripeness;

import android.graphics.Bitmap;
import android.graphics.Color;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Class responsible for calculating ripeness of a strawberry segment.
 */
public class RipenessCalculator {

    private static final int IMAGE_SIZE = 200; // The image size used for calculation.
    private static final int CIELAB_LOWER_BOUND = -128; // The lower bound of CIELAB.
    private static final int CIELAB_UPPER_BOUND = 127; // The upper bound of CIELAB.

    /**
     * This method is responsible for converting a set of RGB values to their corresponding
     * CIELAB values.
     *
     * @param r red channel value.
     * @param g green channel value.
     * @param b blue channel value.
     *
     * @return array of converted CIELAB values.
     */
    public static double[] rgbToLab(double r, double g, double b) {
        double red = r / 255.0;
        double green = g / 255.0;
        double blue = b / 255.0;


        Mat rgbPixel = new Mat(1, 1, CvType.CV_32FC3, new Scalar(red, green, blue));
        Mat labPixel = new Mat();
        Imgproc.cvtColor(rgbPixel, labPixel, Imgproc.COLOR_RGB2Lab);

        return labPixel.get(0, 0);
    }

    /**
     * This function calculates the ripeness for a segment of a strawberry.
     *
     * @param image the image of a strawberry of which to calculate the ripeness.
     *
     * @return the ripeness score.
     */
    public double calculateRipeness(Bitmap image) {
        // Get the starting width and height of the input image
        int h = image.getHeight();
        int w = image.getWidth();

        // Calculate the desired starting width and height for the center of the picture
        int startH = Math.max(h / 2 - (int)(IMAGE_SIZE / 6), 0);
        int startW = Math.max(w / 2 - (int)(IMAGE_SIZE / 6), 0);

        // Calculate the desired ending width and height for the center of the picture
        int endH = Math.min(h / 2 + (int)(IMAGE_SIZE / 6), h);
        int endW = Math.min(w / 2 + (int)(IMAGE_SIZE / 6), w);

        // Calculate the actual width and height for the zoomed-in region
        int actualWidth = endW - startW;
        int actualHeight = endH - startH;

        // Create a new bitmap from the original image, starting at startW and startH
        // and spanning actualWidth and actualHeight
        Bitmap middle = Bitmap.createBitmap(image, startW, startH, actualWidth, actualHeight);

        // Resize the bitmap to zoom 3x
        Bitmap zoomedBitmap = Bitmap.createScaledBitmap(middle, actualWidth * 3, actualHeight * 3, false);

        // Create the sum for each RGB channel (red, green, and blue)
        double[] rgbSums = new double[3];
        for (int i = 0; i < zoomedBitmap.getWidth(); i++) {
            for (int j = 0; j < zoomedBitmap.getHeight(); j++) {
                int color = zoomedBitmap.getPixel(i, j);
                rgbSums[0] += Color.red(color);
                rgbSums[1] += Color.green(color);
                rgbSums[2] += Color.blue(color);
            }
        }

        // Calculate the mean for each RGB channel
        long pixelCount = (long) zoomedBitmap.getWidth() * zoomedBitmap.getHeight();
        double[] rgbMeans = new double[3];
        for (int i = 0; i < 3; i++) {
            rgbMeans[i] = rgbSums[i] / pixelCount;
        }

        // Calculate the CIELAB equivalent for the mean RGB values
        double[] labValues = rgbToLab(rgbMeans[0], rgbMeans[1], rgbMeans[2]);
        double lightness = labValues[0];
        double redness = labValues[1]; //'a' channel for a CIELAB color value represents redness

        return calculateRipenessFromRedness(redness, lightness);
    }

    /**
     * This method computes the score of ripeness, which starts at 0 meaning very little ripeness,
     * going up to 1, which means fully ripe.
     *
     * @param redness the redness of the strawberry.
     * @param lightness the lightness of the strawberry.
     *
     * @return the ripeness of the strawberry.
     */
    public double calculateRipenessFromRedness(double redness, double lightness) {
        if (redness < 0) {
            return 0; //If value falls below threshold, mark as zero redness (0)
        } else if (redness < CIELAB_UPPER_BOUND) {
            //Normalized value by dividing result by total range
            return (redness * (redness / lightness) - CIELAB_LOWER_BOUND) / (CIELAB_UPPER_BOUND - CIELAB_LOWER_BOUND);
        } else {
            return 1; //If value exceeds threshold, mark as maximum redness (1)
        }
    }
}
