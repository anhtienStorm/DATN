package bkav.android.btalk.backup;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;

public class BkavBackupManagerApplication extends Application implements ComponentCallbacks{

    private static Context applicationContext = null;
	public static final Object sDataLock = new Object();
	
    @Override
    public void onCreate() {
        super.onCreate();
        if (applicationContext == null) {
        	applicationContext = getApplicationContext();
        }      
    }

    /**
     * Lấy thông tin Application
     * 
     * @return Trả về giá trị Application
     */
    public static Context getBCleanerAppContext() {
        return applicationContext;
    }
}