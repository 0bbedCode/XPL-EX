package eu.faircode.xlua.x.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.ui.adapters.ConfigAdapter;
import eu.faircode.xlua.x.ui.core.interfaces.IFragmentController;
import eu.faircode.xlua.x.ui.core.interfaces.IListChange;
import eu.faircode.xlua.x.ui.fragments.ConfUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.XPacket;
import eu.faircode.xlua.x.xlua.commands.call.ForceStopAppCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutConfigCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetConfigsCommand;
import eu.faircode.xlua.x.xlua.configs.XPConfig;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;



public class ConfigDialog extends AppCompatDialogFragment implements ConfigAdapter.OnConfigActionListener {
    private static final String TAG = LibUtil.generateTag(ConfigDialog.class);

    private Context context;

    private IListChange<SettingPacket> onChange;

    private XPConfig enabled = null;
    private final List<XPConfig> configs = new ArrayList<>();

    private ConfigAdapter adapter;
    private Button btnEdit, btnApply, btnToFile;
    private View baseView;

    private boolean kill = false;
    private int uid;
    private String packageName;

    public XPConfig getChecked() { return this.adapter != null ? this.adapter.checkedOrEnabled(null, false) : null; }

    public static ConfigDialog create() {
        return new ConfigDialog();
    }


    public ConfigDialog setOnChange(IListChange<SettingPacket> onChange) {
        this.onChange = onChange;
        return this;
    }

    public ConfigDialog setApp(int uid, String packageName) { return setApp(uid, packageName, false); }
    public ConfigDialog setApp(int uid, String packageName, boolean kill) {
        this.uid = uid;
        this.packageName = packageName;
        this.kill = kill;
        return this;
    }

    public ConfigDialog setRootView(View view) {
        this.baseView = view;
        return this;
    }

    public ConfigDialog setConfigs(Context context) {
        List<XPConfig> configs = GetConfigsCommand.get(context);
        String selectedConfig = GetSettingExCommand.getConfig(context, uid, packageName);
        internalSetConfigs(configs, selectedConfig);
        return this;
    }

    private void internalSetConfigs(List<XPConfig> configs, String selectedConfig) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Configs Count=" + ListUtil.size(configs) + " Selected Config=" + selectedConfig);

        this.enabled = null;
        this.configs.clear();
        if (configs != null && !configs.isEmpty()) {
            if (!Str.isEmpty(selectedConfig)) {
                // Find the config with matching name and set as enabled
                XPConfig selected = null;
                for (XPConfig config : configs) {
                    if (config.name.equals(selectedConfig)) {
                        selected = config;
                        this.enabled = config;
                        break;
                    }
                }

                // If found, add it first, then add others
                if (selected != null) {
                    this.configs.add(selected);
                    for (XPConfig config : configs) {
                        if (!config.equals(selected)) {
                            this.configs.add(config);
                        }
                    }
                } else {
                    // No matching config found, add all normally
                    this.configs.addAll(configs);
                }
            } else {
                // No selected config specified, add all normally
                this.configs.addAll(configs);
            }
        }

        if (adapter != null)
            internalRefresh(null, false, true, true, true);
    }

    public void refreshConfigs() {
        adapter.notifyDataSetChanged();
        updateButtonStates();
    }

    private void internalRefresh(Context context, boolean initConfigs, boolean notifyAdapter, boolean updateChecked, boolean setOriginal) {
        if(initConfigs) setConfigs(context);
        if(this.enabled != null) {
            boolean found = false;
            for(XPConfig c : configs) {
                if(c.name.equalsIgnoreCase(this.enabled.name)) {
                    found = true;
                    break;
                }
            }

            if(!found)
                this.enabled = null;
        }

        if(adapter != null) {
            try {
                if(notifyAdapter) adapter.notifyDataSetChanged();
                if(setOriginal) adapter.setOriginalConfigs(configs);
                if(updateChecked) adapter.setCheckedConfig(adapter.checkedOrEnabled(enabled, false));
            }catch (Exception e) {
                Log.e(TAG, "Failed to Update Adapter! Error=" + e);
            }
        }

        updateButtonStates();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null || resultCode != Activity.RESULT_OK) return;
        Uri uri = data.getData();
        if (uri == null) return;

        switch (requestCode) {
            case ConfUtils.REQUEST_OPEN_CONFIG:
                XPConfig config = ConfUtils.readConfigFromUri(context, uri);
                if (config != null) {
                    ConfigCreateDialog.create()
                            .setApp(uid, packageName)
                            .setConfigs(context, false)
                            .consumeFileConfig(config)
                            .setOnFinish((res, cfg) -> {
                                if(baseView != null) {
                                    Snackbar.make(baseView,
                                            Str.combine(getString(R.string.msg_config_push), res.name()),
                                            Snackbar.LENGTH_LONG).show();
                                }

                                if(A_CODE.isSuccessful(res))
                                    internalRefresh(context, true, true,true, true);
                            })
                            .show(getParentFragmentManager(), context.getString(R.string.title_config_modify));
                } else {
                    Snackbar.make(baseView != null ? baseView : getView(),
                            R.string.result_config_read_failed,
                            Snackbar.LENGTH_LONG).show();
                }
                break;

            case ConfUtils.REQUEST_SAVE_CONFIG:
                XPConfig checked = getChecked();
                if (checked != null) {
                    ConfUtils.takePersistablePermissions(context, uri);
                    boolean success = ConfUtils.writeConfigToUri(context, uri, checked);
                    Snackbar.make(baseView != null ? baseView : getView(),
                            success ? R.string.result_file_save_success : R.string.result_file_save_failed,
                            Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.config_dialog, null);

        EditText etSearch = view.findViewById(R.id.etSearch);
        ListView lvConfigs = view.findViewById(R.id.lvConfigs);

        Button btnFromFile = view.findViewById(R.id.btnFromFile);
        btnToFile = view.findViewById(R.id.btnToFile);

        adapter = new ConfigAdapter(context, configs, this);
        internalRefresh(null, false, false, true, true);
        lvConfigs.setAdapter(adapter);
        lvConfigs.setDivider(null);
        lvConfigs.setDividerHeight(0);

        btnFromFile.setOnClickListener(v -> ConfUtils.startConfigFilePicker(this));

        btnToFile.setOnClickListener(v -> {
            XPConfig checked = getChecked();
            if (checked != null) ConfUtils.startConfigSavePicker(this, checked.name);
             else Snackbar.make(v, R.string.msg_no_config_selected, Snackbar.LENGTH_SHORT).show();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { adapter.getFilter().filter(s); }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        builder.setView(view)
                .setTitle(R.string.title_config_manager)
                .setPositiveButton(R.string.option_apply, null)
                .setNegativeButton(R.string.option_cancel, null)
                .setNeutralButton(R.string.option_edit, null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            btnApply = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnEdit = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            btnApply.setOnClickListener(v -> {
                XPConfig checked = getChecked();
                if(checked != null && !Str.isEmpty(checked.name) && !Str.isEmpty(packageName) && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName)) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Apply, Config=" + Str.toStringOrNull(checked));

                    if(A_CODE.isSuccessful(PutSettingExCommand.putConfig(context, uid, packageName, checked.name))) {
                        List<SettingPacket> changed = checked.applySettings(context, uid, packageName, true);
                        checked.applyAssignments(context, uid, packageName, true);
                        if(kill) ForceStopAppCommand.stop(context, uid, packageName);
                        if(baseView != null)
                            Snackbar.make(baseView, getString(R.string.msg_finished_applying_config), Snackbar.LENGTH_LONG).show();

                        if(onChange != null)
                            onChange.onItemsChange(changed);
                    }
                }
            });

            btnEdit.setOnClickListener(v -> {
                XPConfig checked = getChecked();
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Edit, Config=" + Str.toStringOrNull(checked));

                if (checked != null)
                    ConfigCreateDialog.create()
                            .setOnFinish((r, c) -> {
                                if(this.baseView != null) Snackbar.make(baseView, Str.combine(getString(R.string.msg_config_push), r.name()), Snackbar.LENGTH_LONG).show();
                                if(A_CODE.isSuccessful(r)) internalRefresh(context, true, true,true, false);
                            })
                            .setApp(uid, packageName)
                            .setConfigs(configs, checked.name)
                            .show(getParentFragmentManager(), getString(R.string.title_config_modify));
            });

            updateButtonStates();
        });

        return dialog;
    }

    private void updateButtonStates() {
        if (btnEdit != null && btnApply != null) {
            try {
                boolean hasSelection = getChecked() != null;
                btnEdit.setEnabled(hasSelection);
                btnApply.setEnabled(!Str.isEmpty(packageName) && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName) && hasSelection);
                btnEdit.setAlpha(hasSelection ? 1.0f : 0.5f);
                btnApply.setAlpha(hasSelection ? 1.0f : 0.5f);
            }catch (Exception e) {
                Log.e(TAG, "Failed to Update the Edit and Apply Button States, Error=" + e);
            }
        }

        if(btnToFile != null) {
            try {
                boolean hasConfigSelected = getChecked() != null;
                String txt = hasConfigSelected ? getString(R.string.button_config_to_file) :  getString(R.string.button_select_config);
                btnToFile.setEnabled(hasConfigSelected);
                btnToFile.setClickable(hasConfigSelected);
                btnToFile.setText(txt);
            }catch (Exception e) {
                Log.e(TAG, "Failed to Update the Config To File Button State! Error=" + e);
            }
        }
    }

    @Override
    public void onConfigChecked(XPConfig config) {
        internalRefresh(null, false, false,true, false);
    }

    @Override
    public void onConfigDelete(XPConfig config) {
        ConfirmDialog.create()
                .setContext(context)
                .setImage(R.drawable.ic_warining_one)
                .setDelay(1)
                .setMessage(Str.combineEx(getString(R.string.msg_confirm_delete_config), Str.NEW_LINE, config.name))
                .onConfirm(() -> {
                    A_CODE res = XPacket.delete(uid, config).callRes(context, PutConfigCommand.COMMAND_NAME);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Deleted Config with Result: " + res.name());

                    if(A_CODE.isSuccessful(res))
                        internalRefresh(context, true, true, true, false);
                }).show(getParentFragmentManager(), getString(R.string.title_delete_config));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}