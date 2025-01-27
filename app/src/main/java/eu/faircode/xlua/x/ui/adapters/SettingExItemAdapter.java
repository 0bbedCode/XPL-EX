package eu.faircode.xlua.x.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.request.RequestOptions;

import eu.faircode.xlua.databinding.SettingsExItemBinding;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.ui.core.adapter.ListGenericAdapter;
import eu.faircode.xlua.x.ui.core.interfaces.IGenericElementEvent;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;
import eu.faircode.xlua.x.ui.core.model.ListAdapterItemViewHolder;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;

public class SettingExItemAdapter
    extends ListGenericAdapter<
        SettingHolder,
        SettingsExItemBinding,
        SettingExItemAdapter.SettingExItemViewHolder> {

    public SettingExItemAdapter(Context context, IGenericElementEvent<SettingHolder, SettingsExItemBinding> events, IStateManager stateManager) {
        super(context, events, stateManager);
    }

    @NonNull
    @Override
    public SettingExItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SettingsExItemBinding binding = SettingsExItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SettingExItemViewHolder(binding, getEvents(), getRequestOptions(), getStateManager());
    }

    static class SettingExItemViewHolder extends ListAdapterItemViewHolder<SettingHolder, SettingsExItemBinding> {
        public SettingExItemViewHolder(SettingsExItemBinding binding,
                                       IGenericElementEvent<SettingHolder, SettingsExItemBinding> events,
                                       RequestOptions requestOptions,
                                       IStateManager stateManager) {
            super(binding, events, requestOptions, stateManager);
        }

        @Override
        public void bindObject(SettingHolder object) {
            super.bindObject(object);
            if(hasBinding() && hasObject()) {
                unWire();
                //Our parent worries about our states ?
                binding.tiSettingExSettingValue.setText(
                        ObjectUtils.nullOrDefault(Str.getNonNullOrEmptyString(object.getNewValue(), object.getValue()), ""));

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
                binding.cbSettingExEnabled.setOnCheckedChangeListener(shouldWire ? this : null);
                //Text input
            }
        }
    }
}
