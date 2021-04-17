package bkav.android.btalk.messaging.ui.conversation;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.android.messaging.datamodel.action.InsertNewMessageAction;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.ui.AsyncImageView;
import com.android.messaging.ui.conversation.ConversationMessageAdapter;
import com.android.messaging.ui.conversation.ConversationMessageView;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 07/04/2017.
 */

public class BtalkConversationMessageAdapter extends ConversationMessageAdapter {

    private Context mContext;

    public BtalkConversationMessageAdapter(Context context, Cursor cursor, ConversationMessageView.ConversationMessageViewHost host, AsyncImageView.AsyncImageViewDelayLoader imageViewDelayLoader
            , View.OnClickListener viewClickListener, View.OnLongClickListener longClickListener) {
        super(context, cursor, host, imageViewDelayLoader, viewClickListener, longClickListener);
        mContext = context;
    }

    @Override
    protected ConversationMessageView initConversationMessageView(LayoutInflater layoutInflater) {
        return (BtalkConversationMessageView) layoutInflater.inflate(R.layout.btalk_conversation_message_view, null);
    }

    @Override
    protected ConversationMessageView concatConversationMessageView(View view) {
        return (BtalkConversationMessageView) view;
    }

    private static final int MESSAGE_ANIMATION_MAX_WAIT = 500;

    @Override
    protected void setAnimation(View view, int position, ConversationMessageView conversationMessageView) {
        ConversationMessageData data = conversationMessageView.getData();
        final long timeSinceSend = System.currentTimeMillis() - data.getReceivedTimeStamp();
        // animation tin nhan di
        if (data.getReceivedTimeStamp() == InsertNewMessageAction.getLastSentMessageTimestamp()
                && !data.getIsIncoming()
                && timeSinceSend < MESSAGE_ANIMATION_MAX_WAIT) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.push_bot_in);
            // Bkav QuangNDb them ham bat su kien animation
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mListener != null) {
                        mListener.onMessageAnimationEnd();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(animation);
        } else if (data.getIsIncoming() && timeSinceSend < MESSAGE_ANIMATION_MAX_WAIT) {
            // animation tin nhan den
            // TODO: 26/05/2017 Bkav QuangNDb khong xu ly duoc tin nhan den
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.push_left_in);
            view.startAnimation(animation);
        }
    }

}
