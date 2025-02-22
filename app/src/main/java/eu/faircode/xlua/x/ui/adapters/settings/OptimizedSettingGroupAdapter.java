package eu.faircode.xlua.x.ui.adapters.settings;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.databinding.SettingsExGroupBinding;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.core.UINotifier;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.view_registry.ChangedStatesPacket;
import eu.faircode.xlua.x.ui.core.view_registry.CheckBoxState;
import eu.faircode.xlua.x.ui.core.view_registry.IStateChanged;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.core.adapter.EnhancedListAdapter;
import eu.faircode.xlua.x.ui.core.interfaces.IGenericElementEvent;
import eu.faircode.xlua.x.ui.core.interfaces.IListAdapter;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.ui.dialogs.MessageDialog;
import eu.faircode.xlua.x.xlua.settings.GroupStats;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.SettingsGroup;
import eu.faircode.xlua.x.xlua.settings.test.EventTrigger;
import eu.faircode.xlua.x.xlua.settings.test.SharedViewControl;
import eu.faircode.xlua.x.xlua.settings.test.interfaces.IUIViewControl;

public class OptimizedSettingGroupAdapter
        extends EnhancedListAdapter<SettingsGroup, SettingsExGroupBinding, OptimizedSettingGroupAdapter.GroupViewHolder>
        implements IListAdapter<SettingsGroup, SettingsExGroupBinding> {

    private static final int PREFETCH_COUNT = 10;
    private final RecyclerView.RecycledViewPool sharedPool;
    private final UserClientAppContext userContext;

    public OptimizedSettingGroupAdapter(Context context,
                                        IGenericElementEvent<SettingsGroup, SettingsExGroupBinding> events,
                                        IStateManager stateManager,
                                        UserClientAppContext userContext) {

        super(context, events, stateManager, new RecyclerView.RecycledViewPool());
        this.sharedPool = new RecyclerView.RecycledViewPool();
        this.sharedPool.setMaxRecycledViews(0, 15); // Adjust pool size as needed
        this.userContext = userContext;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SettingsExGroupBinding binding = SettingsExGroupBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GroupViewHolder(binding, events, stateManager, sharedPool, userContext);
    }

    public static class GroupViewHolder extends BaseViewHolder<SettingsGroup, SettingsExGroupBinding>
            implements
            View.OnClickListener,
            View.OnLongClickListener,
            CompoundButton.OnCheckedChangeListener,
            IStateChanged,
            UINotifier.IUINotification {

        private ContainersListManager containersListManager;


        private OptimizedContainerAdapter containerAdapter;
        private final RecyclerView.RecycledViewPool sharedPool;
        private boolean isInitialized = false;
        private UserClientAppContext userContext;

        private final GroupStats groupStats = new GroupStats();

        public GroupViewHolder(SettingsExGroupBinding binding,
                               IGenericElementEvent<SettingsGroup, SettingsExGroupBinding> events,
                               IStateManager stateManager,
                               RecyclerView.RecycledViewPool sharedPool,
                               UserClientAppContext userContext) {
            super(binding, events, stateManager);
            this.sharedPool = sharedPool;
            this.userContext = userContext;
            //initializeRecyclerView();
            initializeViews();
        }

        private void initializeViews() {
            if (isInitialized) return;

            // Initialize the containers list manager
            containersListManager = new ContainersListManager(
                    binding.getRoot().getContext(),
                    binding.containersList,
                    manager,
                    userContext);

            isInitialized = true;
        }

        @Override
        public void bind(SettingsGroup item) {
            currentItem = item;

            binding.tvSettingGroupName.setText(item.getGroupName());

            SharedRegistry.ItemState state = sharedRegistry.getItemState(SharedRegistry.STATE_TAG_GROUPS, item.getGroupName());
            onGroupChange(null);
            sharedRegistry.putGroupChangeListener(this, item.getGroupName());
            ensureEventsSubscribed();

            updateExpandedStateForGroup(state.isExpanded);

            wireGroupEvents(true);
        }

        public void ensureEventsSubscribed() {
            if(sharedRegistry != null) {
                UINotifier notifications = sharedRegistry.notifier;
                notifications.subscribeGroup(this);
                for(SettingsContainer container : currentItem.getContainers()) {
                    notifications.prepareGroup(getNotifierId(), UINotifier.containerName(container.getContainerName()));
                    for(SettingHolder setting : container.getSettings()) {
                        notifications.prepareGroup(getNotifierId(), UINotifier.settingName(setting.getName()));
                    }
                }
            }
        }

        @Override
        public void bind(SettingsGroup item, List<Object> payloads) {
            if (payloads.isEmpty()) {
                bind(item);
                return;
            }

            currentItem = item;
            /*Bundle payload = (Bundle) payloads.get(0);
            if (payload.containsKey("groupName")) binding.tvSettingGroupName.setText(payload.getString("groupName"));
            if (payload.containsKey("containersSizeChanged")) containerAdapter.submitList(item.getContainers());*/
            updateStats(true, getContext());
        }

        private void updateExpandedStateForGroup(boolean isExpanded) {
            CoreUiUtils.setViewsVisibility(
                    binding.ivExpanderSettingGroup,
                    isExpanded,
                    binding.containersList);

            if (isExpanded && currentItem != null && !currentItem.getContainers().isEmpty()) {
                containersListManager.submitList(currentItem.getContainers());
            } else {
                containersListManager.clear();
            }
        }

        public void handleExpandClickForGroup(SettingsGroup item) {
            boolean expanded = sharedRegistry.toggleExpanded(SharedRegistry.STATE_TAG_GROUPS, item.getGroupName());
            updateExpandedStateForGroup(expanded);
        }

        @Override
        protected void onViewDetached() {
            wireGroupEvents(false);
            containersListManager.clear();
            if(currentItem != null) {
                sharedRegistry.putGroupChangeListener(null, currentItem.getGroupName());
                sharedRegistry.notifier.unsubscribeGroup(this);
                currentItem = null;
            }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            if(currentItem == null)
                return;

            final int id = view.getId();
            final Context context = view.getContext();
            switch (id) {
                case R.id.tvSettingGroupName:
                case R.id.ivExpanderSettingGroup:
                case R.id.cvSettingGroup:
                    handleExpandClickForGroup(currentItem);
                    break;
                case R.id.ivActionNeeded:
                    MessageDialog.create()
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setName(context.getString(R.string.message_warning_hooks_title))
                            .setMessage(context.getString(R.string.message_warning_hooks_message))
                            .show(manager.getFragmentMan(), context.getString(R.string.menu_info));
                    break;
            }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onLongClick(View view) {
            Resources res = view.getResources();
            if(res != null) {
                int id = view.getId();
                int resId = 0;
                switch (id) {
                    case R.id.ivActionNeeded:
                        resId = R.string.msg_hint_warning_save;
                        break;
                    case R.id.tvStatsCount:
                        resId = R.string.msg_hint_settings_stat;
                        break;
                }

                if(resId > 0)
                    Snackbar.make(view, res.getString(resId), Snackbar.LENGTH_LONG).show();
            }
            return false;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if(currentItem == null)
                return;
            int id = compoundButton.getId();

            if(id == R.id.cbSettingGroupEnabled) {
                /*Update Our Group in State Cache so child can see the reflected changes*/
                sharedRegistry.setChecked(SharedRegistry.STATE_TAG_GROUPS, currentItem.getGroupName(), isChecked);

                /* Update our Children Containers to Reflect out changes */
                sharedRegistry.setCheckedBulk(SharedRegistry.STATE_TAG_CONTAINERS, currentItem.getContainers(), isChecked);

                /* Create Context for our Check Box Details (help for setting color and check state if need to set check state) */
                CheckBoxState checkBoxState = CheckBoxState.from(currentItem.getContainers(), SharedRegistry.STATE_TAG_CONTAINERS, sharedRegistry);

                /* Only update our check box color as its already checked state updated*/
                checkBoxState.updateCheckBoxColor(binding.cbSettingGroupEnabled);

                sharedRegistry.setCheckedBulk(SharedRegistry.STATE_TAG_SETTINGS, getAllSettings(), isChecked);

                /*Notify Our Children Views of the Changes so they can align*/
                /*If it is not created as in the views, then it will not notify the object but the cache will still be aligned*/
                checkBoxState.notifyObjects(currentItem.getContainers(), SharedRegistry.STATE_TAG_GROUPS, sharedRegistry);

                /*Perhaps we should set it via settings method ?*/
            }
        }

        private void wireGroupEvents(boolean wire) {
            if (binding != null) {
                binding.ivExpanderSettingGroup.setOnClickListener(wire ? this : null);
                binding.cvSettingGroup.setOnClickListener(wire ? this : null);
                binding.cvSettingGroup.setOnLongClickListener(wire ? this : null);
                binding.cbSettingGroupEnabled.setOnCheckedChangeListener(wire ? this : null);

                binding.tvSettingGroupName.setOnClickListener(wire ? this : null);

                binding.ivActionNeeded.setOnClickListener(wire ? this : null);

                binding.ivActionNeeded.setOnLongClickListener(wire ? this : null);
                binding.tvStatsCount.setOnLongClickListener(wire ? this : null);
            }
        }

        public List<IIdentifiableObject> getAllSettings() {
            List<SettingsContainer> containers = currentItem.getContainers();
            List<IIdentifiableObject> objects = new ArrayList<>(containers.size());
            for(SettingsContainer container : containers) objects.addAll(container.getSettings());
            return objects;
        }

        @Override
        public void onGroupChange(ChangedStatesPacket packet) {
            if(currentItem != null && binding != null) {
                /* ENSURE our CheckBox is ALWAYS aligned */
                /* From parent we can pre-init the View States for Saved Settings in local settings .. */
                updateStats(true, getContext());
                CheckBoxState
                        .from(getAllSettings(), SharedRegistry.STATE_TAG_SETTINGS, sharedRegistry)
                        .updateCheckBox(binding.cbSettingGroupEnabled, this);
            }
        }

        public void updateStats(boolean forceUpdate, Context context) {
            if(currentItem != null && binding != null) {
                groupStats
                        .update(currentItem, forceUpdate)
                        .updateLabel(binding.tvStatsCount)
                        .updateColor(binding.tvSettingGroupName, context)
                        .updateIv(binding.ivActionNeeded);
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
            return itemView.getContext();
        }

        @Override
        public String getNotifierId() { return currentItem != null ? UINotifier.groupName(currentItem.getGroupName()) : null; }

        @Override
        public void notify(int code, String notifier, Object extra) {
            if(code == UINotifier.CODE_DATA_CHANGED) {
                if(UINotifier.isSettingPrefix(notifier)) {
                    updateStats(true, getContext());
                }
            }
        }
    }
}