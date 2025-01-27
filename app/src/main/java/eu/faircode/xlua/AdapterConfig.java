package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.configs.MockConfig;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.settings.LuaSettingPacket;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.GlobalRandoms;
import eu.faircode.xlua.random.randomizers.NARandomizer;
import eu.faircode.xlua.ui.ViewFloatingAction;
import eu.faircode.xlua.ui.dialogs.NoRandomDialog;
import eu.faircode.xlua.ui.interfaces.ILoader;
import eu.faircode.xlua.utilities.SettingUtil;
import eu.faircode.xlua.utilities.UiUtil;
import eu.faircode.xlua.utilities.ViewUtil;

public class AdapterConfig extends RecyclerView.Adapter<AdapterConfig.ViewHolder> {
    private final List<IRandomizerOld> randomizers = GlobalRandoms.getRandomizers();
    private final List<LuaSettingExtended> settings = new ArrayList<>();
    private final HashMap<String, Boolean> expanded = new HashMap<>();

    private ILoader fragmentLoader;
    private MockConfig config = null;

    public class ViewHolder
            extends
            RecyclerView.ViewHolder
            implements
            AdapterView.OnItemSelectedListener,
            CompoundButton.OnCheckedChangeListener,
            View.OnClickListener,
            View.OnLongClickListener,
            TextWatcher {

        final View itemView;
        final TextView tvSettingName, tvDescription, tvSettingNameFull;
        final TextInputEditText tiSettingsValue;
        final CheckBox cbEnable;
        final ImageView ivBtRandom, ivBtReset, ivExpanderSettings;
        final Spinner spRandomSelector;
        final ArrayAdapter<IRandomizerOld> spRandomizer;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            tvSettingName = itemView.findViewById(R.id.tvSettingConfigName);
            tvSettingNameFull = itemView.findViewById(R.id.tvConfigSettingFullName);
            tvDescription = itemView.findViewById(R.id.tvConfigSettingDescription);
            tiSettingsValue = itemView.findViewById(R.id.tiConfigSettingsValue);
            cbEnable = itemView.findViewById(R.id.cbEnableConfigSetting);

            ivBtRandom = itemView.findViewById(R.id.ivBtRandomConfigSettingValue);
            ivBtReset = itemView.findViewById(R.id.ivBtResetConfigSettingValue);
            ivExpanderSettings = itemView.findViewById(R.id.ivSettingConfigExpander);

            spRandomSelector = itemView.findViewById(R.id.spConfigRandomSelection);
            spRandomizer = new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_spinner_item);
            initDropDown();
        }

        public void initDropDown() {
            spRandomizer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spRandomSelector.setTag(null);
            spRandomSelector.setAdapter(spRandomizer);
            spRandomizer.clear();
            spRandomizer.addAll(randomizers);
        }

        private void unWire() {
            itemView.setOnClickListener(null);
            cbEnable.setOnCheckedChangeListener(null);
            ivBtRandom.setOnClickListener(null);
            ivBtReset.setOnClickListener(null);
            spRandomSelector.setOnItemSelectedListener(null);
            ivBtReset.setOnLongClickListener(null);
            ivBtRandom.setOnLongClickListener(null);
            tiSettingsValue.removeTextChangedListener(this);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            cbEnable.setOnCheckedChangeListener(this);
            ivBtRandom.setOnClickListener(this);
            ivBtReset.setOnClickListener(this);
            spRandomSelector.setOnItemSelectedListener(this);
            ivBtReset.setOnLongClickListener(this);
            ivBtRandom.setOnLongClickListener(this);
            tiSettingsValue.addTextChangedListener(this);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(final View view) {

            Log.i("XLua.AdapterConfig", "IsOpen=" + vf.isOpen() + " IsScrollable=" + vf.isRecyclerScrollable() + " IsActionOpen=" + vf.isActionOpen() + " IsMainHidden="+ vf.isMainHidden());

            //if(!vf.isOpen() && !vf.isRecyclerScrollable())
            //    vf.invokeFloatingActions();

            vf.handleFloatingActions();

            int id = view.getId();
            XLog.i("onClick id=" + id);
            final LuaSettingExtended setting = settings.get(getAdapterPosition());
            String name = setting.getName();
            switch (id) {
                case R.id.itemViewConfig:
                    ViewUtil.internalUpdateExpanded(expanded, name);
                    updateExpanded();
                    break;
                case R.id.ivBtRandomConfigSettingValue:
                    if(NARandomizer.isNA(setting.getRandomizer()))
                        new NoRandomDialog()
                                .show(fragmentLoader.getManager(),
                                        view.getResources().getString(R.string.title_no_random));
                    else setting.randomizeValue(view.getContext());
                    break;
                case R.id.ivBtResetConfigSettingValue:
                    if(setting.isModified()) setting.resetModified(true);
                    break;
            }
        }

        @SuppressLint({"NotifyDataSetChanged", "NonConstantResourceId"})
        @Override
        public void onCheckedChanged(final CompoundButton cButton, boolean isChecked) {
            int id = cButton.getId();
            XLog.i("onCheckedChanged id=" + id);
            int pos = getAdapterPosition();
            LuaSettingExtended setting = settings.get(pos);
            switch (id) {
                case R.id.cbEnableConfigSetting:
                    setting.setIsEnabled(isChecked);
                    config.setSettings(settings);
                    notifyItemChanged(pos);
                    break;
            }
        }

        void updateExpanded() {
            LuaSettingExtended setting = settings.get(getAdapterPosition());
            String name = setting.getName();
            boolean isExpanded = expanded.containsKey(name) && Boolean.TRUE.equals(expanded.get(name));
            ViewUtil.setViewsVisibility(ivExpanderSettings, isExpanded, tiSettingsValue, spRandomSelector, ivBtRandom, tvDescription, ivBtReset);
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updateSelection();  }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { updateSelection(); }

        private void updateSelection() { UiUtil.handleSpinnerSelection(spRandomSelector, settings.get(getAdapterPosition())); }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable editable) {
            LuaSettingExtended setting = settings.get(getAdapterPosition());
            if(!setting.isBusy()) {
                String s = editable.toString();
                if(TextUtils.isEmpty(s)) setting.setModifiedValue(null);
                else setting.setModifiedValue(editable.toString());
            }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onLongClick(View v) {
            int id = v.getId();
            XLog.i("onLongClick id=" + id);
            switch (id) {
                case R.id.ivBtRandomConfigSettingValue:
                    Snackbar.make(v, R.string.menu_setting_random_hint, Snackbar.LENGTH_LONG).show();
                    break;
                case R.id.ivBtResetConfigSettingValue:
                    Snackbar.make(v, R.string.menu_setting_reset_hint, Snackbar.LENGTH_LONG).show();
                    break;
            }

            return true;
        }
    }

    private ViewFloatingAction vf;

    AdapterConfig() { setHasStableIds(true); }
    AdapterConfig(ILoader fragmentLoader, ViewFloatingAction vf) { this(); this.fragmentLoader = fragmentLoader; this.vf = vf; }

    void applyConfig(Context context) {
        config.saveValuesFromInput();
        XLog.i("Applying config setting size=" + settings.size());
        int failed = 0;
        int succeeded = 0;

        for(LuaSettingExtended setting : settings) {
            if(setting.isEnabled()) {
                setting.updateValue(true);
                LuaSettingPacket packet = LuaSettingPacket
                        .create(setting, LuaSettingPacket.CODE_INSERT_UPDATE_SETTING, false)
                        .copyIdentification(fragmentLoader.getApplication());
                XResult res = XLuaCall.sendMockSetting(context, packet);
                if(res.succeeded())
                    succeeded++;
                else {
                    XLog.i("Failed to send setting over bridge: " + packet + " msg=" + res.getFullMessage());
                    failed++;
                }
            }
        }

        Toast.makeText(context, "settings applied=" + succeeded + " failed=" + failed, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("NotifyDataSetChanged")
    void set(MockConfig config) {
        XLog.i("Settings Count=" + config.getSettings().size() + " Config Name=" + config.getName());
        for(LuaSettingExtended setting : config.getSettings()) {
            setting.resetModified();
            setting.bindRandomizer(randomizers);
        }

        this.config = config;
        this.settings.clear();
        this.settings.addAll(config.getSettings());
        notifyDataSetChanged();
    }

    public MockConfig getConfig() {  config.setSettings(this.settings); return config; }
    public String getConfigName() { return config.getName(); }
    public List<LuaSettingExtended> getSettings() { return this.settings; }
    public List<LuaSettingExtended> getEnabledSettings() {
        List<LuaSettingExtended> settingsEnabled = new ArrayList<>();
        for(LuaSettingExtended setting : settings) if(setting.isEnabled()) settingsEnabled.add(setting);
        return settingsEnabled;
    }

    @Override
    public long getItemId(int position) { return settings.get(position).hashCode(); }

    @Override
    public int getItemCount() { return settings.size(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.configsetting, parent, false)); }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.unWire();
        LuaSettingExtended setting = settings.get(position);
        setting.bindInputTextBox(holder.tiSettingsValue, holder);
        String settingName = setting.getName();
        holder.tvSettingName.setText(SettingUtil.cleanSettingName(settingName));
        holder.tvSettingNameFull.setText(setting.getName());
        holder.tiSettingsValue.setText(setting.getValue());
        holder.tvDescription.setText(SettingUtil.generateDescription(setting));
        holder.cbEnable.setChecked(setting.isEnabled());
        boolean enable = UiUtil.initRandomizer(holder.spRandomizer, holder.spRandomSelector, setting, randomizers);
        holder.spRandomSelector.setEnabled(enable);
        holder.ivBtRandom.setEnabled(enable);
        setting.setInputText();
        holder.updateExpanded();
        holder.wire();
    }
}
