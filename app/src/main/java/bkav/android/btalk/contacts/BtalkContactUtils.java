package bkav.android.btalk.contacts;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.incallui.Log;


public class BtalkContactUtils {
    //AnhNDd: dung de show log
    public static boolean DEV_DEBUG = false;

    //AnhNDd: package name cua app
    //public static String PACKAGE_NAME = "bkav.android.btalk";

    //AnhNDd: string de put extra khi export contacts
    public static String SEL_EXPORT= "SelExport";

    public static String TAG = "BtalkContacts";

    public static void showLog(String show) {
        if (DEV_DEBUG) {
            Log.i(TAG, show);
        }
    }
    //--------------------------

    //============================================================
    public static boolean isConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return true;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }
}
