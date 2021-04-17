package bkav.android.btalk.calllog.recoder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CallLog;

import com.android.dialer.calllog.CallLogQueryHandler;
import com.android.messaging.Factory;
import com.android.messaging.datamodel.action.Action;

/*
 * HuyNQn xy ly viec nguoi dung doi ten file ghi am thi se xoa path khoi record_call_data
 * */
public class DeleteCallRecordPathAction extends Action {

    private static final String KEY_PATH_CALL_RECORD = "call_record_path";

    public DeleteCallRecordPathAction(Parcel in) {
        super(in);
    }


    private DeleteCallRecordPathAction(String path) {
        actionParameters.putString(KEY_PATH_CALL_RECORD, path);
    }

    public static void deleteCallRecordPath(String path) {
        DeleteCallRecordPathAction deleteCallRecordPathAction = new DeleteCallRecordPathAction(path);
        deleteCallRecordPathAction.start();
    }

    @Override
    protected Object executeAction() {
        final ContentResolver resolver = Factory.get().getApplicationContext().getContentResolver();
        //Bkav QuangNDb get path can delete tu param, khong dung static
        final String pathDelete = actionParameters.getString(KEY_PATH_CALL_RECORD);
        ContentValues values = new ContentValues();
        values.put(CallLogQueryHandler.RECORD_CALL_DATA, "");
        try {
            resolver.update(CallLog.Calls.CONTENT_URI, values, CallLogQueryHandler.RECORD_CALL_DATA + " LIKE '" + pathDelete + "'", null);
        } catch (Exception e) {

        }
        return null;
    }

    public static final Parcelable.Creator<DeleteCallRecordPathAction> CREATOR =
            new Creator<DeleteCallRecordPathAction>() {
                @Override
                public DeleteCallRecordPathAction createFromParcel(Parcel source) {
                    return new DeleteCallRecordPathAction(source);
                }

                @Override
                public DeleteCallRecordPathAction[] newArray(int size) {
                    return new DeleteCallRecordPathAction[size];
                }
            };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeActionToParcel(dest, flags);
    }
}
