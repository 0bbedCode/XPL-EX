package eu.faircode.xlua.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

import eu.faircode.xlua.R;
import eu.faircode.xlua.api.hook.LuaHooksGroup;
import eu.faircode.xlua.ui.HookWarnings;

public class HookWarningDialog extends AppCompatDialogFragment {
    private Context context;
    private TextView tvWarnMessage;


    private LuaHooksGroup group;
    private String message;
    public HookWarningDialog setText(String text) { this.message = text; return this; }
    public HookWarningDialog setGroup(LuaHooksGroup group) { this.group = group; return this; }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.hookwarning, null);
        tvWarnMessage = view.findViewById(R.id.tvHookWarningMessage);
        tvWarnMessage.setText(message);

        builder.setView(view)
                .setTitle(getString(R.string.title_hook_warning))
                .setNegativeButton(R.string.option_not_ask, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { HookWarnings.setWarnFlag(context, group.name, false); group.hasWarning = false; }
                }).setPositiveButton(R.string.option_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });

        return builder.create();
    }

        @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
