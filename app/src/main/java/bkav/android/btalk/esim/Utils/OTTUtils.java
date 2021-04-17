package bkav.android.btalk.esim.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.android.messaging.Factory;

import static com.android.dialer.DialerApplication.getContext;

public class OTTUtils {
    private static volatile OTTUtils sInstance;

    public static OTTUtils get() {
        if (sInstance == null) {
            sInstance = new OTTUtils();
        }
        return sInstance;
    }
    private final static String MINE_TYPE_CALL_VIBER = "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_call";
    private final static String VIBER_PACKAGE_NAME = "com.viber.voip";
    private final static String ZALO_PACKAGE_NAME = "com.zing.zalo";
    private final static String MINE_TYPE_CALL_ZALO = "vnd.android.cursor.item/com.zing.zalo.call";

    private String getContactIdFromPhoneNumber(String phone) {
        // Bkav HuyNQN fix bug BOS-4535 start: fix loi uri do sdt null hoac ko chua ki tu
        if(TextUtils.isEmpty(phone)){
            return null;
        }
        // Bkav HuyNQN fix bug BOS-4535 end
        final Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        final ContentResolver contentResolver = getContext().getContentResolver();
        final Cursor phoneQueryCursor = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);
        if (phoneQueryCursor != null) {
            if (phoneQueryCursor.moveToFirst()) {
                String result = phoneQueryCursor.getString(phoneQueryCursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
                phoneQueryCursor.close();
                return result;
            }
            phoneQueryCursor.close();
        }
        return null;
    }

    private String getContactMimeTypeDataId(Context context, String contactId, String mimeType) {
        if (TextUtils.isEmpty(mimeType))
            return null;
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.Data._ID}, ContactsContract.Contacts.Data.MIMETYPE + "= ? AND "
                + ContactsContract.Data.CONTACT_ID + "= ?", new String[]{mimeType, contactId}, null);
        if (cursor == null)
            return null;
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        String result = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
        cursor.close();
        return result;
    }

    public void callFreeWithViber(String phone) {
        Intent intent;
        final String contactId = getContactIdFromPhoneNumber(phone);
        final String contactMimeTypeDataId = getContactMimeTypeDataId(getContext(), contactId, MINE_TYPE_CALL_VIBER);
        if (contactMimeTypeDataId != null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.contacts/data/" + contactMimeTypeDataId));
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            intent.setPackage(VIBER_PACKAGE_NAME);
        } else {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + Uri.encode(phone)));
            intent.setClassName(VIBER_PACKAGE_NAME, "com.viber.voip.WelcomeActivity");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Factory.get().getApplicationContext().startActivity(intent);
    }

    public void callFreeWithZalo(String phone) {
        Intent intent;
        final String contactId = getContactIdFromPhoneNumber(phone);
        final String contactMimeTypeDataId = getContactMimeTypeDataId(getContext(), contactId, MINE_TYPE_CALL_ZALO);
        if (contactMimeTypeDataId != null) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            intent.setDataAndType(Uri.parse("content://com.android.contacts/data/" + contactMimeTypeDataId), MINE_TYPE_CALL_ZALO);
        }else {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + Uri.encode(phone)));
            intent.setClassName(ZALO_PACKAGE_NAME, "com.viber.voip.WelcomeActivity");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Factory.get().getApplicationContext().startActivity(intent);
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getContext().getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public boolean isAddProfileViber(String phone){
        return isNetworkConnected() && appInstalledOrNot(VIBER_PACKAGE_NAME) && isCallFreeWithOTT(MINE_TYPE_CALL_VIBER, phone);
    }

    private boolean isCallFreeWithOTT(String mineType, String phone){
        String contactId = getContactIdFromPhoneNumber(phone);
        if(contactId == null){
            return false;
        }
        String contactMimeTypeDataId = getContactMimeTypeDataId(getContext(), contactId, mineType);
        if(contactMimeTypeDataId != null){
            return true;
        }else {
            return false;
        }
    }

    public boolean isAddProfileZalo(String phone){
        return isNetworkConnected() && appInstalledOrNot(ZALO_PACKAGE_NAME) &&  isCallFreeWithOTT(MINE_TYPE_CALL_ZALO,phone);
    }
}
