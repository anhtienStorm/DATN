package bkav.android.btalk.messaging.ui.appsettings;

import android.content.Context;

import com.android.messaging.ui.appsettings.GroupMmsSettingDialog;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 05/06/2017.
 * Bkav QuangNDb custom lai dialog setting MMS group Option
 */

public class BtalkGroupMmsSettingDialog extends GroupMmsSettingDialog{

    private BtalkGroupMmsSettingDialog(Context context, int subId) {
        super(context, subId);
    }
    /**
     * Shows a new group MMS setting dialog.
     */
    public static void showDialog(final Context context, final int subId) {
        new BtalkGroupMmsSettingDialog(context, subId).show();
    }

    @Override
    protected int getIdResMMSGroupDefault() {
        return R.bool.btalk_group_mms_pref_default;
    }
}
