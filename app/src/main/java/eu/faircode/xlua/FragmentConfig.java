package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.faircode.xlua.api.configs.MockConfig;
import eu.faircode.xlua.api.configs.MockConfigPacket;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.api.xmock.XMockQuery;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.ui.ConfigQue;
import eu.faircode.xlua.ui.ViewFloatingAction;
import eu.faircode.xlua.ui.dialogs.ConfigDeleteDialog;
import eu.faircode.xlua.ui.dialogs.RenameDialogEx;
import eu.faircode.xlua.ui.interfaces.IConfigUpdate;
import eu.faircode.xlua.ui.interfaces.ILoader;
import eu.faircode.xlua.ui.transactions.ConfigTransactionResult;
import eu.faircode.xlua.utilities.FileDialogUtil;
import eu.faircode.xlua.utilities.UiUtil;
import eu.faircode.xlua.x.data.JsonFileUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class  FragmentConfig extends
        ViewFloatingAction implements
        View.OnClickListener,
        View.OnLongClickListener,
        AdapterView.OnItemSelectedListener,
        IConfigUpdate,
        ILoader {
    private static final int PICK_FILE_REQUEST_CODE = 1; // This is a request code you define to identify your request
    private static final int PICK_FOLDER_RESULT_CODE = 2;

    private AdapterConfig rvConfigAdapter;
    private Spinner spConfigSelection;
    private ArrayAdapter<MockConfig> spConfigs;

    private final List<MockConfig> unSaved = new ArrayList<>();

    private View view;
    private ConfigQue configsQue;

    public View onCreateView(final @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.configeditor, container, false);
        this.application = AppGeneric.from(getArguments(), getContext());
        this.TAG_ViewFloatingAction = "XLua.FragmentConfig";
        super.initActions();
        super.bindTextViewsToAppId(view, R.id.ivConfigsAppIcon, R.id.tvConfigsPackageName, R.id.tvConfigsPackageFull, R.id.tvConfigsPackageUid);
        super.setFloatingActionBars(this, this, view, R.id.flActionConfigOptions, R.id.flActionConfigSave, R.id.flActionConfigDelete, R.id.flActionConfigImport, R.id.flActionConfigApply, R.id.flActionConfigExport);

        progressBar = view.findViewById(R.id.pbConfigs);
        swipeRefresh = view.findViewById(R.id.swipeRefreshConfigs);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { loadData(); }
        });

        ImageView ivBtDeleteConfig = view.findViewById(R.id.ivDeleteConfig);
        ivBtDeleteConfig.setOnClickListener(this);
        ivBtDeleteConfig.setOnLongClickListener(this);

        super.initRecyclerView(view, R.id.rvConfigSettings, true);
        rvList.setVisibility(View.VISIBLE);
        rvList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean onRequestChildFocus(@NonNull RecyclerView parent, @NonNull RecyclerView.State state, @NonNull View child, View focused) {
                return true;
            }
        };

        llm.setAutoMeasureEnabled(true);
        rvList.setLayoutManager(llm);
        rvConfigAdapter = new AdapterConfig(this, this);
        rvList.setAdapter(rvConfigAdapter);
        configsQue = new ConfigQue(application);
        initDropDown(view);
        loadData();
        return view;
    }

    public void pushConfig(MockConfig config) {
        if(config != null) {
            XLog.i("Push Config=" + config);
            unSaved.add(config);
            spConfigs.add(config);
            spConfigs.notifyDataSetChanged();
        }
    }

    public void pushConfigs(List<MockConfig> configs) {
        if(configs != null) {
            XLog.i("Push Configs Size=" + configs.size());
            spConfigs.clear();
            for(MockConfig c : configs) {
                for (LuaSettingExtended s : c.getSettings())
                    s.setIsEnabled(true);
            }

            spConfigs.addAll(configs);
            spConfigs.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() { super.onResume(); }

    @Override
    public void onPause() { super.onPause(); }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        XLog.i("onClick id=" + id);
        if(!isOpen() && !isRecyclerScrollable())
            invokeFloatingActions();

        try {
            MockConfig config = rvConfigAdapter.getConfig();
            switch (id) {
                case R.id.ivDeleteConfig:
                    if(!unSaved.contains(config))
                        new ConfigDeleteDialog()
                                .setAdapterPosition(-1)
                                .setConfig(config)
                                .setQue(configsQue)
                                .setCallback(this)
                                .show(Objects.requireNonNull(getFragmentManager()), getResources().getString(R.string.title_delete_config));
                    else {
                        unSaved.remove(config);
                        loadData();
                    }
                    break;
                case R.id.flActionConfigApply:
                    setRefreshState(true);
                    rvConfigAdapter.applyConfig(getContext());
                    setRefreshState(false);
                    Snackbar.make(view, R.string.result_config_finish_applying, Snackbar.LENGTH_LONG).show();
                    break;
                case R.id.flActionConfigDelete:
                    if(!unSaved.contains(config))
                        configsQue.sendConfig(
                                getContext(),
                                -1,
                                config,
                                false,
                                true,
                                this);
                    else Snackbar.make(v, R.string.result_config_failed_delete_settings, Snackbar.LENGTH_LONG).show();
                    break;
                case R.id.flActionConfigSave:
                    configsQue.sendConfig(
                            getContext(),
                            -1,
                            rvConfigAdapter.getConfig(),
                            false,
                            false,
                            this);
                    break;
                case R.id.flActionConfigExport:
                    try { startActivityForResult(UiUtil.createSaveFileIntent(), PICK_FOLDER_RESULT_CODE);
                    } catch (Exception e) {
                        XLog.e("Open Directory Error", e, true);
                        Snackbar.make(v, R.string.result_open_directory_failed, Snackbar.LENGTH_LONG).show();
                    }
                    break;
                case R.id.flActionConfigImport:
                    try {
                        startActivityForResult(
                            Intent.createChooser(UiUtil.createOpenFileIntent(), getResources().getString(R.string.title_select_file)),
                                PICK_FILE_REQUEST_CODE);
                    } catch (Exception e) {
                        XLog.e("Open File Error", e, true);
                        Snackbar.make(v, R.string.result_open_file_failed, Snackbar.LENGTH_LONG).show();
                    }
                    break;
                case R.id.flActionConfigOptions:
                    invokeFloatingActions();
                    break;
            }
        }catch (Exception e) { XLog.e("onClick Failed! id=" + id, e, true); }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onLongClick(View v) {
        int code = v.getId();
        XLog.i("onLongClick id=" + code);
        switch (code) {
            case R.id.ivDeleteConfig:
                Snackbar.make(view, R.string.menu_config_delete_hint, Snackbar.LENGTH_LONG).show();
                break;
            case R.id.flActionConfigApply:
                Snackbar.make(view, R.string.menu_config_apply_hint, Snackbar.LENGTH_LONG).show();
                break;
            case R.id.flActionConfigSave:
                Snackbar.make(view, R.string.menu_config_save_hint, Snackbar.LENGTH_LONG).show();
                break;
            case R.id.flActionConfigExport:
                Snackbar.make(view, R.string.menu_config_export_hint, Snackbar.LENGTH_LONG).show();
                break;
            case R.id.flActionConfigImport:
                Snackbar.make(view, R.string.menu_config_import_hint, Snackbar.LENGTH_LONG).show();
                break;
            case R.id.flActionConfigOptions:
                Snackbar.make(view, R.string.menu_config_hint, Snackbar.LENGTH_LONG).show();
                break;
            case R.id.flActionConfigDelete:
                Snackbar.make(view, R.string.menu_config_delete_settings_hint, Snackbar.LENGTH_LONG).show();
                break;
        }

        return true;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null) {
            Snackbar.make(view, getResources().getString(R.string.result_config_read_failed), Snackbar.LENGTH_LONG).show();
            return;
        }

        Uri selectedFileUri = data.getData();
        if(selectedFileUri == null || resultCode != Activity.RESULT_OK) {
            Snackbar.make(view, getResources().getString(R.string.result_config_read_failed), Snackbar.LENGTH_LONG).show();
            return;
        }

        XLog.i("onActivityResult code=" + requestCode);
        switch (requestCode) {
            case PICK_FILE_REQUEST_CODE:
                if(!JsonFileUtil.isJsonFile(getContext(), selectedFileUri)) {
                    Snackbar.make(view, R.string.result_config_parse_failed, Snackbar.LENGTH_LONG).show();
                    return;
                }

                final MockConfig config = FileDialogUtil.readConfig(getContext(), selectedFileUri);
                if(config == null)
                    Snackbar.make(
                            view,
                            getResources().getString(R.string.result_config_read_failed) + " => " + selectedFileUri.getPath(),
                            Snackbar.LENGTH_LONG).show();
                else {
                    String configName = config.getName();
                    for(int i = 0; i < spConfigs.getCount(); i++) {
                        MockConfig conf = spConfigs.getItem(i);
                        assert conf != null;
                        if(configName.equals(conf.getName())) {
                            new RenameDialogEx()
                                    .setConfig(config)
                                    .setCallback(this)
                                    .show(Objects.requireNonNull(getFragmentManager()), getString(R.string.title_config_rename_config));

                            configName += "-" + RandomGenerator.nextInt(10000,999999999);
                            config.setName(configName);
                            break;
                        }
                    }

                    for(LuaSettingExtended setting : config.getSettings()) setting.setIsEnabled(true);
                    pushConfig(config);
                    Snackbar.make(view, getResources().getString(R.string.result_config_read_success) + " " + configName, Snackbar.LENGTH_LONG).show();
                }

                break;
            case PICK_FOLDER_RESULT_CODE:
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Objects.requireNonNull(getContext()).getContentResolver().takePersistableUriPermission(selectedFileUri, takeFlags);

                if(!FileDialogUtil.saveConfigSettings(getContext(), selectedFileUri, rvConfigAdapter))
                    Snackbar.make(view, getResources().getString(R.string.result_file_save_failed) + " " + rvConfigAdapter.getConfigName(), Snackbar.LENGTH_LONG).show();
                else Snackbar.make(view, getResources().getString(R.string.result_file_save_success) + " " + rvConfigAdapter.getConfigName(), Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    public void initDropDown(View view) {
        XLog.i("Creating Drop Down for Configs");
        spConfigs = new ArrayAdapter<>(Objects.requireNonNull(getContext()), android.R.layout.simple_spinner_item);
        spConfigs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spConfigSelection = view.findViewById(R.id.spConfigEdit);
        spConfigSelection.setTag(null);
        spConfigSelection.setAdapter(spConfigs);
        spConfigSelection.setOnItemSelectedListener(this);
    }

    @Override
    public void loadData() {
        XLog.i("Starting Data Loader");
        LoaderManager manager = Objects.requireNonNull(getActivity()).getSupportLoaderManager();
        manager.restartLoader(ActivityMain.LOADER_DATA, new Bundle(), dataLoaderCallbacks).forceLoad();
    }

    @Override
    public void filter(String query) { }

    @Override
    public FragmentManager getManager() { return getFragmentManager(); }

    @Override
    public Fragment getFragment() { return this; }

    @Override
    public AppGeneric getApplication() { return application; }

    LoaderManager.LoaderCallbacks<PropsDataHolder> dataLoaderCallbacks = new LoaderManager.LoaderCallbacks<PropsDataHolder>() {
        @NonNull
        @Override
        public Loader<PropsDataHolder> onCreateLoader(int id, Bundle args) { return new ConfigsDataLoader(getContext()); }

        @Override
        public void onLoadFinished(@NonNull Loader<PropsDataHolder> loader, PropsDataHolder data) {
            XLog.i("Data Loader has Finished!");
            if(data.exception == null) {
                UiUtil.initTheme(getActivity(), data.theme);
                data.configs.addAll(unSaved);
                pushConfigs(data.configs);
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
            }else {
                XLog.e("Data Loader Exception", data.exception, true);
                Snackbar.make(Objects.requireNonNull(getView()), data.exception.toString(), Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<PropsDataHolder> loader) { }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updateSelection(); }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { updateSelection(); }

    private void updateSelection() {
        MockConfig selected = (MockConfig) spConfigSelection.getSelectedItem();
        String configName = (selected == null ? null : selected.getName());
        if(DebugUtil.isDebug()) XLog.i("Config Selected=" + configName);
        if (configName == null ? spConfigSelection.getTag() != null : !configName.equals(spConfigSelection.getTag())) spConfigSelection.setTag(configName);
        if(selected != null) rvConfigAdapter.set(selected);
    }

    @Override
    public void onConfigUpdate(ConfigTransactionResult result) {
        Snackbar.make(view, result.result.getResultMessage(), Snackbar.LENGTH_LONG).show();
        if(result.hasAnySucceeded()) {
            if(result.code != -1) {
                MockConfig config = result.getConfig();
                MockConfigPacket packet = result.getPacket();
                unSaved.remove(config);
                if(packet.getSettings().size() != config.getSettings().size()) {
                    config.setSettings(packet.getSettings());
                    rvConfigAdapter.set(config);
                }
            }
            loadData();
        }
    }

    private static class ConfigsDataLoader extends AsyncTaskLoader<PropsDataHolder> {
        ConfigsDataLoader(Context context) { super(context); setUpdateThrottle(1000); }

        @Nullable
        @Override
        public PropsDataHolder loadInBackground() {
            XLog.i("Data Loader Started");
            PropsDataHolder data = new PropsDataHolder();
            try {
                data.theme = XLuaCall.getTheme(getContext());
                data.configs = new ArrayList<>(XMockQuery.getConfigsEx(getContext()));
                XLog.i("Configs Count=" + data.configs.size());
            }catch (Throwable ex) {
                data.configs.clear();
                data.exception = ex;
                XLog.e("Data Loader Background Exception", ex, true);
            }

            XLog.i("Data Loader Returning. Configs Count=" + data.configs.size());
            return data;
        }
    }

    private static class PropsDataHolder {
        String theme;
        List<MockConfig> configs = new ArrayList<>();
        Throwable exception = null;
    }
}
