package com.example.fruitqualityprediction.sbprocessing.roboflow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;
import com.example.fruitqualityprediction.sbprocessing.segmentation.roboflow.RoboflowDetector;

import org.junit.Assert;
import org.junit.Test;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class RoboflowTest {

    @Test
    public void callTest() throws IOException {
        OpenCVLoader.initDebug();
        // Load the image from assets into a Mat object
        Context context = ApplicationProvider.getApplicationContext();
        InputStream inputStream = context.getAssets().open("strawberryplantripe.jpg");
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        Mat image = new Mat();
        Utils.bitmapToMat(bitmap, image);

        // Perform the detection using RoboflowDetector
        RoboflowDetector roboflowDetector = new RoboflowDetector();
        List<StrawberrySegment> segments = roboflowDetector.detectStrawberries(image);

        // Assertions or further processing based on the detected segments
        Assert.assertNotNull(segments);
        Log.d("Segments size: ", String.valueOf(segments.size()));
        Assert.assertTrue(segments.size() == 3);
        Assert.assertFalse(segments.isEmpty());
    }
    @Test
    public void threeStrawberries() throws IOException {
        OpenCVLoader.initDebug();
        // Load the image from assets into a Mat object
        Context context = ApplicationProvider.getApplicationContext();
        InputStream inputStream = context.getAssets().open("three_strawberries.jpg");
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        Mat image = new Mat();
        Utils.bitmapToMat(bitmap, image);

        // Perform the detection using RoboflowDetector
        RoboflowDetector roboflowDetector = new RoboflowDetector();
        List<StrawberrySegment> segments = roboflowDetector.detectStrawberries(image);

        // Assertions or further processing based on the detected segments
        Assert.assertNotNull(segments);
        Log.d("Segments size: ", String.valueOf(segments.size()));
        Assert.assertTrue(segments.size() == 2);
        Assert.assertFalse(segments.isEmpty());
    }

    @Test
    public void singleStrawberry() throws IOException {
        OpenCVLoader.initDebug();
        // Load the image from assets into a Mat object
        Context context = ApplicationProvider.getApplicationContext();
        InputStream inputStream = context.getAssets().open("blue_Strawb.jpg");
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        Mat image = new Mat();
        Utils.bitmapToMat(bitmap, image);

        // Perform the detection using RoboflowDetector
        RoboflowDetector roboflowDetector = new RoboflowDetector();
        List<StrawberrySegment> segments = roboflowDetector.detectStrawberries(image);

        // Assertions or further processing based on the detected segments
        Assert.assertNotNull(segments);
        Log.d("Segments size: ", String.valueOf(segments.size()));
        Assert.assertTrue(segments.size() == 2);
        Assert.assertFalse(segments.isEmpty());
    }
}
