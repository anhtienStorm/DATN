package bkav.android.btalk.esim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;

import com.android.messaging.Factory;

import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.esim.Utils.TelephonyEsim;
import bkav.android.btalk.esim.provider.ESimDbController;
import bkav.android.btalk.utility.PrefUtils;

/**
 * Bkav QuangNDb class lang nghe su kien vuot home, screen off, trang thai sim, trang thai gui tin nhan, vuot back de kich hoat lai sim cu
 * Luu y khi sua ten class nay phai trao doi voi ben App Esim cua DucLQ vi ben app eSim co ban 1 receiver thay doi profile mac dinh sang day
 */
public class ActiveDefaultProfileReceiver extends BroadcastReceiver {

    // Bkav HuyNQN Lang nghe su kien vuot home tu trong rom
    public final static String ACTION_MOVE_TO_HOME = "bkav.android.btalk.action.HOME";

    public static final String GET_CODE_RESULT = "bkav.android.intent.action.GET_CODE_RESULT";
    public static final String EVENT_BACK_PRESS = "bkav.android.intent.action.EVENT_BACK_PRESS";
    public static final String EVENT_BACK_PRESS_BTALK_ACTIVITY = "bkav.android.intent.action.EXIT_BTALK";
    public static final int SMS_CODE_DEFAULT = 9;
    //Bkav QuangNDb action lang nghe update db cua app esim
    public static final String UPDATE_PROFILE_STATE_FROM_ESIM_APP = "bkav.android.intent.esim.action_UPDATE_PROFILE_STATE";
    public static final String PROFILE_SLOT = "slot";
    public static final String PROFILE_ICCID = "iccid";
    public static final String PROFILE_STATE = "state";
    public static final String DEFAULT_PROFILE_SLOT_1_KEY = "default_profile_slot_1";
    public static final String DEFAULT_PROFILE_SLOT_2_KEY = "default_profile_slot_2";
    //Bkav QuangNDb sms status code de nhan biet khi nao thi gui tin nhan thanh cong
    private static int mSmsStatusCode = SMS_CODE_DEFAULT;
    public String mSimState = "";

    // Bkav HuyNQN them interface lang nghe su kien vuot home
    public interface ActionHome{
        void homeClick();
    }

    private ActionHome mListener;

    public void setListener(ActionHome mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            return;
        }
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            turnOnDefaultProfileIfNeed();
        } else if (intent.getAction().equals(ACTION_MOVE_TO_HOME)) {
            turnOnDefaultProfileIfNeed();
            if(mListener != null){
                mListener.homeClick();
            }
        } else if(intent.getAction().equals(EVENT_BACK_PRESS) || intent.getAction().equals(EVENT_BACK_PRESS_BTALK_ACTIVITY)){
            turnOnDefaultProfileIfNeed();
        } else if (UPDATE_PROFILE_STATE_FROM_ESIM_APP.equals(intent.getAction())) {
            //Bkav QuangNDb cap nhat lai sim mac dinh cua 2 slot
            int slot = intent.getIntExtra(PROFILE_SLOT, -1);
            String iccid = intent.getStringExtra(PROFILE_ICCID);
            boolean state = intent.getBooleanExtra(PROFILE_STATE, false);
            if (slot == 0) {
                PrefUtils.get().saveStringPreferences(context, DEFAULT_PROFILE_SLOT_1_KEY, state ? iccid : "");
            } else if (slot == 1) {
                PrefUtils.get().saveStringPreferences(context, DEFAULT_PROFILE_SLOT_2_KEY, state ? iccid : "");
            }
        }
        // Bkav HuyNQN intent nhan resultcode status cuar tin nhan vua gui
        if (GET_CODE_RESULT.equals(intent.getAction())) {
            mSmsStatusCode = intent.getIntExtra("code", SMS_CODE_DEFAULT);
        }
    }

    //Bkav QuangNDb bat lai profile mac dinh trong truong hop khong dang gui tin nhan
    private void turnOnDefaultProfileIfNeed() {
        // FIXME: 19/03/2020 bug gui tin nhan chua kich hoat se bi loi
        if (mSmsStatusCode != SmsManager.RESULT_ERROR_NO_SERVICE) {
            turnOnDefaultProfile();
        } else {
            mCountDownCheckSignal.start();
        }
    }

    //Bkav QuangNDb cap nhat lai default profile
    private void turnOnDefaultProfile() {
        Context context = Factory.get().getApplicationContext();
        //Bkav QuangNDb them tat logic chi gui sim state change cho 1 minh Btalk
        TelephonyEsim telephonyEsim = new TelephonyEsim(context);
        telephonyEsim.disableBroadcastSimStateChange(-1, false);
        enableProfile(PrefUtils.get().loadStringPreferences(context, DEFAULT_PROFILE_SLOT_1_KEY, ""));
        enableProfile(PrefUtils.get().loadStringPreferences(context, DEFAULT_PROFILE_SLOT_2_KEY, ""));

    }

    //Bkav QuangNDb enable profile tu 1 iccid
    private void enableProfile(String iccid) {
        if (!iccid.isEmpty()) {
            ISimProfile iSimProfile = ESimDbController.getProfileFromIccId(iccid);
            if (iSimProfile != null) {
                ESimUtils.enableProfile(iSimProfile, true);
            }
        }
    }

    private static final int TIME_OUT_CHECK_SIGNAL = 20000/*20s*/;
    private static final int TIME_TICK = 1000/*1s*/;
    //Bkav HuyNQn dem nguoc de doi lai sim mac dinh khi thoat ra ngoai và ResponseCode = 4
    private CountDownTimer mCountDownCheckSignal = new CountDownTimer(TIME_OUT_CHECK_SIGNAL, TIME_TICK) {
        @Override
        public void onTick(long millisUntilFinished) {
            if (mSmsStatusCode != SmsManager.RESULT_ERROR_NO_SERVICE) {
                onFinish();
            }
        }

        @Override
        public void onFinish() {
            turnOnDefaultProfile();
            mSmsStatusCode = SMS_CODE_DEFAULT;
            mCountDownCheckSignal.cancel();
        }
    };
}
