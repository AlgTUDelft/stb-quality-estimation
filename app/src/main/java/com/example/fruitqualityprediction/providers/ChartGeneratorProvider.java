package com.example.fruitqualityprediction.providers;

import com.example.fruitqualityprediction.preferences.VisualisationPreferences;
import com.example.fruitqualityprediction.sbprocessing.FeatureRange;
import com.example.fruitqualityprediction.sbprocessing.visualization.ChartConfiguration;
import com.example.fruitqualityprediction.sbprocessing.visualization.ChartGenerator;
import org.opencv.core.Size;

/**
 * Provides an instance of ChartGenerator using the current configuration.
 */
public class ChartGeneratorProvider {
    private ChartGenerator generator;

    /**
     * Initializes the configuration.
     */
    public ChartGeneratorProvider(VisualisationPreferences preference) {
        updateConfiguration(preference);
    }

    /**
     * Updates the configuration.
     */
    public void updateConfiguration(VisualisationPreferences preference) {
        ChartConfiguration configuration = new ChartConfiguration(
                preference.getRipenessFunction(),
                new Size(700, 700),
                new FeatureRange<>(preference.getTimeMin(), preference.getTimeMax()),
                new FeatureRange<>(preference.getRipenessMin(), preference.getRipenessMax()),
                preference.getTimeUnit(),
                "Ripeness"
        );

        if ((this.generator == null) || (!configuration.equals(this.generator.getConfiguration()))) {
            this.generator = new ChartGenerator(configuration);
        }
    }

    /**
     * Provides an instance of ChartGenerator using the current configuration.
     *
     * @return A chart generator
     */
    public ChartGenerator getGenerator() {
        return this.generator;
    }
}
