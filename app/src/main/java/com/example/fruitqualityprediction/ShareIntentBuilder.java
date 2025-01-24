package com.example.fruitqualityprediction;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import java.io.OutputStream;

/**
 * Provides a small helper class to create Share Intents.
 */
public class ShareIntentBuilder {

    private final Context context; // The current context.

    /**
     * Creates a share intent helper class to reduce coupling in the camera fragment.
     *
     * @param context the context to create the share intent in.
     */
    public ShareIntentBuilder(Context context) {
        this.context = context;
    }

    /**
     * Constructs the share intent used in the camera fragment.
     *
     * @param bitmap the bitmap to share.
     *
     * @return the intent to start.
     */
    public Intent create(Bitmap bitmap) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "share_temp");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        ContentResolver contentResolver = this.context.getContentResolver();
        Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        OutputStream outputStream;
        try {
            outputStream = contentResolver.openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            outputStream.close();
        } catch (Exception e) {
            Log.e("SHARE IMAGE", "Failed to share image.", e);
        }
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        return intent;
    }
}
