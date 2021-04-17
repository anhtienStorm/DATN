package bkav.android.btalk.utility;

import java.lang.reflect.Method;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import bkav.android.btalk.trial_mode.TrialModeController;

import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.util.HelpUtils;

import bkav.android.btalk.R;
import bkav.android.btalk.compat.Utils;

/**
 * Created by trungth on 05/07/2017.
 */

public class Config {
    public static final boolean IS_BPHONE = true;

   /**
    * Check la myanma
    * @return
    */
    public static boolean isMyanmar() {
        return Utils.isMyanmar();
    }

    public static boolean isTrialMode(Context context) {
       return Settings.System.getInt(context.getContentResolver(),
                TrialModeController.MODE_TRIAL, 0) == 1;
    }

    public static AccountWithDataSet changeAccountIfLocal(Context context, AccountWithDataSet account){
        if(isLocalAccount(context, account)) {
            return account = account.type == null ? null :account;
        }
        return account;
    }

    // Bkav HuyNQN ca ban vietnam va myanmar deu thuc hien tao tai khoan local neu khong co acc google
    public static boolean isLocalAccount(Context context, AccountWithDataSet account){
        return /*Config.isMyanmar() && */account !=null && account.name.equals(context.getString(R.string.keep_local));
    }
}
