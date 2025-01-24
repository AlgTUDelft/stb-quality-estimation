package com.example.fruitqualityprediction.sbprocessing.calculator;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import org.tensorflow.lite.Interpreter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Class responsible for applying the encoder model to an image. The model only accepts images of
 * size 200x200 pixels, and preprocessing of the image is done before preparing the input for the
 * model
 */
public class Encoder {

    private static final int IMAGE_SIZE = 200; // The input size of images passed to the encoder.

    private final transient Context context; // The current context.
    private final String modelFileName;

    /**
     * Creates a new encoder object.
     *
     * @param context the current Android context.
     */
    public Encoder(Context context, String modelFileName) {
        this.context = context;
        this.modelFileName = modelFileName;
    }

    /**
     * This method is responsible for preprocessing an image that is then used by an encoder model.
     * The model is dependent on the settings tab, where one of multiple models can be chosen.
     *
     * @param image the image to encode.
     *
     * @return an encoded representation of the image as a flattened array of 900 dimensions.
     */
    public float[] encodeImage(Bitmap image) {
        try {
            if (image == null) {
                throw new RuntimeException("Input image for encoder is null");
            }
            Bitmap newImage = Bitmap.createScaledBitmap(image, IMAGE_SIZE, IMAGE_SIZE, false); //Resizing

            // Allocating memory for byteBuffer
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];

            // Extracting pixel values from the resized Bitmap image and storing them in the intValues array
            newImage.getPixels(intValues, 0, newImage.getWidth(), 0, 0, newImage.getWidth(), newImage.getHeight());

            // Populating the ByteBuffer with the normalized RGB channel values of each pixel.
            int pixel = 0;
            for (int i = 0; i < IMAGE_SIZE; i++) {
                for (int j = 0; j < IMAGE_SIZE; j++) {
                    int val = intValues[pixel++]; // rgb
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val) & 0xFF) * (1.f / 255.f));
                }
            }

            // Loading the model file from the assets directory
            MappedByteBuffer modelBuffer = loadModelFile(context, modelFileName);

            // Creating an Interpreter for the model
            Interpreter.Options options = new Interpreter.Options();
            Interpreter interpreter = new Interpreter(modelBuffer, options);

            // Defining the input and output arrays
            int batchSize = 1;
            int inputSize = IMAGE_SIZE * IMAGE_SIZE * 3;
            int outputSize = 10 * 10 * 9;
            float[][][][] inputArray = new float[batchSize][IMAGE_SIZE][IMAGE_SIZE][3];
            float[][][][] outputArray = new float[batchSize][10][10][9];

            // Populating the input array with the byte buffer data
            byteBuffer.rewind();
            for (int i = 0; i < IMAGE_SIZE; i++) {
                for (int j = 0; j < IMAGE_SIZE; j++) {
                    for (int k = 0; k < 3; k++) {
                        inputArray[0][i][j][k] = byteBuffer.getFloat();
                    }
                }
            }

            // Running inference using the interpreter
            interpreter.run(inputArray, outputArray);

            // Retrieving the result from the output array
            float[] result = new float[outputSize];
            int resultIndex = 0;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    for (int k = 0; k < 9; k++) {
                        result[resultIndex++] = outputArray[0][i][j][k];
                    }
                }
            }

            // Cleaning up resources after using the interpreter
            interpreter.close();

            // Return the result in the form of an array
            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }

        // In case the try/catch block fails, return a default value
        return new float[0];
    }

    /**
     * Loads a TensorFlow Lite model file from the assets directory.
     *
     * @param context       the context of the application.
     * @param modelFileName the name of the .tflite model file to be loaded.
     *
     * @return a mapped byte buffer representing the loaded TensorFlow Lite model.
     *
     * @throws IOException if there is an error while reading or mapping the model file.
     */
    private MappedByteBuffer loadModelFile(Context context, String modelFileName) throws IOException {
        AssetManager assetManager = context.getAssets();
        String folderName = "encoder-models";
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
