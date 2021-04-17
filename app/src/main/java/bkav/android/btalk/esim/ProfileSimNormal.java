package bkav.android.btalk.esim;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import bkav.android.btalk.R;

public class ProfileSimNormal implements ISimProfile {

    private Context mContext;
    private int mSlot;
    private int mSim1Color;
    private int mSim2Color;

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public int getSlot() {
        return mSlot;
    }

    public void setSlot(int mSlot) {
        this.mSlot = mSlot;
    }

    public ProfileSimNormal(Context mContext, int mSlot) {
        this.mContext = mContext;
        this.mSlot = mSlot;
        mSim1Color = mContext.getColor(R.color.esim_01);
        mSim2Color = mContext.getColor(R.color.esim_02);
        subscriptionManager = (SubscriptionManager)mContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        subscriptionInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(mSlot);
    }

    private SubscriptionManager subscriptionManager;

    public SubscriptionInfo getSubscriptionInfo() {
        return subscriptionInfo;
    }

    private SubscriptionInfo subscriptionInfo;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public String getNameSimProfile() {
        return subscriptionInfo.getDisplayName().toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public String getNickNameProfile() {
        return subscriptionInfo.getNumber();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public byte[] getSimIdProfile() {
        return subscriptionInfo.getIccId().getBytes();
    }

    @Override
    public boolean getSimProfileState() {
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public int getSlotSim() {
        return subscriptionInfo.getSimSlotIndex();
    }

    @Override
    public int getProfileIndex() {
        //Bkav QuangNDb tra ve dung so sim theo slot neu la sim thuong
        return subscriptionInfo.getSimSlotIndex() + 1;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public int getColor() {
        return subscriptionInfo.getSimSlotIndex() == 0 ? mSim1Color : mSim2Color;
    }

    @Override
    public int updateColor() {
        return subscriptionInfo.getSimSlotIndex() == 0 ? mSim2Color : mSim1Color;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public String getSimNameSetting() {
        return subscriptionInfo.getDisplayName().toString();
    }

    @Override
    public String toString() {
        return subscriptionInfo.getNumber() + " - " + subscriptionInfo.getDisplayName() + " - " + subscriptionInfo.getCarrierName() + " - " + subscriptionInfo.getIccId();
    }
}
