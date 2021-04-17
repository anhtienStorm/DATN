package bkav.android.btalk.utility;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;

import bkav.android.btalk.messaging.ui.searchSms.Contact;

public class ContactUtils {

    private static volatile ContactUtils sInstance;

    private static final int REQUEST_CODE_READ_PHONE_STATE = 101;
    public static final String OTA_CHANNEL = "ota_channel";
    public static final int STABLE_OTA_CHANNEL = 0;
    public static final int ALPHA_OTA_CHANNEL = 2;

    public static ContactUtils get() {
        if (sInstance == null) {
            sInstance = new ContactUtils();
        }
        return sInstance;
    }

    public void addConnectPhone(Context context, String displayName, String... numbers) {
        ArrayList<String> newNumbers = new ArrayList<>();
        boolean isContactExists = false;
        for (String number : numbers) {
            if (contactExists(context, number)) {
                isContactExists = true;
                continue;
            }
            newNumbers.add(number);
        }
        if (newNumbers.isEmpty()) {
            return;
        }
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        if (isContactExists) {
            // TrungTH neu contact da ton tai
            int rawContactId = getRawContactId(context, displayName);
            for (String newNumber : newNumbers) {
                // Bkav QuangNDb Add phone number
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawContactId))
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                        .build());
            }
        } else {
            // Bkav QuangNDb Khooi tao
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());
            // Bkav QuangNDb Add name
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            displayName)
                    .build());
            for (String newNumber : newNumbers) {
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
        }

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
            Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null,
                    null);
            try {
                if (cur != null && cur.moveToFirst()) {
                    return true;
                }
            } finally {
                if (cur != null)
                    cur.close();
            }
            return false;
        } else {
            return false;
        }
    }

    private int getRawContactId(Context context, String displayName) {
        int rawId = 0;
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.Data.RAW_CONTACT_ID};
        String selection = ContactsContract.Data.DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{displayName};
        Cursor c = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);

        if (c != null && c.moveToFirst()) {
            rawId = c.getInt(c.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
            c.close();
        }

        return rawId;
    }

    /**
     * Bkav QuangNDb Check BKAV CSKH 1900545499 contact exist
     */
    private boolean isOldConnectContactExists(Context context, String number, String name) {
        if (number != null) {
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(number));
            String[] mPhoneNumberProjection = {
                    ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER,
                    ContactsContract.PhoneLookup.DISPLAY_NAME
            };
            Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null,
                    null);
            try {
                if (cur != null && cur.moveToNext()) {
                    if (name.equals(cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)))) {
                        return true;
                    }
                }
            } finally {
                if (cur != null)
                    cur.close();
            }
            return false;
        } else {
            return false;
        }
    }

    //Bkav QuangNDb remove old connect phone
    public void removeOldConnectPhone(Context context, String displayName, String number) {
        if (isOldConnectContactExists(context, number, displayName)) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.CommonDataKinds.Phone.NUMBER + " =?", new String[]{number})
                    .build());
            try {
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getImeiBySlot(Context context, int slot, Activity activity) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.READ_PHONE_STATE},REQUEST_CODE_READ_PHONE_STATE);
        }
        return "Imei slot " + slot + ": " + telephonyManager.getImei(slot);
    }

    // Bkav HuyNQN kiem tra xem co phai la ban OTA ALPHA
    public static boolean checkAlphaOTAChannel(Context context) {
        return android.provider.Settings.System.getInt(context.getContentResolver(),
                OTA_CHANNEL, STABLE_OTA_CHANNEL) == ALPHA_OTA_CHANNEL;
    }
}
