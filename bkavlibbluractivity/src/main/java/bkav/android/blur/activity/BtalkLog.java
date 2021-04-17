package bkav.android.blur.activity;

import android.util.Log;

/**
 * Created by trungth on 25/05/2017.
 */

public class BtalkLog {
    private static final String TAG = "Btalk Log";

    private static final boolean DEBUG = true;

    private static long mTime = 0;

    public static void logTime(String message) {
        if (DEBUG)
            Log.d(TAG, "logTime: " + message + " = " + (System.currentTimeMillis() - mTime));
    }

    public static void resetTime() {
        mTime = System.currentTimeMillis();
    }
}
