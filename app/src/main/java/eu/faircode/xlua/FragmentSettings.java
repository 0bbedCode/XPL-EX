package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.configs.MockConfig;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.api.xmock.XMockQuery;
import eu.faircode.xlua.api.xmock.call.KillAppCommand;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.ui.ConfigQue;
import eu.faircode.xlua.ui.SettingsQue;
import eu.faircode.xlua.ui.dialogs.ClearAppDataDialog;
import eu.faircode.xlua.ui.dialogs.RenameDialogEx;
import eu.faircode.xlua.ui.dialogs.SettingAddDialogEx;
import eu.faircode.xlua.ui.dialogs.SettingsResetDialog;
import eu.faircode.xlua.ui.interfaces.IConfigUpdate;
import eu.faircode.xlua.ui.interfaces.ILoader;
import eu.faircode.xlua.ui.ViewFloatingAction;
import eu.faircode.xlua.ui.interfaces.ISettingUpdateEx;
import eu.faircode.xlua.ui.interfaces.ISettingsReset;
import eu.faircode.xlua.ui.transactions.ConfigTransactionResult;
import eu.faircode.xlua.ui.transactions.SettingTransactionResult;
import eu.faircode.xlua.utilities.CollectionUtil;
import eu.faircode.xlua.utilities.PrefUtil;
import eu.faircode.xlua.utilities.SettingUtil;
import eu.faircode.xlua.utilities.UiUtil;
import eu.faircode.xlua.utilities.ViewUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;

public class FragmentSettings
        extends
        ViewFloatingAction
        implements
        View.OnClickListener,
        View.OnLongClickListener,
        CompoundButton.OnCheckedChangeListener,
        ILoader,
        ISettingUpdateEx,
        ISettingsReset,
        IConfigUpdate {

    private static final String TAG = "XLua.FragmentSettings";

    private AdapterSetting rvAdapter;

    private ImageView ivExpander;
    private CardView cvAppView;
    private CheckBox cbUseDefault;

    private Button btProperties, btConfigs, btKill, btResetAll, btClearData, btSaveChecked, btSettingsToConfig;
    private boolean isViewOpen = true;
    private int lastHeight = 0;

    private View main;
    private static final String USE_DEFAULT = "useDefault";
    private static final String LAST_CHECKED = "lastChecked";
    private SettingsQue que;
    private ConfigQue configQue;

    private List<String> lastChecked = null;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        main = inflater.inflate(R.layout.settingrecyclerview, container, false);
        this.TAG_ViewFloatingAction = "XLua.FragmentSettings";
        this.application = AppGeneric.from(getArguments(), getContext());
        ivExpander = main.findViewById(R.id.ivExpanderSettingsApp);
        cvAppView = main.findViewById(R.id.cvAppInfoSettings);

        que = new SettingsQue(this.application);
        configQue = new ConfigQue(this.application);
        lastChecked = Str.splitToList(PrefUtil.getString(getContext(), LAST_CHECKED));

        btProperties = main.findViewById(R.id.btSettingsToProperties);
        btConfigs = main.findViewById(R.id.btSettingsToConfigs);
        btKill = main.findViewById(R.id.btSettingsKillApp);
        btResetAll = main.findViewById(R.id.btSettingsResetAll);
        btClearData = main.findViewById(R.id.btSettingsClearData);
        btSaveChecked = main.findViewById(R.id.btSettingsSaveChecked);
        btSettingsToConfig = main.findViewById(R.id.btSettingsExportToConfig);

        cbUseDefault = main.findViewById(R.id.cbUseDefaultSettings);
        if(this.application.isGlobal()) {
            cbUseDefault.setEnabled(false);
            btKill.setEnabled(false);
            btClearData.setEnabled(false);
        }
        else
            cbUseDefault.setChecked(XLuaCall.getSettingBoolean(getContext(), application.getUid(), application.getPackageName(), USE_DEFAULT));

        super.initActions();
        super.bindTextViewsToAppId(main, R.id.ivSettingsAppIcon, R.id.tvSettingsPackageName, R.id.tvSettingsPackageFull, R.id.tvSettingsPackageUid);
        super.setFloatingActionBars(this, this, main,  R.id.flSettingsButtonOne, R.id.flSettingsButtonTwo, R.id.flSettingsButtonThree, R.id.flSettingsButtonFour, R.id.flSettingsButtonFive);

        //init Refresh
        progressBar = main.findViewById(R.id.pbSettings);
        int colorAccent = XUtil.resolveColor(requireContext(), R.attr.colorAccent);
        swipeRefresh = main.findViewById(R.id.swipeRefreshSettings);
        swipeRefresh.setColorSchemeColors(colorAccent, colorAccent, colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { loadData(); }
        });

        //init RecyclerView
        super.initRecyclerView(main, R.id.rvSettings, true);
        rvList.setVisibility(View.VISIBLE);
        rvList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean onRequestChildFocus(@NonNull RecyclerView parent, @NonNull RecyclerView.State state, @NonNull View child, View focused) { return true; }
        };

        llm.setAutoMeasureEnabled(true);
        rvList.setLayoutManager(llm);
        rvAdapter = new AdapterSetting(this, que);
        rvList.setAdapter(rvAdapter);
        rvList.addItemDecoration(SettingUtil.createSettingsDivider(getContext()));

        //Ensure Padding Between Top behind app card view and First Element
        cvAppView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = cvAppView.getHeight();
                if(height != lastHeight) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) cvAppView.getLayoutParams();
                    int totalHeight = height + layoutParams.topMargin + layoutParams.bottomMargin + 15;
                    rvList.setPadding(0, totalHeight, 0, 0);
                    int lastHeightCopy = lastHeight;
                    lastHeight = height;
                    UiUtil.setSwipeRefreshLayoutEndOffset(getContext(), swipeRefresh, totalHeight);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) rvList.getLayoutManager();
                    assert layoutManager != null;
                    int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisiblePosition == 0) {
                        if(height > lastHeightCopy)
                            rvList.scrollBy(0, -totalHeight);
                    }
                }
            }
        });

        updateExpanded();
        wire();
        loadData();
        return main;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onLongClick(View v) {
        int code = v.getId();
        if(DebugUtil.isDebug())
            Log.d(TAG, "onLongClick=" + code);
        switch (code) {
            case R.id.flSettingsButtonTwo:
                Snackbar.make(v, R.string.menu_settings_randomize_hint, Snackbar.LENGTH_SHORT).show();
                break;
            case R.id.flSettingsButtonThree:
                Snackbar.make(v, R.string.menu_settings_add_hint, Snackbar.LENGTH_SHORT).show();
                break;
            case R.id.flSettingsButtonFour:
                Snackbar.make(v, R.string.menu_settings_save_hint, Snackbar.LENGTH_SHORT).show();
                break;
            case R.id.flSettingsButtonFive:
                Snackbar.make(v, R.string.menu_settings_delete_hint, Snackbar.LENGTH_SHORT).show();
                break;
            case R.id.flSettingsButtonOne:
                Snackbar.make(v, R.string.menu_settings_hint, Snackbar.LENGTH_SHORT).show();
                break;
            case R.id.cbUseDefaultSettings:
                Toast.makeText(getContext(), R.string.menu_settings_use_default_hint, Toast.LENGTH_LONG).show();
                break;
            case R.id.btSettingsResetAll:
                Snackbar.make(main, getString(R.string.menu_settings_reset_hint), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btSettingsClearData:
                Snackbar.make(main, getString(R.string.button_settings_reset_data_hint), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btSettingsSaveChecked:
                Snackbar.make(main, getString(R.string.button_settings_save_checked_hint), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btSettingsExportToConfig:
                Snackbar.make(main, getString(R.string.button_settings_export_to_config_hint), Snackbar.LENGTH_LONG).show();
                break;

        }

        return true;
    }

    @SuppressLint("NonConstantResourceId") @Override
    public void onClick(View v) {
        int id = v.getId();
        if(DebugUtil.isDebug())
            Log.d(TAG, "onClick id=" + id);
        switch (id) {
            case R.id.btSettingsKillApp:
                final XResult res = KillAppCommand.invokeEx(v.getContext(), application.getPackageName(), application.getUid());
                Snackbar.make(v, res.getResultMessage(), Snackbar.LENGTH_SHORT).show();
                break;
            case R.id.ivExpanderSettingsApp:
                updateExpanded();
                break;
            case R.id.btSettingsToProperties:
                Intent propsIntent = new Intent(v.getContext(), ActivityProperties.class);
                propsIntent.putExtra("packageName", application.getPackageName());
                v.getContext().startActivity(propsIntent);
                break;
            case R.id.btSettingsToConfigs:
                Intent configIntent = new Intent(v.getContext(), ActivityConfig.class);
                configIntent.putExtra("packageName", application.getPackageName());
                v.getContext().startActivity(configIntent);
                break;
            case R.id.flSettingsButtonOne:
                invokeFloatingActions();
                break;
            case R.id.flSettingsButtonTwo:
                rvAdapter.randomizeAll(v.getContext());
                break;
            case R.id.flSettingsButtonThree:
                new SettingAddDialogEx()
                        .setCallback(this)
                        .setQue(que)
                        .show(Objects.requireNonNull(getFragmentManager()), getString(R.string.title_add_dialog_setting_builder));
                break;
            case R.id.flSettingsButtonFour:
                rvAdapter.saveAll(v.getContext());
                break;
            case R.id.flSettingsButtonFive:
                rvAdapter.deleteSelected(v.getContext());
                break;
            case R.id.btSettingsResetAll:
                new SettingsResetDialog()
                        .setCallback(this)
                        .setApplication(application)
                        .show(Objects.requireNonNull(getFragmentManager()), getString(R.string.title_settings_reset));
                break;
            case R.id.btSettingsClearData:
                new ClearAppDataDialog()
                        .setApplication(application)
                        .show(getManager(), getString(R.string.title_delete_appdata));
                break;
            case R.id.btSettingsSaveChecked:
                List<String> selected = new ArrayList<>();
                for(LuaSettingExtended s : rvAdapter.getSettings())
                    if(s.isEnabled())
                        selected.add(s.getName());

                if(!selected.isEmpty()) PrefUtil.setString(getContext(), LAST_CHECKED, Str.joinList(selected));
                else PrefUtil.setString(getContext(), LAST_CHECKED, "");
                break;
            case R.id.btSettingsExportToConfig:
                List<LuaSettingExtended> settings = rvAdapter.getSettingsEnable();
                if(!settings.isEmpty()) {
                    MockConfig config = new MockConfig();
                    config.setName("test");
                    config.setSettings(settings);
                    new RenameDialogEx()
                            .setConfig(config)
                            .setCallback(this)
                            .show(Objects.requireNonNull(getFragmentManager()), getString(R.string.title_config_rename_config));
                }
                break;
        }

    }

    @Override
    public FragmentManager getManager() {  return getFragmentManager(); }

    @Override
    public Fragment getFragment() { return this; }

    @Override
    public AppGeneric getApplication() { return this.application; }

    @Override
    public void filter(String query) { if (rvAdapter != null) rvAdapter.getFilter().filter(query); }

    private void wire() {
        ivExpander.setOnClickListener(this);
        btProperties.setOnClickListener(this);
        btKill.setOnClickListener(this);
        btConfigs.setOnClickListener(this);
        cvAppView.setOnClickListener(this);

        cbUseDefault.setOnCheckedChangeListener(this);
        cbUseDefault.setOnLongClickListener(this);

        btResetAll.setOnClickListener(this);
        btResetAll.setOnLongClickListener(this);

        btSaveChecked.setOnClickListener(this);
        btSaveChecked.setOnLongClickListener(this);

        btClearData.setOnClickListener(this);
        btClearData.setOnLongClickListener(this);

        btSettingsToConfig.setOnClickListener(this);
        btSettingsToConfig.setOnLongClickListener(this);
    }

    void updateExpanded() {
        isViewOpen = !isViewOpen;
        ViewUtil.setViewsVisibility(ivExpander, isViewOpen, btProperties, btConfigs, btKill, cbUseDefault, btResetAll, btSaveChecked, btClearData, btSettingsToConfig);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if(DebugUtil.isDebug())
            Log.d(TAG, "onCheckedChanged id=" + id);
        try {
            if(id == R.id.cbUseDefaultSettings) {
                XLuaCall.putSettingBoolean(
                        cbUseDefault.getContext(),
                        application.getUid(),
                        application.getPackageName(),
                        USE_DEFAULT,
                        isChecked,
                        application.getForceStop());
            }
        }catch (Exception e) {
            Log.e(TAG, "onCheckedChanged Failed, id=" + id + " Stack=" + Log.getStackTraceString(e));
        }
    }

    @Override
    public void onResume() { super.onResume(); }

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public void loadData() {
        XLog.i("Starting data loader");
        LoaderManager manager = Objects.requireNonNull(getActivity()).getSupportLoaderManager();
        manager.restartLoader(ActivityMain.LOADER_DATA, new Bundle(), dataLoaderCallbacks).forceLoad();
    }

    LoaderManager.LoaderCallbacks<SettingsDataHolder> dataLoaderCallbacks = new LoaderManager.LoaderCallbacks<SettingsDataHolder>() {
        @NonNull @Override
        public Loader<SettingsDataHolder> onCreateLoader(int id, Bundle args) { return new SettingsDataLoader(getContext()).setApp(application); }

        @Override
        public void onLoadFinished(@NonNull Loader<SettingsDataHolder> loader, SettingsDataHolder data) {
            XLog.i("onLoadFinished Data Loader Finished");
            if(data.exception == null) {
                UiUtil.initTheme(getActivity(), data.theme);
                if(CollectionUtil.isValid(data.settings)) {
                    SettingUtil.sortSettings(data.settings);
                    if(!lastChecked.isEmpty()) {
                        for(LuaSettingExtended s : data.settings)
                            if(lastChecked.contains(s.getName()))
                                s.setIsEnabled(true);
                    } rvAdapter.set(data.settings, application);
                } setRefreshState(false);
            }else Snackbar.make(Objects.requireNonNull(getView()), data.exception.toString(), Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void onLoaderReset(@NonNull Loader<SettingsDataHolder> loader) { }
    };

    @Override
    public void onSettingUpdate(SettingTransactionResult result) {
        if(result.hasAnySucceeded())
            loadData();
    }

    @Override
    public void onFinish(XResult result) {
        loadData();
    }

    @Override
    public void onConfigUpdate(ConfigTransactionResult result) {
        if(result.hasAnySucceeded()) {
            MockConfig config = result.getConfig();
            Snackbar.make(main, getString(R.string.msg_settings_to_config_exporting) + " " + config.getName(), Snackbar.LENGTH_LONG).show();
            configQue.sendConfig(
                    getContext(),
                    -1,
                    config,
                    false,
                    false,
                    null);
        }
    }

    private static class SettingsDataLoader extends AsyncTaskLoader<SettingsDataHolder> {
        private AppGeneric application;
        public SettingsDataLoader setApp(AppGeneric application) { this.application = application; return this; }

        SettingsDataLoader(Context context) { super(context); setUpdateThrottle(1000); }

        @Nullable
        @Override
        public SettingsDataHolder loadInBackground() {
            if(DebugUtil.isDebug()) Log.d(TAG, "Data loader started");
            SettingsDataHolder data = new SettingsDataHolder();
            try {
                data.theme = XLuaCall.getTheme(getContext());
                data.settings = new ArrayList<>(XMockQuery.getAllSettings(getContext(), application));
                for(LuaSettingExtended s : data.settings)
                    if(s.getName().contains(".parent."))
                        s.settings = data.settings; //Make a copy of the list ? or?

                if(DebugUtil.isDebug()) Log.d(TAG, "Settings from Data Loader=" + data.settings.size());
            }catch (Throwable ex) {
                data.settings.clear();
                data.exception = ex;
                Log.e(TAG, "Data Loader Exception");
            }

            if(DebugUtil.isDebug()) Log.e(TAG, "Data Loader Settings Finished=" + data.settings.size());
            return data;
        }
    }

    private static class SettingsDataHolder {
        String theme;
        List<LuaSettingExtended> settings = new ArrayList<>();
        Throwable exception = null;
    }
}
