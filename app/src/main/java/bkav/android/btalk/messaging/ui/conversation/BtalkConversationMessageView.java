package bkav.android.btalk.messaging.ui.conversation;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.datamodel.data.SubscriptionListData;
import com.android.messaging.ui.AudioAttachmentView;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.ui.MultiAttachmentLayout;
import com.android.messaging.ui.VideoThumbnailView;
import com.android.messaging.ui.conversation.ConversationMessageView;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.Dates;
import com.android.messaging.util.UiUtils;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.messaging.custom_view.BtalkSimIconView;
import bkav.android.btalk.messaging.datamodel.data.BtalkConversationMessageData;
import bkav.android.btalk.messaging.ui.BtalkAudioAttachmentView;
import bkav.android.btalk.messaging.ui.BtalkContactPhotoView;
import bkav.android.btalk.messaging.ui.BtalkConversationDrawables;
import bkav.android.btalk.messaging.ui.BtalkMultiAttachmentLayout;
import bkav.android.btalk.messaging.util.BtalkContactParser;
import bkav.android.btalk.messaging.util.BtalkIconParser;

/**
 * Created by quangnd on 07/04/2017.
 * BKav QuangNdb class custom lai conversation messageview de lam giion Btalk cu
 */

public class BtalkConversationMessageView extends ConversationMessageView {

    private BtalkContactPhotoView mPhotoView;

    private View mLayoutHeader;

    private TextView mDayLabel;

    private Context mContext;

    private BtalkSimIconView mSimIconView;

    private boolean mIsComplete = false;

    private boolean mIsShowIcon = false;// bien de biet xem co hien thi icon hay khong


    public BtalkConversationMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mData = new BtalkConversationMessageData();
        mIsShowIcon = getBooleanInDefaultPref(mContext.getString(R.string.option_show_avatar_key), false);
    }

    /**
     * Bkav QuangNDb Ham check co hien thi box messge ngan khong
     */
    private boolean isShouldSymmetry() {
        // Now check prefs (i.e. settings) to see if the user turned off notifications.
        final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();
        final Context context = Factory.get().getApplicationContext();
        final String prefKey = context.getString(R.string.option_symmetry_box_message_key);
        final boolean defaultValue = context.getResources().getBoolean(
                R.bool.symmetry_box_message_default);
        return prefs.getBoolean(prefKey, defaultValue);
    }

    /**
     * Bkav QuangNDb Ham check setting co always show sub info khong
     */
    private boolean shouldShowDetailsSub() {
        // Now check prefs (i.e. settings) to see if the user turned off notifications.
        final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();
        final Context context = Factory.get().getApplicationContext();
        final String prefKey = context.getString(R.string.option_show_details_key);
        final boolean defaultValue = context.getResources().getBoolean(
                R.bool.show_details_pref_default);
        return prefs.getBoolean(prefKey, defaultValue);
    }

    private boolean getBooleanInDefaultPref(String key, boolean defaultValue) {
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pre.getBoolean(key, defaultValue);
    }

    @Override
    protected void measureHeader(int unspecifiedMeasureSpec) {
        if (!mData.isCanDiffDayNextMessage()) {
            super.measureHeader(unspecifiedMeasureSpec);
        } else {
            mLayoutHeader.measure(unspecifiedMeasureSpec, unspecifiedMeasureSpec);
        }
    }

    @Override
    protected void setMeasuredDimensionForViewGroup(int with, int height) {
        if (!mData.isCanDiffDayNextMessage()) {
            super.setMeasuredDimensionForViewGroup(with, height);
        } else {
            final int arrowBot = mData.isLastMessage() ?
                    getResources().getDimensionPixelSize(R.dimen.large_margin)
                    :
                    getResources().getDimensionPixelSize(R.dimen.medium_margin);
            super.setMeasuredDimensionForViewGroup(with, height + mLayoutHeader.getMeasuredHeight() + arrowBot);
        }
    }

    @Override
    protected int getIconTop() {
        if (!mData.isCanDiffDayNextMessage()) {
            return super.getIconTop();
        } else {
            final int headerHeight = mLayoutHeader.getMeasuredHeight();
            final int arrowBot = getResources().getDimensionPixelSize(R.dimen.medium_margin);
            return getPaddingTop() + headerHeight + getPaddingBottom() + arrowBot;
        }
    }

    @Override
    protected void layoutHeader(int right) {
        if (!mData.isCanDiffDayNextMessage()) {
            super.layoutHeader(right);
        } else {
            final int headerWidth = mLayoutHeader.getMeasuredWidth();
            final int headerHeight = mLayoutHeader.getMeasuredHeight();
            final int headerTop = getPaddingTop();
            final int headerLeft;
            headerLeft = (right / 2) - (headerWidth / 2);
            mLayoutHeader.layout(headerLeft, headerTop, headerLeft + headerWidth, headerTop + headerHeight);
        }
    }

    private long mTimeTouch;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLayoutHeader = findViewById(R.id.layout_header);
        mDayLabel = (TextView) findViewById(R.id.day_label);
        mSimIconView = (BtalkSimIconView) findViewById(R.id.self_send_icon);

        // Anhdts su dung selectable cua text view bi xung dot voi onClick
        mMessageTextView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mTimeTouch = System.currentTimeMillis();
                } else if (event.getAction() == MotionEvent.ACTION_UP && (System.currentTimeMillis() - mTimeTouch) < 200) {
                    if (mHost.isShowActionMode()) {
                        final Object tag = mMessageTextView.getTag();
                        if (tag instanceof MessagePartData) {
                            final Rect bounds = UiUtils.getMeasuredBoundsOnScreen(mMessageTextView);
                            onAttachmentClick((MessagePartData) tag, bounds, false /* longPress */);
                        } else {
                            mHost.onMessageTextClick(BtalkConversationMessageView.this);
                        }
                    } else {
                        new Handler().postDelayed(mRunnableShowInfo, 50);
                    }
                }
                return false;
            }
        });
    }

    // Anhdts neu cham vao link hoac phone thi khong hien
    private Runnable mRunnableShowInfo = new Runnable() {
        @Override
        public void run() {
            if (mMessageTextView == null || mMessageTextView.getContentDescription() == null || !"clickLink".contentEquals(mMessageTextView.getContentDescription())) {
                showOrHideSubInfoWhenClick();
                mMessageTextView.setContentDescription("");
            }
        }
    };

    @Override
    protected void initContactIconView() {
//        mContactIconView = (BtalkContactIconView) findViewById(R.id.conversation_icon);
        mPhotoView = (BtalkContactPhotoView) findViewById(R.id.conversation_photo_icon);
    }

    @Override
    protected void setContactImageResourceUri(Uri avatarUri) {
        String id;
        if (mData.getIsIncoming()) {
            id = mData.getConversationId();
            mPhotoView.setImageResourceUriFromConversation(avatarUri, mData.getSenderContactId(),
                    mData.getSenderContactLookupKey(), mData.getSenderNormalizedDestination()
                    , id, mData.getSenderDisplayName());
        } else {
            id = mData.getSelfParticipantId();
            mPhotoView.setImageResourceUriFromParticipant(avatarUri, mData.getSenderContactId(),
                    mData.getSenderContactLookupKey(), mData.getSenderNormalizedDestination()
                    , id, mData.getSenderDisplayName());
        }
    }

    @Override
    protected void measurePhotoView(int iconMeasureSpec) {
        mPhotoView.measure(iconMeasureSpec, iconMeasureSpec);
    }

    @Override
    protected void layoutPhotoView(int iconLeft, int iconTop, int iconRight, int iconBot) {
        mPhotoView.layout(iconLeft, iconTop, iconRight, iconBot);
    }

    @Override
    protected void setPhotoViewVisibility(int state) {
        mPhotoView.setVisibility(state);
    }

    private boolean shouldShowLayoutHeader() {
        return mData.isCanDiffDayNextMessage();
    }

    /**
     * Bkav QuangNDb ham check xem co phai tin nhan cuoi cung khay khong
     */
    private boolean isLaseMessage() {
        return mData.isLastMessage();
    }

    @Override
    protected void updateViewContent() {
        mIsComplete = false;
        super.updateViewContent();
        if (shouldShowLayoutHeader()) {
            mLayoutHeader.setVisibility(VISIBLE);
            mDayLabel.setText(Dates.getBtalkMessageHeaderTimeString(mData.getReceivedTimeStamp()));
        } else {
            mLayoutHeader.setVisibility(GONE);
        }
        setMessageTextViewClickable(!mHost.isShowActionMode());
        if (!shouldShowDetailsSub()) {
            if (isLaseMessage() && mIsComplete) {
                showSubInfo();
            } else if (!isLaseMessage() && mIsComplete) {
                hideSubInfo();
            }
        } else {
            showSubInfo();
        }
    }

    @Override
    protected void updateTextAppearance() {
        super.updateTextAppearance();
        if (mData.hasAttachments() && !shouldShowMessageTextBubble() && !isMessageFail()) {
            mStatusTextView.setTextColor(getResources().getColor(R.color.btalk_timestamp_text_incoming));
        }
    }

    @Override
    protected ConversationDrawables getConversationDrawables() {
        return BtalkConversationDrawables.get();
    }

    @Override
    protected int getMessageColorResId() {
        // Bkav QuangNDb doi thanh het chu mau trang
        return (mData.getIsIncoming() ?
                R.color.btalk_message_text_color_incoming : R.color.btalk_message_text_color_outgoing);
    }

    @Override
    protected void binAudioAttachment() {
        bindAttachmentsOfSameType(sAudioFilter,
                R.layout.btalk_message_audio_attchment, mAudioViewBinder, BtalkAudioAttachmentView.class);
    }

    @Override
    protected AudioAttachmentView concatAudioAttachmentView(View view) {
        return (BtalkAudioAttachmentView) view;
    }

    @Override
    protected int getIdColorTimestampHasAttachments() {
        // Doi chu thanh mau trang het
//        return R.color.btalk_timestamp_text_incoming;
        return R.color.btalk_timestamp_text_incoming;
    }

    @Override
    protected int getMessageIncomingTextColorId() {
        return R.color.btalk_message_text_color_incoming;
        // Bkav QuangNDb doi chu thanh mau trang het
//        return R.color.btalk_message_text_color_outgoing;
    }

    @Override
    protected int getMessageOutgoingTextColorId() {
        return R.color.btalk_message_text_color_outgoing;
    }

    @Override
    protected int getTimestampIncomingTextColorId() {
        return R.color.btalk_timestamp_text_incoming;
    }

    @Override
    protected int getTimestampOutgoingTextColorId() {
        //Bkav QuangNDb doi sang mau xam het vi cho ra ngoai de giong thiet ke
        return R.color.btalk_timestamp_text_outgoing;
    }

    @Override
    protected void setMessageSelected(String selectedMessageId) {
        setSelected(mHost.isMessageSelected(mData.getMessageId()));
    }


    @Override
    public void onClick(View view) {
        if (mHost.isShowActionMode()) {
            final Object tag = view.getTag();
            if (tag instanceof MessagePartData) {
                final Rect bounds = UiUtils.getMeasuredBoundsOnScreen(view);
                onAttachmentClick((MessagePartData) tag, bounds, false /* longPress */);
            } else {
                mHost.onMessageTextClick(this);
                super.onClick(view);
            }
        } else if (view.getTag() == null) {
            showOrHideSubInfoWhenClick();
        } else {
            super.onClick(view);
        }
    }

    // TODO TrungTH tam thoi rao lai do gay cham chuong trinh
    @Override
    protected void setMessageText(CharSequence text) {
        if (mData.getIsIncoming()) {
            // TrungTH xu ly contact cho tin nhăn den
            text = BtalkContactParser.getInstance().addContactName(text).toString().replaceAll("\\s+$", "");
            mMessageTextView.setText(text);
        }
        final CharSequence smileyText = BtalkIconParser.getInstance().addSmileySpansWithTextSize(text, mMessageTextView.getLineHeight());
        mMessageTextView.setText(smileyText == null ? text : smileyText);

    }

    @Override
    protected void showSimName(SubscriptionListData.SubscriptionListEntry subscriptionEntry, boolean showSimIconAsIncoming) {
        // Bkav QuangNDb khong lam gi de hide sime name khi chua click vao
    }

    @Override
    protected void hideSimName() {
        super.hideSimName();
        mSimIconView.setVisibility(GONE);
        mSimIconView.setImageResourceUri(null, 0);
    }

    @Override
    protected void setBodyLinkTextColor(int messageColor) {
        // Bkav QuangNDb khong lam gi
    }

    @Override
    protected int getSelectedTextColorId() {
        return R.color.btalk_message_text_color_outgoing;
    }

    @Override
    protected void hideTimeStamp() {
        mStatusTextView.setVisibility(GONE);
    }

    @Override
    protected void visibleDeliveryReportView(boolean deliveredBadgeVisible) {
        mDeliveredBadge.setVisibility(GONE);
        // Bkav QuangNDb khong lam gi
    }

    private long mIsTimeShowInfo = 0;

    @Override
    public void showOrHideSubInfoWhenClick() {
        if (mStatusTextView.getVisibility() == VISIBLE) {
            if (!(isLaseMessage() && mIsComplete) && (System.currentTimeMillis() - mIsTimeShowInfo) > 300) {
                hideSubInfo();
            }
        } else {
            showSubInfo();
            mIsTimeShowInfo = System.currentTimeMillis();
        }
    }

    /**
     * Bkav QuangNDb hide sub info cua 1 message
     */
    private void hideSubInfo() {
        if (!shouldShowDetailsSub()) {
            mStatusTextView.setVisibility(GONE);
            mDeliveredBadge.setVisibility(GONE);
            mSimIconView.setVisibility(GONE);
            mSimNameView.setVisibility(GONE);
        }
    }

    /**
     * Bkav QuangNDb show sub info cua 1 message
     */
    private void showSubInfo() {
        final boolean deliveredBadgeVisible =
                mData.getStatus() == MessageData.BUGLE_STATUS_OUTGOING_DELIVERED;
        mDeliveredBadge.setVisibility(deliveredBadgeVisible ? View.VISIBLE : View.GONE);
        mStatusTextView.setVisibility(VISIBLE);
        //HienDTk: fix bug not bound; wasBound = true
//        final boolean showSimIconAsIncoming = mData.getIsIncoming() &&
//                (!mData.hasAttachments() || shouldShowMessageTextBubble());
//        final SubscriptionListData.SubscriptionListEntry subscriptionEntry =
//                mHost.getSubscriptionEntryForSelfParticipant(mData.getSelfParticipantId(),
//                        true /* excludeDefault */);

//        // QuangNDB sua dieu kien hien sim info
//        final boolean simNameVisible = subscriptionEntry != null &&
//                !TextUtils.isEmpty(subscriptionEntry.displayName);

        // QuangNDB sua dieu kien hien sim info
        final boolean simNameVisible = BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot() || ESimUtils.isMultiProfile();
        if (simNameVisible) {
//            final String simNameText = mData.getIsIncoming() ? getResources().getString(
//                    R.string.incoming_sim_name_text, subscriptionEntry.displayName) :
//                    subscriptionEntry.displayName;
//            final Uri iconUri = subscriptionEntry.iconUri;
//            if (iconUri != null) {
//                mSimIconView.setVisibility(VISIBLE);
//                if (subscriptionEntry.iccid != null) {
//                    mSimIconView.setImageDrawable(BtalkCallLogCache.getCallLogCache(getContext()).getSimIconWithIccid(subscriptionEntry.iccid));
//                }else {
//                    mSimIconView.setImageResourceUri(iconUri, getResources().getDimensionPixelSize(R.dimen.sim_icon_size));
//                }
//
//            } else {
//                mSimNameView.setText(simNameText);
//                mSimNameView.setTextColor(showSimIconAsIncoming ? getResources().getColor(
//                        getTimestampIncomingTextColorId()) : subscriptionEntry.displayColor);
//                mSimNameView.setVisibility(VISIBLE);
//            }
            //Bkav QuangNDb doi logic dung iccid trong sim
            mSimIconView.setVisibility(VISIBLE);
            mSimIconView.setImageDrawable(BtalkCallLogCache.getCallLogCache(mContext).getSimIconWithIccid(mData.getIccid()));
        } else {
            hideSimName();
        }
    }

    @Override
    protected void visibleStatusText(boolean statusVisible) {
        if (statusVisible && !mIsComplete) {
            mStatusTextView.setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setVisibility(View.GONE);
        }
    }

    @Override
    protected int getIdResIconSize() {
        return R.dimen.conversation_message_contact_icon_size;
    }

    @Override
    protected int getMaxLeftoverSpace(int horizontalSpace, int arrowWidth) {
        return isShouldIcon() ? horizontalSpace - mPhotoView.getMeasuredWidth() - getPaddingLeft() - getPaddingRight()
                : horizontalSpace - getResources().getDimensionPixelSize(getIdResIconSize()) - arrowWidth - getPaddingLeft() - getPaddingRight();
    }

    @Override
    protected void setMinWidthMessageBox(boolean showArrow, int arrowWidth) {
        int minWidth = getResources().getDimensionPixelSize(R.dimen.min_width_message_box);
        mMessageTextAndInfoView.setMinimumWidth(minWidth);
    }

    @Override
    protected boolean getConditionShowTimestamp() {
        return true;
    }

    @Override
    protected int getIdResTextLeftPadding() {
        return R.dimen.text_message_box_left_right_padding;
    }

    @Override
    protected int getIdResTextRightPadding() {
        return R.dimen.text_message_box_right_padding;
    }

    @Override
    protected int getIdResTextTopPadding() {
        return R.dimen.text_message_box_top_padding;
    }

    @Override
    protected int getIdResTextBotPadding() {
        return R.dimen.text_message_box_top_bot_padding;
    }

    @Override
    protected void setCompletedMessage() {
        mIsComplete = true;
    }

    @Override
    protected MultiAttachmentLayout getMultiAttachmentView() {
        return (BtalkMultiAttachmentLayout) findViewById(R.id.multiple_attachments);
    }

    private void clickMessage() {
        mMessageTextAndInfoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHideSubInfoWhenClick();
            }
        });
        mMessageTextAndInfoView.setLongClickable(false);
    }

    private void resetMessageClick() {
        mMessageTextAndInfoView.setClickable(false);
        mMessageTextAndInfoView.setLongClickable(false);
    }

    @Override
    protected int getIdResDimenPaddingCluster() {
        return R.dimen.btalk_padding_cluster_message;
    }

    // TODO TrungTH tam thoi rao lai do gay cham chuong trinh
    @Override
    protected void addLinkTextIfExist() {
        mMessageTextHasLinks = mHost.isMessageHasLink(mMessageTextView);
    }

    @Override
    protected int getIdResDimenPaddingTopDefault() {
        return R.dimen.message_padding_top_default;
    }

    @Override
    protected int getPaddingBotMessage() {
        if (!mData.isLastMessage()) {
            return super.getPaddingBotMessage();
        } else {
            return getResources().getDimensionPixelSize(R.dimen.btalk_padding_cluster_message);
        }
    }

    public static class IgnoreLinkClickHelper implements OnClickListener, OnTouchListener {

        private boolean mIsClick;
        private final OnClickListener mDelegateLongClickListener;

        public IgnoreLinkClickHelper(OnClickListener clickListener) {
            this.mDelegateLongClickListener = clickListener;
        }

        public static void ignoreLinkClick(final TextView textView, @Nullable final OnClickListener clickListener) {
            final IgnoreLinkClickHelper helper = new IgnoreLinkClickHelper(clickListener);
            textView.setOnClickListener(helper);
            textView.setOnTouchListener(helper);
        }

        @Override
        public void onClick(View v) {
            // Record that this click is a long click.
            mIsClick = true;
            if (mDelegateLongClickListener != null) {
                mDelegateLongClickListener.onClick(v);
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_UP && mIsClick) {
                // This touch event is a long click, preemptively handle this touch event so that
                // the link span won't get a onClicked() callback.
                mIsClick = false;
                return true;
            }

            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mIsClick = false;
            }
            return false;
        }
    }

    /**
     * BKav QuangNDb sua lai khong cho show arrow nua
     */
    @Override
    protected boolean shouldShowMessageBubbleArrow() {
        return false;
    }

    /**
     * Bkav QuangNDb tach code doan measure icon
     */
    @Override
    protected void measureIcon(int iconMeasureSpec) {
        if (isShouldIcon()) {
            measurePhotoView(iconMeasureSpec);
        } else {
            measurePhotoView(0);
        }
    }

    /**
     * BKav QuangNDb tach code doan get max height icon
     */
    @Override
    protected int getMaxIconHeight() {
        return mPhotoView.getMeasuredHeight();
    }

    /**
     * BKav QuangNDb tach code doan get icon measure height
     */
    @Override
    protected int getIconMeasureHeight() {
        return isShouldIcon() ? mPhotoView.getMeasuredHeight() : 0;
    }

    /**
     * BKav QuangNDb tach code doan get icon measure width
     */
    @Override
    protected int getIconMeasureWidth() {
        return isShouldIcon() ? mPhotoView.getMeasuredWidth() : 0;
    }

    /**
     * BKav QuangNDb tach code doan layout icon
     */
    @Override
    protected void layoutIcon(int iconLeft, int iconTop, int iconWidth, int iconHeight) {
        if (isShouldIcon()) {
            layoutPhotoView(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconHeight);
        } else {
            layoutPhotoView(0, 0, 0, 0);
        }
    }

    /**
     * Bkav QuangNDb should show
     */
    private boolean isShouldIcon() {
        return mIsShowIcon && mData.getIsIncoming();
    }


    @Override
    protected Drawable getBubbleDrawable(ConversationDrawables drawableProvider, boolean selected, boolean incoming, boolean needArrow, boolean isError, boolean isPreCluster, boolean isNextCluster) {
        return drawableProvider.getBtalkBubbleDrawable(selected, incoming, isPreCluster, isNextCluster);
    }

    @Override
    protected int getTimestampSelectedTextColorId() {
        if (mData.hasAttachments() && !shouldShowMessageTextBubble()) {
            return R.color.btalk_timestamp_text_incoming;
        }
        return R.color.btalk_timestamp_text_outgoing;
    }

    @Override
    protected Drawable getAudioDrawable() {
        return BtalkConversationDrawables.get().getBtalkBubbleDrawable(
                isSelected(), mData.getIsIncoming(), mData.getCanClusterWithPreviousMessage(),
                mData.getCanClusterWithNextMessage());
    }

    /**
     * Anhdts getAttach
     */
    public MessagePartData getAttach() {
        if (mMessageAttachmentsView == null) {
            return null;
        }
        for (int i = 0, size = mMessageAttachmentsView.getChildCount(); i < size; i++) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(i);
            if (attachmentView instanceof VideoThumbnailView
                    || attachmentView instanceof AudioAttachmentView) {
                final Object tag = attachmentView.getTag();
                if (tag instanceof MessagePartData) {
                    return (MessagePartData) tag;
                }
            }
        }
        return null;
    }

    @Override
    protected int getOutgoingAwaitOrSendingStatusText() {
        return R.string.message_status_send_retrying_or_sending;
    }
}
