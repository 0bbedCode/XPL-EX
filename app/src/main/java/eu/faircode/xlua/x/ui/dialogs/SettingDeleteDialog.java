package eu.faircode.xlua.x.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.dialog.IDialogEventFail;
import eu.faircode.xlua.x.ui.core.dialog.IDialogEventFinish;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;

public class SettingDeleteDialog extends AppCompatDialogFragment  {
    public static SettingDeleteDialog create() { return new SettingDeleteDialog(); }

    private Context context;

    private final List<SettingHolder> settings = new ArrayList<>();
    private SettingsContainer container = null;
    private UserClientAppContext app;

    protected IDialogEventFinish dialogEventFinish;
    protected IDialogEventFail dialogEventFail;

    public SettingDeleteDialog setDialogEventFinish(IDialogEventFinish onDialogEvent) {
        this.dialogEventFinish = onDialogEvent;
        return this;
    }

    public SettingDeleteDialog setDialogEventFail(IDialogEventFail onDialogFail) {
        this.dialogEventFail = onDialogFail;
        return this;
    }

    public SettingDeleteDialog set(List<SettingHolder> settings, SettingsContainer container) {
        this.settings.addAll(settings);
        this.container = container;
        return this;
    }

    public SettingDeleteDialog setApp(UserClientAppContext app) {
        this.app = app;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.settingdelete, null);

        final CheckBox cbDelete, cbDeleteDefaultMap, cbForceKill;

        cbDelete = view.findViewById(R.id.cbDeleteSettingDeleteSetting);
        cbDeleteDefaultMap = view.findViewById(R.id.cbDeleteSettingDeleteDefault);
        cbForceKill = view.findViewById(R.id.cbDeleteSettingForceKill);

        boolean isValidApp = app != null && app.appPackageName != null && app.appUid >= 0;
        //boolean isValidSet = container != null && !settings.isEmpty();

        boolean isValidSettings = isValidApp && !settings.isEmpty();
        cbDelete.setEnabled(isValidSettings);
        cbDelete.setChecked(isValidSettings);

        //cbDeleteDefaultMap.setEnabled(container != null);
        cbDeleteDefaultMap.setEnabled(false);

        boolean isValidForceStop = isValidApp && !app.isGlobal();
        cbForceKill.setEnabled(isValidForceStop);
        cbForceKill.setChecked(isValidForceStop && app.isKill());

        builder.setView(view)
                .setTitle(R.string.title_delete_setting)
                .setNegativeButton(R.string.option_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .setPositiveButton(R.string.option_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(isValidApp) {
                            int finished = 0;
                            boolean deleteSetting = cbDelete.isChecked();
                            boolean deleteDefault = cbDeleteDefaultMap.isChecked();     //ToDo
                            boolean forceKill = cbForceKill.isChecked();
                            //Toast.makeText(context, "NULL NULL For settings delete", Toast.LENGTH_LONG).show();
                            if(deleteSetting) {
                                for(SettingHolder setting : settings) {
                                    A_CODE code = PutSettingExCommand.call(context, setting, app, forceKill, true);
                                    if(A_CODE.isSuccessful(code)) {
                                        setting.setValue(null, true);
                                        setting.ensureUiUpdated("");
                                        setting.setNameLabelColor(context);
                                        finished++;
                                    }
                                }
                            }

                            if(finished > 0 && dialogEventFinish != null) {
                                dialogEventFinish.onFinish();
                                return;
                            }

                            if(dialogEventFail != null)
                                dialogEventFail.onFail(context.getString(R.string.msg_error_nothing_deleted));
                        } else {
                            if(dialogEventFail != null)
                                dialogEventFail.onFail(context.getString(R.string.msg_error_bad_app));
                        }
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
