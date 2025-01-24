package com.example.fruitqualityprediction.sbprocessing.calculator.brix;

import android.content.Context;
import android.graphics.Bitmap;
import com.example.fruitqualityprediction.preferences.ModelPreferences;
import com.example.fruitqualityprediction.providers.PreferenceProvider;
import com.example.fruitqualityprediction.providers.TimeProvider;
import com.example.fruitqualityprediction.sbprocessing.calculator.BaseCalculator;

/**
 * Provides the ability to calculate the Brix of a strawberry given an image of a strawberry and a timestamp. Hyperparameters
 * can be specified, such as Brix prediction model, climate data to be used, and weights.
 */
public class BrixCalculator extends BaseCalculator {

    public static final String FOLDER_NAME = "brix-models";
    public static final String WEIGHTS_DIRECTORY = "weights/";

    /**
     * Constructs a BrixCalculator object with the given context and time provider.
     *
     * @param context      the current Android context.
     * @param timeProvider the time provider.
     */
    public BrixCalculator(Context context, TimeProvider timeProvider, PreferenceProvider preferenceProvider) {
        super(context, timeProvider, preferenceProvider);
    }

    /**
     * Calculates the Brix of a strawberry from a Bitmap.
     *
     * @param timestamp        the timestamp of the image.
     * @param strawberryBitmap picture of the strawberry.
     *
     * @return the calculated Brix.
     */
    public float calculateBrix(String timestamp, Bitmap strawberryBitmap) {
        ModelPreferences modelPreferences = preferenceProvider.getModelPreferences();
        return calculate(timestamp,
                strawberryBitmap,
                modelPreferences.getBrixModelList(),
                modelPreferences.getClimateDataList(),
                modelPreferences.getBrixWeightsList(),
                null,
                null,
                false,
                WEIGHTS_DIRECTORY,
                FOLDER_NAME);
    }

    /*
     * Calls {@link BaseCalculator#calculate(String, int, String, String, String, double[], int[], boolean, String, String)} with default parameters
     *
     * @param timestamp    the timestamp of the image.
     * @param strawberryId the id of the strawberry image, accessed through R.drawable.image_name.
     *
     * @return the calculated Brix.

    public float calculateBrix(String timestamp, int strawberryId) {
        return calculate(timestamp,
                strawberryId,
                "reg_by-m5m4-quantiles-model3-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite",
                "climate-data-standardized.csv",
                "KRR-a100-d1_weights_quantiles.csv",
                null,
                new int[]{0,43},
                true,
                WEIGHTS_DIRECTORY,
                FOLDER_NAME);
    }
    */
}
