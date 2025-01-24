package com.example.fruitqualityprediction.sbprocessing.visualization;

import android.graphics.Bitmap;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Generates a line chart using a provided configuration
 */
public class ChartGenerator {
    private static final int FUNCTION_THICKNESS = 2;
    private static final int AXES_THICKNESS = 2;
    private static final int HIGHLIGHT_THICKNESS = 2;
    private static final int TEXT_THICKNESS = 3;
    private static final Scalar BACKGROUND_COLOR = new Scalar(0, 0, 0, 0);
    private static final Scalar AXES_COLOR = new Scalar(255, 255, 255, 255);
    private static final Scalar HX_COLOR = new Scalar(10, 130, 255, 255);

    private final ChartConfiguration config;
    private final Function function;
    private final Mat functionImage;
    private final ArrayList<Point> points;

    // Range of the displayed area
    private final double xRangeSize;
    private final double yRangeSize;

    // Actual size of the chart (without margins)
    private final double chartWidth;
    private final double chartHeight;

    // Margins
    private final double marginTop;
    private final double marginLeft;
    private final double marginBottom;
    private final double marginRight;
    
    private final Size textSize;

    // Text Range Y
    private double minTextY;
    private double maxTextY;

    private double minTextX;
    private double maxTextX;

    /**
     * Calculates the chart metrics using the provided configuration.
     *
     * @param config The provided configuration
     */
    public ChartGenerator(ChartConfiguration config) {
        this.config = config;
        this.function = new Function("f(x) = " + config.getFunctionString());
        this.functionImage = new Mat((int) config.getImageSize().height,
                (int) config.getImageSize().width, CvType.CV_8UC4,
                BACKGROUND_COLOR);
        this.points = new ArrayList<>();

        this.marginTop = config.getImageSize().height * 0.2;
        this.marginLeft = config.getImageSize().width * 0.25;
        this.marginBottom = config.getImageSize().height * 0.25;
        this.marginRight = config.getImageSize().width * 0.2;
        
        this.textSize = new Size(marginLeft * 0.8, marginLeft * 0.2);

        // Range of the displayed area
        this.xRangeSize = config.getXRange().getMax() - config.getXRange().getMin();
        this.yRangeSize = config.getYRange().getMax() - config.getYRange().getMin();

        // Actual size of the chart (without margins)
        this.chartWidth = config.getImageSize().width - this.marginLeft - this.marginRight;
        this.chartHeight = config.getImageSize().height - this.marginTop - this.marginBottom;

        drawFunction(this.functionImage);
        drawAxes(this.functionImage);
    }

    /**
     * Generates a bitmap image of the chart.
     *
     * @param highlightedY The Y value of the highlighted point
     *
     * @return A bitmap image of the generated chart
     */
    public Bitmap getChart(double highlightedY) {
        Mat chart = functionImage.clone();
        highlightValue(chart, highlightedY);

        Bitmap bitmap = Bitmap.createBitmap(chart.cols(), chart.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(chart, bitmap);

        return bitmap;
    }

    /**
     * Draws the axes and the labels
     *
     * @param image The image of the chart
     */
    public void drawAxes(Mat image) {
        double tipLength = config.getImageSize().width * 0.05;
        double xTipLength = 0.5 * tipLength / (chartWidth + tipLength);
        double yTipLength = 0.5 * tipLength / (chartHeight + tipLength);
        Imgproc.arrowedLine(
                image,
                new Point(marginLeft, marginTop + chartHeight),
                new Point(marginLeft + chartWidth, marginTop + chartHeight),
                AXES_COLOR,
                AXES_THICKNESS,
                Imgproc.LINE_AA,
                0,
                xTipLength
        );
        Imgproc.arrowedLine(
                image,
                new Point(marginLeft, marginTop + chartHeight),
                new Point(marginLeft, marginTop),
                AXES_COLOR,
                AXES_THICKNESS,
                Imgproc.LINE_AA,
                0,
                yTipLength
        );

        DecimalFormat decimalFormat = new DecimalFormat("#0.0#");

        // Max Y
        Mat textBlock = generateTextBlock(
                decimalFormat.format(config.getYRange().getMax()),
                textSize,
                AXES_COLOR
        );
        Point location = new Point((marginLeft - textBlock.width()) * 0.8, marginTop - (double) textBlock.height() / 3);
        overlayImage(image, textBlock, location);
        minTextY = location.y + textBlock.height();

        // Min Y
        textBlock = generateTextBlock(
                decimalFormat.format(config.getYRange().getMin()),
                textSize,
                AXES_COLOR
        );
        location = new Point((marginLeft - textBlock.width()) * 0.8, marginTop + chartHeight - (double) textBlock.height() / 2);
        overlayImage(image, textBlock, location);
        maxTextY = location.y - textBlock.height();

        // Min X
        textBlock = generateTextBlock(
                decimalFormat.format(config.getXRange().getMin()),
                textSize,
                AXES_COLOR
        );
        location = new Point(marginLeft - 0.5 * textBlock.width(), marginTop + chartHeight + textBlock.height());
        overlayImage(image, textBlock, location);
        minTextX = location.x + textBlock.width();

        // Max X
        textBlock = generateTextBlock(
                decimalFormat.format(config.getXRange().getMax()),
                textSize,
                AXES_COLOR
        );
        location = new Point(marginLeft + chartWidth - 0.5 * textBlock.width(), marginTop + chartHeight + textBlock.height());
        overlayImage(image, textBlock, location);
        maxTextX = location.x - textBlock.width();

        // Label Y
        textBlock = generateTextBlock(
                config.getYLabel(),
                new Size(marginLeft * 1.5, textSize.height),
                AXES_COLOR
        );
        location = new Point(
                marginLeft - 0.5 * textBlock.width(),
                (minTextY - textSize.height) / 2 - 0.25 * textBlock.height()
        );
        overlayImage(image, textBlock, location);

        // Label X
        textBlock = generateTextBlock(
                config.getXLabel(),
                new Size(0.7 * marginRight, textSize.height),
                AXES_COLOR
        );
        location = new Point(
                marginLeft + chartWidth + 0.65 * marginRight - 0.5 * textBlock.width(),
                marginTop + chartHeight - 0.5 * textBlock.height()
        );
        overlayImage(image, textBlock, location);
    }

    /**
     * Overlays an image on top of another.
     *
     * @param image The original image
     * @param overlay The image to be overlaid
     * @param point The location of the top right corner of the overlaid image
     */
    public void overlayImage(Mat image, Mat overlay, Point point) {
        Range xRange = new Range((int) point.x, (int) (point.x + overlay.width()));
        Range yRange = new Range((int) point.y, (int) (point.y + overlay.height()));
        overlay.copyTo(image.colRange(xRange).rowRange(yRange));
    }

    /**
     * Generates an image containing text.
     *
     * @param text The text in the image.
     * @param targetSize The maximum size of the image. Height has priority over width.
     * @param color The color of the text
     *
     * @return The text block image
     */
    public Mat generateTextBlock(String text, Size targetSize, Scalar color) {
        double fontScale = 3 * config.getImageSize().height / 500;

        Mat textImage = new Mat(
                (int) config.getImageSize().height,
                (int) config.getImageSize().width,
                CvType.CV_8UC4,
                BACKGROUND_COLOR
        );

        Imgproc.putText(
                textImage,
                text,
                new Point(0, textImage.height() * 0.5),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                fontScale,
                color,
                (int) (TEXT_THICKNESS * fontScale)
        );

        MatOfPoint indices = new MatOfPoint();
        Mat textImageBin = new Mat();
        Imgproc.cvtColor(textImage, textImageBin, Imgproc.COLOR_RGBA2GRAY);
        Core.findNonZero(textImageBin, indices);
        Rect boundingRect = Imgproc.boundingRect(indices);
        Mat textBlock = new Mat(textImage, boundingRect);

        double targetWidth = (int) (textBlock.width() * targetSize.height / textBlock.height());
        double targetHeight = targetSize.height;
        if (targetWidth >= targetSize.width) {
            targetWidth = targetSize.width;
            targetHeight = (int) (textBlock.height() * targetSize.width / textBlock.width());
        }
        Imgproc.resize(textBlock, textBlock, new Size(targetWidth, targetHeight), 0, 0, Imgproc.INTER_AREA);

        return textBlock;
    }

    /**
     * Draws the mathematical function on an image.
     *
     * @param image The image where the function will be drawn.
     */
    public void drawFunction(Mat image) {
        Point lastDisplayPoint = null;
        double step = (config.getXRange().getMax() - config.getXRange().getMin()) / chartWidth;
        for (double x = config.getXRange().getMin(); x < config.getXRange().getMax(); x += step) {
            Expression expression = new Expression("f(" + x + ")", function);
            double y = expression.calculate();
            if (Double.isNaN(y) || Double.isInfinite(y)) {
                continue;
            }

            Point point = new Point(x, y);
            points.add(point);
            Point displayPoint = mapPointToImage(point);
            Scalar color = VisualizationUtils.interpolateColor(y, config.getYRange().getMin(), config.getYRange().getMax());
            if (displayPoint != null) {
                Imgproc.circle(
                        image,
                        displayPoint,
                        FUNCTION_THICKNESS,
                        color
                );
                if (lastDisplayPoint != null) {
                    Imgproc.line(
                            image,
                            displayPoint,
                            lastDisplayPoint,
                            color,
                            FUNCTION_THICKNESS
                    );
                }
            }
            lastDisplayPoint = displayPoint;
        }
    }

    /**
     * Maps a point from the 2D space where the function is displayed
     * to its corresponding location on the image.
     *
     * @param point The initial point.
     *
     * @return A location in the image.
     * If the point is outside the visible range of the chart null is returned instead.
     */
    public Point mapPointToImage(Point point) {
        double x = point.x;
        double y = point.y;

        // Move the point to chart bounds
        x -= config.getXRange().getMin();
        y -= config.getYRange().getMin();

        // Scale so it fits higher bound
        x = x / xRangeSize * chartWidth;
        y = y / yRangeSize * chartHeight;

        // Move to chart location
        x += marginLeft;
        y += marginBottom;

        // Move the origin to the bottom
        y = config.getImageSize().height - y;

        if (((x < marginLeft) || (x > marginLeft + chartWidth)) ||
            ((y < marginTop) || (y > marginTop + chartHeight))) {
            return null;
        }

        return new Point(x, y);
    }

    /**
     * Highlights a point in the chart.
     *
     * @param image The image where the point is drawn.
     * @param highlightedY The Y value of the corresponding point in the chart.
     */
    public void highlightValue(Mat image, double highlightedY) {
        Point highlightedPoint = findHighlightedPoint(highlightedY);
        if (highlightedPoint == null) {
            return;
        }

        Scalar highlightedColor = VisualizationUtils.interpolateColor(
                highlightedY,
                config.getYRange().getMin(),
                config.getYRange().getMax()
        );

        Point highlightedDisplayPoint = mapPointToImage(highlightedPoint);
        if (highlightedDisplayPoint != null) {
            Imgproc.line(
                    image,
                    mapPointToImage(new Point(config.getXRange().getMin(), highlightedPoint.y)),
                    highlightedDisplayPoint,
                    highlightedColor,
                    HIGHLIGHT_THICKNESS
            );
            Imgproc.line(
                    image,
                    highlightedDisplayPoint,
                    mapPointToImage(new Point(highlightedPoint.x, config.getYRange().getMin())),
                    HX_COLOR,
                    HIGHLIGHT_THICKNESS
            );

            DecimalFormat decimalFormat = new DecimalFormat("#0.0#");

            // Y
            Mat textBlock = generateTextBlock(
                    decimalFormat.format(highlightedY),
                    textSize,
                    highlightedColor
            );
            double y = highlightedDisplayPoint.y - 0.5 * textBlock.height();
            y = Math.max(y, minTextY);
            y = Math.min(y, maxTextY);
            Point location = new Point((marginLeft - textBlock.width()) * 0.8, y);
            overlayImage(image, textBlock, location);

            // X
            textBlock = generateTextBlock(
                    decimalFormat.format(highlightedPoint.x),
                    textSize,
                    HX_COLOR
            );
            double x = highlightedDisplayPoint.x - 0.5 * textBlock.width();
            x = Math.max(x, minTextX);
            x = Math.min(x, maxTextX);
            location = new Point(x, marginTop + chartHeight + textBlock.height());
            overlayImage(image, textBlock, location);

            Imgproc.circle(image, highlightedDisplayPoint, (int) (0.03 * chartHeight), highlightedColor, -1);
        }
    }

    /**
     * Finds the location of the highlighted point in the entire image.
     *
     * @param highlightedY The Y value corresponding to the highlighted point.
     *
     * @return The new X and Y coordinates of the point.
     * If the point is outside the visible range of the chart null is returned instead.
     */
    public Point findHighlightedPoint(double highlightedY) {
        Point lastPoint = null;
        for (Point point : points) {
            if (lastPoint == null) {
                lastPoint = point;
                continue;
            }
            Point minY = lastPoint.y < point.y ? lastPoint : point;
            Point maxY = lastPoint.y > point.y ? lastPoint : point;
            if ((highlightedY >= minY.y) && (highlightedY <= maxY.y)) {
                double intC = (highlightedY - minY.y) / (maxY.y - minY.y);
                return new Point(minY.x * (1.0 - intC) + maxY.x * intC, highlightedY);
            }
        }

        return null;
    }

    /**
     * A getter for the configuration.
     *
     * @return The configuration with with the generator was initialized.
     */
    public ChartConfiguration getConfiguration() {
        return this.config;
    }
}
