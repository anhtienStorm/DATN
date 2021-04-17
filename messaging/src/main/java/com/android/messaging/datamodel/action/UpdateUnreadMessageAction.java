package com.android.messaging.datamodel.action;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.util.SendBroadcastUnreadMessage;

/**
 * Created by quangnd on 14/08/2017.
 * Update unread message to launch
 */

public class UpdateUnreadMessageAction extends Action {

    public static void updateUnreadMessageAction() {
        new UpdateUnreadMessageAction().start();
    }
    private UpdateUnreadMessageAction() {
    }

    public UpdateUnreadMessageAction(Parcel in) {
        super(in);
    }

    @Override
    protected Object executeAction() {
        final DatabaseWrapper db = DataModel.get().getDatabase();
        db.beginTransaction();
        try {
            // Bkav QuangNDB ban intent notify cho launcher unread message
            SendBroadcastUnreadMessage.sendLocalBroadCast(Factory.get().getApplicationContext(),db);
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        return null;
    }
    public static final Parcelable.Creator<UpdateUnreadMessageAction> CREATOR
            = new Parcelable.Creator<UpdateUnreadMessageAction>() {
        @Override
        public UpdateUnreadMessageAction createFromParcel(final Parcel in) {
            return new UpdateUnreadMessageAction(in);
        }

        @Override
        public UpdateUnreadMessageAction[] newArray(final int size) {
            return new UpdateUnreadMessageAction[size];
        }
    };
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeActionToParcel(dest, flags);
    }
}
