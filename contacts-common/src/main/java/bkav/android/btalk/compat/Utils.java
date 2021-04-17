package bkav.android.btalk.compat;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

public class Utils {

    /**
     * Check la myanma
     * @return
     */
    public static boolean isMyanmar() {
        return "myanmar".equals(getSystemProperty("ro.bkav.market",""));
    }

    public static String getSystemProperty(String property, String defaultValue) {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method getter = clazz.getDeclaredMethod("get", String.class);
            String value = (String) getter.invoke(null, property);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
            Log.d("Btalk", "Unable to read system properties");
        }
        return defaultValue;
    }

}
