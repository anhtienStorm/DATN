package bkav.android.btalk.trial_mode;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteReadOnlyDatabaseException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;

import com.android.contacts.common.compat.CompatUtils;
import com.android.dialer.database.DialerDatabaseHelper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import bkav.android.btalk.BtalkExecutors;
import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.utility.PrefUtils;

import static android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX;
import static android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT;
import static com.android.dialer.database.DialerDatabaseHelper.Tables.SMARTDIAL_TABLE;

public class TrialModeUtils {

    public static final String IS_TRIAL_MODE_PREF_KEY = "is_trial_mode";


    public static void updateDataInTrialMode(Context context) {
        // xoa data cu da ton tai trong may
        TrialModeUtils.deleteDatabaseTrialMode(context);
        // insert CSDL trialmode
        TrialModeUtils.insertDatabaseTrialMode(context);
        PrefUtils.get().saveBooleanPreferences(context, IS_TRIAL_MODE_PREF_KEY, true);
    }

    public static void updateDataOutTrialMode(Context context) {
        // xoa CSDL trialmode de ve trang thai bt
        TrialModeUtils.deleteDatabaseTrialMode(context);
        PrefUtils.get().saveBooleanPreferences(context, IS_TRIAL_MODE_PREF_KEY, false);
    }

    private static void insertDatabaseTrialMode(Context context) {
        BtalkExecutors.runOnBGThread(new Runnable() {
            @Override
            public void run() {
                generateTrialContact(context);
                generateTrialSms(context);
                generateCallLog(context);
            }
        });

    }

    private static void deleteDatabaseTrialMode(Context context) {
        //Bkav QuangNDb xoa du lieu dialer
        DialerDatabaseHelper dialerDatabaseHelper = DialerDatabaseHelper.getInstance(context);
        // Bkav HienDTk: fix loi - BOS-3324 - Start
        // Bkav HienDTk: fix loi SQLiteReadOnlyDatabaseException
        try {
            final SQLiteDatabase db = dialerDatabaseHelper.getWritableDatabase();
            db.delete(SMARTDIAL_TABLE, null, null);
        }catch (SQLiteReadOnlyDatabaseException e){
            e.printStackTrace();
        }
        // Bkav HienDTk: fix loi - BOS-3324 - Start

        BtalkExecutors.runOnBGThread(new Runnable() {
            @Override
            public void run() {
                // xoa toan bo tin nhan, call log, contact
                deleteAllMessage(context);
                deleteAllContact(context);
                deleteAllCallLog(context);
            }
        });
    }

    private static void deleteAllMessage(Context context) {
        Uri inboxUri = Uri.parse("content://sms/");
        Cursor cursor = context.getContentResolver().query(inboxUri, null, null, null, null);
        while (cursor.moveToNext()) {
            // Delete the SMS
            String pid = cursor.getString(0); // Get id;
            String uri = "content://sms/" + pid;
            context.getContentResolver().delete(Uri.parse(uri),
                    null, null);
        }
        cursor.close();
    }

    //Bkav QuangNDb delete all contact in devices
    private static void deleteAllContact(Context context) {
        try (Cursor cur = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)) {
            if (cur != null) {
                while (cur.moveToNext()) {
                    String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                    context.getContentResolver().delete(uri, null, null);
                }
            }
        }
    }

    private static void deleteAllCallLog(Context context) {
        context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, null, null);
    }

    //Bkav QuangNDb generate data contact trial mode
    private static void generateTrialContact(Context context) {
        addPhoneContact(context, CompatUtils.isBCY() ? "Bkav CSKH" : "CSKH", new String[]{"02473050069", "02473050050", "1800545448", "02862966626"}
            , toByteArray(drawableToBitmap(context.getDrawable(R.drawable.bphone))));
    }

    private static void generateTrialSms(Context context) {
        //===========================BKAV SENT========================
        addSms(context, "02473050069", MESSAGE_TYPE_SENT, "Smartphone cao cấp, made in Vietnam");
        //===========================BKAV INBOX========================

        addSms(context, "02473050069", MESSAGE_TYPE_INBOX, CompatUtils.isBCY() ? "Trải nghiệm không giới hạn" : "Bphone – Trải nghiệm không giới hạn");
    }

    private static void generateCallLog(Context context) {
        addCallLog(context, "02473050069", CallLog.Calls.INCOMING_TYPE);
        addCallLog(context, "02473050069", CallLog.Calls.MISSED_TYPE);
        addCallLog(context, "02473050069", CallLog.Calls.OUTGOING_TYPE);

        // Bkav HuyNQN BOS-2304 start, thuc hien gui yeu cau toi launcher de tao so misscall
        Intent intentUnread = new Intent();
        intentUnread.setAction(BtalkActivity.ACTION_UNREAD_CHANGED);
        intentUnread.putExtra(BtalkActivity.INTENT_EXTRA_BADGE_COUNT, 1);
        intentUnread.putExtra(BtalkActivity.INTENT_EXTRA_PACKAGENAME, "bkav.android.btalk");
        intentUnread.putExtra(BtalkActivity.INTENT_EXTRA_ACTIVITY_NAME, "bkav.android.btalk.activities.BtalkActivity");
        intentUnread.setClassName("bkav.android.launcher3",
                "com.android.launcher3.bkav.BkavUnreadReceive");
        context.sendBroadcast(intentUnread);
    }

    private static final String ACTION_UNREAD_CHANGED_O = "android.intent.action.BADGE_COUNT_UPDATE";
    public static final String INTENT_EXTRA_PACKAGENAME = "badge_count_package_name";
    public static final String INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name";
    public static final String INTENT_EXTRA_BADGE_COUNT = "badge_count";
    private static void addSms(Context context, String address, int type, String body) {
        Uri uri = Uri.parse("content://sms/");
        ContentValues values = new ContentValues();
        long now = System.currentTimeMillis();
        values.put("address", address);
        values.put("date", now);
        values.put("read", 0);
        values.put("type", type);
        values.put("body", body);
        context.getContentResolver().insert(uri, values);
        /** This is very important line to solve the problem */
        context.getContentResolver().delete(Uri.parse("content://sms/conversations/-1"), null, null);
        values.clear();

        // Bkav HuyNQN thuc hien gui yeu cau toi launcher de tao so tin nhan chua doc
        Intent intentUnread = new Intent();
        intentUnread.setAction(ACTION_UNREAD_CHANGED_O);
        intentUnread.putExtra(INTENT_EXTRA_BADGE_COUNT, 1);
        intentUnread.putExtra(INTENT_EXTRA_PACKAGENAME, "bkav.android.btalk");
        intentUnread.putExtra(INTENT_EXTRA_ACTIVITY_NAME, "bkav.android.btalk.activities.BtalkMessageActivity");
        intentUnread.setClassName("bkav.android.launcher3", "com.android.launcher3.bkav.BkavUnreadReceive");
        context.sendBroadcast(intentUnread);
        // Bkav HuyNQN BOS-2304 end
    }

    private static void addCallLog(Context context, String address, int type) {
        Uri uri = CallLog.Calls.CONTENT_URI;
        ContentValues values = new ContentValues();
        long now = System.currentTimeMillis();
        values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
        values.put(CallLog.Calls.TYPE, type);
        values.put(CallLog.Calls.DATE, now);
        values.put(CallLog.Calls.DURATION, 50000);
        values.put(CallLog.Calls.NUMBER, address);
        context.getContentResolver().insert(uri, values);
        values.clear();
    }

    //Bkav QuangNDb add phone with multi number
    private static void addPhoneContact(Context context, String displayName, String[] mobileNumbers, byte[] imageData) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        // Bkav QuangNDb Khooi tao
        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());
        // Bkav QuangNDb Add name
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .build());
        for (String newNumber : mobileNumbers) {
            // Bkav QuangNDb Add phone number
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                    .build());
        }
        // Bkav QuangNDb Add photo
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageData)
                .build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Bkav QuangNDb convert bitmap to byte array
    private static byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // TODO: 06/05/2019 de qua 80 se bi crash voi anh JPG
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream); // => Chuyen len 100%
        return stream.toByteArray();
    }

    //Bkav QuangNDb convert drawable to bitmap
    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
