package bkav.android.btalk.esim;

import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.util.Log;

public class TelephonyStateListener extends PhoneStateListener {
    public interface TelephonyListener {

        void stateInService(String s);

        void stateOutOfService(String s);
    }

    private TelephonyListener mListener;

    public void setListener(TelephonyListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onServiceStateChanged(ServiceState serviceState) {
        int state = serviceState.getState();

        switch (state) {
            case ServiceState.STATE_IN_SERVICE:
                if (mListener != null) {
                    mListener.stateInService("STATE_IN_SERVICE");
                }
                break;
            case ServiceState.STATE_OUT_OF_SERVICE:
            case ServiceState.STATE_EMERGENCY_ONLY:
            case ServiceState.STATE_POWER_OFF:
                if (mListener != null) {
                    mListener.stateOutOfService("STATE_OUT_OF_SERVICE");
                }
                break;
        }
    }
}
