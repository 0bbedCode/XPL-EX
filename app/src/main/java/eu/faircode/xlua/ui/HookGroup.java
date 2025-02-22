package eu.faircode.xlua.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.faircode.xlua.AppGeneric;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.app.XLuaApp;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.hook.assignment.LuaAssignment;
import eu.faircode.xlua.api.hook.assignment.LuaAssignmentPacket;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.api.xlua.XLuaQuery;
import eu.faircode.xlua.api.xmock.XMockQuery;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.ui.interfaces.IHookTransaction;
import eu.faircode.xlua.ui.interfaces.IHookTransactionEx;
import eu.faircode.xlua.ui.transactions.HookTransactionResult;
import eu.faircode.xlua.x.Str;

public class HookGroup {
    public int id;
    public String name;
    public String title;

    private static final String TAG = "XLua.HookGroup";

    public HookGroup() { }
    public HookGroup(Context context, String groupName) {
        Resources resources = context.getResources();
        this.id = resources.getIdentifier("group_" +
                groupName.toLowerCase().replaceAll("[^a-z]", "_"), "string", context.getPackageName());
        this.name = groupName;
        this.title = (this.id > 0 ? resources.getString(this.id) : name);
    }

    private boolean exception = false;
    private int installed = 0;
    private int optional = 0;
    private long used = -1;

    private AppGeneric application;
    private Map<String, XLuaHook> hooks = new HashMap<>();
    private Map<String, LuaAssignment> assignments = new HashMap<>();

    private List<String> collection = new ArrayList<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(50);
    private final Object lock = new Object();

    public boolean hasException() { return this.exception; }
    public int getInstalled() { return this.installed; }
    public int getOptional() { return this.optional; }
    public long getUsed() { return this.used; }

    public int getAssigned() { return this.assignments != null ? this.assignments.size() : 0; }
    public boolean hasAssigned() { return getAssigned() > 0; }
    public boolean allAssigned() { return getAssigned() == hooks.size(); }
    public boolean noneAssigned() { return getAssigned() <= 0; }

    public boolean containsAssignedHook(String hookId) { return hasAssigned() && assignments.containsKey(hookId); }

    public void removeAssignment(LuaAssignment assignment) { removeAssignment(assignment.getHook().getObjectId()); }
    public void removeAssignment(String hookId) {
        synchronized (lock) {
            assignments.remove(hookId);
            exception = false;
            installed = 0;
            optional = 0;
            used = -1;
            for(LuaAssignment assignment : assignments.values()) {
                if (assignment.getException() != null) exception = true;
                if (assignment.getInstalled() >= 0) installed++;
                if (assignment.getHook().isOptional()) optional++;
                if (assignment.getRestricted()) used = Math.max(used, assignment.getUsed());
            }
        }
    }

    public void putAssignment(LuaAssignment assignment) {
        synchronized (lock) {
            String hId = assignment.getHook().getObjectId();
            if(!assignments.containsKey(hId)) {
                if (assignment.getException() != null) exception = true;
                if (assignment.getInstalled() >= 0) installed++;
                if (assignment.getHook().isOptional()) optional++;
                if (assignment.getRestricted()) used = Math.max(used, assignment.getUsed());
            }

            assignments.put(assignment.getHook().getObjectId(), assignment);
        }
    }

    public void send(
            final Context context,
            final XLuaHook hook,
            final int adapterPosition,
            final boolean assign,
            final IHookTransactionEx iCallback) {

        final List<String> hookIds = new ArrayList<>();
        hookIds.add(hook.getObjectId());
        final LuaAssignmentPacket packet = LuaAssignmentPacket.create(
                application.getUid(),
                application.getPackageName(),
                hookIds,
                !assign,
                !application.isGlobal() && application.getForceStop());

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.ensureNoDoubleNewLines("Packet=" + packet + " assign=" + assign + " pos=" + adapterPosition + " hook id=" + hook.getObjectId()));

        //XLog.i("Packet=" + packet + " assign=" + assign + " pos=" + adapterPosition + " hood id=" + hook.getId());

        final HookTransactionResult result = new HookTransactionResult();
        result.context = context;
        result.id = hook.getObjectId().hashCode();
        result.hooks.add(hook);
        result.adapterPosition = adapterPosition;
        result.code = packet.getCode();
        result.packets.add(packet);
        result.group = this;

        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Assignment Packet created: Property packet=" + packet);

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    final XResult ret = XLuaCall.assignHooks(context, packet);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            result.result = ret;
                            if(ret.succeeded()) result.succeeded.add(hook);
                            else if(ret.failed()) result.failed.add(hook);
                            if(iCallback != null) iCallback.onHookUpdate(result);
                        }
                    });
                }
            });
        }catch (Exception e) {
            XLog.e("Failed to Add Assignment: hook=" + hook.getObjectId(), e, true);
            result.result = XResult.create().setFailed("Failed to send assignments error!");
            result.failed.add(hook);
            try { if(iCallback != null) iCallback.onHookUpdate(result);
            }catch (Exception ex) {  XLog.e("Failed to execute Callback! ", e, true); }
        }
    }

    public void sendAll(final Context context, final int position, final boolean assign, final IHookTransaction iCallback) {
        final ArrayList<String> hookIds = new ArrayList<>();
        final ArrayList<LuaAssignment> assignments = new ArrayList<>();
        for (XLuaHook hook : hooks.values())
            if (hook.isAvailable(application.isGlobal() ? null : application.getPackageName(), collection) &&
                    (name == null || name.equalsIgnoreCase(hook.getGroup()))) {
                hookIds.add(hook.getObjectId());
                assignments.add(new LuaAssignment(hook));
            }


        if(DebugUtil.isDebug())
            Log.d(TAG, "Sending Assignments, App=" + application + " Hooks Size=" + hookIds.size());

        executor.submit(new Runnable() {
            @Override
            public void run() {
                final XResult res = XLuaCall.assignHooks(
                        context,
                        application.getUid(),
                        application.getPackageName(),
                        hookIds,
                        !assign,
                        application.getForceStop());

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(res.succeeded()) {
                            for(LuaAssignment assignment : assignments) {
                                if (assign) putAssignment(assignment);
                                else removeAssignment(assignment);
                            }
                        }
                        if(iCallback != null)
                            iCallback.onGroupFinished(assignments, position, assign, res);
                    }
                });
            }
        });
    }

    public HookGroup setCollection(List<String> collection) { this.collection = collection; return this; }
    public List<String> getCollection() { return this.collection; }

    public HookGroup setApplication(AppGeneric application) { if(this.application == null) this.application = application; return this; }
    public AppGeneric getApplication() { return this.application; }

    public List<XLuaHook> getHooks() { return new ArrayList<>(this.hooks.values()); }
    public Map<String, XLuaHook> getHooksMap() { return this.hooks; }
    public boolean hasHooks() { return this.hooks != null && !this.hooks.isEmpty(); }
    public int hooksSize() { return hasHooks() ? this.hooks.size() : 0; }

    public HookGroup putHook(XLuaHook hook) {
        if(hook.getGroup().equalsIgnoreCase(this.name))
            hooks.put(hook.getObjectId(), hook);

        return this;
    }

    public HookGroup bindAssignmentsFromApp(XLuaApp app) {
        synchronized (lock) {
            assignments.clear();
            for (LuaAssignment assignment : app.getAssignments())
                if (assignment.getHook().getGroup().equalsIgnoreCase(name)) {
                    if (assignment.getException() != null) exception = true;
                    if (assignment.getInstalled() >= 0) installed++;
                    if (assignment.getHook().isOptional()) optional++;
                    if (assignment.getRestricted()) used = Math.max(used, assignment.getUsed());
                    assignments.put(assignment.getHook().getObjectId(), assignment);
                }

            return this;
        }
    }

    public static List<HookGroup> getGroups(Context context, AppGeneric application) {
        Map<String, HookGroup> groups = new HashMap<>();
        try {
            Collection<XLuaHook> hooks = XLuaQuery.getHooks(context, true);

            //XLuaApp app = XLuaCall.getApp(context, application, true, true);
            XLuaApp app = null;
            for (XLuaApp a : XLuaQuery.getApps(context, true)) {
                if(a.getPackageName().equalsIgnoreCase(application.getPackageName())) {
                    app = a;
                    break;
                }
            }

            if(app == null) {
                XLog.e("App Object is NULL from the search...", new Throwable(), true);
                return new ArrayList<>();
            }

            List<String> collection = XLuaCall.getCollections(context);
            Map<String, LuaSettingExtended> settings = LuaSettingExtended.toMap(XMockQuery.getAllSettings(context, application));
            XLog.i("Hooks Size=" + hooks.size() + " Collection Size=" + collection.size() + " Settings Size=" + settings.size());
            application.setForceStop(app.getForceStop());
            for (XLuaHook hook : hooks) {
                if(hook.getManagedSettings().isEmpty() && hook.getSettings() != null) hook.initSettings(settings);
                HookGroup group;
                if (groups.containsKey(hook.getGroup())) group = groups.get(hook.getGroup());
                else {
                    group = new HookGroup(context, hook.getGroup()).setApplication(application).setCollection(collection);
                    groups.put(hook.getGroup(), group);
                }
                if(group == null) continue;
                group.putHook(hook);
            }

            for(HookGroup group : groups.values()) group.bindAssignmentsFromApp(app);
        }catch (Exception e) { XLog.e("Failed to create Groups: app=" + application, e, true); }
        XLog.i("Created Groups=" + groups.size());
        List<HookGroup> groupsList = new ArrayList<>(groups.values());
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc
        Collections.sort(groupsList, new Comparator<HookGroup>() {
            @Override
            public int compare(HookGroup group1, HookGroup group2) { return collator.compare(group1.title, group2.title); }
        });
        return groupsList;
    }
}
