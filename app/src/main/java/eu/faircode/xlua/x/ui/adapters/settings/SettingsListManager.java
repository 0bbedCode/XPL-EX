package eu.faircode.xlua.x.ui.adapters.settings;
import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.databinding.SettingsExItemBinding;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.core.view_registry.ChangedStatesPacket;
import eu.faircode.xlua.x.ui.core.view_registry.IStateChanged;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.core.adapter.ListViewManager;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;

public class SettingsListManager extends ListViewManager<SettingHolder, SettingsExItemBinding> {

    private static final String TAG = "XLua.SettingsListManager";

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
            if(DebugUtil.isDebug()) Log.d(TAG, "[bindItemView] Binding Setting=" + Str.toStringOrNull(setting));
            binding.tvSettingExNameNice.setText(Str.getNonNullOrEmptyString(setting.getName(), "null"));
            //binding.tiSettingExSettingValue.setText(
            //        ObjectUtils.nullOrDefault(
            //                Str.getNonNullOrEmptyString(setting.getNewValue(), setting.getValue()), ""));


            binding.tiSettingExSettingValue.setText(Str.getNonNullString(setting.getNewValue(), ""));

            setupTextInputEx(binding.tvSettingExNameNice, binding.tiSettingExSettingValue, setting);
            setupCheckbox(binding.cbSettingExEnabled, setting, binding);


            //Get value either from ".value" or ".newValue"

            //Bind to the actual object
        }
    }

    private void setupTextInputEx(TextView tvName, EditText textInput, SettingHolder setting) {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (setting != null) {
                    String str = s.toString();
                    setting.setNewValue(str);
                    setting.setNameLabelColor(context);
                }
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


    private void setupTextInput(TextView tvName, TextInputEditText textInput, SettingHolder setting) {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (setting != null) {
                    String str = s.toString();
                    setting.setNewValue(str);
                    setting.setNameLabelColor(context);
                }
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

        //PoC
        /*int[] backspaceCount = {0}; // Track backspace presses
        long lastBackspaceTime = 0;
        final long BACKSPACE_TIMEOUT = 1000; // Reset counter after 1 second

        textInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                Editable editable = textInput.getText();
                if (keyCode == KeyEvent.KEYCODE_DEL && editable != null && editable.length() == 0) {
                    long currentTime = System.currentTimeMillis();

                    // Reset counter if too much time has passed
                    if (currentTime - lastBackspaceTime > BACKSPACE_TIMEOUT) {
                        backspaceCount[0] = 0;
                    }

                    backspaceCount[0]++;
                    lastBackspaceTime = currentTime;

                    // After 3 quick backspaces on empty field
                    if (backspaceCount[0] >= 3) {
                        // Show delete prompt
                        new AlertDialog.Builder(context)
                                .setTitle("Delete?")
                                .setMessage("Do you want to delete this item?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    // Handle delete
                                })
                                .setNegativeButton("No", null)
                                .show();
                        backspaceCount[0] = 0; // Reset counter
                        return true;
                    }
                }
            }
            return false;
        });*/
    }

    private void setupCheckbox(CheckBox checkbox, SettingHolder setting, SettingsExItemBinding binding) {
        SharedRegistry.ItemState state = stateRegistry.getItemState(SharedRegistry.STATE_TAG_SETTINGS, setting.getId());
        checkbox.setChecked(state.isChecked);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                stateRegistry.setChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getId(), isChecked);
                stateRegistry.notifyGroupChange(SettingsContainer.sharedContainerName(setting.getContainerName()), SharedRegistry.STATE_TAG_SETTINGS);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "On Checked : is Checked=" + isChecked + " Container Name=" + setting.getContainerName() + " Setting Name=" + setting.getName());
            }
        };

        checkbox.setOnCheckedChangeListener(onCheckedChangeListener);

        stateRegistry.putGroupChangeListener(new IStateChanged() {
            @Override
            public void onGroupChange(ChangedStatesPacket packet) {
                if(packet.isFrom(SharedRegistry.STATE_TAG_CONTAINERS)) {
                    boolean isChecked = stateRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getId());
                    binding.cbSettingExEnabled.setOnCheckedChangeListener(null);
                    binding.cbSettingExEnabled.setChecked(isChecked);
                    binding.cbSettingExEnabled.setOnCheckedChangeListener(onCheckedChangeListener);
                }
            }
        }, setting.getId());
    }

    @Override
    protected void cleanupItemView(SettingsExItemBinding binding) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "[cleanupItemView] Cleaning up List View Stack=" + RuntimeUtils.getStackTraceSafeString());

        binding.tiSettingExSettingValue.setOnFocusChangeListener(null);
        binding.tiSettingExSettingValue.addTextChangedListener(null);
        binding.cbSettingExEnabled.setOnCheckedChangeListener(null);


        //stateRegistry.putGroupChangeListener(null, binding.tvSettingExNameNice.toString());
        stateRegistry.putGroupChangeListener(null, Str.combine("setting:", binding.tvSettingExNameNice.getText().toString(), false));
    }
}