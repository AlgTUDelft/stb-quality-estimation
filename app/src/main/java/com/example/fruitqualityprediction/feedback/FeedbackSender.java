package com.example.fruitqualityprediction.feedback;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import com.example.fruitqualityprediction.UriUtils;
import com.example.fruitqualityprediction.providers.DateProvider;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * Creates an instance that collects and sends feedback to the system maintainer via email.
 */
public class FeedbackSender {

    public static final String SYSTEM_MAINTAINER_EMAIL = "sysadmin@example.com"; // The email address of the system maintainer.

    private final Bitmap fullImage; // The full processed image.
    private final UriUtils uriUtils; // URI utility methods.
    private final DateProvider dateProvider; // Provides the current date.

    private StrawberrySegment strawberry; // A strawberry segment to provide feedback on.
    private AlertDialog.Builder builder; // The message dialog.

    /**
     * Use to provide feedback on the processing of an overall image. This is used to provide
     * feedback on the segmentation of images.
     *
     * @param fullImage the full processed image.
     */
    public FeedbackSender(Bitmap fullImage, UriUtils uriUtils, DateProvider dateProvider) {
        this.fullImage = fullImage;
        this.uriUtils = uriUtils;
        this.dateProvider = dateProvider;
    }

    /**
     * Use to provide feedback on the processing of an image segment. This is used to provide
     * feedback on the processed values, like if the ripeness or Brix values are off.
     *
     * @param fullImage  the full processed image.
     * @param strawberry the strawberry segment.
     */
    public FeedbackSender(Bitmap fullImage, UriUtils uriUtils, DateProvider dateProvider, StrawberrySegment strawberry) {
        this(fullImage, uriUtils, dateProvider);
        this.strawberry = strawberry;
    }

    /**
     * Use to provide feedback on the processing of an overall image. This is used to provide
     * feedback on the segmentation of images. Also allows for custom message dialogs.
     *
     * @param fullImage the full processed image.
     * @param builder   the alert dialog builder for requesting a message.
     */
    public FeedbackSender(Bitmap fullImage, UriUtils uriUtils, DateProvider dateProvider, AlertDialog.Builder builder) {
        this(fullImage, uriUtils, dateProvider);
        this.builder = builder;
    }

    /**
     * Prompts the user to provide a message along with their feedback. If they accept,
     * the feedback will be sent to the system maintainer.
     *
     * @param context the current Android context.
     */
    public void process(Context context) {
        if (builder == null) {
            builder = new AlertDialog.Builder(context);
        }

        builder.setTitle("Send Feedback");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String msg = input.getText().toString();
            sendFeedback(context, msg);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Highlights the current strawberry in the original image and returns the new image.
     *
     * @return the new bitmap with the current strawberry highlighted.
     */
    private Bitmap createHighlightedSegmentBitmap() {
        if (this.strawberry == null) {
            return this.fullImage;
        }
        Mat mat = new Mat();
        Utils.bitmapToMat(this.fullImage, mat);
        // Calculate the adjusted stroke width based on the scaling factor
        int strokeWidth = Math.max(strawberry.getBoundingBox().width, strawberry.getBoundingBox().height) / 10;
        Scalar color = new Scalar(255, 0, 255, 255); // Purple
        Imgproc.rectangle(mat, strawberry.getBoundingBox(), color, strokeWidth);
        Bitmap segmentBitmap = Bitmap.createBitmap(this.fullImage.getWidth(), this.fullImage.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, segmentBitmap);
        return segmentBitmap;
    }

    /**
     * Generates a random UUID.
     *
     * @return a UUID.
     */
    private UUID generateUUID() {
        return UUID.randomUUID();
    }

    /**
     * Sends all attributes and images along with the user message to the system maintainer.
     *
     * @param message the user message to send.
     */
    public void sendFeedback(Context context, String message) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        String subject;
        String body;
        if (this.strawberry != null) {
            // Feedback on particular segment with its values
            subject = "Feedback on models";
            body = String.format(
                    "%s\n\n" +
                    "Ripeness: %f\n" +
                    "Brix: %f\n" +
                    "Marketable: %s\n" +
                    "Roundness: %f\n" +
                    "Smoothness: %f",
                    message,
                    this.strawberry.getRipeness(),
                    this.strawberry.getBrix(),
                    this.strawberry.getMarketabilityAsString(),
                    this.strawberry.getRoundness(),
                    this.strawberry.getSmoothness()
            );
        } else {
            // Feedback on segmentation as a whole
            subject = "Feedback on segmentation";
            body = message;
        }
        UUID uuid = this.generateUUID();
        subject += " (" + uuid.toString() + ")";

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { SYSTEM_MAINTAINER_EMAIL });
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        emailIntent.setType("image/png");
        ArrayList<Uri> uris = new ArrayList<>();
        Bitmap highlightedBitmap = this.createHighlightedSegmentBitmap();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = fmt.format(dateProvider.getDate());
        Optional<Uri> fullUri = uriUtils.convertBitmapToTempUri(context, timestamp + "_full", highlightedBitmap);
        Optional<Uri> jsonFileUri = JsonExporter.getJsonUri(context);

        fullUri.ifPresent(uri -> Log.d("bitmap uri", uri.toString()));
        fullUri.ifPresent(uris::add);
        if (this.strawberry != null) {
            Optional<Uri> segmentUri = uriUtils.convertBitmapToTempUri(context, timestamp, this.strawberry.getBitmap());
            segmentUri.ifPresent(uris::add);
        }
        jsonFileUri.ifPresent(uris::add);
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(emailIntent);
    }
}
