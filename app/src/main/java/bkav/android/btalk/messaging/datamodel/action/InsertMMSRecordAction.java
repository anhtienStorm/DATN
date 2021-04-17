package bkav.android.btalk.messaging.datamodel.action;

import android.content.Intent;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.Action;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.messaging.datamodel.data.CallRecordData;
import bkav.android.btalk.receiver.CallRecorderReceiver;

import static bkav.android.btalk.receiver.CallRecorderReceiver.DATE;
import static bkav.android.btalk.receiver.CallRecorderReceiver.FROM_TEL;
import static bkav.android.btalk.receiver.CallRecorderReceiver.PATH;
import static bkav.android.btalk.receiver.CallRecorderReceiver.SUB_ID;
import static bkav.android.btalk.receiver.CallRecorderReceiver.TO_TEL;
import static bkav.android.btalk.receiver.CallRecorderReceiver.TYPE;
import static com.android.messaging.datamodel.DatabaseHelper.CALL_RECORD_TABLE;

/**
 * Created by quangnd on 24/10/2017.
 */

public class InsertMMSRecordAction extends Action {

    private static final String QUERY_CALL_RECORD = "select *"
            + " from " + CALL_RECORD_TABLE;

    public static void insertMMSRecordAction() {
        final InsertMMSRecordAction action = new InsertMMSRecordAction();
        action.start();
    }

    public InsertMMSRecordAction(Parcel in) {
        super(in);
    }
    public InsertMMSRecordAction() {
    }

    @Override
    protected Object executeAction() {
        final DatabaseWrapper db = DataModel.get().getDatabase();
        List<CallRecordData> callRecordDatas = new ArrayList<>();
        db.beginTransaction();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(QUERY_CALL_RECORD, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    final CallRecordData callRecordData = new CallRecordData();
                    callRecordData.initFromCursor(cursor);
                    callRecordDatas.add(callRecordData);
                    final Intent intent = new Intent(Factory.get().getApplicationContext(), CallRecorderReceiver.class);
                    intent.putExtra(DATE, callRecordData.getDate());
                    intent.putExtra(FROM_TEL, callRecordData.getFrom());
                    intent.putExtra(TO_TEL, callRecordData.getTo());
                    intent.putExtra(PATH, callRecordData.getFilePath());
                    intent.putExtra(TYPE, callRecordData.getType());
                    intent.putExtra(SUB_ID, callRecordData.getSubId());
                    Factory.get().getApplicationContext().sendBroadcast(intent);
                } while (cursor.moveToNext());
            }
            db.setTransactionSuccessful();
            //Bkav QuangNDb delete cac ban ghi trong bang call record di
            Factory.get().getApplicationContext().getContentResolver().delete(MessagingContentProvider.CALL_RECORD_URI, null, null);
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            db.endTransaction();
        }

        return null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeActionToParcel(dest, flags);
    }
    public static final Parcelable.Creator<InsertMMSRecordAction> CREATOR
            = new Parcelable.Creator<InsertMMSRecordAction>() {
        @Override
        public InsertMMSRecordAction createFromParcel(final Parcel in) {
            return new InsertMMSRecordAction(in);
        }

        @Override
        public InsertMMSRecordAction[] newArray(final int size) {
            return new InsertMMSRecordAction[size];
        }
    };
}
