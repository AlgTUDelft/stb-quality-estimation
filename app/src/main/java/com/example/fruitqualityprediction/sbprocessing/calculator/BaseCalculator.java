package com.example.fruitqualityprediction.sbprocessing.calculator;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import com.example.fruitqualityprediction.providers.PreferenceProvider;
import com.example.fruitqualityprediction.providers.TimeProvider;
import org.tensorflow.lite.Interpreter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides an outline for the Brix and firmness calculators, as they share some functionality.
 */
public abstract class BaseCalculator {

    protected final transient Context context; // The current context.
    protected final PreferenceProvider preferenceProvider; // The user's preferences.
    protected final TimeProvider timeProvider; // The calculator's time provider.

    /**
     * Constructs a BaseCalculator object with the given context and time provider. Used for
     * value calculators such as Brix or Firmness, which share functionality.
     *
     * @param context      the current Android context.
     * @param timeProvider the time provider.
     */
    public BaseCalculator(Context context, TimeProvider timeProvider, PreferenceProvider preferenceProvider) {
        this.timeProvider = timeProvider;
        this.context = context;
        this.preferenceProvider = preferenceProvider;
    }

    /**
     * Convert the data in the csv to a list of lists, where each list in the list represents a row in the csv file.
     *
     * @param csvFile the csv file to convert.
     *
     * @return the list of lists.
     */
    public List<List<String>> getRowsFromCSV(String csvFile) {
        List<List<String>> rows = new ArrayList<>();
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(csvFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                List<String> row = Arrays.asList(line.split(","));
                rows.add(row);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    /**
     * Find the 336 rows above and including the row which contains the given timestamp.
     *
     * @param n         the number of rows
     * @param fileName  the file name of the climate data
     * @param timestamp the timestamp to look for
     *
     * @return 336 rows above and including the row which contains the given timestamp. This represents
     *         climate data from the last 14 days or the last 336 hours.
     */
    public List<List<String>> getLastNRowsByTimestamp(int n, String fileName,String directory, String timestamp) {
        List<List<String>> rows = getRowsFromCSV(directory + fileName);
        int timestampIndex = -1;
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (row.get(0).equals(timestamp)) {
                timestampIndex = i;
                break;
            }
        }
        if (timestampIndex == -1) {
            return null;
        }
        return rows.subList(Math.max(timestampIndex - (n - 1), 0), timestampIndex + 1);
    }

    /**
     * Parse the data by converting string values to double values and empty values to Double.Nan.
     *
     * @param data the data to parse.
     *
     * @return the parsed data.
     */
    public double[][] parseData(String[][] data) {
        double[][] parsedData = new double[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 1; j < data[i].length; j++) {
                if (!data[i][j].equals("")) {
                    double value = Double.parseDouble(data[i][j]);
                    parsedData[i][j] = value;
                } else {
                    parsedData[i][j] = Double.NaN;
                }
            }
        }
        return parsedData;
    }

    /**
     * Preprocess the data by parsing it.
     *
     * @param data the data to preprocess.
     *
     * @return the preprocessed data.
     */
    public double[][] preprocessData(String[][] data) {
        return parseData(data);
    }

    /**
     * Removes the provided columns from the 2D array.
     *
     * @param data           the data, represented as a 2D array. Could be the climate data or the weights.
     * @param excludeIndices the indices of the columns to be removed.
     *
     * @return the modified data with the removed columns.
     */
    public double[][] removeGivenColumns(double[][] data, int[] excludeIndices) {
        double[][] modifiedData = new double[data.length][data[0].length - excludeIndices.length];
        for (int i = 0; i < data.length; i++) {
            int k = 0;
            for (int j = 0; j < data[i].length; j++) {
                if (!contains(excludeIndices, j)) {
                    modifiedData[i][k] = data[i][j];
                    k++;
                }
            }
        }
        return modifiedData;
    }

    /**
     * Returns whether a value is present in an array.
     *
     * @param arr   the array to search in.
     * @param value the value to search for.
     *
     * @return a boolean indicating whether the value is present in the array.
     */
    private boolean contains(int[] arr, int value) {
        for (int i : arr) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs the multiplication of the weights and the climate data.
     *
     * @param weights             the weights, either the quantile weights or the mean weights.
     * @param avgFeatures         the averaged feature values of the last 336 hours, or 14 days.
     * @param quantiles           the quantiles or the mean array of the brix/firmness distribution
     *                            that is in the process of being calculated.
     * @param climateDataFilename the climate data file.
     * @param weightsFilename     the weights file.
     */
    public void multiplyWeightsWithClimateData(double[][] weights, double[] avgFeatures, double[] quantiles,
                                               String climateDataFilename, String weightsFilename) {
        FeaturesOrderRetriever featuresOrderRetriever = new FeaturesOrderRetriever(context, weightsFilename,
                climateDataFilename);
        List<Integer> featuresOrder = featuresOrderRetriever.getFeaturesOrder();
        for (int weightsColumnIndex = 0; weightsColumnIndex < weights[0].length; weightsColumnIndex++) {
            dotProduct(weights, weightsColumnIndex, avgFeatures, featuresOrder.get(weightsColumnIndex), quantiles);
        }
    }

    /**
     * Calculates the dot product of a column of weights and an item of avgFeatures.
     *
     * @param weights          the weights, represented as a 2D array.
     * @param column           the column index in the weights.
     * @param avgFeatures      the averaged feature values of the last 336 hours, or 14 days.
     * @param avgFeaturesIndex the index of the element in average features.
     * @param quantiles        the quantiles or the mean array of the Brix/firmness distribution
     *                         that is in the process of being calculated.
     */
    private static void dotProduct(double[][] weights, int column, double[] avgFeatures, int avgFeaturesIndex,
                                   double[] quantiles) {
        for (int row = 0; row < weights.length; row++) {
            quantiles[row] += weights[row][column] * avgFeatures[avgFeaturesIndex];
        }
    }

    /**
     * Computes the quantiles taking into consideration the mean and the variance
     * @param quantiles the quantiles to compute.
     */
    public void computeActualQuantilesFromMeanAndVariance(double[] quantiles) {
        if (quantiles.length == 1) {
            quantiles[0] = computeValueUsingMeanAndVariance((float) quantiles[0], 8.15574002328204f, 1.627314306578812f);
        } else {
            quantiles[0] = computeValueUsingMeanAndVariance((float) quantiles[0], 6.910000000000001f, 1.7156857142857143f);
            quantiles[1] = computeValueUsingMeanAndVariance((float) quantiles[1], 7.290714285714286f, 1.6462137755102046f);
            quantiles[2] = computeValueUsingMeanAndVariance((float) quantiles[2], 7.629642857142858f, 1.5955034438775508f);
            quantiles[3] = computeValueUsingMeanAndVariance((float) quantiles[3], 7.834999999999999f, 1.563360714285714f);
            quantiles[4] = computeValueUsingMeanAndVariance((float) quantiles[4], 8.033928571428572f, 1.4691167091836737f);
            quantiles[5] = computeValueUsingMeanAndVariance((float) quantiles[5], 8.292142857142858f, 1.5092096938775512f);
            quantiles[6] = computeValueUsingMeanAndVariance((float) quantiles[6], 8.632857142857143f, 1.5218489795918368f);
            quantiles[7] = computeValueUsingMeanAndVariance((float) quantiles[7], 8.980714285714287f, 1.8081566326530607f);
            quantiles[8] = computeValueUsingMeanAndVariance((float) quantiles[8], 9.470714285714285f, 2.457742346938776f);
        }
    }

    /**
     * Returns the quantiles or the mean array of the Brix/firmness distribution given climate data.
     *
     * @param timestamp                   the timestamp with a precision of 1 hour.
     * @param climateDataFilename         the file name of the climate data.
     * @param weightsFilename             the file name of the weights file.
     * @param excludedWeights             the weights to exclude.
     * @param useHardcodedExcludedWeights whether to use the hardcoded excluded weights.
     * @param weightsDirectory            the weights directory.
     *
     * @return the quantiles or the mean array of the Brix/firmness distribution, depending on the
     *         weights file name.
     */
    public double[] getQuantiles(String timestamp, String climateDataFilename, String weightsFilename,
                                 int[] excludedWeights, boolean useHardcodedExcludedWeights, String weightsDirectory) {
        // last 336 hours or last 14 days
        int n = 336;
        List<List<String>> lastNRows = getLastNRowsByTimestamp(n, climateDataFilename,"climate-data/",  timestamp);
        int rowExpectedSize = 25;
        if (lastNRows.get(0).size() != rowExpectedSize) {
            lastNRows = lastNRows.subList(1, lastNRows.size());
        }
        String[][] data = new String[lastNRows.size()][lastNRows.get(0).size()];
        for (int i = 0; i < lastNRows.size(); i++) {
            List<String> row = lastNRows.get(i);
            data[i] = row.toArray(new String[0]);
        }
        double[][] preprocessedData = preprocessData(data);
        int[] excludeIndices = {0};
        preprocessedData = removeGivenColumns(preprocessedData, excludeIndices);
        for (int i = 0; i < preprocessedData.length; i++) {
            for (int j = 0; j < preprocessedData[i].length; j++) {
                if (Double.isNaN(preprocessedData[i][j])) {
                    preprocessedData[i][j] = 0.0;
                }
            }
        }
        double[][] avgFeatures = new double[1][preprocessedData[0].length];
        for (int i = 0; i < preprocessedData[0].length; i++) {
            double sum = 0.0f;
            for (int j = 0; j < preprocessedData.length; j++) {
                sum += preprocessedData[j][i];
            }
            avgFeatures[0][i] = sum / preprocessedData.length;
        }
        double[][] rows = getRowsFromCSV(weightsDirectory + weightsFilename).stream()
                .map(row -> row.stream()
                        .mapToDouble(Double::parseDouble)
                        .toArray())
                .toArray(double[][]::new);

        if (!useHardcodedExcludedWeights) {
            // Parse the excluded columns into an array of integers
            String excludedColumns = null;
            switch (weightsDirectory) {
                case "weights/" -> excludedColumns = this.preferenceProvider.getModelPreferences().getExcludedBrixColumns();
                case "firmness-weights/" -> excludedColumns = this.preferenceProvider.getModelPreferences().getExcludedFirmnessColumns();
            }
            excludedWeights = parseExcludedColumns(excludedColumns);
        }

        double[][] weights = removeGivenColumns(rows, excludedWeights);
        double[] quantiles = new double[weights.length];

        // climate-data is used, because only the feature names are needed for the features order retrieval,
        // and the feature names in climate-data have the expected format of the column names, while the feature names in
        // climate-data-standardized do not
        multiplyWeightsWithClimateData(weights, avgFeatures[0], quantiles, "climate-data/climate-data.csv",
                weightsDirectory + weightsFilename);

        computeActualQuantilesFromMeanAndVariance(quantiles);

        return quantiles;
    }

    /**
     * Retrieves the excluded columns from the settings in String form and transforms them into
     * an array of integers representing column indices.
     *
     * @param excludedColumns excluded columns in String form.
     *
     * @return excluded columns in array form.
     */
    private int[] parseExcludedColumns(String excludedColumns) {
        if (excludedColumns.isEmpty()) {
            return new int[0];
        }

        String[] indicesArray = excludedColumns.split(",");
        int[] indices = new int[indicesArray.length];
        for (int i = 0; i < indicesArray.length; i++) {
            indices[i] = Integer.parseInt(indicesArray[i].trim());
        }

        return indices;
    }

    /**
     * Runs a Brix/firmness prediction model based on a provided model name.
     *
     * @param input         the input of the model. It is the concatenated feature array obtain from
     *                      the image encoder and the quantiles or the mean array of the
     *                      Brix/firmness distribution.
     * @param modelFileName the name of the Brix/firmness prediction model.
     *
     * @return an array of size 1, containing the Brix/firmness.
     */
    public float[] runModel(Context context, float[] input, String modelFileName, String folderName) {
        try {
            // Load the model file from the assets directory
            MappedByteBuffer modelBuffer = loadModelFile(context, modelFileName, folderName);

            // Create an Interpreter for the model
            Interpreter.Options options = new Interpreter.Options();
            Interpreter interpreter = new Interpreter(modelBuffer, options);

            // Define the input and output arrays
            float[][] inputArray = new float[1][input.length];
            inputArray[0] = input;
            float[][] outputArray = new float[1][1];

            // Run inference using the interpreter
            interpreter.run(inputArray, outputArray);

            // Retrieve the result from the output array
            float[] result = outputArray[0];

            // Clean up resources
            interpreter.close();

            // Return the result
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // Return null in case of an error
    }

    /**
     * Converts an array of type float to an array of type double.
     *
     * @param arr an array of type float.
     *
     * @return an array of type double.
     */
    public double[] floatArrayToDoubleArray(float[] arr) {
        double[] arr2 = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            arr2[i] = arr[i];
        }
        return arr2;
    }

    /**
     * Converts an array of type double to an array of type float.
     *
     * @param arr an array of type double.
     *
     * @return an array of type float.
     */
    public float[] doubleArrayToFloatArray(double[] arr) {
        float[] arr2 = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            arr2[i] = (float) arr[i];
        }
        return arr2;
    }

    /**
     * Concatenates two arrays. An important used case is for concatenating the feature array, which is the
     * results of the image encoder with the quantiles or the mean, a result of multiplication of the weights and the
     * climate data.
     *
     * @param arr1 the first array.
     * @param arr2 the second array.
     *
     * @return the concatenated array.
     */
    protected double[] concatenateArrays(double[] arr1, double[] arr2) {
        double[] arr3 = new double[arr1.length + arr2.length];
        int i = 0;
        int j = 0;
        while (i < arr1.length) {
            arr3[j] = arr1[i];
            i++;
            j++;
        }
        i = 0;
        while (i < arr2.length) {
            arr3[j] = arr2[i];
            i++;
            j++;
        }
        return arr3;
    }

    /**
     * Considering the value represents how many standard deviations it is away from the mean,
     * calculates the actual value.
     *
     * @param value    the value.
     * @param mean     the mean of the value distribution.
     * @param variance the variance of the value distribution.
     *
     * @return the actual value.
     */
    public float computeValueUsingMeanAndVariance(float value, float mean, float variance) {
        return mean + (float)(value * Math.sqrt(variance));
    }

    /**
     * Extracts the time from an image name. Returns the image name if the time is not contained in the image name
     * or if it is in the wrong format.
     *
     * @param imgName the image name.
     *
     * @return the formatted image name.
     */
    public String formatImageName(String imgName) {
        String pattern = "(\\d{4})_(\\d{2})(\\d{2})_(\\d{2})(\\d{2})(\\d{2})_.*";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(imgName);

        String formattedImageName = "";

        if (matcher.matches()) {
            // Extract the individual components from the matched groups
            String year = matcher.group(1);
            String month = matcher.group(2);
            String day = matcher.group(3);
            String hour = matcher.group(4);
            String minute = matcher.group(5);
            String second = matcher.group(6);

            int yearInt = Integer.parseInt(year);
            int monthInt = Integer.parseInt(month);
            int dayInt = Integer.parseInt(day);
            int hourInt = Integer.parseInt(hour);
            int minuteInt = Integer.parseInt(minute);
            int secondInt = Integer.parseInt(second);

            timeProvider.setYear(yearInt);
            if (monthInt >= 1 && monthInt <= 12) {
                timeProvider.setMonth(monthInt);
            } else {
                return getCurrentTime();
            }
            YearMonth yearMonth = YearMonth.of(yearInt, monthInt);
            int daysInMonth = yearMonth.lengthOfMonth();
            if (dayInt >= 1 && dayInt <= daysInMonth) {
                timeProvider.setDay(dayInt);
            } else {
                return getCurrentTime();
            }
            if (hourInt >= 0 && hourInt <= 23) {
                timeProvider.setHour(hourInt);
            } else {
                return getCurrentTime();
            }
            if (minuteInt >= 0 && minuteInt <= 59) {
                timeProvider.setMinute(minuteInt);
            } else {
                return getCurrentTime();
            }
            if (secondInt >= 0 && secondInt <= 59) {
                timeProvider.setSecond(secondInt);
            } else {
                return getCurrentTime();
            }

            // Create a LocalDateTime object with the extracted components
            LocalDateTime dateTime = LocalDateTime.of(
                    Integer.parseInt(year),
                    Integer.parseInt(month),
                    Integer.parseInt(day),
                    Integer.parseInt(hour),
                    Integer.parseInt(minute),
                    Integer.parseInt(second)
            );

            // Format the LocalDateTime object to the desired format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            formattedImageName = dateTime.format(formatter);
        } else {
            // imgName does not match the desired format
            formattedImageName = imgName;
        }

        // Return the original image name if it doesn't match the expected format
        return formattedImageName;
    }

    /**
     * Gets the current time in the correct format for the calculator. Rounds to nearest hour.
     *
     * @return time in format for the calculator.
     */
    public String getCurrentTime() {

        timeProvider.setYear(2021);
        timeProvider.roundToNearestHour();

        // Format the current time using the formatter
        return timeProvider.format("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Returns either the current time, or the time contained in the image name.
     *
     * @return the time.
     */
    public String getTime(String imageName) {
        if (imageName != null) {
            String formattedImageName = formatImageName(imageName);
            if (imageName.equals(formattedImageName)) {
                return getCurrentTime();
            } else {
                timeProvider.roundToNearestHour();
                return timeProvider.format("yyyy-MM-dd HH:mm:ss");
            }
        } else {
            return getCurrentTime();
        }
    }

    /**
     * Calculates a features of a strawberry from a Bitmap.
     *
     * @param timestamp           the timestamp of the image.
     * @param strawberryBitmap    picture of the strawberry.
     * @param modelName           the name of the feature prediction model.
     * @param climateDataFilename the name of the file of the climate data.
     * @param weightsFilename     the name of the file of the file, containing weights used in multiplying the climate data
     *                            to result in the quantiles or the mean of the feature distribution.
     * @param quantiles           the quantiles or the mean. If they are not hardcoded, they are null.
     *
     * @return the calculated feature.
     */
    public float calculate(String timestamp,
                           Bitmap strawberryBitmap,
                           String modelName,
                           String climateDataFilename,
                           String weightsFilename,
                           double[] quantiles,
                           int[] excludedWeights,
                           boolean useHardcodedExcludedWeights,
                           String weightsDirectory,
                           String folderName) {
        if (quantiles == null) {
            quantiles = getQuantiles(timestamp, climateDataFilename, weightsFilename, excludedWeights, useHardcodedExcludedWeights, weightsDirectory);
        }
        Encoder encoder = new Encoder(context, preferenceProvider.getModelPreferences().getEncoderModelsList());
        float[] featureArrayFloat = encoder.encodeImage(strawberryBitmap);
        double[] featureArray = floatArrayToDoubleArray(featureArrayFloat);
        double[] quantilesModelInput = concatenateArrays(featureArray, quantiles);
        float[] quantilesModelInputFloat = doubleArrayToFloatArray(quantilesModelInput);
        float feature = runModel(context,quantilesModelInputFloat, modelName, folderName)[0];
        return computeValueUsingMeanAndVariance(feature, 7.94470588f, 2.81376609f);
    }



    /**
     * Loads a TensorFlow Lite model file from the assets directory.
     *
     * @param context The context of the application.
     * @param modelFileName The name of the .tflite model file to be loaded.
     *
     * @return A mapped byte buffer representing the loaded TensorFlow Lite model.
     *
     * @throws IOException if there is an error while reading or mapping the model file.
     */
    protected MappedByteBuffer loadModelFile(Context context, String modelFileName, String folderName) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(folderName + "/" + modelFileName);
        File tempFile = File.createTempFile("temp", null);
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.close();

        RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "r");
        MappedByteBuffer modelBuffer = randomAccessFile.getChannel()
                .map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile.length());
        randomAccessFile.close();

        // Delete the temporary file
        tempFile.delete();

        return modelBuffer;
    }
}
