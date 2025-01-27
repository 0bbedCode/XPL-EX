package eu.faircode.xlua.utilities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.List;

public class ParcelUtil {
    private static final String TAG = "XLua.ParcelUtil";
    public static final String IGNORE_VALUE = "<<!>>";


    public static String parcelToHexString(Parcel parcel) {
        Log.w(TAG, "AD ID (phs) pos=" + parcel.dataPosition() + " size=" + parcel.dataSize());
        byte[] parcelBytes = parcel.marshall();
        parcel.setDataPosition(0);  // Reset parcel for future use

        StringBuilder hexString = new StringBuilder();
        for (byte b : parcelBytes) {
            hexString.append(String.format("%02X ", b));
        }

        return hexString.toString();
    }

    //public static void writeInt(Parcel in, int value, String defaultIgnore) {}


    public static void writeString(Parcel in, String value, String ignoreValue) {
        writeString(in, value, ignoreValue, true);
    }

    public static void writeString(Parcel in, String value, String ignoreValue, boolean checkEmpty) {
        if(in == null)
            return;

        String vw = value;
        if((!checkEmpty && value == null) || (checkEmpty && !StringUtil.isValidString(value)))
            vw = ignoreValue;

        in.writeString(vw);
    }

    public static String readString(Parcel in, String defaultValue, String ignoreValue) {
        String val = null;
        try {
            val = in.readString();
        }catch (Exception e) {
            Log.e(TAG, "Failed to read Parcel String: [" + e + "]");
        }

        if(val == null)
            return defaultValue;

        if(val.equals(ignoreValue))
            return defaultValue;

        return val;
    }

    public static void writeBool(Parcel in, Boolean bResult) {
        if(bResult == null) {
            //?
        }else {
            in.writeByte(bResult ? (byte)1 : (byte)0);
        }
    }

    public static Boolean readBool(Parcel in) {
        return in.readByte() == 1;
    }
    /*public final <T extends Parcelable> List<T> readParcelableList(List<T> list, Parcel p) {
        final int N = p.readInt();// readInt();
        if (N == -1) {
            list.clear();
            return list;
        }

        final int M = list.size();
        int i = 0;
        for (; i < M && i < N; i++) {
            //list.set(i, (T) readParcelable(cl));
            list.size(i, (T)p.readParcelable())
        }
        for (; i<N; i++) {
            list.add((T) readParcelable(cl));
        }
        for (; i<M; i++) {
            list.remove(N);
        }
        return list;
    }*/
}
