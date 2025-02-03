package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Process;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.adapters.SettingSmallAdapter;
import eu.faircode.xlua.x.ui.core.dialog.IDialogEventFinishObject;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.commands.XPacket;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutConfigCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetConfigsCommand;
import eu.faircode.xlua.x.xlua.configs.XPConfig;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class ConfigCreateDialog extends AppCompatDialogFragment {
    private static final String TAG = "XLua.ConfigCreateDialog";

    private Context context;
    private XPConfig config = new XPConfig();


    private SharedRegistry sharedRegistry = new SharedRegistry();

    private IDialogEventFinishObject<XPConfig> onFinish;
    private FlexboxLayout flexTags;
    private final List<String> selectedTags = new ArrayList<>();

    private final Map<String, XPConfig> configs = new HashMap<>();

    private int uid;
    private String packageName;


    public static ConfigCreateDialog create() { return new ConfigCreateDialog(); }

    public ConfigCreateDialog setApp(int uid, String packageName) {
        this.uid = uid;
        this.packageName = packageName;
        return this;
    }

    public ConfigCreateDialog setOnFinish(IDialogEventFinishObject<XPConfig> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    public ConfigCreateDialog setConfigs(Context context) { return setConfigs(context, true); }
    public ConfigCreateDialog setConfigs(Context context, boolean focusEnabled) {
        //sharedRegistry = new SharedRegistry();
        List<XPConfig> configs = GetConfigsCommand.get(context);
        String selectedConfig = GetSettingExCommand.getConfig(context, uid, packageName);
        setConfigs(configs, selectedConfig, focusEnabled);

        return this;
    }

    public ConfigCreateDialog consumeFileConfig(XPConfig config) {
        this.config = config;
        for(SettingPacket setting : config.settings)
            sharedRegistry.setChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getSharedId(), true);

        return this;
    }

    public ConfigCreateDialog setHookIds(List<String> hookIds) {
        if(ListUtil.isValid(hookIds) && config != null)
           ListUtil.addAllIfValid(this.config.hooks, hookIds, true);

        return this;
    }

    public ConfigCreateDialog setConfigs(List<XPConfig> configs, String selected) { return setConfigs(configs, selected, true); }
    public ConfigCreateDialog setConfigs(List<XPConfig> configs, String selected, boolean focusEnabled) {
        if(ListUtil.isValid(configs)) {
            for(XPConfig c : configs) {
                this.configs.put(c.name, c);
                if(focusEnabled) {
                    if(!Str.isEmpty(selected) && c.name.equals(selected)) {
                        this.config = c;
                        for(SettingPacket setting : config.settings)
                            sharedRegistry.setChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getSharedId(), true);
                    }
                }
            }
        }

        return this;
    }

    public ConfigCreateDialog setSettings(List<SettingPacket> settings) {
        if(ListUtil.isValid(settings)) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Setting Settings, Count=" + settings.size());

            config.settings.clear();
            for(SettingPacket packet : settings) {
                if(packet.value != null) {
                    sharedRegistry.setChecked(SharedRegistry.STATE_TAG_SETTINGS, packet.getSharedId(), true);
                    config.settings.add(packet);
                }
            }
        }

        return this;
    }


    /*public ConfigCreateDialog setConfigName(String configName) {
        this.configName = configName;
        return this;
    }

    public ConfigCreateDialog setConfigAuthor(String author) {
        this.configAuthor = author;
        return this;
    }

    public ConfigCreateDialog setConfigVersion(String version) {
        this.configVersion = version;
        return this;
    }

    public ConfigCreateDialog setSettings(List<SettingPacket> settings) {
        this.settings.clear();
        if (settings != null) {
            ListUtil.addAllIfValid(this.settings, settings);
            for (SettingPacket setting : settings) {
                sharedRegistry.setChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getId(), true);
            }
        }
        return this;
    }

    public ConfigCreateDialog setHookIds(List<String> hookIds) {
        this.hookIds.clear();
        ListUtil.addAllIfValid(this.hookIds, hookIds);
        return this;
    }*/

    /*private void updateParentDialog() {
        try {
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof ConfigDialog) {
                ((ConfigDialog) parentFragment).refreshConfigs();
            }
        }catch (Exception ignored) { }
    }*/

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.create_config_dialog, null);

        EditText tiConfigName = view.findViewById(R.id.tiConfigName);
        EditText tiConfigAuthor = view.findViewById(R.id.tiConfigAuthor);
        EditText tiConfigVersion = view.findViewById(R.id.tiConfigVersion);

        // Initialize fields if available
        if (!Str.isEmpty(config.name)) tiConfigName.setText(config.name);
        if (!Str.isEmpty(config.author)) tiConfigAuthor.setText(config.author);
        if (!Str.isEmpty(config.version)) tiConfigVersion.setText(config.version);

        flexTags = view.findViewById(R.id.flexTags);
        ListUtil.addAllIfValid(this.selectedTags, config.getTags(), true);
        setupFlexboxWithTags();
        updateFlexboxWithTags();


        ListView lvSettings = view.findViewById(R.id.listviewSettings);
        ListView lvHooks = view.findViewById(R.id.listviewHooks);

        CheckBox cbCheckSettingsBulk = view.findViewById(R.id.cbCheckSettingsBulk);
        TextView tvSelectedSettingsLabel = view.findViewById(R.id.tvSelectedSettingsLabel);
        ImageView ivExpanderConfigSettings = view.findViewById(R.id.ivExpanderConfigSettings);

        CheckBox cbCheckHooksBulk = view.findViewById(R.id.cbCheckHooksBulk);
        TextView tvSelectedHooksLabel = view.findViewById(R.id.tvSelectedHooksLabel);
        ImageView ivExpanderConfigHooks = view.findViewById(R.id.ivExpanderConfigHooks);

        // Settings ListView setup
        SettingSmallAdapter settingsAdapter = new SettingSmallAdapter(context, config.settings, sharedRegistry, () -> {
            updateBulkCheckboxAndLabel(cbCheckSettingsBulk, tvSelectedSettingsLabel, config.settings.size(), SharedRegistry.STATE_TAG_SETTINGS);
        });

        lvSettings.setAdapter(settingsAdapter);

        // Settings expander and bulk checkbox
        boolean isSettingsExpanded = sharedRegistry.isExpanded(SharedRegistry.STATE_TAG_SETTINGS, "view_settings");
        updateExpanded(isSettingsExpanded, ivExpanderConfigSettings, lvSettings);

        if (config.settings.isEmpty()) {
            ivExpanderConfigSettings.setEnabled(false);
            ivExpanderConfigSettings.setClickable(false);
            ivExpanderConfigSettings.setAlpha(0.3f);
            tvSelectedSettingsLabel.setText("---");
        }

        cbCheckSettingsBulk.setOnClickListener(v -> {
            boolean bulkChecked = cbCheckSettingsBulk.isChecked();
            for (SettingPacket setting : config.settings)
                sharedRegistry.setChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getSharedId(), bulkChecked);

            settingsAdapter.notifyDataSetChanged();
            updateBulkCheckboxAndLabel(cbCheckSettingsBulk, tvSelectedSettingsLabel, config.settings.size(), SharedRegistry.STATE_TAG_SETTINGS);
        });

        ivExpanderConfigSettings.setOnClickListener(v -> {
            boolean newState = sharedRegistry.toggleExpanded(SharedRegistry.STATE_TAG_SETTINGS, "view_settings");
            updateExpanded(newState, ivExpanderConfigSettings, lvSettings);
        });

        // Hooks ListView setup
        ArrayAdapter<String> hooksAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_multiple_choice, config.hooks);
        lvHooks.setAdapter(hooksAdapter);

        // Set initial state for hooks (all checked by default)
        for (String hookId : config.hooks) {
            sharedRegistry.setChecked(SharedRegistry.STATE_TAG_HOOKS, hookId, true);
            int position = hooksAdapter.getPosition(hookId);
            if (position != -1)
                lvHooks.setItemChecked(position, true);
        }

        // Set the initial expanded state for hooks
        boolean isHooksExpanded = sharedRegistry.isExpanded(SharedRegistry.STATE_TAG_HOOKS, "view_hooks");
        updateExpanded(isHooksExpanded, ivExpanderConfigHooks, lvHooks);


        lvHooks.setOnItemClickListener((parent, v, position, id) -> {
            String hookId = config.hooks.get(position);
            boolean isChecked = lvHooks.isItemChecked(position);
            sharedRegistry.setChecked(SharedRegistry.STATE_TAG_HOOKS, hookId, isChecked);
            updateBulkCheckboxAndLabel(cbCheckHooksBulk, tvSelectedHooksLabel, config.hooks.size(), SharedRegistry.STATE_TAG_HOOKS);
        });

        cbCheckHooksBulk.setOnClickListener(v -> {
            boolean bulkChecked = cbCheckHooksBulk.isChecked();
            for (String hookId : config.hooks) {
                sharedRegistry.setChecked(SharedRegistry.STATE_TAG_HOOKS, hookId, bulkChecked);
                int position = hooksAdapter.getPosition(hookId);
                if (position != -1)
                    lvHooks.setItemChecked(position, bulkChecked);
            }

            hooksAdapter.notifyDataSetChanged();
            updateBulkCheckboxAndLabel(cbCheckHooksBulk, tvSelectedHooksLabel, config.hooks.size(), SharedRegistry.STATE_TAG_HOOKS);
        });

        ivExpanderConfigHooks.setOnClickListener(v -> {
            boolean newState = sharedRegistry.toggleExpanded(SharedRegistry.STATE_TAG_HOOKS, "view_hooks");
            updateExpanded(newState, ivExpanderConfigHooks, lvHooks);
        });

        if (config.hooks.isEmpty()) {
            ivExpanderConfigHooks.setEnabled(false);
            ivExpanderConfigHooks.setClickable(false);
            ivExpanderConfigHooks.setAlpha(0.3f);
            tvSelectedHooksLabel.setText("---");
        }

        // Initialize labels for both bulk checkboxes
        updateBulkCheckboxAndLabel(cbCheckSettingsBulk, tvSelectedSettingsLabel, config.settings.size(), SharedRegistry.STATE_TAG_SETTINGS);
        updateBulkCheckboxAndLabel(cbCheckHooksBulk, tvSelectedHooksLabel, config.hooks.size(), SharedRegistry.STATE_TAG_HOOKS);

        builder.setView(view)
                .setTitle(R.string.title_config_modify)
                .setNegativeButton(R.string.option_cancel, null)
                .setPositiveButton(R.string.option_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String configName = CoreUiUtils.getInputTextText(tiConfigName);
                        if(!Str.isEmpty(configName)) {
                            boolean isKnownConfig = configs.containsKey(configName);
                            XPConfig cfg = new XPConfig();
                            //if(isKnownConfig)
                            //    cfg = configs.get(configName);
                            //if(cfg == null)
                            //    cfg = new XPConfig();

                            cfg.name = configName;
                            cfg.author = CoreUiUtils.getInputTextText(tiConfigAuthor, Str.EMPTY);
                            cfg.version = CoreUiUtils.getInputTextText(tiConfigVersion, Str.EMPTY);

                            cfg.setTags(selectedTags);
                            cfg.settings.clear();
                            cfg.hooks.clear();

                            for(SettingPacket setting : config.settings)
                                if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getSharedId()))
                                    cfg.settings.add(setting);

                            for (String hookId : config.hooks)
                                if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_HOOKS, hookId))
                                    cfg.hooks.add(hookId);

                            A_CODE res = XPacket.push(Process.myUid(), cfg)
                                    .callRes(context, PutConfigCommand.COMMAND_NAME);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, "Sent Config: " + Str.noNL(cfg) + " Is Known: " + isKnownConfig + " Bundle Result=" + res.name());

                            /*try {
                                Fragment parentFragment = getParentFragment();
                                if (parentFragment instanceof ConfigDialog) {
                                    ((ConfigDialog) parentFragment).setConfigs(context);
                                    ((ConfigDialog) parentFragment).refreshConfigs();
                                }
                            }catch (Exception e) {
                                Log.e(TAG, "Error Updating dialog config list, error=" + e);
                            }*/

                            if(onFinish != null)
                                onFinish.onFinish(res, cfg);


                            // One final refresh after callback
                            /*try {
                                Fragment parentFragment = getParentFragment();
                                if (parentFragment instanceof ConfigDialog) {
                                    ((ConfigDialog) parentFragment).forceRefresh();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error doing final config list refresh", e);
                            }*/
                        }
                    }
                });

        AlertDialog dialog = builder.create();

        /*dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);


            // Set initial state for existing config
            boolean isExistingConfig = !Str.isEmpty(config.name) && configs.containsKey(config.name);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(isExistingConfig ?
                    R.string.option_update : R.string.option_create);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isExistingConfig);

            tiConfigName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                public void afterTextChanged(Editable s) {
                    String input = s.toString().trim();
                    boolean isKnownConfig = configs.containsKey(input);
                    boolean isEmpty = input.isEmpty();

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(isKnownConfig ?
                            R.string.option_update : R.string.option_create);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!isEmpty);

                    if (isKnownConfig) {
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setText(R.string.option_open);
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> openConfig(input));
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
                    }
                }
            });
        });*/

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);

            // Initial state update for configs loaded from file or existing configs
            if (!Str.isEmpty(config.name)) {
                updateButtonStates(dialog, config.name);
            }

            tiConfigName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                public void afterTextChanged(Editable s) {
                    String input = s.toString().trim();
                    updateButtonStates(dialog, input);
                }
            });
        });

        return dialog;
    }


    private void updateButtonStates(AlertDialog dialog, String configName) {
        boolean isKnownConfig = configs.containsKey(configName);
        boolean isEmpty = Str.isEmpty(configName);

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        positiveButton.setText(isKnownConfig ?
                R.string.option_update : R.string.option_create);
        positiveButton.setEnabled(!isEmpty);

        if (isKnownConfig) {
            neutralButton.setVisibility(View.VISIBLE);
            neutralButton.setText(R.string.option_open);
            neutralButton.setEnabled(true);
            neutralButton.setOnClickListener(v -> openConfig(configName));
        } else {
            neutralButton.setVisibility(View.GONE);
        }
    }

    private void openConfig(String configName) {
        // TODO: Implement config opening logic
        Log.d(TAG, "Opening config: " + configName);
    }

    private void updateExpanded(boolean isExpanded, ImageView ivExpander, ListView list) {
        CoreUiUtils.setViewsVisibility(ivExpander, isExpanded, list);
    }

    private void updateBulkCheckboxAndLabel(CheckBox checkbox, TextView label, int total, String tag) {
        int checked = 0;

        if (total == 0) {
            label.setText("---");
            checkbox.setChecked(false);
            checkbox.setButtonTintList(context.getResources().getColorStateList(android.R.color.darker_gray, null));
            return;
        }

        if (tag.equals(SharedRegistry.STATE_TAG_SETTINGS))
            for (SettingPacket setting : config.settings) {
                if (sharedRegistry.isChecked(tag, setting.getSharedId()))
                    checked++;
        } else {
            for (String hookId : config.hooks)
                if (sharedRegistry.isChecked(tag, hookId))
                    checked++;
        }

        if (checked == 0) {
            checkbox.setChecked(false);
            checkbox.setButtonTintList(context.getResources().getColorStateList(android.R.color.darker_gray, null));
        } else if (checked == total) {
            checkbox.setChecked(true);
            checkbox.setButtonTintList(context.getResources().getColorStateList(R.color.colorAccent, null));
        } else {
            checkbox.setChecked(true);
            checkbox.setButtonTintList(context.getResources().getColorStateList(android.R.color.darker_gray, null));
        }

        label.setText(String.format("%d/%d", checked, total));
    }

    private void setupFlexboxWithTags() {
        // Add the "+" button initially
        Button plusButton = createStyledPlusButton(); // Invoke the styled plus button creation
        plusButton.setOnClickListener(v -> openTagSelectionDialog());
        flexTags.addView(plusButton);
    }


    private void addPlusButton() {
        Button plusButton = createStyledButton("+");
        plusButton.setOnClickListener(v -> openTagSelectionDialog());
        flexTags.addView(plusButton);
    }

    private void openTagSelectionDialog() {
        OptionsListDialog.create()
                .setTitle(getString(R.string.title_select_tags))
                .setOptions(XPConfig.DEFAULT_TAGS)
                .setAllowMultiple(true)
                .setCheckStyleMaterial()
                .setChecked(selectedTags)
                .onConfirm((enabled, disabled) -> {
                    selectedTags.clear();
                    selectedTags.addAll(enabled);
                    updateFlexboxWithTags();
                }).show(getParentFragmentManager(), getString(R.string.title_select_tags));
    }

    private void updateFlexboxWithTags() {
        flexTags.removeAllViews();
        if(ListUtil.isValid(selectedTags)) {
            for (String tag : selectedTags) {
                Button tagButton = createStyledButton(tag);
                flexTags.addView(tagButton);
            }
        }

        addPlusButton();
    }

    private Button createStyledPlusButton() {
        Button button = new Button(context);
        button.setText("+");
        button.setBackgroundResource(R.drawable.rounded_corner);
        button.setPadding(15, 0, 15, 0); // Larger padding for the "+" button
        button.setTextSize(14); // Increased text size for better visibility
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(12, 12, 12, 12); // Equal spacing between buttons

        //params.height = 60; // Reduced height for the plus button
        //Does look nice at a RAW 60 Height
        params.height = CoreUiUtils.dpToPx(context, 30);    //Default is 50dp
        //.height is in "px" format though
        button.setLayoutParams(params);
        return button;
    }

    private Button createStyledButton(String text) {
        Button button = new Button(context);
        button.setText(text);
        button.setBackgroundResource(R.drawable.rounded_corner);
        button.setPadding(10, 0, 10, 0); // Padding for content inside the button
        button.setTextSize(10);
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(12, 12, 12, 12); // Equal spacing between buttons
        params.height = CoreUiUtils.dpToPx(context, 30);
        button.setLayoutParams(params);
        return button;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}