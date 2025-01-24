package com.example.fruitqualityprediction.feedback;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opencv.core.Rect;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.AlertDialog;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.fruitqualityprediction.UriUtils;
import com.example.fruitqualityprediction.feedback.FeedbackSender;
import com.example.fruitqualityprediction.MainActivity;
import com.example.fruitqualityprediction.providers.DateProvider;
import com.example.fruitqualityprediction.sbprocessing.segmentation.StrawberrySegment;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class FeedbackSenderTest {
    @Mock
    private Context context;

    @Mock
    private UriUtils mockUriUtils;

    @Mock
    private PackageManager packageManager;

    @Mock
    private DateProvider dateProvider;

    @Captor
    private ArgumentCaptor<Intent> intentCaptor;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);


    @Before
    public void setup() {
        when(context.getPackageManager()).thenReturn(packageManager);
        when(packageManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(Collections.emptyList());
    }

    @Test
    public void feedbackProvider_InitializationWithoutStrawberry_Successful() {
        Bitmap fullImage = createMockBitmap();
        FeedbackSender feedbackSender = new FeedbackSender(fullImage, mockUriUtils, new DateProvider());
        assertNotNull(feedbackSender);
    }

    @Test
    public void feedbackProvider_InitializationWithStrawberry_Successful() {
        Bitmap fullImage = createMockBitmap();
        StrawberrySegment strawberry = new StrawberrySegment(createMockBoundingBox(0,0,20,20));
        FeedbackSender feedbackSender = new FeedbackSender(fullImage, mockUriUtils, new DateProvider(), strawberry);
        assertNotNull(feedbackSender);
    }


    @Test
    public void feedbackProvider_SendFeedback_WithoutStrawberry_Successful() {
        Bitmap fullImage = createMockBitmap();
        FeedbackSender feedbackSender = new FeedbackSender(fullImage, mockUriUtils, dateProvider);
        Date currentTime = new Date();
        when(dateProvider.getDate()).thenReturn(currentTime);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = fmt.format(currentTime);
        when(mockUriUtils.convertBitmapToTempUri(context, timestamp, fullImage)).thenReturn(Optional.of(new Uri.Builder().build()));
        feedbackSender.sendFeedback(context, "Test feedback message");

        verify(context).startActivity(intentCaptor.capture());

        Intent capturedIntent = intentCaptor.getValue();
        assertEquals(Intent.ACTION_SEND_MULTIPLE, capturedIntent.getAction());
        assertEquals("image/png", capturedIntent.getType());
        assertArrayEquals(new String[]{FeedbackSender.SYSTEM_MAINTAINER_EMAIL}, capturedIntent.getStringArrayExtra(Intent.EXTRA_EMAIL));
        String expectedSubject = "Feedback on segmentation";
        String actualSubject = capturedIntent.getStringExtra(Intent.EXTRA_SUBJECT);
        assertTrue(actualSubject.startsWith(expectedSubject));
        assertEquals("Test feedback message", capturedIntent.getStringExtra(Intent.EXTRA_TEXT));
        int expectedFlag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        int actualFlags = capturedIntent.getFlags();
        boolean hasFlag = (actualFlags & expectedFlag) != 0;
        assertTrue(hasFlag);
    }

    @Test
    public void feedbackProvider_SendFeedback_WithStrawberry_Successful() {
        // Create mock objects
        Bitmap fullImage = createMockBitmap();
        StrawberrySegment strawberry = new StrawberrySegment(createMockBoundingBox(0, 0, 20, 20));
        strawberry.setRipeness(1.0);
        strawberry.setBrix(1f);
        strawberry.setFirmness(1f);
        strawberry.setRoundness(1f);
        strawberry.setSmoothness(1.0);
        strawberry.setMarketability(true);
        strawberry.setBitmap(createMockBitmap());

        // Create the feedbackProvider instance
        FeedbackSender feedbackSender = new FeedbackSender(fullImage, mockUriUtils, new DateProvider(), strawberry);
        feedbackSender.sendFeedback(context, "Test feedback message");

        // Capture the intent passed to startActivity()
        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(context).startActivity(intentCaptor.capture());

        // Verify that the captured intent is created and populated correctly
        Intent capturedIntent = intentCaptor.getValue();
        assertNotNull(capturedIntent);
        assertEquals(Intent.ACTION_SEND_MULTIPLE, capturedIntent.getAction());
        assertEquals("image/png", capturedIntent.getType());
        assertArrayEquals(new String[]{FeedbackSender.SYSTEM_MAINTAINER_EMAIL}, capturedIntent.getStringArrayExtra(Intent.EXTRA_EMAIL));

        String subject = capturedIntent.getStringExtra(Intent.EXTRA_SUBJECT);
        assertNotNull(subject);
        assertTrue(subject.startsWith("Feedback on models"));

        String body = capturedIntent.getStringExtra(Intent.EXTRA_TEXT);
        assertNotNull(body);
        assertTrue(body.contains("Test feedback message"));
        assertTrue(body.contains("Ripeness:"));
        assertTrue(body.contains("Brix:"));
        assertTrue(body.contains("Marketable:"));
        assertTrue(body.contains("Roundness:"));
        assertTrue(body.contains("Smoothness:"));
    }

    @Test
    public void process_ShouldShowAlertDialogWithTitle() {
        AlertDialog.Builder mockBuilder = mock(AlertDialog.Builder.class);
        FeedbackSender feedbackSender = new FeedbackSender(createMockBitmap(), mockUriUtils, new DateProvider(), mockBuilder);

        Context context = ApplicationProvider.getApplicationContext();
        feedbackSender.process(context);

        // Verify that the setTitle method was called on the mock builder instance
        verify(mockBuilder).setTitle("Send Feedback");

        // Verify that the setPositiveButton method was called with the expected text and listener
        verify(mockBuilder).setPositiveButton(eq("Send"), any(DialogInterface.OnClickListener.class));

        // Verify that the setNegativeButton method was called with the expected text and listener
        verify(mockBuilder).setNegativeButton(eq("Cancel"), any(DialogInterface.OnClickListener.class));

        // Verify that the show method was called on the mock builder instance
        verify(mockBuilder).show();
    }


//    @Test
//    @UiThreadTest
//    public void process_ShouldSetInputTypeToMultiLine() {
//        EditText mockEditText = mock(EditText.class);
//        AlertDialog.Builder builder = mock(AlertDialog.Builder.class);
//
//        FeedbackProvider feedbackProvider = new FeedbackProvider(createMockBitmap());
//
//        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
//        scenario.onActivity(activity -> {
//            feedbackProvider.process(activity);
//
//            verify(mockEditText).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
//        });
//    }

//    @Test
//    public void process_ShouldSendFeedbackOnPositiveButtonClick() {
//        AlertDialog.Builder builder = mock(AlertDialog.Builder.class);
//        AlertDialog alertDialog = mock(AlertDialog.class);
//        EditText mockEditText = mock(EditText.class);
//        when(builder.setView(mockEditText)).thenReturn(builder);
//        when(builder.setPositiveButton("Send", null)).thenReturn(builder);
//        when(builder.setNegativeButton("Cancel", null)).thenReturn(builder);
//        when(builder.show()).thenReturn(alertDialog);
//        FeedbackProvider feedbackProvider = new FeedbackProvider(createMockBitmap());
//
//        feedbackProvider.process(ApplicationProvider.getApplicationContext());
//
//        ArgumentCaptor<DialogInterface.OnClickListener> clickListenerCaptor =
//                ArgumentCaptor.forClass(DialogInterface.OnClickListener.class);
//        verify(builder).setPositiveButton("Send", clickListenerCaptor.capture());
//
//        // Simulate button click and verify sendFeedback() is called
//        clickListenerCaptor.getValue().onClick(alertDialog, DialogInterface.BUTTON_POSITIVE);
//        verify(feedbackProvider).sendFeedback(ApplicationProvider.getApplicationContext(), "");
//    }

    private Rect createMockBoundingBox(int x, int y, int width, int height) {
        Rect boundingBox = new Rect(x, y, x + width, y + height);
        return boundingBox;
    }

    /**
     * Used to create a "fake" Bitmap that can be used in tests.
     * @return mock Bitmap
     */
    private Bitmap createMockBitmap() {
        int width = 1000;
        int height = 1000;
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        return bitmap;
    }
}