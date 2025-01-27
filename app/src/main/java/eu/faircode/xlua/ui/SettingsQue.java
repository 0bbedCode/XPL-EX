package eu.faircode.xlua.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.faircode.xlua.AppGeneric;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.settings.LuaSettingPacket;
import eu.faircode.xlua.api.xstandard.interfaces.ISettingUpdate;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.ui.interfaces.ISettingUpdateEx;
import eu.faircode.xlua.ui.transactions.SettingTransactionResult;

public class SettingsQue {
    private static final String TAG = "XLua.SettingsQue";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Object lock = new Object();
    private final AppGeneric application;

    public SettingsQue(AppGeneric application) { this.application = application; }

    public void batchUpdate(final Context context, final List<LuaSettingExtended> settings, final boolean delete, final ISettingUpdate onResult) {
        for(LuaSettingExtended sm : settings)
            sm.setIsBusy(true);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    final List<LuaSettingExtended> successful = new ArrayList<>();
                    final List<LuaSettingExtended> failed = new ArrayList<>();
                    for(final LuaSettingExtended s : settings) {
                        final LuaSettingPacket packet = createUpdatePacket(s, delete, false);
                        try {
                            final XResult ret = XLuaCall.sendSetting(context, packet);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if(ret.failed()) {
                                        failed.add(s);
                                        if(onResult != null) onResult.onSettingUpdateFailed(context, s, ret);
                                    }else {
                                        successful.add(s);
                                        if(delete) {
                                            s.setValueForce(null);
                                            s.resetModified(true);
                                        }
                                        if(onResult != null) onResult.onSettingUpdatedSuccessfully(context, s, ret);
                                    }
                                }
                            });
                        }catch (final Exception e) {
                            Log.e(TAG, "Failed to update setting setting=" + s + " e=" + e);
                            if(onResult != null) onResult.onSettingUpdateFailed(context, s, XResult.create().setFailed(e.getMessage()));
                            failed.add(s);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    failed.add(s);
                                    if(onResult != null) onResult.onSettingUpdateFailed(context, s, XResult.create().setFailed(e.getMessage()));
                                }
                            });
                        }
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(onResult != null) onResult.onBatchFinished(context, successful, failed);
                        }
                    });
                }
            }
        });
    }

    public void batchUpdate(final Context context, final List<LuaSettingExtended> settings, final boolean delete, final ISettingUpdateEx callback) {
        for(LuaSettingExtended sm : settings)
            sm.setIsBusy(true);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    //final List<LuaSettingExtended> successful = new ArrayList<>();
                    //final List<LuaSettingExtended> failed = new ArrayList<>();


                    final SettingTransactionResult result = new SettingTransactionResult();
                    result.context = context;
                    result.adapterPosition = -1;
                    //result.result = ret;
                    //result.settings.add(setting);
                    //result.id = setting.getName().hashCode();
                    //result.packets.add(packet);
                    //result.code = packet.getCode();
                    //if(ret.succeeded()) result.succeeded.add(setting);
                    //else if(ret.failed()) result.failed.add(setting);
                    //if(onCallback != null) onCallback.onSettingUpdate(result);


                    for(final LuaSettingExtended s : settings) {
                        result.settings.add(s);
                        /* CHECK_CHECK more for kill hard coding fix later */
                        final LuaSettingPacket packet = createUpdatePacket(s, delete, false);
                        result.packets.add(packet);
                        result.code = packet.getCode();
                        try {
                            final XResult ret = XLuaCall.sendSetting(context, packet);
                            result.results.add(ret);
                            if(ret.failed()) result.failed.add(s);
                            else result.succeeded.add(s);
                            /*new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if(ret.failed()) {
                                        failed.add(s);
                                        if(onResult != null) onResult.onSettingUpdateFailed(context, s, ret);
                                    }else {
                                        successful.add(s);
                                        if(delete) {
                                            s.setValueForce(null);
                                            s.resetModified(true);
                                        }
                                        if(onResult != null) onResult.onSettingUpdatedSuccessfully(context, s, ret);
                                    }
                                }
                            });*/
                        }catch (final Exception e) {
                            XLog.e("Failed to send setting Exception: s=" + s, e, true);
                            result.results.add(XResult.create().setFailed(e.getMessage()));
                            result.failed.add(s);
                            /*Log.e(TAG, "Failed to update setting setting=" + s + " e=" + e);
                            if(onResult != null) onResult.onSettingUpdateFailed(context, s, XResult.create().setFailed(e.getMessage()));
                            failed.add(s);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    failed.add(s);
                                    if(onResult != null) onResult.onSettingUpdateFailed(context, s, XResult.create().setFailed(e.getMessage()));
                                }
                            });*/
                        }
                    }
                    if(callback != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSettingUpdate(result);
                            }
                        });
                    }
                }
            }
        });
    }


    public void updateSetting(
            final Context context,
            final LuaSettingExtended setting,
            final int adapterPosition,
            final boolean insertSetting,
            final boolean insertDefaultMap,
            final ISettingUpdateEx onCallback) { sendSettingEx(context, setting, adapterPosition, insertSetting, insertDefaultMap, false, false, application.getForceStop(), onCallback); }

    public void updateSetting(
            final Context context,
            final LuaSettingExtended setting,
            final int adapterPosition,
            final boolean insertSetting,
            final boolean insertDefaultMap,
            final boolean forceKill,
            final ISettingUpdateEx onCallback) { sendSettingEx(context, setting, adapterPosition, insertSetting, insertDefaultMap, false, false, forceKill, onCallback); }

    public void deleteSetting(
            final Context context,
            final LuaSettingExtended setting,
            final int adapterPosition,
            final boolean deleteSetting,
            final boolean deleteDefaultMap,
            final boolean forceKill,
            final ISettingUpdateEx onCallback) { sendSettingEx(context, setting, adapterPosition, false, false, deleteSetting, deleteDefaultMap, forceKill, onCallback); }

    public void sendSettingEx(
            final Context context,
            final LuaSettingExtended setting,
            final int adapterPosition,
            final boolean insertSetting,
            final boolean insertDefaultMap,
            final boolean deleteSetting,
            final boolean deleteDefaultMap,
            final boolean forceKill,
            final ISettingUpdateEx onCallback) {

        /* CHECK_CHECK */

        if(setting.isBusy()) {
            XLog.e("Setting is Busy, wait til it is ready! setting=" + setting);
            return;
        }


        if(!deleteDefaultMap && !deleteSetting && !setting.isModified()) {
            XLog.e("Make changes to the setting before 'sending' setting=" + setting + " del de=" + deleteDefaultMap + " del=" + deleteSetting, new Throwable(), true);
            return;
        }

        try {
            setting.setIsBusy(true);
            executor.submit(new Runnable() {
                @Override
                public void run() { synchronized (lock) {
                    try {
                        final LuaSettingPacket packet = createPacket(setting, insertSetting, insertDefaultMap, deleteSetting, deleteDefaultMap, forceKill);
                        final XResult ret = XLuaCall.sendMockSetting(context, packet);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                SettingTransactionResult result = new SettingTransactionResult();
                                result.context = context;
                                result.adapterPosition = adapterPosition;
                                result.result = ret;
                                result.settings.add(setting);
                                result.id = setting.getName().hashCode();
                                result.packets.add(packet);
                                result.code = packet.getCode();
                                if(ret.succeeded()) result.succeeded.add(setting);
                                else if(ret.failed()) result.failed.add(setting);
                                if(onCallback != null) onCallback.onSettingUpdate(result);
                            }
                        });
                    }catch (Exception e) {
                        XLog.e("Failed to execute setting Transaction, setting=" + setting + " pos=" + adapterPosition, e, true);
                    }
                }
                }
            });
        }catch (Exception e) {
            XLog.e("Failed to wait for setting to apply, setting=" + setting + " pos=" + adapterPosition, e, true);
        }
    }

    /*public void sendSetting(
            final Context context,
            final LuaSettingExtended setting,
            final int adapterPosition,
            final boolean delete,
            final boolean forceKill,
            final ISettingUpdateEx onCallback) {
        if(setting.isBusy()) {
            XLog.e("Setting is Busy, wait til it is ready! setting=" + setting);
            return;
        }

        if(!delete && !setting.isModified()) {
            XLog.e("Make changes to the setting before 'sending' setting=" + setting);
            return;
        }

        try {
            executor.submit(new Runnable() {
                @Override
                public void run() { synchronized (lock) {
                    try {
                        final LuaSettingPacket packet = createUpdatePacket(setting, delete, forceKill);
                        final XResult ret = XLuaCall.sendSetting(context, packet);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                SettingTransactionResult result = new SettingTransactionResult();
                                result.context = context;
                                result.adapterPosition = adapterPosition;
                                result.result = ret;
                                result.settings.add(setting);
                                result.id = setting.getName().hashCode();
                                result.code = delete ? LuaSettingPacket.CODE_DELETE_SETTING : LuaSettingPacket.CODE_INSERT_UPDATE_SETTING;
                                if(ret.succeeded()) result.succeeded.add(setting);
                                else if(ret.failed()) result.failed.add(setting);
                                if(onCallback != null) onCallback.onSettingUpdate(result);
                            }
                        });
                    }catch (Exception e) {
                        XLog.e("Failed to execute setting Transaction, setting=" + setting + " pos=" + adapterPosition, e, true);
                    }
                }
                }
            });
        }catch (Exception e) {
            XLog.e("Failed to wait for setting to apply, setting=" + setting + " pos=" + adapterPosition, e, true);
        }
    }*/

    /*public void sendSetting(final Context context, final  LuaSettingExtended setting, final boolean deleteSetting, final boolean forceKill) { sendSetting(context, setting, deleteSetting, forceKill); }
    public void sendSetting(final Context context, final  LuaSettingExtended setting, final boolean deleteSetting, final boolean forceKill, final ISettingUpdate onResult) {
        if(!deleteSetting && !setting.isModified()) {
            Log.w(TAG, "Make changes to the setting before 'sending' setting=" + setting);
            return;
        }

        try {
            executor.submit(new Runnable() {
                @Override
                public void run() { synchronized (lock) {
                    final LuaSettingPacket packet = createUpdatePacket(setting, deleteSetting, forceKill);
                    final XResult ret = XLuaCall.sendSetting(context, packet);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(onResult != null) {
                                if(ret.succeeded()) onResult.onSettingUpdatedSuccessfully(context, setting, ret);
                                else onResult.onSettingUpdateFailed(context, setting, ret);
                            }
                        }
                    });
                }
                }
            });
        }catch (Exception e) {
            Log.e(TAG, "Failed to wait for Setting to apply , setting=" + setting);
        }
    }*/

    public LuaSettingPacket createPacket(
            LuaSettingExtended setting,
            boolean insertSetting,
            boolean insertDefaultMap,
            boolean deleteSetting,
            boolean deleteDefaultMap,
            boolean forceKill) {

        int code = (deleteSetting || deleteDefaultMap) ?
                LuaSettingPacket.getCodeForDeletion(deleteSetting, deleteDefaultMap) :
                LuaSettingPacket.getCodeForInsertOrUpdate(insertSetting, insertDefaultMap);
        LuaSettingPacket packet = setting.createPacket(code, forceKill)
                .copyIdentification(application);

        if(packet.isInsertOrUpdateSetting()) packet.setValueForce(setting.getModifiedValue());
        else if(packet.isDeleteSetting()) packet.setValueForce(null);
        return packet;
    }

    public LuaSettingPacket createUpdatePacket(LuaSettingExtended setting, boolean deleteSetting, boolean forceKill) {
        LuaSettingPacket packet =
                LuaSettingPacket.create(setting,
                                LuaSettingPacket.getCodeInsertOrDelete(deleteSetting),
                                forceKill)
                        .copyIdentification(application);

        if(!deleteSetting) packet.setValueForce(setting.getModifiedValue());
        else packet.setValueForce(null);
        return packet;
    }
}
