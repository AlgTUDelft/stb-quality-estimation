package com.example.fruitqualityprediction.sbprocessing.marketability;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.test.core.app.ApplicationProvider;
import com.example.fruitqualityprediction.R;
import com.example.fruitqualityprediction.sbprocessing.ripeness.RipenessCalculator;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;

import org.junit.Before;
import org.junit.Test;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Rect;

public class MarketabilityCalculatorTest {
    private Context context;
    private MarketabilityCalculator marketabilityCalculator;

    @Before
    public void setUp() {
        this.context = ApplicationProvider.getApplicationContext();
        this.marketabilityCalculator = new MarketabilityCalculator();

        OpenCVLoader.initDebug();
    }

    @Test
    public void testTrue() {
        StrawberrySegment strawberrySegment = new StrawberrySegment(new Rect());
        strawberrySegment.setRipeness(0.7);
        strawberrySegment.setSmoothness(0.1);
        strawberrySegment.setRoundness(0.2);
        boolean isMarketable = this.marketabilityCalculator.isMarketable(strawberrySegment);

        assertTrue(isMarketable);
    }

    @Test
    public void testFalse() {
        StrawberrySegment strawberrySegment = new StrawberrySegment(new Rect());
        boolean isMarketable = this.marketabilityCalculator.isMarketable(strawberrySegment);

        assertFalse(isMarketable);
    }

}
