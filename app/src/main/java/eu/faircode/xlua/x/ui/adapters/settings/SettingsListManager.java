package eu.faircode.xlua.x.ui.adapters.settings;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.databinding.SettingsExItemBinding;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.core.UINotifier;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.ui.core.view_registry.ChangedStatesPacket;
import eu.faircode.xlua.x.ui.core.view_registry.IStateChanged;
import eu.faircode.xlua.x.ui.core.view_registry.SettingSharedRegistry;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.core.adapter.ListViewManager;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;
import eu.faircode.xlua.x.ui.dialogs.TimePairsDialog;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;

public class SettingsListManager extends ListViewManager<SettingHolder, SettingsExItemBinding> {

    private static final String TAG = LibUtil.generateTag(SettingsListManager.class);

    public SettingsListManager(Context context, LinearLayout containerView, IStateManager stateManager) {
        super(context, containerView, stateManager);
    }

    @Override
    protected SettingsExItemBinding inflateItemView(ViewGroup parent) {
        SettingsExItemBinding binding = SettingsExItemBinding.inflate(inflater, parent, false);
        binding.getRoot().setTag(binding);
        return binding;
    }

    @Override
    protected String getStateTag() { return SharedRegistry.STATE_TAG_SETTINGS; }

    @Override
    protected void bindItemView(SettingsExItemBinding binding, SettingHolder setting) {
        if(setting != null) {
            binding.tvSettingExNameNice.setText(Str.getNonNullOrEmptyString(setting.getName(), "null"));
            binding.tiSettingExSettingValue.setText(Str.getNonNullString(setting.getNewValue(), Str.EMPTY));
            setupTextInputEx(binding.tvSettingExNameNice, binding.tiSettingExSettingValue, setting);
            setupCheckbox(binding.cbSettingExEnabled, setting, binding);
        }
    }

    private void setupTextInputEx(TextView tvName, EditText textInput, SettingHolder setting) {
        if(CoreUiUtils.SPECIAL_TIME_SETTINGS.contains(setting.getName()) || CoreUiUtils.SPECIAL_TIME_APP_SETTINGS.contains(setting.getName())) {

            setting.setBindings(tvName, textInput, null);
            setting.setNameLabelColor(context);

            textInput.setFocusable(false);
            textInput.setFocusableInTouchMode(false);
            textInput.setClickable(true);
            textInput.setCursorVisible(false);
            textInput.setInputType(InputType.TYPE_NULL);

            // Optional: Add a dropdown arrow drawable
            Drawable arrowDrawable = ContextCompat.getDrawable(textInput.getContext(), android.R.drawable.arrow_down_float);
            textInput.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowDrawable, null);
            textInput.setOnClickListener(view -> {
                String newVal = setting.getNewValue();
                String cleaned = Str.isEmpty(newVal) ? Str.EMPTY : newVal.replaceAll(Str.WHITE_SPACE, Str.EMPTY).toLowerCase();
                boolean isTimeKind = CoreUiUtils.APP_TIME_KINDS.contains(cleaned);
                TimePairsDialog.create()
                        .setTimePairs(isTimeKind ? new ArrayList<>() : Str.splitToList(cleaned)) // Optional: Set initial pairs
                        .setTimePairsFinishListener(timePairs -> {
                            String val = Str.joinList(timePairs);
                            if(Str.isEmpty(val))
                                val = null;

                            setting.setNewValue(val);
                            setting.ensureUiUpdated(val);
                            setting.setNameLabelColor(context);
                            setting.notifyUpdate(stateRegistry.notifier);
                        }).show(stateManager.getFragmentMan(), view.getContext().getString(R.string.title_time_pairs));
            });
        } else {
            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String str = s.toString();
                    setting.setNewValue(str);
                    setting.setNameLabelColor(context);
                    setting.notifyUpdate(stateRegistry.notifier);
                }
            };

            setting.setBindings(tvName, textInput, watcher);
            setting.setNameLabelColor(context);

            textInput.addTextChangedListener(watcher);

            //Extra shit
            textInput.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            });
        }
    }


    private void setupCheckbox(CheckBox checkbox, SettingHolder setting, SettingsExItemBinding binding) {
        SharedRegistry.ItemState state = stateRegistry.getItemState(SharedRegistry.STATE_TAG_SETTINGS, setting.getObjectId());
        checkbox.setChecked(state.isChecked);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                stateRegistry.setChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getObjectId(), isChecked);
                stateRegistry.notifyGroupChange(SettingsContainer.sharedContainerName(setting.getContainerName()), SharedRegistry.STATE_TAG_SETTINGS);
            }
        };

        checkbox.setOnCheckedChangeListener(onCheckedChangeListener);

        stateRegistry.putGroupChangeListener(new IStateChanged() {
            @Override
            public void onGroupChange(ChangedStatesPacket packet) {
                if(packet.isFrom(SharedRegistry.STATE_TAG_CONTAINERS)) {
                    boolean isChecked = stateRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getObjectId());
                    binding.cbSettingExEnabled.setOnCheckedChangeListener(null);
                    binding.cbSettingExEnabled.setChecked(isChecked);
                    binding.cbSettingExEnabled.setOnCheckedChangeListener(onCheckedChangeListener);
                }
            }
        }, setting.getObjectId());
    }

    @Override
    protected void cleanupItemView(SettingsExItemBinding binding) {
        binding.tiSettingExSettingValue.setOnFocusChangeListener(null);
        binding.tiSettingExSettingValue.addTextChangedListener(null);
        binding.cbSettingExEnabled.setOnCheckedChangeListener(null);
        stateRegistry.putGroupChangeListener(null, SharedRegistry.sharedSettingName(CoreUiUtils.getText(binding.tvSettingExNameNice)));
    }
}