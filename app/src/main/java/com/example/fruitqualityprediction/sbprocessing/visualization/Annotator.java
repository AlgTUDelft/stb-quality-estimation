package com.example.fruitqualityprediction.sbprocessing.visualization;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.example.fruitqualityprediction.UriUtils;
import com.example.fruitqualityprediction.feedback.FeedbackSender;
import com.example.fruitqualityprediction.R;
import com.example.fruitqualityprediction.providers.DateProvider;
import com.example.fruitqualityprediction.providers.PreferenceProvider;
import com.example.fruitqualityprediction.providers.TimeProvider;
import com.example.fruitqualityprediction.sbprocessing.FeatureRange;
import com.example.fruitqualityprediction.sbprocessing.calculator.brix.BrixCalculator;
import com.example.fruitqualityprediction.sbprocessing.calculator.firmness.FirmnessCalculator;
import com.example.fruitqualityprediction.sbprocessing.marketability.MarketabilityCalculator;
import com.example.fruitqualityprediction.sbprocessing.marketability.RoundnessCalculator;
import com.example.fruitqualityprediction.sbprocessing.marketability.SmoothnessCalculator;
import com.example.fruitqualityprediction.sbprocessing.ripeness.RipenessCalculator;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Annotates an image and sets up the click event dialogues.
 */
public class Annotator {

    public static final FeatureRange<Double> RIPENESS_RANGE = new FeatureRange<>(0.0, 1.0); // The range of ripeness values.
    public static final FeatureRange<Float> BRIX_RANGE = new FeatureRange<>(0f, 12f); // The range of Brix values.
    public static final FeatureRange<Double> MARKETABILITY_RANGE = new FeatureRange<>(0.0, 1.0); // The range of marketability values.

    private final PreferenceProvider preferenceProvider;
    private final Map<Rect, StrawberrySegment> boundingBoxInfoMap = new HashMap<>(); // Maps bounding boxes to segments.
    private final String imageName;

    private ChartGenerator chartGenerator; // Generates sigmoid charts.

    /**
     * Create a new Annotator instance.
     *
     * @param preferenceProvider The preference provider.
     * @param imageName The filename of the image.
     */
    public Annotator(PreferenceProvider preferenceProvider, String imageName) {
        this.preferenceProvider = preferenceProvider;
        this.imageName = imageName;
    }

    /**
     * A setter for the chart generator.
     *
     * @param chartGenerator The new chart generator
     */
    public void setChartGenerator(ChartGenerator chartGenerator) {
        this.chartGenerator = chartGenerator;
    }

    /**
     * This method is responsible for taking an array of ColorExtractorStrawberrySegment's, which are
     * a combination of a bounding box and a corresponding image, and annotates a bitmap, which
     * represents the original image, with the bounding boxes containing relevant information.
     *
     * @param image        the original image.
     * @param strawberries the list of strawberry objects.
     *
     * @return the bitmap with overlaid bounding boxes.
     */
    public Bitmap overlayBoundingBoxes(Mat image, List<StrawberrySegment> strawberries) {
        for (StrawberrySegment strawberry : strawberries) {
            // Calculate the adjusted stroke width based on the scaling factor
            int strokeWidth = Math.max(strawberry.getBoundingBox().width, strawberry.getBoundingBox().height) / 15;

            // Determine the quality value based on the chosen quality attribute
            Scalar color = switch (this.preferenceProvider.getProcessingPreferences().getBoundingBoxColorPreference()) {
                case "Ripeness" -> interpolateColor(
                        strawberry.getRipeness(),
                        RIPENESS_RANGE.getMin(),
                        RIPENESS_RANGE.getMax());
                case "Brix" -> interpolateColor(
                        strawberry.getBrix(),
                        BRIX_RANGE.getMin(),
                        BRIX_RANGE.getMax());
                case "Marketability" -> interpolateColor(
                        strawberry.getMarketabilityAsInteger(),
                        MARKETABILITY_RANGE.getMin(),
                        MARKETABILITY_RANGE.getMax());
                default -> new Scalar(0, 0, 0);
            };

            color.val[3] = 255.0;
            Imgproc.rectangle(image, strawberry.getBoundingBox(), color, strokeWidth);

            boundingBoxInfoMap.put(strawberry.getBoundingBox(), strawberry);

        }
        // Draw text with qualities on top of the bounding boxes if needed
        if (this.preferenceProvider.getProcessingPreferences().getDisplayText()) {
            displayAdditionalText(image, strawberries);
        }

        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bitmap);
        return bitmap;
    }

    /**
     * Display the additional information text onto the current image. This includes ripeness,
     * Brix and marketability, if enabled from the settings.
     *
     * @param image        the image to be annotated.
     * @param strawberries the list of segments from which the values are extracted.
     */
    @SuppressLint("DefaultLocale")
    public void displayAdditionalText(Mat image, List<StrawberrySegment> strawberries) {
        Set<String> selectedAttributes = preferenceProvider.getProcessingPreferences().getSelectedAttributes();

        int lineType = Imgproc.LINE_AA;
        double fontScale = 2; // Increase this value to make the text even bigger (1.5 times 1.5 = 2.25)
        int thickness = 4; // Increase this value to make the text thicker
        int lineSpacing = 50; // Spacing between lines of text
        int lineCount = selectedAttributes.size(); // Total number of lines of text

        for (StrawberrySegment strawberry : strawberries) {
            Point textPosition = new Point(strawberry.getBoundingBox().tl().x, strawberry.getBoundingBox().tl().y - (lineSpacing * lineCount));

            if (selectedAttributes.contains("Ripeness")) {
                double ripenessPercentage = strawberry.getRipeness() * 100.0;
                String ripenessIndication = getRipenessIndication(ripenessPercentage);
                // Draw black outline
                Imgproc.putText(image, ripenessIndication + " " + "(" +String.format("%.2f", ripenessPercentage) +
                        "%" + ")",
                        textPosition, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, new Scalar(0, 0, 0),
                        thickness + 2, lineType);

                // Draw white text
                Imgproc.putText(image, ripenessIndication + " " + "(" + String.format("%.2f", ripenessPercentage) +
                                "%" + ")",
                        textPosition, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, new Scalar(255, 255, 255),
                        thickness, lineType);

                textPosition.y += lineSpacing;
            }

            if (selectedAttributes.contains("Brix")) {
                // Draw black outline
                Imgproc.putText(image, "Brix: " + String.format("%.2f", strawberry.getBrix()),
                        textPosition, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, new Scalar(0, 0, 0),
                        thickness + 2, lineType);

                // Draw white text
                Imgproc.putText(image, "Brix: " + String.format("%.2f", strawberry.getBrix()),
                        textPosition, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, new Scalar(255, 255, 255),
                        thickness, lineType);

                textPosition.y += lineSpacing;
            }

            if (selectedAttributes.contains("Firmness")) {
                // Draw black outline
                Imgproc.putText(image, "Firmness: " + String.format("%.2f", strawberry.getFirmness()),
                        textPosition, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, new Scalar(0, 0, 0),
                        thickness + 2, lineType);

                // Draw white text
                Imgproc.putText(image, "Firmness: " + String.format("%.2f", strawberry.getFirmness()),
                        textPosition, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, new Scalar(255, 255, 255),
                        thickness, lineType);

                textPosition.y += lineSpacing;
            }



            if (selectedAttributes.contains("Marketability")) {
                // Draw black outline
                Imgproc.putText(image, "Marketable: " + strawberry.getMarketabilityAsString(),
                        textPosition, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, new Scalar(0, 0, 0),
                        thickness + 2, lineType);

                // Draw white text
                Imgproc.putText(image, "Marketable: " + strawberry.getMarketabilityAsString(), textPosition, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, new Scalar(255, 255, 255),
                        thickness, lineType);
            }
        }
    }

    /**
     * Calculates the ripeness indication based on a certain percentage of ripeness. This indication
     * is meant to be more interpretable to humans that a simple percentage.
     *
     * @param ripenessPercentage the ripeness percentage.
     *
     * @return a string indication of the ripeness (e.g. Ripe).
     */
    public String getRipenessIndication(double ripenessPercentage) {
        int targetRipeness = preferenceProvider.getProcessingPreferences().getTargetRipeness();
        Log.d("Target ripeness", String.valueOf(targetRipeness));

        if (ripenessPercentage < targetRipeness * 0.20) {
            return "Unripe";
        } else if (ripenessPercentage < targetRipeness * 0.50) {
            return "Slightly Ripe";
        } else if (ripenessPercentage < targetRipeness * 0.70) {
            return "Mildly Ripe";
        } else if (ripenessPercentage < targetRipeness * 0.90) {
            return "Moderately Ripe";
        } else{
            return "Fully Ripe";
        }
    }

    /**
     * Used for calculating the color of the bounding box. Strawberries with a low ripeness score
     * are given a more red border, while better strawberries with a higher score get a border that
     * turns green.
     *
     * @param qualityAttribute the ripeness score ([0,1]).
     * @param min              the minimum qualityAttribute value.
     * @param max              the maximum qualityAttribute value.
     *
     * @return corresponding hsv color for the border.
     */
    public Scalar interpolateColor(double qualityAttribute, double min, double max) {
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

    /**
     * Updates the ImageView with the provided Bitmap image and enables touch event handling.
     * The touch events are processed to calculate the relative position of the clicks
     * in relation to the original image.
     *
     * @param imageView the ImageView to update with the bitmap.
     * @param bitmap    the Bitmap image to display in the ImageView.
     */
    @SuppressLint("ClickableViewAccessibility")
    public void updateImageview(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);

        imageView.setOnTouchListener((v, event) -> {

            //We need the actual values of the matrix of the bitmap in order to calculate
            //the relative position of the clicks in relation to the original image
            float[] matrixValues = new float[9];
            imageView.getImageMatrix().getValues(matrixValues);

            // Extract the translation values from the matrix
            float translateX = matrixValues[Matrix.MTRANS_X];
            float translateY = matrixValues[Matrix.MTRANS_Y];

            // Extract the scaling values from the matrix
            float scaleX = matrixValues[Matrix.MSCALE_X];
            float scaleY = matrixValues[Matrix.MSCALE_Y];

            // Calculating the touch coordinates relative to the actual bitmap size
            float bitmapX = (event.getX() - translateX) / scaleX;
            float bitmapY = (event.getY() - translateY) / scaleY;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.d("Transmitted X", String.valueOf(bitmapX));
                Log.d("Transmitted Y", String.valueOf(bitmapY));
                // Pass the click event coordinates to the Annotator's handleClickEvent method
                handleClickEvent(bitmap, bitmapX, bitmapY, v.getContext());
            }
            return true;
        });
    }

    /**
     * Handles the clicking functionality of bounding boxes on top of annotated image. Upon
     * clicking the image view, this method is called with the click coordinates and checks
     * against the map of bounding boxes to see if any of them were clicked. If any were clicked,
     * then a dialog is shown.
     *
     * @param bitmap  the full processed bitmap.
     * @param x       the x coordinate of click.
     * @param y       the y coordinate of click.
     * @param context the view in which the dialog should be displayed.
     */
    public void handleClickEvent(Bitmap bitmap, float x, float y, Context context) {
        for (Rect boundingBox : boundingBoxInfoMap.keySet()) {
            Point clickPoint = new Point((int) x, (int) y);
            if (boundingBox.contains(clickPoint)) {
                // Retrieve the additional information for the clicked bounding box
                StrawberrySegment strawberry = boundingBoxInfoMap.get(boundingBox);
                if (strawberry == null) break;

                if (strawberry.getRipeness() == null) {
                    RipenessCalculator ripenessCalculator = new RipenessCalculator();
                    strawberry.setRipeness(ripenessCalculator.calculateRipeness(bitmap));
                }
                if (strawberry.getBrix() == null) {
                    BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
                    strawberry.setBrix(brixCalculator.calculateBrix(brixCalculator.getTime(imageName), bitmap));
                }
                if (strawberry.getRoundness() == null) {
                    RoundnessCalculator roundnessCalculator = new RoundnessCalculator();
                    strawberry.setRoundness(roundnessCalculator.calculateRoundness(bitmap));
                }
                if (strawberry.getSmoothness() == null) {
                    SmoothnessCalculator smoothnessCalculator = new SmoothnessCalculator();
                    strawberry.setSmoothness(smoothnessCalculator.calculateSmoothness(bitmap));
                }
                if (strawberry.getMarketability() == null) {
                    MarketabilityCalculator marketabilityCalculator = new MarketabilityCalculator();
                    strawberry.setMarketability(marketabilityCalculator.isMarketable(strawberry));
                }
                if (strawberry.getFirmness() == null) {
                    FirmnessCalculator firmnessCalculator = new FirmnessCalculator(context, new TimeProvider(), preferenceProvider);
                    strawberry.setFirmness(firmnessCalculator.calculateFirmnessFromView(firmnessCalculator.getTime(imageName), bitmap));
                }

                // Show the ripeness value in the dialog
                showRipenessDialog(bitmap, context, strawberry);

                break;
            }
        }
    }

    /**
     * Method responsible for displaying dialog which contains additional information about the
     * selected strawberry.
     *
     * @param fullImage  the full processed image.
     * @param context    the current context.
     * @param strawberry the strawberry segment.
     */
    private void showRipenessDialog(Bitmap fullImage, Context context, StrawberrySegment strawberry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_bitmap, null);

        ImageView imageView = dialogView.findViewById(R.id.imageView);
        TextView ripenessTextView = dialogView.findViewById(R.id.ripenessTextView);
        TextView brixTextView = dialogView.findViewById(R.id.brixTextView);
//        TextView firmnessTextView = dialogView.findViewById(R.id.firmnessTextView);
        TextView marketabilityTextView = dialogView.findViewById(R.id.marketabilityTextView);
        TextView roundnessTextView = dialogView.findViewById(R.id.roundnessTextView);
        TextView smoothnessTextView = dialogView.findViewById(R.id.smoothnessTextView);
        Button feedbackButton = dialogView.findViewById(R.id.feedbackButton);
        feedbackButton.setOnClickListener((e) -> showFeedbackDialog(context, fullImage, strawberry));

        // Resize the bitmap while maintaining aspect ratio
        Bitmap resizedBitmap = resizeForDialog(strawberry.getBitmap(), 600, 600);
        imageView.setImageBitmap(resizedBitmap);

        ripenessTextView.setText("Ripe: " + String.format("%.02f", strawberry.getRipeness()*100.0)
                + "% " + "(" + getRipenessIndication(strawberry.getRipeness()*100.0) + ")");
        brixTextView.setText("Brix: " + String.format("%.02f", strawberry.getBrix()));

        Bitmap chart = chartGenerator.getChart(strawberry.getRipeness());
        ImageView imageViewChart = dialogView.findViewById(R.id.imageViewChart);
        imageViewChart.setImageBitmap(chart);

        marketabilityTextView.setText("Marketable: " + strawberry.getMarketabilityAsString());
//        firmnessTextView.setText("Firmness: " + String.format("%.02f", strawberry.getFirmness().get()));
        roundnessTextView.setText("Roundness: " + String.format("%.02f", strawberry.getRoundness()));
        smoothnessTextView.setText("Smoothness: " + String.format("%.02f", strawberry.getSmoothness()));

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Resizes a bitmap to display in the dialog
     *
     * @param bitmap    the bitmap to resize.
     * @param maxWidth  the maximum width.
     * @param maxHeight the maximum height.
     *
     * @return the resized bitmap.
     */
    private Bitmap resizeForDialog(Bitmap bitmap, int maxWidth, int maxHeight) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth, newHeight;

        if (originalWidth > originalHeight) {
            // Landscape orientation
            newWidth = Math.min(originalWidth, maxWidth);
            newHeight = (int) (originalHeight * (newWidth / (double) originalWidth));
        } else {
            // Portrait or square orientation
            newHeight = Math.min(originalHeight, maxHeight);
            newWidth = (int) (originalWidth * (newHeight / (double) originalHeight));
        }

        // Increase the size if smaller than 400 pixels
        if (newWidth < 400 && newHeight < 400) {
            double scaleFactor = Math.max(400.0 / newWidth, 400.0 / newHeight);
            newWidth = (int) (newWidth * scaleFactor);
            newHeight = (int) (newHeight * scaleFactor);
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }

    /**
     * Starts the feedback-giving routine.
     *
     * @param context    the current context.
     * @param fullImage  the full processed image.
     * @param strawberry the strawberry segment.
     */
    private void showFeedbackDialog(Context context, Bitmap fullImage, StrawberrySegment strawberry) {
        FeedbackSender fbProvider = new FeedbackSender(fullImage, new UriUtils(), new DateProvider(), strawberry);
        fbProvider.process(context);
    }
}
