package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import eu.faircode.xlua.R;

public class ErrorDialog extends AppCompatDialogFragment {
    private Context context;
    private String errorTitle = "";
    private String errorMessage = "";
    private int iconResId = -1;

    public static ErrorDialog create() {
        return new ErrorDialog();
    }

    public ErrorDialog setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
        return this;
    }

    public ErrorDialog setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public ErrorDialog setIcon(int resourceId) {
        this.iconResId = resourceId;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.error_dialog, null);

        TextView tvErrorTitle = view.findViewById(R.id.tvErrorTitle);
        TextView tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        ImageView ivErrorIcon = view.findViewById(R.id.ivErrorIcon);

        tvErrorTitle.setText(errorTitle);
        tvErrorMessage.setText(errorMessage);

        if (iconResId != -1) {
            ivErrorIcon.setImageResource(iconResId);
        }

        builder.setView(view)
                .setPositiveButton(R.string.option_ok, null);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}