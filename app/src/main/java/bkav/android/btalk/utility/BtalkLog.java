package bkav.android.btalk.utility;

import android.util.Log;

/**
 * Created by trungth on 25/05/2017.
 */

public class BtalkLog {
    private static final String TAG = "Btalk Log";

    private static final boolean DEBUG = false;

    public static long mTime = 0;

    public static void logTime(String message) {
        if (DEBUG)
            Log.d(TAG, "logTime: " + message + " = " + (System.currentTimeMillis() - mTime));
    }

    /**
     * QuangNDb them log D de dubug
     */
    public static void logD(String methodName, String message) {
        if (DEBUG) {
            Log.d(TAG, methodName + " " + message + ": ");
        }
    }
    public static void resetTime() {
        mTime = System.currentTimeMillis();
    }
}
