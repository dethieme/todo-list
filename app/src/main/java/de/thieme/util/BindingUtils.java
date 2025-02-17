package de.thieme.util;

import android.icu.text.SimpleDateFormat;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import java.util.Date;
import java.util.Locale;

public class BindingUtils {

    @BindingAdapter("formattedDate")
    public static void setFormattedDate(TextView textView, long timestamp) {
        if (timestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            textView.setText(sdf.format(new Date(timestamp)));
        } else {
            textView.setText("");
        }
    }
}
