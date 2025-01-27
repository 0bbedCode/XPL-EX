package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import eu.faircode.xlua.R;

public class ConfirmDialog extends AppCompatDialogFragment {
    private Context context;
    private String message = "";
    private int imageResId = -1;
    private int delaySeconds = 0;
    private Runnable onConfirm;
    private Runnable onCancel;
    private Button positiveButton;
    private CountDownTimer timer;

    public static ConfirmDialog create() {
        return new ConfirmDialog();
    }

    public ConfirmDialog setContext(Context context) {
        this.context = context;
        return this;
    }

    public ConfirmDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public ConfirmDialog setImage(int resourceId) {
        this.imageResId = resourceId;
        return this;
    }

    public ConfirmDialog setDelay(int seconds) {
        this.delaySeconds = seconds;
        return this;
    }

    public ConfirmDialog onConfirm(Runnable action) {
        this.onConfirm = action;
        return this;
    }

    public ConfirmDialog onCancel(Runnable action) {
        this.onCancel = action;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.confirm_dialog, null);

        TextView messageView = view.findViewById(R.id.tvMessage);
        ImageView imageView = view.findViewById(R.id.ivIcon);

        messageView.setText(message);

        if (imageResId != -1) {
            imageView.setImageResource(imageResId);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }

        builder.setView(view)
                .setPositiveButton(R.string.option_ok, (dialog, which) -> {
                    if (onConfirm != null) onConfirm.run();
                })
                .setNegativeButton(R.string.option_cancel, (dialog, which) -> {
                    if (onCancel != null) onCancel.run();
                });

        AlertDialog dialog = builder.create();

        if (delaySeconds > 0) {
            dialog.setOnShowListener(dialogInterface -> {
                positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setEnabled(false);
                positiveButton.setTextColor(Color.GRAY);
                startCountdown();
            });
        }

        return dialog;
    }

    private void startCountdown() {
        timer = new CountDownTimer(delaySeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                positiveButton.setText(getString(R.string.option_ok) + " (" + secondsLeft + ")");
            }

            @Override
            public void onFinish() {
                positiveButton.setEnabled(true);
                positiveButton.setText(getString(R.string.option_ok));
                positiveButton.setTextColor(context.getColor(android.R.color.holo_blue_dark));
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}