package com.example.fruitqualityprediction.fragments;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.fruitqualityprediction.R;
import com.example.fruitqualityprediction.providers.ChartGeneratorProvider;
import com.example.fruitqualityprediction.providers.PreferenceProvider;
import com.example.fruitqualityprediction.sbprocessing.ImageProcessor;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;
import com.google.common.util.concurrent.ListenableFuture;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A class that controls the AR tab
 */
public class ARFragment extends Fragment {

    private final ChartGeneratorProvider chartGeneratorProvider;
    private final PreferenceProvider preferenceProvider;

    private transient ImageView imageView; // Represents the view that displays the bounding boxes and annotations.
    private transient List<StrawberrySegment> strawberrySegments; // Contains the segment data from the last frame.
    private boolean segmentationInProgress; // Represents whether segmentation is currently happening on the background thread.

    /**
     * Initializes the chart generator provider.
     *
     * @param chartGeneratorProvider The chart generator provider.
     * @param preferenceProvider The preference provider
     */
    public ARFragment(ChartGeneratorProvider chartGeneratorProvider, PreferenceProvider preferenceProvider) {
        this.chartGeneratorProvider = chartGeneratorProvider;
        this.preferenceProvider = preferenceProvider;
    }

    /**
     * Set up the initial view.
     *
     * @param inflater           the LayoutInflater object that can be used to inflate
     *                           any views in the fragment.
     * @param container          if non-null, this is the parent view that the fragment's UI should
     *                           be attached to. The fragment should not add the view itself, but
     *                           this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState if non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     *
     * @return the view to display.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ar, container, false);
        this.imageView = view.findViewById(R.id.imageView);
        this.segmentationInProgress = false;
        return view;
    }

    /**
     * Start the camera when the fragment has loaded.
     */
    @Override
    public void onStart() {
        super.onStart();
        startCamera();
    }

    /**
     * Starts the camera.
     */
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getActivity());
        cameraProviderFuture.addListener(() -> {
            try{
                // Fetch a camera provider
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                Size resolution = new Size(imageView.getWidth(), imageView.getHeight());

                // Create an image analysis routine
                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setTargetResolution(resolution)
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(getContext()), imageProxy -> {
                    // Process to be evaluated whenever the image analysis routine is invoked

                    Image image = imageProxy.getImage();

                    JavaCamera2View.JavaCamera2Frame jc2f = new JavaCamera2View.JavaCamera2Frame(image);
                    Mat mat = jc2f.rgba();
                    Core.rotate(mat, mat, Core.ROTATE_90_CLOCKWISE);

                    // If we're not already performing segmentation in the background, start a new thread to do so
                    if (!segmentationInProgress) {
                        segmentationInProgress = true;
                        Mat clone = mat.clone();
                        // Spawn a new background thread
                        new Thread(() -> {
                            // Perform segmentation
                            ImageProcessor imageProcessor = new ImageProcessor(clone, getContext(), preferenceProvider, null);
                            imageProcessor.detectSegments();
                            strawberrySegments = imageProcessor.getStrawberrySegments();
                            segmentationInProgress = false;
                        }).start();
                    }

                    ImageProcessor imageProcessor = new ImageProcessor(mat, getContext(), preferenceProvider, null);
                    if (strawberrySegments != null && !strawberrySegments.isEmpty()) {
                        imageProcessor.setChartGenerator(chartGeneratorProvider.getGenerator());
                        imageProcessor.importBoundingBoxes(strawberrySegments);
                        imageProcessor.extractStrawberryImages();
                        imageProcessor.annotate(imageView);
                    } else {
                        // Display empty bitmap if no segmentation occurred, or if no segments were found.
                        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(mat, bitmap);
                        imageView.setImageBitmap(bitmap);
                    }

                    imageProxy.close();
                });

                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);
                }
                catch (Exception ex) {
                    Log.e("CAMERA", "Use case binding failed", ex);
                }
            }
            catch (InterruptedException ex) {
                Log.e("CAMERA", "Interrupted camera provider future", ex);
            }
            catch (ExecutionException ex) {
                Log.e("CAMERA", "Execution exception", ex);
            }
        }, ContextCompat.getMainExecutor(getActivity()));
    }
}