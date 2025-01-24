package com.example.fruitqualityprediction.sbprocessing.brix;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.core.app.ApplicationProvider;

import com.example.fruitqualityprediction.R;
import com.example.fruitqualityprediction.preferences.ModelPreferences;
import com.example.fruitqualityprediction.providers.PreferenceProvider;
import com.example.fruitqualityprediction.providers.TimeProvider;
import com.example.fruitqualityprediction.sbprocessing.calculator.brix.BrixCalculator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class BrixCalculatorTest {
    @Mock
    private PreferenceProvider preferenceProvider;

    @Before
    public void setUp() {
        when(preferenceProvider.getModelPreferences()).thenReturn(new ModelPreferences(
                "0,43",
                "0",
                "climate-data-standardized.csv",
                "KRR-a100-d1_weights_quantiles.csv",
                "reg_by-m5m4-quantiles-model3-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "reg_by-m5m4-mean-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "KRR-a100-d1_weights_mean.csv",
                "image-encoder.tflite"
        ));
    }

    /**
     * Tests the values of the quantiles when using the standardized climate data. The lowest quantile value is around 8.26 and the highest quantile value
     * is around 10.88. The mean is around 9.47. The following timestamp is used: 2021-04-15 14:00:00.
     */
    @Test
    public void quantilesTestClimateDataStandardized() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        double[] expectedQuantiles1 = new double[] {8.262151718139648, 8.604662895202637, 8.904406547546387, 9.098698616027832, 9.258563041687012, 9.509469032287598, 9.846129417419434, 10.256317138671875, 10.88333797454834};
        double[] expectedQuantiles2 = new double[] {9.470901489257812};
        double[] quantiles1 = brixCalculator.getQuantiles("2021-04-15 14:00:00",
                "climate-data-standardized.csv", "KRR-a100-d1_weights_quantiles.csv",
                new int[]{0,43},true, "weights/");
        double[] quantiles2 = brixCalculator.getQuantiles("2021-04-15 14:00:00",
                "climate-data-standardized.csv", "KRR-a100-d1_weights_mean.csv",
                new int[]{0},true,"weights/");
        double delta = 0.1;
        assertArrayEquals(expectedQuantiles1, quantiles1, delta);
        assertArrayEquals(expectedQuantiles2, quantiles2, delta);
    }

    /**
     * Tests the values of the quantiles when using the climate data. The lowest quantile value is around -11.78 and the highest quantile value
     * is around 30.04. The mean is around 7.38. The following timestamp is used: 2021-04-15 14:00:00. The conclusion from this test is that the climate data must be standardized, because
     * the values for the quantiles fall outside the expected Brix values.
     */
    @Test
    public void quantilesTestClimateData() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        double[] expectedQuantiles1 = new double[] {-11.783304214477539, -7.004291534423828, -2.950870990753174, -1.4358720779418945, 4.061666488647461, 9.38169002532959, 13.104987144470215, 18.79428482055664, 30.04135513305664};
        double[] expectedQuantiles2 = new double[] {7.387355327606201};
        double[] quantiles1 = brixCalculator.getQuantiles("2021-04-15 14:00:00",
                "climate-data.csv", "KRR-a100-d1_weights_quantiles.csv",
                new int[]{0,43},true, "weights/");
        double[] quantiles2 = brixCalculator.getQuantiles("2021-04-15 14:00:00",
                "climate-data.csv", "KRR-a100-d1_weights_mean.csv",
                new int[]{0},true, "weights/");
        double delta = 0.1;
        assertArrayEquals(expectedQuantiles1, quantiles1, delta);
        assertArrayEquals(expectedQuantiles2, quantiles2, delta);
    }

    /**
     * The following test has been made in order to compare the values for the brix using different Brix prediction models, and to possibly select the one
     * that gives the most reasonable results.
     */
    @Test
    public void brixTestClimateDataStandardized() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        String timestamp = "2021-04-15 14:00:00";
        Bitmap goodStrawberry1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.good_strawberry1);
        float brix1 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-quantiles-model3-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data-standardized.csv", "KRR-a100-d1_weights_quantiles.csv",
                null, new int[]{0,43},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float brix2 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-quantiles-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s0.tflite",
                "climate-data-standardized.csv", "KRR-a100-d1_weights_quantiles.csv",
                null,new int[]{0,43},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float brix3 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-quantiles-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data-standardized.csv", "KRR-a100-d1_weights_quantiles.csv",
                null, new int[]{0,43},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float brix4 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-mean-model3-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data-standardized.csv", "KRR-a100-d1_weights_mean.csv",
                null,new int[]{0},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float brix5 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-mean-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data-standardized.csv", "KRR-a100-d1_weights_mean.csv",
                null, new int[]{0},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float delta = 0.05f;
        assertEquals(11.634148, brix1, delta);
        assertEquals(14.782795, brix2, delta);
        assertEquals(9.559099, brix3, delta);
        assertEquals(9.847465, brix4, delta);
        assertEquals(11.196546, brix5, delta);
    }

    /**
     * This test was conducted while in the process of finding out that the result of the multiplication of the weights and the climate data represents
     * how many standard deviations away from the mean of the distribution the quantiles or the mean values are. It was necessary to hardcode the quantiles
     * because, the quantiles were close to 0, possibly negative and not in increasing order. This is not the case anymore.
     *
     */
    @Test
    public void brixTestClimateHardcodedQuantilesOrMean() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        String timestamp = "2021-04-15 14:00:00";
        double[] quantiles = new double[] {7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9};
        Bitmap goodStrawberry1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.good_strawberry1);
        float brix1 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-quantiles-model3-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data-standardized.csv", "KRR-a100-d1_weights_quantiles.csv",
                quantiles, new int[]{0,43},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float brix2 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-quantiles-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s0.tflite",
                "climate-data-standardized.csv", "KRR-a100-d1_weights_quantiles.csv",
                quantiles,new int[]{0,43},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float brix3 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-quantiles-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data-standardized.csv", "KRR-a100-d1_weights_quantiles.csv",
                quantiles,new int[]{0,43},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        quantiles = new double[] {7.5};
        float brix4 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-mean-model3-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data/climate-data-standardized.csv", "weights/KRR-a100-d1_weights_mean.csv",
                quantiles,new int[]{0},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float brix5 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-mean-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data/climate-data-standardized.csv", "weights/KRR-a100-d1_weights_mean.csv",
                quantiles, new int[]{0},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float delta = 0.05f;
        assertEquals(10.901943, brix1, delta);
        assertEquals(12.498933, brix2, delta);
        assertEquals(8.462505, brix3, delta);
        assertEquals(8.828803, brix4, delta);
        assertEquals(9.685141, brix5, delta);
    }

    /**
     * The following test has been made in order to compare the values for the brix using different Brix prediction models, and to possibly select the one
     * that gives the most reasonable results. Climate data has been used because it was not known then that the climate data needs to be standardized.
     */
    @Test
    public void brixTestClimateData() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        String timestamp = "2021-04-15 14:00:00";
        Bitmap goodStrawberry1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.good_strawberry1);
        float brix1 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-quantiles-model3-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data.csv", "KRR-a100-d1_weights_quantiles.csv",
                null,new int[]{0,43},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float brix2 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-quantiles-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s0.tflite",
                "climate-data.csv", "KRR-a100-d1_weights_quantiles.csv",
                null, new int[]{0,43},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float brix3 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-quantiles-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data.csv", "KRR-a100-d1_weights_quantiles.csv",
                null, new int[]{0,43},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float brix4 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-mean-model3-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data.csv", "KRR-a100-d1_weights_mean.csv",
                null,new int[]{0},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float brix5 = brixCalculator.calculate(timestamp, goodStrawberry1, "reg_by-m5m4-mean-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data.csv", "KRR-a100-d1_weights_mean.csv",
                null, new int[]{0},true, BrixCalculator.WEIGHTS_DIRECTORY, BrixCalculator.FOLDER_NAME);
        float delta = 0.05f;
        assertEquals(10.601145, brix1, delta);
        assertEquals(2.4646063, brix2, delta);
        assertEquals(5.1600175, brix3, delta);
        assertEquals(8.771408, brix4, delta);
        assertEquals(9.577779, brix5, delta);
    }

    /**
     * This test shows the Brix values for different strawberries, using the same timestamp. The values revolve around  approximately 11.5.
     */
    @Test
    public void brixTest() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        String timestamp = "2021-04-15 14:00:00";
        Bitmap goodStrawberry1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.good_strawberry1);
        Bitmap goodStrawberry2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.good_strawberry2);
        Bitmap badStrawberry1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.bad_strawberry1);
        Bitmap badStrawberry2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.bad_strawberry2);
        Bitmap strawberry1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry1);
        Bitmap strawberry2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry2);
        Bitmap strawberry3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry3);
        Bitmap strawberry4 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry4);
        Bitmap strawberry5 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry5);
        Bitmap strawberry6 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry6);
        Bitmap strawberry7 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry7);
        Bitmap strawberry8 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry8);
        Bitmap strawberry9 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry9);
        Bitmap strawberry10 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry10);
        Bitmap strawberry11 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry11);
        Bitmap strawberry12 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry12);
        Bitmap strawberry13 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry13);
        Bitmap strawberry14 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry14);
        Bitmap strawberry15 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry15);
        Bitmap strawberry16 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry16);
        Bitmap strawberry17 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry17);
        Bitmap strawberry18 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry18);
        Bitmap strawberry19 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry19);
        Bitmap strawberry20 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry20);
        Bitmap strawberry21 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry21);
        Bitmap strawberry22 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry22);
        Bitmap strawberry23 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry23);
        Bitmap strawberry24 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry24);
        Bitmap strawberry25 = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry25);
        float brixGood1 = brixCalculator.calculateBrix(timestamp, goodStrawberry1);
        float brixGood2 = brixCalculator.calculateBrix(timestamp, goodStrawberry2);
        float brixBad1 = brixCalculator.calculateBrix(timestamp, badStrawberry1);
        float brixBad2 = brixCalculator.calculateBrix(timestamp, badStrawberry2);
        float brix1 = brixCalculator.calculateBrix(timestamp, strawberry1);
        float brix2 = brixCalculator.calculateBrix(timestamp, strawberry2);
        float brix3 = brixCalculator.calculateBrix(timestamp, strawberry3);
        float brix4 = brixCalculator.calculateBrix(timestamp, strawberry4);
        float brix5 = brixCalculator.calculateBrix(timestamp, strawberry5);
        float brix6 = brixCalculator.calculateBrix(timestamp, strawberry6);
        float brix7 = brixCalculator.calculateBrix(timestamp, strawberry7);
        float brix8 = brixCalculator.calculateBrix(timestamp, strawberry8);
        float brix9 = brixCalculator.calculateBrix(timestamp, strawberry9);
        float brix10 = brixCalculator.calculateBrix(timestamp, strawberry10);
        float brix11 = brixCalculator.calculateBrix(timestamp, strawberry11);
        float brix12 = brixCalculator.calculateBrix(timestamp, strawberry12);
        float brix13 = brixCalculator.calculateBrix(timestamp, strawberry13);
        float brix14 = brixCalculator.calculateBrix(timestamp, strawberry14);
        float brix15 = brixCalculator.calculateBrix(timestamp, strawberry15);
        float brix16 = brixCalculator.calculateBrix(timestamp, strawberry16);
        float brix17 = brixCalculator.calculateBrix(timestamp, strawberry17);
        float brix18 = brixCalculator.calculateBrix(timestamp, strawberry18);
        float brix19 = brixCalculator.calculateBrix(timestamp, strawberry19);
        float brix20 = brixCalculator.calculateBrix(timestamp, strawberry20);
        float brix21 = brixCalculator.calculateBrix(timestamp, strawberry21);
        float brix22 = brixCalculator.calculateBrix(timestamp, strawberry22);
        float brix23 = brixCalculator.calculateBrix(timestamp, strawberry23);
        float brix24 = brixCalculator.calculateBrix(timestamp, strawberry24);
        float brix25 = brixCalculator.calculateBrix(timestamp, strawberry25);
        float delta = 0.05f;
        assertEquals( 11.634148, brixGood1, delta);
        assertEquals( 11.596604, brixGood2, delta);
        assertEquals(11.47419, brixBad1, delta);
        assertEquals(11.507109, brixBad2, delta);
        assertEquals(11.494509, brix1, delta);
        assertEquals( 11.705663, brix2, delta);
        assertEquals( 11.935935, brix3, delta);
        assertEquals( 11.638942, brix4, delta);
        assertEquals(11.488028, brix5, delta);
        assertEquals(11.507221, brix6, delta);
        assertEquals(11.494242, brix7, delta);
        assertEquals( 11.642197, brix8, delta);
        assertEquals(11.4892435, brix9, delta);
        assertEquals( 11.668884, brix10, delta);
        assertEquals( 11.615195, brix11, delta);
        assertEquals( 11.663831, brix12, delta);
        assertEquals( 11.769739, brix13, delta);
        assertEquals(11.483592, brix14, delta);
        assertEquals( 11.57468, brix15, delta);
        assertEquals( 11.826858, brix16, delta);
        assertEquals( 11.939009, brix17, delta);
        assertEquals( 11.631935, brix18, delta);
        assertEquals( 11.742751, brix19, delta);
        assertEquals( 11.879547, brix20, delta);
        assertEquals( 11.723465, brix21, delta);
        assertEquals( 11.796642, brix22, delta);
        assertEquals(11.47284, brix23, delta);
        assertEquals( 11.634148, brix24, delta);
        assertEquals(11.486715, brix25, delta);
    }

    /**
     * Tests getting the last n rows from the data functionality.
     */
    @Test
    public void testGetLastNRowsByTimestamp() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        List<List<String>> result = brixCalculator.getLastNRowsByTimestamp(3, "sample1.csv", "", "2023-05-13");
        List<List<String>> expectedResult = Arrays.asList(
                Arrays.asList("2023-05-11", "data2"),
                Arrays.asList("2023-05-12", "data3"),
                Arrays.asList("2023-05-13", "data4")
        );
        assertEquals(expectedResult, result);
    }

    /**
     * Tests removing certain columns from the data.
     */
    @Test
    public void testRemoveGivenColumns() {
        double[][] data = {
                {1.0, 2.0, 3.0, 4.0},
                {5.0, 6.0, 7.0, 8.0},
                {9.0, 10.0, 11.0, 12.0}
        };
        int[] excludeIndices = {1, 3};
        double[][] expected = {
                {1.0, 3.0},
                {5.0, 7.0},
                {9.0, 11.0}
        };
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        double[][] result = brixCalculator.removeGivenColumns(data, excludeIndices);
        assertArrayEquals(expected, result);
    }

    /**
     * Tests whether the BrixCalculator works in concordance to the TimeProvider. When requesting
     * The current time, it should provide the time given by the TimeProvider. It should also round
     * up to the nearest hour when given a date from the TimeProvider.
     */
    @Test
    public void testTimeProvider() {
        Context context = ApplicationProvider.getApplicationContext();
        String expectedPattern = "2021-01-01 15:00:00";


        TimeProvider timeProvider = new TimeProvider();
        timeProvider.setYear(2021);
        timeProvider.setMonth(1);
        timeProvider.setDay(1);
        timeProvider.setHour(14);
        timeProvider.setMinute(30);
        timeProvider.setSecond(0);

        BrixCalculator brixCalculator = new BrixCalculator(context, timeProvider, preferenceProvider);

        Assert.assertEquals(expectedPattern, brixCalculator.getCurrentTime());
    }

    @Test
    public void formatImageNameTestValid() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        String imageName = "2021_0625_165759_1114.JPG";
        String formattedImageName = brixCalculator.formatImageName(imageName);
        assertEquals("2021-06-25 16:57:59", formattedImageName);
    }

    @Test
    public void formatImageNameTestInvalidMonth() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        String imageName = "2021_1725_165759_1114.JPG";
        String formattedImageName = brixCalculator.formatImageName(imageName);
        String currentTimestamp = brixCalculator.getCurrentTime();
        assertEquals(currentTimestamp, formattedImageName);
    }

    @Test
    public void formatImageNameTestInvalidDay31June() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        String imageName = "2021_0631_165759_1114.JPG";
        String formattedImageName = brixCalculator.formatImageName(imageName);
        String currentTimestamp = brixCalculator.getCurrentTime();
        assertEquals(currentTimestamp, formattedImageName);
    }

    @Test
    public void formatImageNameTestInvalidDay0June() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        String imageName = "2021_0600_165759_1114.JPG";
        String formattedImageName = brixCalculator.formatImageName(imageName);
        String currentTimestamp = brixCalculator.getCurrentTime();
        assertEquals(currentTimestamp, formattedImageName);
    }

    @Test
    public void formatImageNameTestInvalidHour24() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        String imageName = "2021_0625_245759_1114.JPG";
        String formattedImageName = brixCalculator.formatImageName(imageName);
        String currentTimestamp = brixCalculator.getCurrentTime();
        assertEquals(currentTimestamp, formattedImageName);
    }

    @Test
    public void formatImageNameTestInvalidMinute60() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        String imageName = "2021_0625_166059_1114.JPG";
        String formattedImageName = brixCalculator.formatImageName(imageName);
        String currentTimestamp = brixCalculator.getCurrentTime();
        assertEquals(currentTimestamp, formattedImageName);
    }

    @Test
    public void formatImageNameTestInvalidSecond60() {
        Context context = ApplicationProvider.getApplicationContext();
        BrixCalculator brixCalculator = new BrixCalculator(context, new TimeProvider(), preferenceProvider);
        String imageName = "2021_0625_165760_1114.JPG";
        String formattedImageName = brixCalculator.formatImageName(imageName);
        String currentTimestamp = brixCalculator.getCurrentTime();
        assertEquals(currentTimestamp, formattedImageName);
    }
}