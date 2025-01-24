package com.example.fruitqualityprediction.feedback;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.core.content.FileProvider;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class JsonExporter {

    private static final String JSON_DATA_FOLDER_NAME = "Json-Data";
    private static final String JSON_FILE_NAME = "data";
    private final List<StrawberrySegment> strawberrySegments;

    public JsonExporter(List<StrawberrySegment> strawberrySegments) {
        this.strawberrySegments = strawberrySegments;
    }

    /**
     * Creates json file for the currently processed image.
     *
     * @param context current context of the application
     */
    public void createJson(Context context) {

        Date time = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = fmt.format(time);

        JsonData jsonData = new JsonData(timestamp, this.strawberrySegments);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String JsonToReturn = gson.toJson(jsonData);
        // File would be saved as data.json in the folder Json-Data
        writeToJsonFile(context, JSON_FILE_NAME, JsonToReturn);
        Log.d("export as json", "Json string created");
    }

    /**
     * Writes json to file.
     *
     * @param context current context of the application
     * @param filename is the name of the json file
     * @param updatedJsonData is the contents of the json file to be saved
     */
    private void writeToJsonFile(Context context, String filename, String updatedJsonData) {
        try{
            File jsonFolder = new File(context.getCacheDir(), JSON_DATA_FOLDER_NAME);
            jsonFolder.mkdir();
            //create a new file in the jsonFolder as "<identifier>.json"
            File tempFile = new File(jsonFolder, filename + ".json");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            outputStream.write(updatedJsonData.getBytes());
            outputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a URI to the json file containing data of the processed image.
     *
     * @param context current context of the application
     *
     * @return a URI to the json file containing data of the processed image
     */
    public static Optional<Uri> getJsonUri(Context context) {
        try{
            File jsonDataFolder = new File(context.getCacheDir(), JSON_DATA_FOLDER_NAME);
            File jsonFile = new File(jsonDataFolder, JSON_FILE_NAME + ".json");

            Uri jsonFileUri = FileProvider.getUriForFile(context,"com.example.fruitqualityprediction.provider", jsonFile);
            return Optional.of(jsonFileUri);

        }catch (Exception e) {
            Log.e("JSON ERROR", "Could not find Json file");
            return Optional.empty();
        }
    }
}
