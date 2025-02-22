package eu.faircode.xlua.x.file;

import android.os.Process;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.x.data.utils.Linq;

public enum UnixUserId {
    NONE(-1),
    ROOT(0),            //root
    SYSTEM(Process.SYSTEM_UID),//system
    SHELL(2000),        //shell
    NOBODY(9999),       //nobody
    MEDIA_RW(1023),     //media_rw
    BLUETOOTH(1002),    //bluetooth
    WIFI(1010),         //wifi
    RADIO(1001),        //radio
    INPUT(1004),        //input
    GRAPHICS(1003),     //graphics
    LOG(1007),          //log
    SECURITY(1009),     //security
    ADB(1041),          //adb
    //U0_AN(1000000),   //Can be between from 10000 ? not rlly a known one dont use this  (u0_an)
    APP_ZYGOTE(1015);   //app_zygote

    private final int value;
    UnixUserId(int value) { this.value = value; }
    public int getValue() { return value; }

    public static UnixUserId fromValue(int value) { return Linq.firstWhereOrDefault(values(), NONE, (v) -> v.value == value); }
}
