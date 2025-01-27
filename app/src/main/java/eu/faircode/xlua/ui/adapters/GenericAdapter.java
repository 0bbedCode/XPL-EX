package eu.faircode.xlua.ui.adapters;


//Wrap this around SettingsEx Class still have the outer context that gets passed ?
//

//oh parent is just like this but for container type , ez


/*public class GenericAdapterOld<TElement, TBinding implements ViewBinding> extends ListAdapter<TElement, GenericAdapter.SettingsExViewHolder> {
    public interface OnAdapterClick<TElement> {
        void onClick(TElement object);
        void onItemLongClick(TElement object);
        void onBindingChanged(TElement object);
        void onIsInvalid(TElement object);
    }

    static class GenericViewHolder<TElement, TBinding> extends RecyclerView.ViewHolder {
        private

    }

    static class SettingsExViewHolder extends RecyclerView.ViewHolder {
        private SettingsExItemBinding binding;
        private RequestOptions options;
        private GenericAdapterOld.OnItemClickListener onClick;
        private SettingHolder object;

        SettingsExViewHolder(SettingsExItemBinding binding, GenericAdapterOld.OnItemClickListener onClick, RequestOptions requestOptions) {
            super(binding.getRoot());
            this.binding = binding;
            this.onClick = onClick;
            this.options = requestOptions;
        }

        void bind(SettingHolder object) {
            this.object = object;
        }

    }


    private static final DiffUtil.ItemCallback<SettingHolder> DIFF_CALLBACK = new DiffUtil.ItemCallback<SettingHolder>() {
        @Override
        public boolean areItemsTheSame(@NonNull SettingHolder oldItem, @NonNull SettingHolder newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull SettingHolder oldItem, @NonNull SettingHolder newItem) {
            return false;
        }
    }
}


public class SettingsExAdapterr extends ListAdapter<SettingHolder, GenericAdapterOld.SettingsExViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(SettingHolder setting);
        void onItemLongClick(SettingHolder setting);
    }


    static class SettingsExViewHolder extends RecyclerView.ViewHolder {
        private SettingsExItemBinding binding;
        private RequestOptions options;
        private GenericAdapterOld.OnItemClickListener onClick;
        private SettingHolder object;

        SettingsExViewHolder(SettingsExItemBinding binding, GenericAdapterOld.OnItemClickListener onClick, RequestOptions requestOptions) {
            super(binding.getRoot());
            this.binding = binding;
            this.onClick = onClick;
            this.options = requestOptions;
        }

        void bind(SettingHolder object) {
            this.object = object;
        }

    }


    private static final DiffUtil.ItemCallback<SettingHolder> DIFF_CALLBACK = new DiffUtil.ItemCallback<SettingHolder>() {
        @Override
        public boolean areItemsTheSame(@NonNull SettingHolder oldItem, @NonNull SettingHolder newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull SettingHolder oldItem, @NonNull SettingHolder newItem) {
            return false;
        }
    }
}*/
