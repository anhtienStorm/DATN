package bkav.android.btalk.esim.Utils;

import android.content.Context;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.messaging.Factory;
import com.qualcomm.qti.lpa.IUimLpaService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.esim.LPAController;
import bkav.android.btalk.esim.ProfileSimNormal;
import bkav.android.btalk.esim.ViberProfile;
import bkav.android.btalk.esim.ZaloProfile;
import bkav.android.btalk.esim.provider.ESimDbController;
import bkav.android.btalk.utility.PrefUtils;

import static bkav.android.btalk.esim.ActiveDefaultProfileReceiver.DEFAULT_PROFILE_SLOT_1_KEY;
import static bkav.android.btalk.esim.ActiveDefaultProfileReceiver.DEFAULT_PROFILE_SLOT_2_KEY;

public class ESimUtils {

    private static boolean mIsSupportEsim;


    //Bkav QuangNDb check ho tro hien thi eSim trong truong hop
    public static boolean isSupportEsim() {
        return mIsSupportEsim && ESimDbController.isEsimExist();
    }

    private static String getSystemProperty(String property, String defaultValue) {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method getter = clazz.getDeclaredMethod("get", String.class);
            String value = (String) getter.invoke(null, property);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static void checkSupportEsim() {
        mIsSupportEsim = !getSystemProperty("ro.bkav.hw.esim", "0").equals("0");
    }

    public static boolean isMultiProfile() {
        return ESimDbController.countSim() > 1;
    }

    //Bkav QuangNDb kich hoat 1 profile
    public static void enableProfile(ISimProfile profile, boolean isDefault) {
        try {
            if (!isDefault) {
                Context context = Factory.get().getApplicationContext();
                //Bkav QuangNDb them tat logic chi gui sim state change cho 1 minh Btalk
                TelephonyEsim telephonyEsim = new TelephonyEsim(context);
                telephonyEsim.disableBroadcastSimStateChange(profile.getSlotSim(), true);
            }
            ESimDbController.updateDBWhenActivatingEsim(profile);
            IUimLpaService mService = LPAController.getInstance(Factory.get().getApplicationContext()).getIUimLpaService();
            if (mService != null) {
                mService.uimLpaEnableProfile(profile.getSlotSim(), LPAController.TOKEN, profile.getSimIdProfile());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //Bkav QuangNDb check truong hop sim bi disable
    public static boolean isSlotNotReady(int slot) {
        TelephonyManager telephonyManager = (TelephonyManager) Factory.get().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return TelephonyManager.SIM_STATE_NOT_READY == telephonyManager.getSimState(slot);

    }

    //Bkav QuangNDb get all profile(sim thuong, esim, zalo, viber) tu 1 so dien thoai
    public static List<ISimProfile> getAllProfileWithNumber(String number) {
        Context context = Factory.get().getApplicationContext();
        List<ISimProfile> listSim = new ArrayList<>();
        if (isSupportEsim()) {
            listSim.addAll(ESimDbController.getAllSim());
        } else {
            if (!isSlotNotReady(0)) {
                ProfileSimNormal sim1 = new ProfileSimNormal(context, 0);
                if (sim1.getSubscriptionInfo() != null) {
                    listSim.add(sim1);
                }
            }

            if (!isSlotNotReady(1)) {
                ProfileSimNormal sim2 = new ProfileSimNormal(context, 1);
                if (sim2.getSubscriptionInfo() != null) {
                    listSim.add(sim2);
                }
            }
        }
        if (OTTUtils.get().isAddProfileZalo(number)) {
            ZaloProfile zaloProfile = new ZaloProfile();
            listSim.add(zaloProfile);
        }
        if (OTTUtils.get().isAddProfileViber(number)) {
            ViberProfile viberProfile = new ViberProfile();
            listSim.add(viberProfile);
        }
        return listSim;
    }

    //Bkav QuangNDb get all profile(sim thuong, esim, zalo, viber) cua 1 slot tu 1 so dien thoai
    public static List<ISimProfile> getAllProfileFromSlotWithNumber(int slot, String number) {
        Context context = Factory.get().getApplicationContext();
        List<ISimProfile> listSim = new ArrayList<>();
        if (isSlotNotReady(slot)) {
            return listSim;
        }
        if (isSupportEsim()) {
            listSim.addAll(ESimDbController.getAllProfileForSlot(slot));
        } else {
            ProfileSimNormal profileSimNormal = new ProfileSimNormal(context, slot);
            if (profileSimNormal.getSubscriptionInfo() != null) {
                listSim.add(profileSimNormal);
            }
        }
        if (OTTUtils.get().isAddProfileZalo(number)) {
            ZaloProfile zaloProfile = new ZaloProfile();
            listSim.add(zaloProfile);
        }
        if (OTTUtils.get().isAddProfileViber(number)) {
            ViberProfile viberProfile = new ViberProfile();
            listSim.add(viberProfile);
        }
        return listSim;
    }

    //Bkav QuangNDb get all profile(sim thuong, esim, zalo, viber) tu 1 so dien thoai va exclude activate profile
    public static List<ISimProfile> getAllProfileWithNumberExcludeActivateProfile(String number, int defaultSlot) {
        Context context = Factory.get().getApplicationContext();
        List<ISimProfile> listSim = new ArrayList<>();
        if (isSupportEsim()) {
            listSim.addAll(ESimDbController.getAllSimExcludeActiveDefaultSlot(defaultSlot));
        } else {
            if (!isSlotNotReady(0)) {
                ProfileSimNormal sim1 = new ProfileSimNormal(context, 0);
                if (sim1.getSubscriptionInfo() != null) {
                    listSim.add(sim1);
                }
            }

            if (!isSlotNotReady(1)) {
                ProfileSimNormal sim2 = new ProfileSimNormal(context, 1);
                if (sim2.getSubscriptionInfo() != null) {
                    listSim.add(sim2);
                }
            }
        }
        if (OTTUtils.get().isAddProfileZalo(number)) {
            ZaloProfile zaloProfile = new ZaloProfile();
            listSim.add(zaloProfile);
        }
        if (OTTUtils.get().isAddProfileViber(number)) {
            ViberProfile viberProfile = new ViberProfile();
            listSim.add(viberProfile);
        }
        return listSim;
    }

    //Bkav QuangNDb get all profile(sim thuong, esim) khong get profile OTT
    public static List<ISimProfile> getAllProfileExcludeOTT() {
        Context context = Factory.get().getApplicationContext();
        List<ISimProfile> listSim = new ArrayList<>();
        if (isSupportEsim()) {
            listSim.addAll(ESimDbController.getAllSim());
        } else {
            if (!isSlotNotReady(0)) {
                ProfileSimNormal sim1 = new ProfileSimNormal(context, 0);
                if (sim1.getSubscriptionInfo() != null) {
                    listSim.add(sim1);
                }
            }

            if (!isSlotNotReady(1)) {
                ProfileSimNormal sim2 = new ProfileSimNormal(context, 1);
                if (sim2.getSubscriptionInfo() != null) {
                    listSim.add(sim2);
                }
            }
        }
        return listSim;
    }

    //Bkav QuangNDb lay ra 1 activate profile(esim, sim thuong) tu 1 slot
    public static ISimProfile getActivateProfileFromSlot(int slot) {
        if (isSupportEsim()) {
            return ESimDbController.getActivateProfileFromSlot(slot);
        }
        ProfileSimNormal profileSimNormal = new ProfileSimNormal(Factory.get().getApplicationContext(), slot);
        if (profileSimNormal.getSubscriptionInfo() != null) {
            return profileSimNormal;
        }
        return null;
    }

    //Bkav QuangNDb luu lai default esim profile de kich hoat lai khi can thiet
    public static void saveEsimDefaultProfile(Context context) {
        if (mIsSupportEsim) {
            //Bkav QuangNDb save profile slot 2
            String iccid = ESimDbController.getIccIdActivateProfileWithSlot(0, context);
            PrefUtils.get().saveStringPreferences(context, DEFAULT_PROFILE_SLOT_1_KEY, iccid);
            //Bkav QuangNDb save profile slot 2
            iccid = ESimDbController.getIccIdActivateProfileWithSlot(1, context);
            PrefUtils.get().saveStringPreferences(context, DEFAULT_PROFILE_SLOT_2_KEY, iccid);
        }
    }
}
