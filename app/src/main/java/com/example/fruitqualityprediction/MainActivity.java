package com.example.fruitqualityprediction;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import com.example.fruitqualityprediction.fragments.ARFragment;
import com.example.fruitqualityprediction.fragments.CameraFragment;
import com.example.fruitqualityprediction.fragments.SettingsFragment;
import com.example.fruitqualityprediction.providers.ChartGeneratorProvider;
import com.example.fruitqualityprediction.providers.PreferenceProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.IOException;
import org.opencv.android.OpenCVLoader;

/**
 * The main activity of the application and the starting point.
 */
public class MainActivity extends AppCompatActivity {

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("Loaded", "Success");
        } else {
            Log.d("Loaded", "Error");
        }
    }

    // The required permissions for the application to run. Will prompt for these permissions on startup.
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA
    };
    private static final String[] REQUIRED_PERMISSIONS_API_BELOW_29 = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_CODE_PERMISSIONS = 10; // Expected status code.
    private static final String PREF_FIRST_RUN = "firstRun"; // The preference name of the "firstRun" setting.

    private transient ARFragment arFrag; // The AR UI fragment.
    private transient CameraFragment cameraFrag; // The Camera UI fragment.
    private transient SettingsFragment settingsFrag; // The Settings UI fragment.
    private transient Fragment currentFrag; // The currently active UI fragment.

    private UriUtils uriUtils; // Provides URI helper methods.
    private PreferenceProvider preferenceProvider;
    private ChartGeneratorProvider chartGeneratorProvider;

    /**
     * Called before anything is on-screen. This sets up the UI fragments and ensures we have the correct permissions.
     *
     * @param savedInstanceState if the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it
     *                           most recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.preferenceProvider = new PreferenceProvider(prefs);
        boolean isFirstRun = prefs.getBoolean(PREF_FIRST_RUN, true);

        setContentView(R.layout.activity_main);


        this.chartGeneratorProvider = new ChartGeneratorProvider(this.preferenceProvider.getVisualisationPreferences());
        this.uriUtils = new UriUtils();

        this.arFrag = new ARFragment(chartGeneratorProvider, preferenceProvider);
        this.cameraFrag = new CameraFragment(chartGeneratorProvider, preferenceProvider, this.uriUtils);
        this.settingsFrag = new SettingsFragment();

        if (isFirstRun) {
            setFragment(settingsFrag);
            setFragment(cameraFrag);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(PREF_FIRST_RUN, false);
            editor.apply();
        }

        BottomNavigationView navBar = findViewById(R.id.bottomNavBar);
        navBar.setOnItemSelectedListener(it -> {
            int itemId = it.getItemId();

            if (itemId == R.id.ar) {
                setFragment(arFrag);
            } else if (itemId == R.id.upload) {
                setFragment(cameraFrag);
                chooseImage();
            } else if (itemId == R.id.camera) {
                setFragment(cameraFrag);
            } else if (itemId == R.id.settings) {
                setFragment(settingsFrag);
            }

            return true;
        });

        if (wereAllPermissionsGranted()) {
            setCameraChecked(true);
            setFragment(cameraFrag);
        }
        else{
            if (Build.VERSION.SDK_INT < 29) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS_API_BELOW_29, REQUEST_CODE_PERMISSIONS);
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        }
    }

    /**
     * Sets the camera fragment icon at the bottom navigation bar as checked or not.
     *
     * @param checked whether the icon should be shown as selected.
     */
    private void setCameraChecked(boolean checked) {
        BottomNavigationView navBar = findViewById(R.id.bottomNavBar);
        navBar.getMenu().findItem(R.id.camera).setChecked(checked);
    }

    /**
     * Opens the photo gallery to choose a picture for processing.
     */
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imageSelector.launch(intent);
    }

    /**
     * Decodes the image and previews it before processing.
     */
    @SuppressWarnings("PMD.FieldDeclarationsShouldBeAtStartOfClass") // Logic makes more sense near methods related to same functionality
    private final transient ActivityResultLauncher<Intent> imageSelector = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                setCameraChecked(true);
                if (result.getResultCode() != Activity.RESULT_OK) {
                    return;
                }
                Intent data = result.getData();
                if (data == null) {
                    return;
                }
                Uri imageUri = data.getData();
                if (imageUri == null) {
                    return;
                }
                String imageName = uriUtils.getImageNameFromUri(this, imageUri);
                try {
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    if (currentFrag.equals(cameraFrag)) {
                        cameraFrag.uploadImage(bmp, imageName);
                    }
                    else {
                        Bundle args = new Bundle();
                        args.putParcelable("image", bmp);
                        cameraFrag.setArguments(args);
                        setFragment(cameraFrag);
                    }
                }
                catch (IOException ex) {
                    Log.e("IMAGE SELECTOR", "Could not open local image", ex);
                    showToast("Failed to open image.");
                }
            }
    );

    /**
     * Sets a fragment as active.
     *
     * @param frag the fragment to make active.
     */
    private void setFragment(Fragment frag) {
        this.preferenceProvider.updateVisualisationPreferences();
        this.preferenceProvider.updateProcessingPreference();
        this.preferenceProvider.updateModelPreferences();
        this.chartGeneratorProvider.updateConfiguration(this.preferenceProvider.getVisualisationPreferences());

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.flContainer, frag);
        tx.commit();
        currentFrag = frag;
    }

    /**
     * Called after permission has been requested.
     *
     * @param requestCode  the request code passed.
     * @param permissions  the requested permissions. Never null.
     * @param grantResults the grant results for the corresponding permissions
     *                     which is either
     *                     {@link android.content.pm.PackageManager#PERMISSION_GRANTED} or
     *                     {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (wereAllPermissionsGranted()) {
                setCameraChecked(true);
                setFragment(cameraFrag);
            }
            else {
                showToast("Not all permissions were granted by the user.");
                finish();
            }
        }
    }

    /**
     * Determines whether all necessary permissions were granted.
     *
     * @return whether we have all necessary permissions.
     */
    private boolean wereAllPermissionsGranted() {
        String[] reqPermissions;
        if (Build.VERSION.SDK_INT < 29) {
            reqPermissions = REQUIRED_PERMISSIONS_API_BELOW_29;
        } else {
            reqPermissions = REQUIRED_PERMISSIONS;
        }
        for (String perm : reqPermissions) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), perm) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return !(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Shows a popup message at the bottom of the screen.
     *
     * @param text the text to display.
     */
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}