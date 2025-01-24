package com.example.fruitqualityprediction.sbprocessing.brix;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import static org.junit.Assert.assertEquals;
import androidx.test.core.app.ApplicationProvider;

import com.example.fruitqualityprediction.R;
import com.example.fruitqualityprediction.sbprocessing.calculator.Encoder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ImageEncoderTest {
    Context context = ApplicationProvider.getApplicationContext();
    Encoder encoder = new Encoder(context, "image-encoder.tflite");

    //Tests for image encoding are limited, as there is not much functionality outside of
    //passing input and getting output for a bitmap. We can check that the output dimensionality
    //is of correct size, however.
    @Test
    public void testEncodeImage() {

        // Decode the image file into a Bitmap object
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.strawberry);

        //Running the encodeImage function on the bitmap
        float[] result = encoder.encodeImage(bitmap);

        //Output should be of dimensionality 10x10x9, so 900
        assertEquals("The length of the result is not 900",900, result.length);

    }
    @Test(expected = RuntimeException.class)
    public void encodeImage_invalidBitmap_returnsEmptyArray() {
        // Create a null bitmap
        Bitmap image = null;

        float[] result = encoder.encodeImage(image);
    }


}
