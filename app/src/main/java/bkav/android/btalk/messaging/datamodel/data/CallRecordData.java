package bkav.android.btalk.messaging.datamodel.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.MessagingContentProvider;

/**
 * Created by quangnd on 24/10/2017.
 */

public class CallRecordData {

    private int mId = -1;
    private long mDate;
    private String mFrom;
    private String mTo;
    private int mSubId;
    private int mType;
    private String mFilePath;

    public CallRecordData(long mDate, String mFrom, String mTo, int mSubId, int mType, String mFilePath) {
        this.mDate = mDate;
        this.mFrom = mFrom;
        this.mTo = mTo;
        this.mSubId = mSubId;
        this.mType = mType;
        this.mFilePath = mFilePath;
    }

    public CallRecordData() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long mDate) {
        this.mDate = mDate;
    }

    public String getFrom() {
        return mFrom;
    }

    public void setFrom(String from) {
        this.mFrom = from;
    }

    public String getTo() {
        return mTo;
    }

    public void setTo(String to) {
        this.mTo = to;
    }

    public int getSubId() {
        return mSubId;
    }

    public void setSubId(int mSubId) {
        this.mSubId = mSubId;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        this.mFilePath = mFilePath;
    }

    //TrungTh insertToMMS entry vao csdl;
    public Uri insertToDb(Context context) {
        ContentValues values = getContentValues();
        Uri uri = context.getContentResolver().insert(getUri(), values);
        if (uri != null) {
            mId = (int) ContentUris.parseId(uri);
        }
        return uri;
    }

    //TrungTh update entry trong csdl
    public int updateToDb(Context context) {
        Uri uri = ContentUris.withAppendedId(getUri(), mId);
        ContentValues values = getContentValues();
        return context.getContentResolver().update(uri, values, null, null);
    }

    /**
     * QuangNDb xoa 1 ban ghi trong db
     */
    public int deleteToDb(Context context) {
        return context.getContentResolver().delete(getUri(), null, null);
    }

    public Uri getUri() {
        return MessagingContentProvider.CALL_RECORD_URI;
    }

    public void initFromCursor(Cursor cursor) {
        mId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CallRecordColumns._ID));
        mDate = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.CallRecordColumns.DATE));
        mFrom = cursor.getString(cursor.getColumnIndex(DatabaseHelper.CallRecordColumns.FROM));
        mTo = cursor.getString(cursor.getColumnIndex(DatabaseHelper.CallRecordColumns.TO));
        mType = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CallRecordColumns.TYPE));
        mSubId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CallRecordColumns.SUB_ID));
        mFilePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.CallRecordColumns.FILE_PATH));
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.CallRecordColumns.DATE, mDate);
        values.put(DatabaseHelper.CallRecordColumns.FROM, mFrom);
        values.put(DatabaseHelper.CallRecordColumns.TO, mTo);
        values.put(DatabaseHelper.CallRecordColumns.TYPE, mType);
        values.put(DatabaseHelper.CallRecordColumns.SUB_ID, mSubId);
        values.put(DatabaseHelper.CallRecordColumns.FILE_PATH, mFilePath);
        return values;
    }
}
