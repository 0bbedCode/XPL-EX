package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
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

import com.google.android.material.snackbar.BaseTransientBottomBar;
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
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.xlua.commands.XPacket;
import eu.faircode.xlua.x.xlua.commands.call.AssignHooksCommand;
import eu.faircode.xlua.x.xlua.commands.call.ForceStopAppCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutConfigCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetConfigsCommand;
import eu.faircode.xlua.x.xlua.configs.XPConfig;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.hook.AppXpPacket;
import eu.faircode.xlua.x.xlua.hook.AssignmentsPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class ConfigDialog extends AppCompatDialogFragment implements ConfigAdapter.OnConfigActionListener {
    private static final String TAG = "XLua.ConfigDialog";

    private Context context;
    private final List<XPConfig> configs = new ArrayList<>();
    private XPConfig enabledConfig;

    private ConfigAdapter adapter;
    private Button btnEdit, btnApply;
    private View baseView;

    private boolean kill = false;
    private int uid;
    private String packageName;

    public static ConfigDialog create() {
        return new ConfigDialog();
    }

    public ConfigDialog setApp(int uid, String packageName) {
        this.uid = uid;
        this.packageName = packageName;
        return this;
    }

    public ConfigDialog test() {
        for(int i = 0; i < RandomGenerator.nextInt(4, 10); i++) {
            XPConfig c = new XPConfig();
            //For some reason some of the Names are empty Strings or white spaces maybe ?? I think they are empty
            c.name = RandomGenerator.nextStringAlpha(3, 14);
            c.type = "test";
            c.author = "Obc";
            c.version = "1.0" + RandomGenerator.nextStringAlpha(3, 8);
            c.setTags(RandomGenerator.nextElements(XPConfig.DEFAULT_TAGS));

            if(i == 0)
                this.enabledConfig = c;

            for(int j = 0; j < RandomGenerator.nextInt(3, 10); j++) {
                String hookId = RandomGenerator.nextStringAlpha(6, 15);
                c.hooks.add(hookId);
            }

            for(int j = 0; j < RandomGenerator.nextInt(3, 10); j++) {
                String settingName = RandomGenerator.nextStringAlpha(6, 15);
                String settingValue = RandomGenerator.nextStringAlpha(6, 19);
                SettingPacket packet = new SettingPacket();
                packet.name = settingName;
                packet.value = settingValue;

                c.settings.add(packet);
            }

            this.configs.add(c);
        }

        return this;
    }

    public ConfigDialog setRootView(View view) {
        this.baseView = view;
        return this;
    }

    public ConfigDialog setConfigs(Context context) {
        List<XPConfig> configs = GetConfigsCommand.get(context);
        String selectedConfig = GetSettingExCommand.getConfig(context, uid, packageName);
        setConfigs(configs, selectedConfig);
        return this;
    }

    public ConfigDialog setConfigs(List<XPConfig> configs, String selectedConfig) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Configs Count=" + ListUtil.size(configs) + " Selected Config=" + selectedConfig);

        this.configs.clear();
        if (configs != null && !configs.isEmpty()) {
            if (!Str.isEmpty(selectedConfig)) {
                // Find the config with matching name and set as enabled
                XPConfig selected = null;
                for (XPConfig config : configs) {
                    if (config.name.equals(selectedConfig)) {
                        selected = config;
                        this.enabledConfig = config;
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

        if (adapter != null) {
            adapter.notifyDataSetChanged();
            adapter.setEnabledConfig(enabledConfig);
            updateButtonStates();
        }
        return this;
    }

    private ConfigDialog setEnabledConfig(XPConfig config) {
        this.enabledConfig = config;
        if (adapter != null)
            adapter.setEnabledConfig(config);

        updateButtonStates();
        return this;
    }

    public void refreshConfigs() {
        // Refresh your configs list here and update adapter
        adapter.notifyDataSetChanged();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.config_dialog, null);

        EditText etSearch = view.findViewById(R.id.etSearch);
        ListView lvConfigs = view.findViewById(R.id.lvConfigs);

        adapter = new ConfigAdapter(context, configs, this);
        adapter.setOriginalConfigs(configs);
        adapter.setEnabledConfig(enabledConfig);
        lvConfigs.setAdapter(adapter);
        lvConfigs.setDivider(null);
        lvConfigs.setDividerHeight(0);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

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
            //Button btnCreate = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            btnApply.setOnClickListener(v -> {
                if(enabledConfig != null && !Str.isEmpty(enabledConfig.name) && !Str.isEmpty(packageName) && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName)) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Apply, Config=" + Str.toStringOrNull(enabledConfig));

                    if(A_CODE.isSuccessful(PutSettingExCommand.putConfig(context, uid, packageName, enabledConfig.name))) {
                        enabledConfig.applySettings(context, uid, packageName, true);
                        enabledConfig.applyAssignments(context, uid, packageName, true);
                        if(kill)
                            ForceStopAppCommand.stop(context, uid, packageName);

                        if(baseView != null)
                            Snackbar.make(baseView, getString(R.string.msg_finished_applying_config), Snackbar.LENGTH_LONG).show();

                        try {
                            Fragment fragment = getParentFragment();
                            if(fragment instanceof IFragmentController) {
                                IFragmentController controller = (IFragmentController) fragment;
                                controller.refresh();
                            }
                        }catch (Exception ignored) { }
                    }
                }
            });

            btnEdit.setOnClickListener(v -> {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Edit, Config=" + Str.toStringOrNull(enabledConfig));

                if (enabledConfig != null)
                    ConfigCreateDialog.create()
                            .setOnFinish((r, c) -> {
                                if(this.baseView != null)
                                    Snackbar.make(baseView, Str.combine(getString(R.string.msg_config_push), r.name()), Snackbar.LENGTH_LONG).show();
                                if(A_CODE.isSuccessful(r)) {
                                    setConfigs(context);
                                    refreshConfigs();
                                }
                            })
                            .setApp(uid, packageName)
                            .setConfigs(configs, enabledConfig == null ? null : enabledConfig.name)
                            .show(getParentFragmentManager(), getString(R.string.title_config_modify));
            });

            updateButtonStates();
        });

        return dialog;
    }

    private void updateButtonStates() {
        if (btnEdit != null && btnApply != null) {
            boolean hasSelection = enabledConfig != null;
            btnEdit.setEnabled(hasSelection);
            btnApply.setEnabled(!Str.isEmpty(packageName) && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName) && hasSelection);
            btnEdit.setAlpha(hasSelection ? 1.0f : 0.5f);
            btnApply.setAlpha(hasSelection ? 1.0f : 0.5f);
        }
    }

    @Override
    public void onConfigChecked(XPConfig config) {
        setEnabledConfig(config);
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

                    if(A_CODE.isSuccessful(res)) {
                        setConfigs(context);
                        refreshConfigs();
                    }
                }).show(getParentFragmentManager(), getString(R.string.title_delete_config));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}