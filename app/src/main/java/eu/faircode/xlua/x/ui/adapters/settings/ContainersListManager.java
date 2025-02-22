package eu.faircode.xlua.x.ui.adapters.settings;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.databinding.SettingsExItemContainerBinding;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.ui.core.UINotifier;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.adapter.ListViewManager;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.ui.core.util.UiLog;
import eu.faircode.xlua.x.ui.core.util.UiRandomUtils;
import eu.faircode.xlua.x.ui.core.view_registry.ChangedStatesPacket;
import eu.faircode.xlua.x.ui.core.view_registry.CheckBoxState;
import eu.faircode.xlua.x.ui.core.view_registry.IStateChanged;
import eu.faircode.xlua.x.ui.core.view_registry.SettingSharedRegistry;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.dialogs.HooksDialog;
import eu.faircode.xlua.x.ui.dialogs.MessageDialog;
import eu.faircode.xlua.x.ui.dialogs.SettingDeleteDialog;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.hook.AppAssignmentInfo;
import eu.faircode.xlua.x.xlua.settings.GroupStats;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;

/**
 * ContainersListManager - ListView-style implementation for container views
 */
public class ContainersListManager extends ListViewManager<SettingsContainer, SettingsExItemContainerBinding> {

    private static final String TAG = LibUtil.generateTag(ContainersListManager.class);
    private final UserClientAppContext userContext;
    private final Map<String, ContainerViewHolder> viewHolders = new HashMap<>();

    public ContainersListManager(Context context, LinearLayout containerView, IStateManager stateManager, UserClientAppContext userContext) {
        super(context, containerView, stateManager);
        this.userContext = userContext;
        this.userContext.bindShared(stateRegistry);
    }

    @Override
    protected SettingsExItemContainerBinding inflateItemView(ViewGroup parent) {
        SettingsExItemContainerBinding binding = SettingsExItemContainerBinding.inflate(inflater, parent, false);
        binding.getRoot().setTag(binding);
        return binding;
    }

    @Override
    protected String getStateTag() {
        return SharedRegistry.STATE_TAG_CONTAINERS;
    }

    @Override
    protected void bindItemView(SettingsExItemContainerBinding binding, SettingsContainer container) {
        if (container == null) return;

        // Create or get view holder
        ContainerViewHolder holder = viewHolders.get(container.getContainerName());
        if (holder == null) {
            holder = new ContainerViewHolder(binding, stateManager, userContext);
            viewHolders.put(container.getContainerName(), holder);
        } else {
            holder.binding = binding;
        }

        // Delegate binding to holder
        holder.bind(container);
    }

    @Override
    protected void cleanupItemView(SettingsExItemContainerBinding binding) {
        if (binding == null) return;

        // Find the holder for this view
        String containerName = binding.tvSettingContainerNameFull.getText().toString();
        ContainerViewHolder holder = viewHolders.get(containerName);

        if (holder != null) {
            holder.onViewDetached();
            viewHolders.remove(containerName);
        } else {
            // Basic cleanup if holder not found
            binding.cbSettingContainerEnabled.setOnCheckedChangeListener(null);
            binding.tvSettingContainerNameNice.setOnClickListener(null);
            binding.tvSettingContainerNameFull.setOnClickListener(null);
            binding.ivExpanderSettingContainer.setOnClickListener(null);
            binding.ivBtSettingContainerDelete.setOnClickListener(null);
            binding.ivBtSettingContainerRandomize.setOnClickListener(null);
            binding.ivBtSettingContainerReset.setOnClickListener(null);
            binding.ivBtSettingContainerSave.setOnClickListener(null);
            binding.tvHookCount.setOnClickListener(null);
            binding.ivBtHookMenu.setOnClickListener(null);
            binding.ivBtWildcard.setOnClickListener(null);
            binding.spSettingContainerRandomizer.setOnItemSelectedListener(null);
            binding.settingsList.removeAllViews();
        }
    }

    @Override
    public void clear() {
        super.clear();

        // Cleanup all holders
        for (ContainerViewHolder holder : viewHolders.values()) {
            holder.onViewDetached();
        }

        viewHolders.clear();
    }

    /**
     * ViewHolder pattern adapted to work with LinearLayout
     */
    public class ContainerViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener,
            CompoundButton.OnCheckedChangeListener,
            AdapterView.OnItemSelectedListener,
            IStateChanged,
            UINotifier.IUINotification {

        public SettingsExItemContainerBinding binding;
        private final IStateManager manager;
        private SettingsContainer currentItem;
        private final UserClientAppContext userContext;
        private SharedRegistry sharedRegistry;
        private final GroupStats groupStats = new GroupStats();
        private SettingsListManager settingsManager;

        public ContainerViewHolder(SettingsExItemContainerBinding binding,
                                   IStateManager stateManager,
                                   UserClientAppContext userContext) {
            this.binding = binding;
            this.manager = stateManager;
            this.userContext = userContext;
            this.sharedRegistry = stateManager.getSharedRegistry();
            this.settingsManager = new SettingsListManager(binding.getRoot().getContext(), binding.settingsList, stateManager);
        }

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
            updateHookCount(context, false, false);
            updateSpinner(context, binding.spSettingContainerRandomizer);
            updateStats(true, getContext());

            sharedRegistry.putGroupChangeListener(this, currentItem.getObjectId());
            sharedRegistry.notifier.subscribeGroup(this);

            wireContainerEvents(true);
        }

        private void updateExpandedStateForContainer(boolean isExpanded) {
            // Show/hide the expanded container content
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

            // If expanded, populate the settings list
            if (isExpanded && currentItem != null && currentItem.hasSettings()) {
                settingsManager.submitList(currentItem.getSettings());
            } else {
                settingsManager.clear();
            }
        }

        private void updateHookCount(Context context, boolean refresh, boolean refreshMap) {
            if (currentItem != null && sharedRegistry != null) {
                if (refreshMap)
                    sharedRegistry.asSettingShared().refreshAssignments(context, userContext);

                AppAssignmentInfo info = sharedRegistry.asSettingShared().getAssignmentInfo(currentItem, refresh, context);
                CoreUiUtils.setTextColor(binding.tvHookCount, info.getLabelColor(context), false);
                CoreUiUtils.setText(binding.tvHookCount, info.getPrefix(), false);
            }
        }

        private void updateSpinner(Context context, Spinner spinner) {
            if (!ObjectUtils.anyNull(context, spinner, currentItem)) {
                String tag = currentItem.getName() + "_spin_array";
                ArrayAdapter<IRandomizer> adapter = sharedRegistry.getSharedObject(tag);
                if (adapter == null) {
                    adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
                    sharedRegistry.pushSharedObject(tag, adapter);
                }

                spinner.setAdapter(adapter);
                UiRandomUtils.initRandomizer(adapter, spinner, currentItem, sharedRegistry.asSettingShared());
            }
        }

        public void updateStats(boolean forceUpdate, Context context) {
            if (currentItem != null && binding != null) {
                groupStats
                        .update(currentItem, forceUpdate)
                        .updateColor(binding.tvSettingContainerNameNice, context)
                        .updateIv(binding.ivActionNeeded);
            }
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
                    if(sharedRegistry.asSettingShared().getAssignmentInfo(
                            currentItem, false, context).getCount() < 1) {
                        Snackbar.make(view, view.getResources().getString(R.string.msg_error_no_hooks), Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    if(!UiLog.ensureNotGlobal(view, userContext))
                        HooksDialog.create()
                                .set(userContext.appUid, userContext.appPackageName,  context, currentItem.getAllNames())
                                .setDialogEvent(() -> updateHookCount(context, true, true))
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
                                holder.notifyUpdate(sharedRegistry);
                                Snackbar.make(view, res.getString(R.string.save_setting_success), Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }
                    break;
                case R.id.ivBtSettingContainerRandomize:
                    RandomizerSessionContext ctx = new RandomizerSessionContext()
                            .randomize(
                                    manager.getAsFragment(),
                                    settingShared.getSettingsForContainer(currentItem),
                                    context,
                                    sharedRegistry);

                    Snackbar.make(view, res.getString(ctx.getRandomizedCount() == 0 ?
                            R.string.msg_error_randomizer_none :
                            R.string.msg_result_randomized), Snackbar.LENGTH_LONG).show();
                    break;
                case R.id.ivBtSettingContainerReset:
                    ListUtil.forEachVoid(settingShared.getSettingsForContainer(currentItem), (o, i) -> o.reset(context, sharedRegistry.notifier));
                    break;
                case R.id.ivBtSettingContainerDelete:
                    SettingDeleteDialog.create()
                            .set(settingShared.getSettingsForContainer(currentItem), currentItem)
                            .setNotifier(settingShared.notifier)
                            .setApp(userContext)
                            .setDialogEventFail((m) -> Snackbar.make(view, res.getString(R.string.msg_error_result_generic) + m, Snackbar.LENGTH_LONG).show())
                            .setDialogEventFinish(() -> Snackbar.make(view, res.getString(R.string.msg_result_deleted), Snackbar.LENGTH_LONG).show())
                            .show(manager.getFragmentMan(), res.getString(R.string.title_delete_action));
                    break;

                case R.id.ivBtWildcard:
                    ListUtil.forEachVoid(settingShared.getSettingsForContainer(currentItem), (o, i) -> {
                        //Check if it has a Randomizer
                        String newValue = "%random%";
                        o.setNewValue(newValue);
                        o.ensureUiUpdated(newValue);
                        o.setNameLabelColor(context);
                        o.notifyUpdate(settingShared.notifier);
                    });
                    break;
                case R.id.ivActionNeeded:
                    MessageDialog.create()
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setName(res.getString(R.string.message_warning_hooks_title))
                            .setMessage(res.getString(R.string.message_warning_hooks_message))
                            .show(manager.getFragmentMan(), res.getString(R.string.menu_info));
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
                    case R.id.ivActionNeeded:
                        resId = R.string.msg_hint_warning_save;
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
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            handleSpinnerUpdate(adapterView.getContext());
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            handleSpinnerUpdate(adapterView.getContext());
        }

        public void handleSpinnerUpdate(Context context) {
            //Check then updates spinner ??
            final SettingSharedRegistry settingShared = sharedRegistry.asSettingShared();
            Spinner spinner = binding.spSettingContainerRandomizer;
            IRandomizer randomizer = (IRandomizer) spinner.getSelectedItem();
            if(randomizer.isOption()) {
                if(!(randomizer instanceof RandomOptionNullElement)) {
                    RandomizerSessionContext.create()
                            .updateToOption(
                                    manager.getAsFragment(),
                                    settingShared.getSettingsForContainer(currentItem),
                                    randomizer,
                                    context,
                                    sharedRegistry);
                }
            } else {
                for(SettingHolder holder : settingShared.getSettingsForContainer(currentItem, false))
                    sharedRegistry.pushSharedObject(holder.getObjectId(), randomizer);
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

                binding.ivActionNeeded.setOnClickListener(wire ? this : null);

                binding.ivActionNeeded.setOnLongClickListener(wire ? this : null);

                binding.ivBtWildcard.setOnClickListener(wire ? this : null);
                binding.ivBtWildcard.setOnLongClickListener(wire ? this : null);
            }
        }

        public void onViewDetached() {
            wireContainerEvents(false);
            settingsManager.clear();
            if(currentItem != null) {
                sharedRegistry.putGroupChangeListener(null, currentItem.getObjectId());
                sharedRegistry.notifier.unsubscribeGroup(this);
                currentItem = null;
            }
        }

        @Override
        public void onGroupChange(ChangedStatesPacket packet) {
            if(currentItem != null) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "On Group Changed, Item=" + currentItem.getContainerName() + " From:" + packet.changedGroup);

                if(packet.isFrom(SharedRegistry.STATE_TAG_SETTINGS)) {
                    /*Update Our Check Box State using our Children*/
                    CheckBoxState stateControl = CheckBoxState.from(currentItem.getSettings(), SharedRegistry.STATE_TAG_SETTINGS, sharedRegistry);

                    /* Update our Check Box */
                    stateControl.updateCheckBox(binding.cbSettingContainerEnabled, this);

                    /*We update our group here as its just easier less code on the child*/
                    sharedRegistry.notifyGroupChange(currentItem.getGroup(), SharedRegistry.STATE_TAG_SETTINGS);
                }
                else if(packet.isFrom(SharedRegistry.STATE_TAG_GROUPS)) {
                    /*Update our check box color & checked state*/
                    CheckBoxState stateControl = CheckBoxState.from(currentItem.getSettings(), SharedRegistry.STATE_TAG_SETTINGS, sharedRegistry);

                    /* Update our Check Box */
                    stateControl.updateCheckBox(binding.cbSettingContainerEnabled, this);

                    /* Caller (Group) is suppose to be responsible for setting states for settings & updating */
                    /* So we will not "update" the settings but if this is called then this means this view exists then notify child view */
                    stateControl.notifyObjects(currentItem.getSettings(), SharedRegistry.STATE_TAG_CONTAINERS, sharedRegistry);
                }
            }
        }

        public Context getContext() {
            if(binding != null) {
                try {
                    LinearLayout v = binding.getRoot();
                    Context ctx = v.getContext();
                    if(ctx != null)
                        return ctx;
                }catch (Exception ignored) {  }
            }
            return binding.getRoot().getContext();
        }

        @Override
        public String getNotifierId() { return currentItem != null ? UINotifier.containerName(currentItem.getContainerName()) : null; }

        @Override
        public void notify(int code, String notifier, Object extra) {
            if(code == UINotifier.CODE_DATA_CHANGED) {
                if(UINotifier.isSettingPrefix(notifier)) {
                    //Do nothing for now
                    updateStats(true, getContext());
                }
            }
        }
    }
}