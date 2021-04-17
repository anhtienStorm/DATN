package bkav.android.btalk.messaging.datamodel.action;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.sms.MmsUtils;

/**
 * Created by quangnd on 18/10/2017.
 * action test xoa toan bo tin nhan
 */

public class DeleteAllMessageAction extends Action {

    private static final String QUERY_ALL_MESSAGE = "select _id from messages";

    public DeleteAllMessageAction(Parcel in) {
        super(in);
    }

    public DeleteAllMessageAction() {
    }

    public static void deleteAllMessage() {
        new DeleteAllMessageAction().start();
    }

    @Override
    protected Object executeAction() {
        final DatabaseWrapper db = DataModel.get().getDatabase();
        db.beginTransaction();
        // First find the thread id for this conversation.
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(QUERY_ALL_MESSAGE, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    final String messageId = cursor.getString(0);
                    if (!TextUtils.isEmpty(messageId)) {
                        // Check message still exists
                        final MessageData message = BugleDatabaseOperations.readMessage(db, messageId);
                        if (message != null) {
                            // Delete from local DB
                            BugleDatabaseOperations.deleteMessage(db, messageId);
                            MessagingContentProvider.notifyMessagesChanged(message.getConversationId());
                            // We may have changed the conversation list
                            MessagingContentProvider.notifyConversationListChanged();

                            final Uri messageUri = message.getSmsMessageUri();
                            if (messageUri != null) {
                                // Delete from telephony DB
                                MmsUtils.deleteMessage(messageUri);
                            }
                        }
                    }
                } while (cursor.moveToNext());
            }
            db.setTransactionSuccessful();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            db.endTransaction();
        }


        return null;
    }

    public static final Parcelable.Creator<DeleteAllMessageAction> CREATOR
            = new Parcelable.Creator<DeleteAllMessageAction>() {
        @Override
        public DeleteAllMessageAction createFromParcel(final Parcel in) {
            return new DeleteAllMessageAction(in);
        }

        @Override
        public DeleteAllMessageAction[] newArray(final int size) {
            return new DeleteAllMessageAction[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeActionToParcel(dest, flags);
    }
}
