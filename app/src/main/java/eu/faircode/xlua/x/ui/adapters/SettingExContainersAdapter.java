package eu.faircode.xlua.x.ui.adapters;


/**
 *
 * This will be the Element that Contains the Settings if Multiple, most have One Setting but some like SIM have (2)
 * Override "onViewRecycled(@NonNull ListAdapterItemViewHolder<SettingsContainer, SettingExItemContainerBinding> holder)" to Control Recycling
 *         super.onViewRecycled(holder);
 *         holder.clearImage();
 *
 *  Call to this Classes Constructor via with this, Fragment(requireContext(), this) so this will need to extend "IGenericElementEvent"
 *
 */
/*public class SettingExContainersAdapter
        extends ListGenericAdapter<
        SettingsContainer,
        SettingsExItemContainerBinding,
        SettingExContainersAdapter.SettingContainerViewHolder> {

    private static final String TAG = "XLua.SettingContainersAdapter";
    public SettingExContainersAdapter(Context context, IGenericElementEvent<SettingsContainer, SettingsExItemContainerBinding> events, IStateManager stateManager) {
        super(context, events, stateManager);
    }

    @NonNull
    @Override
    public SettingContainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SettingsExItemContainerBinding binding = SettingsExItemContainerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
         return new SettingContainerViewHolder(binding, getEvents(), getRequestOptions(), getStateManager());
    }

    @Override
    public void onBindViewHolder(@NonNull SettingContainerViewHolder holder, int position, @NonNull List<Object> payloads) {
        if(payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {

        }
    }

    static class SettingContainerViewHolder
            extends ListAdapterItemViewHolder<SettingsContainer, SettingsExItemContainerBinding>
            implements IGenericElementEvent<SettingHolder, SettingsExItemBinding> {

        private ListGenericAdapter<SettingHolder, SettingsExItemBinding, ?> adapter;
        public SettingContainerViewHolder(SettingsExItemContainerBinding binding, IGenericElementEvent<SettingsContainer, SettingsExItemContainerBinding> onEvents, RequestOptions requestOptions, IStateManager stateManager) {
            super(binding, onEvents, requestOptions, stateManager);
        }

        @Override
        public void bindObject(SettingsContainer object) {
            super.bindObject(object);
            if(hasBinding() && hasObject()) {
                if(DebugUtil.isDebug()) Log.d(TAG, "Is Binding Settings Container Object, String=" + Str.toStringOrNull(object));
                unWire();
                binding.tvSettingContainerNameNice.setText(object.getNameNice());
                binding.tvSettingContainerNameFull.setText(object.getName());
                binding.tvSettingContainerDescription.setText(object.getDescription());
                if(adapter == null) {
                    adapter = new SettingExItemAdapter(binding.recyclerView.getContext(), this, stateManager);
                    //new RecyclerViewWrapper<SettingHolder, SettingsExItemBinding>(binding.recyclerView)
                    //        .setVisibility(true)
                    //        .setHasFixedSize(true)
                    //        .useLinearLayoutManager(binding.recyclerView.getContext())
                    //        .ensureAdapterIsLinked(adapter);
                }

                if(events != null) events.onBindFinished(this);
                wire();
            }
        }

        @Override
        public void wire() { doWire(true); }

        @Override
        public void unWire() { doWire(false); }

        private void doWire(boolean shouldWire) {
            if(binding != null) {
                binding.cbSettingContainerEnabled.setOnCheckedChangeListener(shouldWire ? this : null);
                CoreUiUtils.linkEventsToView(
                        shouldWire,
                        this,
                        this,
                        binding.ivBtSettingContainerDelete,
                        binding.ivBtSettingContainerRandomize,
                        binding.ivBtSettingContainerReset,
                        binding.ivBtSettingContainerSave,
                        binding.ivExpanderSettingContainer);
            }
        }

        @Override
        public void onClick(IGenericViewHolder<SettingHolder, SettingsExItemBinding> holder, View view) {

        }

        @Override
        public void onLongClick(IGenericViewHolder<SettingHolder, SettingsExItemBinding> holder, View view) {

        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onCheckChanged(IGenericViewHolder<SettingHolder, SettingsExItemBinding> holder, CompoundButton compoundButton, boolean isChecked) {
            int id = compoundButton.getId();
            switch (id) {
                case R.id.cbSettingExEnabled:
                    //stateManager.getStateManager().flipEnabled(
                    //        "settings",
                    //        holder.getObject().getName(),
                    //        false,
                    //        false);
                    break;
            }
        }

        @Override
        public void onBindFinished(IGenericViewHolder<SettingHolder, SettingsExItemBinding> holder) {
            //Pair<Boolean, Boolean> state = stateManager.getStateManager().ensureState("settings", holder.getObject().getName(), false, false);
            //holder.getBinding().cbSettingExEnabled.setChecked(state.first);
        }
    }
}*/
