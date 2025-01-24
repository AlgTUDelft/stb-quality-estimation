package com.example.fruitqualityprediction.sbprocessing.calculator.firmness;

import android.content.Context;
import android.graphics.Bitmap;
import com.example.fruitqualityprediction.providers.PreferenceProvider;
import com.example.fruitqualityprediction.providers.TimeProvider;
import com.example.fruitqualityprediction.sbprocessing.calculator.BaseCalculator;

/**
 * Provides the ability to calculate the firmness of a strawberry given an image of a strawberry
 * and a timestamp.
 */
public class FirmnessCalculator extends BaseCalculator {

    public static final String FOLDER_NAME = "firmness-models";
    public static final String WEIGHTS_DIRECTORY = "firmness-weights/";

    /**
     * Constructs a FirmnessCalculator object with the given context and time provider.
     *
     * @param context      the current Android context.
     * @param timeProvider the time provider.
     */
    public FirmnessCalculator(Context context, TimeProvider timeProvider, PreferenceProvider preferenceProvider) {
        super(context, timeProvider, preferenceProvider);
    }

    /**
     * Calculates the firmness of a strawberry from a Bitmap.
     *
     * @param timestamp        the timestamp of the image.
     * @param strawberryBitmap picture of the strawberry.
     *
     * @return the calculated firmness.
     */
    public float calculateFirmnessFromView(String timestamp, Bitmap strawberryBitmap) {
        return -1;
        /*
        TODO: Calculate firmness once models are provided

        ModelPreferences modelPreferences = preferenceProvider.getModelPreferences();
        return calculate(timestamp,
                strawberryBitmap,
                modelPreferences.getFirmnessModelList(),
                modelPreferences.getClimateDataList(),
                modelPreferences.getFirmnessWeightsList(),
                null,
                null,
                false,
                WEIGHTS_DIRECTORY,
                FOLDER_NAME);
         */
    }
}
