package bkav.android.btalk.messaging.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.android.messaging.Factory;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.util.ImageUtils;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 12/04/2017.
 * Bkav QuangNDb: Custom lai lop ConversationDrawables cua source goc
 */

public class BtalkConversationDrawables extends ConversationDrawables {

    private static ConversationDrawables sInstance;
    // Bkav QuangNDb them cac box message giong nhu egove
    private Drawable mFistMessageSingleBubble;
    private Drawable mFistMessageNotSingleBubbleIncoming;
    private Drawable mMidMessageBubbleIncoming;
    private Drawable mLastMessageBubbleIncoming;

    private Drawable mFistMessageNotSingleBubbleOutgoing;
    private Drawable mMidMessageBubbleOutgoing;
    private Drawable mLastMessageBubbleOutgoing;
    private int mOutgoingAudioButtonColor;


    private BtalkConversationDrawables(Context context) {
        super(context);
    }

    public static ConversationDrawables get() {
        if (sInstance == null) {
            sInstance = new BtalkConversationDrawables(Factory.get().getApplicationContext());
        }
        return sInstance;
    }

    @Override
    protected void setUpOtherDrawables(Resources resources) {
        mThemeColor = ContextCompat.getColor(mContext, R.color.theme_color);
        mIncomingAudioProgressBackgroundDrawable =
                ContextCompat.getDrawable(mContext, R.drawable.audio_progress_bar_background_outgoing);
        mOutgoingAudioProgressBackgroundDrawable =
                ContextCompat.getDrawable(mContext, R.drawable.audio_progress_bar_background_incoming);
        mAudioProgressForegroundDrawable =
                ContextCompat.getDrawable(mContext, R.drawable.audio_progress_bar_progress);
        mOutgoingBubbleColor = ContextCompat.getColor(mContext, R.color.bubble_color_out_going);
        mIncomingAudioButtonColor =
                ContextCompat.getColor(mContext, R.color.bubble_color_out_going);
        mOutgoingAudioButtonColor =
                ContextCompat.getColor(mContext, android.R.color.white);
//        mIncomingBubbleDrawable = ContextCompat.getDrawable(mContext,R.drawable.message_incoming_bubble);
//        mIncomingBubbleNoArrowDrawable =
//                ContextCompat.getDrawable(mContext,R.drawable.message_incoming_bubble_not_arrow);
//        mOutgoingBubbleDrawable =  ContextCompat.getDrawable(mContext,R.drawable.message_out_going_bubble);
//        mOutgoingBubbleNoArrowDrawable =
//                ContextCompat.getDrawable(mContext,R.drawable.btalk_message_bubble_outgoing_no_arrow);
//        mIncomingBubbleDrawable = resources.getDrawable(R.drawable.msg_bubble_incoming);
//        mIncomingBubbleDrawable = resources.getDrawable(R.drawable.bubble_incoming_white);
        mIncomingBubbleDrawable = resources.getDrawable(R.drawable.btalk_message_bubble_outgoing_no_arrow);
        mIncomingBubbleNoArrowDrawable = resources.getDrawable(R.drawable.btalk_message_bubble_outgoing_no_arrow);
//        mOutgoingBubbleDrawable =  resources.getDrawable(R.drawable.bubble_outgoing_white);
        mOutgoingBubbleDrawable = resources.getDrawable(R.drawable.btalk_message_bubble_outgoing_no_arrow);
        mOutgoingBubbleNoArrowDrawable =
                resources.getDrawable(R.drawable.btalk_message_bubble_outgoing_no_arrow);
        mFistMessageSingleBubble = ContextCompat.getDrawable(mContext, R.drawable.btalk_first_message_single_bubble);
        mFistMessageNotSingleBubbleIncoming = ContextCompat.getDrawable(mContext, R.drawable.btalk_first_message_not_single_bubble_incoming);
        mMidMessageBubbleIncoming = ContextCompat.getDrawable(mContext, R.drawable.btalk_mid_message_bubble_incoming);
        mLastMessageBubbleIncoming = ContextCompat.getDrawable(mContext, R.drawable.btalk_last_message_bubble_incoming);

        mLastMessageBubbleOutgoing = ContextCompat.getDrawable(mContext, R.drawable.btalk_last_message_bubble_outgoing);
        mFistMessageNotSingleBubbleOutgoing = ContextCompat.getDrawable(mContext, R.drawable.btalk_first_message_not_single_bubble_outgoing);
        mMidMessageBubbleOutgoing = ContextCompat.getDrawable(mContext, R.drawable.btalk_mid_message_bubble_outgoing);
    }

    @Override
    public Drawable getBtalkBubbleDrawable(boolean selected, boolean incoming, boolean isPreCluster, boolean isNextCluster) {
        final Drawable protoDrawable;
        if (!isPreCluster && !isNextCluster) {
            protoDrawable = mFistMessageSingleBubble;
        } else if (isPreCluster && !isNextCluster) {
            protoDrawable = incoming ? mLastMessageBubbleIncoming : mLastMessageBubbleOutgoing;
        } else if (!isPreCluster) {
            protoDrawable = incoming ? mFistMessageNotSingleBubbleIncoming : mFistMessageNotSingleBubbleOutgoing;
        } else {
            protoDrawable = incoming ? mMidMessageBubbleIncoming : mMidMessageBubbleOutgoing;
        }

        int color;
        if (selected) {
            color = mSelectedBubbleColor;
        } else if (incoming) {
            color = mThemeColor;
        } else {
            color = mOutgoingBubbleColor;
        }

        return ImageUtils.getTintedDrawable(mContext, protoDrawable, color);
    }

    protected int getAudioButtonColor(final boolean incoming) {
        return incoming ? mIncomingAudioButtonColor : mOutgoingAudioButtonColor;
    }
    @Override
    public Drawable getPlayButtonDrawable(final boolean incoming) {
        return ImageUtils.getTintedDrawableAudio(
                mContext, mAudioPlayButtonDrawable, getAudioButtonColor(incoming));
    }
    @Override
    public Drawable getPauseButtonDrawable(final boolean incoming) {
        return ImageUtils.getTintedDrawableAudio(
                mContext, mAudioPauseButtonDrawable, getAudioButtonColor(incoming));
    }
    @Override
    public Drawable getAudioProgressDrawable(final boolean incoming) {
        return ImageUtils.getTintedDrawableAudio(
                mContext, mAudioProgressForegroundDrawable, getAudioButtonColor(incoming));
    }
}
