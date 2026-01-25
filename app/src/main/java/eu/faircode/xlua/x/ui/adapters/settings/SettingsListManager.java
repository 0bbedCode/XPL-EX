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
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.text.TextUtils;
import android.widget.Toast;
import android.view.LayoutInflater;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.databinding.SettingsExItemBinding;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.core.UINotifier;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.ui.core.view_registry.ChangedStatesPacket;
import eu.faircode.xlua.x.ui.core.view_registry.IStateChanged;
import eu.faircode.xlua.x.ui.core.view_registry.SettingSharedRegistry;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.core.adapter.ListViewManager;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;
import eu.faircode.xlua.x.ui.dialogs.SettingsSearchDialog;
import eu.faircode.xlua.x.ui.dialogs.TimePairsDialog;
import eu.faircode.xlua.x.ui.dialogs.wifi.WifiListDialog;
import eu.faircode.xlua.x.ui.dialogs.wifi.XWifiNetwork;
import eu.faircode.xlua.x.ui.dialogs.wifi.XWifiUtils;
import eu.faircode.xlua.x.ui.fragments.SettingFragmentUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

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

    //TODO: Make this shit cleaner I know you hate UI but still...!!!
    private void setupTextInputEx(TextView tvName, EditText textInput, SettingHolder setting) {
        if(RandomizersCache.SETTING_XP_DEFAULTS.equalsIgnoreCase(setting.getName())) {
            setting.setBindings(tvName, textInput, null);
            setting.setNameLabelColor(context);

            //Fix the Index Names
            textInput.setFocusable(false);
            textInput.setFocusableInTouchMode(false);
            textInput.setClickable(true);
            textInput.setCursorVisible(false);
            textInput.setInputType(InputType.TYPE_NULL);

            Drawable arrowDrawable = ContextCompat.getDrawable(textInput.getContext(), android.R.drawable.arrow_down_float);
            textInput.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowDrawable, null);

            textInput.setOnClickListener(view -> {
                SettingsSearchDialog.create()
                        .setSettings(SettingFragmentUtils.getAllSettingsFromFragment(this.stateManager.getAsFragment()))
                        .setCheckedFromValue(setting)
                        .removeParentSettingFromList()
                        .setOnFinishListener((a, b) -> {
                            final String lstString = Str.joinList(b, Str.NEW_LINE);
                            final String base64 = Str.toBase64String(lstString, Str.CHAR_SET_UTF_8);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Total Settings (%s) were Checked out of (%s) settings",
                                        ListUtil.size(b),
                                        ListUtil.size(a)));

                            //ToDo:
                            //So Pretty I see my flow (almost least this set listener block)
                            //Use Styles such as this, combining TryRun, Dialog Util sm CoreUiUtils etc etc
                            //Just like Str we can make a big Class called Ui or Something >:)
                            setting.setNewValue(base64);
                            TryRun.onMain(() -> {
                                setting.ensureUiUpdated(base64);
                                setting.setNameLabelColor(context);
                                setting.notifyUpdate(stateRegistry.notifier);
                            });
                        })
                        .show(this.stateManager.getFragmentMan(), context.getString(R.string.title_settings_search));
            });
        }
        else if(RandomizersCache.SETTING_NETWORK_ALLOW_LIST.equalsIgnoreCase(setting.getName())) {

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
                List<XWifiNetwork> items = XWifiUtils.fromBase64String(newVal);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Wifi Networks Saved=" + items.size());

                WifiListDialog.create()
                        .setList(items)
                        .setCallback(new WifiListDialog.WifiNetworkCallback() {
                            @Override
                            public void onNetworksUpdated(List<XWifiNetwork> updatedNetworks) {
                                // Save the updated networks list
                                //saveNetworks(updatedNetworks);
                                if(!ListUtil.isValid(updatedNetworks))
                                    return;

                                String newValue = XWifiUtils.toBase64String(updatedNetworks);
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, "Networks Being Saved=" + ListUtil.size(updatedNetworks) + " New Value=" + newValue);

                                setting.setNewValue(newValue);
                                setting.ensureUiUpdated(newValue);
                                setting.setNameLabelColor(context);
                                setting.notifyUpdate(stateRegistry.notifier);
                            }
                        })
                        .show(stateManager.getFragmentMan(), context.getString(R.string.title_wifi_networks));
            });
        }
        else if(CoreUiUtils.SPECIAL_TIME_SETTINGS.contains(setting.getName()) ||
                CoreUiUtils.SPECIAL_TIME_APP_SETTINGS.contains(setting.getName())) {

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
        } else if (setting.getName().startsWith("location.")) {
            setting.setBindings(tvName, textInput, null);
            setting.setNameLabelColor(context);

            textInput.setFocusable(false);
            textInput.setFocusableInTouchMode(false);
            textInput.setClickable(true);
            textInput.setCursorVisible(false);
            textInput.setInputType(InputType.TYPE_NULL);

            // Using search icon if available, or fallback to arrow
            Drawable searchDrawable = ContextCompat.getDrawable(textInput.getContext(), R.drawable.ic_search);
            textInput.setCompoundDrawablesWithIntrinsicBounds(null, null, searchDrawable, null);

            textInput.setOnClickListener(view -> showLocationSearch(view.getContext(), setting));
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

    private void showLocationSearch(Context context, SettingHolder currentSetting) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Search Location");

        View dialogView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
        // We need a custom layout with edit text and list view, let's create it programmatically to be safe or simple

        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        final android.widget.EditText input = new android.widget.EditText(context);
        input.setHint("Enter address, city, or place...");
        layout.addView(input);

        final android.widget.Button searchBtn = new android.widget.Button(context);
        searchBtn.setText("Search");
        layout.addView(searchBtn);

        final android.widget.ListView listView = new android.widget.ListView(context);
        listView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(listView);

        builder.setView(layout);
        builder.setNegativeButton("Cancel", null);

        final android.app.AlertDialog dialog = builder.create();

        searchBtn.setOnClickListener(v -> {
            String query = input.getText().toString();
            if (TextUtils.isEmpty(query)) return;

            new Thread(() -> {
                try {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocationName(query, 10);

                    v.post(() -> {
                        if (addresses == null || addresses.isEmpty()) {
                            Toast.makeText(context, "No results found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
                        final List<Address> addressList = new ArrayList<>(addresses);

                        for (Address addr : addresses) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i <= addr.getMaxAddressLineIndex(); i++) {
                                sb.append(addr.getAddressLine(i)).append(" ");
                            }
                            adapter.add(sb.toString());
                        }

                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((parent, view1, position, id) -> {
                            Address selected = addressList.get(position);
                            double lat = selected.getLatitude();
                            double lon = selected.getLongitude();

                            updateLocationSettings(context, lat, lon);
                            dialog.dismiss();
                        });
                    });
                } catch (Exception e) {
                    v.post(() -> Toast.makeText(context, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        dialog.show();
    }

    private void updateLocationSettings(Context context, double lat, double lon) {
        // We need to update both latitude and longitude settings.
        // We can access all settings via the shared registry or state manager if possible.
        // Or access the container settings if we know the container.

        SettingSharedRegistry settingShared = stateRegistry.getSharedRegistry().asSettingShared();
        // Assuming location settings are in a container named "location" or similar, or we can iterate all loaded settings.
        // We don't easily have the container instance here, but we can look up settings by name if we iterate.

        // Iterate over all settings managed by the shared registry for the current view context
        // This might be expensive if many settings, but usually it's fine for a click action.

        // Setting names are "location.latitude" and "location.longitude"

        // We can use the registry to find them if they are loaded.
        // But settingShared usually stores things by container.

        // Let's try to find them in the current list managed by this adapter/manager if possible,
        // but ListViewManager manages a specific list of items passed to it.
        // SettingsListManager extends ListViewManager<SettingHolder, ...>

        // We can try to iterate the settings in the current container if we had a reference to it.
        // But SettingsListManager is initialized with a container view, not the SettingsContainer object directly.

        // However, we can access the fragment's settings via SettingFragmentUtils if available.
        List<SettingHolder> allSettings = SettingFragmentUtils.getAllSettingsFromFragment(this.stateManager.getAsFragment());

        if (ListUtil.isValid(allSettings)) {
            for (SettingHolder s : allSettings) {
                if ("location.latitude".equals(s.getName())) {
                    String val = String.valueOf(lat);
                    s.setNewValue(val);
                    s.ensureUiUpdated(val);
                    s.notifyUpdate(stateRegistry.notifier);
                } else if ("location.longitude".equals(s.getName())) {
                    String val = String.valueOf(lon);
                    s.setNewValue(val);
                    s.ensureUiUpdated(val);
                    s.notifyUpdate(stateRegistry.notifier);
                }
            }
            Toast.makeText(context, "Location updated: " + lat + ", " + lon, Toast.LENGTH_SHORT).show();
        } else {
             Toast.makeText(context, "Could not find location settings to update", Toast.LENGTH_SHORT).show();
        }
    }
}