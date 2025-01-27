package eu.faircode.xlua;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.api.xlua.XLuaCall;

public class FragmentDatabase extends Fragment {
    private static final String TAG = "XLua.FragmentDatabase";

    private ProgressBar pbDatabases;
    private SwipeRefreshLayout swipeRefresh;
    private AdapterDatabase rvDatabaseAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(TAG, "FragmentDatabase.onCreateView Enter");
        final View main = inflater.inflate(R.layout.dbrecyclerview, container, false);

        pbDatabases =  main.findViewById(R.id.pbDBs);

        int colorAccent = XUtil.resolveColor(getContext(), R.attr.colorAccent);
        swipeRefresh = main.findViewById(R.id.swipeRefreshDatabase);
        swipeRefresh.setColorSchemeColors(colorAccent, colorAccent, colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        RecyclerView rvProps = main.findViewById(R.id.rvDatabases);
        rvProps.setVisibility(View.VISIBLE);
        rvProps.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean onRequestChildFocus(RecyclerView parent, RecyclerView.State state, View child, View focused) {
                return true;
            }
        };

        llm.setAutoMeasureEnabled(true);
        rvProps.setLayoutManager(llm);
        rvDatabaseAdapter = new AdapterDatabase();
        rvProps.setAdapter(rvDatabaseAdapter);

        Log.i(TAG, "FragmentDatabase.onCreateView Leave");
        return main;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void loadData() {
        Log.i(TAG, "Starting data loader");
        LoaderManager manager = getActivity().getSupportLoaderManager();
        manager.restartLoader(ActivityMain.LOADER_DATA, new Bundle(), dataLoaderCallbacks).forceLoad();
    }

    LoaderManager.LoaderCallbacks dataLoaderCallbacks = new LoaderManager.LoaderCallbacks<DBsDataHolder>() {
        @Override
        public Loader<DBsDataHolder> onCreateLoader(int id, Bundle args) {
            return new DBsDataLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<DBsDataHolder> loader, DBsDataHolder data) {
            Log.i(TAG, "onLoadFinished");
            if(data.exception == null) {
                ActivityBase activity = (ActivityBase) getActivity();
                if (!data.theme.equals(activity.getThemeName()))
                    activity.recreate();

                rvDatabaseAdapter.set(data.dbs);
                swipeRefresh.setRefreshing(false);
                pbDatabases.setVisibility(View.GONE);
                Log.i(TAG, "onLoadFinished Exit");
            }else {
                Log.e(TAG, Log.getStackTraceString(data.exception));
                Snackbar.make(getView(), data.exception.toString(), Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(Loader<DBsDataHolder> loader) {
            // Do nothing
        }
    };

    private static class DBsDataLoader extends AsyncTaskLoader<DBsDataHolder> {
        DBsDataLoader(Context context) {
            super(context);
            setUpdateThrottle(1000);
        }

        @Nullable
        @Override
        public DBsDataHolder loadInBackground() {
            Log.i(TAG, "Data loader started");
            DBsDataHolder data = new DBsDataHolder();
            try {
                data.theme = XLuaCall.getSettingValue(getContext(), "theme");
                //data.theme = XProvider.getSetting(getContext(), "global", "theme");
                if (data.theme == null)
                    data.theme = "light";

                Log.i(TAG, "Getting Props...");
                //List<XDataBase> props = XMockProxyApi.getDatabaseFiles(getContext());
                //Log.i(TAG, "Props=" + props.size());
                //data.dbs.addAll(props);
                //make sure it syncs with cache if needed
            }catch (Throwable ex) {
                data.dbs.clear();
                data.exception = ex;
                Log.e(TAG, ex.getMessage());
            }

            Log.i(TAG, "DataLoader Props Finished=" + data.dbs.size());
            return data;
        }
    }

    private static class DBsDataHolder {
        String theme;
        List<XDatabaseOld> dbs = new ArrayList<>();
        Throwable exception = null;
    }
}
