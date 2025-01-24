package com.example.fruitqualityprediction.ui;


import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.not;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.fruitqualityprediction.MainActivity;
import com.example.fruitqualityprediction.R;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CameraFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Tests whether taking a photo correctly displays/hides all components.
     */
    @Test
    @Ignore
    public void takePhotoShouldDisplayPreview() {
        Espresso.onView(ViewMatchers.withId(R.id.imageCaptureButton))
                .perform(click());

        Espresso.onView(withId(R.id.imageView))
                .check(matches(isDisplayed()));

        Espresso.onView(withId(R.id.closePreviewButton))
                .check(matches(isDisplayed()));

        Espresso.onView(withId(R.id.processPreviewButton))
                .check(matches(isDisplayed()));

        Espresso.onView(withId(R.id.viewFinder))
                .check(matches(not(isDisplayed())));

        Espresso.onView(withId(R.id.imageCaptureButton))
                .check(matches(not(isDisplayed())));
    }

    /**
     * Test whether all components are displayed/hidden correctly upon closing the preview.
     */
    @Test
    @Ignore
    public void closingPreview() {
        Espresso.onView(withId(R.id.imageCaptureButton))
                .perform(click());

        Espresso.onView(withId(R.id.closePreviewButton))
                .perform(click());

        Espresso.onView(withId(R.id.closePreviewButton))
                .check(matches(not(isDisplayed())));

        Espresso.onView(withId(R.id.imageView))
                .check(matches(not(isDisplayed())));

        Espresso.onView(withId(R.id.viewFinder))
                .check(matches(isDisplayed()));

        Espresso.onView(withId(R.id.imageCaptureButton))
                .check(matches(isDisplayed()));

        Espresso.onView(withId(R.id.savePreviewButton))
                .check(matches(not(isDisplayed())));

        Espresso.onView(withId(R.id.processPreviewButton))
                .check(matches(not(isDisplayed())));
    }

    /**
     * See whether clicking the process image button twice causes the program to crash.
     * Correct behavior does not crash the program when clicking twice.
     * @throws InterruptedException
     */
//    @Test
//    public void captureImageProcessTwice() throws InterruptedException {
//
//        Thread.sleep(3000);
//
//        Espresso.onView(withId(R.id.imageCaptureButton))
//                .perform(click());
//        ViewActions.closeSoftKeyboard();
//
//
//        Espresso.onIdle();
//
//        Espresso.onView(withId(R.id.processPreviewButton))
//                .check(ViewAssertions.matches(isDisplayed()));
//
//        Thread.sleep(3000);
//
//        Espresso.onView(withId(R.id.processPreviewButton))
//                .perform(click());
//
//        Espresso.onIdle();
//
//        Thread.sleep(1000);
//
//        Espresso.onView(withId(R.id.processPreviewButton))
//                .perform(click());
//
//        Espresso.onIdle();
//
//        Espresso.onView(withId(R.id.closePreviewButton))
//                .check(ViewAssertions.matches(isDisplayed()));
//
//        Espresso.onView(withId(R.id.imageView))
//                .check(ViewAssertions.matches(isDisplayed()));
//
//        Espresso.onView(withId(R.id.viewFinder))
//                .check(ViewAssertions.matches(not(isDisplayed())));
//
//        Espresso.onView(withId(R.id.imageCaptureButton))
//                .check(ViewAssertions.matches(not(isDisplayed())));
//
//        Espresso.onView(withId(R.id.savePreviewButton))
//                .check(ViewAssertions.matches(isDisplayed()));
//
//        Espresso.onView(withId(R.id.processPreviewButton))
//                .check(ViewAssertions.matches(isDisplayed()));
//    }
//
    /**
     * Checks whether the app does not crash when taking a picture, closing it and retaking another
     * picture.
     * @throws InterruptedException
     */
    @Test
    @Ignore
    public void captureImageRetake() throws InterruptedException {

        Espresso.onView(withId(R.id.imageCaptureButton))
                .perform(click());
        ViewActions.closeSoftKeyboard();


        Espresso.onIdle();

        Espresso.onView(withId(R.id.processPreviewButton))
                .check(ViewAssertions.matches(isDisplayed()));

        Thread.sleep(2000);

        Espresso.onIdle();

        Espresso.onView(withId(R.id.closePreviewButton))
                .perform(click());

        Thread.sleep(1000);

        Espresso.onView(withId(R.id.imageCaptureButton))
                .perform(click());

        Thread.sleep(1000);

        Espresso.onView(withId(R.id.processPreviewButton))
                .perform(click());

        Espresso.onView(withId(R.id.closePreviewButton))
                .check(ViewAssertions.matches(isDisplayed()));

        Espresso.onView(withId(R.id.imageView))
                .check(ViewAssertions.matches(isDisplayed()));

        Espresso.onView(withId(R.id.viewFinder))
                .check(ViewAssertions.matches(not(isDisplayed())));

        Espresso.onView(withId(R.id.imageCaptureButton))
                .check(ViewAssertions.matches(not(isDisplayed())));

        Espresso.onView(withId(R.id.savePreviewButton))
                .check(ViewAssertions.matches(isDisplayed()));

        Espresso.onView(withId(R.id.processPreviewButton))
                .check(ViewAssertions.matches(not(isDisplayed())));
    }

    /**
     * Tests whether the application correctly modifies the save button.
     * @throws InterruptedException
     */
    @Test
    @Ignore
    public void captureImageSavePreview() throws InterruptedException {

        Thread.sleep(3000);

        Espresso.onView(withId(R.id.imageCaptureButton))
                .perform(click());
        ViewActions.closeSoftKeyboard();

        Espresso.onIdle();

        Espresso.onView(withId(R.id.processPreviewButton))
                .check(ViewAssertions.matches(isDisplayed()));

        Espresso.onIdle();

        Thread.sleep(2000);

        Espresso.onView(withId(R.id.savePreviewButton))
                .perform(click());

        Espresso.onView(withId(R.id.closePreviewButton))
                .check(ViewAssertions.matches(isDisplayed()));

        Espresso.onView(withId(R.id.imageView))
                .check(ViewAssertions.matches(isDisplayed()));

        Espresso.onView(withId(R.id.viewFinder))
                .check(ViewAssertions.matches(not(isDisplayed())));

        Espresso.onView(withId(R.id.imageCaptureButton))
                .check(ViewAssertions.matches(not(isDisplayed())));

        Espresso.onView(withId(R.id.savePreviewButton))
                .check(ViewAssertions.matches(withText("Saved")));
    }

//    @Test
//    public void captureImageOrientationChange() {
//        // Perform the click action to capture an image
//        Espresso.onView(withId(R.id.imageCaptureButton))
//                .perform(click());
//
//        // Capture the current activity instance
//        Activity activity = getActivity(activityScenarioRule);
//
//        // Simulate an orientation change
//        rotateScreen(activity);
//
//        // Check that the image preview is still displayed
//        Espresso.onView(withId(R.id.imageView))
//                .check(matches(isDisplayed()));
//
//        // Check that other UI elements are still displayed correctly
////        Espresso.onView(withId(R.id.closePreviewButton))
////                .check(matches(isDisplayed()));
////        Espresso.onView(withId(R.id.viewFinder))
////                .check(matches(not(isDisplayed())));
////        Espresso.onView(withId(R.id.imageCaptureButton))
////                .check(matches(not(isDisplayed())));
////        Espresso.onView(withId(R.id.processPreviewButton))
////                .check(matches(isDisplayed()));
//    }
//
//    private void rotateScreen(Activity activity) {
//        int orientation = activity.getResources().getConfiguration().orientation;
//        activity.setRequestedOrientation(
//                orientation == Configuration.ORIENTATION_PORTRAIT
//                        ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//                        : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        );
//    }
//
//    private Activity getActivity(ActivityScenarioRule<?> activityScenarioRule) {
//        final AtomicReference<Activity> activityRef = new AtomicReference<>();
//        activityScenarioRule.getScenario().onActivity(activityRef::set);
//        return activityRef.get();
//    }

}