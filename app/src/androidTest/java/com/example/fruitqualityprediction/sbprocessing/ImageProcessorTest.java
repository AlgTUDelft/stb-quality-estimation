package com.example.fruitqualityprediction.sbprocessing;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.test.core.app.ApplicationProvider;

import com.example.fruitqualityprediction.preferences.ProcessingPreferences;
import com.example.fruitqualityprediction.providers.PreferenceProvider;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;
import com.example.fruitqualityprediction.sbprocessing.segmentation.color.ColorStrawberryDetector;

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
public class ImageProcessorTest {

    private Mat image;
    private Mat originalImage;
    private ImageProcessor imageProcessor;

    @Mock
    private PreferenceProvider preferenceProvider;

    @Before
    public void setUp() throws Exception {
        OpenCVLoader.initDebug();
        image = new Mat(500, 500, CvType.CV_8UC3, new Scalar(255,255,255));
        originalImage = image.clone();
        Context context = ApplicationProvider.getApplicationContext();
        imageProcessor = new ImageProcessor(image, context, preferenceProvider, null);
    }

    @Test
    public void detectSegments() {
        assertNull(imageProcessor.getStrawberrySegments());
        when(preferenceProvider.getProcessingPreferences()).thenReturn(new ProcessingPreferences(
            new ColorStrawberryDetector(),
        1,
                "",
                true,
                new HashSet<>()));
        imageProcessor.detectSegments();

        assertEquals(new ArrayList<>(), imageProcessor.getStrawberrySegments());
    }

    @Test
    public void importSegments() {
        assertNull(imageProcessor.getStrawberrySegments());

        ArrayList<StrawberrySegment> segments = new ArrayList<>();

        imageProcessor.importBoundingBoxes(segments);

        assertEquals(segments, imageProcessor.getStrawberrySegments());
        assertNotSame(segments, imageProcessor.getStrawberrySegments());
    }

    @Test
    public void extractStrawberryImages() {
        assertNull(imageProcessor.getStrawberrySegments());

        List<StrawberrySegment> segments = new ArrayList<>();
        segments.add(new StrawberrySegment(new Rect(10,10,10,10)));
        segments.add(new StrawberrySegment(new Rect(20,20,20,20)));

        imageProcessor.importBoundingBoxes(segments);

        assertEquals(segments, imageProcessor.getStrawberrySegments());

        imageProcessor.extractStrawberryImages();
        segments = imageProcessor.getStrawberrySegments();

        for(StrawberrySegment segment : segments) {
            assertEquals(segment.getBoundingBox().width, segment.getBitmap().getWidth());
            assertEquals(segment.getBoundingBox().height, segment.getBitmap().getHeight());
        }
    }

    @Test
    public void annotate_withQualityAttribute() {
        assertNull(imageProcessor.getStrawberrySegments());

        List<StrawberrySegment> segments = new ArrayList<>();
        StrawberrySegment s1 = new StrawberrySegment(new Rect(10, 10, 10, 10));
        StrawberrySegment s2 = new StrawberrySegment(new Rect(20, 20, 20, 20));

        s1.setBitmap(createMockBitmap());
        s1.setRipeness(1.0);
        s1.setBrix(1f);
        s1.setFirmness(1f);
        s1.setMarketability(true);
        s1.setRoundness(1f);
        s1.setSmoothness(1.0);

        s2.setBitmap(createMockBitmap());
        s2.setRipeness(1.0);
        s2.setBrix(1f);
        s2.setFirmness(1f);
        s2.setMarketability(true);
        s2.setRoundness(1f);
        s2.setSmoothness(1.0);

        segments.add(s1);
        segments.add(s2);

        imageProcessor.importBoundingBoxes(segments);

        assertEquals(2, imageProcessor.getStrawberrySegments().size());
        imageProcessor.getStrawberrySegments().get(0).setBitmap(createMockBitmap());
        imageProcessor.getStrawberrySegments().get(0).setRipeness(1.0);
        imageProcessor.getStrawberrySegments().get(0).setBrix(12f);
        imageProcessor.getStrawberrySegments().get(0).setFirmness(12f);
        imageProcessor.getStrawberrySegments().get(1).setBitmap(createMockBitmap());
        imageProcessor.getStrawberrySegments().get(1).setRipeness(1.0);
        imageProcessor.getStrawberrySegments().get(1).setBrix(12f);
        imageProcessor.getStrawberrySegments().get(1).setFirmness(12f);

        ImageView imageView = new ImageView(ApplicationProvider.getApplicationContext());
        imageView.setImageBitmap(createMockBitmap());

        Bitmap originalBitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, originalBitmap);

        when(preferenceProvider.getProcessingPreferences()).thenReturn(new ProcessingPreferences(
                new ColorStrawberryDetector(),
                1,
                "Ripeness",
                true,
                new HashSet<>()));
        Bitmap annotatedBitmap = imageProcessor.annotate(imageView);

        // Assert that the annotated bitmap is not null and matches the original image dimensions
        assertNotNull(annotatedBitmap);
        assertEquals(originalBitmap.getWidth(), annotatedBitmap.getWidth());
        assertEquals(originalBitmap.getHeight(), annotatedBitmap.getHeight());
    }

    @Test
    public void annotate_withoutQualityAttribute() {
        assertNull(imageProcessor.getStrawberrySegments());

        List<StrawberrySegment> segments = new ArrayList<>();
        StrawberrySegment s1 = new StrawberrySegment(new Rect(10, 10, 10, 10));
        StrawberrySegment s2 = new StrawberrySegment(new Rect(20, 20, 20, 20));

        s1.setBitmap(createMockBitmap());
        s1.setRipeness(1.0);
        s1.setBrix(1f);
        s1.setFirmness(1f);
        s1.setMarketability(true);
        s1.setRoundness(1f);
        s1.setSmoothness(1.0);

        s2.setBitmap(createMockBitmap());
        s2.setRipeness(1.0);
        s2.setBrix(1f);
        s2.setFirmness(1f);
        s2.setMarketability(true);
        s2.setRoundness(1f);
        s2.setSmoothness(1.0);

        segments.add(s1);
        segments.add(s2);

        imageProcessor.importBoundingBoxes(segments);

        assertEquals(2, imageProcessor.getStrawberrySegments().size());
        imageProcessor.getStrawberrySegments().get(0).setBitmap(createMockBitmap());
        imageProcessor.getStrawberrySegments().get(0).setRipeness(1.0);
        imageProcessor.getStrawberrySegments().get(0).setBrix(12f);
        imageProcessor.getStrawberrySegments().get(0).setFirmness(12f);
        imageProcessor.getStrawberrySegments().get(1).setBitmap(createMockBitmap());
        imageProcessor.getStrawberrySegments().get(1).setRipeness(1.0);
        imageProcessor.getStrawberrySegments().get(1).setBrix(12f);
        imageProcessor.getStrawberrySegments().get(1).setFirmness(12f);

        ImageView imageView = new ImageView(ApplicationProvider.getApplicationContext());
        imageView.setImageBitmap(createMockBitmap());

        Bitmap originalBitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, originalBitmap);

        when(preferenceProvider.getProcessingPreferences()).thenReturn(new ProcessingPreferences(
                new ColorStrawberryDetector(),
                1,
                "Ripeness",
                true,
                new HashSet<>()));
        Bitmap annotatedBitmap = imageProcessor.annotate(imageView);

        // Assert that the annotated bitmap is not null and matches the original image dimensions
        assertNotNull(annotatedBitmap);
        assertEquals(originalBitmap.getWidth(), annotatedBitmap.getWidth());
        assertEquals(originalBitmap.getHeight(), annotatedBitmap.getHeight());

    }

    @Test
    public void getStrawberrySegments() {
        assertNull(imageProcessor.getStrawberrySegments());
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
}