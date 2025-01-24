package com.example.fruitqualityprediction.providers;

import android.content.SharedPreferences;
import com.example.fruitqualityprediction.preferences.ModelPreferences;
import com.example.fruitqualityprediction.preferences.ProcessingPreferences;
import com.example.fruitqualityprediction.preferences.VisualisationPreferences;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberryDetector;
import com.example.fruitqualityprediction.sbprocessing.segmentation.color.ColorStrawberryDetector;
import com.example.fruitqualityprediction.sbprocessing.segmentation.remote.RemoteStrawberryDetector;
import com.example.fruitqualityprediction.sbprocessing.segmentation.roboflow.RoboflowDetector;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides the current preferences.
 */
public class PreferenceProvider {

    private final SharedPreferences prefs;

    private VisualisationPreferences visualisationPreferences;
    private ProcessingPreferences processingPreferences;
    private ModelPreferences modelPreferences;

    public PreferenceProvider(SharedPreferences prefs) {
        this.prefs = prefs;
        updateProcessingPreference();
        updateModelPreferences();
        updateVisualisationPreferences();
    }

    /**
     * Leads the current values of the visualisation preferences.
     */
    public void updateVisualisationPreferences() {
        String ripenessFunction = this.prefs.getString("ripeness_function", "1/(1+e^(-x+5))");
        double ripenessMin = Double.parseDouble(this.prefs.getString("ripeness_minimum", "0"));
        double ripenessMax = Double.parseDouble(this.prefs.getString("ripeness_maximum", "1"));
        double timeMin = Double.parseDouble(this.prefs.getString("time_minimum", "0"));
        double timeMax = Double.parseDouble(this.prefs.getString("time_maximum", "10"));
        String timeUnit = this.prefs.getString("time_unit", "Weeks");

        this.visualisationPreferences = new VisualisationPreferences(ripenessFunction, ripenessMin, ripenessMax, timeMin, timeMax, timeUnit);
    }

    /**
     * Leads the current values of the processing preferences.
     */
    public void updateProcessingPreference() {
        String segmentation = this.prefs.getString("seg_model","Color-Segmentation");
        StrawberryDetector strawberryDetector = switch (segmentation) {
            case "Roboflow" -> new RoboflowDetector();
            case "Remote-Color-Segmentation" -> new RemoteStrawberryDetector("color");
            case "Remote-YOLOX-Segmentation" -> new RemoteStrawberryDetector("yolox");
            default -> new ColorStrawberryDetector();
        };

        String selectedPercentage = this.prefs.getString("percentage_preference", "100");
        // Remove the "%" symbol from the selected percentage string
        selectedPercentage = selectedPercentage.replaceAll("%", "");
        int targetRipeness = Integer.parseInt(selectedPercentage);

        String boundingBoxColorPreference = this.prefs.getString("bounding_box_color", "Ripeness");
        boolean displayText = this.prefs.getBoolean("text_visibility", false);

        Set<String> selectedAttributes = this.prefs.getStringSet("selected_attributes", new HashSet<>());

        this.processingPreferences = new ProcessingPreferences(strawberryDetector, targetRipeness, boundingBoxColorPreference, displayText, selectedAttributes);
    }

    /**
     * Leads the current values of the model preferences.
     */
    public void updateModelPreferences() {
        String excludedBrixColumns = this.prefs.getString("excluded_brix_columns", "KRR-a100-d1_weights_mean.csv");
        String excludedFirmnessColumns =  this.prefs.getString("excluded_firmness_columns", "KRR-a100-d1_weights_mean.csv");
        String climateDataList = this.prefs.getString("climate_data_list","climate-data-standardized.csv");
        String brixModelList = this.prefs.getString("brix_models_list", "reg_by-m5m4-mean-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite");
        String brixWeightsList = this.prefs.getString("weights_list", "KRR-a100-d1_weights_mean.csv");
        String firmnessModelList = this.prefs.getString("firmness_models_list", "reg_by-m5m4-mean-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite");
        String firmnessWeightsList = this.prefs.getString("firmness_weights_list", "KRR-a100-d1_weights_mean.csv");
        String encoderModelsList = this.prefs.getString("encoder_models_list", "image-encoder.tflite");

        this.modelPreferences = new ModelPreferences(excludedBrixColumns, excludedFirmnessColumns, climateDataList, brixWeightsList, brixModelList ,firmnessModelList, firmnessWeightsList, encoderModelsList);
    }

    /**
     * A getter for the visualisation preferences.
     *
     * @return The current visualisation preferences
     */
    public VisualisationPreferences getVisualisationPreferences() {
        return visualisationPreferences;
    }

    /**
     * A getter for the processing preferences.
     *
     * @return The current processing preferences
     */
    public ProcessingPreferences getProcessingPreferences() {
        return processingPreferences;
    }

    /**
     * A getter for the model preferences.
     *
     * @return The current model preferences
     */
    public ModelPreferences getModelPreferences() {
        return modelPreferences;
    }
}
