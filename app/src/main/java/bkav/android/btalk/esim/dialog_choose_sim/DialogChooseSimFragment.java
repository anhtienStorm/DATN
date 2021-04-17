package bkav.android.btalk.esim.dialog_choose_sim;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.android.contacts.common.CallUtil;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.util.DialerUtils;
import com.android.messaging.Factory;
import com.android.messaging.ui.UIIntents;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.esim.ActiveDefaultProfileReceiver;
import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.esim.TelephonyStateListener;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.esim.Utils.OTTUtils;
import bkav.android.btalk.esim.adapter.BtalkEsimDialogAdapter;
import bkav.android.btalk.fragments.BtalkPhoneFragment;
import bkav.android.btalk.mutilsim.SimUltil;

public class DialogChooseSimFragment extends DialogFragment implements BtalkEsimDialogAdapter.ESimAdapterListener
        , TelephonyStateListener.TelephonyListener, ActiveDefaultProfileReceiver.ActionHome {

    private static final int SLOT_0 = 0;
    private static final int SLOT_1 = 1;
    private RecyclerView mRecyclerView;
    private BtalkEsimDialogAdapter mBtalkEsimDialogAdapter;
    private static final String PHONE_NUMBER = "number";
    private static final String CALL_INTENT = "call_intent";
    private String mNumber;
    private ISimProfile mCurrentProfile;
    private CheckBox mAlwaysUse;
    private boolean mIsDismiss;
    private boolean mIsStateInService;
    private TelephonyStateListener mTelephonyStateListener;
    private TelephonyManager mTelephonyManager;
    private static final int TIME_OUT_CHECK_SIGNAL = 30000/*30s*/;//Bkav QuangNDb thoi gian timeout check xem sim moi duoc kich hoat co song hay khong
    private static final int TIME_TICK = 1000/*1s*/;//Bkav QuangNDb cu 1s se check xem sim moi active da co song hay chua
    private Intent mCallIntent;

    //Bkav QuangNDb bien dem nguoc check song
    private CountDownTimer mCountDownTimer = new CountDownTimer(TIME_OUT_CHECK_SIGNAL, TIME_TICK) {
        @Override
        public void onTick(long millisUntilFinished) {
            if (mIsStateInService) {
                // Bkav HuyNQN delay de tranh bi loi khong kha dung
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onFinish();
                    }
                },2000);

            }
        }

        @Override
        public void onFinish() {
            makeACallAfterCheckSignal();
        }
    };

    //Bkav QuangNDb thuc hien cuoc goi sau khi sim da enable va sau khi check song
    private void makeACallAfterCheckSignal() {
        mIsShowDialogOrRequestFakeCall = false;
        makeACall(mCurrentProfile);
        mDialogSimEnable.dismiss();
        mIsDismiss = true;
    }

    private int mDefaultSlot = BtalkEsimDialogAdapter.INDEX_SLOT; // Bkav HuyNQN bien xac dinh khe sim mac dinh

    //Bkav QuangNDb set default trong truong hop goi bang sim khac khi co eSim thi bo item profile da duoc active ra
    public void setSlotDefault(int slotDefault) {
        this.mDefaultSlot = slotDefault;
    }

    public static DialogChooseSimFragment newInstance(String number) {
        Bundle args = new Bundle();
        args.putString(PHONE_NUMBER, number);
        DialogChooseSimFragment fragment = new DialogChooseSimFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static DialogChooseSimFragment newInstance(Intent callIntent) {
        Bundle args = new Bundle();
        args.putParcelable(CALL_INTENT, callIntent);
        String number = callIntent.getData().toString().replaceAll("[^0123456789PWN\\,\\;\\*\\#\\+\\:]", "");
        args.putString(PHONE_NUMBER, number);
        DialogChooseSimFragment fragment = new DialogChooseSimFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private ActiveDefaultProfileReceiver mActionHome ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        getContext().registerReceiver(mSimStateReceiver, intentFilter);
        mNumber = getArguments().getString(PHONE_NUMBER);
        mCallIntent = getArguments().getParcelable(CALL_INTENT);
        mTelephonyStateListener = new TelephonyStateListener();
        mTelephonyStateListener.setListener(DialogChooseSimFragment.this);
        mTelephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mTelephonyStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        registerActionHome();
    }

    private void registerActionHome() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActiveDefaultProfileReceiver.ACTION_MOVE_TO_HOME);
        mActionHome = new ActiveDefaultProfileReceiver();
        mActionHome.setListener(this);
        getContext().registerReceiver(mActionHome, intentFilter);
    }

    //Bkav QuangNDb co bao khong co slot dat mac dinh khi goi
    private static final int NO_DEFAULT = 3;

    private List<ISimProfile> getListSim() {
        return mDefaultSlot == NO_DEFAULT ? ESimUtils.getAllProfileWithNumber(mNumber) : ESimUtils.getAllProfileWithNumberExcludeActivateProfile(mNumber, mDefaultSlot);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_choose_sim, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDialogSimEnable = new ProgressDialog(getContext());
        mRecyclerView = view.findViewById(R.id.list_esim);
        mRecyclerView.setHasFixedSize(true);
        mBtalkEsimDialogAdapter = new BtalkEsimDialogAdapter(getListSim());
        mBtalkEsimDialogAdapter.setListener(this);
        mRecyclerView.setAdapter(mBtalkEsimDialogAdapter);
        mAlwaysUse = view.findViewById(R.id.chk_always_use);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == BtalkPhoneFragment.CALL_REQUEST_CODE) {
            if (data.getBooleanExtra("end_call", false)) {
                mIsShowDialogOrRequestFakeCall = false;
                dismiss();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Bkav HuyNQN se thuc hien dismiss dialog khi thuc hien vuot home va mo lai
        if (mIsDismiss) {
            mIsDismiss = false;
            dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCurrentProfile = null;
        if (mSimStateReceiver != null) {
            getContext().unregisterReceiver(mSimStateReceiver);
        }
    }

    @Override
    public void setClickItemProfile(ISimProfile profile) {
        mCurrentProfile = profile;
        // Bkav TienNAb: fix loi khong duoc so dien thoai khi bat dau bang "84"
        if (mNumber != null){
            if (mNumber.startsWith("84")){
                mNumber = "+" + mNumber;
            }
        }

        if ("Viber".equals(profile.getNameSimProfile())) {
            OTTUtils.get().callFreeWithViber(mNumber);
            dismiss();
            return;
        }
        if ("Zalo".equals(profile.getNameSimProfile())) {
            OTTUtils.get().callFreeWithZalo(mNumber);
            dismiss();
            return;
        }

        if (!profile.getSimProfileState()) {

            ESimUtils.enableProfile(profile, false);

            if (DialerUtils.isDialerBkav(getContext())) {
                try {
                    // Bkav HuyNQN neu la ma MMI se khong goi vao FakeCall ma bat showDialogEnableEsim
                    if(SimUltil.isPotentialInCallMMICode(getUriHandle()) || SimUltil.isPotentialMMICode(getUriHandle())){
                        showDialogEnableEsim(profile);
                    }else {
                        UIIntents.get().requestFakeCall(mNumber, this, profile.getProfileIndex());
                        mIsShowDialogOrRequestFakeCall = true;
                    }
                } catch (Exception e) {
                    showDialogEnableEsim(profile);
                }
            } else {
                showDialogEnableEsim(profile);
            }
        } else {
            makeACall(profile);
            mIsDismiss = true;
        }
        if(mListener != null){
            mListener.chooseSimCall();
        }
    }

    private ProgressDialog mDialogSimEnable;
    private boolean mIsShowDialogOrRequestFakeCall;

    private void showDialogEnableEsim(ISimProfile profile) {
        mDialogSimEnable.setTitle(getResources().getString(R.string.title_enable_esim));
        mDialogSimEnable.setMessage(profile.getNameSimProfile() + " " + getResources().getString(R.string.message_enable_esim));
        mIsShowDialogOrRequestFakeCall = true;
        mDialogSimEnable.show();

    }

    private static final String EXTRA_SIM_STATE = "ss";
    public static final String LOADED = "LOADED";
    private BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mCurrentProfile != null) {
                String state = intent.getExtras().getString(EXTRA_SIM_STATE);
                if (LOADED.equalsIgnoreCase(state) && mIsShowDialogOrRequestFakeCall) {
                    if (mIsStateInService) {
                        makeACallAfterCheckSignal();
                    } else {
                        mCountDownTimer.start();
                    }
                }
            }
        }
    };


    //Bkav QuangNDb thuc hien cuoi goi
    private void makeACall(ISimProfile profile) {
        Context context = Factory.get().getApplicationContext();
        List<PhoneAccountHandle> subscriptionAccountHandles =
                PhoneAccountUtils.getSubscriptionPhoneAccounts(context);
        if (subscriptionAccountHandles == null) {
            return;
        }
        String callId = "";
        if (BtalkCallLogCache.getCallLogCache(context).isHasSimOnAllSlot()) { // Bkav HuyNQN fix loi lap 1 sim se bi crash do khong lay duoc callIs
            callId = profile.getSlotSim() == SLOT_0 ? subscriptionAccountHandles.get(0).getId() : subscriptionAccountHandles.get(1).getId();
        } else {
            callId = subscriptionAccountHandles.get(0).getId();
        }

        if (mCallIntent != null) {
            for (PhoneAccountHandle handle : subscriptionAccountHandles) {
                if (handle.getId().equals(callId)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mCallIntent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", handle);
                    }
                    DialerUtils.startActivityWithErrorToast(getActivity() == null ? context : getActivity(), mCallIntent);
                }
            }
        } else {
            SimUltil.callWithSlotSim(getActivity() == null ? context : getActivity(), callId, mNumber);
        }
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        // TODO: 07/01/2020 neu tick vao check box thi chon sim do lam mac dinh luon, can check lai kich ban eSim va chi chay o khi
        //  Btalk la priv-app (nap vao trong ROM) build tren android studio doan code duoi day khong hoat dong
        if (mAlwaysUse.isChecked()) {
            try {
                Class<?> c = Class.forName(TelecomManager.class.getName());
                Method setUserSelectedOutgoingPhoneAccount = c.getMethod("setUserSelectedOutgoingPhoneAccount", new Class[]{PhoneAccountHandle.class});
                setUserSelectedOutgoingPhoneAccount.invoke(telecomManager, profile.getSlotSim() == 0 ? subscriptionAccountHandles.get(0) : subscriptionAccountHandles.get(1));
            } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stateInService(String s) {
        mIsStateInService = true;
    }

    @Override
    public void stateOutOfService(String s) {
        mIsStateInService = false;
    }

    // Bkav HuyNQN lay ra uri thuc hien cuoc goi
    private Uri getUriHandle(){
        if(mCallIntent != null){
            return mCallIntent.getData();
        }else {
            return CallUtil.getCallIntent(mNumber).getData();
        }
    }

    @Override
    public void homeClick() { // Bkav HuyNQN vuot home thuc hien dong dialog chon sim
        mIsDismiss = true;
    }
    public interface IDialogChooseSimFragment{
        void chooseSimCall();
    }
    private IDialogChooseSimFragment mListener;

    public void setListener(IDialogChooseSimFragment mListener) {
        this.mListener = mListener;
    }
}
