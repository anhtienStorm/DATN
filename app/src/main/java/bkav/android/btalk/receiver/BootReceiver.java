package bkav.android.btalk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;

/**
 * Created by anhdt on 14/08/2017.
 *
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        BtalkDialerDatabaseHelper.getInstance(context).startSmartDialUpdateThread();
    }
}
