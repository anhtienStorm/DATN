package bkav.android.btalk.receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import bkav.android.btalk.activities.BtalkActivity;

public class MissCallReceiverLollipop extends BroadcastReceiver {
    private static boolean ring = false;
    private static boolean callReceived = false;

    @Override
    public void onReceive(final Context mContext, Intent intent) {
        // Get the current Phone State

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            return;
        }

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (state == null) {
            return;
        }

        Bundle bundle = intent.getExtras();
        String number;
        if (bundle != null) {
            number = bundle.getString("incoming_number");
        } else {
            return;
        }
        // If phone state "Rininging"
        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            ring = true;
            // Get the Caller's Phone Number

        }


        // If incoming call is received
        if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            callReceived = true;
        }


        // If phone is Idle
        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            // If phone was ringing(ring=true) and not received(callReceived=false) , then it is a missed call
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ring && !callReceived) {
                        int unRead = 0;
                        String[] selections = new String[]{CallLog.Calls._ID};
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        Cursor cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, selections, CallLog.Calls.IS_READ + " = 0 ", null, null);
                        if (cursor != null) {
                            unRead = cursor.getCount();
                            cursor.close();
                        }
                        Intent intentUnread = new Intent();
                        intentUnread.setAction(BtalkActivity.ACTION_UNREAD_CHANGED);
                        intentUnread.putExtra(BtalkActivity.INTENT_EXTRA_BADGE_COUNT, unRead);
                        intentUnread.putExtra(BtalkActivity.INTENT_EXTRA_PACKAGENAME, "bkav.android.btalk");
                        intentUnread.putExtra(BtalkActivity.INTENT_EXTRA_ACTIVITY_NAME, "bkav.android.btalk.activities.BtalkActivity");
                        intentUnread.setClassName("bkav.android.launcher3",
                                "com.android.launcher3.bkav.BkavUnreadReceive");
                        mContext.sendBroadcast(intentUnread);
                        //workingWithFunctions();
                        ring = false;
                    }
                    callReceived = false;
                }
            }, 1000);
        }
    }
}
