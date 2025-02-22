package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignmentsCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetSettingsExCommand;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.XBackup;

public class BackupDialog extends AppCompatDialogFragment {
    private static final String TAG = LibUtil.generateTag(BackupDialog.class);

    //These "all" versions require auth check ?

    private Context context;
    private XBackup backup;
    private boolean wasCreatedFromFile;
    private Button positiveButton;

    private EditText tiBackupName;
    private CheckBox cbDefinitions, cbAllHooks, cbSettings, cbAssignments, cbDeleteOld;

    private final List<XLuaHook> allHooks = new ArrayList<>();

    public interface IOnOperationExportOrApplied {
        void onFinishedSuccessfully(XBackup instance, boolean wasImported);
    }

    private IOnOperationExportOrApplied onOperationEvent;

    public static BackupDialog create() { return new BackupDialog(); }

    public BackupDialog setBackup(XBackup backup, boolean wasImportedFromFile) {
        this.backup = backup;
        this.wasCreatedFromFile = wasImportedFromFile;
        return this;
    }

    public BackupDialog setDefinitions(Context context) { return setDefinitions(GetHooksCommand.dump(context, true)); }
    public BackupDialog setDefinitions(List<XLuaHook> definitions) {
        if (backup == null)
            backup = new XBackup();

        if(DebugUtil.isDebug())
            Log.d(TAG, "Setting Definitions, Count=" + ListUtil.size(definitions));

        backup.getDefinitions().clear();
        if (ListUtil.isValid(definitions)) {
            backup.getDefinitions().addAll(definitions);
            updateCheckboxCounts();
        }
        return this;
    }

    public BackupDialog setAllHooks(Context context) { return setAllHooks(GetHooksCommand.getHooks(context, true, true));}
    public BackupDialog setAllHooks(List<XLuaHook> hooks) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Setting All Hooks, Count=" + ListUtil.size(hooks));

        ListUtil.addAllIfValid(this.allHooks, hooks, true);
        updateCheckboxCounts();
        return this;
    }

    public BackupDialog setSettings(Context context) { return setSettings(GetSettingsExCommand.dump(context, true)); }
    public BackupDialog setSettings(List<SettingPacket> settings) {
        if (backup == null)
            backup = new XBackup();

        if(DebugUtil.isDebug())
            Log.d(TAG, "Setting Settings, Count=" + ListUtil.size(settings));

        backup.getSettings().clear();
        if (ListUtil.isValid(settings)) {
            for(SettingPacket packet : settings)
                if(packet.value != null)
                    backup.addSetting(packet);

            updateCheckboxCounts();
        }
        return this;
    }

    public BackupDialog setAssignments(Context context) { return setAssignments(GetAssignmentsCommand.dump(context, false)); }
    public BackupDialog setAssignments(List<AssignmentPacket> assignments) {
        if (backup == null)
            backup = new XBackup();

        if(DebugUtil.isDebug())
            Log.d(TAG, "Setting Assignments, Count=" + ListUtil.size(assignments));

        backup.getAssignments().clear();
        if (ListUtil.isValid(assignments)) {
            for(AssignmentPacket packet : assignments)
                if(!Str.isEmpty(packet.getHookId()))
                    backup.addAssignment(packet);

            updateCheckboxCounts();
        }
        return this;
    }

    public BackupDialog setOnOperationExportedOrApplied(IOnOperationExportOrApplied onEvent) {
        this.onOperationEvent = onEvent;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.backup_dialog, null);

        initializeViews(view);
        setupListeners();
        updateCheckboxCounts();

        builder.setView(view)
                .setTitle(R.string.title_backup)
                .setPositiveButton(wasCreatedFromFile ? R.string.option_apply : R.string.option_export, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do more init
                        if(!wasCreatedFromFile) {
                            backup.setDate(System.currentTimeMillis());
                            backup.setAppVersion(BuildConfig.VERSION_NAME);
                        }

                        backup = backup.createFilteredCopy(
                                cbDefinitions.isChecked(),
                                true,
                                cbAssignments.isChecked(),
                                cbSettings.isChecked());

                        backup.dropOld = cbDeleteOld.isChecked();
                        notifyOperationComplete();
                    }
                })
                .setNegativeButton(R.string.option_cancel, null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            updateCheckboxCounts();
            ensurePositiveButtonState();

            //updateButtonStates();
            /*positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //notifyOperationComplete
                    //BackupDialogUtils.startBackupSaver(this, "");
                    notifyOperationComplete();
                }
            });*/
        });

        return dialog;
    }

    private void initializeViews(View view) {
        tiBackupName = view.findViewById(R.id.tiBackupName);
        cbDefinitions = view.findViewById(R.id.cbDefinitions);
        cbAllHooks = view.findViewById(R.id.cbAllHooks);
        cbSettings = view.findViewById(R.id.cbSettings);
        cbAssignments = view.findViewById(R.id.cbAssignments);
        cbDeleteOld = view.findViewById(R.id.cbDeleteOld);


        if (backup != null && !Str.isEmpty(backup.getName())) {
            tiBackupName.setText(backup.getName());
        }
    }

    final CompoundButton.OnCheckedChangeListener checkListener = (compoundButton, b) -> {
        updateCheckboxCounts();
        ensurePositiveButtonState();
    };

    private void checkboxWire(boolean wire) {
        try {
            cbAllHooks.setOnCheckedChangeListener(wire ? checkListener : null);
            cbDefinitions.setOnCheckedChangeListener(wire ? checkListener : null);
            cbSettings.setOnCheckedChangeListener(wire ? checkListener : null);
            cbAssignments.setOnCheckedChangeListener(wire ? checkListener : null);
            if(wasCreatedFromFile && cbDeleteOld != null) {
                cbDeleteOld.setOnCheckedChangeListener(wire ? checkListener : null);
            }
        }catch (Exception e) {
            Log.e(TAG, "Check Box Link Error Event, Error=" + e);
        }
    }

    private void setupListeners() {
        tiBackupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (backup != null)
                    backup.setName(s.toString());

                //updateButtonStates();
            }
        });
    }

    private void updateCheckboxCounts() {
        if (!isAdded())
            return;

        new Handler(Looper.getMainLooper()).post(() -> {
            checkboxWire(false);
            if (backup != null) {
                ensureCheckBoxState(cbDefinitions, R.string.text_definitions_count, ListUtil.size(backup.getDefinitions()));
                ensureCheckBoxState(cbSettings, R.string.text_settings_count, ListUtil.size(backup.getSettings()));
                ensureCheckBoxState(cbAssignments, R.string.text_assignments_count, ListUtil.size(backup.getAssignments()));
            }

            ensureCheckBoxState(cbAllHooks, R.string.text_all_hooks, ListUtil.size(allHooks));
            if(cbDeleteOld != null) {
                try {
                    cbDeleteOld.setVisibility(wasCreatedFromFile ? View.VISIBLE : View.GONE);
                    cbDeleteOld.setEnabled(wasCreatedFromFile);
                }catch (Exception ignored) { }
            }
            checkboxWire(true);
        });
    }

    public void ensureCheckBoxState(CheckBox checkBox, int resId, int count) {
        if(checkBox != null) {
            try {
                checkBox.setText(getString(resId, count));
                checkBox.setEnabled(count > 0);
                if(count < 1)
                    checkBox.setChecked(false);
            }catch (Exception e) {
                Log.e(TAG, "Failed to Ensure the Check Box State, Error=" + e);
            }
        }
    }

    /*private void updateButtonStates() {
        if (positiveButton == null) return;

        boolean hasName = backup != null && !TextUtils.isEmpty(backup.getName());
        boolean hasContent = backup != null && (
                ListUtil.isValid(backup.getDefinitions()) ||
                        ListUtil.isValid(backup.getSettings()) ||
                        ListUtil.isValid(backup.getAssignments()) ||
                        ListUtil.isValid(backup.getScripts())
        );

        positiveButton.setEnabled(hasName && hasContent);
        positiveButton.setAlpha(hasName && hasContent ? 1.0f : 0.5f);

        if (DebugUtil.isDebug()) {
            Log.d(TAG, "Button states updated - hasName: " + hasName +
                    ", hasContent: " + hasContent);
        }
    }*/

    private void ensurePositiveButtonState() { ensurePositiveButtonState(isPositiveEnabled()); }
    private void ensurePositiveButtonState(boolean enable) {
        if(positiveButton != null) {
            try {
                positiveButton.setEnabled(enable);
                positiveButton.setAlpha(enable ? 1.0f : 0.5f);
            }catch (Exception e) {
                Log.e(TAG, "Failed to Set Positive Button State! Error=" + e);
            }
        }
    }

    private boolean isPositiveEnabled() {
        if(backup != null) {
            try {
                return (ListUtil.isValid(backup.getAssignments()) && cbAssignments.isChecked()) ||
                        (ListUtil.isValid(backup.getSettings()) && cbSettings.isChecked()) ||
                        (ListUtil.isValid(backup.getDefinitions()) && cbDefinitions.isChecked()) ||
                        (ListUtil.isValid(allHooks) && cbAllHooks.isChecked());
            }catch (Exception e) {
                Log.e(TAG, "Failed to get is Positive Enabled, Error=" + e);
            }
        }

        return false;
    }


    /*private void updateButtonStates() {
        if (positiveButton == null) return;

        boolean hasValidContent = false;

        // Check each section that's checked AND has data
        if (cbDefinitions != null && cbDefinitions.isChecked()) {
            hasValidContent |= ListUtil.isValid(backup.getDefinitions());
        }

        if (cbAllHooks != null && cbAllHooks.isChecked()) {
            hasValidContent |= ListUtil.isValid(allHooks);
        }

        if (cbSettings != null && cbSettings.isChecked()) {
            hasValidContent |= ListUtil.isValid(backup.getSettings());
        }

        if (cbAssignments != null && cbAssignments.isChecked()) {
            hasValidContent |= ListUtil.isValid(backup.getAssignments());
        }

        // Enable if ANY checked section has valid data
        positiveButton.setEnabled(hasValidContent);
        positiveButton.setAlpha(hasValidContent ? 1.0f : 0.5f);

        if (DebugUtil.isDebug()) {
            Log.d(TAG, "Button states updated - hasValidContent: " + hasValidContent);
        }
    }*/

    private void notifyOperationComplete() {
        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, "on Finished Dump=" + backup.dumpLog());

            if (onOperationEvent != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    new Handler(Looper.getMainLooper())
                            .post(() -> onOperationEvent.onFinishedSuccessfully(backup, wasCreatedFromFile));
                } else {
                    onOperationEvent.onFinishedSuccessfully(backup, wasCreatedFromFile);
                }
            }
        } catch (Exception e) {
            if (DebugUtil.isDebug()) {
                Log.e(TAG, "Error notifying operation complete: " + e);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}