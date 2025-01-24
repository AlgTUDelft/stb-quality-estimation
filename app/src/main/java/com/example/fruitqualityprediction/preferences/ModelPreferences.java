package com.example.fruitqualityprediction.preferences;

/**
 * A class that contains the model preferences selected in the 'settings' tab.
 */
public class ModelPreferences {
    private final String excludedBrixColumns; // Column indices to exclude from the brix weights file
    private final String excludedFirmnessColumns; // Column indices to exclude from the firmness weights file
    private final String climateDataList;
    private final String brixWeightsList;
    private final String brixModelList;
    private final String firmnessModelList;
    private final String firmnessWeightsList;
    private final String encoderModelsList;

    /**
     * Initializes the fields
     *
     * @param excludedBrixColumns Column indices to exclude from the brix weights file
     * @param excludedFirmnessColumns Column indices to exclude from the firmness weights file
     * @param climateDataList A filename
     * @param brixWeightsList A filename
     * @param brixModelList A filename
     * @param firmnessModelList A filename
     * @param firmnessWeightsList A filename
     * @param encoderModelsList A filename
     */
    public ModelPreferences(String excludedBrixColumns, String excludedFirmnessColumns, String climateDataList, String brixWeightsList, String brixModelList, String firmnessModelList, String firmnessWeightsList, String encoderModelsList) {
        this.excludedBrixColumns = excludedBrixColumns;
        this.excludedFirmnessColumns = excludedFirmnessColumns;
        this.brixWeightsList = brixWeightsList;
        this.brixModelList = brixModelList;
        this.climateDataList = climateDataList;
        this.firmnessModelList = firmnessModelList;
        this.firmnessWeightsList = firmnessWeightsList;
        this.encoderModelsList = encoderModelsList;
    }

    /**
     * A getter for the excluded brix columns.
     *
     * @return Column indices to exclude from the brix weights file
     */
    public String getExcludedBrixColumns() {
        return excludedBrixColumns;
    }

    /**
     * A getter for the excluded firmness columns.
     *
     * @return Column indices to exclude from the firmness weights file
     */
    public String getExcludedFirmnessColumns() {
        return excludedFirmnessColumns;
    }

    /**
     * A getter for the brix model list.
     *
     * @return A filename
     */
    public String getBrixModelList() {
        return brixModelList;
    }

    /**
     * A getter for the climate data list.
     *
     * @return A filename
     */
    public String getClimateDataList() {
        return climateDataList;
    }

    /**
     * A getter for the brix weights list.
     *
     * @return A filename
     */
    public String getBrixWeightsList() {
        return brixWeightsList;
    }

    /**
     * A getter for the firmness model list.
     *
     * @return A filename
     */
    public String getFirmnessModelList() {
        return firmnessModelList;
    }

    /**
     * A getter for the firmness weights list.
     *
     * @return A filename
     */
    public String getFirmnessWeightsList() {
        return firmnessWeightsList;
    }

    /**
     * A getter for the encoder model list.
     *
     * @return A filename
     */
    public String getEncoderModelsList() {
        return encoderModelsList;
    }
}
