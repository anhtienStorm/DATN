package bkav.android.btalk.bmsblocked;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.contacts.common.util.ContactDisplayUtils;

import java.util.ArrayList;

import bkav.android.btalk.R;

public class BmsUtils {
    public static final String AUTHORITY = "com.bkav.bms.antispam";
    public static final String URI_ANTI_SPAM_PROVIDER = "content://com.bkav.bms.antispam/black_list";
    private static final String DATA_TYPE_BLACK_LIST = "black_list";
    private static final String DATA_TYPE_REPORT_SPAM = "report_spam";
    public static final String VALUE_PHONE_NUMBER = "phone_number";
    public static final String KEY_UNLOCKED = "unlocked";
    public static final String KEY_LOCKED = "locked";
    public static final String BMS_BPHONE_PACKAGE_NAME = "com.bkav.bphone.bms";
    public static final String BMS_BPHONE_MAIN_ACTIVITY = "com.bkav.ui.activity.BMSActivity";
    private static final String COLUMN_PHONE_FORMAT = "formattednumber";
    public static final String BMS_BPHONE_EXTRA_FROM_BTALK = "bms_bphone_extra_from_btalk";
    public static final String BMS_BPHONE_VALUE_EXTRA_FROM_BTALK = "open_black_list_antispam";

    static final String URL_BLACK_LIST = "content://" + AUTHORITY + "/" + DATA_TYPE_BLACK_LIST;
    static final Uri CONTENT_URI_BLACK_LIST = Uri.parse(URL_BLACK_LIST);
    static final String URL_REPORT_SPAM = "content://" + AUTHORITY + "/" + DATA_TYPE_REPORT_SPAM;
    static final Uri CONTENT_URI_REPORT_SPAM = Uri.parse(URL_REPORT_SPAM);
    private Cursor cursor;

    // Bkav HuyNQN intent goi den giao dien chan sdt cua bms
    public static Intent intentBlackListBms(Context context){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setClassName(BMS_BPHONE_PACKAGE_NAME,BMS_BPHONE_MAIN_ACTIVITY);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BMS_BPHONE_EXTRA_FROM_BTALK, BMS_BPHONE_VALUE_EXTRA_FROM_BTALK);
        return intent;
    }

    // Bkav HuyNQN lay ra nhung so dang bi chan tu db cua BMS
    public static ArrayList<String> getAllCallsBlocks(Context context){
        ArrayList<String> calls = new ArrayList<>();
        String[] projection = new String[]{VALUE_PHONE_NUMBER};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse(URI_ANTI_SPAM_PROVIDER), null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int col = cursor.getColumnIndex(projection[0]);
                String number = cursor.getString(col);
                calls.add(number);
            }
        }
        cursor.close();
        return calls;
    }

    // Bkav HuyNQN them so dien thoai can chan vao db cua bms
    public static void insertNumberBlock(Context context, String number){
        ContentValues values = new ContentValues();
        values.put(VALUE_PHONE_NUMBER,number);
        context.getContentResolver().insert(Uri.parse(URI_ANTI_SPAM_PROVIDER),values);
    }

    // Bkav HuyNQN them so dien thoai spam vao db cua bms
    public static void insertNumberSpam(Context context, String number){
        ContentValues values = new ContentValues();
        values.put(VALUE_PHONE_NUMBER,number);
        context.getContentResolver().insert(CONTENT_URI_REPORT_SPAM,values);
    }

    // Bkav HuyNQN xoa khoi danh sach chan cua BMS
    public static void deleteNumberBlock(Context context, String number){
        context.getContentResolver().delete(Uri.parse(URI_ANTI_SPAM_PROVIDER),
                VALUE_PHONE_NUMBER + " LIKE ?",
                new String[]{number});
    }

    // Bkav HuyNQN dialog thong bao chan so
    public static void showDialogAddCallLogBlocked(Context context, String numberBlock){
        String message = context.getString(com.android.dialer.R.string.block_number_confirmation_message_new_filtering);
        CharSequence title = ContactDisplayUtils.getTtsSpannedPhoneNumber(context.getResources(),
                com.android.dialer.R.string.block_number_confirmation_title,
                numberBlock);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(context.getString(R.string.text_block), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                BmsUtils.insertNumberBlock(context, numberBlock);
                Intent intent = new Intent();
                intent.setAction(KEY_LOCKED);
                context.sendBroadcast(intent);
            }
        });
        builder.setNegativeButton(context.getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Bkav HuyNQN dialog thong bao chan so
    public static void showDialogUnblocked(Context context, String numberBlock){
        CharSequence message = ContactDisplayUtils.getTtsSpannedPhoneNumber(context.getResources(),
                com.android.dialer.R.string.unblock_number_confirmation_title,
                numberBlock);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);

        builder.setPositiveButton(context.getString(R.string.text_unblock), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                BmsUtils.deleteNumberBlock(context, numberBlock);
                Intent intent = new Intent();
                intent.setAction(KEY_UNLOCKED);
                context.sendBroadcast(intent);
            }
        });
        builder.setNegativeButton(context.getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Bkav HuyNQN kiem tra xem so co bi block hay khong
    public static boolean isHasNumberBlocks(Context context, String numberPhone){
        Cursor cursor;
        ContentResolver contentResolver = context.getContentResolver();
        cursor = contentResolver.query(Uri.parse(URI_ANTI_SPAM_PROVIDER),
                null,
                COLUMN_PHONE_FORMAT + " LIKE ?",
                new String[]{numberPhone},
                null);

        if (cursor != null && cursor.getCount() > 0) {
            return true;
        }
        return false;
    }
}
