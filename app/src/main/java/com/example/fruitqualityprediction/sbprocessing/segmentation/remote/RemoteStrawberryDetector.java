package com.example.fruitqualityprediction.sbprocessing.segmentation.remote;

import android.util.Log;
import com.example.fruitqualityprediction.sbprocessing.segmentation.SegmentationUtils;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberryDetector;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sends an image to a remote server to detect the strawberries in an image.
 */
public class RemoteStrawberryDetector implements StrawberryDetector {

    private static final String ENDPOINT_PROTOCOL = "http://"; // The protocol to use to reach the endpoint.
    private static final String ENDPOINT_HOST = "localhost:8080"; // The endpoint host.
    private static final String ENDPOINT_PATH = "segmentation/"; // The path at the endpoint host.

    private final String segmentationMethod; // The segmentation method to use.

    private int maxSide = 640; // The maximum side of the image for segmentation restrictions.

    /**
     * A constructor that initializes the segmentation method.
     *
     * @param method the segmentation method. Accepted methods: 'color', 'yolox'
     */
    public RemoteStrawberryDetector(String method) {
        this.segmentationMethod = method;
    }

    /**
     * A setter for the maximum side. The maximum side determines
     * how big the biggest side of the image will be after resizing.
     *
     * @param maxSide the maximum side.
     */
    public void setMaxSide(int maxSide) {
        this.maxSide = maxSide;
    }

    /**
     * Sends an image to a remote server for segmentation.
     *
     * @param image the input image.
     *
     * @return the bounding boxes of the detected segments.
     */
    @Override
    public List<StrawberrySegment> detectStrawberries(Mat image) {
        final int originalHeight = image.height();
        if (this.maxSide > 0) {
            image = SegmentationUtils.resizeImage(image, this.maxSide);
        }

        byte[] data = imageToByteArray(image); // Convert to byte array.

        final int height = image.height();
        final int width = image.width();
        final int type = image.type();
        final float scaling = (float) height / originalHeight;

        AtomicReference<ArrayList<StrawberrySegment>> strawberries = new AtomicReference<>(new ArrayList<>());
        Thread connectionThread = new Thread(() -> {
            try {
                // Construct request parameters
                String parameters =
                        "?height=" + height +
                                "&width=" + width +
                                "&type=" + type;

                // Construct full request
                URL url = new URL(
                        ENDPOINT_PROTOCOL +
                                ENDPOINT_HOST + "/" +
                                ENDPOINT_PATH +
                                segmentationMethod +
                                parameters);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/octet-stream");
                connection.setRequestProperty("Content-Length", String.valueOf(data.length));

                // Fill request body with byte array
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.write(data);
                outputStream.flush();
                outputStream.close();

                // Get response
                int responseCode = connection.getResponseCode();
                Log.d("SENT", Integer.toString(responseCode));

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Decode response
                    String responseString = buildStringFromStream(connection.getInputStream());
                    strawberries.set(decodeStrawberryList(responseString, scaling));
                } else {
                    Log.d("HTTP Error", connection.toString());
                }

                connection.disconnect();
            } catch (IOException | JSONException e) {
                Log.d("REQUEST FAILED", e.toString());
            }
        });

        connectionThread.start();
        try {
            connectionThread.join();
        } catch (InterruptedException e) {
            Log.d("CONNECTION", e.toString());
        }

        return strawberries.get();
    }

    /**
     * Converts an OpenCV Mat object to byte array.
     *
     * @param image the input Mat image.
     *
     * @return the converted byte array.
     */
    public byte[] imageToByteArray(Mat image) {
        int dataSize = (int) (image.total() * image.channels());
        byte[] data = new byte[dataSize];

        image.get(0,0, data);

        return data;
    }

    /**
     * Decodes the JSON response containing the bounding boxes.
     *
     * @param responseString the JSON response.
     * @param scaling the scaling applied to the image.
     *
     * @return the strawberry segments generated from the detected bounding boxes.
     */
    public ArrayList<StrawberrySegment> decodeStrawberryList(String responseString, float scaling) throws IOException, JSONException {
        ArrayList<StrawberrySegment> strawberries = new ArrayList<>();
        if (!responseString.isEmpty()) {
            JSONArray jsonArray = new JSONArray(responseString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int x = (int) (jsonObject.getInt("x") / scaling);
                int y = (int) (jsonObject.getInt("y") / scaling);
                int width = (int) (jsonObject.getInt("width") / scaling);
                int height = (int) (jsonObject.getInt("height") / scaling);

                Rect boundingBox = new Rect(x, y, width, height);
                strawberries.add(new StrawberrySegment(boundingBox));
            }
        }

        return strawberries;
    }

    /**
     * Constructs a string from an input stream.
     *
     * @param inputStream the input stream.
     *
     * @return a string.
     */
    public String buildStringFromStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        inputStream.close();

        return response.toString();
    }
}
