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

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.ui.adapters.LogAdapter;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignedHooksExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignmentsCommand;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.log.LogPacket;

public class LogDialog extends AppCompatDialogFragment {
    private static final String TAG = LibUtil.generateTag(LogDialog.class);

    public static LogDialog create() { return new LogDialog(); }

    //public static LogDialog create(Context context) { return new LogDialog(context); }

    private Context context;
    private RecyclerView rvLogs;
    private EditText etSearchLogs;
    private CheckBox cbNewToOld;
    private Spinner spLogType;
    private LogAdapter logAdapter;
    private final List<LogPacket> logs = new ArrayList<>();

    //public LogDialog(Context context) { this.context = context; }
    //public LogDialog(Context context, List<LogPacket> logs) {
    //    this.context = context;
    //    this.logs = logs;
    //}

    private int uid;
    private String packageName;

    public LogDialog setApp(int uid, String packageName) {
        this.uid = uid;
        this.packageName = packageName;
        return this;
    }

    public LogDialog refresh(Context context) {
        List<AssignmentPacket> assignments = GetAssignmentsCommand.get(context, true, uid, packageName, 0);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Got Assignments=" + ListUtil.size(assignments));

        this.logs.clear();
        if(ListUtil.isValid(assignments)) {
            for(AssignmentPacket packet : assignments) {
                Log.e(TAG, "HookId=" + packet.getHookId());
                LogPacket log = new LogPacket();
                log.category = packet.getHookId();
                log.message = Str.isEmpty(packet.oldValue) && Str.isEmpty(packet.newValue) ? "Installed" :
                        Str.combineEx("New:\n", packet.newValue, "\n\nOld:\n", packet.oldValue);
                log.type = 0;
                log.time = packet.used;
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Log Hook Id=" + packet.hook + " Data=" + Str.toStringOrNull(packet));

                this.logs.add(log);
            }
        }

        return this;
    }

    /*public LogDialog test() {
        int ran = RandomGenerator.nextInt(3, 50);

        List<LogPacket> packets = new ArrayList<>();

        for(int i = 0; i < ran; i++) {
            LogPacket t = new LogPacket();
            t.category = RandomGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(5, 13)) + ".apk";
            t.message = RandomGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(23, 67));
            t.type = RandomGenerator.nextInt(13, 24);
            t.time = System.currentTimeMillis();
            packets.add(t);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Generated {" + packets.size() + "} Logs...");

        this.logs = packets;

        return this;
    }*/

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.log_dialog, null);

        initializeUI(view);
        setupRecyclerView();
        setupSearchFunctionality();

        builder.setView(view)
                .setTitle("App Logs")
                .setNegativeButton("Close", (dialog, which) -> dismiss());

        return builder.create();
    }

    private void initializeUI(View view) {
        etSearchLogs = view.findViewById(R.id.etSearchLogs);
        cbNewToOld = view.findViewById(R.id.cbNewToOld);
        spLogType = view.findViewById(R.id.spLogType);
        rvLogs = view.findViewById(R.id.rvLogs);

        // Example for spinner setup
        spLogType.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, new String[]{"Error", "Usage", "Deploy"}));

        // Setup checkbox listener for sorting
        cbNewToOld.setOnCheckedChangeListener((buttonView, isChecked) -> sortLogsByDate(isChecked));
    }

    private void setupRecyclerView() {
        rvLogs.setLayoutManager(new LinearLayoutManager(context));
        logAdapter = new LogAdapter(context, logs);
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
        List<LogPacket> filteredLogs = new ArrayList<>();
        for (LogPacket log : logs) {
            if (log.message.toLowerCase().contains(query.toLowerCase()) ||
                    log.category.toLowerCase().contains(query.toLowerCase())) {
                filteredLogs.add(log);
            }
        }
        logAdapter = new LogAdapter(context, filteredLogs);
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
