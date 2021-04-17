package bkav.android.btalk.messaging.datamodel.action;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.action.Action;

import bkav.android.btalk.messaging.datamodel.data.CallRecordData;

/**
 * Created by quangnd on 24/10/2017.
 */

public class InsertCallRecordAction extends Action {

    private static final String KEY_DATE = "key_date";
    private static final String KEY_FROM = "key_from";
    private static final String KEY_TO = "key_to";
    private static final String KEY_TYPE = "key_type";
    private static final String KEY_SUB_ID = "key_sub_id";
    private static final String KEY_FILE_PATH = "key_file_path";

    public InsertCallRecordAction(final long date, final String from, final String to,
                                  final int type, final int subId, final String filePath) {
        actionParameters.putLong(KEY_DATE, date);
        actionParameters.putString(KEY_FROM, from);
        actionParameters.putString(KEY_TO, to);
        actionParameters.putInt(KEY_TYPE, type);
        actionParameters.putInt(KEY_SUB_ID, subId);
        actionParameters.putString(KEY_FILE_PATH, filePath);
    }

    public static void insertCallRecord(final long date, final String from, final String to,
                                        final int type, final int subId, final String filePath) {
        final InsertCallRecordAction action = new InsertCallRecordAction(date, from, to, type, subId, filePath);
        action.start();
    }

    public InsertCallRecordAction(Parcel in) {
        super(in);
    }

    public InsertCallRecordAction() {

    }

    @Override
    protected Object executeAction() {
        final long date = actionParameters.getLong(KEY_DATE);
        final String from = actionParameters.getString(KEY_FROM);
        final String to = actionParameters.getString(KEY_TO);
        final int type = actionParameters.getInt(KEY_TYPE);
        final int sub_id = actionParameters.getInt(KEY_SUB_ID);
        final String filepath = actionParameters.getString(KEY_FILE_PATH);
        final CallRecordData callRecordData = new CallRecordData(date, from, to, sub_id, type, filepath);
        callRecordData.insertToDb(Factory.get().getApplicationContext());
        return null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeActionToParcel(dest, flags);
    }

    public static final Parcelable.Creator<InsertCallRecordAction> CREATOR
            = new Parcelable.Creator<InsertCallRecordAction>() {
        @Override
        public InsertCallRecordAction createFromParcel(final Parcel in) {
            return new InsertCallRecordAction(in);
        }

        @Override
        public InsertCallRecordAction[] newArray(final int size) {
            return new InsertCallRecordAction[size];
        }
    };




}
