/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.dialer.calllog;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.provider.VoicemailContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.dialer.util.AppCompatConstants;
import com.android.dialer.util.TelecomUtil;

/**
 * Receiver for call log events.
 * <p>
 * It is currently used to handle {@link VoicemailContract#ACTION_NEW_VOICEMAIL} and
 * {@link Intent#ACTION_BOOT_COMPLETED}.
 */
public class CallLogReceiver extends BroadcastReceiver {
    private static final String TAG = "CallLogReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Anhdts neu co quyen thi khi khoi dong lai hien thong bao goi nho len
        if (VoicemailContract.ACTION_NEW_VOICEMAIL.equals(intent.getAction())) {
            CallLogNotificationsService.updateVoicemailNotifications(context, intent.getData());
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            CallLogNotificationsService.updateVoicemailNotifications(context, null);
            updateMissCallNotify(context);
        } else {
            Log.w(TAG, "onReceive: could not handle: " + intent);
        }
    }

    /**
     * Anhdts update notify cuoc goi nho
     */
    public void updateMissCallNotify(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // Bkav TienNAb: Them Type MISSED_IMS_TYPE cua sim volte de truy van cuoc goi nho khi dung sim volte
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                new String[]{CallLog.Calls.NUMBER},
                "(" + CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE
                        + " or " + CallLog.Calls.TYPE + " = " + AppCompatConstants.MISSED_IMS_TYPE + ")" + " and " + CallLog.Calls.IS_READ + " = 0 ", null,
                CallLog.Calls.DATE + " desc");
        if (cursor == null) {
            return;
        }
        if (cursor.getCount() > 0) {
            updateMissCallNotificationLauncher(context, cursor.getCount());
            cursor.moveToFirst();

            if (TelecomUtil.isDefaultDialer(context)) {
                CallLogNotificationsService.updateMissedCallNotifications(context, cursor.getCount(), cursor.getString(0));
            }
        }
        cursor.close();
    }

    public static final String INTENT_EXTRA_BADGE_COUNT = "badge_count";
    public static final String ACTION_UNREAD_CHANGED = "me.leolin.shortcutbadger.BADGE_COUNT_UPDATE";

    // Anhdts khong hieu sao tren 3 btalk bi nhay vao nhanh ung dung thu 3
    private static final String ACTION_UNREAD_CHANGED_O = "android.intent.action.BADGE_COUNT_UPDATE";
    public static final String INTENT_EXTRA_PACKAGENAME = "badge_count_package_name";
    public static final String INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name";

    /**
     * Anhdts cap nhat so cuoc goi nho
     */
    private void updateMissCallNotificationLauncher(Context context, int count) {
        Intent intentUnread = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intentUnread.setAction(ACTION_UNREAD_CHANGED_O);
            intentUnread.putExtra(INTENT_EXTRA_BADGE_COUNT, count);
            intentUnread.putExtra(INTENT_EXTRA_PACKAGENAME, "bkav.android.btalk");
            intentUnread.putExtra(INTENT_EXTRA_ACTIVITY_NAME, "bkav.android.btalk.activities.BtalkActivity");
            intentUnread.setClassName("bkav.android.launcher3", "com.android.launcher3.bkav.BkavUnreadReceive");
        } else {
            intentUnread.setAction(ACTION_UNREAD_CHANGED);
            intentUnread.putExtra(INTENT_EXTRA_BADGE_COUNT, count);
            intentUnread.putExtra(INTENT_EXTRA_PACKAGENAME, "bkav.android.btalk");
            intentUnread.putExtra(INTENT_EXTRA_ACTIVITY_NAME, "bkav.android.btalk.activities.BtalkActivity");
            intentUnread.setClassName("bkav.android.launcher3",
                    "com.android.launcher3.bkav.BkavUnreadReceive");
        }
        context.sendBroadcast(intentUnread);
    }
}
