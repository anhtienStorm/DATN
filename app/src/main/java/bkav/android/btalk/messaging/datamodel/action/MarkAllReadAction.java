package bkav.android.btalk.messaging.datamodel.action;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.datamodel.action.ActionMonitor;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.util.SendBroadcastUnreadMessage;

import static com.android.messaging.datamodel.DatabaseHelper.MESSAGES_TABLE;
import static com.android.messaging.datamodel.DatabaseHelper.MessageColumns.CONVERSATION_ID;
import static com.android.messaging.datamodel.DatabaseHelper.MessageColumns.READ;
import static com.android.messaging.datamodel.DatabaseHelper.MessageColumns.STATUS;

/**
 * Created by quangnd on 13/10/2017.
 * Action update tat ca tin nhan chua doc thanh da doc
 */

public class MarkAllReadAction extends Action {

    private static final String QUERY_UNREAD_CONVERSATION = "select " + CONVERSATION_ID
            + " from " + MESSAGES_TABLE
            + " where " + READ + " = 0 and " + STATUS + " <> 3"
            + " group by " + CONVERSATION_ID;

    public interface MarkAllReadInterface{
        void onMarkAllReadSucceed();//Bkav QuangNDb Sau khi mark all xong
    }

    public MarkAllReadAction() {
    }

    public MarkAllReadAction(String key) {
        super(key);
    }

    public MarkAllReadAction(Parcel in) {
        super(in);
    }

    /**
    * Bkav QuangNDb Mark all read unread message
    */
    public static MarkAllReadMonitor markAllRead(MarkAllReadInterface markAllReadInterface) {
        final MarkAllReadMonitor monitor = new MarkAllReadMonitor(markAllReadInterface, null);
        final MarkAllReadAction markAllReadAction = new MarkAllReadAction(monitor.getActionKey());
        markAllReadAction.start(monitor);
        return monitor;
    }

    @Override
    protected Object executeAction() {
        final DatabaseWrapper db = DataModel.get().getDatabase();
        db.beginTransaction();
        try {
            Cursor cursor = null;
            try {
                cursor = db.rawQuery(QUERY_UNREAD_CONVERSATION, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        final String conversationId = cursor.getInt(cursor.getColumnIndex(CONVERSATION_ID)) + "";
                        // Mark all messages in thread as read in telephony
                        final long threadId = BugleDatabaseOperations.getThreadId(db, conversationId);
                        if (threadId != -1) {
                            MmsUtils.updateSmsReadStatus(threadId, Long.MAX_VALUE);
                        }
                        // Update local db
                        final ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.MessageColumns.CONVERSATION_ID, conversationId);
                        values.put(DatabaseHelper.MessageColumns.READ, 1);
                        values.put(DatabaseHelper.MessageColumns.SEEN, 1);     // if they read it, they saw it

                        final int count = db.update(DatabaseHelper.MESSAGES_TABLE, values,
                                "(" + DatabaseHelper.MessageColumns.READ + " !=1 OR " +
                                        DatabaseHelper.MessageColumns.SEEN + " !=1 ) AND " +
                                        DatabaseHelper.MessageColumns.CONVERSATION_ID + "=?",
                                new String[] { conversationId });
                        if (count > 0) {
                            MessagingContentProvider.notifyMessagesChanged(conversationId);
                        }
                    } while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            SendBroadcastUnreadMessage.sendLocalBroadCast(Factory.get().getApplicationContext(),db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return null;
    }

    public static final Parcelable.Creator<MarkAllReadAction> CREATOR
            = new Parcelable.Creator<MarkAllReadAction>() {
        @Override
        public MarkAllReadAction createFromParcel(final Parcel in) {
            return new MarkAllReadAction(in);
        }

        @Override
        public MarkAllReadAction[] newArray(final int size) {
            return new MarkAllReadAction[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeActionToParcel(dest, flags);
    }

    /**
     * A monitor that notifies a listener upon completion
     */
    public static class MarkAllReadMonitor extends ActionMonitor
            implements ActionMonitor.ActionCompletedListener {
        private final MarkAllReadInterface mListener;

        MarkAllReadMonitor(final MarkAllReadInterface listener, Object data) {
            super(STATE_CREATED, generateUniqueActionKey("GetOrCreateConversationAction"), data);
            setCompletedListener(this);
            mListener = listener;
        }

        @Override
        public void onActionSucceeded(final ActionMonitor monitor,
                                      final Action action, final Object data, final Object result) {
            mListener.onMarkAllReadSucceed();
        }

        @Override
        public void onActionFailed(final ActionMonitor monitor,
                                   final Action action, final Object data, final Object result) {
            // TODO: Currently onActionFailed is only called if there is an error in
        }
    }
}
