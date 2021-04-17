package bkav.android.btalk.messaging.ui.searchSms;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.datamodel.data.ConversationMessageData;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.ui.BtalkContactPhotoView;

/**
 * Created by quangnd on 10/06/2017.
 */

public class SearchViewHolder extends ClickyViewHolder<ConversationMessageData> {

    protected View mRootView;
    protected BtalkContactPhotoView mAvatar;
    protected TextView mName;
    protected TextView mTimeStamp;
    protected TextView mSnippet;

    public SearchViewHolder(Context context, View itemView) {
        super(context, itemView);
        mRootView = itemView;
        mAvatar = (BtalkContactPhotoView) itemView.findViewById(R.id.conversation_icon);
        mName = (TextView) itemView.findViewById(R.id.conversation_name);
        mSnippet = (TextView) itemView.findViewById(R.id.conversation_snippet);
        mTimeStamp = (TextView) itemView.findViewById(R.id.conversation_timestamp);
    }

    public View getRootView() {
        return mRootView;
    }

    public BtalkContactPhotoView getAvatar() {
        return mAvatar;
    }

    public TextView getName() {
        return mName;
    }

    public TextView getTimeStamp() {
        return mTimeStamp;
    }

    public TextView getSnippet() {
        return mSnippet;
    }

}
