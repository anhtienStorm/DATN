package bkav.android.btalk.messaging.datamodel.action;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.Action;

/**
 * Created by quangnd on 25/10/2017.
 */

public class DeleteAllCallRecordAction extends Action {

    public DeleteAllCallRecordAction(Parcel in) {
        super(in);
    }

    public DeleteAllCallRecordAction() {
    }

    public static void deleteAllCallRecord() {
        DeleteAllCallRecordAction deleteAllCallRecordAction = new DeleteAllCallRecordAction();
        deleteAllCallRecordAction.start();
    }

    @Override
    protected Object executeAction() {
        Factory.get().getApplicationContext().getContentResolver().delete(MessagingContentProvider.CALL_RECORD_URI, null, null);
        return null;
    }

    public static final Parcelable.Creator<DeleteAllCallRecordAction> CREATOR
            = new Parcelable.Creator<DeleteAllCallRecordAction>() {
        @Override
        public DeleteAllCallRecordAction createFromParcel(final Parcel in) {
            return new DeleteAllCallRecordAction(in);
        }

        @Override
        public DeleteAllCallRecordAction[] newArray(final int size) {
            return new DeleteAllCallRecordAction[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }


}
