package bkav.android.btalk.trial_mode;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;

import java.util.ArrayList;

import bkav.android.btalk.utility.Config;

public class TrialModeController {

    public static final String MODE_TRIAL = "mode_trial";

    private Context mContext;

    private ContentObserver mTrialModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            updateTrialMode();
        }
    };

    public TrialModeController(Context context) {
        mContext = context;
        context.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(MODE_TRIAL), true, mTrialModeObserver);

    }

    private void updateTrialMode() {
        boolean isTrialMode = Config.isTrialMode(mContext);
        if (isTrialMode) {
            TrialModeUtils.updateDataInTrialMode(mContext);
        } else {
            // xoa CSDL trialmode de ve trang thai bt
            TrialModeUtils.updateDataOutTrialMode(mContext);
        }
    }

    //Bkav QuangNDb add phone
    private static void addPhoneContact(Context context, String displayName, String mobileNumber, byte[] imageData) {
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

    //Bkav QuangNDb add phone
    private static void addMailContact(Context context, String displayName, String mail, byte[] imageData) {
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
        // Bkav QuangNDb Add mail number
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, mail)
                .build());
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

    //Bkav QuangNDb add phone
    private static void addTempContact(Context context, String displayName, byte[] imageData) {
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
    /**
     * Bkav QuangNDb Check contact exist
     */
    private static boolean contactExists(Context context, String number) {
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
