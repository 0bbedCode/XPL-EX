package eu.faircode.xlua.x.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.properties.MockPropSetting;
import eu.faircode.xlua.api.xstandard.UserIdentityPacket;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.utilities.StreamUtil;
import eu.faircode.xlua.utilities.StringUtil;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;

public class JsonHelperEx {
    private static final String TAG = "XLua.JsonHelperEx";

    private static final String ERROR_JSON_HOOKS = "Parsing HOOKS.JSON using the (non hook) JSON Parser... please use the correct JSON Parser avoid this one for Hooks! ";
    private static final String BLACKLISTED_JSON = "hooks.json";

    /**
     * Read a Collection of IJsonSerial Elements from Json file in Apk assets
     * <p>
     * Will search for the Target Json File, then once found it will Parse the Json Contents into a Collection of IJsonSerial Elements.
     * Ensure the Target Class Object to parse each Json Element into has a No Argument Constructor and overrides "fromJSONObject" function from IJsonSerial.
     * </p>
     * @param apk Base PackageName to search for Json File
     * @param jsonName Name of target Json to Search For
     * @param stopOnFirst Stop when on the first occurrence of Target Json
     * @param typeClass Class (inherits IJsonSerial) to Read each Json Element as
     * @return Collection of IJsonSerial Elements that were Parsed from the Json File
     */
    public static <T extends IJsonType> List<T> findJsonElementsFromAssets(String apk, String jsonName, boolean stopOnFirst, Class<T> typeClass) {
        List<T> list = new ArrayList<>();
        jsonName = StringUtil.trimEnsureEnd(jsonName, ".json");
        if(!StringUtil.isValidString(jsonName)) {
            Log.e(TAG, "Parameter Target Json was Null or Empty... ");
            return list;
        }

        String logEnd = " Json Name=" + jsonName + " Apk=" + apk + " Stop On First Json=" + stopOnFirst + " Class Name=" + typeClass.getName();

        if(DebugUtil.isDebug())
            Log.d(TAG, "Searching for Json File in Assets: " + logEnd);

        if(jsonName.equalsIgnoreCase(BLACKLISTED_JSON))
            Log.w(TAG, ERROR_JSON_HOOKS + logEnd);

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apk);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if(!entry.isDirectory() && name.startsWith("assets/") && name.endsWith(jsonName)) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Found Target Json: " + jsonName + "  Full asset File path=" + name);

                    Collection<T> items = parseJson(entry, zipFile, jsonName, typeClass);
                    if(!items.isEmpty()) list.addAll(items);
                    if(stopOnFirst) break;
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "Failed searching for Target Json: " + logEnd + " Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return list;
        }finally {
            StreamUtil.close(zipFile);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Finished Searching & Parsing Json File! Elements Parsed Count=" + ListUtil.size(list) + " " + logEnd);

        return list;
    }

    /**
     * Read a Collection of IJsonSerial Elements from a Found Json File in Apk assets
     * <p>
     * Parse a ZipFile that is suppose to represent the Target / Found Json File.
     * Pass off the results of the targeted Json Files or File to then Parse Json Contents into a Collection of IJsonSerial
     * Ensure the Target Class Object to parse each Json Element into has a No Argument Constructor and overrides "fromJSONObject" function from IJsonSerial.
     * </p>
     * @param entry Entry to the root Zip file
     * @param zipFile ZipFile that represents Json file to be Parsed into IJsonSerial Elements
     * @param jsonName Name of target Json that you will be parsing
     * @param typeClass Class (inherits IJsonSerial) to Read each Json Element as
     * @return Collection of IJsonSerial Elements that were Parsed from the Json File
     */
    public static <T extends IJsonType> Collection<T> parseJson(ZipEntry entry, ZipFile zipFile, String jsonName, Class<T> typeClass) {
        Collection<T> list = new ArrayList<>();
        jsonName = StringUtil.trimEnsureEnd(jsonName, ".json");
        if(!StringUtil.isValidString(jsonName)) {
            Log.e(TAG, "Parameter Target Json was Null or Empty... ");

            return list;
        }

        String entryName = entry.getName();
        String entryPath = entryName.substring(0, entryName.length() - jsonName.length() - 1);

        String logEnd = " Path=" + entryPath + " Json Name=" + jsonName + " Class Name=" + typeClass.getName();

        if(DebugUtil.isDebug())
            Log.d(TAG, "Parsing Json From Assets:" + logEnd);

        if(jsonName.equalsIgnoreCase(BLACKLISTED_JSON))
            Log.w(TAG, ERROR_JSON_HOOKS + logEnd);

        InputStream is = null;
        try {
            is = zipFile.getInputStream(entry);
            String json = new Scanner(is).useDelimiter("\\A").next();
            JSONArray jArray = new JSONArray(json);
            for (int i = 0; i < jArray.length(); i++) {
                T item = typeClass.newInstance();
                item.fromJSONObject(jArray.getJSONObject(i));
                list.add(item);
            }
        }catch (JSONException je) {
            Log.e(TAG, "Failed to Parse/Read Json Element to IJsonSerial Object. Function [fromJSONObject] failed for IJsonSerial class=" + typeClass.getName() + logEnd + " Error=" + je + " Stack=" + RuntimeUtils.getStackTraceSafeString(je));
            return list;
        }catch (IllegalAccessException ie) {
            Log.e(TAG, "Failed to Construct IJsonSerial class=" + typeClass.getName() + logEnd + " Error=" + ie + " Stack=" + RuntimeUtils.getStackTraceSafeString(ie));
            return list;
        }catch (Exception e) {
            Log.e(TAG,"Failed to parse Json From Assets!" + logEnd + " Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return list;
        }finally {
            StreamUtil.close(zipFile);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Finished Parsing JSON From Assets! Elements Parsed Count=" + ListUtil.size(list) + " " + logEnd);

        return list;
    }

    public static List<String> getStringArrayAsList(JSONObject rootObject, String name) {
        List<String> items = new ArrayList<>();
        try {
            JSONArray jArray = rootObject.getJSONArray(name);
            if(jArray.length() < 1) {
                Log.e(TAG, "JSON Array is Empty! Name=" + name + "... Stack=" + RuntimeUtils.getStackTraceSafeString());
                return items;
            }

            for(int i = 0; i < jArray.length(); i++)
                items.add(jArray.getString(i));

            if(DebugUtil.isDebug())
                Log.d(TAG, "Finished Getting String List from Json Array! Count=" + items.size() + " Name=" + name);

            return items;
        }catch (Exception e) {
            Log.e(TAG, "Error Getting Json Items as String List for Object Name:" + name + " Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return items;
        }
    }
}
