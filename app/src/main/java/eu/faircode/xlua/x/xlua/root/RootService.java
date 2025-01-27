package eu.faircode.xlua.x.xlua.root;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import androidx.annotation.NonNull;

public class RootService extends com.topjohnwu.superuser.ipc.RootService implements Handler.Callback {
    private static final String TAG = "ObbedCode.RootService";

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        Log.i(TAG, "RootService: onBind");
        Handler handler = new Handler(Looper.getMainLooper(), this);
        return new Messenger(handler).getBinder();
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        Log.i(TAG, "RootService: " + msg.toString() + " what: " + msg.what);
        return false;
    }
}
