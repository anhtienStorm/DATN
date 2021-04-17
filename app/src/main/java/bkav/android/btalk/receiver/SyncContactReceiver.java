package bkav.android.btalk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import bkav.android.btalk.BtalkApplication;
import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;

public class SyncContactReceiver extends BroadcastReceiver {

    private static final String ACTION_SYNC_CONTACT = "bkav.android.SYNC_CONTACT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(ACTION_SYNC_CONTACT)) {
            if (!BtalkApplication.isIsForeground()) {
                BtalkDialerDatabaseHelper.getInstance(context).startSmartDialUpdateThread();
            }
        }
    }

}
