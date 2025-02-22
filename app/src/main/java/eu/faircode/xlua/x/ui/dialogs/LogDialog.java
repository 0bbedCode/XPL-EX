package eu.faircode.xlua.x.ui.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.ui.adapters.LogAdapter;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignedHooksExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignmentsCommand;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.log.LogPacket;

public class LogDialog extends AppCompatDialogFragment {
    private static final String TAG = LibUtil.generateTag(LogDialog.class);

    public static LogDialog create() { return new LogDialog(); }

    //public static LogDialog create(Context context) { return new LogDialog(context); }

    private String currentSearchQuery = ""; // To maintain search state


    public static final String SPECIAL_PREFIX  = "Setting:";
    private Context context;
    private RecyclerView rvLogs;
    private EditText etSearchLogs;
    private CheckBox cbNewToOld, cbShowInstalled;
    private Spinner spLogType;
    private LogAdapter logAdapter;
    private final List<LogPacket> logs = new ArrayList<>();

    private UserClientAppContext app;
    public LogDialog setApp(UserClientAppContext app) {
        this.app = app;
        return this;
    }

    private boolean initialShowInstalled = true;

    public LogDialog setShowInstalled(boolean showInstalled) {
        this.initialShowInstalled = showInstalled;
        return this;
    }


    public LogDialog refresh(Context context) {
        List<AssignmentPacket> assignments = GetAssignmentsCommand.get(context, true, app.appUid, app.appPackageName, 3);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Got Assignments=" + ListUtil.size(assignments));

        this.logs.clear();
        if(ListUtil.isValid(assignments)) {
            for(AssignmentPacket packet : assignments) {
                if(!Str.isEmpty(packet.getHookId())) {
                    LogPacket log = new LogPacket();

                    log.category = packet.getHookId();
                    if(!Str.isEmpty(log.category))
                        if(log.category.startsWith(SPECIAL_PREFIX) && log.category.length() > SPECIAL_PREFIX.length())
                            log.category = log.category.substring(SPECIAL_PREFIX.length());

                    log.message = Str.isEmpty(packet.oldValue) && Str.isEmpty(packet.newValue) ?
                            "Installed" :
                            Str.combineEx("New:\n", packet.newValue, "\n\nOld:\n", packet.oldValue);
                    log.type = 0;
                    log.time = packet.used;
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Log Hook Id=" + packet.hook + " Data=" + Str.toStringOrNull(packet));

                    this.logs.add(log);
                }
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Total Logs in the Dialog=" + this.logs.size() + " From Command=" + ListUtil.size(assignments));

        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.log_dialog, null);

        initializeUI(view);
        setupRecyclerView();
        setupSearchFunctionality();

        // Apply initial filtering based on initialShowInstalled
        cbShowInstalled.setChecked(initialShowInstalled);
        filterLogs(Str.EMPTY);

        builder.setView(view)
                .setTitle(getString(R.string.title_logs))
                .setNegativeButton(getString(R.string.option_close), (dialog, which) -> dismiss());

        return builder.create();
    }

    private void initializeUI(View view) {
        etSearchLogs = view.findViewById(R.id.etSearchLogs);
        cbNewToOld = view.findViewById(R.id.cbNewToOld);
        cbShowInstalled = view.findViewById(R.id.cbShowInstalled); // Add this line
        spLogType = view.findViewById(R.id.spLogType);
        rvLogs = view.findViewById(R.id.rvLogs);


        // Set initial state and filter
        //cbShowInstalled.setChecked(initialShowInstalled);

        // Example for spinner setup
        spLogType.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, new String[]{"Error", "Usage", "Deploy"}));

        // Setup checkbox listener for sorting
        cbNewToOld.setOnCheckedChangeListener((buttonView, isChecked) -> sortLogsByDate(isChecked));
        cbShowInstalled.setOnCheckedChangeListener((buttonView, isChecked) -> filterLogs(currentSearchQuery));
        //filterLogs(Str.EMPTY);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        rvLogs.setLayoutManager(layoutManager);

        // Create adapter with filtered list based on initialShowInstalled
        List<LogPacket> filteredLogs = new ArrayList<>();
        for (LogPacket log : logs) {
            if (initialShowInstalled || !log.message.equalsIgnoreCase("installed")) {
                filteredLogs.add(log);
            }
        }

        logAdapter = new LogAdapter(context, filteredLogs, app);
        rvLogs.setAdapter(logAdapter);
    }

    private void setupSearchFunctionality() {
        etSearchLogs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterLogs(s.toString());
            }
        });
    }

    private void filterLogs(String query) {
        currentSearchQuery = query;
        List<LogPacket> filteredLogs = new ArrayList<>();
        boolean showInstalled = cbShowInstalled.isChecked();

        for (LogPacket log : logs) {
            boolean matchesSearch = query.isEmpty() ||
                    log.message.toLowerCase().contains(query.toLowerCase()) ||
                    log.category.toLowerCase().contains(query.toLowerCase());
            boolean matchesInstalledFilter = showInstalled ||
                    !log.message.equalsIgnoreCase("installed");

            if (matchesSearch && matchesInstalledFilter) {
                filteredLogs.add(log);
            }
        }

        logAdapter = new LogAdapter(context, filteredLogs, app);
        rvLogs.setAdapter(logAdapter);
    }

    private void sortLogsByDate(boolean newestToOldest) {
        if (newestToOldest) {
            Collections.sort(logs, new Comparator<LogPacket>() {
                @Override
                public int compare(LogPacket log1, LogPacket log2) {
                    return Long.compare(log2.time, log1.time);
                }
            });
        } else {
            Collections.sort(logs, new Comparator<LogPacket>() {
                @Override
                public int compare(LogPacket log1, LogPacket log2) {
                    return Long.compare(log1.time, log2.time);
                }
            });
        }
        logAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
