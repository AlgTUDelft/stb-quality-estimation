package com.example.fruitqualityprediction.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.fruitqualityprediction.R;
import com.example.fruitqualityprediction.ShareIntentBuilder;
import com.example.fruitqualityprediction.UriUtils;
import com.example.fruitqualityprediction.feedback.FeedbackSender;
import com.example.fruitqualityprediction.providers.ChartGeneratorProvider;
import com.example.fruitqualityprediction.feedback.JsonExporter;
import com.example.fruitqualityprediction.providers.DateProvider;
import com.example.fruitqualityprediction.providers.PreferenceProvider;
import com.example.fruitqualityprediction.sbprocessing.ImageProcessor;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This fragment represents the camera UI where users take pictures, process, save, export them, and more.
 */
public class CameraFragment extends Fragment {

    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"; // The format in which to export saved images.

    private final ChartGeneratorProvider chartGeneratorProvider; // Generates sigmoid charts.
    private final PreferenceProvider preferenceProvider;
    private final UriUtils uriUtils; // Provides URI helper methods.

    private transient View view; // The view of the fragment, containing all subcomponents.
    private transient ImageCapture imageCapture; // The process that actually captures images.
    private transient ExecutorService cameraExecutor; // Manages the camera system.
    private transient String imageName;



    /**
     * Initializes the chart generator provider.
     *
     * @param chartGeneratorProvider The chart generator provider.
     * @param preferenceProvider The preference provider.
     */
    public CameraFragment(ChartGeneratorProvider chartGeneratorProvider, PreferenceProvider preferenceProvider, UriUtils uriUtils) {
        this.chartGeneratorProvider = chartGeneratorProvider;
        this.preferenceProvider = preferenceProvider;
        this.uriUtils = uriUtils;
    }

    /**
     * Whether the user has already saved the preview in the current context.
     */
    private boolean hasSavedPreview = false;

    /**
     * Called when the view is being created, and starts the camera.
     *
     * @param inflater           the LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          if non-null, this is the parent view that the fragment's
     *                           UI should be attached to. The fragment should not add the view
     *                           itself, but this can be used to generate the LayoutParams of the
     *                           view.
     * @param savedInstanceState if non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     *
     * @return The newly created view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_camera, container, false);
        startCamera();
        return this.view;
    }

    /**
     * Called whenever the view is created. This hooks up all button and camera events.
     *
     * @param view               the View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState if non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Bind on-click events
        view.findViewById(R.id.imageCaptureButton).setOnClickListener((e) -> takePhoto());
        view.findViewById(R.id.closePreviewButton).setOnClickListener((e) -> closePreview());
        view.findViewById(R.id.savePreviewButton).setOnClickListener((e) -> savePreview());
        view.findViewById(R.id.processPreviewButton).setOnClickListener((e) -> processPreview());
        view.findViewById(R.id.shareButton).setOnClickListener((e) -> sharePreview());
        view.findViewById(R.id.feedbackButton).setOnClickListener((e) -> showFeedbackDialog());
        // Spawn new camera management thread
        cameraExecutor = Executors.newSingleThreadExecutor();

        // If we started this fragment with an argument, we may have uploaded an image
        // instead of taken our own picture.
        Bundle args = getArguments();
        if (args != null) {
            Bitmap inputImage = args.getParcelable("image");
            if (inputImage != null) {
                uploadImage(inputImage, null);
            }
        }
        setArguments(null);

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Binds the camera provider to this application.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getActivity());
        cameraProviderFuture.addListener(() -> {
            try{
                // Fetch a camera provider
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                // Set the target surface for our preview capture
                preview.setSurfaceProvider(((PreviewView) view.findViewById(R.id.viewFinder)).getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                imageCapture = new ImageCapture.Builder().build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            }
            catch (Exception ex) {
                Log.e("CAMERA", "Execution exception", ex);
            }
        }, ContextCompat.getMainExecutor(getActivity()));
    }

    /**
     * Supplies the content of the camera preview to showImage.
     */
    private void takePhoto() {
        showImage(((PreviewView) view.findViewById(R.id.viewFinder)).getBitmap(), true);
    }

    /**
     * Returns the bitmap currently being shown in the preview.
     *
     * @return the currently shown bitmap.
     */
    private Bitmap getPreviewBitmap() {
        ImageView imageView = view.findViewById(R.id.imageView);
        return ((BitmapDrawable) imageView.getDrawable()).getBitmap();
    }

    /**
     * Displays the supplied image as the preview for what will be processed.
     *
     * @param image           the image to be processed
     * @param showSavePreview whether the Save Preview button should be visible. This is false when uploading from the photo gallery
     */
    private void showImage(Bitmap image, boolean showSavePreview) {
        view.findViewById(R.id.imageCaptureButton).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
        ((ImageView) view.findViewById(R.id.imageView)).setImageBitmap(image);
        view.findViewById(R.id.viewFinder).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.closePreviewButton).setVisibility(View.VISIBLE);
        view.findViewById(R.id.savePreviewButton).setVisibility(showSavePreview ? View.VISIBLE : View.INVISIBLE);
        view.findViewById(R.id.processPreviewButton).setVisibility(View.VISIBLE);
    }

    /**
     * Closes the preview if one is being shown, and shows the uploaded image.
     *
     * @param image the image to show before processing.
     */
    public void uploadImage(Bitmap image, String imageName) {
        closePreview();
        this.imageName = imageName;
        showImage(image, false);
    }

    /**
     * Saves the content of the preview surface to the photo gallery.
     */
    private void savePreview() {
        if (hasSavedPreview) {
            return;
        }
        setSaveButtonUsable(false);
        Bitmap bmp = getPreviewBitmap();
        // Multi-thread saving the bitmap as PNG compression can take some time
        new Thread(() -> {
            try {
                // Save the preview bitmap to the photo gallery.
                uriUtils.saveBitmapToGallery(getContext(), bmp, Bitmap.CompressFormat.PNG, "image/png", new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()));
            }
            catch (IOException ex) {
                Log.e("SAVE PREVIEW", "Failed to save preview to file", ex);
            }
        }).start();
    }

    /**
     * Updates the text and icon of the save button to indicate whether it can be used.
     *
     * @param usable whether the button is usable.
     */
    private void setSaveButtonUsable(boolean usable) {
        Button saveButton = view.findViewById(R.id.savePreviewButton);
        String text = "Saved";
        int icon = R.drawable.saved_icon;
        hasSavedPreview = !usable;
        if (usable) {
            icon = R.drawable.save_icon;
            text = "Save";
        }
        saveButton.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        saveButton.setText(text);
    }

    /**
     * Closes the preview and shows the camera again.
     */
    private void closePreview() {
        if (view.findViewById(R.id.closePreviewButton).getVisibility() != View.VISIBLE) {
            return;
        }
        view.findViewById(R.id.closePreviewButton).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.imageView).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.viewFinder).setVisibility(View.VISIBLE);
        view.findViewById(R.id.imageCaptureButton).setVisibility(View.VISIBLE);
        view.findViewById(R.id.savePreviewButton).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.processPreviewButton).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.shareButton).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.feedbackButton).setVisibility(View.INVISIBLE);
        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setImageDrawable(null);
        setSaveButtonUsable(true);
    }

    /**
     * Processes the image.
     */
    private void processPreview() {
        ImageView iv = view.findViewById(R.id.imageView);
        view.findViewById(R.id.processPreviewButton).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.shareButton).setVisibility(View.VISIBLE);
        view.findViewById(R.id.feedbackButton).setVisibility(View.VISIBLE);
        view.findViewById(R.id.savePreviewButton).setVisibility(View.VISIBLE); // Save button should be visible after processing, even for uploaded images
        setSaveButtonUsable(true);
        ImageProcessor imageProcessor = new ImageProcessor(getPreviewBitmap(), getContext(), preferenceProvider, imageName);
        imageProcessor.setChartGenerator(chartGeneratorProvider.getGenerator());
        imageProcessor.process(iv);
        JsonExporter jsonExporter = new JsonExporter(imageProcessor.getStrawberrySegments());
        jsonExporter.createJson(getContext());
    }

    /**
     * Shows native share dialog for the processed image. This exports the image with the bounding boxes;
     * not the actual quality information itself.
     */
    private void sharePreview() {
        startActivity(Intent.createChooser(
                new ShareIntentBuilder(getContext())
                        .create(getPreviewBitmap()),
                "Share Image"));
    }

    /**
     * Creates a prompt for the user to provide feedback to the system maintainer about the segmentation.
     */
    private void showFeedbackDialog() {
        FeedbackSender fbProvider = new FeedbackSender(getPreviewBitmap(), uriUtils, new DateProvider());
        fbProvider.process(getContext());
    }

    /**
     * Shuts down the camera when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
    }
}
