package com.example.fruitqualityprediction.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import com.example.fruitqualityprediction.BuildConfig;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import com.example.fruitqualityprediction.R;
import com.example.fruitqualityprediction.settingsadapters.PercentagePreference;
import androidx.preference.ListPreference;
import org.tensorflow.lite.Interpreter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * The fragment where settings are shown and edited.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String BRIX_MODELS_DIRECTORY = "brix-models"; // The directory where different Brix models are located.
    private static final String FIRMNESS_MODELS_DIRECTORY = "firmness-models"; // The directory where different firmness models are located.
    private static final String ENCODER_MODELS_DIRECTORY = "encoder-models"; // The directory where different encoder models are located.
    private static final String CLIMATE_DATA_DIRECTORY = "climate-data"; // The directory where different climate data is located.
    private static final String BRIX_WEIGHTS_DIRECTORY = "weights"; // The directory where different Brix weight files are located.
    private static final String FIRMNESS_WEIGHTS_DIRECTORY = "firmness-weights"; // The directory where different firmness weight files are located.

    private boolean isDeveloperMenuVisible = false; // Whether the developer menu is currently visible.

    /**
     * Initializes the preferences for the fragment based on the provided parameters.
     *
     * @param savedInstanceState if the fragment is being re-created from a previous saved state,
     *                           this is the state.
     * @param rootKey            if non-null, this preference fragment should be rooted at the
     *                           PreferenceScreen with this key.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        PercentagePreference percentagePreference = findPreference("percentage_preference");

        setPercentagePreferenceSummary(percentagePreference);

        PreferenceCategory processingCategory = findPreference("processing_category");
        createSelectedAttributesPreference(processingCategory);
        SwitchPreferenceCompat attributesShown = findPreference("text_visibility");
        MultiSelectListPreference selectShown = findPreference("selected_attributes");

        PreferenceCategory modelsCategory = findPreference("models_category");
        EditTextPreference excludedBrixColumnsPref = findPreference("excluded_brix_columns");
        EditTextPreference excludedFirmnessColumnsPref = findPreference("excluded_firmness_columns");
        Preference enterSecretKeyPref = findPreference("enter_developer_key");

        boolean isKeyEntered = isKeyEntered();

        if (isKeyEntered || isDeveloperMenuVisible) { // Check if the key is entered or secret menu is visible
            modelsCategory.setVisible(true);
            excludedBrixColumnsPref.setVisible(true);
            excludedFirmnessColumnsPref.setVisible(true);
            isDeveloperMenuVisible = true; // Update the visibility state
        } else {
            modelsCategory.setVisible(false);
            excludedBrixColumnsPref.setVisible(false);
            excludedFirmnessColumnsPref.setVisible(false);
            isDeveloperMenuVisible = false; // Update the visibility state
        }

        createBrixModelsPreference(modelsCategory);
        createFirmnessModelsPreference(modelsCategory);
        createEncoderModelsPreference(modelsCategory);
        createClimateDataPreference(modelsCategory);
        createWeightsPreference(modelsCategory);
        createFirmnessWeightsPreference(modelsCategory);

        setBrixWeightsPreferenceSummary();
        setFirmnessWeightsPreferenceSummary();
        setExcludedColumnsPreferenceSummary(excludedBrixColumnsPref, "weights/", "weights_list");
        setExcludedColumnsPreferenceSummary(excludedFirmnessColumnsPref, "firmness-weights/","firmness_weights_list");
        setClimateDataPreferenceSummary();

        setShownAttributesDependency(attributesShown, selectShown);

        setupResetButton(savedInstanceState, rootKey);

        enterSecretKeyPref.setOnPreferenceClickListener(preference -> {
            showSecretKeyDialog();
            return true;
        });

        restrictInputToNumbers(findPreference("ripeness_minimum"));
        restrictInputToNumbers(findPreference("ripeness_maximum"));
        restrictInputToNumbers(findPreference("time_minimum"));
        restrictInputToNumbers(findPreference("time_maximum"));
    }

    /**
     * Restricts the input type of a preference to floating point numbers.
     *
     * @param editTextPreference The preference object
     */
    private void restrictInputToNumbers(EditTextPreference editTextPreference) {
        editTextPreference.setOnBindEditTextListener(editText -> editText.setInputType(
                        InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_SIGNED |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
                ));
    }

    /**
     * Prompts the user to enter the password for the developer options.
     */
    private void showSecretKeyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Developer Key");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String enteredKey = input.getText().toString();
            String advancedKey = BuildConfig.ADVANCED_KEY;

            if (enteredKey.equals(advancedKey)) {
                PreferenceCategory modelsCategory = findPreference("models_category");
                EditTextPreference excludedBrixColumnsPref = findPreference("excluded_brix_columns");
                EditTextPreference excludedFirmnessColumnsPref = findPreference("excluded_firmness_columns");
                modelsCategory.setVisible(true);
                excludedBrixColumnsPref.setVisible(true);
                excludedFirmnessColumnsPref.setVisible(true);
                isDeveloperMenuVisible = true; // Update the visibility state
            } else {
                Toast.makeText(requireContext(), "Invalid developer key", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Whether the correct key was ever entered.
     * @return whether the developer options are enabled.
     */
    private boolean isKeyEntered() {
        String enteredKey = System.getProperty("ADVANCED_KEY");
        return enteredKey != null;
    }


    /**
     * Sets up the reset button preference click listener. When clicked, it resets all of the
     * current settings to the default settings of the application.
     *
     * @param savedInstanceState if the fragment is being re-created from a previous saved state,
     *                           this is the state.
     * @param rootKey            if non-null, this preference fragment should be rooted at the
     *                           PreferenceScreen with this key.
     */
    private void setupResetButton(final Bundle savedInstanceState, final String rootKey) {
        Preference resetButton = findPreference("reset_button");
        resetButton.setOnPreferenceClickListener(preference -> {
            // Reset all preferences to their default values
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.edit().clear().apply();

            // Update the UI to reflect the default values
            getPreferenceScreen().removeAll();
            addPreferencesFromResource(R.xml.preferences);

            // Recreate the preferences and their associated UI elements
            onCreatePreferences(savedInstanceState, rootKey);

            // Show a notification that settings have been reset
            Toast.makeText(getContext(), "Settings reset to default", Toast.LENGTH_SHORT).show();

            return true;
        });
    }

    /**
     * Creates the shown attributes preference and adds it to the processing category.
     *
     * @param processingCategory the preference category for processing.
     */
    private void createSelectedAttributesPreference(PreferenceCategory processingCategory) {
        MultiSelectListPreference attributePreference = new MultiSelectListPreference(requireContext());
        attributePreference.setKey("selected_attributes");
        attributePreference.setTitle("Select shown attributes");
        CharSequence[] attributeValues = {"Brix", "Marketability", "Ripeness", "Firmness"};
        CharSequence[] attributeEntries = {"Brix", "Marketability", "Ripeness", "Firmness"};
        attributePreference.setEntries(attributeEntries);
        attributePreference.setEntryValues(attributeValues);
        processingCategory.addPreference(attributePreference);
    }

    /**
     * Creates the Brix models preference and adds it to the models category.
     *
     * @param modelsCategory the preference category for models.
     */
    private void createBrixModelsPreference(PreferenceCategory modelsCategory) {
        ListPreference brixModelsList = createListPreference("brix_models_list", "Brix Model",
                "Select a Brix Model", getFilesFromDirectory(BRIX_MODELS_DIRECTORY));
        brixModelsList.setDefaultValue(
                "reg_by-m5m4-mean-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite");
        modelsCategory.addPreference(brixModelsList);

        setListPreferenceSummary(brixModelsList, BRIX_MODELS_DIRECTORY);
    }

    /**
     * Creates the firmness models preference and adds it to the models category.
     *
     * @param modelsCategory the preference category for models.
     */
    private void createFirmnessModelsPreference(PreferenceCategory modelsCategory) {
        ListPreference firmnessModelsList = createListPreference("firmness_models_list", "Firmness Model",
                "Select a Firmness Model", getFilesFromDirectory(FIRMNESS_MODELS_DIRECTORY));
        firmnessModelsList.setDefaultValue(
                "reg_by-m5m4-mean-modelb-by-l1-w0-KRR-a100-d1-all_ckpt_s1.tflite");
        modelsCategory.addPreference(firmnessModelsList);

        setListPreferenceSummary(firmnessModelsList, FIRMNESS_MODELS_DIRECTORY);
    }

    /**
     * Creates the Encoder models preference and adds it to the models category.
     *
     * @param modelsCategory the preference category for models.
     */
    private void createEncoderModelsPreference(PreferenceCategory modelsCategory) {
        ListPreference encoderModelsList = createListPreference("encoder_models_list", "Encoder Model",
                "Select an Encoder Model", getFilesFromDirectory(ENCODER_MODELS_DIRECTORY));
        encoderModelsList.setDefaultValue("image-encoder.tflite");
        modelsCategory.addPreference(encoderModelsList);
        setListPreferenceSummary(encoderModelsList, ENCODER_MODELS_DIRECTORY);
    }

    /**
     * Creates the Climate Data preference and adds it to the models category.
     *
     * @param modelsCategory the preference category for models.
     */
    private void createClimateDataPreference(PreferenceCategory modelsCategory) {
        ListPreference climateDataList = createListPreference("climate_data_list", "Climate Data",
                "Select a Climate Data File", getFilesFromDirectory(CLIMATE_DATA_DIRECTORY));
        climateDataList.setDefaultValue("climate-data-standardized.csv");
        modelsCategory.addPreference(climateDataList);
    }

    /**
     * Creates the Weights preference and adds it to the models category.
     *
     * @param modelsCategory the preference category for models.
     */
    private void createWeightsPreference(PreferenceCategory modelsCategory) {
        ListPreference weightDataList = createListPreference("weights_list", "Brix Weights",
                "Select a Brix Weights File", getFilesFromDirectory(BRIX_WEIGHTS_DIRECTORY));
        weightDataList.setDefaultValue("KRR-a100-d1_weights_mean.csv");
        modelsCategory.addPreference(weightDataList);
    }

    /**
     * Creates the Weights preference and adds it to the models category.
     *
     * @param modelsCategory the preference category for models.
     */
    private void createFirmnessWeightsPreference(PreferenceCategory modelsCategory) {
        ListPreference weightDataList = createListPreference("firmness_weights_list", "Firmness Weights",
                "Select a Firmness Weights File", getFilesFromDirectory(FIRMNESS_WEIGHTS_DIRECTORY));
        weightDataList.setDefaultValue("KRR-a100-d1_weights_mean.csv");
        modelsCategory.addPreference(weightDataList);
    }

    /**
     * Sets the summary for the Climate Data preference to display the selected data climate file.
     */
    private void setClimateDataPreferenceSummary() {
        ListPreference climateDataList = findPreference("climate_data_list");
        climateDataList.setSummaryProvider((Preference.SummaryProvider<ListPreference>) preference -> {
            String selectedClimateData = preference.getValue();
            return selectedClimateData;
        });
    }

    /**
     * Sets the summary for the Weights preference based on the selected weights file.
     */
    private void setBrixWeightsPreferenceSummary() {
        ListPreference weightDataList = findPreference("weights_list");
        weightDataList.setSummaryProvider((Preference.SummaryProvider<ListPreference>) preference -> {
            String selectedWeightsFile = preference.getValue();
            int numColumns = getNumColumnsFromCSV("weights/" + selectedWeightsFile);
            return "Features: " + numColumns;
        });
        weightDataList.setOnPreferenceChangeListener((preference, newValue) -> {
            setExcludedColumnsPreferenceSummary(findPreference("excluded_brix_columns"),
                    "weights/", "weights_list");
            return true;
        });
    }

    /**
     * Sets the summary for the Firmness Weights preference based on the selected weights file.
     */
    private void setFirmnessWeightsPreferenceSummary() {
        ListPreference weightDataList = findPreference("firmness_weights_list");
        weightDataList.setSummaryProvider((Preference.SummaryProvider<ListPreference>) preference -> {
            String selectedWeightsFile = preference.getValue();
            int numColumns = getNumColumnsFromCSV("firmness-weights/" + selectedWeightsFile);
            return "Features: " + numColumns;
        });
        weightDataList.setOnPreferenceChangeListener((preference, newValue) -> {
            setExcludedColumnsPreferenceSummary(findPreference("excluded_firmness_columns"),
                    "firmness-weights/", "firmness_weights_list");
            return true;
        });
    }

    /**
     * Sets the summary for the target percentage ripeness preference
     *
     * @param percentagePreference the target ripeness percentage preference.
     */
    private void setPercentagePreferenceSummary(PercentagePreference percentagePreference) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String selectedPercentage = sharedPreferences.getString(percentagePreference.getKey(), "");

        // Set the summary of the PercentagePreference
        if (!TextUtils.isEmpty(selectedPercentage)) {
            String summary = percentagePreference.getSummaryFromPercentage(selectedPercentage);
            percentagePreference.setSummary(summary);
        }
    }

    /**
     * Sets the summary for the excluded columns preference.
     * 
     * @param excludedColumnsPref the excluded columns preference.
     * @param weightDirectory     the weights directory.
     * @param weightList          the weights list.
     */
    private void setExcludedColumnsPreferenceSummary(EditTextPreference excludedColumnsPref, String weightDirectory, String weightList) {
        excludedColumnsPref.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
            String excludedColumns = preference.getText();

            // Check if excludedColumns is empty
            if (TextUtils.isEmpty(excludedColumns)) {
                return "Excluded columns: None";
            }

            // Validate excluded columns indices
            String[] indices = excludedColumns.split(", ");
            for (String indexStr : indices) {
                try {
                    int index = Integer.parseInt(indexStr.trim());

                    // Check if index is valid
                    if (index < 0 || index >= getNumColumnsFromCSV(weightDirectory + getSelectedWeightsFile(weightList))) {
                        showInvalidExcludedColumnDialog(index);
                        return "Excluded columns: Invalid";
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid excluded column index: " + indexStr, Toast.LENGTH_SHORT).show();
                    return "Excluded columns: Invalid";
                }
            }

            return "Excluded columns: " + excludedColumns;
        });
    }

    /**
     * Helper method to get the selected weights file.
     *
     * @return The selected weights file.
     */
    private String getSelectedWeightsFile(String weightList) {
        ListPreference weightDataList = findPreference(weightList);
        return weightDataList.getValue();
    }

    /**
     * Show an AlertDialog indicating an invalid excluded column index.
     *
     * @param invalidIndex the invalid excluded column index.
     */
    private void showInvalidExcludedColumnDialog(int invalidIndex) {
        Context context = getContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Invalid Excluded Column Index")
                .setMessage("The excluded column index " + invalidIndex + " is invalid. Please enter a valid index.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Sets the summary and change listener for a ListPreference, based on the selected model.
     *
     * @param listPreference the ListPreference object to set the summary and change listener for.
     * @param directory      the directory where the models are stored.
     */
    private void setListPreferenceSummary(ListPreference listPreference, String directory) {
        String selectedModel = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString(listPreference.getKey(), "");
        String modelDimensions = getModelDimensions(directory, selectedModel);
        listPreference.setSummary(modelDimensions);

        listPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            String modelName = (String) newValue;
            String newModelDimensions = getModelDimensions(directory, modelName);
            listPreference.setSummary(newModelDimensions);
            return true;
        });
    }

    /**
     * Retrieves the number of columns from a CSV file.
     *
     * @param csvFile the path to the CSV file.
     *
     * @return the number of columns in the CSV file.
     */
    private int getNumColumnsFromCSV(String csvFile) {
        AssetManager assetManager = requireContext().getAssets();
        try (InputStream inputStream = assetManager.open(csvFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String firstRow = reader.readLine();
            return firstRow.split(",").length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Sets the dependency between a SwitchPreferenceCompat and a MultiSelectListPreference,
     * where the enabling/disabling of the MultiSelectListPreference depends on the value of the
     * SwitchPreferenceCompat.
     *
     * @param attributesShown the SwitchPreferenceCompat representing the attribute to be shown.
     * @param selectShown     the MultiSelectListPreference to be enabled/disabled based on the
     *                        value of attributesShown.
     */
    private void setShownAttributesDependency(SwitchPreferenceCompat attributesShown, MultiSelectListPreference selectShown) {
        attributesShown.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean isTrue = Boolean.parseBoolean(newValue.toString());
            selectShown.setEnabled(isTrue);
            return true;
        });

        selectShown.setEnabled(attributesShown.isChecked());
    }

    /**
     * Creates a ListPreference with the specified parameters.
     *
     * @param key          the key for the ListPreference.
     * @param title        the title of the ListPreference.
     * @param dialogTitle  the title to display in the preference dialog.
     * @param entries      the array of entries to be shown in the preference dialog.
     *
     * @return the created ListPreference object.
     */
    private ListPreference createListPreference(String key, String title, String dialogTitle,
                                                String[] entries) {
        ListPreference listPreference = new ListPreference(requireContext());
        listPreference.setKey(key);
        listPreference.setTitle(title);
        listPreference.setDialogTitle(dialogTitle);
        listPreference.setEntries(entries);
        listPreference.setEntryValues(entries);

        return listPreference;
    }

    /**
     * Retrieves the names of the models available in the specified directory.
     *
     * @param directory the directory from which to fetch the model names.
     *
     * @return an array of strings containing the names of the models in the directory.
     */
    private String[] getFilesFromDirectory(String directory) {
        List<String> modelNames = new ArrayList<>();
        AssetManager assetManager = requireContext().getAssets();
        try {
            String[] files = assetManager.list(directory);
            if (files != null) {
                for (String file : files) {
                    modelNames.add(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return modelNames.toArray(new String[0]);
    }

    /**
     * Retrieves the selected model from the models directory and returns the dimensions of the
     * input, as well as the dimensions of the output in string form. This is used as a visual
     * representation in text form for the input and output dimensions in the settings menu for
     * models.
     *
     * @param directory the directory which the models should be retrieved from. Depends on which
     *                  type of models are being retrieved.
     * @param modelName the name of the model to be retrieved from the directory.
     *
     * @return the input and output shape in String form.
     */
    private String getModelDimensions(String directory, String modelName) {
        StringBuilder dimensions = new StringBuilder();
        try {
            // Open the model file from the directory
            AssetFileDescriptor fileDescriptor = requireContext().getAssets().openFd(directory + "/" + modelName);
            FileInputStream inputStream = fileDescriptor.createInputStream();
            ByteBuffer buffer = inputStream.getChannel().map(FileChannel.MapMode.READ_ONLY,
                    fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());

            // Create the interpreter with the model file
            Interpreter interpreter = new Interpreter(buffer);
            int inputTensorIndex = 0;
            int outputTensorIndex = 0;

            // Get input dimensions for the model
            int[] inputShape = interpreter.getInputTensor(inputTensorIndex).shape();
            dimensions.append("Input Shape: [");
            for (int i = 0; i < inputShape.length; i++) {
                dimensions.append(inputShape[i]);
                if (i < inputShape.length - 1) {
                    dimensions.append(", ");
                }
            }
            dimensions.append("]");

            dimensions.append("\n");

            // Get output dimensions for the model
            int[] outputShape = interpreter.getOutputTensor(outputTensorIndex).shape();
            dimensions.append("Output Shape: [");
            for (int i = 0; i < outputShape.length; i++) {
                dimensions.append(outputShape[i]);
                if (i < outputShape.length - 1) {
                    dimensions.append(", ");
                }
            }
            dimensions.append("]");

            // Closing the interpreter
            interpreter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dimensions.toString();
    }
}
