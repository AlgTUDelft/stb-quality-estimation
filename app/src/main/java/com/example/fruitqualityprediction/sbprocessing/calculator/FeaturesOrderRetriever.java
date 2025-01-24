package com.example.fruitqualityprediction.sbprocessing.calculator;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides to ability to retrieve the order in which the features of the climate data need to be taken
 * for the multiplication of weights and climate data.
 */
public class FeaturesOrderRetriever {
    private final transient Context context; // The current context.
    private final Map<String, String> dictionary = new HashMap<>(); // The mapping dictionary.
    private final List<String> weightsColumnNames = new ArrayList<>(); // The column names of the weights.
    private final List<String> featuresColumnNames = new ArrayList<>(); // The column names of the features.
    private final String attributeInformationFile; // The attribute information file.
    private final String weightsFile; // The weights file.
    private final String climateDataFile; // The climate data file.

    /**
     * Creates a new order retrieval helper.
     *
     * @param context         the current Android context.
     * @param weightsFile     the weights file.
     * @param climateDataFile the climate data file.
     */
    public FeaturesOrderRetriever(Context context, String weightsFile, String climateDataFile) {
        this.context = context;
        this.attributeInformationFile = "attribute_information/AttributeInformation.csv";
        this.weightsFile = weightsFile;
        this.climateDataFile = climateDataFile;
    }

    /**
     * Based on the attribute information file, creates a dictionary, represented by a hashmap, that maps
     * a weights column name to a climate data column name.
     */
    public void createDictionary() {
        String csvDelimiter = ";";
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(attributeInformationFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(csvDelimiter);
                String data2 = data[2].replace("\u00A0", " ");
                dictionary.put(data[0], data2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the names of the weights based on the weights file.
     */
    public void createWeightsColumnNames() {
        String csvDelimiter = ",";
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(weightsFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            if ((line = reader.readLine()) != null) {
                String[] data = line.split(csvDelimiter);
                weightsColumnNames.addAll(Arrays.asList(data));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the names of the climate data features based on the weights file.
     */
    public void createFeaturesColumnNames() {
        String entryRegex = "\"([^\"]*)\"";
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(climateDataFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            if ((line = reader.readLine()) != null) {
                Pattern pattern = Pattern.compile(entryRegex);
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    String entry = matcher.group(1);
                    if (entry == null) {
                        entry = matcher.group(2);
                    }
                    featuresColumnNames.add(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes the specification of number of weeks that could be present at the end of a weight name.
     *
     * @param input the string to process.
     *
     * @return the processed string.
     */
    public static String removeTrailingDashAndDigit(String input) {
        // Regular expression to match a dash followed by an integer at the end of the string
        String regex = "-\\d$";
        return input.replaceAll(regex, "");
    }

    /**
     * Retrieves the feature column index based on the weights column index.
     *
     * @param weightColumnIndex the weights column index.
     * @param dictionary        the dictionary.
     *
     * @return the weights column index.
     */
    public int getFeatureColumnIndex(int weightColumnIndex, Map<String, String> dictionary) {
        // Get the column name of the first file based on the given index
        String firstFileColumnName = getWeightColumnName(weightColumnIndex);

        if (dictionary.containsKey(firstFileColumnName)) {
            // Lookup the corresponding column name in the second file
            String featureColumnName = dictionary.get(firstFileColumnName);

            // Get the column index of the second file based on the column name
            return getFeatureColumnIndex(featureColumnName);
        } else {
            return -1;
        }
    }

    /**
     * Retrieves the name of a weight based on the column index.
     *
     * @param columnIndex the column index.
     *
     * @return the name of the weight.
     */
    public String getWeightColumnName(int columnIndex) {
        // Check if the given index is valid
        if (columnIndex < 0 || columnIndex > weightsColumnNames.size() - 1) {
            throw new IllegalArgumentException("Invalid column index");
        }

        // Return the column name based on the index
        String item = weightsColumnNames.get(columnIndex);
        return removeTrailingDashAndDigit(item);
    }

    /**
     * Retrieves the index of a feature column based on a feature column name.
     *
     * @param columnName the feature column name.
     *
     * @return the feature column index.
     */
    public int getFeatureColumnIndex(String columnName) {
        // Iterate through the column names to find the matching index
        for (int i = 0; i < featuresColumnNames.size(); i++) {
            if (columnName.equals(featuresColumnNames.get(i))) {
                // Return the index if the column name matches
                return i;
            }
        }

        // If no matching column name is found, throw an exception
        throw new IllegalArgumentException("Invalid column name");
    }

    /**
     * Retrieves the features order by iteratively finding the feature column index every weight needs
     * to be multiplied with.
     *
     * @return the features order.
     */
    public List<Integer> getFeaturesOrder() {
        createDictionary();
        createWeightsColumnNames();
        createFeaturesColumnNames();
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < weightsColumnNames.size(); i++) {
            int featureColumnIndex = getFeatureColumnIndex(i, getDictionary());
            if (featureColumnIndex != -1) {
                order.add(featureColumnIndex);
            }
        }
        return order;
    }

    /**
     * Retrieves the dictionary.
     *
     * @return the dictionary.
     */
    public Map<String, String> getDictionary() {
        return dictionary;
    }
}
