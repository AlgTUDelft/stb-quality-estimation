package com.example.fruitqualityprediction.sbprocessing.marketability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.core.app.ApplicationProvider;

import com.example.fruitqualityprediction.R;
import com.example.fruitqualityprediction.sbprocessing.marketability.SmoothnessCalculator;

import org.junit.Before;
import org.junit.Test;

public class SmoothnessCalculatorTest {

    private Context context;
    private SmoothnessCalculator smoothnessCalculator;

    @Before
    public void setUp() throws Exception {
        this.context = ApplicationProvider.getApplicationContext();
        this.smoothnessCalculator = new SmoothnessCalculator();
    }

    @Test
    public void compareDifferentSurfaceTypes() {
        Bitmap smoothSurface = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.smooth_surface);
        Bitmap veryRoughSurface = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.very_rough_surface);

        double smoothSurfaceSmoothness = smoothnessCalculator.calculateSmoothness(smoothSurface);
        double veryRoughSurfaceSmoothness = smoothnessCalculator.calculateSmoothness(veryRoughSurface);

        assertTrue(smoothSurfaceSmoothness < veryRoughSurfaceSmoothness);
    }

    @Test
    public void compareStrawberrySmoothnessTest1() {
        Bitmap strawberry = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberry);
        Bitmap strawberry5 = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberry5);

        double strawberrySmoothness = smoothnessCalculator.calculateSmoothness(strawberry);
        double strawberry5Smoothness = smoothnessCalculator.calculateSmoothness(strawberry5);

        assertTrue(strawberrySmoothness < strawberry5Smoothness);
    }

    @Test
    public void compareStrawberrySmoothnessTest2() {
        Bitmap strawberry = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberry);
        Bitmap strawberryPink = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberrypink);

        double strawberrySmoothness = smoothnessCalculator.calculateSmoothness(strawberry);
        double strawberryPinkSmoothness = smoothnessCalculator.calculateSmoothness(strawberryPink);

        assertTrue(strawberrySmoothness < strawberryPinkSmoothness);
    }

    // Two similar strawberries should return the same result
    @Test
    public void similarStrawberryTest() {
        Bitmap strawberry1 = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberry1);

        double r1 = smoothnessCalculator.calculateSmoothness(strawberry1);
        double r2 = smoothnessCalculator.calculateSmoothness(strawberry1);

        assertEquals(r1, r2, 0.0);
    }

}