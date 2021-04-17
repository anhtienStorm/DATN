package bkav.android.btalk.esim;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;

// TODO: 14/03/2020 khong can dung class nay nua
public final class TelephonyInfo {

    private static TelephonyInfo mTelephonyInfo;
    private String mImeiSIM1;
    private String mImeiSIM2;
    private boolean mIsSIM1Ready;
    private boolean mIsSIM2Ready;

    public String getImsiSIM1() {
        return mImeiSIM1;
    }


    public String getiSIM2() {
        return mImeiSIM2;
    }


    public boolean isSIM1Ready() {
        return mIsSIM1Ready;
    }


    public boolean isSIM2Ready() {
        return mIsSIM2Ready;
    }


    public boolean isDualSIM() {
        return mImeiSIM2 != null;
    }

    private TelephonyInfo() {
    }

    public static TelephonyInfo getInstance(Context context) {

        mTelephonyInfo = new TelephonyInfo();

        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

        mTelephonyInfo.mImeiSIM1 = telephonyManager.getDeviceId();

        mTelephonyInfo.mImeiSIM2 = null;

        try {
            mTelephonyInfo.mImeiSIM1 = getDeviceIdBySlot(context, "getDeviceIdGemini", 0);
            mTelephonyInfo.mImeiSIM2 = getDeviceIdBySlot(context, "getDeviceIdGemini", 1);
        } catch (GeminiMethodNotFoundException e) {
            e.printStackTrace();

            try {
                mTelephonyInfo.mImeiSIM1 = getDeviceIdBySlot(context, "getDeviceId", 0);
                mTelephonyInfo.mImeiSIM2 = getDeviceIdBySlot(context, "getDeviceId", 1);
            } catch (GeminiMethodNotFoundException e1) {
                //Call here for next manufacturer's predicted method name if you wish
                e1.printStackTrace();
            }
        }

        mTelephonyInfo.mIsSIM1Ready = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
        mTelephonyInfo.mIsSIM2Ready = false;

        try {
            mTelephonyInfo.mIsSIM1Ready = getSIMStateBySlot(context, "getSimStateGemini", 0);
            mTelephonyInfo.mIsSIM2Ready = getSIMStateBySlot(context, "getSimStateGemini", 1);
        } catch (GeminiMethodNotFoundException e) {

            e.printStackTrace();

            try {
                mTelephonyInfo.mIsSIM1Ready = getSIMStateBySlot(context, "getSimState", 0);
                mTelephonyInfo.mIsSIM2Ready = getSIMStateBySlot(context, "getSimState", 1);
            } catch (GeminiMethodNotFoundException e1) {
                //Call here for next manufacturer's predicted method name if you wish
                e1.printStackTrace();
            }
        }

        return mTelephonyInfo;
    }

    private static String getDeviceIdBySlot(Context context, String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {

        String imei = null;

        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {

            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimID.invoke(telephony, obParameter);

            if (ob_phone != null) {
                imei = ob_phone.toString();

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return imei;
    }

    private static boolean getSIMStateBySlot(Context context, String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {

        boolean isReady = false;

        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {

            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimStateGemini = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimStateGemini.invoke(telephony, obParameter);

            if (ob_phone != null) {
                int simState = Integer.parseInt(ob_phone.toString());
                if (simState == TelephonyManager.SIM_STATE_READY) {
                    isReady = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return isReady;
    }


    private static class GeminiMethodNotFoundException extends Exception {

        private static final long serialVersionUID = -996812356902545308L;

        public GeminiMethodNotFoundException(String info) {
            super(info);
        }
    }

}
