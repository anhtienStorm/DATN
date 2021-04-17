package com.android.common.util;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

public class DeviceVersionUtil {

    private static final String DEVICE_BL01_FLAG = "BL01";
    private static final String MODEL_ID_PROPERTY = "persist.sys.bkav.modelid";
    private static final String TAG = "Btalk";

    /**
     * Bkav HaiKH: Check the device is BL01
     * @return
     */
    public static boolean isBL01Device(){
        String device = getSystemProperty(MODEL_ID_PROPERTY, DEVICE_BL01_FLAG);
        return device.equals(DEVICE_BL01_FLAG);
    }

    /**
     * Bkav HaiKH: Gets the version of the device in use
     * @param property model id
     * @param defaultValue
     * @return
     */
    public static String getSystemProperty(String property, String defaultValue) {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method getter = clazz.getDeclaredMethod("get", String.class);
            String value = (String) getter.invoke(null, property);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to read system properties");
        }
        return defaultValue;
    }
}
