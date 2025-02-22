package eu.faircode.xlua.x.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import  eu.faircode.xlua.R;

import eu.faircode.xlua.databinding.SettingsExFragmentBinding;
import eu.faircode.xlua.databinding.SettingsExGroupBinding;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.PrefManager;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.activities.SettingsExActivity;
import eu.faircode.xlua.x.ui.adapters.settings.OptimizedSettingGroupAdapter;
import eu.faircode.xlua.x.ui.core.RecyclerDynamicSizeAdjuster;
import eu.faircode.xlua.x.ui.core.CoreUiColors;
import eu.faircode.xlua.x.ui.core.CoreUiLog;
import eu.faircode.xlua.x.ui.core.DataEventKind;
import eu.faircode.xlua.x.ui.core.interfaces.IListChange;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.fragment.ListFragment;
import eu.faircode.xlua.x.ui.core.util.ListFragmentUtils;
import eu.faircode.xlua.x.ui.core.view_registry.SettingSharedRegistry;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.dialogs.ConfigCreateDialog;
import eu.faircode.xlua.x.ui.dialogs.ConfigDialog;
import eu.faircode.xlua.x.ui.dialogs.ConfirmDialog;
import eu.faircode.xlua.x.ui.dialogs.LogDialog;
import eu.faircode.xlua.x.ui.dialogs.OptionsListDialog;
import eu.faircode.xlua.x.ui.dialogs.ProfileDialog;
import eu.faircode.xlua.x.ui.dialogs.SettingsProgressDialog;
import eu.faircode.xlua.x.ui.models.SettingsExGroupViewModel;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.ClearAppDataCommand;
import eu.faircode.xlua.x.xlua.commands.call.ForceStopAppCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetSettingsExCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsGroup;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.test.EventTrigger;
import eu.faircode.xlua.x.xlua.settings.test.SharedSpace;
import eu.faircode.xlua.x.xlua.settings.test.SharedViewControl;
import eu.faircode.xlua.x.xlua.settings.test.interfaces.IUIViewControl;

/**
 * Ensure system takes but some data ?
 * So if Element is expandable, is expanded but user refresh it wont over write flag for refresh but update things like value etc...
 *
 *
 * ToDo:
 *          Parent Control Applies the UnChecked to already Randomized Session in its Control
 *              Then Randomizes checked controls
 *
 * ToDo: I want to look into using more of "this" for "setUserContext" on the "viewModel", this class will be the handler for that Context Data
 *          Also I want to POSSIBLY add Pager for Three Main Views ?
 *          We Can do a linking List Control Interface passing, pass to parent the Search Control if Fragment or Activity
 *          Option to bind it to parents ?
 *
 * ToDo:    [1] Clean up Base to helper static classes ? or builder ?
 *          [2] Do Function override for "binding::inflate"
 *          [3] Have some flag system either to always ensure Context, and or in the "onCreate" "onCreateView" "onViewCreated" to always ensure User Context
 */
public class SettingExFragment
                extends
        ListFragment<SettingsGroup, SettingsExFragmentBinding, SettingsExGroupBinding>
        implements
        IListChange<SettingPacket>,
        IUIViewControl,
        CompoundButton.OnCheckedChangeListener {

    private static final String TAG = LibUtil.generateTag(SettingExFragment.class);
    public static SettingExFragment newInstance(UserClientAppContext context) { return ListFragmentUtils.newInstance(SettingExFragment.class, context); }

    private SharedViewControl sharedViewControl;
    private boolean isViewOpen = true;
    private final SettingSharedRegistry sharedRegistry = new SettingSharedRegistry();

    @Override
    public SharedViewControl getSharedViewControl() {
        if(this.sharedViewControl == null) this.sharedViewControl = new SharedViewControl().setTag(SettingsExActivity.SHARED_TAG).setFragment(this);
        return sharedViewControl;
    }

    @Override
    public void setSharedViewControl(SharedViewControl sharedViewControl) { this.sharedViewControl = sharedViewControl; }


    @Override
    public SharedRegistry getSharedRegistry() { return sharedRegistry; }

    @Override
    public Fragment getAsFragment() {
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.createViewModel(SettingsExGroupViewModel.class, true);
        super.onCreate(savedInstanceState);

        try {
            sharedRegistry.refreshAssignments(requireContext(), getUserContext());
            sharedRegistry.bindToUserContext(getUserContext());
            PrefManager prefManager = sharedRegistry.ensurePrefsOpen(getContext(), PrefManager.SETTINGS_NAMESPACE);
            if(prefManager != null) {
                List<String> checked = prefManager.getStringList(
                        PrefManager.nameForChecked(true),
                        ListUtil.emptyList(),
                        false);

                try {
                    String pkgName = getAppPackageName();
                    if(pkgName != null && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(pkgName))
                        ListUtil.addAllIfValid(checked,
                                prefManager.getStringList(PrefManager.nameForChecked(false, pkgName),
                                        ListUtil.emptyList(),
                                        false));    //Error is jere "addAllIfValid"
                }catch (Exception e) {
                    Log.e(TAG, Str.fm("Error onCreate, error pushing Final Checked! Error=" + e));
                }

                if(ListUtil.isValid(checked))
                    for(String setting : checked)
                        sharedRegistry.setChecked(SharedRegistry.STATE_TAG_SETTINGS, RandomizerSessionContext.sharedSettingName(setting), true);
            }
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Error onCreate, Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e)));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.ensureHasUserContext();
        super.setAdapter(new OptimizedSettingGroupAdapter(requireContext(), null, this, getUserContext().bindShared(sharedRegistry)));

        super.initApplicationView(
                binding.ivAppIslandAppIcon,
                binding.tvAppIslandAppName,
                binding.tvAppIslandPackageName,
                binding.tvAppIslandPackageUid);

        super.addViewsToEventController(
                binding.ivAppIslandExpander,
                binding.cvAppIsland,

                binding.tvAppIslandPackageName,
                binding.tvAppIslandPackageUid,
                binding.tvAppIslandAppName,
                binding.ivAppIslandAppIcon,
                binding.ivAppIslandExpander,

                binding.btAppIslandToLogsDialog,
                binding.btAppIslandProfileDialog,
                binding.btAppIslandForceStop,
                binding.btAppIslandSettingsResetAll,
                binding.btAppIslandClearData,
                binding.btAppIslandSaveChecked,
                binding.btAppIslandConfigDialog,
                binding.btAppIslandCreateConfigDialog);

        CoreUiUtils.setViewStates(
                !super.isGlobal(),
                //binding.cbAppIslandUseDefaultSettings,
                binding.btAppIslandForceStop,
                binding.btAppIslandClearData);

        super.initFloatingActions(
                binding.flSettingsButtonOne,
                binding.flSettingsButtonTwo,
                binding.flSettingsButtonThree,
                binding.flSettingsButtonFour,
                binding.flSettingsButtonFive,
                binding.flSettingsButtonSix);

        super.setProgressBar(binding.pbSettings);
        super.initSwipeRefreshLayout(binding.swipeRefreshSettings, CoreUiColors.getSwipeRefreshColor(requireContext()));
        super.initRecyclerView(binding.rvSettings);

        RecyclerDynamicSizeAdjuster.create().startTopViewAdjuster(binding.cvAppIsland, binding.rvSettings, binding.swipeRefreshSettings);
        updateExpanded();

        initCheckboxes();

        super.startObserver();
        super.wire();
    }

    @Override
    public void wire() {
        super.wire();
        binding.cbForceStop.setOnCheckedChangeListener(this);
        binding.cbUseDefaultValues.setOnCheckedChangeListener(this);
        binding.btAppIslandProfileDialog.setOnClickListener(this);
    }

    public void initCheckboxes() {
        UserClientAppContext ctx = getUserContext();
        if(ctx != null) {
            binding.cbForceStop.setEnabled(!ctx.isGlobal());
            if(!ctx.isGlobal()) {
                binding.cbForceStop.setChecked(ctx.kill);
                sharedRegistry.setChecked(SharedRegistry.STATE_TAG_KILL, getUserContext().appPackageName, ctx.kill);
            }
        }
    }

    void updateExpanded() {
        isViewOpen = !isViewOpen;
        CoreUiUtils.setViewsVisibility(
                binding.ivAppIslandExpander,
                isViewOpen,

                binding.btAppIslandToLogsDialog,
                binding.btAppIslandProfileDialog,
                binding.btAppIslandForceStop,
                binding.buttonGridLayout,

                binding.cbForceStop,
                binding.cbUseDefaultValues,
                //binding.scrollViewGrid,
                //binding.cbAppIslandUseDefaultSettings,
                binding.btAppIslandConfigDialog,
                binding.btAppIslandSettingsResetAll,
                binding.btAppIslandSaveChecked,
                binding.btAppIslandClearData,
                binding.btAppIslandCreateConfigDialog);
    }

    @Override
    protected void dataEvent(DataEventKind kind, List<SettingsGroup> settingsGroups) {
        if(kind == DataEventKind.ON_RESUME) Log.d(TAG, "onResume=Size=" + ListUtil.size(settingsGroups));
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        int id = compoundButton.getId();
        switch (id) {
            case R.id.cbForceStop:
                sharedRegistry.setChecked(SharedRegistry.STATE_TAG_KILL, getUserContext().appPackageName, checked);
                break;
            case R.id.cbUseDefaultValues:
                //
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = CoreUiLog.getViewIdOnClick(v, TAG);
        final Context context = v.getContext();

        switch (id) {
            case R.id.tvAppIslandPackageName:
            case R.id.tvAppIslandPackageUid:
            case R.id.tvAppIslandAppName:
            case R.id.ivAppIslandAppIcon:
            case R.id.ivAppIslandExpander:
                updateExpanded();
                break;
            case R.id.flSettingsButtonOne:
                recyclerViewWrapper.getFloatingActionButtonContext().invokeFloatingActions();
                break;
            case R.id.flSettingsButtonSix:
                Map<String, IRandomizer> randomizers = RandomizersCache.getCopy();
                for(SettingHolder holder : SettingFragmentUtils.filterChecked(SettingFragmentUtils.getSettings(getLiveData()), sharedRegistry)) {
                    if(randomizers.containsKey(holder.getName())) {
                        String rnd = "%random%";
                        holder.setNewValue(rnd);
                        holder.ensureUiUpdated(rnd);
                        holder.setNameLabelColor(context);
                        holder.notifyUpdate(sharedRegistry.notifier);
                    }
                }
                break;
            case R.id.flSettingsButtonFive:
                List<SettingHolder> holders = SettingFragmentUtils.filterChecked(SettingFragmentUtils.getSettings(getLiveData()), sharedRegistry);
                ConfirmDialog.create()
                        .setContext(context)
                        .setMessage(Str.combine(getString(R.string.msg_confirm_delete_settings), String.valueOf(holders.size())))
                        .setDelay(0) // 5 second delay before OK is enabled
                        .setImage(R.drawable.ic_warining_one) // Optional warning icon
                        .onConfirm(() -> {
                            for(SettingHolder holder : holders) {
                                if(A_CODE.isSuccessful(PutSettingExCommand.call(context, holder, getUserContext(), getUserContext().isKill(), true))) {
                                    holder.setValue(null, true);
                                    holder.ensureUiUpdated(Str.EMPTY);
                                    holder.setNameLabelColor(context);
                                    holder.notifyUpdate(sharedRegistry.notifier);
                                }
                            }
                        })
                        .show(getFragmentMan(), getString(R.string.title_confirm));

                break;
            case R.id.flSettingsButtonFour:
                new SettingsProgressDialog()
                        .setNotifier(sharedRegistry.notifier)
                        .setData(SettingFragmentUtils.getSettingPackets(
                                getLiveData(),
                                getSharedRegistry(),
                                getUserContext(),
                                ActionFlag.PUSH), v.getContext())
                        .show(getFragmentMan(), getString(R.string.title_deploy_settings));
                break;
            case R.id.flSettingsButtonTwo:
                RandomizerSessionContext ctx = new RandomizerSessionContext()
                        .randomize(
                                this,
                                null,
                                context,
                                sharedRegistry);

                Snackbar.make(v, Str.combineEx(getString(R.string.msg_succeeded_count), " =", String.valueOf(ctx.getRandomizedCount())), Snackbar.LENGTH_LONG)
                        .show();
                break;
            case R.id.btAppIslandProfileDialog:
                ProfileDialog.create()
                        .setApp(context, getUserContext())
                        .setSettings(context, SettingFragmentUtils.filterChecked(SettingFragmentUtils.getSettings(getLiveData()), sharedRegistry))
                        .initKnownProfiles(context)
                        .show(getFragmentMan(),  getString(R.string.title_profile_manager));
                break;
            case R.id.btAppIslandToLogsDialog:
                LogDialog.create()
                        .setShowInstalled(false)
                        .setApp(getUserContext())
                        .refresh(context)
                        .show(getFragmentMan(), getString(R.string.title_logs));
                break;
            case R.id.btAppIslandForceStop:
                ConfirmDialog.create()
                        .setContext(context)
                        .setMessage(getString(R.string.msg_confirm_force_stop))
                        .setDelay(0) // 5 second delay before OK is enabled
                        .setImage(R.drawable.ic_serv_cold) // Optional warning icon
                        .onConfirm(() -> handleCodeToSnack(ForceStopAppCommand.stop(context, getAppUid(), getAppPackageName()), getString(R.string.result_prefix_force_stop), v))
                        .show(getFragmentMan(), getString(R.string.title_confirm));
                break;
            case R.id.btAppIslandClearData:
                ConfirmDialog.create()
                        .setContext(context)
                        .setMessage(getString(R.string.msg_confirm_clear_data))
                        .setDelay(0) // 5 second delay before OK is enabled
                        .setImage(R.drawable.ic_warining_one) // Optional warning icon
                        .onConfirm(() -> handleCodeToSnack(ClearAppDataCommand.clear(context, getAppPackageName()), getString(R.string.result_prefix_cleared_app_data), v))
                        .show(getFragmentMan(), getString(R.string.title_confirm));
                break;
            case R.id.btAppIslandSaveChecked:
                final List<String> checked = SettingFragmentUtils.settingsToNameList(SettingFragmentUtils.filterChecked(SettingFragmentUtils.getSettings(getLiveData()), sharedRegistry));
                final List<String> options = Arrays.asList(getString(R.string.title_global), getString(R.string.title_app));
                OptionsListDialog.create()
                        .setTitle(getString(R.string.title_save_options))
                        .setMessage(Str.combineEx(getString(R.string.msg_confirm_save_options_config), Str.WHITE_SPACE, checked.isEmpty() ? getString(R.string.option_delete) : String.valueOf(checked.size())))
                        .setIcon(R.drawable.ic_checklist_38)
                        .setOptions(options)
                        .setDefaultCheck(options.get(1))
                        .setAllowMultiple(false)
                        .onConfirm((c, d) -> {
                            if(!c.isEmpty())
                                sharedRegistry
                                        .ensurePrefsOpen(context, PrefManager.SETTINGS_NAMESPACE)
                                        .putStringList(PrefManager.nameForChecked(options.get(0).equalsIgnoreCase(c.get(0)), getAppPackageName()), checked);

                            Snackbar.make(v, c.isEmpty() ? getString(R.string.msg_error_bad_options) : getString(R.string.msg_shared_prefs_saved), Snackbar.LENGTH_LONG).show();
                        }).show(getFragmentMan(), getString(R.string.title_save_options));
                break;
            case R.id.btAppIslandConfigDialog:
                ConfigDialog.create()
                        .setOnChange(this)
                        .setApp(getAppUid(), getAppPackageName(), getUserContext().isKill())
                        .setConfigs(context)
                        .setRootView(v)
                        .show(getFragmentMan(), getString(R.string.title_config_manager));
                        //We need to some how force full update the main fragment
                break;
            case R.id.btAppIslandCreateConfigDialog:
                ConfigCreateDialog.create()
                        .setApp(getAppUid(), getAppPackageName())
                        .setConfigs(context, false)
                        .setSettings(SettingFragmentUtils.filterCheckedAsPackets(SettingFragmentUtils.getSettings(getLiveData()), sharedRegistry))
                        .setHookIds(SettingFragmentUtils.getHookIds(context, getAppUid(), getAppPackageName(), getLiveData(), sharedRegistry))
                        .show(getFragmentMan(), getString(R.string.title_config_modify));
                break;
            case R.id.btAppIslandSettingsResetAll:
                ConfirmDialog.create()
                        .setContext(context)
                        .setMessage(getString(R.string.msg_confirm_delete_all_settings))
                        .setDelay(3) // 5 second delay before OK is enabled
                        .setImage(R.drawable.ic_warining_one) // Optional warning icon
                        .onConfirm(() -> {
                            List<SettingPacket> changed = new ArrayList<>();
                            int uid = getAppUid();
                            String pkg = getAppPackageName();
                            for(SettingPacket packet : GetSettingsExCommand.get(context, true, uid, pkg, GetSettingsExCommand.FLAG_ONE)) {
                                if(!GetSettingExCommand.SETTING_SELECTED_CONFIG.equalsIgnoreCase(packet.name) && packet.value != null) {
                                    packet.value = null;
                                    packet.setUserIdentity(UserIdentity.fromUid(uid, pkg));
                                    packet.setActionPacket(ActionPacket.create(ActionFlag.DELETE, false));
                                    if(A_CODE.isSuccessful(PutSettingExCommand.call(context, packet))) {
                                        packet.value = null;
                                        changed.add(packet);
                                    }
                                }
                            }

                            onItemsChange(changed);
                        })
                        .show(getFragmentMan(), getString(R.string.title_confirm));


                break;
        }
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onLongClick(View v) {
        int id = CoreUiLog.getViewIdOnLongClick(v, TAG);
        switch (id) {
            case R.id.btAppIslandProfileDialog:
                Snackbar.make(v, getString(R.string.msg_hint_create_profile), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btAppIslandToLogsDialog:
                Snackbar.make(v, getString(R.string.msg_hint_logs), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btAppIslandClearData:
                Snackbar.make(v, getString(R.string.msg_hint_clear_app), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btAppIslandForceStop:
                Snackbar.make(v, getString(R.string.msg_hint_force_stop), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btAppIslandSettingsResetAll:
                Snackbar.make(v, getString(R.string.msg_hint_reset_settings), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btAppIslandCreateConfigDialog:
                Snackbar.make(v, getString(R.string.msg_hint_create_configs), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btAppIslandConfigDialog:
                Snackbar.make(v, getString(R.string.msg_hint_manage_configs), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btAppIslandSaveChecked:
                Snackbar.make(v, getString(R.string.msg_hint_save_checked), Snackbar.LENGTH_LONG).show();
                break;
        }

        return false;
    }

    @Override
    public void onItemsChange(List<SettingPacket> items) {
        Log.i(TAG, "On Item Change! Item Count=" + ListUtil.size(items));
        if(!ListUtil.isValid(items))
            return;

        try {
            List<SettingHolder> cachedSettings = SettingFragmentUtils.getSettings(getLiveData());
            if(!ListUtil.isValid(cachedSettings))
                return;

            Log.i(TAG, Str.fm("On Items Change! Cached Size=[%s] Updated Size=[%s]", ListUtil.size(cachedSettings), ListUtil.size(items)));
            WeakHashMap<String, SettingHolder> holders = new WeakHashMap<>();
            for(SettingHolder holder : cachedSettings)
                holders.put(holder.getName(), holder);

            //ToDo: Check for added / removed ?
            for(SettingPacket packet : items) {
                SettingHolder holder = holders.get(packet.name);
                if(holder != null) {
                    String value = packet.value;
                    holder.setValue(value, true);
                    holder.ensureUiUpdated(value == null ? Str.EMPTY : value);
                    holder.setNameLabelColor(getContext());
                    holder.notifyUpdate(sharedRegistry.notifier);
                }
            }
        }catch (Exception e) {
            Log.e(TAG, Str.fm("On Event Item Change Count [%s] Failed, Error=%s", ListUtil.size(items), e));
        }
    }

    public void handleCodeToSnack(A_CODE code, String extraIfSucceeded, View v) {
        Snackbar.make(v,
                A_CODE.isSuccessful(code) ?
                        Str.combine(getString(R.string.msg_task_finished_command), TextUtils.isEmpty(extraIfSucceeded) ? "" :  " >> " + extraIfSucceeded) :
                        Str.combine(getString(R.string.msg_task_failure), " >> " + code.name(), false) , Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onEvent(EventTrigger event) {
        IUIViewControl.super.onEvent(event);
        //We can handle refresh events
        //Or adding shit etc
    }

    @Override
    public void onView() {
        IUIViewControl.super.onView();
    }

    @Override
    public void onClean() {
        IUIViewControl.super.onClean();
    }
}
