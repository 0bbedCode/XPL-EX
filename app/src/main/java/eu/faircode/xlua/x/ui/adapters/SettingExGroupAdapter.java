package eu.faircode.xlua.x.ui.adapters;


/*public class SettingExGroupAdapter
        extends
        ListGenericAdapter<SettingsGroup, SettingsExGroupBinding, SettingExGroupAdapter.SettingGroupViewHolder> {

    private static final String TAG = "XLua.SettingsExGroupAdapter";
    public SettingExGroupAdapter(
            Context context,
            IGenericElementEvent<SettingsGroup, SettingsExGroupBinding> events,
            IStateManager stateManager) {
        super(context, events, stateManager);
    }

    @NonNull
    @Override
    public SettingGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SettingsExGroupBinding binding = SettingsExGroupBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SettingGroupViewHolder(binding, getEvents(), getRequestOptions(), getStateManager());
    }

    static class SettingGroupViewHolder
            extends ListAdapterItemViewHolder<SettingsGroup, SettingsExGroupBinding>
            implements IGenericElementEvent<SettingsContainer, SettingsExItemContainerBinding> {

        public SettingGroupViewHolder(SettingsExGroupBinding binding, IGenericElementEvent<SettingsGroup, SettingsExGroupBinding> onEvents, RequestOptions requestOptions, IStateManager stateManager) {
            super(binding, onEvents, requestOptions, stateManager);
            //adapter = new SettingExContainersAdapter(binding.recyclerView.getContext(), this, stateManager);
            //new RecyclerViewWrapper<SettingsContainer, SettingsExItemContainerBinding>(binding.recyclerView)
            //        .setVisibility(true)
            //        .setHasFixedSize(true)
            //        .useLinearLayoutManager(binding.recyclerView.getContext())
            //        .ensureAdapterIsLinked(adapter);
        }

        private ListGenericAdapter<SettingsContainer, SettingsExItemContainerBinding, ?> adapter;

        @Override
        public void bindObject(SettingsGroup object) {
            super.bindObject(object);
            if(this.binding != null && this.object != null) {
                if(DebugUtil.isDebug()) Log.d(TAG, "Is Binding Settings Group Object, String=" + Str.toStringOrNull(object));
                unWire();
                binding.tvSettingGroupName.setText(object.getGroupName());

                List<SettingsContainer> containers = object.getContainers();
                adapter.submitList(object.getContainers());


                if(events != null) events.onBindFinished(this);
                //super.createViewModel(SettingsExGroupViewModel.class, true);
                wire();
            }
        }

        @Override
        public void wire() { doWire(true); }

        @Override
        public void unWire() { doWire(false); }

        private void doWire(boolean shouldWire) {
            if(binding != null) {
                binding.cbSettingGroupEnabled.setOnCheckedChangeListener(shouldWire ? this : null);
                CoreUiUtils.linkEventsToView(shouldWire, this, this, binding.spSettingGroupRandomizer, binding.ivExpanderSettingGroup);
            }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(IGenericViewHolder<SettingsContainer, SettingsExItemContainerBinding> holder, View view) {
            int id = CoreUiLog.getViewIdOnClickItem(view);
            SettingsExItemContainerBinding binding = holder.getBinding();
            switch (id) {
                case R.id.ivExpanderSettingContainer:
                    //stateManager.getStateManager().flipExpanded(
                    //        "containers",
                    //        holder.getObject().getContainerName(), false, false).second
                    boolean isExpanded = stateManager.getStateManager().isExpanded("containers", holder.getObject().getContainerName());
                    CoreUiUtils.setViewsVisibility(
                            binding.ivExpanderSettingContainer,
                            isExpanded,
                            binding.recyclerView,
                            binding.tvSettingContainerDescription,
                            binding.ivBtSettingContainerDelete,
                            binding.ivBtSettingContainerRandomize,
                            binding.ivBtSettingContainerReset,
                            binding.ivBtSettingContainerSave,
                            binding.spSettingContainerRandomizer);

                    break;
            }
        }

        @Override
        public void onLongClick(IGenericViewHolder<SettingsContainer, SettingsExItemContainerBinding> holder, View view) {

        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onCheckChanged(IGenericViewHolder<SettingsContainer, SettingsExItemContainerBinding> holder, CompoundButton compoundButton, boolean isChecked) {
            int id = compoundButton.getId();
            switch (id) {
                case R.id.cbSettingContainerEnabled:
                    //No longer called stateManager

                    stateManager.getStateManager().toggleChecked("containers", holder.getObject().getContainerName());
                    //stateManager.getStateManager().flipEnabled(
                    //        "containers",
                    //        holder.getObject().getContainerName(),
                    //        false,
                    //        false);
                    break;
            }
        }

        @Override
        public void onBindFinished(IGenericViewHolder<SettingsContainer, SettingsExItemContainerBinding> holder) {
            SettingsExItemContainerBinding binding = holder.getBinding();

            ViewStateRegistry.ItemState state = stateManager.getStateManager().getItemState("containers", holder.getObject().getContainerName());
            //Pair<Boolean, Boolean> pair = stateManager.getStateManager().ensureState("containers", holder.getObject().getContainerName(), false, false);
            //binding.cbSettingContainerEnabled.setChecked(pair.first);
            binding.cbSettingContainerEnabled.setChecked(state.isChecked);

            CoreUiUtils.setViewsVisibility(
                    binding.ivExpanderSettingContainer,
                    //pair.second,
                    state.isExpanded,
                    binding.recyclerView,
                    binding.tvSettingContainerDescription,
                    binding.ivBtSettingContainerDelete,
                    binding.ivBtSettingContainerRandomize,
                    binding.ivBtSettingContainerReset,
                    binding.ivBtSettingContainerSave,
                    binding.spSettingContainerRandomizer);
        }
    }
}*/

//in bindObject

                /*if(adapter == null) {
                    adapter = new SettingExContainersAdapter(binding.recyclerView.getContext(), this, stateManager);
                    new RecyclerViewWrapper<SettingsContainer, SettingsExItemContainerBinding>(binding.recyclerView)
                            .setVisibility(true)
                            .setHasFixedSize(true)
                            .useLinearLayoutManager(binding.recyclerView.getContext())
                            .ensureAdapterIsLinked(adapter);
                    //To "filter" it will "rebind" in the MAIN GROUP Repo Have it go through ? Filter out not needed ?
                    //Or pass the filter object down or from down get from up
                }*/



                /*if (containerViewModel == null) {
                    Fragment fragment = FragmentManager.findFragment(binding.getRoot());
                    containerViewModel = new ViewModelProvider(fragment).get(SettingsExContainerViewModel.class);
                    containerViewModel.setRepository(SettingsRepository.INSTANCE);
                }


                if (containerViewModel != null && binding.getRoot().isAttachedToWindow()) {
                    Fragment fragment = FragmentManager.findFragment(binding.getRoot());
                    containerViewModel.getRawLiveData().observe(fragment.getViewLifecycleOwner(), containers -> {
                        if (adapter != null) {
                            adapter.submitList(containers);
                            //containerViewModel.setIsRefreshing(false);
                            //if (elements != null) {
                            //    holder.onDataChanged(elements);
                            //}
                        }
                    });
                }*/

                /*Fragment fragment = (Fragment)holder;
                TViewModel model = new ViewModelProvider(fragment).get(classModel);
                IListViewModel<TElement> viewModel = (IListViewModel<TElement>)model;
                if(setUserContext) {
                    holder.ensureHasUserContext(false);  // Use the interface method
                    IUserContext iCX = viewModel.getAsUserContext();
                    iCX.setUserContext(holder.getUserContext());
                }
                holder.setViewModel(viewModel);*/