package bkav.android.btalk.messaging.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.messaging.datamodel.SyncManager;
import com.android.messaging.util.PhoneUtils;

import bkav.android.btalk.utility.BtalkLog;

/**
 * Created by quangnd on 16/08/2017.
 * class sync message
 * QuangNDb them receiver sync message de bat dc su kien khi khong dat default sms app
 */

public class SyncMessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        BtalkLog.logD("SyncMessageReceiver","onReceive: i received");
        if (!PhoneUtils.getDefault().isDefaultSmsApp()) {
            SyncManager.immediateSync();
            BtalkLog.logD("SyncMessageReceiver","onReceive: OK SYNC");
        }
    }
}
