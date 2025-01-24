package com.example.fruitqualityprediction.sbprocessing;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.example.fruitqualityprediction.providers.PreferenceProvider;
import com.example.fruitqualityprediction.providers.TimeProvider;
import com.example.fruitqualityprediction.sbprocessing.marketability.MarketabilityCalculator;
import com.example.fruitqualityprediction.sbprocessing.marketability.RoundnessCalculator;
import com.example.fruitqualityprediction.sbprocessing.marketability.SmoothnessCalculator;
import com.example.fruitqualityprediction.sbprocessing.calculator.firmness.FirmnessCalculator;
import com.example.fruitqualityprediction.sbprocessing.ripeness.RipenessCalculator;
import com.example.fruitqualityprediction.sbprocessing.calculator.brix.BrixCalculator;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberryDetector;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;
import com.example.fruitqualityprediction.sbprocessing.visualization.Annotator;
import com.example.fruitqualityprediction.sbprocessing.visualization.ChartGenerator;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that handles the strawberry image processing
 */
public class ImageProcessor {

    private static final int MAX_PIXELS = 2048;

    private final Annotator annotator; // The annotator instance used for this processing round.
    private final Context context; // The current context.
    private final PreferenceProvider preferenceProvider;
    private final String imageName;

    private List<StrawberrySegment> strawberrySegments; // All strawberry segments in the image.
    private Mat image; // The full image.

    /**
     * The base constructor.
     *
     * @param context The current Android context.
     * @param preferenceProvider The current preference provider.
     * @param imageName The filename of the image.
     */
    private ImageProcessor(Context context, PreferenceProvider preferenceProvider, String imageName) {
        this.annotator = new Annotator(preferenceProvider, imageName);
        this.context = context;
        this.preferenceProvider = preferenceProvider;
        this.imageName = imageName;
    }

    /**
     * Creates a new image processor.
     *
     * @param bitmap The bitmap to process.
     * @param context The current Android context.
     * @param preferenceProvider The current preference provider.
     * @param imageName The filename of the image.
     */
    public ImageProcessor(Bitmap bitmap, Context context, PreferenceProvider preferenceProvider, String imageName) {
        this(context, preferenceProvider, imageName);
        Mat matImage = new Mat();
        Utils.bitmapToMat(bitmap, matImage);
        this.image = resizeImage(matImage, MAX_PIXELS);
    }

    /**
     * Creates a new image processor.
     *
     * @param image the image to process.
     * @param context the current Android context.
     * @param preferenceProvider The current preference provider.
     * @param imageName The filename of the image.
     */
    public ImageProcessor(Mat image, Context context, PreferenceProvider preferenceProvider, String imageName) {
        this(context, preferenceProvider, imageName);
        this.image = resizeImage(image, MAX_PIXELS);
    }

    /**
     * A getter for the list of strawberry segments.
     *
     * @return the current list of segments in the image processor.
     */
    public List<StrawberrySegment> getStrawberrySegments() {
        return strawberrySegments;
    }

    /**
     * Detect segments in the current image.
     */
    public void detectSegments() {
        StrawberryDetector strawberryDetector = this.preferenceProvider.getProcessingPreferences().getStrawberryDetector();
        this.strawberrySegments = strawberryDetector.detectStrawberries(image);
    }

    /**
     * Import a list of already detected segments.
     *
     * @param importedSegments the list of segments to be imported.
     */
    public void importBoundingBoxes(List<StrawberrySegment> importedSegments) {
        this.strawberrySegments = new ArrayList<>();
        for (StrawberrySegment importedSegment : importedSegments) {
            this.strawberrySegments.add(new StrawberrySegment(importedSegment.getBoundingBox()));
        }
    }

    /**
     * Extracts strawberry images from the original image using the bounding boxes of the segments.
     */
    public void extractStrawberryImages() {
        int imageWidth = image.cols();
        int imageHeight = image.rows();

        for (StrawberrySegment strawberrySegment : this.strawberrySegments) {
            // Get the bounding box coordinates
            int x = strawberrySegment.getBoundingBox().x;
            int y = strawberrySegment.getBoundingBox().y;
            int width = strawberrySegment.getBoundingBox().width;
            int height = strawberrySegment.getBoundingBox().height;

            // Adjust the bounding box coordinates to stay within image boundaries
            if (x < 0) {
                width += x; // Adjust width if x is negative
                x = 0; // Set x to 0
            }
            if (y < 0) {
                height += y; // Adjust height if y is negative
                y = 0; // Set y to 0
            }
            if (x + width > imageWidth) {
                width = imageWidth - x; // Adjust width if it exceeds image width
            }
            if (y + height > imageHeight) {
                height = imageHeight - y; // Adjust height if it exceeds image height
            }

            // Create the strawberry Mat and Bitmap using adjusted bounding box coordinates
            Rect adjustedBoundingBox = new Rect(x, y, width, height);
            Mat strawberryMat = new Mat(image, adjustedBoundingBox);
            Bitmap strawberryBitmap = Bitmap.createBitmap(strawberryMat.cols(), strawberryMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(strawberryMat, strawberryBitmap);
            strawberrySegment.setBitmap(strawberryBitmap);
        }
    }

    /**
     * Calculates the ripeness of all detected segments.
     */
    public void calculateRipeness() {
        RipenessCalculator ripenessCalculator = new RipenessCalculator();
        for (StrawberrySegment strawberrySegment : this.strawberrySegments) {
            strawberrySegment.setRipeness(ripenessCalculator.calculateRipeness(strawberrySegment.getBitmap()));
        }
    }

    /**
     * Calculates the Brix of all detected segments.
     */
    public void calculateBrix() {
    BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        for (StrawberrySegment strawberrySegment : this.strawberrySegments) {
            float brix = brixCalculator.calculateBrix(brixCalculator.getTime(imageName), strawberrySegment.getBitmap());
            strawberrySegment.setBrix(brix);
        }
    }

    /**
     * Calculates the firmness of all detected segments.
     */
    public void calculateFirmness() {
        FirmnessCalculator firmnessCalculator = new FirmnessCalculator(context, new TimeProvider(), preferenceProvider);

        for (StrawberrySegment strawberrySegment : this.strawberrySegments) {
            float firmness = firmnessCalculator.calculateFirmnessFromView(firmnessCalculator.getTime(imageName), strawberrySegment.getBitmap());
            strawberrySegment.setFirmness(firmness);
        }
    }

    /**
     * Calculates the roundness of all detected segments.
     */
    public void calculateRoundness() {
        RoundnessCalculator roundnessCalculator = new RoundnessCalculator();
        for (StrawberrySegment strawberrySegment : this.strawberrySegments) {
            double roundness = roundnessCalculator.calculateRoundness(strawberrySegment.getBitmap());
            strawberrySegment.setRoundness(roundness);
        }
    }

    /**
     * Calculates the marketability of all detected segments
     */
    public void calculateMarketability() {
        MarketabilityCalculator marketabilityCalculator = new MarketabilityCalculator();
        for (StrawberrySegment strawberrySegment : this.strawberrySegments) {
            boolean marketability = marketabilityCalculator.isMarketable(strawberrySegment);
            strawberrySegment.setMarketability(marketability);
        }
    }

    /**
     * calculates the smoothness of all detected segments
     */
    public void calculateSmoothness() {
        SmoothnessCalculator smoothnessCalculator = new SmoothnessCalculator();
        for (StrawberrySegment strawberrySegment : this.strawberrySegments) {
            double smoothness = smoothnessCalculator.calculateSmoothness(strawberrySegment.getBitmap());
            strawberrySegment.setSmoothness(smoothness);
        }
    }

    /**
     * Draw bounding boxes over the original image, display it in an image view and set up click events.
     *
     * @param imageView        the image view where the annotated image will be displayed.
     *
     * @return the annotated bitmap.
     */
    public Bitmap annotate(ImageView imageView) {
        String qualityAttribute = this.preferenceProvider.getProcessingPreferences().getBoundingBoxColorPreference();

        // Set up the annotator with the desired bounding box color
        if (qualityAttribute != null) {
            switch (qualityAttribute) {
                case "Ripeness" -> calculateRipeness();
                case "Brix" -> calculateBrix();
                case "Marketability" -> calculateMarketability();
                case "Roundness" -> calculateRoundness();
                case "Smoothness" -> calculateSmoothness();
            }
        }

        // Perform the annotation
        Bitmap bitmap = annotator.overlayBoundingBoxes(image, strawberrySegments);
        annotator.updateImageview(imageView, bitmap);

        return bitmap;
    }

    /**
     * Resizes the Mat image in case it exceeds the maxPixels parameter.
     *
     * @param image     the Mat image to resize.
     * @param maxPixels the maximum amount of allowed pixels.
     *
     * @return the (possibly resized) image.
     */
    public Mat resizeImage(Mat image, int maxPixels) {
        int originalWidth = image.cols();
        int originalHeight = image.rows();

        if (originalWidth > maxPixels || originalHeight > maxPixels) {
            int newWidth, newHeight;
            if (originalWidth > originalHeight) {
                newWidth = maxPixels;
                newHeight = (int) (originalHeight / (float) originalWidth * newWidth);
            } else {
                newHeight = maxPixels;
                newWidth = (int) (originalWidth / (float) originalHeight * newHeight);
            }

            // Resize the image
            Mat resizedImage = new Mat();
            Imgproc.resize(image, resizedImage, new Size(newWidth, newHeight));
            return resizedImage;
        } else {
            // Image is smaller, return the original image
            return image;
        }
    }

    /**
     * Processes a bitmap and returns a bitmap annotated with bounding boxes and segment data.
     *
     * @param imageView the image view to annotate in.
     *
     * @return the annotated bitmap.
     */
    public Bitmap process(ImageView imageView) {
        this.detectSegments();
        this.extractStrawberryImages();
        this.calculateBrix();
        this.calculateFirmness();
        this.calculateRipeness();
        this.calculateRoundness();
        this.calculateSmoothness();
        this.calculateMarketability();

        return this.annotate(imageView);
    }

    /**
     * A setter for the chart generator.
     *
     * @param chartGenerator The new chart generator
     */
    public void setChartGenerator(ChartGenerator chartGenerator) {
        this.annotator.setChartGenerator(chartGenerator);
    }
}
