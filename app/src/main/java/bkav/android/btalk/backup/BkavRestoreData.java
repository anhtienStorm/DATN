package bkav.android.btalk.backup;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.util.Base64;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import bkav.android.btalk.backup.vcard.VCardConfig;
import bkav.android.btalk.backup.vcard.VCardEntryCommitter;
import bkav.android.btalk.backup.vcard.VCardEntryConstructor;
import bkav.android.btalk.backup.vcard.VCardEntryCounter;
import bkav.android.btalk.backup.vcard.VCardException;
import bkav.android.btalk.backup.vcard.VCardInterpreter;
import bkav.android.btalk.backup.vcard.VCardInterpreterCollection;
import bkav.android.btalk.backup.vcard.VCardNestedException;
import bkav.android.btalk.backup.vcard.VCardNotSupportedException;
import bkav.android.btalk.backup.vcard.VCardParser;
import bkav.android.btalk.backup.vcard.VCardParser_V21;
import bkav.android.btalk.backup.vcard.VCardParser_V30;
import bkav.android.btalk.backup.vcard.VCardSourceDetector;
import bkav.android.btalk.backup.vcard.VCardVersionException;

/**
 * Class thuc hien cac cong viec Restore Calllog, Contact, SMS
 * 
 * @author KhanhVD/BAM
 *
 */
class BkavRestoreData {
    
    private Context mContext;
    public static final int RESTORE_FINISH              = 0;
    public static final int RESTORE_CALLLOG_BEGIN       = 1;
    public static final int RESTORE_CONTACTS_BEGIN      = 2;
    public static final int RESTORE_SMS_FINISH_BEGIN    = 3;    
    
    BkavRestoreData(Context context) {
        mContext = context;
    }
    
    // Thuc hien viec backup
    void doBackupData() {
        restoreCallLog();
        restoreSms();
    }

    /**
     * Restore call log.
     *
     * @return true Thanh cong
     *         false Loi
     */
    private boolean restoreCallLog() {
        AppUtils.writeLog("BkavRestoreData::restoreCallLog::start", mContext.getPackageName());
        File calllogsRestore = new File(mContext.getFilesDir(), 
                BackupRestoreConstants.FILE_NAME_CALLLOG);
        
        // Khong tim thay file
        if (!calllogsRestore.exists()) {
            return false;
        }    
        
        AppUtils.writeLog("BkavRestoreData::restoreCallLog", mContext.getPackageName());

        synchronized (BkavBackupManagerApplication.sDataLock) {
            // Ma ho lai file
            if (BackupRestoreUtils.deCompressFile(mContext, calllogsRestore.getAbsolutePath(), 
                    calllogsRestore.getAbsolutePath(), null) == -1) {
                return false;
            }
    
            try {
                String number = null, name = null, numberType = null, numberLabel = null, line = "";
                long date = 0, duration = 0;
                int newc = 0, type = 0, count = 0;
                BufferedReader in = new BufferedReader(new FileReader(calllogsRestore));
                BufferedReader tmpReader = new BufferedReader(new FileReader(calllogsRestore));
                ContentResolver contentResolver = mContext.getContentResolver();
                ContentValues values = new ContentValues();
    
                tmpReader.close();
    
                while ((line = in.readLine()) != null) {  
                    switch (count % 8) {
                    case 0:
                        if (line.equals("null")) number = null;
                        else number = line;
                        break;
                    case 1:
                        if (line.equals("null")) date = 0;
                        else date = Long.valueOf(line);
                        break;
                    case 2:
                        if (line.equals("null")) type = 0;
                        else type = Integer.valueOf(line);
                        break;
                    case 3:
                        if (line.equals("null")) duration = 0;
                        else duration = Long.valueOf(line);
                        break;
                    case 4:
                        if (line.equals("null")) newc = 0;
                        else newc = Integer.valueOf(line);
                        break;
                    case 5:
                        if (line.equals("null")) name = null;
                        else name = line;
                        break;
                    case 6:
                        if (line.equals("null")) numberType = null;
                        else numberType = line;
                        break;
                    case 7:
                        if (line.equals("null")) numberLabel = null;
                        else numberLabel = line;
                        break;
    
                    default:
                        break;
                    }      
    
                    if ((count % 8) == 7) {
                        Cursor cu = contentResolver.query(CallLog.Calls.CONTENT_URI, null, 
                                CallLog.Calls.NUMBER + "=\"" + number + "\" and " +
                                        CallLog.Calls.DATE + "=\"" + date + "\"", null, null);
    
                        if (cu != null) {
                            if (cu.getCount() == 0) {
    
                                values.clear();
                                values.put(CallLog.Calls.NUMBER, number);
                                values.put(CallLog.Calls.DATE, date);
                                values.put(CallLog.Calls.DURATION, duration);
                                values.put(CallLog.Calls.TYPE, type);
                                values.put(CallLog.Calls.NEW, newc);
                                values.put(CallLog.Calls.CACHED_NAME, name);
                                values.put(CallLog.Calls.CACHED_NUMBER_TYPE, numberType);
                                values.put(CallLog.Calls.CACHED_NUMBER_LABEL, numberLabel);
    
                                contentResolver.insert(CallLog.Calls.CONTENT_URI, values);
                            }
                        }
    
                        if(cu != null){
                            cu.close();
                        }
                    }
                    count++;
                }
    
                calllogsRestore.delete();
                in.close();
            } catch (RuntimeException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    /**
     * Restore from vcard file.
     * @param mUri
     */
    private void restoreFromVcardFile(Uri mUri) {
        
        //Ghet List Email to check email exits
        BackupRestoreUtils.getContactEmailList(mContext);        
        VCardEntryCounter counter = new VCardEntryCounter();
        VCardSourceDetector detector = new VCardSourceDetector();
        VCardInterpreterCollection builderCollection = new VCardInterpreterCollection
                (Arrays.asList(counter, detector));
        try  {
            readOneVCardFile(mUri, VCardConfig.DEFAULT_CHARSET,
                    builderCollection, null, true, null);
        } catch (VCardNestedException e) {
            e.printStackTrace();
            try {
                readOneVCardFile(mUri,VCardConfig.DEFAULT_CHARSET,
                        counter, detector,false, null);

            } catch (VCardNestedException e2)  {
                e2.printStackTrace();
            }
        }

        String charset = detector.getEstimatedCharset();
        ArrayList<String> mErrorFileNameList = new ArrayList<String>();
        doActuallyReadOneVCard(mUri, null, charset, true, detector,
                mErrorFileNameList);
    }
    
    /**
     * Read one v card file.
     *
     * @param uri the uri
     * @param charset the charset
     * @param builder the builder
     * @param detector the detector
     * @param throwNestedException the throw nested exception
     * @param errorFileNameList the error file name list
     * @return true, if successful
     * @throws VCardNestedException the v card nested exception
     */
    private boolean readOneVCardFile(Uri uri, String charset,
                                     VCardInterpreter builder, VCardSourceDetector detector,
                                     boolean throwNestedException, List<String> errorFileNameList)
                    throws VCardNestedException {

        SharedPreferences prefs = 
                PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean mCanceled = false;
        InputStream is = null;
        VCardParser mVCardParser;
        ContentResolver resolver = mContext.getContentResolver();

        try  {
            is = resolver.openInputStream(uri);
            mVCardParser = new VCardParser_V30();
            prefs.edit().putInt(BackupRestoreConstants.VCARD_VERSION_WHEN_RESTORE, 
                    BackupRestoreConstants.vCard30Code).commit();
            try {
                mVCardParser.parse(is, charset, builder, mCanceled);
            } catch (VCardVersionException e1) {
            
                e1.printStackTrace();
                try  {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (builder instanceof VCardEntryConstructor) {
                    // Let the object clean up internal temporal objects,
                    ((VCardEntryConstructor) builder).clear();
                }

                is = resolver.openInputStream(uri);

                try  {
                    mVCardParser = new VCardParser_V21(detector);
                    mVCardParser.parse(is, charset, builder, mCanceled);
                    prefs.edit().putInt(BackupRestoreConstants.VCARD_VERSION_WHEN_RESTORE, 
                            BackupRestoreConstants.vCard21Code).commit();

                } catch (VCardVersionException e2) {

                }

            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }  catch (IOException e) {

            return false;

        }  catch (VCardNotSupportedException e) {
            if ((e instanceof VCardNestedException) && throwNestedException) 
                throw (VCardNestedException) e;

            if (errorFileNameList != null) errorFileNameList.add(uri.toString());

            return false;
        } catch (VCardException e) {
            e.printStackTrace();

            if (errorFileNameList != null)  {
                errorFileNameList.add(uri.toString());
            }

            return false;
        }
        return true;
    }

    /**
     * Do actually read one v card.
     *
     * @param uri the uri
     * @param account the account
     * @param charset the charset
     * @param showEntryParseProgress the show entry parse progress
     * @param detector the detector
     * @param errorFileNameList the error file name list
     * @return the uri
     */
    private Uri doActuallyReadOneVCard(Uri uri,
            android.accounts.Account account, String charset,
            boolean showEntryParseProgress, VCardSourceDetector detector,
            List<String> errorFileNameList) {

        VCardEntryConstructor builder = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        int vcardType = 1;

        if (prefs.getInt(BackupRestoreConstants.VCARD_VERSION_WHEN_RESTORE, 0) == 
                BackupRestoreConstants.vCard21Code) {
            vcardType = VCardConfig.VCARD_TYPE_V21_GENERIC_UTF8;
        } else if(prefs.getInt(BackupRestoreConstants.VCARD_VERSION_WHEN_RESTORE, 0) == 
                BackupRestoreConstants.vCard30Code) {
            vcardType = VCardConfig.VCARD_TYPE_V30_GENERIC_UTF8;
        }

        if (charset != null) {
            builder = new VCardEntryConstructor(charset, charset, false,
                    vcardType, null); // account
        } else {
            charset = VCardConfig.DEFAULT_CHARSET;
            builder = new VCardEntryConstructor(null, null, false, vcardType,
                    null);// mAccount
        }

        ContentResolver resolver = mContext.getContentResolver();
        VCardEntryCommitter committer = new VCardEntryCommitter(resolver);
        builder.addEntryHandler(committer);

        try {
            if (!readOneVCardFile(uri, charset, builder, detector, false, null)) {
                return null;
            } 


        } catch (VCardNestedException e) {
            e.printStackTrace();
        }

        final ArrayList<Uri> createdUris = committer.getCreatedUris();
        
        //the number of contact

        return (createdUris == null || createdUris.size() != 1) ? null
                : createdUris.get(0);
    }
    
    /**
     * Phuong thuc thuc hien phuc hoi SMS.
     *
     * @return true Thanh cong
     *         fale Xay ra loi
     */
    private boolean restoreSms() {
        AppUtils.writeLog("BkavRestoreData::restoreSMS::start", mContext.getPackageName());
     // Tao file XML de luu tru tin nhan
        File smsRestore = new File(mContext.getFilesDir(), 
                BackupRestoreConstants.FILE_NAME_SMS);
        // Khong ton tai file backup
        if (!smsRestore.exists()) {
            return false;
        }
        
        AppUtils.writeLog("BkavRestoreData::restoreSMS", mContext.getPackageName());
        // Uri dành cho máy thường
        final Uri SMS_CONTENT_URI = Uri.parse("content://sms");

        synchronized (BkavBackupManagerApplication.sDataLock) {
            if (BackupRestoreUtils.deCompressFile(mContext, 
                    smsRestore.getAbsolutePath(), 
                    smsRestore.getAbsolutePath(), null) == -1) {
                return false;
            }
    
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp;
    
            try {          
                // Parse XML
                InputStream in = new BufferedInputStream(new FileInputStream(
                        smsRestore));
                sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                SmsItemHandler handler = new SmsItemHandler();
                xr.setContentHandler(handler);
                xr.parse(new InputSource(in));
    
                // Get danh sach tin nhan
                ArrayList<SmsMessage> lstMsg = handler.getMsgs();
    
                // Contentvalue de chen tin nhan
                ContentValues values = new ContentValues();
    
                // Tong so tin nhan can insrert
                int smsCounttmp = lstMsg.size();        
    
                // bien tam luu tru noi dung tin nhan
                String tmpbody = "";
    
                // bien tam luu tru tin nhan
                SmsMessage msg;
    
                // bien tam luu tru thread id
                long thread_id ;
    
                // ContentResolver thuc hien chen tin nhan
                ContentResolver resolver = mContext.getContentResolver();
    
                // Flag check exist
                boolean hasCheckExist = true;
    
                // Xoa Db tin nhan check trung
                BackupSmsDb.getInstance(mContext).deleteAll();
    
                // Khoi tao db tin nhan check trung
                int totalSMS = BackupSmsDb.getInstance(mContext).createDB(mContext);
    
                // Neu khong co tin nhan nao thi khong check trung
                if ( totalSMS == 0){
                    hasCheckExist = false;
                }
    
                // Vong lap thuc hien chen tin nhan
                for (int i = (smsCounttmp - 1); i >= 0; i--) {               
                    try {
                        // get tin nhan va giai ma noi dung
                        msg = lstMsg.get(i);
                        tmpbody = msg.getBody().toString();
                        tmpbody = new String(Base64.decode(tmpbody.getBytes(),
                                Base64.NO_WRAP));
    
                        // Check trung sms
                        if (hasCheckExist){
                            if (BackupSmsDb.getInstance(mContext)
                                    .checkSessionExist(msg.getDate(), msg.getAddress())){
                                continue;
                            }
                        }
    
                        // get Thread id
                        thread_id = getThreadId(msg.getAddress(), mContext);
    
                        // clear content value
                        values.clear();
    
                        // chen du lieu
                        if (thread_id > 0) {
                            values.put("thread_id", thread_id);
                        } else {
                            values.put("thread_id", 0);
                        }
                        values.put("address", msg.getAddress());
                        values.put("body", tmpbody);
                        values.put("date", msg.getDate());
                        values.put("person", msg.getPerson());
                        values.put("type", msg.getType());
                        values.put("read", true);
    
                        // Chèn tin nhắn
                        resolver.insert(SMS_CONTENT_URI, values);
                    } catch (Exception e) {
                        
                    }
                }
                smsRestore.delete();          
                resolver.delete(Uri.parse("content://sms/conversations/-1"), null, null);
                return true;
    
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }
    
    /** HashMap luu tru threadid cua tin nhan */
    HashMap<String, Long> threadIdMap = new HashMap <String, Long>();
    
    /**
     * Ham get thread id cua tin nhan theo so dien thoai
     * @param phone So dien thoai
     * @param context context
     * @return thread id cua tin nhan
     */
    private long getThreadId(String phone, Context context){
        long id = -1;
        
        // Neu hasmap null khoi tao lai
        if (threadIdMap == null)
            threadIdMap = new HashMap <String, Long>();
        // Kiem tra xem da co thread id cua so dien thoai tuong ung chua
        // neu co thi return gia tri
        if(threadIdMap.containsKey(phone)){
            id = threadIdMap.get(phone);
        }
        // Neu chua co thi query vao db de get thread id
        else{
            id = BackupRestoreUtils.getSMSThreadId(context, phone);
            if (id != -1){
                threadIdMap.put(phone, id);
            }
        }       
        return id;      
    }   
}
