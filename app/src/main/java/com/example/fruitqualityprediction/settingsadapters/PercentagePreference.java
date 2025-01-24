package com.example.fruitqualityprediction.settingsadapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import com.example.fruitqualityprediction.R;

/**
 * Extends the Preference class to allow for percentages.
 */
public class PercentagePreference extends Preference {

    /**
     * Constructor for a custom list preference that enables showing of colors next to the
     * percentage targets.
     *
     * @param context the context in which the preference is displayed.
     * @param attrs   attributes of the preference (list items).
     */
    public PercentagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets the value of the preference and updates the summary accordingly.
     * @param selectedPercentage the selected percentage value to be saved.
     */
    public void setValue(String selectedPercentage) {
        // Save the selected value to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getKey(), selectedPercentage);
        editor.apply();

        // Update the summary to reflect the selected value
        setSummary(getSummaryFromPercentage(selectedPercentage));
    }

    /**
     * Gets the summary in form of a percentage from the currently selected option.
     *
     * @param selectedPercentage selected preference percentage.
     *
     * @return summary in string form.
     */
    public String getSummaryFromPercentage(String selectedPercentage) {
        String[] percentageOptions = getContext().getResources().getStringArray(R.array.percentage_options);
        for (String option : percentageOptions) {
            String[] parts = option.split("\\|");
            if (parts.length == 2 && parts[0].equals(selectedPercentage)) {
                return parts[0];
            }
        }
        return null; // Return null if no matching percentage option found
    }

    /**
     * Adds an onClick listener that changes the value of the selected option whenever a change
     * occurs in the dialog for changing the percentage.
     */
    @Override
    protected void onClick() {
        // Display the custom AlertDialog with the list of percentages and color squares
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getTitle());

        String[] percentageOptions = getContext().getResources().getStringArray(R.array.percentage_options);
        Drawable[] colorDrawables = new Drawable[percentageOptions.length];

        for (int i = 0; i < percentageOptions.length; i++) {
            String option = percentageOptions[i];
            String[] parts = option.split("\\|");
            if (parts.length == 2) {
                String colorValue = parts[1];
                int color = Color.parseColor(colorValue);
                colorDrawables[i] = new ColorDrawable(color);
            }
        }

        builder.setAdapter(new ColorDrawableAdapter(getContext(), colorDrawables, percentageOptions), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedPercentage = percentageOptions[which].split("\\|")[0];
                setValue(selectedPercentage);
            }
        });

        builder.show();
    }

    /**
     * Creates an adapter for the custom preference which enables colors to be displayed next to the
     * chosen percentage for the setting.
     */
    private static class ColorDrawableAdapter extends ArrayAdapter<String> {
        private final Drawable[] drawables;
        private final String[] percentageOptions;

        /**
         * Constructor for the color adapter.
         *
         * @param context           the context in which the adapter operates.
         * @param drawables         list of drawable objects (colors).
         * @param percentageOptions the percentage option value passed from the settings.
         */
        ColorDrawableAdapter(Context context, Drawable[] drawables, String[] percentageOptions) {
            super(context, 0, percentageOptions);
            this.drawables = drawables;
            this.percentageOptions = percentageOptions;
        }

        /**
         * Returns a custom View for each item in the adapter's data set to be displayed in a
         * ListView or Spinner.
         *
         * @param position    the position of the item within the adapter's data set of the item
         *                    whose view we want.
         * @param convertView the old view to reuse, if possible. If it is not possible to convert
         *                    this view to display the correct data, this method can create a new
         *                    view.
         * @param parent      the parent that this view will eventually be attached to.
         *
         * @return the updated view.
         */
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_color_drawable, parent, false);
            }

            Drawable drawable = drawables[position];
            String option = percentageOptions[position];
            String[] parts = option.split("\\|");
            String percentage = parts[0];

            ImageView imageView = convertView.findViewById(R.id.image_view);
            imageView.setImageDrawable(drawable);

            // Adjust the size of the ImageView
            int desiredSizeInDp = 60; // Change the desired size as desired
            int desiredSizeInPx = (int) (desiredSizeInDp * getContext().getResources().getDisplayMetrics().density);
            imageView.getLayoutParams().width = desiredSizeInPx;
            imageView.getLayoutParams().height = desiredSizeInPx;

            TextView textView = convertView.findViewById(R.id.text_view);
            textView.setText(percentage);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40); // Change the text size as desired

            return convertView;
        }
    }
}