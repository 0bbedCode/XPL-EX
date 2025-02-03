package eu.faircode.xlua.x.ui.adapters.settings;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.databinding.SettingsExGroupBinding;
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
            IUIViewControl {

        private OptimizedContainerAdapter containerAdapter;
        private final RecyclerView.RecycledViewPool sharedPool;
        private boolean isInitialized = false;
        private UserClientAppContext userContext;

        public GroupViewHolder(SettingsExGroupBinding binding,
                               IGenericElementEvent<SettingsGroup, SettingsExGroupBinding> events,
                               IStateManager stateManager,
                               RecyclerView.RecycledViewPool sharedPool,
                               UserClientAppContext userContext) {
            super(binding, events, stateManager);
            this.sharedPool = sharedPool;
            this.userContext = userContext;
            //this.userContext.kill = stateManager.getSharedRegistry().isChecked("pkg_kill", this.userContext.appPackageName);
            initializeRecyclerView();
        }

        private void initializeRecyclerView() {
            if (isInitialized || containerAdapter != null) return;

            LinearLayoutManager layoutManager = new LinearLayoutManager(binding.getRoot().getContext());
            layoutManager.setInitialPrefetchItemCount(PREFETCH_COUNT);

            binding.recyclerView.setLayoutManager(layoutManager);
            binding.recyclerView.setRecycledViewPool(sharedPool);
            binding.recyclerView.setItemAnimator(null); // Disable animations for better performance
            binding.recyclerView.setHasFixedSize(true);

            containerAdapter = new OptimizedContainerAdapter(
                    binding.getRoot().getContext(),
                    null,                           //Set this from "this" to "null"
                    manager,
                    userContext);

            binding.recyclerView.setAdapter(containerAdapter);

            isInitialized = true;
        }

        @Override
        public void bind(SettingsGroup item) {
            currentItem = item;

            //We want to listen to changes on
            manager.getSharedViewControl()
                            .registerEventListeners(this, SharedViewControl.G_SETTINGS, SharedViewControl.G_S_CONTAINERS);

            binding.tvSettingGroupName.setText(item.getGroupName());

            SharedRegistry.ItemState state = sharedRegistry.getItemState(SharedRegistry.STATE_TAG_GROUPS, item.getGroupName());
            onGroupChange(null);
            sharedRegistry.putGroupChangeListener(this, item.getGroupName());

            updateExpandedStateForGroup(state.isExpanded, item);

            wireGroupEvents(true);
        }

        @Override
        public void bind(SettingsGroup item, List<Object> payloads) {
            if (payloads.isEmpty()) {
                bind(item);
                return;
            }

            currentItem = item;
            Bundle payload = (Bundle) payloads.get(0);
            if (payload.containsKey("groupName")) binding.tvSettingGroupName.setText(payload.getString("groupName"));
            if (payload.containsKey("containersSizeChanged")) containerAdapter.submitList(item.getContainers());
        }

        private void updateExpandedStateForGroup(boolean isExpanded, SettingsGroup item) {
            CoreUiUtils.setViewsVisibility(
                    binding.ivExpanderSettingGroup,
                    isExpanded,
                    binding.recyclerView,
                    binding.spSettingGroupRandomizer);

            List<SettingsContainer> containers = item.getContainers();
            if (isExpanded && containers != null && !containers.isEmpty())
                containerAdapter.submitList(containers);
        }

        public void handleExpandClickForGroup(SettingsGroup item) {
            boolean expanded = sharedRegistry.toggleExpanded(SharedRegistry.STATE_TAG_GROUPS, item.getGroupName());
            updateExpandedStateForGroup(expanded, item);
        }

        @Override
        protected void onViewDetached() {
            wireGroupEvents(false);
            containerAdapter.submitList(null);
            if(currentItem != null) {
                sharedRegistry.putGroupChangeListener(null, currentItem.getGroupName());
                currentItem = null;
            }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            if(currentItem == null) return;
            int id = view.getId();
            switch (id) {
                case R.id.tvSettingGroupName:
                case R.id.ivExpanderSettingGroup:
                case R.id.cvSettingGroup:
                    handleExpandClickForGroup(currentItem);
                    break;
            }
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if(currentItem == null)
                return;
            int id = compoundButton.getId();

            SharedViewControl sharedViewControl = getSharedViewControl();
            for(SettingsContainer container : currentItem.getContainers()) {
                for(SettingHolder holder : container.getSettings()) {
                    sharedViewControl.setChecked(SharedViewControl.G_SETTINGS, holder.getSharedId(), isChecked);
                }
            }


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
                CheckBoxState
                        .from(getAllSettings(), SharedRegistry.STATE_TAG_SETTINGS, sharedRegistry)
                        .updateCheckBox(binding.cbSettingGroupEnabled, this);
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
                for(SettingsContainer container : currentItem.getContainers()) {
                    for(SettingHolder holder : container.getSettings()) {
                        if(sharedViewControl.isChecked(SharedViewControl.G_SETTINGS, holder.getSharedId()))
                            yes++;
                        else
                            not++;
                    }
                }
                CheckBoxState.create(yes, yes + not).updateCheckBox(binding.cbSettingGroupEnabled, this);

                //We can make it also so object link

                //Ye but we need it to be all checked first ?
                //Should we let it update the states first ? then init events ?
                //Maybe look up all checked shit
                //Lets for now ignore this all
                //There is no easy way of going about tbh
                //Plus the last at the end needs some way of knowing the top ?
                //Fuck this UI shit
                //hmm

                //Hmm lets try this
                //Lets make a Check Pair System, Checks can have Children and Parents
                //Like a link
                //We can set all flags how ever starting from bottom ?

                //For the UI Coloring Bullshit

                //Apply State Changes First ?
                //If top is checked then it self & checks all children in state then notify

                //If Bottom is Checked it checks it self, then notifies
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
            //return currentItem != null && currentItem
            //return IUIViewControl.super.isView(id);
            return true;
        }
    }
}