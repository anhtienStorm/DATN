package bkav.android.btalk.backup;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Luu tru du lieu su dung mang cua tung ung dung
 *
 * @author Nghiem Dinh Mung
 *
 */
public class BackupSmsDb extends SQLiteOpenHelper {
    /** ten co so du lieu */
    private static final String DATABASE_NAME = "backup_sms_check_dup.db";

    /** ten bang */
    private static final String TABLE_NAME = "sms";

    /** database version */
    private static final int DATABASE_VERSION = 1;

    /** danh sach cot */
    public static final String[] SMS_COLS = {BaseColumns._ID, "date", "address"};

    private static BackupSmsDb helper;

    private static SQLiteDatabase mDb;

    /**
     * Get Database instance
     *
     * @param context
     * @return
     */
    public static synchronized BackupSmsDb getInstance(Context context) {
        if (helper == null) {
            helper = new BackupSmsDb(context);

        }

        return helper;
    }

    /**
     * Ham khoi tao
     *
     * @param context
     */
    private BackupSmsDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Khoi tao db
     */
    @SuppressLint("NewApi")
    public int createDB(Context context) {
        int numberSms = 0;
        Cursor smss = getMessages(context);
        if (smss != null) {
            numberSms = smss.getCount();
            if (numberSms > 0) {
                smss.moveToFirst();
                for (int i = 0; i < numberSms; i++) {
                    insertSession(smss.getLong(2), smss.getString(0));
                    if (!smss.isLast()) {
                        smss.moveToNext();
                    }

                }
            }
            smss.close();
        }
        return numberSms;

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {

            String createSessionTable = "create table if not exists "
                    + TABLE_NAME + " (" + BaseColumns._ID
                    + " integer primary key autoincrement,"
                    + SMS_COLS[1] + " long  , "
                    + SMS_COLS[2] + " varchar(100)); " +
                    " CREATE INDEX my_idex ON "
                    + TABLE_NAME + " ( " + SMS_COLS[1] + ")";
            sqLiteDatabase.execSQL(createSessionTable);
        } catch (Exception e) {
            // Error when creating database
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion,
                          int newVersion) {
        try {
            sqLiteDatabase.execSQL("drop table " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        } catch (Exception e) {
            // Error when upgrade database
        }

    }

    // Xoa co so du lieu
    public void deleteAll() {
        this.deleteRow(TABLE_NAME, null, null);
    }

    public int deleteRow(String table, String clause, String[] params) {
        SQLiteDatabase database = this.getWritableDatabase();
        int count = 0;
        try {
            count = database.delete(table, clause, params);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.close();
        }
        return count;
    }

    @Override
    protected void finalize() throws Throwable {
        if (helper != null)
            helper.close();
        super.finalize();
    }

    /**
     * Opnen database
     *
     * @return
     */
    private static boolean openDatabase() {
        try {
            if ((mDb == null) || ((mDb != null) && (!mDb.isOpen()))) {
                mDb = helper.getWritableDatabase();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Them mot ung dung moi vao co do du lieu
     *
     * @return
     */
    public boolean insertSession(long date, String address) {
        openDatabase();
        try {
            final ContentValues contentValues = new ContentValues();

            contentValues.put(SMS_COLS[1], date);
            contentValues.put(SMS_COLS[2], address);

            mDb.insertOrThrow(TABLE_NAME, null, contentValues);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Kiem tra su ton tai cua ung dung
     *
     * @return
     */
    public boolean checkSessionExist(long date, String address) {
        openDatabase();
        boolean result = false;
        try {
            Cursor cursor = mDb.query(TABLE_NAME, new String[]{"*"},
                    "date = ? and address = ? ", new String[]{date + "",
                            address}, null, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    result = true;

                }
                cursor.close();

            }

        } catch (Exception e) {

        }
        return result;

    }

    /**
     * Gets the messages từ content provider
     * @param context
     * @return
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
