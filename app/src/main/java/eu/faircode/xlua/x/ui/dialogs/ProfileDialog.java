package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.adapters.DirectoryAdapter;
import eu.faircode.xlua.x.ui.adapters.SettingSmallAdapter;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.commands.call.GetAppDirectoriesCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetProfileCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetProfileListCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutAppProfileCommand;
import eu.faircode.xlua.x.xlua.configs.AppProfile;
import eu.faircode.xlua.x.xlua.configs.PathDetails;
import eu.faircode.xlua.x.xlua.configs.XPConfig;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.hook.HookGroupOrganizer;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class ProfileDialog extends AppCompatDialogFragment {
    private static final String TAG = "XLua.ProfileDialog";

    public static ProfileDialog create() { return new ProfileDialog(); }

    private Context context;
    private UserClientAppContext app;
    private UserIdentity identity;
    private final List<PathDetails> paths = new ArrayList<>();
    private final List<SettingPacket> settings = new ArrayList<>();
    private final List<String> hooks = new ArrayList<>();
    private final SharedRegistry sharedRegistry = new SharedRegistry();

    private final List<String> knownProfiles = new ArrayList<>();

    public static final int POS_FLAG_CREATE = 0x0;
    public static final int POS_FLAG_UPDATE = 0x1;

    private int posFlag = 0x0;


    public ProfileDialog initKnownProfiles(Context context) {
        List<String> kp = GetProfileListCommand.get(context, app.appUid, app.appPackageName);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Got Known Profiles, Profiles=" + Str.joinList(kp));

        ListUtil.addAllIfValid(this.knownProfiles, kp);
        if(DebugUtil.isDebug() && knownProfiles.contains("Test"))
            knownProfiles.add("Test");


        return this;
    }

    public ProfileDialog setApp(Context context, UserClientAppContext app) {
        this.app = app;
        this.identity = UserIdentity.fromUid(app.appUid, app.appPackageName);
        ListUtil.addAllIfValid(this.paths, GetAppDirectoriesCommand.get(context, this.identity), true);
        return this;
    }

    public ProfileDialog setSettings(Context context, List<SettingHolder> settings) {
        for(SettingHolder setting : settings) {
            SettingPacket packet = new SettingPacket(setting.getName(), setting.getValue());
            this.settings.add(packet);
            sharedRegistry.setChecked(SharedRegistry.STATE_TAG_SETTINGS, packet.getObjectId(), true); // Auto-check all settings
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Settings List=" + ListUtil.size(this.settings));

        ListUtil.addAllIfValid(this.hooks, HookGroupOrganizer.getHookIdsFromSettingPackets(context, this.settings));
        if(DebugUtil.isDebug())
            Log.d(TAG, "Settings List=" + ListUtil.size(this.settings) + " Hook Count Size=" + ListUtil.size(this.hooks));

        return this;
    }



    public Pair<Integer, Integer> getStatsExample() {
        int checked = 0;
        int total = this.settings.size();
        for(SettingPacket setting : this.settings)
            if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getObjectId()))
                checked++;

        return Pair.create(checked, total);
    }


    public ProfileDialog setHooks(List<String> hookIds) {
        //Send IDS here
        return this;
    }

    /*
           ToDO: If Apply Is selected then set setting "0,pkg,profile_name,input"
                    Have same profile treatment next to JSON icon in Main screen under app
                    Hmm have "soft" handling for the hooks, as in list hooks that were linked to those checked settings, easy change

                   ToDo: possible clear logs ez but no i want
                        Add a button to check all that ONLY are set values
                        Also finish force debug flag setter



     */

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.profile_dialog, null);

        // Initialize UI components
        ImageView ivAppIcon = view.findViewById(R.id.ivAppIcon);
        TextView tvAppName = view.findViewById(R.id.tvAppName);
        TextView tvAppPkg = view.findViewById(R.id.tvAppPackageName);
        TextView tvAppUid = view.findViewById(R.id.tvAppUid);
        ListView lvDirectories = view.findViewById(R.id.listviewDirectories);
        ListView lvSettings = view.findViewById(R.id.listviewSettings);
        ImageView ivExpanderProfileSettings = view.findViewById(R.id.ivExpanderProfileSettings);
        CheckBox cbCheckSettingsBulk = view.findViewById(R.id.cbCheckSettingsBulk);
        TextView tvSelectedSettingsLabel = view.findViewById(R.id.tvSelectedSettingsLabel);

        EditText tiProfileName = view.findViewById(R.id.tiProfileName);
        EditText tiProfileVersion = view.findViewById(R.id.tiProfileVersion);
        EditText tiProfileDescription = view.findViewById(R.id.tiProfileDescription);

        // Set app data
        app.setImageViewTextViewTexts(context, ivAppIcon, tvAppName, tvAppPkg, tvAppUid);

        // Directories ListView setup
        lvDirectories.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        DirectoryAdapter directoryAdapter = new DirectoryAdapter(context, paths);
        lvDirectories.setAdapter(directoryAdapter);
        for (int i = 0; i < lvDirectories.getCount(); i++) {
            lvDirectories.setItemChecked(i, true);
        }

        // Initialize the Settings Adapter
        SettingSmallAdapter settingsAdapter = new SettingSmallAdapter(context, this.settings, sharedRegistry, () -> {
            updateBulkCheckboxAndLabel(cbCheckSettingsBulk, tvSelectedSettingsLabel);
        });
        lvSettings.setAdapter(settingsAdapter);

        // Handle bulk checkbox click
        cbCheckSettingsBulk.setOnClickListener(v -> {
            boolean bulkChecked = cbCheckSettingsBulk.isChecked();
            for (SettingPacket setting : settings) {
                sharedRegistry.setChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getObjectId(), bulkChecked);
            }
            settingsAdapter.notifyDataSetChanged(); // Update UI
            updateBulkCheckboxAndLabel(cbCheckSettingsBulk, tvSelectedSettingsLabel);
        });

        // Handle the expander for settings
        boolean isExpanded = sharedRegistry.isExpanded(SharedRegistry.STATE_TAG_SETTINGS, "view_settings");
        int totalSettings = settings.size();
        updateExpanded(isExpanded, ivExpanderProfileSettings, lvSettings);

        if (totalSettings == 0) {
            // No settings: Update UI for disabled state
            tvSelectedSettingsLabel.setText("---");
            ivExpanderProfileSettings.setClickable(false); // Disable interaction
            ivExpanderProfileSettings.setAlpha(0.3f); // Dim the expander visually
        } else {
            // Initialize expander state
            updateExpanded(isExpanded, ivExpanderProfileSettings, lvSettings);

            ivExpanderProfileSettings.setOnClickListener(v -> {
                boolean newState = sharedRegistry.toggleExpanded(SharedRegistry.STATE_TAG_SETTINGS, "view_settings");
                updateExpanded(newState, ivExpanderProfileSettings, lvSettings);
            });
        }

        // Initialize bulk checkbox and label state
        updateBulkCheckboxAndLabel(cbCheckSettingsBulk, tvSelectedSettingsLabel);

        // Add logic to the profile name input field
        builder.setView(view)
                .setTitle(R.string.title_profile_manager)
                .setNegativeButton(R.string.option_cancel, (dialog, which) -> {
                    // Handle cancel button
                })
                .setPositiveButton(R.string.option_create, (dialog, which) -> {
                    //They need to "click" yes to "apply" it WHEN "created"
                    //??Yes ??
                    String profileName = Str.toString(tiProfileName, Str.EMPTY);
                    String profileVersion = Str.toString(tiProfileVersion, Str.EMPTY);
                    String profileDescription = Str.toString(tiProfileDescription, Str.EMPTY);

                    if(TextUtils.isEmpty(profileName)) {
                        Log.e(TAG, "Error on positive? Empty or Null Profile Name??");
                        return;
                    }

                    AppProfile profile = new AppProfile();
                    profile.setUserIdentity(UserIdentity.fromUid(app.appUid, app.appPackageName));
                    profile.setActionPacket(ActionPacket.create(posFlag == POS_FLAG_CREATE ? ActionFlag.PUSH: ActionFlag.UPDATE, app.kill));
                    profile.name = profileName;
                    profile.version = profileVersion;
                    profile.description = profileDescription;

                    profile.creationDate = System.currentTimeMillis();
                    profile.lastApplied = -1;

                    List<PathDetails> enabledDirs = new ArrayList<>();
                    for(int i = 0; i < lvDirectories.getCount(); i++) {
                        PathDetails p = paths.get(i);
                        if(lvDirectories.isItemChecked(i))
                            enabledDirs.add(p);

                    }

                    ListUtil.addAllIfValid(profile.fileBackups, enabledDirs);
                    List<SettingPacket> enabledSettings = new ArrayList<>();
                    for(SettingPacket setting : settings)
                        if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getObjectId()))
                            enabledSettings.add(setting);

                    XPConfig config = XPConfig.create(profile, null, enabledSettings);
                    profile.config = config;

                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Sending Put Profile Packet=" + profile);

                    A_CODE result = PutAppProfileCommand.callEx(context, profile);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Sent Profile Put Packet, Result=" + result.name() + " Profile=" + profile);

                });

        AlertDialog dialog = builder.create();

        // Add "Open" button dynamically
        dialog.setOnShowListener(d -> {
            // Add "Open" button and disable initially
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);

            tiProfileName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // No action needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // No action needed
                }

                public void afterTextChanged(Editable s) {
                    String input = s.toString().trim();
                    boolean isKnownProfile = knownProfiles.contains(input);
                    boolean isEmpty = input.isEmpty();

                    // Update positive button
                    posFlag = isKnownProfile ? POS_FLAG_UPDATE : POS_FLAG_CREATE;
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(isKnownProfile ? R.string.option_update : R.string.option_create);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!isEmpty);

                    if(isKnownProfile && DebugUtil.isDebug()) {
                        try {
                            AppProfile profile = GetProfileCommand.get(context, app.appUid, app.appPackageName, input);
                            Log.d(TAG, "Got Profile from input: " + input + " Profile=" + Str.toStringOrNull(profile));

                        }catch (Exception e) {
                            Log.e(TAG, "Failed to Get Known Profile from User Input: Error=" + e + " STR=====" + input);
                        }
                    }

                    // Handle "Open" button
                    if (isKnownProfile) {
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setText(R.string.option_open);
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true); // Enable the "Open" button
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                            // Handle "Open" button click
                            openProfile(input);
                        });
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
                    }
                }
            });
        });

        return dialog;
    }

    /**
     * Method to open a profile.
     */
    private void openProfile(String profileName) {
        // Logic to open the profile
        Log.d(TAG, "Opening profile: " + profileName);
    }



    /**
     * Updates the expanded state of the settings list view using CoreUiUtils.setViewsVisibility.
     */
    private void updateExpanded(boolean isExpanded, ImageView ivExpander, ListView list) {
        CoreUiUtils.setViewsVisibility(
                ivExpander,
                isExpanded,
                list
        );
    }

    private void updateBulkCheckboxAndLabel(CheckBox cbCheckSettingsBulk, TextView tvSelectedSettingsLabel) {
        int checked = 0;
        int total = settings.size();

        // Handle the case where there are no settings
        if (total == 0) {
            tvSelectedSettingsLabel.setText("---");
            cbCheckSettingsBulk.setChecked(false);
            cbCheckSettingsBulk.setButtonTintList(context.getResources().getColorStateList(android.R.color.darker_gray, null));
            return; // Exit early, nothing else to update
        }

        // Count the number of checked items
        for (SettingPacket setting : settings) {
            if (sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getObjectId())) {
                checked++;
            }
        }

        // Determine the bulk checkbox state
        if (checked == 0) {
            // None checked: Unchecked bulk checkbox
            cbCheckSettingsBulk.setChecked(false);
            cbCheckSettingsBulk.setButtonTintList(context.getResources().getColorStateList(android.R.color.darker_gray, null));
        } else if (checked == total) {
            // All checked: Checked bulk checkbox with accent color
            cbCheckSettingsBulk.setChecked(true);
            cbCheckSettingsBulk.setButtonTintList(context.getResources().getColorStateList(R.color.colorAccent, null));
        } else {
            // Some checked: Checked bulk checkbox with gray color
            cbCheckSettingsBulk.setChecked(true);
            cbCheckSettingsBulk.setButtonTintList(context.getResources().getColorStateList(android.R.color.darker_gray, null));
        }

        // Update the selected settings label
        tvSelectedSettingsLabel.setText(String.format("%d/%d", checked, total));
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
