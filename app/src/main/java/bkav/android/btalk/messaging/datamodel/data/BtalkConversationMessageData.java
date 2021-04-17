package bkav.android.btalk.messaging.datamodel.data;

import android.database.Cursor;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.action.DeleteMessageAction;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.util.Dates;

import java.io.File;
import java.util.Calendar;

/**
 * Created by quangnd on 07/04/2017.
 */

public class BtalkConversationMessageData extends ConversationMessageData {

    private boolean mCanDiffDayNextMessage = false;

    private boolean mIsLastMessage = false;
    @Override
    public String getFormattedReceivedTimeStamp() {
        return Dates.getBtalkMessageTimeString(mReceivedTimestamp).toString();
    }

    @Override
    public void bind(Cursor cursor) {
        super.bind(cursor);
        if (!cursor.isFirst() && cursor.moveToPrevious()) {
            mCanDiffDayNextMessage = canDiffDayNextMessage(cursor);
            cursor.moveToNext();
        }
        if (cursor.isFirst()) {
            mCanDiffDayNextMessage = true;
        }
        mIsLastMessage = cursor.isLast();

        //Bkav QuangNDb xy ly truong hop neu path audio trong mms bi xoa boi duyet file thi xoa tin nhan luon
        for (MessagePartData partData : mParts) {
            if (partData.isAudio()) {
                Cursor cursorPart = Factory.get().getApplicationContext().getContentResolver().query(partData.getContentUri(),
                        new String[]{"cid"},null, null, null);
                if (cursorPart != null) {
                    if (cursorPart.getCount() > 0 && cursorPart.moveToNext()) {
                        String cid = cursorPart.getString(0);
                        //Bkav QuangNDb de check tu ban moi thi xoa, cac file ghi am cu van giu lai
                        if (cid.startsWith("/storage/emulated")) {
                            File file = new File(cid);
                            if (!file.exists()) {
                                DeleteMessageAction.deleteMessage(mMessageId);
                            }
                        }
                    }
                    cursorPart.close();
                }
            }
        }

    }

    /**
     *  Bkav QuangNDb them ham check xem 2 tin nhan co khac ngay nhau hay khong
     *  neu khac return true
     */
    private boolean canDiffDayNextMessage(Cursor cursor) {
        final long nextReceivedTimestamp = cursor.getLong(INDEX_RECEIVED_TIMESTAMP);
        Calendar timeCalendarCurrentMessage = Calendar.getInstance();
        Calendar timeCalendarNextMessage = Calendar.getInstance();
        timeCalendarCurrentMessage.setTimeInMillis(mReceivedTimestamp);
        timeCalendarNextMessage.setTimeInMillis(nextReceivedTimestamp);
        return !(timeCalendarCurrentMessage.get(Calendar.DATE) == timeCalendarNextMessage.get(Calendar.DATE)
                && timeCalendarCurrentMessage.get(Calendar.MONTH) == timeCalendarNextMessage.get(Calendar.MONTH)
                && timeCalendarCurrentMessage.get(Calendar.YEAR) == timeCalendarNextMessage.get(Calendar.YEAR));
    }

    @Override
    public boolean isCanDiffDayNextMessage() {
        return mCanDiffDayNextMessage;
    }

    @Override
    public boolean isLastMessage() {
        return mIsLastMessage;
    }

    @Override
    protected boolean getConditionCanNotCluster(long mReceivedTimestamp, long otherReceivedTimestamp) {
        Calendar timeCalendarCurrentMessage = Calendar.getInstance();
        Calendar timeCalendarOtherMessage = Calendar.getInstance();
        timeCalendarCurrentMessage.setTimeInMillis(mReceivedTimestamp);
        timeCalendarOtherMessage.setTimeInMillis(otherReceivedTimestamp);
        return !(timeCalendarCurrentMessage.get(Calendar.DATE) == timeCalendarOtherMessage.get(Calendar.DATE)
                && timeCalendarCurrentMessage.get(Calendar.MONTH) == timeCalendarOtherMessage.get(Calendar.MONTH)
                && timeCalendarCurrentMessage.get(Calendar.YEAR) == timeCalendarOtherMessage.get(Calendar.YEAR));
    }
}
