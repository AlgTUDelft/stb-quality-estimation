package com.example.fruitqualityprediction.sbprocessing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ApplicationProvider;

import com.example.fruitqualityprediction.R;
import com.example.fruitqualityprediction.sbprocessing.ripeness.RipenessCalculator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opencv.android.OpenCVLoader;

@RunWith(MockitoJUnitRunner.class)
public class RipenessCalculatorTest {
    private RipenessCalculator ripenessCalculator;
    private Bitmap strawberry;
    private Bitmap lightStrawberry;
    private Bitmap notRipeStrawberry;

    String ripenessTag = "Ripeness";
    String outOfBoundsMessage = "Ripeness value is out of bounds [0,1]";

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        ripenessCalculator = new RipenessCalculator();
        strawberry = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry);
        lightStrawberry = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberrypink);
        notRipeStrawberry = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry_not_ripe);
        OpenCVLoader.initDebug();
    }

    @Test
    public void testCalculateRipeness() {
        double ripeness = ripenessCalculator.calculateRipeness(strawberry);
        Log.d(ripenessTag, String.valueOf(ripeness));
        assertTrue(outOfBoundsMessage,ripeness >= 0 && ripeness <= 1);
    }

    @Test
    public void testCalculateLightStrawberry() {
        double ripeness = ripenessCalculator.calculateRipeness(lightStrawberry);
        Log.d(ripenessTag, String.valueOf(ripeness));
        assertTrue(outOfBoundsMessage,ripeness >= 0 && ripeness <= 1);
    }

    @Test
    public void testCalculateNotRipeStrawberry() {
        double ripeness = ripenessCalculator.calculateRipeness(notRipeStrawberry);
        Log.d(ripenessTag, String.valueOf(ripeness));
        assertTrue(outOfBoundsMessage,ripeness >= 0 && ripeness <= 1);
    }
    @Test
    public void testRipeBetterThanNotRipe() {
        double ripenessNotRipe = ripenessCalculator.calculateRipeness(notRipeStrawberry);
        double ripenessRipe = ripenessCalculator.calculateRipeness(strawberry);
        assertTrue("Ripe strawberry should have a better score than not ripe strawberry",
                ripenessRipe > ripenessNotRipe);
    }

    @Test
    public void testFullRedImage() {
        Bitmap fullRed = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(fullRed);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawRect(0, 0, 300, 300, paint);
        double ripeness = ripenessCalculator.calculateRipeness(fullRed);
        Log.d(ripenessTag, String.valueOf(ripeness));
        assertTrue(outOfBoundsMessage,ripeness >= 0 && ripeness <= 1);
    }

    @Test
    public void testFullGreenImage() {
        Bitmap fullRed = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(fullRed);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        canvas.drawRect(0, 0, 300, 300, paint);
        double ripeness = ripenessCalculator.calculateRipeness(fullRed);
        Log.d(ripenessTag, String.valueOf(ripeness));
        assertEquals("Strawberries that are green have a score of 0",ripeness,0.0,0.001);
    }

    @Test
    public void testFullPinkImage() {
        Bitmap fullPink = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(fullPink);
        Paint paint = new Paint();
        paint.setColor(Color.rgb(255, 192, 203)); // set color to pink
        canvas.drawRect(0, 0, 300, 300, paint); // draw a rectangle on the canva
        double ripeness = ripenessCalculator.calculateRipeness(fullPink);
        Log.d(ripenessTag, String.valueOf(ripeness));
        assertTrue(outOfBoundsMessage,ripeness >= 0 && ripeness <= 1);
    }

    @Test
    public void testOrangeRedish() {
        Bitmap orangeRedish = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(orangeRedish);
        Paint paint = new Paint();
        paint.setColor(Color.rgb(255, 95, 91)); // set color to a orange-red gradient
        canvas.drawRect(0, 0, 300, 300, paint); // draw a rectangle on the canvas
        double ripeness = ripenessCalculator.calculateRipeness(orangeRedish);
        Log.d(ripenessTag, String.valueOf(ripeness));
        assertTrue(outOfBoundsMessage,ripeness >= 0 && ripeness <= 1);
    }

    @Test
    public void testBlueish() {
        Bitmap blueIsh = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(blueIsh);
        Paint paint = new Paint();
        paint.setColor(Color.rgb(0,130,180)); // set color to a blue color
        canvas.drawRect(0, 0, 300, 300, paint); // draw a rectangle on the canvas
        double ripeness = ripenessCalculator.calculateRipeness(blueIsh);
        Log.d(ripenessTag, String.valueOf(ripeness));
        assertEquals("Blue strawberries should have a score of 0",ripeness, 0.0,0.001);
    }

    @Test
    public void testRgbToLab() {
        double[] rgb = new double[]{255.0,70.0,100};
        assertArrayEquals(ripenessCalculator.rgbToLab(rgb[0],rgb[1],rgb[2]), new double[]{58.56,70.39,25.79},0.5);
    }

    @Test
    public void testRgbToLab2() {
        double[] rgb = new double[]{100.0,161.0,200.0};
        assertArrayEquals(ripenessCalculator.rgbToLab(rgb[0],rgb[1],rgb[2]), new double[]{63.646,-8.935,-26.091},0.5);
    }

    @Test
    public void testRgbToLab3() {
        double[] rgb = new double[]{0.0,0.0,0.0};
        assertArrayEquals(ripenessCalculator.rgbToLab(rgb[0],rgb[1],rgb[2]), new double[]{0.0,0.0,0.0},0.5);
    }

    @Test
    public void testRgbToLab4() {
        double[] rgb = new double[]{255.0,255.0,255.0};
        assertArrayEquals(ripenessCalculator.rgbToLab(rgb[0],rgb[1],rgb[2]), new double[]{100.0,0.005,-0.010},0.5);
    }

    @Test
    public void testRipenessCalculator() {
        float redness = 128.0f;
        float lightness = 100.0f;
        assertEquals("Expecting ripeness score of 1",ripenessCalculator.calculateRipenessFromRedness(redness, lightness), 1.0,0.0001);
    }

    @Test
    public void testRipenessCalculator2() {
        float redness = 64.0f;
        float lightness = 100.0f;
        assertEquals("Expecting ripeness score of nearly 0.66",
                ripenessCalculator.calculateRipenessFromRedness(redness, lightness), 0.66,0.02);
    }

    @Test
    public void testRipenessCalculator3() {
        float redness = -1.0f;
        float lightness = 100.0f;
        assertEquals("Expecting ripeness score of 0",
                ripenessCalculator.calculateRipenessFromRedness(redness, lightness), 0.0,0.0);
    }

}
