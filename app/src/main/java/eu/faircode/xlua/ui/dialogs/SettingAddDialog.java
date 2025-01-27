package eu.faircode.xlua.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import eu.faircode.xlua.AppGeneric;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.api.settings.LuaSettingPacket;

public class SettingAddDialog extends AppCompatDialogFragment {
    private static final String TAG = "XLua.SettingAddDialog";

    private EditText edSettingName;
    private EditText edSettingValue;
    private EditText edSettingDescription;

    private CheckBox cbCreateSetting;
    private CheckBox cbCreateDefaultSetting;

    private ISettingDialogListener listener;
    private AppGeneric application = null;

    public void setApplication(AppGeneric app) {
        this.application = app;
        Log.i(TAG, "Application set=" + this.application);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.settingadd, null);

        edSettingName = view.findViewById(R.id.etAddSettingSettingName);
        edSettingDescription = view.findViewById(R.id.etAddSettingSettingDescription);
        edSettingValue = view.findViewById(R.id.etAddSettingSettingValue);
        cbCreateSetting = view.findViewById(R.id.cbAddSettingSettingAddCheck);
        cbCreateDefaultSetting = view.findViewById(R.id.cbAddSettingDefaultSettingAddCheck);

        builder.setView(view)
                .setTitle("Setting Builder")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "Add Setting Dialog Was Cancelled");
                    }
                }).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String settingName = edSettingName.getText().toString();
                        String settingDescription = edSettingDescription.getText().toString();
                        String settingValue = edSettingValue.getText().toString();
                        boolean createSetting = cbCreateSetting.isChecked() && Str.isValidNotWhitespaces(settingName) && Str.isValidNotWhitespaces(settingValue);
                        boolean createDefault = cbCreateDefaultSetting.isChecked();
                        if(!createDefault && !createSetting)
                            return;

                        Log.i(TAG, "Initialized all the Data from setting add now creating packet...");
                        LuaSettingPacket packet = LuaSettingPacket
                                .create(application.getUid(),
                                        application.getPackageName(),
                                        settingName,
                                        createSetting ? settingValue: null,
                                        createDefault ? settingDescription : null,
                                        LuaSettingPacket.getCodeForInsertOrUpdate(createSetting, createDefault),
                                        false,
                                        createDefault ? settingValue : null);

                        Log.i(TAG, "Finishing Packet Build=" + packet);
                        listener.pushSettingPacket(packet);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ISettingDialogListener) context;
        }catch (Exception e) {
            Log.e(TAG, "onAttach Error: " + e + "\n" + Log.getStackTraceString(e));
        }
    }
}
