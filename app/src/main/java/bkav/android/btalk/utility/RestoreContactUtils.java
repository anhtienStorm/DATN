package bkav.android.btalk.utility;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;

import com.android.dialer.database.DialerDatabaseHelper;
import com.android.messaging.Factory;

import java.util.ArrayList;
import java.util.List;

import static com.android.dialer.database.DialerDatabaseHelper.Tables.SMARTDIAL_TABLE;

public class RestoreContactUtils {

    private static volatile RestoreContactUtils sInstance;

    public static RestoreContactUtils get() {
        if (sInstance == null) {
            sInstance = new RestoreContactUtils();
        }
        return sInstance;
    }

    /**Bkav QuangNDb restore contact from smart dialer database*/
    public void restoreContactFromSmartDialer() {
        //Bkav QuangNDb neu vao che do trai nghiem thi khong tu them contact nua
        if (Config.isTrialMode(Factory.get().getApplicationContext())) {
            return;
        }
        Context context = Factory.get().getApplicationContext();
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        List<String> contactNameList = new ArrayList<>();
        if (cur != null && cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                contactNameList.add(name);
            }
            cur.close();
        }
        DialerDatabaseHelper dialerDatabaseHelper = DialerDatabaseHelper.getInstance(context);
        final SQLiteDatabase db = dialerDatabaseHelper.getReadableDatabase();
        Cursor cursor = db.query(SMARTDIAL_TABLE, null, null, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(cursor.getColumnIndex("display_name"));
                if (displayName.startsWith("?DATE:")) {
                    continue;
                }
                String phoneNumber = cursor.getString(cursor.getColumnIndex("phone_number"));
                if (!contactNameList.contains(displayName)) {
                    //Bkav QuangNDb restore
                    addPhoneContact(context, displayName, phoneNumber);
                }
            }
            cursor.close();
        }
    }

    //Bkav QuangNDb add phone
    private void addPhoneContact(Context context, String displayName, String mobileNumber) {
        if (contactExists(context, mobileNumber))
            return;
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
        // Bkav QuangNDb Add phone number
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Bkav QuangNDb Check contact exist
     */
    private boolean contactExists(Context context, String number) {
        if (number != null) {
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(number));
            String[] mPhoneNumberProjection = {
                    ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER,
                    ContactsContract.PhoneLookup.DISPLAY_NAME
            };
            try (Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null,
                    null)) {
                if (cur != null && cur.moveToFirst()) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }
}
