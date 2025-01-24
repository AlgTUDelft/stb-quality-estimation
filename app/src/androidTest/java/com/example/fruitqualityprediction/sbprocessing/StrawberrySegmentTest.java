package com.example.fruitqualityprediction.sbprocessing;

import static org.junit.Assert.assertEquals;

import android.graphics.Bitmap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.opencv.core.Rect;


import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;

public class StrawberrySegmentTest {

    private Bitmap mockBitmap;
    private Rect mockBoundingBox;

    private StrawberrySegment strawberrySegment;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        strawberrySegment = new StrawberrySegment(mockBoundingBox);
        mockBitmap = createMockBitmap();
        mockBoundingBox = createMockBoundingBox(0,0,20,20);
        strawberrySegment.setBitmap(mockBitmap);
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
     * Used to create a "fake" Bitmap that can be used in tests.
     * @return mock Bitmap
     */
    private Bitmap createMockBitmap() {
        int width = 1000;
        int height = 1000;
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        return bitmap;
    }

    @Test
    public void testGetBitmap() {
        strawberrySegment.setBitmap(mockBitmap);
        Bitmap result = strawberrySegment.getBitmap();
        assertEquals(mockBitmap, result);
    }

//    @Test
//    public void testSetBitmap() {
//        strawberrySegment.setBitmap(mockBitmap);
//        verify(mockBitmap, times(1)).getPixels(any(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
//    }

//    @Test
//    public void testGetBoundingBox() {
//        Rect result = strawberrySegment.getBoundingBox();
//        assertEquals(mockBoundingBox, result);
//    }

    @Test
    public void testGetRipeness() {
        double ripeness = 0.75;
        strawberrySegment.setRipeness(ripeness);
        double result = strawberrySegment.getRipeness();
        assertEquals(ripeness, result, 0.0);
    }

    @Test
    public void testSetRipeness() {
        double ripeness = 0.5;
        strawberrySegment.setRipeness(ripeness);
        double result = strawberrySegment.getRipeness();
        assertEquals(ripeness, result, 0.0);
    }

    @Test
    public void testGetBrix() {
        float brix = 5.0f;
        strawberrySegment.setBrix(brix);
        float result = strawberrySegment.getBrix();
        assertEquals(brix, result, 0.0);
    }

    @Test
    public void testSetBrix() {
        float brix = 8.0f;
        strawberrySegment.setBrix(brix);
        float result = strawberrySegment.getBrix();
        assertEquals(brix, result, 0.0);
    }

    @Test
    public void testGetRoundness() {
        float roundness = 0.8f;
        strawberrySegment.setRoundness(roundness);
        double result = strawberrySegment.getRoundness();
        assertEquals(roundness, result, 0.0);
    }

    @Test
    public void testSetRoundness() {
        double roundness = 0.6;
        strawberrySegment.setRoundness(roundness);
        double result = strawberrySegment.getRoundness();
        assertEquals(roundness, result, 0.0);
    }

    @Test
    public void testGetMarketability() {
        boolean marketability = false;
        strawberrySegment.setMarketability(marketability);
        boolean result = strawberrySegment.getMarketability();
        assertEquals(marketability, result);
    }

    @Test
    public void testSetMarketability() {
        boolean marketability = true;
        strawberrySegment.setMarketability(marketability);
        boolean result = strawberrySegment.getMarketability();
        assertEquals(marketability, result);
    }

    @Test
    public void testGetSmoothness() {
        double smoothness = 0.8;
        strawberrySegment.setSmoothness(smoothness);
        double result = strawberrySegment.getSmoothness();
        assertEquals(smoothness, result, 0.0);
    }

    @Test
    public void testSetSmoothness() {
        double smoothness = 0.6;
        strawberrySegment.setSmoothness(smoothness);
        double result = strawberrySegment.getSmoothness();
        assertEquals(smoothness, result, 0.0);
    }

    @Test
    public void testEquals() {
        StrawberrySegment segment1 = new StrawberrySegment(mockBoundingBox);
        segment1.setRipeness(0.5);
        segment1.setBrix(5.0f);
        segment1.setRoundness(0.8f);
        segment1.setBitmap(mockBitmap);

        StrawberrySegment segment2 = new StrawberrySegment(mockBoundingBox);
        segment2.setRipeness(0.5);
        segment2.setBrix(5.0f);
        segment2.setRoundness(0.8f);
        segment2.setBitmap(mockBitmap);

        assertEquals(segment1, segment2);
    }

    @Test
    public void testHashCode() {
        StrawberrySegment segment1 = new StrawberrySegment(mockBoundingBox);
        segment1.setRipeness(0.5);
        segment1.setBrix(5.0f);
        segment1.setRoundness(0.8f);
        segment1.setBitmap(mockBitmap);

        StrawberrySegment segment2 = new StrawberrySegment(mockBoundingBox);
        segment2.setRipeness(0.5);
        segment2.setBrix(5.0f);
        segment2.setRoundness(0.8f);
        segment2.setBitmap(mockBitmap);

        assertEquals(segment1.hashCode(), segment2.hashCode());
    }
}