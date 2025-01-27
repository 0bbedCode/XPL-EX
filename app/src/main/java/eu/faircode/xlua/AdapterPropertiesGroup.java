package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import eu.faircode.xlua.api.properties.MockPropGroupHolder;
import eu.faircode.xlua.api.properties.MockPropSetting;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.settings.LuaSettingPacket;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.random.GlobalRandoms;
import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.randomizers.NARandomizer;
import eu.faircode.xlua.ui.dialogs.NoRandomDialog;
import eu.faircode.xlua.ui.transactions.PropTransactionResult;
import eu.faircode.xlua.ui.PropertyQue;
import eu.faircode.xlua.ui.dialogs.PropertyAddDialogEx;
import eu.faircode.xlua.ui.dialogs.SettingDeleteDialogEx;
import eu.faircode.xlua.ui.interfaces.ILoader;
import eu.faircode.xlua.ui.interfaces.IPropertyUpdate;
import eu.faircode.xlua.ui.interfaces.ISettingUpdateEx;
import eu.faircode.xlua.ui.transactions.SettingTransactionResult;
import eu.faircode.xlua.ui.SettingsQue;
import eu.faircode.xlua.utilities.SettingUtil;
import eu.faircode.xlua.utilities.StringUtil;
import eu.faircode.xlua.utilities.UiUtil;
import eu.faircode.xlua.utilities.ViewUtil;

public class AdapterPropertiesGroup extends RecyclerView.Adapter<AdapterPropertiesGroup.ViewHolder> implements Filterable {
    private final List<IRandomizerOld> randomizers = GlobalRandoms.getRandomizers();

    private final List<MockPropGroupHolder> groups = new ArrayList<>();
    private List<MockPropGroupHolder> filtered = new ArrayList<>();

    private final HashMap<String, Boolean> expanded = new HashMap<>();

    private boolean dataChanged = false;
    private CharSequence query = null;

    private ILoader fragmentLoader;
    private SettingsQue settingsQue;
    private PropertyQue propertyQue;

    public class ViewHolder extends RecyclerView.ViewHolder
            implements
            View.OnClickListener,
            View.OnLongClickListener,
            TextWatcher,
            View.OnTouchListener,
            AdapterView.OnItemSelectedListener,
            ISettingUpdateEx,
            IPropertyUpdate {

        final View itemView;
        final TextView tvSettingName, tvSettingNameFull, tvSettingDescription;
        final TextInputEditText tiSettingValue;
        final ImageView ivBtSave, ivBtDelete, ivBtRandomize, ivBtReset, ivDropDown, ivBtAddProperty;

        final AdapterProperty adapterProps;
        final RecyclerView rvGroupProps;

        final Spinner spRandomSelector;
        final ArrayAdapter<IRandomizerOld> spRandomizer;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            ivDropDown = itemView.findViewById(R.id.ivSettingDropDown);
            tvSettingName = itemView.findViewById(R.id.tvSettingName);
            tvSettingNameFull = itemView.findViewById(R.id.tvGroupPropertiesSettingFullName);
            tvSettingDescription = itemView.findViewById(R.id.tvSettingDescriptionFromProperties);
            tiSettingValue = itemView.findViewById(R.id.tiSettingValueFromProperties);

            ivBtSave = itemView.findViewById(R.id.ivBtSaveSettingFromProperties);
            ivBtDelete = itemView.findViewById(R.id.ivBtDeleteSettingFromProperties);
            ivBtRandomize = itemView.findViewById(R.id.ivBtRandomSettingValueFromProperties);
            ivBtReset = itemView.findViewById(R.id.ivBtResetSettingValueFromProperties);
            ivBtAddProperty = itemView.findViewById(R.id.ivBtAddPropertyToSettingsGroup);

            //Start of Drop Down
            spRandomizer = new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_spinner_item);
            spRandomizer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spRandomSelector = itemView.findViewById(R.id.spSettingRandomizerSpinnerProperties);

            spRandomSelector.setTag(null);
            spRandomSelector.setAdapter(spRandomizer);

            spRandomizer.clear();
            spRandomizer.addAll(GlobalRandoms.getRandomizers());
            //Init settings Adapter
            rvGroupProps = itemView.findViewById(R.id.rvGroupProperties);
            rvGroupProps.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(itemView.getContext());
            llm.setAutoMeasureEnabled(true);
            rvGroupProps.setLayoutManager(llm);
            adapterProps = new AdapterProperty(fragmentLoader, propertyQue);
            rvGroupProps.setAdapter(adapterProps);
        }


        @SuppressLint("ClickableViewAccessibility")
        private void unWire() {
            itemView.setOnClickListener(null);
            tiSettingValue.removeTextChangedListener(this);
            ivDropDown.setOnClickListener(null);
            ivBtDelete.setOnClickListener(null);
            ivBtRandomize.setOnClickListener(null);
            ivBtSave.setOnClickListener(null);
            ivBtReset.setOnClickListener(null);
            ivBtAddProperty.setOnClickListener(null);
            ivBtAddProperty.setOnLongClickListener(null);
            ivBtReset.setOnLongClickListener(null);
            ivBtSave.setOnLongClickListener(null);
            ivBtDelete.setOnLongClickListener(null);
            ivBtRandomize.setOnLongClickListener(null);
            spRandomSelector.setOnItemSelectedListener(null);

        }

        @SuppressLint("ClickableViewAccessibility")
        private void wire() {
            itemView.setOnClickListener(this);
            tiSettingValue.addTextChangedListener(this);
            ivDropDown.setOnClickListener(this);
            ivBtDelete.setOnClickListener(this);
            ivBtRandomize.setOnClickListener(this);
            ivBtSave.setOnClickListener(this);
            ivBtReset.setOnClickListener(this);
            ivBtAddProperty.setOnClickListener(this);
            ivBtAddProperty.setOnLongClickListener(this);
            ivBtReset.setOnLongClickListener(this);
            ivBtSave.setOnLongClickListener(this);
            ivBtDelete.setOnLongClickListener(this);
            ivBtRandomize.setOnLongClickListener(this);
            spRandomSelector.setOnItemSelectedListener(this);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(final View view) {
            int id = view.getId();
            XLog.i("onClick id=" + id);
            try {
                int position = getAdapterPosition();
                MockPropGroupHolder group = filtered.get(position);
                LuaSettingExtended setting = group.getSetting();
                String name = group.getSettingName();
                XLog.i("selected=" + group);

                switch (id) {
                    case R.id.ivSettingDropDown:
                    case R.id.itemViewPropGroup:
                        ViewUtil.internalUpdateExpanded(expanded, name);
                        updateExpanded();
                        break;
                    case R.id.ivBtDeleteSettingFromProperties:
                        new SettingDeleteDialogEx()
                                .setAdapterPosition(position)
                                .setApplication(fragmentLoader.getApplication())
                                .setSetting(setting)
                                .setSettingsQue(settingsQue)
                                .setCallback(this)
                                .show(fragmentLoader.getManager(),  view.getContext().getString(R.string.title_delete_setting));
                        break;
                    case R.id.ivBtRandomSettingValueFromProperties:
                        if(NARandomizer.isNA(setting.getRandomizer()))
                            new NoRandomDialog()
                                    .show(fragmentLoader.getManager(),
                                            view.getResources().getString(R.string.title_no_random));
                        else setting.randomizeValue(view.getContext());
                        break;
                    case R.id.ivBtSaveSettingFromProperties:
                        settingsQue.updateSetting(
                                view.getContext(),
                                setting, position,
                                true,
                                false,
                                fragmentLoader.getApplication().getForceStop(),
                                this);
                        break;
                    case R.id.ivBtResetSettingValueFromProperties:
                        if(setting.isModified()) setting.resetModified(true);
                        break;
                    case R.id.ivBtAddPropertyToSettingsGroup:
                        new PropertyAddDialogEx()
                                .setCallback(this)
                                .setSettingName(setting.getName())
                                .setPropertyQue(propertyQue)
                                .show(Objects.requireNonNull(fragmentLoader.getManager()), view.getContext().getString(R.string.title_add_property));
                        break;

                }
            }catch (Exception e) {  XLog.e("onClick: Failed! code=" + id, e, true); }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onLongClick(View v) {
            int id = v.getId();
            XLog.i("onLongClick id=" + id);
            try {
                switch (id) {
                    case R.id.ivBtDeleteSettingFromProperties:
                        Snackbar.make(v, R.string.menu_setting_delete_hint, Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.ivBtRandomSettingValueFromProperties:
                        Snackbar.make(v, R.string.menu_setting_random_hint, Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.ivBtSaveSettingFromProperties:
                        Snackbar.make(v, R.string.menu_setting_save_hint, Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.ivBtResetSettingValueFromProperties:
                        Snackbar.make(v, R.string.menu_setting_reset_hint, Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.ivBtAddPropertyToSettingsGroup:
                        Snackbar.make(v, R.string.menu_setting_add_prop_hint, Snackbar.LENGTH_LONG).show();
                        break;

                }
            }catch (Exception e) {  XLog.e("onClick: Failed! code=" + id, e, true); return false; }
            return true;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            MockPropGroupHolder group = filtered.get(getAdapterPosition());
            LuaSettingExtended setting = group.getSetting();
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
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updateSelection();  }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { updateSelection(); }

        private void updateSelection() {
            MockPropGroupHolder group = filtered.get(getAdapterPosition());
            UiUtil.handleSpinnerSelection(spRandomSelector, group.getSetting());
        }

        void updateExpanded() {
            MockPropGroupHolder group = filtered.get(getAdapterPosition());
            String name = group.getSettingName();
            boolean isExpanded = expanded.containsKey(name) && Boolean.TRUE.equals(expanded.get(name));
            ViewUtil.setViewsVisibility(ivDropDown, isExpanded, tiSettingValue, rvGroupProps, ivBtAddProperty, ivBtDelete, ivBtRandomize, ivBtReset, ivBtSave, tvSettingDescription, spRandomSelector);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
            }

            return false;
        }

        @SuppressLint("NotifyDataSetChanged")
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
                }
            }catch (Exception e) { XLog.e("Failed to provide Update", e, true); }
        }

        @Override
        public void onPropertyUpdate(PropTransactionResult result) {
            if(result != null && result.hasAnySucceeded() && fragmentLoader != null) {
                Snackbar.make(itemView, R.string.result_property_added_success, Snackbar.LENGTH_LONG).show();
                fragmentLoader.loadData();
            }else Snackbar.make(itemView, R.string.result_property_added_failed, Snackbar.LENGTH_LONG).show();
        }
    }

    AdapterPropertiesGroup() { setHasStableIds(true); }
    AdapterPropertiesGroup(ILoader loader) { this(); this.fragmentLoader = loader; this.propertyQue = new PropertyQue(loader.getApplication()); this.settingsQue = new SettingsQue(loader.getApplication()); }

    void set(List<MockPropGroupHolder> groups, Context context) {
        for (int i = groups.size() - 1; i >= 0; i--) {
            MockPropGroupHolder holder = groups.get(i);
            LuaSettingExtended setting = holder.getSetting();
            if(setting == null) {
                XLog.w("Setting from Mock Prop Group is NULL. Make sure you linked it to a VALID setting. Creating....");
                AppGeneric app = fragmentLoader.getApplication();
                setting = new LuaSettingExtended(app.getUid(), app.getPackageName(), holder.getSettingName(), "", "", "", true);
                settingsQue.updateSetting(context, setting, -1, true, true, false, null);
                holder.setSetting(setting);
            }

            setting.resetModified();
            setting.bindRandomizer(randomizers);
        }


        this.dataChanged = true;
        this.groups.clear();
        this.groups.addAll(groups);
        XLog.i("<set> Prop Group Count=" + this.groups.size());
        getFilter().filter(query);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            private boolean expanded1 = false;

            @Override
            protected FilterResults performFiltering(CharSequence query) {
                AdapterPropertiesGroup.this.query = query;
                List<MockPropGroupHolder> visible = new ArrayList<>(groups);
                List<MockPropGroupHolder> results = new ArrayList<>();
                if (!StringUtil.isValidAndNotWhitespaces(query)) results.addAll(visible);
                else {
                    String q = query.toString().toLowerCase().trim();
                    for(MockPropGroupHolder p : visible) {
                        if(p.getSettingName().toLowerCase().contains(q)) results.add(p);
                        else if(p.getValue() != null && p.getValue().toLowerCase().contains(q)) results.add(p);
                        else if(SettingUtil.cleanSettingName(p.getSettingName()).toLowerCase().contains(q)) results.add(p);
                        else {
                            for(MockPropSetting s : p.getProperties()) {
                                if(s.getName().toLowerCase().contains(q)) {
                                    results.add(p);
                                    break;
                                }
                            }
                        }
                    }
                }

                if (results.size() == 1) {
                    String settingName = results.get(0).getSettingName();
                    if (!expanded.containsKey(settingName)) {
                        expanded1 = true;
                        expanded.put(settingName, true);
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = results;
                filterResults.count = results.size();
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence query, FilterResults result) {
                final List<MockPropGroupHolder> groups = (result.values == null ? new ArrayList<MockPropGroupHolder>() : (List<MockPropGroupHolder>) result.values);
                XLog.i("Filtered Groups size=" + groups.size());
                if(dataChanged) {
                    dataChanged = false;
                    filtered = groups;
                    notifyDataSetChanged();
                }else {
                    DiffUtil.DiffResult diff =
                            DiffUtil.calculateDiff(new PropertiesDiffCallback(expanded1, filtered, groups));
                    filtered = groups;
                    diff.dispatchUpdatesTo(AdapterPropertiesGroup.this);
                }
            }
        };
    }

    private static class PropertiesDiffCallback extends DiffUtil.Callback {
        private final boolean refresh;
        private final List<MockPropGroupHolder> prev;
        private final List<MockPropGroupHolder> next;

        PropertiesDiffCallback(boolean refresh, List<MockPropGroupHolder> prev, List<MockPropGroupHolder> next) {
            this.refresh = refresh;
            this.prev = prev;
            this.next = next;
        }

        @Override
        public int getOldListSize() {
            return prev.size();
        }

        @Override
        public int getNewListSize() {
            return next.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            MockPropGroupHolder g1 = prev.get(oldItemPosition);
            MockPropGroupHolder g2 = next.get(newItemPosition);
            return (!refresh && g1.getSettingName().equalsIgnoreCase(g2.getSettingName()));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            MockPropGroupHolder g1 = prev.get(oldItemPosition);
            MockPropGroupHolder g2 = next.get(newItemPosition);

            if(!g1.getSettingName().equalsIgnoreCase(g2.getSettingName()))
                return false;

            return true;
        }
    }

    @Override
    public long getItemId(int position) { return filtered.get(position).hashCode(); }

    @Override
    public int getItemCount() { return filtered.size(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.propgroup, parent, false)); }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.unWire();
        MockPropGroupHolder group = filtered.get(position);
        String sName = group.getSettingName();
        group.getSetting().bindInputTextBox(holder.tiSettingValue, holder);

        holder.tvSettingName.setText(SettingUtil.cleanSettingName(sName));
        holder.tvSettingNameFull.setText(sName);
        holder.tiSettingValue.setText(group.getValue());
        holder.tvSettingDescription.setText(group.getDescription());
        holder.adapterProps.set(group.getProperties());

        boolean enable = UiUtil.initRandomizer(holder.spRandomizer, holder.spRandomSelector, group.getSetting(), randomizers);
        holder.spRandomSelector.setEnabled(enable);
        holder.ivBtRandomize.setEnabled(enable);
        group.getSetting().setInputText();

        holder.updateExpanded();
        holder.wire();
    }
}
