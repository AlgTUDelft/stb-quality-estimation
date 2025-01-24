package com.example.fruitqualityprediction.sbprocessing.segmentation.sam;
/*
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.fruitqualityprediction.R;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

/**
 * Segments strawberries using the Segment-Anything model.
 */
public class SegmentAnythingModelEncoder {
/*

    private static final int INPUT_WIDTH = 1024;
    private static final int INPUT_HEIGHT = 1024;
    private static final int INPUT_SIZE = INPUT_WIDTH * INPUT_HEIGHT;
    private static final float SCALE_FACTOR = 1.0f / 255;
    private static final float[] MEAN = new float[] { 123.675f, 116.28f, 103.53f }; // try dynamic?
    private static final float[] STD = new float[] { 58.395f, 57.12f, 57.375f };

    private static final String MODEL_NAME = "bit_b_q.onnx";


    private static Mat img;


    public SegmentAnythingModelEncoder(String modelPath) {

    }

    public static Mat preprocessOpenCV(Bitmap bitmap) {

        Mat image = new Mat();

        Utils.bitmapToMat(bitmap, image);

        Imgproc.resize(image, image, new Size(1024, 1024));

        Mat imgFloat = new Mat(image.rows(), image.cols(), CvType.CV_32FC3);

        image.convertTo(imgFloat, CvType.CV_32FC3, SCALE_FACTOR);

       // imgFloat = centerCrop(imgFloat);

        Mat blob = Dnn.blobFromImage(
                imgFloat,
                SCALE_FACTOR,
                new Size(INPUT_WIDTH, INPUT_HEIGHT),
                MEAN,
                true,
                false
        );

        Core.divide(blob, STD, blob);

        return blob;
    }
    public static Bitmap resizeImage(Bitmap bitmapImage) {

        return bitmapImage;
    }

    /**
     * Takes an image of size 1024x1024 as input and transforms it into OnnxTensor
     *
     * @return an OnnxTensor
     * /
    public static OnnxTensor createTensorFromImage(Bitmap bitmapImage, OrtEnvironment environment) throws OrtException {
       // Mat matImage = new Mat();
        int[] pixels = new int[INPUT_SIZE];
        bitmapImage.getPixels(pixels, 0, INPUT_WIDTH, 0, 0, INPUT_WIDTH, INPUT_HEIGHT);

        float[][][] tensorData = new float[INPUT_HEIGHT][INPUT_WIDTH][3];
        for(int i = 0; i < INPUT_SIZE; i++) {
            int x = i % INPUT_WIDTH;
            int y = i / INPUT_WIDTH;

            tensorData[x][y][0] = (((pixels[i] >> 16) & 0xFF) * SCALE_FACTOR);// - MEAN[0]) / STD[0];
            tensorData[x][y][1] = (((pixels[i] >> 8) & 0xFF) * SCALE_FACTOR);// - MEAN[1]) / STD[1];
            tensorData[x][y][2] = ((pixels[i] & 0xFF) * SCALE_FACTOR); //- MEAN[2]) / STD[2];

            if(tensorData[x][y][0] < 0 || tensorData[x][y][1] <0 || tensorData[x][y][2] <0) {
                System.out.println("asda");
            }
        }

        return OnnxTensor.createTensor(environment, tensorData);
    }

    public static void test(Context context) throws OrtException, IOException {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sb);

        OrtEnvironment environment = OrtEnvironment.getEnvironment();
        OrtSession model = environment.createSession(getPath(MODEL_NAME, context));
        OnnxTensor tensor = createTensorFromImage(bitmap, environment);
        LinkedHashMap<String, OnnxTensor> inputs = new LinkedHashMap<>();
        inputs.put(model.getInputNames().iterator().next(), tensor);
        OrtSession.Result res = model.run(inputs);




        //Mat inputBlob = getPreprocessedImage(bitmap);

        try {
            Net dnnNet = Dnn.readNetFromONNX(getPath(MODEL_NAME, context));
            System.out.println("success");

           // dnnNet.setInput(inputBlob);

            Mat classification = dnnNet.forward();

            Log.d("AA", "Uhhhhhh");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getPath(String file, Context context) throws IOException {
        BufferedInputStream inputStream = new BufferedInputStream(context.getAssets().open(file));
        byte[] data = new byte[inputStream.available()];
        inputStream.read(data);
        inputStream.close();
        File outFile = new File(context.getFilesDir(), file);
        FileOutputStream os = new FileOutputStream(outFile);
        os.write(data);
        os.close();
        return outFile.getAbsolutePath();
    }
*/
}