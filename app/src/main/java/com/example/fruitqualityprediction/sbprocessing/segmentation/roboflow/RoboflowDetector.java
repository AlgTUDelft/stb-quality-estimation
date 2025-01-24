package com.example.fruitqualityprediction.sbprocessing.segmentation.roboflow;

import android.graphics.Bitmap;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberryDetector;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * Implements strawberry segmentation using the Roboflow API.
 */
public class RoboflowDetector implements StrawberryDetector {

    final String API_KEY = "6HQ3BYPcnO0otJgxQEPO"; // The API key to use at the endpoint.
    final String MODEL_ENDPOINT = "strawberry---ripe---not-ripe/3"; // The Roboflow endpoint.

    /**
     * Creates a request for the Roboflow model endpoint of the respective segmentation model.
     * This returns a JSON response containing each individual segmentation. This output is then
     * transformed into a List of StrawberrySegments, which can then be used for annotation.
     *
     * @param image the input image.
     *
     * @return a list of segmented strawberries.
     */
    @Override
    public List<StrawberrySegment> detectStrawberries(Mat image) {
        AtomicReference<ArrayList<StrawberrySegment>> strawberries = new AtomicReference<>(new ArrayList<>());

        Thread connectionThread = new Thread(() -> {
            try {
                Bitmap bitmapImage = convertMatToBitmap(image);
                String base64Image = encodeImageToBase64(bitmapImage);
                String uploadURL = buildRoboflowURL();

                HttpURLConnection connection = setupConnection(uploadURL, base64Image);

                String response = getResponse(connection);
                List<StrawberrySegment> segments = parseResponse(response);
                strawberries.set((ArrayList<StrawberrySegment>) segments);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        connectionThread.start();

        try {
            connectionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return strawberries.get();
    }

    /**
     * Converts an OpenCV Mat object to a Bitmap.
     *
     * @param image the input image in Mat format.
     *
     * @return the converted Bitmap object.
     */
    private Bitmap convertMatToBitmap(Mat image) {
        Bitmap bitmapImage = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bitmapImage);
        return bitmapImage;
    }

    /**
     * Encodes a Bitmap image to a Base64 string.
     *
     * @param bitmapImage the input Bitmap image.
     *
     * @return the Base64-encoded string representation of the image.
     */
    private String encodeImageToBase64(Bitmap bitmapImage) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageData = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(imageData);
    }

    /**
     * Builds the URL for the Roboflow API request.
     *
     * @return the constructed URL string.
     */
    private String buildRoboflowURL() {
        String uploadURL = "https://detect.roboflow.com/" + MODEL_ENDPOINT + "?api_key=" + API_KEY;
        // You can add additional parameters to the URL if needed
        // uploadURL += "&param=value";
        return uploadURL;
    }

    /**
     * Sets up the connection to the Roboflow API.
     *
     * @param uploadURL   the URL for the API request.
     * @param base64Image the Base64-encoded image data.
     *
     * @return the HttpURLConnection object for the connection.
     * @throws IOException if an error occurs during the connection setup.
     */
    private HttpURLConnection setupConnection(String uploadURL, String base64Image) throws IOException {
        URL url = new URL(uploadURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", Integer.toString(base64Image.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");
        connection.setUseCaches(false);
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(base64Image);
        wr.close();

        return connection;
    }

    /**
     * Reads and returns the response from the API request.
     *
     * @param connection the HttpURLConnection object for the connection.
     *
     * @return the response string from the API.
     * @throws IOException if an error occurs while reading the response.
     */
    private String getResponse(HttpURLConnection connection) throws IOException {
        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }

    /**
     * Parses the JSON response from the API and creates StrawberrySegment instances.
     *
     * @param response the JSON response string from the API.
     *
     * @return a list of StrawberrySegment instances representing the detected strawberries.
     */
    private List<StrawberrySegment> parseResponse(String response) {
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        JsonArray predictions = jsonResponse.getAsJsonArray("predictions");

        List<StrawberrySegment> segments = new ArrayList<>();
        for (int i = 0; i < predictions.size(); i++) {
            JsonObject prediction = predictions.get(i).getAsJsonObject();
            double x = prediction.get("x").getAsDouble();
            double y = prediction.get("y").getAsDouble();
            double width = prediction.get("width").getAsDouble();
            double height = prediction.get("height").getAsDouble();

            double x1 = x - (width / 2);
            double y1 = y - (height / 2);

            Rect boundingBox = new Rect((int) x1, (int) y1, (int) width, (int) height);
            StrawberrySegment segment = new StrawberrySegment(boundingBox);
            segments.add(segment);
        }

        return segments;
    }
}