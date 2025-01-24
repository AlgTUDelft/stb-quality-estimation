package com.example.fruitqualityprediction.sbprocessing.marketability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.test.core.app.ApplicationProvider;
import com.example.fruitqualityprediction.R;
import org.junit.Before;
import org.junit.Test;

public class RoundnessCalculatorTest {

    //good_strawberry2
    //strawberry25
    //strawberry9

    //strawberry4
    //strawberrypink

    //strawberry19
    //strawberry20

    private Context context;
    private RoundnessCalculator roundnessCalculator;

    @Before
    public void setUp() throws Exception {
        this.context = ApplicationProvider.getApplicationContext();
        this.roundnessCalculator = new RoundnessCalculator();
    }

    @Test
    public void compareRoundnessTest1() {
        Bitmap goodStrawberry2 = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.good_strawberry2);
        Bitmap strawberry4 = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberry4);

        double goodStrawberry2Roundness = roundnessCalculator.calculateRoundness(goodStrawberry2);
        double strawberry4Roundness = roundnessCalculator.calculateRoundness(strawberry4);

        assertTrue(goodStrawberry2Roundness > strawberry4Roundness);
    }


    @Test
    public void compareRoundnessTest2() {
        Bitmap goodStrawberry2 = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.good_strawberry2);
        Bitmap strawberryPink = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberrypink);

        double goodStrawberry2Roundness = roundnessCalculator.calculateRoundness(goodStrawberry2);
        double strawberryPinkRoundness = roundnessCalculator.calculateRoundness(strawberryPink);

        assertTrue(goodStrawberry2Roundness > strawberryPinkRoundness);
    }


    @Test
    public void compareRoundnessTest3() {
        Bitmap strawberry4 = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberry4);
        Bitmap strawberry19 = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberry19);

        double goodStrawberry4Roundness = roundnessCalculator.calculateRoundness(strawberry4);
        double strawberry19Roundness = roundnessCalculator.calculateRoundness(strawberry19);

        assertTrue(goodStrawberry4Roundness > strawberry19Roundness);
    }


    @Test
    public void compareRoundnessTest4() {
        Bitmap strawberry4 = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberry4);
        Bitmap strawberry20 = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberry20);

        double goodStrawberry4Roundness = roundnessCalculator.calculateRoundness(strawberry4);
        double strawberry20Roundness = roundnessCalculator.calculateRoundness(strawberry20);

        assertTrue(goodStrawberry4Roundness > strawberry20Roundness);
    }


    // Two same strawberries should give the same result
    @Test
    public void testSimilarStrawBerries() {
        Bitmap strawberry1 = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberry1);

        double r1 = roundnessCalculator.calculateRoundness(strawberry1);
        double r2 = roundnessCalculator.calculateRoundness(strawberry1);

        assertEquals(r1, r2, 0.1);
    }


    // Two different strawberries should give different result
    @Test
    public void testDifferentStrawberries() {
        Bitmap strawberry1 = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberry1);
        Bitmap strawberry2 = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.strawberry2);

        double r1 = roundnessCalculator.calculateRoundness(strawberry1);
        double r2 = roundnessCalculator.calculateRoundness(strawberry2);

        assertNotEquals(r1, r2);
    }
}