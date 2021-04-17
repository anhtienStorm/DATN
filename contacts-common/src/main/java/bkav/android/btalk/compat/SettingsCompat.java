package bkav.android.btalk.compat;

import android.content.Context;
import android.provider.Settings;

import com.android.contacts.common.compat.CompatUtils;

/**
 * Created by trungth on 25/04/2017.
 */

public class SettingsCompat {
    public static class System {

        /**
         * Compatibility version of {@link android.provider.Settings.System#canWrite(Context)}
         *
         * Note: Since checking preferences at runtime started in M, this method always returns
         * {@code true} for SDK versions prior to 23. In those versions, the app wouldn't be
         * installed if it didn't have the proper permission
         */
        public static boolean canWrite(Context context) {
            if (CompatUtils.isMarshmallowCompatible()) {
                return Settings.System.canWrite(context);
            }
            return true;
        }
    }
}
