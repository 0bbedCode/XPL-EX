package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.settings.LuaSettingPacket;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.random.GlobalRandoms;
import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.randomizers.NARandomizer;
import eu.faircode.xlua.ui.dialogs.NoRandomDialog;
import eu.faircode.xlua.ui.dialogs.SettingDeleteDialogEx;
import eu.faircode.xlua.ui.interfaces.ILoader;
import eu.faircode.xlua.ui.SettingsQue;
import eu.faircode.xlua.ui.interfaces.ISettingUpdateEx;
import eu.faircode.xlua.ui.transactions.SettingTransactionResult;
import eu.faircode.xlua.utilities.SettingUtil;
import eu.faircode.xlua.utilities.UiUtil;
import eu.faircode.xlua.utilities.ViewUtil;

public class AdapterHookSettings extends RecyclerView.Adapter<AdapterHookSettings.ViewHolder> {
    private final List<IRandomizerOld> randomizers = GlobalRandoms.getRandomizers();
    private final List<LuaSettingExtended> settings = new ArrayList<>();
    private final HashMap<String, Boolean> expanded = new HashMap<>();

    private SettingsQue settingsQue;
    private ILoader fragmentLoader;

    public class ViewHolder extends RecyclerView.ViewHolder
            implements
            CompoundButton.OnCheckedChangeListener,
            View.OnClickListener,
            View.OnLongClickListener,
            TextWatcher,
            AdapterView.OnItemSelectedListener,
            ISettingUpdateEx {

        final View view;
        final TextView tvSettingName, tvSettingNameFull, tvSettingDescription;
        final TextInputEditText tiSettingValue;
        final ImageView btRandomize, btReset, btSave, btDelete, ivExpander;
        final Spinner spRandomSelector;
        final ArrayAdapter<IRandomizerOld> adapterRandomizer;

        ViewHolder(View itemView) {
            super(itemView);

            this.view = itemView;
            this.tvSettingName = view.findViewById(R.id.tvHookSettingName);
            this.tvSettingNameFull = view.findViewById(R.id.tvHookSettingFullName);
            this.tvSettingDescription = view.findViewById(R.id.tvHookSettingDescription);
            this.tiSettingValue = view.findViewById(R.id.tvHookSettingValue);
            this.ivExpander = view.findViewById(R.id.ivExpanderSettingValue);
            this.btSave = view.findViewById(R.id.ivBtHookSettingDelete);
            this.btDelete = view.findViewById(R.id.ivBtHookSettingSave);
            this.btReset = view.findViewById(R.id.ivBtHookSettingReset);

            this.btRandomize = view.findViewById(R.id.ivBtHookSettingRandomize);
            this.spRandomSelector = view.findViewById(R.id.spHookSettingRandomizer);

            this.adapterRandomizer = new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_spinner_item);
            this.adapterRandomizer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spRandomSelector.setTag(null);
            this.spRandomSelector.setAdapter(adapterRandomizer);
        }

        @SuppressLint("ClickableViewAccessibility")
        private void unWire() {
            //this.view.setOnClickListener(null);
            this.tvSettingName.setOnClickListener(null);
            this.btSave.setOnClickListener(null);
            this.btSave.setOnLongClickListener(null);
            this.btReset.setOnClickListener(null);
            this.btReset.setOnLongClickListener(null);
            this.btRandomize.setOnClickListener(null);
            this.btRandomize.setOnLongClickListener(null);
            this.btDelete.setOnClickListener(null);
            this.btDelete.setOnLongClickListener(null);
            this.ivExpander.setOnClickListener(null);
            this.tvSettingName.setOnLongClickListener(null);
            this.tiSettingValue.removeTextChangedListener(this);
            this.spRandomSelector.setOnItemSelectedListener(null);
        }

        @SuppressLint("ClickableViewAccessibility")
        private void wire() {
            //this.view.setOnClickListener(this);
            this.tvSettingName.setOnClickListener(this);
            this.btSave.setOnClickListener(this);
            this.btSave.setOnLongClickListener(this);
            this.btReset.setOnClickListener(this);
            this.btReset.setOnLongClickListener(this);
            this.btRandomize.setOnClickListener(this);
            this.btRandomize.setOnLongClickListener(this);
            this.btDelete.setOnClickListener(this);
            this.btDelete.setOnLongClickListener(this);
            this.ivExpander.setOnClickListener(this);
            this.tvSettingName.setOnLongClickListener(this);
            this.tiSettingValue.addTextChangedListener(this);
            this.spRandomSelector.setOnItemSelectedListener(this);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(final View view) {
            int code = view.getId();
            XLog.i("onClick id=" + code);
            try {
                final int pos = getAdapterPosition();
                final LuaSettingExtended setting = settings.get(pos);
                switch (code) {
                    case R.id.itemViewHookSettings:
                    case R.id.ivExpanderSettingValue:
                    case R.id.tvHookSettingName:
                        ViewUtil.internalUpdateExpanded(expanded, setting.getName());
                        updateExpanded();
                        break;
                    case R.id.ivBtHookSettingDelete:
                        new SettingDeleteDialogEx()
                                .setAdapterPosition(pos)
                                .setApplication(fragmentLoader.getApplication())
                                .setSetting(setting)
                                .setSettingsQue(settingsQue)
                                .setCallback(this)
                                .show(fragmentLoader.getManager(),  view.getContext().getString(R.string.title_delete_setting));
                        break;
                    case R.id.ivBtHookSettingSave:
                        settingsQue.updateSetting(
                                view.getContext(),
                                setting,
                                pos,
                                true,
                                false,
                                fragmentLoader.getApplication().getForceStop(),
                                this);
                        break;
                    case R.id.ivBtHookSettingReset:
                        if(setting.isModified()) setting.resetModified(true);
                        break;
                    case R.id.ivBtHookSettingRandomize:
                        if(NARandomizer.isNA(setting.getRandomizer()))
                            new NoRandomDialog()
                                    .show(fragmentLoader.getManager(),
                                            view.getResources().getString(R.string.title_no_random));
                        else setting.randomizeValue(view.getContext());
                        break;
                }
            }catch (Exception e) { XLog.e("onClick Failed: code=" + code, e, true); }
        }

        @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
        @Override
        public void onCheckedChanged(final CompoundButton cButton, final boolean isChecked) {  }

        void updateExpanded() {
            LuaSettingExtended setting = settings.get(getAdapterPosition());
            String name = setting.getName();
            boolean isExpanded = expanded.containsKey(name) && Boolean.TRUE.equals(expanded.get(name));
            int rotation = isExpanded ? 87 : 0;
            ivExpander.setRotation(rotation);
            ViewUtil.setViewsVisibility(null, isExpanded, tiSettingValue, btDelete, btRandomize, btReset, btSave, spRandomSelector, tvSettingDescription);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onLongClick(View v) {
            int code = view.getId();
            XLog.i("onLongClick id=" + code);
            try {
                switch (code) {
                    case R.id.ivBtHookSettingDelete:
                        Toast.makeText(v.getContext(), R.string.menu_setting_delete_hint, Toast.LENGTH_LONG).show();
                        break;
                    case R.id.ivBtHookSettingSave:
                        Toast.makeText(v.getContext(), R.string.menu_setting_save_hint, Toast.LENGTH_LONG).show();
                        break;
                    case R.id.ivBtHookSettingReset:
                        Toast.makeText(v.getContext(), R.string.menu_setting_reset_hint, Toast.LENGTH_LONG).show();
                        break;
                    case R.id.ivBtHookSettingRandomize:
                        Toast.makeText(v.getContext(), R.string.menu_setting_random_hint, Toast.LENGTH_LONG).show();
                        break;
                }
            }catch (Exception e) { XLog.e("onLongClick Failed: code=" + code, e); }
            return true;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updateSelection(); }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { updateSelection(); }

        private void updateSelection() { UiUtil.handleSpinnerSelection(spRandomSelector, settings, getAdapterPosition()); }

        @Override
        public void afterTextChanged(Editable editable) {
            LuaSettingExtended setting = settings.get(getAdapterPosition());
            if(!setting.isBusy()) {
                String s = editable.toString();
                if(TextUtils.isEmpty(s)) setting.setModifiedValue(null);
                else setting.setModifiedValue(editable.toString());
            }
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onSettingUpdate(SettingTransactionResult result) {
            try {
                Toast.makeText(result.context, result.result.getResultMessage(), Toast.LENGTH_SHORT).show();
                LuaSettingExtended setting = result.getSetting();
                setting.setIsBusy(false);
                if(result.hasAnySucceeded()) {
                    LuaSettingPacket packet = result.getPacket();
                    if(packet.isDeleteDefault()) {
                        fragmentLoader.loadData();
                        return;
                    }

                    if(packet.isDeleteSetting()) setting.setModifiedValueToNull().setValueToNull();
                    setting.updateValue(true);
                    fragmentLoader.loadData();//we should force load either way since other elemens can have the same setting ??
                }
            }catch (Exception e) { XLog.e("Failed to provide setting Update", e, true); }
        }
    }

    AdapterHookSettings() { setHasStableIds(true); }
    AdapterHookSettings(ILoader loader) { this(); this.fragmentLoader = loader; this.settingsQue = new SettingsQue(loader.getApplication()); }

    public void set(List<LuaSettingExtended> settings) {
        if(!settings.isEmpty()) {
            for(LuaSettingExtended s : settings) {
                s.resetModified();
                s.bindRandomizer(randomizers);
            }

            this.settings.clear();
            this.settings.addAll(settings);
        }
    }

    @Override
    public long getItemId(int position) { return settings.get(position).hashCode(); }

    @Override
    public int getItemCount() { return settings.size(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.hooksetting, parent, false)); }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.unWire();
        final LuaSettingExtended setting = settings.get(position);
        setting.bindInputTextBox(holder.tiSettingValue);
        holder.tvSettingName.setText(SettingUtil.cleanSettingName(setting.getName()));
        holder.tvSettingNameFull.setText(setting.getName());
        holder.tvSettingDescription.setText(SettingUtil.generateDescription(setting));
        boolean enable = UiUtil.initRandomizer(holder.adapterRandomizer, holder.spRandomSelector, setting, randomizers);
        holder.spRandomSelector.setEnabled(enable);
        holder.btRandomize.setEnabled(enable);
        holder.tiSettingValue.setEnabled(!setting.getName().endsWith(".bool"));
        setting.setInputText();
        holder.updateExpanded();
        holder.wire();
    }
}
