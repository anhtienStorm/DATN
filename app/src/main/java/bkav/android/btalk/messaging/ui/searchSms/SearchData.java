package bkav.android.btalk.messaging.ui.searchSms;

import android.database.Cursor;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.data.ConversationListItemData;

import bkav.android.btalk.messaging.datamodel.data.BtalkConversationListItemData;

/**
 * Created by quangnd on 10/06/2017.
 * Bkav QuangNDb class nay tam dong lai, khong dung nua
 */

public class SearchData {

    protected long mThreadId;
    //    protected Contact mContact;
//    protected ParticipantData mParticipantData;
    protected ConversationListItemData mConversationListItemData;
    protected String mBody;
    protected long mDate;
    protected long mRowId;

    private static final String TAG = "SearchData";

    public SearchData(Cursor cursor) {
        final int threadIdPos = cursor.getColumnIndex("thread_id");
        final int addressPos = cursor.getColumnIndex("address");
        final int bodyPos = cursor.getColumnIndex("body");
        final int datePos = cursor.getColumnIndex("date");
        final int rowidPos = cursor.getColumnIndex("_id");

        mThreadId = cursor.getLong(threadIdPos);

        String conversationId = getConversationId(mThreadId);

        String address = cursor.getString(addressPos);

//        mContact = address != null ? Contact.get(address, false) : null;

        mBody = cursor.getString(bodyPos);

        mDate = cursor.getLong(datePos);

        mRowId = cursor.getLong(rowidPos);

//        mParticipantData = getParticipantFromAddress(PhoneUtils.getDefault().formatForDisplay(address));

        mConversationListItemData = getConversationData(conversationId);

    }

    /**
     * Bkav QuangNDb lay du lieu conversation listitem data
     */
    private ConversationListItemData getConversationData(String conversationId) {
        Cursor cursor = null;
        ConversationListItemData listItemData = new BtalkConversationListItemData();
        try {
            String where = ConversationListItemData.ConversationListViewColumns._ID + " = " + conversationId;
            cursor = Factory.get().getApplicationContext().getContentResolver().query(MessagingContentProvider.CONVERSATIONS_URI
                    , ConversationListItemData.PROJECTION, where, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                listItemData.bind(cursor);
                return listItemData;
            }
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Bkav QuangNDb lay conversation id tu database, tu 1 thread id truyen vao
     */
    private String getConversationId(long threadId) {
        String conversationId;
        Cursor cursor = null;
        try {
            String where = DatabaseHelper.ConversationColumns.SMS_THREAD_ID + " = " + threadId;
            cursor = Factory.get().getApplicationContext().getContentResolver().query(MessagingContentProvider.CONVERSATIONS_URI_ALL, null, where, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                conversationId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ConversationColumns._ID));
                return conversationId;
            }
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
