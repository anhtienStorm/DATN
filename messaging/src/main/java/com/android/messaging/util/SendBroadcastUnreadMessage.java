package com.android.messaging.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.DatabaseWrapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by quangnd on 12/07/2017.
 * class ban intent cho launcher ve so tin nhan chua doc nen icon
 */

public class SendBroadcastUnreadMessage {

    public static final String INTENT_EXTRA_BADGE_COUNT = "badge_count";
    public static final String ACTION_UNREAD_CHANGED = "me.leolin.shortcutbadger.BADGE_COUNT_UPDATE";

    // Anhdts khong hieu sao tren 3 btalk bi nhay vao nhanh ung dung thu 3
    private static final String ACTION_UNREAD_CHANGED_O = "android.intent.action.BADGE_COUNT_UPDATE";
    public static final String INTENT_EXTRA_PACKAGENAME = "badge_count_package_name";
    public static final String INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name";

    private static final String TAG = "SendBroadcastUnreadMess";

    public static void sendBroadCast(Context context) {
        int unread = getUnreadMessage();
        Intent intentUnread = new Intent ();
        intentUnread.setAction( ACTION_UNREAD_CHANGED );
        intentUnread.putExtra( INTENT_EXTRA_BADGE_COUNT, unread);
        intentUnread.putExtra(INTENT_EXTRA_ACTIVITY_NAME, new ComponentName("bkav.android.btalk", "bkav.android.btalk.activities.BtalkMessageActivity"));
        context.sendBroadcast( intentUnread );
    }

    public static int getUnreadMessage() {
        Cursor cursor = null;
        try {
            Uri uri = Telephony.Sms.Inbox.CONTENT_URI;
            cursor = Factory.get().getApplicationContext().getContentResolver().query(uri, null, Telephony.Sms.Inbox.READ + " = 0", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                return cursor.getCount();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    public static void sendLocalBroadCast(Context context, DatabaseWrapper db) {
        // Anhdts neu Btalk khong phai app mac dinh thi khong hien thong bao goi nho
        int unread = context.getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(context)) ?
                getLocalUnreadMessage(context, db) : 0;
        // Bkav HienDTk: fix bug - BOS-2782 - Start
        // Bkav HienDTk: neu tat notification dot thi unread = 0 => de khong hien thi dau cham thong bao
        if(!isShowNotificationDot(context)){
            unread = 0;
        }
        // Bkav HienDTk: fix bug - BOS-2782 - End
        Intent intentUnread = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intentUnread.setAction(ACTION_UNREAD_CHANGED_O);
            intentUnread.putExtra(INTENT_EXTRA_BADGE_COUNT, unread);
            intentUnread.putExtra(INTENT_EXTRA_PACKAGENAME, "bkav.android.btalk");
            intentUnread.putExtra(INTENT_EXTRA_ACTIVITY_NAME, "bkav.android.btalk.activities.BtalkMessageActivity");
            intentUnread.setClassName("bkav.android.launcher3", "com.android.launcher3.bkav.BkavUnreadReceive");
        } else {
            intentUnread.setAction( ACTION_UNREAD_CHANGED );
            intentUnread.putExtra(INTENT_EXTRA_BADGE_COUNT, unread);
            intentUnread.putExtra(INTENT_EXTRA_PACKAGENAME, "bkav.android.btalk");
            intentUnread.putExtra(INTENT_EXTRA_ACTIVITY_NAME, "bkav.android.btalk.activities.BtalkMessageActivity");
            intentUnread.setClassName("bkav.android.launcher3", "com.android.launcher3.bkav.BkavUnreadReceive");
        }
        context.sendBroadcast(intentUnread);
    }

    // Bkav HienDTk: fix bug - BOS-2782 - Start
    // Bkav HienDTk: doc gia tri bat/tat trong notification dot
    public static boolean isShowNotificationDot(Context context){
        boolean notificationDot  = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = notificationManager.getNotificationChannel(context.getString(R.string.message_notification_chanel_id));

            if (mChannel!=null) {
                notificationDot = mChannel.canShowBadge();
            }
        }
        return notificationDot;
    }
    // Bkav HienDTk: fix bug - BOS-2782 - End

    public static int getLocalUnreadMessage(Context context, DatabaseWrapper db) {
//        String sql = "select * from messages where read = 0 and message_status <> 3";// tin nhan chua doc va khac draf
        //Bkav QuangNDb sua lai cau query de khong lay tin nhan luu tru lam tin nhan chua doc nua
        String sql = "select _id,conversation_id from messages where read = 0 and message_status <> 3 and conversation_id not in (SELECT _id from conversations where archive_status = 1)";// tin nhan chua doc va khac draf

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                ArrayList<Long> threadIds = new ArrayList<>();
                threadIds.add(cursor.getLong(1));

                while (cursor.moveToNext()) {
                    long threadId = cursor.getLong(1);

                    if (!threadIds.contains(threadId)) {
                        threadIds.add(threadId);
                    }
                }

                StringBuilder threads = new StringBuilder();
                for (long thread : threadIds) {
                    if (threads.length() != 0) {
                        threads.append(",");
                    }
                    threads.append(thread);
                }


                String sqlCheck = "select conversation_id from messages," +
                        "(select latest_message_id from conversations where _id in (" + threads.toString() +
                        ")) where _id = latest_message_id and read = 1";// tin nhan chua doc va khac draf
                Cursor cursorCheck = db.rawQuery(sqlCheck, null);
                int countRemove = 0;

                if (cursorCheck != null && cursorCheck.getCount() > 0) {
                    ArrayList<Long> threadRemove = new ArrayList<>();
                    cursorCheck.moveToFirst();
                    do {
                        threadRemove.add(cursorCheck.getLong(0));
                    } while (cursorCheck.moveToNext());

                    cursor.moveToFirst();
                    StringBuilder idRemove = new StringBuilder();
                    do {
                        if (threadRemove.contains(cursor.getLong(1))) {
                            if (idRemove.length() != 0) {
                                idRemove.append(",");
                            }
                            idRemove.append(cursor.getLong(0));
                            countRemove++;
                        }
                    } while (cursor.moveToNext());
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("read", 1);
                    db.update("messages", contentValues,
                            "_id in (" + idRemove.toString() + ")", null);
                }
                if (cursorCheck != null) {
                    cursorCheck.close();
                }
                return cursor.getCount() - countRemove;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

}
