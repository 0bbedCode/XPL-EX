package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;

public class MessageDialog extends AppCompatDialogFragment {
    private Context context;
    private String title;
    private String name = "";
    private String message = "";
    private int iconResId = -1;

    public static MessageDialog create() {
        return new MessageDialog();
    }

    public MessageDialog setName(String name) {
        this.name = name;
        return this;
    }

    public MessageDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public MessageDialog setIcon(int resourceId) {
        this.iconResId = resourceId;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.message_dialog, null);

        ImageView ivIcon = view.findViewById(R.id.ivDialogIcon);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvMessage = view.findViewById(R.id.tvMessage);

        if (iconResId != -1) {
            ivIcon.setImageResource(iconResId);
            ivIcon.setVisibility(View.VISIBLE);
        }

        tvName.setText(name);
        tvMessage.setText(message);

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