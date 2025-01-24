package com.example.fruitqualityprediction.sbprocessing;

import android.graphics.Bitmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.when;

import org.junit.Assert;

import com.example.fruitqualityprediction.preferences.ProcessingPreferences;
import com.example.fruitqualityprediction.providers.PreferenceProvider;
import com.example.fruitqualityprediction.providers.TimeProvider;
import com.example.fruitqualityprediction.sbprocessing.segmentation.color.ColorStrawberryDetector;
import com.example.fruitqualityprediction.sbprocessing.visualization.Annotator;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;
import com.example.fruitqualityprediction.sbprocessing.visualization.VisualizationUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class AnnotatorTest {

    private Bitmap bitmap;
    private Annotator annotator;

    @Mock
    private PreferenceProvider preferenceProvider;

    @Before
    public void setup() {
        OpenCVLoader.initDebug();
        bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        annotator = new Annotator(this.preferenceProvider, null);
    }

    /**
     *  Tests whether the boxes are overlaid by checking whether the two bitmaps are different.
     */
    @Test
    public void testOverlayBoundingBoxes() {
        List<StrawberrySegment> strawberries = new ArrayList<>();

        // Create a sample strawberry segment with a bounding box
        StrawberrySegment strawberry = new StrawberrySegment(new Rect(10, 10, 50, 50));
        strawberry.setBitmap(Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888));
        strawberry.setRipeness(1.0);

        strawberries.add(strawberry);

        Mat image = createMockMat();
        Utils.bitmapToMat(bitmap, image);
        when(preferenceProvider.getProcessingPreferences()).thenReturn(new ProcessingPreferences(
                new ColorStrawberryDetector(),
                1,
                "Ripeness",
                true,
                new HashSet<>()));
        Bitmap resultBitmap = annotator.overlayBoundingBoxes(image, strawberries);

        assertNotNull(resultBitmap);

        assertNotSame(bitmap, resultBitmap);

    }

    /**
     * Asserts whether the output for a ripeness of 1.0 (perfect) is correct.
     */
    @Test
    public void testGetColorFromGradientPerfect() {
        double ripeness = 1; // Full ripeness score, results in fully green color
        Scalar expectedColor = new Scalar(0,255,0);

        Scalar color = VisualizationUtils.interpolateColor(ripeness,0,1);
        assertEquals(expectedColor, color);
    }


    /**
     * Asserts whether the output for a ripeness of 0.5 (average) is correct.
     */
    @Test
    public void testGetColorFromGradientMid() {
        double ripeness = 0.5; // Halfway between red and green in the color gradient
        Scalar expectedColor = new Scalar(255.0, 255.0, 0.0);

        Scalar color = VisualizationUtils.interpolateColor(ripeness,0,1);

        assertEquals(expectedColor, color);
    }

    /**
     * Asserts whether the output for a ripeness of 0 is correct.
     */
    @Test
    public void testGetColorFromGradientLow() {
        double ripeness = 0.0; // Halfway between red and green in the color gradient
        Scalar expectedColor = new Scalar(255.0, 0.0, 0.0);

        Scalar color = VisualizationUtils.interpolateColor(ripeness,0,1);

        assertEquals(expectedColor, color);
    }

    /**
     * Checks whether the timeProvider round up correctly.
     */
    @Test
    public void testNearestHourUp() {
        String currentTimePattern = "yyyy-MM-dd HH:mm:ss";
        String expectedPattern = "2021-01-01 14:00:00";

        // Create a mock object for the TimeProvider class
        TimeProvider mockTimeProvider = new TimeProvider();

        mockTimeProvider.setYear(2021);
        mockTimeProvider.setMonth(01);
        mockTimeProvider.setDay(01);
        mockTimeProvider.setHour(13);
        mockTimeProvider.setMinute(31);
        mockTimeProvider.setSecond(0);

        mockTimeProvider.roundToNearestHour();

        String actualTime = mockTimeProvider.format(currentTimePattern);

        Assert.assertEquals(expectedPattern, actualTime);
    }

    /**
     * Checks whether the timeProvider round down correctly.
     */
    @Test
    public void testNearestHourDown() {
        String currentTimePattern = "yyyy-MM-dd HH:mm:ss";
        String expectedPattern = "2021-01-01 13:00:00";

        // Create a mock object for the TimeProvider class
        TimeProvider mockTimeProvider = new TimeProvider();

        mockTimeProvider.setYear(2021);
        mockTimeProvider.setMonth(01);
        mockTimeProvider.setDay(01);
        mockTimeProvider.setHour(13);
        mockTimeProvider.setMinute(29);
        mockTimeProvider.setSecond(0);

        mockTimeProvider.roundToNearestHour();

        String actualTime = mockTimeProvider.format(currentTimePattern);

        Assert.assertEquals(expectedPattern, actualTime);
    }

    /**
     * Checks whether the timeProvider round up correctly when at exactly 30 minutes past a certain
     * hour.
     */
    @Test
    public void testEdgeCaseTimeProvider() {
        String currentTimePattern = "yyyy-MM-dd HH:mm:ss";
        String expectedPattern = "2021-01-01 15:00:00";

        // Create a mock object for the TimeProvider class
        TimeProvider mockTimeProvider = new TimeProvider();

        mockTimeProvider.setYear(2021);
        mockTimeProvider.setMonth(01);
        mockTimeProvider.setDay(01);
        mockTimeProvider.setHour(14);
        mockTimeProvider.setMinute(30); //If the time is exactly half past hour x, it should
                                        //be rounded to hour x+1
        mockTimeProvider.setSecond(0);

        mockTimeProvider.roundToNearestHour();

        String actualTime = mockTimeProvider.format(currentTimePattern);

        Assert.assertEquals(expectedPattern, actualTime);
    }

//    @Test
//    public void overlayBoundingBoxes_shouldDrawBoundingBoxesOnBitmap() {
//        // Mock the ColorExtractorStrawberrySegment objects
//        StrawberrySegment strawberry1 = createMockStrawberrySegment(100, 100, 50, 50);
//        StrawberrySegment strawberry2 = createMockStrawberrySegment(200, 200, 50, 50);
//        List<StrawberrySegment> strawberries = new ArrayList<>();
//        strawberries.add(strawberry1);
//        strawberries.add(strawberry2);
//
//
//        Bitmap bitmap = createMockBitmap();
//        Mat mat = bitmapToMat(bitmap);
//        Bitmap result = annotator.overlayBoundingBoxes(mat, strawberries);
//
//        assertEquals(Bitmap.Config.ARGB_8888, result.getConfig());
//        assertEquals(bitmap.getWidth(), result.getWidth());
//        assertEquals(bitmap.getHeight(), result.getHeight());
//
//        Assert.assertNotNull(result);
//        Assert.assertNotSame(result,bitmap);
//
//        Rect expectedBoundingBox1 = new Rect(100, 100, 150, 150);
//        assertTrue(isBoundingBoxDrawn(bitmap,result, expectedBoundingBox1));
//
//        Rect expectedBoundingBox2 = new Rect(200, 200, 250, 250);
//        assertTrue(isBoundingBoxDrawn(bitmap,result, expectedBoundingBox2));
//
//    }
//
//    private boolean isBoundingBoxDrawn(Bitmap original,Bitmap result, Rect boundingBox) {
//        int startX = Math.max(0, boundingBox.x);
//        int startY = Math.max(0, boundingBox.y);
//        int endX = Math.min(result.getWidth(), boundingBox.x + boundingBox.width);
//        int endY = Math.min(result.getHeight(), boundingBox.y + boundingBox.height);
//        for (int x = startX; x < endX; x++) {
//            for (int y = startY; y < endY; y++) {
//                int pixel = result.getPixel(x, y);
//                if (pixel != Color.TRANSPARENT && original.getPixel(x, y)!=pixel) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }


    /**
     * Creates a mock strawberry object to be used in testing.
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    private StrawberrySegment createMockStrawberrySegment(int x, int y, int width, int height) {
        Rect boundingBox = createMockBoundingBox(x, y, width, height);
        StrawberrySegment strawberry = new StrawberrySegment(boundingBox);
        return strawberry;
    }

    /**
     * Creates a mock Rect to be used in tests.
     * @param x
     * @param y
     * @param width
     * @param height
     * @return mocked Rect representing a bounding box
     */
    private Rect createMockBoundingBox(int x, int y, int width, int height) {
        Rect boundingBox = new Rect(x, y, x + width, y + height);
        return boundingBox;
    }

    /**
     * Used to create a "fake" Mat that can be used in tests.
     * @return mock Mat
     */
    private Mat createMockMat() {
        int width = 1000;
        int height = 1000;
        int type = CvType.CV_8UC4; // Assuming 4-channel (RGBA) Mat
        Mat mat = new Mat(height, width, type);
        return mat;
    }

}
