package eu.faircode.xlua.x.hook.interceptors.ipc.holders;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;

public class IntentQueryData {
    private static final String TAG = "XLua.IntentQueryData";

    public String authority;
    public String[] selectionArgs;
    public List<String> segments = new ArrayList<>();
    public List<String> allArgs = new ArrayList<>();
    public Cursor result;
    public Cursor newResult;

    public StringBuilder oldChanges = new StringBuilder();
    public StringBuilder newChanges = new StringBuilder();

    public boolean hasSelectionArgs() { return selectionArgs != null && selectionArgs.length > 0;  }
    public boolean hasAuthority() { return !TextUtils.isEmpty(authority); }
    public boolean hasSegments() { return !segments.isEmpty(); }
    public boolean hasResult() { return result != null; }
    public boolean hasArgs() { return !allArgs.isEmpty(); }

    public IntentQueryData(XParam param, boolean getResult) {
        Uri uri = param.tryGetArgument(0, null);
        if(uri != null) {
            if(getResult) this.result = param.tryGetResult(null);
            try {
                this.authority = uri.getAuthority();
                this.selectionArgs = param.extractSelectionArgs();
                List<String> seg = uri.getPathSegments();
                if(seg != null && !seg.isEmpty()) this.segments.addAll(seg);
                if(!this.segments.isEmpty()) this.allArgs.addAll(this.segments);
                if(this.selectionArgs != null && this.selectionArgs.length > 0) this.allArgs.addAll(Arrays.asList(this.selectionArgs));
            }catch (Exception e) {
                Log.e(TAG, "Error in Constructor<IntentQueryData>. Error:" + e);
            }
        }
    }

    public boolean intercept(XParam param) {
        try {
            if(!hasAuthority()) {
                Log.e(TAG, "Authority is Invalid or Missing from [query]");
                return false;
            }

            if(!hasArgs()) {
                Log.e(TAG, "Authority: " + authority + " Does not have any args at all...");
                return false;
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Filtering Authority=" + authority + " All Args=" + Str.joinList(allArgs));

            String authLow = authority.toLowerCase();
            String actualAuth = authLow;
            String settings = param.getSetting("query:" + authLow);
            if(settings == null) {
                //then get wild card
                settings = param.getSetting("query:*");
                actualAuth = "*";
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Failed to find Authority Resolver: " + authority + " Using Wild Card (*)");
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Filtering Authority=" + authLow + " Settings=" + settings + " Actual Auth=" + actualAuth);

            if(!TextUtils.isEmpty(settings)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Found Authority for Query=" + authority + " Settings=" + settings + " All Args=" + Str.joinList(allArgs));

                oldChanges.append(authority).append("\n");
                newChanges.append(authority).append("\n");

                String[] allSettings = settings.split("\\|");
                for(String setting : allSettings) {
                    for(String arg: allArgs) {
                        if(arg.equalsIgnoreCase(setting)) {
                            String settingName = param.getSetting("query:[" + actualAuth + "]:" + setting);
                            if(settingName == null) {
                                Log.e(TAG, "Authority: " + authority + " Is Missing Setting: " + setting);
                                continue;
                            }

                            String settingValue = param.getSetting(settingName);
                            if(settingValue == null) {
                                Log.e(TAG, "Authority: " + authority + " Is Missing Setting Value: " + setting);
                                continue;
                            }

                            if(DebugUtil.isDebug())
                                Log.d(TAG, "Authority [" + authority + "] Arg=" + arg + " Setting=" + setting + " Setting Name=" + settingName + " Setting Value=" + settingValue);

                            newResult = replaceValue(result, settingValue, arg);
                        }
                    }
                }

                if(newResult != null) {
                    //Update this using Str Builder
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Replaced Cursor for [query] => Old=" + oldChanges.toString() + ".\nNew=" + newChanges.toString());

                    param.setResult(newResult);
                    param.setOldResult(oldChanges.toString());
                    param.setNewResult(newChanges.toString());
                    param.setSettingResult(authority);
                    return true;
                }
            }
            return false;
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting Query: " + e);
            return false;
        }
    }

    public MatrixCursor replaceValue(Cursor c, String newValue, String... possibleKeyNames) {
        if (c == null || newValue == null) {
            throw new IllegalArgumentException("Cursor and newValue must not be null");
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Searching [" + authority + "] Query Result for Keys: " + Str.joinArray(possibleKeyNames) + "   New Value: " + newValue);

        MatrixCursor newCursor = new MatrixCursor(c.getColumnNames());
        int valIx = c.getColumnIndex("value");
        int keyIx = c.getColumnIndex("key");

        if (valIx == -1) {
            Log.w(TAG, "No 'value' column found in cursor");
            return new MatrixCursor(c.getColumnNames());
        }

        try {
            if (keyIx == -1) {
                // Single column cursor
                if (c.moveToFirst()) {
                    do {
                        MatrixCursor.RowBuilder rowBuilder = newCursor.newRow();
                        rowBuilder.add(newValue);
                        // Only replace the first value
                        newValue = c.getString(valIx);
                        oldChanges.append("[" + possibleKeyNames[0] + "]:[Not Sure(Single Column)]").append("\n");
                        newChanges.append("[" + possibleKeyNames[0] + "]:" + newValue).append("\n");
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Cursor Replaced Key: " + possibleKeyNames[0] + " New Value: " + newValue + " Authority: " + authority);

                    } while (c.moveToNext());
                }
            } else {
                // Key-value cursor
                if (c.moveToFirst()) {
                    do {
                        String key = c.getString(keyIx);
                        String value = c.getString(valIx);
                        MatrixCursor.RowBuilder rowBuilder = newCursor.newRow();
                        rowBuilder.add(key);

                        if (shouldReplace(key, possibleKeyNames)) {
                            rowBuilder.add(newValue);
                            if (DebugUtil.isDebug())
                                Log.d(TAG, "Cursor Replacing at Key: " + key + " Value: " + newValue + " Old Value:" + value);

                            oldChanges.append("[" + key + "]:" + value).append("\n");
                            newChanges.append("[" + key + "]:" + newValue).append("\n");
                        } else {
                            rowBuilder.add(value);
                        }
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to process cursor: " + e.getMessage(), e);
        } finally {
            newCursor.moveToFirst();
        }

        return newCursor;
    }

    private static boolean shouldReplace(String key, String[] possibleKeyNames) {
        if (possibleKeyNames == null || possibleKeyNames.length == 0) {
            return true; // Replace all if no specific keys are provided
        }
        for (String possibleKey : possibleKeyNames) {
            if (key.equalsIgnoreCase(possibleKey))
                return true;
        }
        return false;
    }


    //content://com.vivo.vms.IdProvider/IdentifierId/OAID
    //Target Service  like "com.google.gsf.service"
    //Then column Arg like "android_id"
    //Then use setting to replace
}
