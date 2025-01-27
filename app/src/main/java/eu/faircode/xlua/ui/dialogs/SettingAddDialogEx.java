package eu.faircode.xlua.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.ui.SettingsQue;
import eu.faircode.xlua.ui.interfaces.ISettingUpdateEx;

public class SettingAddDialogEx extends AppCompatDialogFragment {
    private EditText settingName, settingValue, settingDescription;
    private CheckBox checkSetting, checkSettingDefault;

    private ISettingUpdateEx callback;
    private Context context;
    private SettingsQue que;

    public SettingAddDialogEx setCallback(ISettingUpdateEx callback){  this.callback = callback; return this; }
    public SettingAddDialogEx setQue(SettingsQue que) { this.que = que; return this; }

    public SettingAddDialogEx() { }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.settingadd, null);

        settingName = view.findViewById(R.id.etAddSettingSettingName);
        settingDescription = view.findViewById(R.id.etAddSettingSettingDescription);
        settingValue = view.findViewById(R.id.etAddSettingSettingValue);
        checkSetting = view.findViewById(R.id.cbAddSettingSettingAddCheck);
        checkSettingDefault = view.findViewById(R.id.cbAddSettingDefaultSettingAddCheck);

        builder.setView(view)
                .setTitle(getString(R.string.title_add_dialog_setting_builder))
                .setNegativeButton(R.string.option_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Log.i(TAG, "Add Setting Dialog Was Cancelled");
                    }
                }).setPositiveButton(R.string.option_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = settingName.getText().toString();
                        String value = settingValue.getText().toString();
                        String descr = settingDescription.getText().toString();
                        if(value.isEmpty()) checkSetting.setChecked(false);

                        boolean settingCheck = checkSetting.isChecked();
                        boolean settingDefaultCheck = checkSettingDefault.isChecked();

                        if(!settingCheck && !settingDefaultCheck) {
                            Toast.makeText(context, R.string.error_add_dialog_no_checks, Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(!Str.isValidNotWhitespaces(name)) {
                            Toast.makeText(context, R.string.error_add_dialog_bad_input, Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(Str.hasChars(name, Str.SPACE_CHAR)) {
                            Toast.makeText(context, R.string.error_add_dialog_bad_name, Toast.LENGTH_LONG).show();
                            return;
                        }

                        LuaSettingExtended setting = new LuaSettingExtended();
                        setting.setName(name);
                        setting.setDescription(descr);
                        setting.setValue(value);
                        que.updateSetting(context, setting, -1, settingCheck, settingDefaultCheck, callback);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
