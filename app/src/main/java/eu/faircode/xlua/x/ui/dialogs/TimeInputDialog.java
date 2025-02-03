package eu.faircode.xlua.x.ui.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;

public class TimeInputDialog extends AppCompatDialogFragment {
    private static final String TAG = "XLua.TimeInputDialog";
    private static final String[] TIME_UNITS = {"NANO", "SECOND", "MINUTE", "HOUR", "DAY"};

    private Context context;
    private IDialogTimeFinish timeFinishListener;

    public interface IDialogTimeFinish {
        void onTimeFinish(long value, String symbol);
    }

    public static TimeInputDialog create() {
        return new TimeInputDialog();
    }

    public TimeInputDialog setTimeFinishListener(IDialogTimeFinish listener) {
        this.timeFinishListener = listener;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.time_input_dialog, null);

        EditText etTimeValue = view.findViewById(R.id.etTimeValue);
        Spinner spinnerTimeUnit = view.findViewById(R.id.spinnerTimeUnit);

        // Setup spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                TIME_UNITS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeUnit.setAdapter(adapter);

        // Build dialog
        builder.setView(view)
                .setTitle(R.string.title_time_input)
                .setPositiveButton(R.string.option_ok, null)
                .setNegativeButton(R.string.option_cancel, null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            etTimeValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!Str.isEmpty(s.toString()));
                }
            });

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String valueStr = etTimeValue.getText().toString();
                long value = Str.tryParseLong(valueStr);
                String symbol = TIME_UNITS[spinnerTimeUnit.getSelectedItemPosition()];

                if (timeFinishListener != null) {
                    timeFinishListener.onTimeFinish(value, symbol);
                }
                dialog.dismiss();
            });
        });

        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}