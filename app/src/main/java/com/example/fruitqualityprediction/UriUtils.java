package com.example.fruitqualityprediction;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

public class UriUtils {

    /**
     * Saves a bitmap to the photo gallery. Taken from
     * <a href="https://stackoverflow.com/questions/56904485/how-to-save-an-image-in-android-q-using-mediastore">...</a>,
     * written by PerracoLabs.
     *
     * @param context     the context of the caller.
     * @param bitmap      the bitmap to save.
     * @param format      the format to which to convert the bitmap.
     * @param mimeType    the multi-media type of the converted file.
     * @param displayName the display name of the converted file.
     *
     * @return the location of the converted file.
     * @throws IOException if the bitmap could not be saved.
     */
    @NonNull
    public Uri saveBitmapToGallery(@NonNull final Context context, @NonNull final Bitmap bitmap,
                                   @NonNull final Bitmap.CompressFormat format,
                                   @NonNull final String mimeType,
                                   @NonNull final String displayName) throws IOException {
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);

        final ContentResolver resolver = context.getContentResolver();
        Uri uri = null;

        try {
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, values);
            if (uri == null) {
                throw new IOException("Failed to create new MediaStore record.");
            }
            try (OutputStream stream = resolver.openOutputStream(uri)) {
                if (stream == null) {
                    throw new IOException("Failed to open output stream.");
                }
                if (!bitmap.compress(format, 95, stream)) {
                    throw new IOException("Failed to save bitmap.");
                }
            }
            return uri;
        }
        catch (IOException e) {
            if (uri != null) {
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(uri, null, null);
            }
            throw e;
        }
    }

    /**
     * Gets the image name from the image.
     *
     * @param context  the context in which to perform the operation.
     * @param imageUri the image uri.
     *
     * @return the image name.
     */
    public String getImageNameFromUri(Context context, Uri imageUri) {
        String imageName = null;
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
            cursor = context.getContentResolver().query(imageUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                imageName = cursor.getString(columnNameIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return imageName;
    }

    /**
     * Saves a bitmap to a temporary file for use in Intents.
     *
     * @param context    the current context.
     * @param identifier a unique identifier used for the filename.
     * @param bitmap     the bitmap to save.
     *
     * @return a URI to a PNG file representing the bitmap.
     */
    public Optional<Uri> convertBitmapToTempUri(Context context, String identifier, Bitmap bitmap) {
        try {
            File imagesFolder = new File(context.getCacheDir(), "images");
            imagesFolder.mkdirs();
            File tempFile = new File(imagesFolder, identifier + ".png");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    "com.example.fruitqualityprediction.provider",
                    tempFile);
            return Optional.of(fileUri);
        }
        catch (IOException ex) {
            Log.e("MAIL ERROR", "Could not save bitmap to temp file.", ex);
            return Optional.empty();
        }
    }
}
