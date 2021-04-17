package bkav.android.btalk.esim.Utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TelephonyEsim {
    private Context mContext;

    private TelephonyManager mTelephonyManager;
    Class<?> mClass;


    public TelephonyEsim(Context context){
        mContext = context;
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            mClass = Class.forName("android.telephony.TelephonyManager");
        } catch (ClassNotFoundException e) {
            mClass = null;
        }
    }

    private boolean isReady() {
        return mTelephonyManager != null && mClass != null;
    }

    public void disableBroadcastSimStateChange(int slotId, boolean disable){
        if(isReady()){
            Method method = null;
            try {
                method = mClass.getMethod("dissableBroadcastSimStateChange", int.class, boolean.class);
                method.setAccessible(true);
                method.invoke(mTelephonyManager, slotId, disable);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
