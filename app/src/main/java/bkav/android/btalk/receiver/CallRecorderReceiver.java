package bkav.android.btalk.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.MessagingContentProvider;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.datamodel.action.InsertCallRecordAction;
import bkav.android.btalk.messaging.util.BtalkPermissionUtil;

import static android.provider.Telephony.BaseMmsColumns.MESSAGE_BOX_INBOX;
import static android.provider.Telephony.BaseMmsColumns.MESSAGE_BOX_SENT;
import static android.provider.Telephony.BaseMmsColumns.THREAD_ID;

/**
 * Created by quangnd on 29/07/2017.
 * Receiver xu ly sau khi ghi am cuoc goi xong thi luu lai
 */

public class CallRecorderReceiver extends BroadcastReceiver {

    private static final int FROM = 137;
    private static final int TO = 151;
    public static final String FROM_TEL = "from_tel";
    public static final String TO_TEL = "to_tel";
    public static final String PATH = "rec_path";
    public static final String TYPE = "type_call"; // 1 la ghi am cuoc goi den, 2 la ghi am cuoc goi di
    public static final String SUB_ID = "sub_id";
    public static final String DATE = "date";
    private static final int INCOMING = 1;
    private static final int OUTGOING = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        String from = intent.getStringExtra(FROM_TEL);
        String to = intent.getStringExtra(TO_TEL);
        String path = intent.getStringExtra(PATH);
        int type = intent.getIntExtra(TYPE, 1);
        int subId = intent.getIntExtra(SUB_ID, -1);
        long date = intent.getLongExtra(DATE, -1);

        if (BtalkPermissionUtil.hasStoragePermissions(context)) {//Bkav QuangNDb neu co quyen thi ghi vao mms luon
            if(checkThreadId(new String[]{to},context)){// Bkav HuyNQN Kiem tra xem lien he da tung nhan tin sms chua
                insertToMMS(context, new String[]{to}, from, path, type, subId, date);
            }
        } else/* if (BtalkUtils.loadBooleanPreferences(context, ALLOW_CACHE_CALL_RECORD, true))*/ {//Bkav QuangNDb khong co quyen thi ghi tam vao csdl
            //TrungTH Cu khong co quyen thi luu cuoc goi nay lai de ghi khi co quyen
            long now = System.currentTimeMillis();
            InsertCallRecordAction.insertCallRecord(now / 1000L, from, to, type, subId, path);
        }
//        deleteFile(path, context);
    }

    private static final String TAG = "CallRecorderReceiver";

    private void deleteFile(String path, Context context) {
        try {
            File file = new File(path);
            file.delete();
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA
                    + " like '" + path + "'", null);
        } catch (Exception e) {
            Log.e(TAG, "deleteFile: ", e);
        }
    }

    public Uri insertToMMS(Context context, String[] to, String from, String path, int type, int sudId, long date) {
        try {
            File recordFile = new File(path);
            if (!recordFile.exists()) {
                return null;
            }
            // Neu file nho qua thi bo qua k insertToMMS vao tin nhan <10kb thi bo qua
            long size = recordFile.length() / 1024;//kb
            if (size < 10) {
                return null;
            }
            byte[] rawData = new byte[(int) recordFile.length()];
            DataInputStream input = null;
            try {
                input = new DataInputStream(new FileInputStream(recordFile));
                input.read(rawData);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            Uri destUri = Uri.parse("content://mms");
            long thread_id = getOrCreateThreadId(context, to);
            // Create a new message entry
            long now = System.currentTimeMillis();
            ContentValues mmsValues = new ContentValues();
            mmsValues.put(THREAD_ID, thread_id);
            mmsValues.put("date", date == -1 ? now / 1000L : date);
            mmsValues.put("msg_box", type == OUTGOING ? MESSAGE_BOX_SENT : MESSAGE_BOX_INBOX);
            //mmsValues.put("m_id", System.currentTimeMillis());
            mmsValues.put("read", type == OUTGOING ? 1 : 1);
            mmsValues.put("seen", type == OUTGOING ? 1 : 1);
            mmsValues.put("sub", "");
            mmsValues.put("sub_cs", 106);
            mmsValues.put("ct_t", "application/vnd.wap.multipart.related");
            mmsValues.put("exp", rawData.length);
            mmsValues.put("m_cls", "personal");
            mmsValues.put("m_type", 132); // 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
            mmsValues.put("v", 19);
            mmsValues.put("pri", 129);
            mmsValues.put("tr_id", "T" + Long.toHexString(now));
            mmsValues.put("sub_id", sudId);
            mmsValues.put("st", 0);
            mmsValues.put("resp_st", 0);
            mmsValues.put("retr_st", 0);

            // Insert message
            Uri res = context.getContentResolver().insert(destUri, mmsValues);
            String messageId = res.getLastPathSegment().trim();

            // Create part media
            //Bkav QuangNDb them path de luu luon path vao mms
            createPartMedia(context, messageId, rawData, path);
            // Create part text
            createPartText(context, messageId, type);

            // Create from address
            createFromAddr(context, messageId, from);

            // Create to addresses
            for (String addr : to) {
                createToAddr(context, messageId, addr);
            }
            Toast.makeText(context, R.string.notify_recoding_call, Toast.LENGTH_SHORT).show();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Uri createPartText(Context context, String messageId, int type) {
        ContentValues mmsPartValue = new ContentValues();
        mmsPartValue.put("mid", messageId);
        mmsPartValue.put("ct", "text/plain");
        mmsPartValue.put("text", type == INCOMING ? context.getString(R.string.record_incoming_call) : context.getString(R.string.record_outgoing_call));
        mmsPartValue.put("cid", "<" + System.currentTimeMillis() + ">");
        Uri partUri = Uri.parse("content://mms/" + messageId + "/part");
        return context.getContentResolver().insert(partUri, mmsPartValue);
    }

    private Uri createPartMedia(Context context, String id, byte[] imageBytes, String path) throws Exception {
        ContentValues mmsPartValue = new ContentValues();
        mmsPartValue.put("mid", id);
        mmsPartValue.put("ct", "audio/amr");
        //Bkav QuangNDb luu path vao cid
        mmsPartValue.put("cid", path);
        Uri partUri = Uri.parse("content://mms/" + id + "/part");
        Uri res = context.getContentResolver().insert(partUri, mmsPartValue);
        // Add data to part
        OutputStream os = null;
        if (res != null) {
            os = context.getContentResolver().openOutputStream(res);
        }
        ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
        byte[] buffer = new byte[256];
        for (int len = 0; (len = is.read(buffer)) != -1; ) {
            if (os != null) {
                os.write(buffer, 0, len);
            }
        }
        if (os != null) {
            os.close();
        }
        is.close();

        return res;
    }

    private Uri createToAddr(Context context, String id, String addr) throws Exception {
        ContentValues addrValues = new ContentValues();
        addrValues.put("address", addr);
        addrValues.put("charset", "106");
        addrValues.put("type", TO); // TO
        Uri addrUri = Uri.parse("content://mms/" + id + "/addr");
        return context.getContentResolver().insert(addrUri, addrValues);
    }

    private Uri createFromAddr(Context context, String id, String addr) throws Exception {
        ContentValues addrValues = new ContentValues();
        addrValues.put("address", addr);
        addrValues.put("charset", "106");
        addrValues.put("type", FROM); // TO
        Uri addrUri = Uri.parse("content://mms/" + id + "/addr");
        return context.getContentResolver().insert(addrUri, addrValues);
    }

    private long getOrCreateThreadId(Context context, String[] numbers) {
        HashSet<String> recipients = new HashSet<String>();
        recipients.addAll(Arrays.asList(numbers));
        return Telephony.Threads.getOrCreateThreadId(context, recipients);
    }

    // Bkav HuyNQN Kiem tra xem lien lac da tung nhan tin hay chua
    private boolean checkThreadId(String[] to,Context context) {
        if(!BtalkPermissionUtil.hasPermissionReadSMS(context)){ // Bkav HuyNQN kiem tra xem duoc cap quyen read sms hay chua
            return false;
        }
        long thread_id = getOrCreateThreadId(context, to);
        Cursor cursor = null;
        try {
            String where = DatabaseHelper.ConversationColumns.SMS_THREAD_ID + " = " + thread_id;
            cursor = Factory.get().getApplicationContext().getContentResolver().query(MessagingContentProvider.CONVERSATIONS_URI_ALL, null, where, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            } else {
                return false;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
