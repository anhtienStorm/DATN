package bkav.android.btalk.backup;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Base64;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Class thuc hien viec backup du lieu
 *
 * @author Anhdts
 *
 */
class BkavBackupData {

    private Context mContext;

    BkavBackupData(Context context) {
        mContext = context;
    }

    /**
     * Thuc hien viec backup
     */
    void doBackupData() {
        AppUtils.writeLog("BkavBackupDataThreadNew:backupSMS", mContext.getPackageName());
        backupSMS();
        AppUtils.writeLog("BkavBackupDataThreadNew:backupCallog", mContext.getPackageName());
        backupCallLog();
    }

    /**
     * Thuc hien sao luu du lieu tin nhan
     * @return true Thanh cong
     *         false Loi
     */
    private boolean backupSMS() {
        // Tao file XML de luu tru tin nhan
        File smsBackup = new File(mContext.getFilesDir(),
                BackupRestoreConstants.FILE_NAME_SMS);

        if (smsBackup.exists()) {
            smsBackup.delete();
        }

        //Get ta ca tin nhan
        Cursor cursorDat = getMessages(mContext);

        //Tong do tin nhan
        int totalCountNumber = 0;

        if (cursorDat == null || (cursorDat.getCount() == 0)) {
            /* Neu khong get dk tin nhan thi return error */
            return false;
        } else {
            /* Nguoc lai thi bat dau doc tin nhan */
            cursorDat.moveToFirst();
            totalCountNumber = cursorDat.getCount();

            if (totalCountNumber < 1) {
                /* Neu so luong tin nhan nho hon 1 return error */
                return false;
            }
        }

        synchronized (BkavBackupManagerApplication.sDataLock) {
            //FileOutputStream de ghi file
            FileOutputStream foutStream = null;

            // XmlSerializer de ghi du lieu XML
            XmlSerializer xmlSerial = null;
            String tmpStr = "";
            Long tmpLong;
            Integer tmpInt;

            try {
                smsBackup.createNewFile();
                foutStream = new FileOutputStream(smsBackup);
                xmlSerial = Xml.newSerializer();

                // UTF-8 encoding
                xmlSerial.setOutput(foutStream, "UTF-8");
                xmlSerial.startDocument(null, Boolean.TRUE);

                //start a tag called "sms"
                xmlSerial.startTag(null, BackupRestoreConstants.ROOT_TAG);

                //ghi du lieu xML
                for (int i = 0; i < totalCountNumber; i++) {
                    //Start mot tin nhan
                    xmlSerial.startTag(null, BackupRestoreConstants.SMS_TAG);
                    tmpStr = cursorDat.getString(0);

                    if (tmpStr != null) {
                        xmlSerial.startTag(null, BackupRestoreConstants.ADDRESS_TAG);
                        xmlSerial.text(tmpStr);
                        xmlSerial.endTag(null, BackupRestoreConstants.ADDRESS_TAG);
                    } else {
                        xmlSerial.endTag(null, BackupRestoreConstants.SMS_TAG);
                        cursorDat.moveToNext();
                        continue;
                    }

                    tmpLong = cursorDat.getLong(1);
                    if (tmpLong != null) {
                        xmlSerial.startTag(null, BackupRestoreConstants.PERSON_TAG);
                        xmlSerial.text(Long.toString(tmpLong));
                        xmlSerial.endTag(null, BackupRestoreConstants.PERSON_TAG);
                    } else {
                        xmlSerial.endTag(null, BackupRestoreConstants.SMS_TAG);
                        cursorDat.moveToNext();
                        continue;
                    }

                    tmpLong = cursorDat.getLong(2);
                    if (tmpLong != null) {
                        xmlSerial.startTag(null, BackupRestoreConstants.DATE_TAG);
                        xmlSerial.text(Long.toString(tmpLong));
                        xmlSerial.endTag(null, BackupRestoreConstants.DATE_TAG);
                    } else {
                        xmlSerial.endTag(null, BackupRestoreConstants.SMS_TAG);
                        cursorDat.moveToNext();
                        continue;
                    }

                    tmpStr = cursorDat.getString(3);
                    if (tmpStr != null) {
                        xmlSerial.startTag(null, BackupRestoreConstants.BODY_TAG);
                        tmpStr = Base64.encodeToString(tmpStr.getBytes(),
                                Base64.NO_WRAP);
                        xmlSerial.text(tmpStr);
                        xmlSerial.endTag(null, BackupRestoreConstants.BODY_TAG);
                    } else {
                        xmlSerial.endTag(null, BackupRestoreConstants.SMS_TAG);
                        cursorDat.moveToNext();
                        continue;
                    }

                    tmpInt = cursorDat.getInt(4);
                    if (tmpInt != null) {
                        xmlSerial.startTag(null, BackupRestoreConstants.TYPE_TAG);
                        xmlSerial.text(Integer.toString(tmpInt));
                        xmlSerial.endTag(null, BackupRestoreConstants.TYPE_TAG);
                    } else {
                        xmlSerial.endTag(null, BackupRestoreConstants.SMS_TAG);
                        cursorDat.moveToNext();
                        continue;
                    }

                    // end mot tin nhan
                    xmlSerial.endTag(null, BackupRestoreConstants.SMS_TAG);
                    // doc tin nhan moi
                    cursorDat.moveToNext();
                }

                //Dong Cursor
                cursorDat.close();
                //Write end root tag
                xmlSerial.endTag(null, BackupRestoreConstants.ROOT_TAG);
                xmlSerial.endDocument();
                // write xml data into the FileOutputStream
                xmlSerial.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (foutStream != null) {
                    try {
                        foutStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    foutStream = null;
                }
                // Ma hoa file
                BackupRestoreUtils.compressFile(mContext, smsBackup.getAbsolutePath(), null, null);
            }
        }
        return true;
    }

    /**
     * Backup call log.
     * @return true Thanh cong
     *         false Loi
     */
    private boolean backupCallLog() {
        File calllogsBackup = new File(mContext.getFilesDir(),
                BackupRestoreConstants.FILE_NAME_CALLLOG);

        if (calllogsBackup.exists()) {
            calllogsBackup.delete();
        }

        PrintWriter pw = null;
        Cursor cursor = null;
        synchronized (BkavBackupManagerApplication.sDataLock) {
            try {
                // PrintWriter to ghi file
                pw = new PrintWriter(new FileWriter(calllogsBackup));

                // Get call log via provider
                cursor = mContext.getContentResolver().query(
                        android.provider.CallLog.Calls.CONTENT_URI, null, null,
                        null, android.provider.CallLog.Calls.DATE + " DESC");

                // Get thong so cua cuoc goi
                if (cursor != null) {
                    int numberColumn = cursor
                            .getColumnIndex(android.provider.CallLog.Calls.NUMBER);
                    int dateColumn = cursor
                            .getColumnIndex(android.provider.CallLog.Calls.DATE);
                    int typeColumn = cursor
                            .getColumnIndex(android.provider.CallLog.Calls.TYPE);
                    int durationColumn = cursor
                            .getColumnIndex(android.provider.CallLog.Calls.DURATION);
                    int newColumn = cursor
                            .getColumnIndex(android.provider.CallLog.Calls.NEW);
                    int nameColumn = cursor
                            .getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME);
                    int typeNumColumn = cursor
                            .getColumnIndex(android.provider.CallLog.Calls.CACHED_NUMBER_TYPE);
                    int numlabelColumn = cursor
                            .getColumnIndex(android.provider.CallLog.Calls.CACHED_NUMBER_LABEL);

                    // Write to file
                    while (cursor.moveToNext()) {
                        String number = cursor.getString(numberColumn);
                        pw.println(number);

                        long date = cursor.getLong(dateColumn);
                        pw.println(Long.toString(date));

                        int type = cursor.getInt(typeColumn);
                        pw.println(Integer.toString(type));

                        long duration = cursor.getLong(durationColumn);
                        pw.println(Long.toString(duration));

                        int newc = cursor.getInt(newColumn);
                        pw.println(Integer.toString(newc));

                        String name = cursor.getString(nameColumn);
                        pw.println(name);

                        String typeNum = cursor.getString(typeNumColumn);
                        pw.println(typeNum);

                        String numLabel = cursor.getString(numlabelColumn);
                        pw.println(numLabel);
                    }
                }
                pw.flush();
            } catch (IOException e) {
                e.printStackTrace();
                if (calllogsBackup.exists()) {
                    calllogsBackup.delete();
                }
                return false;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (pw != null)
                    pw.close();
                // ma hoa file
                BackupRestoreUtils.compressFile(mContext, calllogsBackup.getAbsolutePath(), null, null);
            }
        }
        return true;
    }

    /**
     * Gets the messages từ content provider
     * @return Cursor
     */
    public Cursor getMessages(Context context) {
        String SORT_ORDER = "date DESC";
        final Uri SMS_CONTENT_URI = Uri.parse("content://sms");

        ContentResolver resolver = context.getContentResolver();
        return resolver.query(SMS_CONTENT_URI,
                new String[]{BackupRestoreConstants.ADDRESS,
                        BackupRestoreConstants.PERSON,
                        BackupRestoreConstants.DATE,
                        BackupRestoreConstants.BODY,
                        BackupRestoreConstants.TYPE},
                null,
                null,
                SORT_ORDER);
    }
}
