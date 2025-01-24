package com.example.fruitqualityprediction.sbprocessing.segmentation;

import static org.junit.Assert.*;

import android.graphics.Bitmap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opencv.core.Rect;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class StrawberrySegmentTest {

    private StrawberrySegment strawberrySegment;
    private Rect boundingBox;
    @Mock
    private Bitmap bitmap;

    @Before
    public void setup() {
        boundingBox = new Rect(0, 0, 100, 100);
        strawberrySegment = new StrawberrySegment(boundingBox);
        strawberrySegment.setBitmap(bitmap);
    }

    @Test
    public void getBitmap() {
        assertSame(strawberrySegment.getBitmap(), bitmap);
    }

    @Test
    public void getBoundingBox() {
        assertSame(boundingBox, strawberrySegment.getBoundingBox());
    }

    @Test
    public void setBitmap() {
        assertSame(bitmap, strawberrySegment.getBitmap());
        strawberrySegment.setBitmap(null);
        assertNull(strawberrySegment.getBitmap());
    }

    @Test
    public void getRipeness() {
        assertNull(strawberrySegment.getRipeness());
    }

    @Test
    public void setRipeness() {
        assertNull(strawberrySegment.getRipeness());
        strawberrySegment.setRipeness(1.0);
        assertEquals(1, strawberrySegment.getRipeness(), 0.01);
    }

    @Test
    public void getBrix() {
        assertNull(strawberrySegment.getBrix());
    }

    @Test
    public void setBrix() {
        assertNull(strawberrySegment.getBrix());
        strawberrySegment.setBrix(1f);
        assertEquals(1, strawberrySegment.getBrix(), 0.01);
    }

    @Test
    public void getRoundness() {
        assertNull(strawberrySegment.getRoundness());
    }

    @Test
    public void setRoundness() {
        assertNull(strawberrySegment.getRoundness());
        strawberrySegment.setRoundness(1);
        assertEquals(1, strawberrySegment.getRoundness(), 0.01);
    }

    @Test
    public void getSmoothness() {
        assertNull(strawberrySegment.getSmoothness());
    }

    @Test
    public void setSmoothness() {
        assertNull(strawberrySegment.getSmoothness());
        strawberrySegment.setSmoothness(1.0);
        assertEquals(1, strawberrySegment.getSmoothness(), 0.01);
    }

    @Test
    public void getMarketability() {
        assertNull(strawberrySegment.getMarketability());
    }

    @Test
    public void setMarketability() {
        assertNull(strawberrySegment.getMarketability());
        strawberrySegment.setMarketability(true);
        assertEquals(true, strawberrySegment.getMarketability());
    }

    @Test
    public void getFirmness() {
        assertNull(strawberrySegment.getFirmness());
    }

    @Test
    public void setFirmness() {
        assertNull(strawberrySegment.getFirmness());
        strawberrySegment.setFirmness(1f);
        assertEquals(1, strawberrySegment.getFirmness(), 0.01);
    }
}