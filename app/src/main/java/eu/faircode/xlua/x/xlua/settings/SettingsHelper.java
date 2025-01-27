package eu.faircode.xlua.x.xlua.settings;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import eu.faircode.xlua.x.Str;

public class SettingsHelper {
    public static boolean matchesLabel(String data, TextView label) { return label != null && !TextUtils.isEmpty(data) && data.equalsIgnoreCase(getLabelText(label)); }


    public static boolean setLabelText(TextView tv, String text) {
        if(tv == null) return false;
        text = Str.ensureIsNotNullOrDefault(text, "");
        try {
            tv.setText(text);
            return true;
        }catch (Exception ignored) { return false; }
    }

    public static String getLabelText(TextView tv) { return getLabelText(tv, null); }
    public static String getLabelText(TextView tv, String defaultText) {
        if(tv == null) return defaultText;
        try {
            CharSequence c = tv.getText();
            return c == null ? defaultText : c.toString();
        }catch (Exception ignored) {
            return defaultText;
        }
    }

    public static boolean setInputTextText(TextInputEditText inputEditText, String text) { return setInputTextText(inputEditText, null, text); }
    public static boolean setInputTextText(TextInputEditText inputEditText, TextWatcher textWatcher, String text) {
        if(inputEditText == null) return false;
        text = Str.ensureIsNotNullOrDefault(text, "");
        try {
            if(textWatcher != null) inputEditText.removeTextChangedListener(textWatcher);
            inputEditText.setText(text);
            if(textWatcher != null) inputEditText.addTextChangedListener(textWatcher);
            return true;
        }catch (Exception ignored) { return false; }
    }

    public static String getInputTextText(TextInputEditText inputEditText) { return getInputTextText(inputEditText, null); }
    public static String getInputTextText(TextInputEditText inputEditText, String defaultText) {
        if(inputEditText == null) return defaultText;
        try {
            Editable e = inputEditText.getText();
            return e == null ? defaultText : e.toString();
        }catch (Exception ignored) {
            return defaultText;
        }
    }
}
