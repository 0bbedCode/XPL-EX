package eu.faircode.xlua.x.ui.adapters;

/*private static class ContainerListAdapter extends BaseAdapter {
    private final Context context;
    private final IStateManager stateManager;
    private List<SettingsContainer> containers = new ArrayList<>();
    private final LayoutInflater inflater;

    ContainerListAdapter(Context context, IStateManager stateManager) {
        this.context = context;
        this.stateManager = stateManager;
        this.inflater = LayoutInflater.from(context);
    }

    void updateContainers(List<SettingsContainer> newContainers) {
        containers = new ArrayList<>(newContainers);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return containers.size();
    }

    @Override
    public SettingsContainer getItem(int position) {
        return containers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SettingsExItemContainerBinding binding;
        if (convertView == null) {
            binding = SettingsExItemContainerBinding.inflate(inflater, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (SettingsExItemContainerBinding) convertView.getTag();
        }

        SettingsContainer container = getItem(position);
        bindContainer(binding, container);
        return convertView;
    }

    private void bindContainer(SettingsExItemContainerBinding binding, SettingsContainer container) {
        binding.tvSettingContainerNameNice.setText(container.getNameNice());
        binding.tvSettingContainerNameFull.setText(container.getName());
        binding.tvSettingContainerDescription.setText(container.getDescription());

        ViewStateRegistry.ItemState state = stateManager.getItemState("containers", container.getContainerName());
        binding.cbSettingContainerEnabled.setChecked(state.isChecked);

        setupContainerListeners(binding, container);
    }

    private void setupContainerListeners(SettingsExItemContainerBinding binding, SettingsContainer container) {
        binding.cbSettingContainerEnabled.setOnCheckedChangeListener((buttonView, isChecked) ->
                stateManager.setChecked("containers", container.getContainerName(), isChecked));

        binding.ivExpanderSettingContainer.setOnClickListener(v -> {
            boolean expanded = stateManager.toggleExpanded("containers", container.getContainerName());
            updateContainerExpandedState(binding, expanded);
        });
    }

    private void updateContainerExpandedState(SettingsExItemContainerBinding binding, boolean expanded) {
        CoreUiUtils.setViewsVisibility(
                binding.ivExpanderSettingContainer,
                expanded,
                binding.recyclerView,
                binding.tvSettingContainerDescription,
                binding.spSettingContainerRandomizer
        );
    }
}*/
