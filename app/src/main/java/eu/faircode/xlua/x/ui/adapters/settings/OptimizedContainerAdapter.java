package eu.faircode.xlua.x.ui.adapters.settings;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.databinding.SettingsExItemContainerBinding;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.util.UiLog;
import eu.faircode.xlua.x.ui.core.util.UiRandomUtils;
import eu.faircode.xlua.x.ui.core.view_registry.ChangedStatesPacket;
import eu.faircode.xlua.x.ui.core.view_registry.IStateChanged;
import eu.faircode.xlua.x.ui.core.view_registry.SettingSharedRegistry;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.core.view_registry.CheckBoxState;
import eu.faircode.xlua.x.ui.core.adapter.EnhancedListAdapter;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;
import eu.faircode.xlua.x.ui.core.interfaces.IGenericElementEvent;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.ui.dialogs.HooksDialog;
import eu.faircode.xlua.x.ui.dialogs.SettingDeleteDialog;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.hook.data.AssignmentData;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.test.EventTrigger;
import eu.faircode.xlua.x.xlua.settings.test.SharedViewControl;
import eu.faircode.xlua.x.xlua.settings.test.interfaces.IUIViewControl;


public class OptimizedContainerAdapter
        extends EnhancedListAdapter<SettingsContainer, SettingsExItemContainerBinding, OptimizedContainerAdapter.ContainerViewHolder> {

    private UserClientAppContext userContext;
    public OptimizedContainerAdapter(Context context,
                                     IGenericElementEvent<SettingsContainer, SettingsExItemContainerBinding> events,
                                     IStateManager stateManager,
                                     UserClientAppContext userContext) {
        super(context, events, stateManager, null);
        this.userContext = userContext;
        this.userContext.bindShared(stateManager.getSharedRegistry());
    }

    @NonNull
    @Override
    public ContainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SettingsExItemContainerBinding binding = SettingsExItemContainerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ContainerViewHolder(binding, events, stateManager, userContext);
    }

    static class ContainerViewHolder
            extends
            BaseViewHolder<SettingsContainer, SettingsExItemContainerBinding>
        implements
            View.OnClickListener,
            View.OnLongClickListener,
            CompoundButton.OnCheckedChangeListener,
            AdapterView.OnItemSelectedListener,
            IStateChanged, IUIViewControl {

        private final SettingsListManager settingsManager;
        private final UserClientAppContext userContext;

        public ContainerViewHolder(SettingsExItemContainerBinding binding,
                                   IGenericElementEvent<SettingsContainer, SettingsExItemContainerBinding> events,
                                   IStateManager stateManager,
                                   UserClientAppContext userContext) {
            super(binding, events, stateManager);
            this.userContext = userContext;
            this.settingsManager = new SettingsListManager(binding.getRoot().getContext(), binding.settingsList, stateManager);
        }

        @Override
        public void bind(SettingsContainer item) {
            currentItem = item;

            wireContainerEvents(false);

            binding.tvSettingContainerNameNice.setText(item.getNameNice());
            binding.tvSettingContainerNameFull.setText(item.getName());
            binding.tvSettingContainerDescription.setText(item.getDescription());

            SharedRegistry.ItemState state = sharedRegistry.getItemState(SharedRegistry.STATE_TAG_CONTAINERS, item.getContainerName());

            CheckBoxState.from(currentItem.getSettings(), SharedRegistry.STATE_TAG_SETTINGS, sharedRegistry)
                    .updateCheckBox(binding.cbSettingContainerEnabled, null);

            Context context = binding.getRoot().getContext();

            updateExpandedStateForContainer(state.isExpanded);
            updateHookCount(context, false);
            updateSpinner(context, binding.spSettingContainerRandomizer);

            wireContainerEvents(true);

            //initRandomizer
            //
            //adapterRandomizer = new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_spinner_item);
            //adapterRandomizer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //ivBtRandomize = itemView.findViewById(R.id.ivBtRandomSettingValue);
            //spRandomSelector = itemView.findViewById(R.id.spSettingRandomizerSpinner);
            //if(DebugUtil.isDebug()) Log.d(TAG, "Created the Empty Array for Configs Fragment Config");
            //spRandomSelector.setTag(null);
            //spRandomSelector.setAdapter(adapterRandomizer);
        }


        /*
                private void updateSelection() {
            if(UiUtil.handleSpinnerSelection(spRandomSelector, filtered, getAdapterPosition())) {
                LuaSettingExtended setting = filtered.get(getAdapterPosition());
                SettingUtil.initCardViewColor(spRandomSelector.getContext(), tvSettingName, cvSetting, setting);
            }
        }

         public static boolean handleSpinnerSelection(Spinner spRandomizer, List<LuaSettingExtended> filtered, int position) {
        IRandomizerOld selected = (IRandomizerOld) spRandomizer.getSelectedItem();
        String name = selected.getName();
        try {
            if (name == null ? spRandomizer.getTag() != null : !name.equals(spRandomizer.getTag())) {
                XLog.i("Selected Randomizer Drop Down spinner Modified. randomizer=" + name);
                spRandomizer.setTag(name);
            }

            LuaSettingExtended setting = filtered.get(position);
            if(setting == null)
                return false;

            IRandomizerOld randomizer = setting.getRandomizer();
            if(randomizer != null) {
                List<ISpinnerElement> options = randomizer.getOptions();
                if(options != null && !options.isEmpty() && (randomizer.isSetting(setting.getName()))) {
                    if(selected instanceof ISpinnerElement) {
                        ISpinnerElement element = (ISpinnerElement) selected;
                        if(!element.getName().equals(DataNullElement.EMPTY_ELEMENT.getName())) {
                            if(selected instanceof IManagedSpinnerElement) {
                                IManagedSpinnerElement managedElement = (IManagedSpinnerElement)element;
                                setting.setModifiedValue(managedElement.generateString(spRandomizer.getContext()), true);
                            }else setting.setModifiedValue(element.getValue(), true);
                            //SettingUtil.initCardViewColor(spRandomizer.getContext(), tvSettingName, cvSetting, setting);
                            return true;
                        }
                    } return false;
                }
            } setting.bindRandomizer(selected);
        }catch (Exception e) { XLog.e("Failed to Init Randomizer Drop Down Spinner.", e); }
        return false;
    }

         */

        @Override
        public void bind(SettingsContainer item, List<Object> payloads) {
            if (payloads.isEmpty()) {
                bind(item);
                return;
            }

            currentItem = item;
            Bundle payload = (Bundle) payloads.get(0);
            if (payload.containsKey("containerName")) binding.tvSettingContainerNameNice.setText(payload.getString("containerName"));
            if (payload.containsKey("description")) binding.tvSettingContainerDescription.setText(payload.getString("description"));
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            final SettingSharedRegistry settingShared = sharedRegistry.asSettingShared();
            final Context context = view.getContext();
            final Resources res = view.getResources();
            if(ObjectUtils.anyNull(currentItem, settingShared, context, res))
                return;

            int id = view.getId();
            switch (id) {
                case R.id.tvSettingContainerNameFull:
                case R.id.tvSettingContainerNameNice:
                case R.id.ivExpanderSettingContainer:
                    updateExpandedStateForContainer(sharedRegistry.toggleExpanded(SharedRegistry.STATE_TAG_CONTAINERS, currentItem.getContainerName()));
                    break;
                case R.id.tvHookCount:
                case R.id.ivBtHookMenu:
                    if(!UiLog.ensureNotGlobal(view, userContext) && !UiLog.ensureHasDataContainer(view, currentItem))
                        HooksDialog.create()
                                .set(userContext.appUid, userContext.appPackageName,  context, currentItem.getAllNames())
                                .setDialogEvent(() -> updateHookCount(context, true))
                                .show(manager.getFragmentMan(), res.getString(R.string.title_hooks_assign));
                    break;
                case R.id.ivBtSettingContainerSave:
                    for(SettingHolder holder : settingShared.getSettingsForContainer(currentItem)) {
                        if(holder.isNotSaved()) {
                            A_CODE code = PutSettingExCommand.call(view.getContext(), holder, userContext, userContext.isKill(), false);
                            if(code == A_CODE.FAILED)
                                Snackbar.make(view, res.getString(R.string.save_setting_error), Snackbar.LENGTH_LONG)
                                        .show();
                            else {
                                holder.setValue(holder.getNewValue(), true);
                                holder.setNameLabelColor(view.getContext());
                                Snackbar.make(view, res.getString(R.string.save_setting_success), Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }
                    break;
                case R.id.ivBtSettingContainerRandomize:
                   /* RandomizerSessionContext ctx = new RandomizerSessionContext();
                    ctx.setContext(context);
                    ctx.setSharedRegistry(sharedRegistry);
                    ctx.setRandomizers();
                    ctx.setSettings(settingShared.getSettingsForContainer(currentItem));
                    //ctx.
                    ctx.setForceUpdate(true);
                    ctx.randomizeAll();*/

                    RandomizerSessionContext ctx = new RandomizerSessionContext()
                            .randomize(
                                    manager.getAsFragment(),
                                    settingShared.getSettingsForContainer(currentItem),
                                    context,
                                    sharedRegistry);

                    //manager.

                    /*RandomizerSessionContext ctx = RandomizerSessionContext.create();
                    ctx.setContext(context);
                    ctx.setSharedRegistry(sharedRegistry);

                    //ctx.setSettings(SettingFragmentUtils.filterChecked(SettingFragmentUtils.getSettings(getLiveData()), sharedRegistry));
                    ctx.setRandomizers();
                    //ctx.randomizeAll();
                    List<SettingHolder> all = SettingFragmentUtils.getSettings(getLiveData());
                    ctx.randomize(false, all, SettingFragmentUtils.filterChecked(all, sharedRegistry));*/


                    //int randomized = settingShared.randomize(settingShared.getSettingsForContainer(currentItem), context);
                    Snackbar.make(view, res.getString(ctx.getRandomizedCount() == 0 ?
                                    R.string.msg_error_randomizer_none :
                                    R.string.msg_result_randomized), Snackbar.LENGTH_LONG).show();
                    break;
                case R.id.ivBtSettingContainerReset:
                    ListUtil.forEachVoid(settingShared.getSettingsForContainer(currentItem), (o, i) -> o.reset(context));
                    break;
                case R.id.ivBtSettingContainerDelete:
                    SettingDeleteDialog.create()
                            .set(settingShared.getSettingsForContainer(currentItem), currentItem)
                            .setApp(userContext)
                            .setDialogEventFail((m) -> Snackbar.make(view, res.getString(R.string.msg_error_result_generic) + m, Snackbar.LENGTH_LONG).show())
                            .setDialogEventFinish(() -> Snackbar.make(view, res.getString(R.string.msg_result_deleted), Snackbar.LENGTH_LONG).show())
                                .show(manager.getFragmentMan(), res.getString(R.string.title_delete_action));
                    break;

                case R.id.ivBtWildcard:
                    ListUtil.forEachVoid(settingShared.getSettingsForContainer(currentItem), (o, i) -> {
                        //String newValue = "%%" + currentItem.getName() + "%%_1%%";
                        String newValue = "%random%";
                        o.setNewValue(newValue);
                        o.ensureUiUpdated(newValue);
                        o.setNameLabelColor(context);
                    });
                    break;
            }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onLongClick(View view) {
            Resources res = view.getResources();
            if(currentItem != null && res != null) {
                int id = view.getId();
                int resId = 0;
                switch (id) {
                    case R.id.tvHookCount:
                    case R.id.ivBtHookMenu:
                        resId = R.string.msg_hint_hook_control;
                        break;
                    case R.id.ivBtWildcard:
                        resId = R.string.msg_hint_wild_card;
                        break;
                    case R.id.ivBtSettingContainerDelete:
                        resId = R.string.msg_hint_delete_container;
                        break;
                    case R.id.ivBtSettingContainerRandomize:
                        resId = R.string.msg_hint_randomize_container;
                        break;
                    case R.id.ivBtSettingContainerReset:
                        resId = R.string.msg_hint_reset_container;
                        break;
                    case R.id.ivBtSettingContainerSave:
                        resId = R.string.msg_hint_save_container;
                        break;
                }

                if(resId > 0)
                    Snackbar.make(view, Str.fm(res.getString(resId), currentItem.data.enabled, currentItem.data.total), Snackbar.LENGTH_LONG).show();
            }
            return false;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if(currentItem == null)
                return;

            int id = compoundButton.getId();
            if(id == R.id.cbSettingContainerEnabled) {

                //SharedViewControl sharedViewControl = getSharedViewControl();
                //for (SettingHolder holder : currentItem.getSettings())
                //    sharedViewControl.setChecked(SharedViewControl.G_SETTINGS, holder.getSharedId(), isChecked);
                //sharedViewControl.notifyChecked(SharedViewControl.G_S_CONTAINERS);


                /* Update our Checked State Cache */
                sharedRegistry.setChecked(SharedRegistry.STATE_TAG_CONTAINERS, currentItem.getContainerName(), isChecked);

                /* Update the Child Settings Check State Cache for each */
                sharedRegistry.setCheckedBulk(SharedRegistry.STATE_TAG_SETTINGS, currentItem.getSettings(), isChecked);

                /* Get State Control for the settings (helps us set our check box color) ... */
                CheckBoxState stateControl = CheckBoxState.from(currentItem.getSettings(), SharedRegistry.STATE_TAG_SETTINGS, sharedRegistry);

                /*Update Our Check Box Color (no need to set check as its already updated)*/
                stateControl.updateCheckBoxColor(binding.cbSettingContainerEnabled);

                /*Notify the Children that there was an update*/
                stateControl.notifyObjects(currentItem.getSettings(), SharedRegistry.STATE_TAG_CONTAINERS, sharedRegistry);

                /*Notify the Parent that there was an Update
                * Since the Update Handler for Groups simply takes all settings for the "CheckBoxState" we don't need specific handlers for it
                * Just ensure the settings are fully updated via cache
                * */
                sharedRegistry.notifyGroupChange(currentItem.getGroup(), SharedRegistry.STATE_TAG_CONTAINERS);
            }
        }

        @Override
        protected void onViewDetached() {
            wireContainerEvents(false);
            settingsManager.clear();
            if(currentItem != null) {
                sharedRegistry.putGroupChangeListener(null, currentItem.getSharedId());
                currentItem = null;
            }
        }

        private void updateExpandedStateForContainer(boolean isExpanded) {
            if(!isExpanded)
                settingsManager.clear();
            CoreUiUtils.setViewsVisibility(
                    binding.ivExpanderSettingContainer,
                    isExpanded,
                    binding.tvHookCount,
                    binding.settingsScrollView,
                    binding.tvSettingContainerDescription,
                    binding.ivBtSettingContainerDelete,
                    binding.ivBtSettingContainerRandomize,
                    binding.ivBtSettingContainerReset,
                    binding.ivBtSettingContainerSave,
                    binding.ivBtWildcard,
                    binding.spSettingContainerRandomizer,
                    binding.ivBtHookMenu);
            if (isExpanded && currentItem != null && currentItem.hasSettings())
                settingsManager.submitList(currentItem.getSettings());
        }

        private void updateHookCount(Context context, boolean refresh) {
            if(currentItem != null && sharedRegistry != null) {
                AssignmentData data = CoreUiUtils.ensureAssignmentDataInit(
                        context,
                        userContext.appUid,
                        userContext.appPackageName,
                        sharedRegistry,
                        currentItem,
                        refresh);

                //int c = data.total > 0 ?  R.attr.colorUnsavedSetting : data.enabled > 0 ? R.attr.colorAccent : R.attr.colorTextOne;
                int color = XUtil.resolveColor(context, data.total > 0 ?  R.attr.colorUnsavedSetting  : R.attr.colorTextOne);
                CoreUiUtils.setTextColor(binding.tvHookCount, color, false);
                CoreUiUtils.setText(binding.tvHookCount, data.toString(), false);
            }
        }

        private void wireContainerEvents(boolean wire) {
            if (binding != null) {
                binding.cbSettingContainerEnabled.setOnCheckedChangeListener(wire ? this : null);

                binding.ivBtSettingContainerDelete.setOnClickListener(wire ? this : null);
                binding.ivBtSettingContainerDelete.setOnLongClickListener(wire ? this : null);

                binding.ivBtSettingContainerRandomize.setOnClickListener(wire ? this : null);
                binding.ivBtSettingContainerRandomize.setOnLongClickListener(wire ? this : null);

                binding.ivBtSettingContainerSave.setOnClickListener(wire ? this : null);
                binding.ivBtSettingContainerSave.setOnLongClickListener(wire ? this : null);

                binding.ivBtSettingContainerReset.setOnClickListener(wire ? this : null);
                binding.ivBtSettingContainerReset.setOnLongClickListener(wire ? this : null);


                binding.tvHookCount.setOnClickListener(wire ? this : null);
                binding.tvHookCount.setOnLongClickListener(wire ? this : null);

                binding.ivBtHookMenu.setOnClickListener(wire ? this : null);
                binding.ivBtHookMenu.setOnLongClickListener(wire ? this : null);

                binding.tvSettingContainerNameNice.setOnClickListener(wire ? this : null);
                binding.tvSettingContainerNameFull.setOnClickListener(wire ? this : null);
                binding.ivExpanderSettingContainer.setOnClickListener(wire ? this : null);

                binding.spSettingContainerRandomizer.setOnItemSelectedListener(wire ? this : null);

                binding.ivBtWildcard.setOnClickListener(wire ? this : null);
                binding.ivBtWildcard.setOnLongClickListener(wire ? this : null);

                if(currentItem != null)
                    sharedRegistry.putGroupChangeListener(this, currentItem.getSharedId());
            }
        }

        @Override
        public void onGroupChange(ChangedStatesPacket packet) {
            if(DebugUtil.isDebug())
                Log.d("XLua.OptimizedContainerAdapter", "Packet=" + Str.toStringOrNull(packet) + "Group: " + currentItem.getGroup() + " Container: " + currentItem.getContainerName() + " Name: " + currentItem.getName() + " Count=" + currentItem.getSettings().size());

            if(currentItem != null) {
                if(packet.isFrom(SharedRegistry.STATE_TAG_SETTINGS)) {
                    /*Update Our Check Box State using our Children*/
                    CheckBoxState stateControl = CheckBoxState.from(currentItem.getSettings(), SharedRegistry.STATE_TAG_SETTINGS, sharedRegistry);

                    /* Update our Check Box */
                    stateControl.updateCheckBox(binding.cbSettingContainerEnabled, this);

                    /*We update our group here as its just easier less code on the child*/
                    sharedRegistry.notifyGroupChange(currentItem.getGroup(), SharedRegistry.STATE_TAG_SETTINGS);
                }
                else if(packet.isFrom(SharedRegistry.STATE_TAG_GROUPS)) {
                    /*Update Has been requested from Parent View */
                    /*Align our Children (settings) in our container to our check */
                    //boolean isChecked = stateRegistry.isChecked(ViewStateRegistry.STATE_TAG_CONTAINERS, currentItem.getContainerName());
                    //stateRegistry.setCheckedBulk(ViewStateRegistry.STATE_TAG_SETTINGS, currentItem.getSettings(), isChecked);

                    /*Update our check box color & checked state*/
                    CheckBoxState stateControl = CheckBoxState.from(currentItem.getSettings(), SharedRegistry.STATE_TAG_SETTINGS, sharedRegistry);

                    /* Update our Check Box */
                    stateControl.updateCheckBox(binding.cbSettingContainerEnabled, this);

                    /* Caller (Group) is suppose to be responsible for setting states for settings & updating */
                    /* So we will not "update" the settings but if this is called then this means this view exists then notify child view */
                    stateControl.notifyObjects(currentItem.getSettings(), SharedRegistry.STATE_TAG_CONTAINERS, sharedRegistry);

                    /*Now Finally Notify our Children Objects to Update their check box states*/
                    //stateControl.notifyObjects(currentItem.getSettings(), ViewStateRegistry.STATE_TAG_CONTAINERS, stateRegistry);
                }
            }
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { handleSpinnerUpdate(adapterView.getContext()); }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) { handleSpinnerUpdate(adapterView.getContext()); }

        public void handleSpinnerUpdate(Context context) {
            //Check then updates spinner ??
            final SettingSharedRegistry settingShared = sharedRegistry.asSettingShared();
            Spinner spinner = binding.spSettingContainerRandomizer;
            IRandomizer randomizer = (IRandomizer) spinner.getSelectedItem();
            if(randomizer.isOption()) {
                if(!(randomizer instanceof RandomOptionNullElement)) {
                    for(SettingHolder holder : settingShared.getSettingsForContainer(currentItem, true)) {
                       /* RandomizerSessionContext ctx = new RandomizerSessionContext();
                        ctx.setContext(context);
                        ctx.setSharedRegistry(sharedRegistry);

                        ctx.stack.push(holder.getName());
                        randomizer.randomize(ctx);

                        String newValue = ctx.getValue(holder.getName());
                        holder.setNewValue(newValue);
                        holder.ensureUiUpdated(newValue);
                        holder.setNameLabelColor(context);*/
                    }
                }
            } else {
                for(SettingHolder holder : settingShared.getSettingsForContainer(currentItem, false))
                    sharedRegistry.pushSharedObject(holder.getSharedId(), randomizer);
            }
        }

        public void updateSpinner(Context context, Spinner spinner) {
            if(!ObjectUtils.anyNull(context, spinner, currentItem)) {
                String tag = currentItem.getName() + "_spin_array";
                ArrayAdapter<IRandomizer> adapter = sharedRegistry.getSharedObject(tag);
                if(adapter == null) {
                    adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
                    sharedRegistry.pushSharedObject(tag, adapter);
                }

                spinner.setAdapter(adapter);    //We can make this faster using a single time init state, store index local
                //We can also init once store something in data like style, then all we need to do is find the index selected
                UiRandomUtils.initRandomizer(adapter, spinner, currentItem, sharedRegistry.asSettingShared());
            }
        }

        @Override
        public SharedViewControl getSharedViewControl() {
            return null;
        }

        @Override
        public void setSharedViewControl(SharedViewControl viewControl) {

        }

        @Override
        public void onEvent(EventTrigger event) {
            SharedViewControl sharedViewControl = getSharedViewControl();
            if(event.isCheckEvent()) {
                int not = 0;
                int yes = 0;
                for (SettingHolder holder : currentItem.getSettings())
                    if (sharedViewControl.isChecked(SharedViewControl.G_SETTINGS, holder.getSharedId())) yes++;
                    else not++;
                CheckBoxState.create(yes, yes + not).updateCheckBox(binding.cbSettingContainerEnabled, this);
            }
        }

        @Override
        public void onView() {
            IUIViewControl.super.onView();
        }

        @Override
        public void onClean() {
            IUIViewControl.super.onClean();
        }

        @Override
        public boolean isView(String id) {
            return IUIViewControl.super.isView(id);
        }
    }
}