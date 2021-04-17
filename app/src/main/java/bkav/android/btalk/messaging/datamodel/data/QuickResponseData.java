package bkav.android.btalk.messaging.datamodel.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.MessagingContentProvider;

import java.io.Serializable;

/**
 * Created by quangnd on 29/11/2017.
 * Du lieu quick reponse lay tu db
 */

public class QuickResponseData implements Serializable{
    private int mId = -1;
    private String mResponse;
    private boolean mIsDefault;

    public static final String KEY_TRANSFER = "quick_response_data";

    public QuickResponseData(String response, boolean isDefault) {
        this.mResponse = response;
        this.mIsDefault = isDefault;
    }

    public QuickResponseData() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getResponse() {
        return mResponse;
    }

    public void setResponse(String resp) {
        this.mResponse = resp;
    }

    public boolean isDefault() {
        return mIsDefault;
    }

    public void setDefault(boolean aDefault) {
        mIsDefault = aDefault;
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
        ContentValues values = getContentValues();
        values.put(DatabaseHelper.QuickResponseColumns._ID, mId);
        return context.getContentResolver().update(getUri(), values, "_id = "+mId, null);
    }

    /**
     * QuangNDb xoa 1 ban ghi trong db
     */
    public int deleteToDb(Context context) {
        return context.getContentResolver().delete(getUri(), "_id = "+mId, null);
    }

    public Uri getUri() {
        return MessagingContentProvider.QUICK_RESPONSE_URI;
    }

    /**Bkav QuangNDb bin du lieu tu cursor vao doi tuong*/
    public void bind(Cursor cursor) {
        mId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.QuickResponseColumns._ID));
        mResponse = cursor.getString(cursor.getColumnIndex(DatabaseHelper.QuickResponseColumns.RESPONSE));
        mIsDefault = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.QuickResponseColumns.IS_DEFAULT)) > 0;
    }

    /**Bkav QuangNDb lay content values*/
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.QuickResponseColumns.RESPONSE, mResponse);
        values.put(DatabaseHelper.QuickResponseColumns.IS_DEFAULT, mIsDefault);
        return values;
    }
}
