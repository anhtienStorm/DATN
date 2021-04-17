package com.android.ex.chips;

import android.util.Log;

/**
 * Created by quangnd on 25/08/2017.
 */

public class LogTime {
    private static final String TAG = "Chip Log";

    private static final boolean DEBUG = true;

    private static long mTime = 0;

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
