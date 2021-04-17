package com.android.messaging.util;

import android.util.Log;

/**
 * Created by quangnd on 24/08/2017.
 */

public class LogTime {
    private static final String TAG = "Btalk Log";

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
