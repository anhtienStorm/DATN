package bkav.android.btalk;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.android.contacts.ContactsApplication;
import com.android.contacts.common.compat.CompatUtils;
import com.android.dialer.DialerApplication;
import com.android.messaging.BugleApplication;
import bkav.android.btalk.calllog.recoder.AutoDelateRecoder;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.esim.LPAController;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.messaging.BtalkBugleApplication;
import bkav.android.btalk.trial_mode.TrialModeController;
import bkav.android.btalk.utility.BtalkLog;


/**
 * Created by trungth on 15/03/2017.
 */

public class BtalkApplication extends Application implements LifecycleObserver {

    private ContactsApplication mContactsApplication;

    private BugleApplication mBugleApplication;

    private DialerApplication mDialerApplication;

    //Bkav QuangNDb check activity foreground
    private static boolean isForeground;

    public static boolean isIsForeground() {
        return isForeground;
    }

    //Bkav QuangNDb lang nghe life cycle de tranh loi smart dialer thinh thoang khong hoat dong khi sync contact
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void activityResumed() {
        isForeground = true;
    }

    //Bkav QuangNDb lang nghe life cycle de tranh loi smart dialer thinh thoang khong hoat dong khi sync contact
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void activityStop() {
        isForeground = false;
    }

    private LPAController mLpaController;

    @Override
    public void onCreate() {
        super.onCreate();
        //Bkav QuangNDb connect esim service
        mLpaController = LPAController.getInstance(getApplicationContext());
        // Bkav HuyNQN  BPHONE4-236 start, dang ki service ESIM
//        mLpaController.checkConnectService(getApplicationContext());
        ESimUtils.checkSupportEsim();
        BtalkLog.resetTime();
        // Bkav Trungth - Khoi tao contactApplication
        mContactsApplication = new ContactsApplication(this);
        mContactsApplication.onCreate();
        // Bkav QuangNDb - sua ham khoi tao cua messageApplication
        mBugleApplication = new BtalkBugleApplication(this);
        mBugleApplication.onCreate();
        mDialerApplication = new DialerApplication(this);
        mDialerApplication.onCreate();
        new TrialModeController(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mBugleApplication != null) {
            mBugleApplication.onConfigurationChanged(newConfig);
        }
        //TrungTH Clear cache de load lai
        BtalkCallLogCache.getCallLogCache(getApplicationContext()).clearCache();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mBugleApplication != null) {
            mBugleApplication.onLowMemory();
        }
    }


    @Override
    public ContentResolver getContentResolver() {
        if (mContactsApplication != null) {
            ContentResolver resolver = mContactsApplication.getContentResolver();
            if (resolver != null) {
                return resolver;
            }
        }
        return super.getContentResolver();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        if (mContactsApplication != null) {
            SharedPreferences prefs = mContactsApplication.getSharedPreferences(name, mode);
            if (prefs != null) {
                return prefs;
            }
        }
        return super.getSharedPreferences(name, mode);
    }

    @Override
    public Object getSystemService(String name) {
        if (mContactsApplication != null) {
            Object service = mContactsApplication.getSystemService(name);
            if (service != null) {
                return service;
            }
        }
        return super.getSystemService(name);
    }
}
