package eu.faircode.xlua.x.ui.adapters.settings;
// For OptimizedSettingItemAdapter.java

// For ContainerViewHolder part (additional imports)


//Deprecated
/*public class OptimizedSettingItemAdapter extends EnhancedListAdapter<SettingHolder, SettingsExItemBinding, OptimizedSettingItemAdapter.SettingViewHolder> {

    public OptimizedSettingItemAdapter(Context context,
                                       IGenericElementEvent<SettingHolder, SettingsExItemBinding> events,
                                       IStateManager stateManager) {
        super(context, events, stateManager, null);
    }

    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SettingsExItemBinding binding = SettingsExItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SettingViewHolder(binding, events, stateManager);
    }

    static class SettingViewHolder extends BaseViewHolder<SettingHolder, SettingsExItemBinding>
            implements
            View.OnClickListener,
            CompoundButton.OnCheckedChangeListener {

        private static final String STATE_TAG = "settings";

        public SettingViewHolder(SettingsExItemBinding binding,
                                 IGenericElementEvent<SettingHolder, SettingsExItemBinding> events,
                                 IStateManager stateManager) {
            super(binding, events, stateManager);
        }

        @Override
        public void bind(SettingHolder item) {
            if (currentItem != item) {
                wireSettingEvents(false);
                currentItem = item;
            }

            binding.tvSettingExNameNice.setText(item.getNameNice());
            binding.tiSettingExSettingValue.setText(ObjectUtils.nullOrDefault(
                            Str.getNonNullOrEmptyString(item.getNewValue(), item.getValue()),
                            "")
            );

            ViewStateRegistry.ItemState state = stateRegistry.getItemState(STATE_TAG, item.getName());
            binding.cbSettingExEnabled.setChecked(state.isChecked);

            wireSettingEvents(true);
        }

        @Override
        public void bind(SettingHolder item, List<Object> payloads) {
            //ignore
        }

        @Override
        public void onClick(View view) {
            if(currentItem == null) return;
            // Handle clicks if needed
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(currentItem == null) return;
            int id = buttonView.getId();
            if(id == R.id.cbSettingExEnabled) {
                stateRegistry.setChecked(STATE_TAG, currentItem.getName(), isChecked);
            }
        }

        private void wireSettingEvents(boolean wire) {
            if (binding != null) {
                binding.cbSettingExEnabled.setOnCheckedChangeListener(wire ? this : null);
                // Add other event wiring as needed
            }
        }

        @Override
        protected void onViewDetached() {
            wireSettingEvents(false);
            currentItem = null;
        }
    }
}*/