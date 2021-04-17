/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.messaging.ui.conversation;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.datamodel.data.SubscriptionListData.SubscriptionListEntry;
import com.android.messaging.datamodel.media.ImageRequestDescriptor;
import com.android.messaging.datamodel.media.MessagePartImageRequestDescriptor;
import com.android.messaging.datamodel.media.UriImageRequestDescriptor;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.ui.AsyncImageView;
import com.android.messaging.ui.AsyncImageView.AsyncImageViewDelayLoader;
import com.android.messaging.ui.AudioAttachmentView;
import com.android.messaging.ui.ContactIconView;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.ui.MultiAttachmentLayout;
import com.android.messaging.ui.MultiAttachmentLayout.OnAttachmentClickListener;
import com.android.messaging.ui.PersonItemView;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.VideoThumbnailView;
import com.android.messaging.util.AccessibilityUtil;
import com.android.messaging.util.Assert;
import com.android.messaging.util.AvatarUriUtil;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.ImageUtils;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.UiUtils;
import com.android.messaging.util.YouTubeUtil;
import com.google.common.base.Predicate;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The view for a single entry in a conversation.
 */
public class ConversationMessageView extends FrameLayout implements View.OnClickListener,
        View.OnLongClickListener, OnAttachmentClickListener {
    public interface ConversationMessageViewHost {
        boolean onAttachmentClick(ConversationMessageView view, MessagePartData attachment,
                Rect imageBounds, boolean longPress);
        SubscriptionListEntry getSubscriptionEntryForSelfParticipant(String selfParticipantId,
                boolean excludeDefault);
        //Bkav QuangNDb them method ho tro multi check
        boolean isMessageSelected(final String messageId);
        boolean isShowActionMode();
        boolean onMessageTextClick(ConversationMessageView view);
        boolean isMessageHasLink(TextView messageText);
    }

    protected ConversationMessageData mData;

    protected LinearLayout mMessageAttachmentsView;
    private MultiAttachmentLayout mMultiAttachmentView;
    private AsyncImageView mMessageImageView;
    protected TextView mMessageTextView;
    protected boolean mMessageTextHasLinks;
    private boolean mMessageHasYouTubeLink;
    protected TextView mStatusTextView;
    private TextView mTitleTextView;
    private TextView mMmsInfoTextView;
    private LinearLayout mMessageTitleLayout;
    private TextView mSenderNameTextView;
    protected ContactIconView mContactIconView;
    protected ConversationMessageBubbleView mMessageBubble;
    private View mSubjectView;
    private TextView mSubjectLabel;
    private TextView mSubjectText;
    protected View mDeliveredBadge;
    private ViewGroup mMessageMetadataView;
    protected ViewGroup mMessageTextAndInfoView;
    protected TextView mSimNameView;

    private boolean mOneOnOne;
    protected ConversationMessageViewHost mHost;

    //Bkav QuangNDb Tach bien audioview thanh bien global de them thao tac nhan su kien cuoc goi
    protected AudioAttachmentView mAudioView;

    public ConversationMessageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        // TODO: we should switch to using Binding and DataModel factory methods.
        mData = new ConversationMessageData();
    }

    @Override
    protected void onFinishInflate() {
        initContactIconView();

        mMessageAttachmentsView = (LinearLayout) findViewById(R.id.message_attachments);
        mMultiAttachmentView = getMultiAttachmentView();
        mMultiAttachmentView.setOnAttachmentClickListener(this);

        mMessageImageView = (AsyncImageView) findViewById(R.id.message_image);
        mMessageImageView.setOnClickListener(this);
        mMessageImageView.setOnLongClickListener(this);

        initMessageTextView();
        initMessageClick();
        IgnoreLinkLongClickHelper.ignoreLinkLongClick(mMessageTextView, this);

        mStatusTextView = (TextView) findViewById(R.id.message_status);
        mTitleTextView = (TextView) findViewById(R.id.message_title);
        mMmsInfoTextView = (TextView) findViewById(R.id.mms_info);
        mMessageTitleLayout = (LinearLayout) findViewById(R.id.message_title_layout);
        mSenderNameTextView = (TextView) findViewById(R.id.message_sender_name);
        mMessageBubble = (ConversationMessageBubbleView) findViewById(R.id.message_content);
        mSubjectView = findViewById(R.id.subject_container);
        mSubjectLabel = (TextView) mSubjectView.findViewById(R.id.subject_label);
        mSubjectText = (TextView) mSubjectView.findViewById(R.id.subject_text);
        mDeliveredBadge = findViewById(R.id.smsDeliveredBadge);
        mMessageMetadataView = (ViewGroup) findViewById(R.id.message_metadata);
        mMessageTextAndInfoView = (ViewGroup) findViewById(R.id.message_text_and_info);
        // Bkav QuangNDb them ham click vao message
//        clickMessage();
        mSimNameView = (TextView) findViewById(R.id.sim_name);
    }



    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int horizontalSpace = MeasureSpec.getSize(widthMeasureSpec);
        final int iconSize = getResources()
                .getDimensionPixelSize(getIdResIconSize());

        final int unspecifiedMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        final int iconMeasureSpec = MeasureSpec.makeMeasureSpec(iconSize, MeasureSpec.EXACTLY);

        measureIcon(iconMeasureSpec);

        // Bkav QuangNDb them ham measure lai header layout
        measureHeader(unspecifiedMeasureSpec);
        final int arrowWidth =
                getResources().getDimensionPixelSize(R.dimen.message_bubble_arrow_width);

        // We need to subtract contact icon width twice from the horizontal space to get
        // the max leftover space because we want the message bubble to extend no further than the
        // starting position of the message bubble in the opposite direction.
        // Bkav QuangNDb tach code doan getMaxLeftoverSpace de custom lai
        final int maxLeftoverSpace = getMaxLeftoverSpace(horizontalSpace, arrowWidth);
        final int messageContentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(maxLeftoverSpace,
                MeasureSpec.AT_MOST);

        mMessageBubble.measure(messageContentWidthMeasureSpec, unspecifiedMeasureSpec);

        // Them doan getMax box chat height
        final int maxHeight = Math.max(getMaxIconHeight(), mMessageBubble.getMeasuredHeight());
        //Bkav QuangNDb tach code doan setmeasure cua view group ra de override lai o lop con
        setMeasuredDimensionForViewGroup(horizontalSpace, maxHeight + getPaddingBottom() + getPaddingTop());
    }



    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right,
            final int bottom) {
        final boolean isRtl = AccessibilityUtil.isLayoutRtl(this);

        final int iconWidth = getIconMeasureWidth();
        final int iconHeight = getIconMeasureHeight();
        // Bkav QuangNDb tach code doan get iconTop ra de override lai o class con
        final int iconTop = getIconTop();
        // Bkav TienNAb: sua lai chieu dai max cua messageView theo thiet ke
        final int contentWidth = (right -left) - iconWidth - getPaddingLeft() - getPaddingRight();
        final int contentHeight = mMessageBubble.getMeasuredHeight();

        final int iconLeft;
        final int contentLeft;
        if (mData.getIsIncoming()) {
            if (isRtl) {
                iconLeft = (right - left) - getPaddingRight() - iconWidth;
                contentLeft = iconLeft - contentWidth;
            } else {
                iconLeft = getPaddingLeft();
                contentLeft = iconLeft + iconWidth;
            }
        } else {
            if (isRtl) {
                iconLeft = getPaddingLeft();
                contentLeft = iconLeft + iconWidth;
            } else {
                iconLeft = (right - left) - getPaddingRight() - iconWidth;
                contentLeft = iconLeft - contentWidth;
            }
        }

        mMessageBubble.layout(contentLeft, iconTop, contentLeft + contentWidth, iconTop + contentHeight);
        // BKav QuangNDB them ham layout lai photoview
        // Bkav TienNAb: sua lai vi tri hien thi icon
        layoutIcon(iconLeft,iconTop + contentHeight - iconHeight - mMessageMetadataView.getHeight(), iconWidth, iconHeight);
        // Bkav QuangNDb them ham layoutHeader de layout lai header view
        layoutHeader(right);
    }



    /**
     * Fills in the data associated with this view.
     *
     * @param cursor The cursor from a MessageList that this view is in, pointing to its entry.
     */
    public void bind(final Cursor cursor) {
        bind(cursor, true, null);
    }

    /**
     * Fills in the data associated with this view.
     *
     * @param cursor The cursor from a MessageList that this view is in, pointing to its entry.
     * @param oneOnOne Whether this is a 1:1 conversation
     */
    public void bind(final Cursor cursor,
            final boolean oneOnOne, final String selectedMessageId) {
        mOneOnOne = oneOnOne;

        // Update our UI model
        mData.bind(cursor);

        // Bkav QuangNdb tach code doan setSelected de override lai o class con
        setMessageSelected(selectedMessageId);

        // Update text and image content for the view.
        updateViewContent();

        // Update colors and layout parameters for the view.
        updateViewAppearance();

        updateContentDescription();
    }


    public void setHost(final ConversationMessageViewHost host) {
        mHost = host;
    }

    /**
     * Sets a delay loader instance to manage loading / resuming of image attachments.
     */
    public void setImageViewDelayLoader(final AsyncImageViewDelayLoader delayLoader) {
        Assert.notNull(mMessageImageView);
        mMessageImageView.setDelayLoader(delayLoader);
        mMultiAttachmentView.setImageViewDelayLoader(delayLoader);
    }

    public ConversationMessageData getData() {
        return mData;
    }

    /**
     * Returns whether we should show simplified visual style for the message view (i.e. hide the
     * avatar and bubble arrow, reduce padding).
     */
    protected boolean shouldShowSimplifiedVisualStyle() {
        return mData.getCanClusterWithPreviousMessage();
    }

    // Bkav TienNAb: ham check nen show icon hay khong
    protected boolean shouldShowIcon(){
        return mData.getCanClusterWithNextMessage();
    }

    /**
     * Returns whether we need to show message bubble arrow. We don't show arrow if the message
     * contains media attachments or if shouldShowSimplifiedVisualStyle() is true.
     */
    protected boolean shouldShowMessageBubbleArrow() {
        return !shouldShowSimplifiedVisualStyle()
                && !(mData.hasAttachments() || mMessageHasYouTubeLink);
    }

    /**
     * Returns whether we need to show a message bubble for text content.
     */
    protected boolean shouldShowMessageTextBubble() {
        if (mData.hasText()) {
            return true;
        }
        final String subjectText = MmsUtils.cleanseMmsSubject(getResources(),
                mData.getMmsSubject());
        if (!TextUtils.isEmpty(subjectText)) {
            return true;
        }
        return false;
    }

    protected void updateViewContent() {
        updateMessageContent();
        int titleResId = -1;
        int statusResId = -1;
        String statusText = null;
        switch(mData.getStatus()) {
            case MessageData.BUGLE_STATUS_INCOMING_AUTO_DOWNLOADING:
            case MessageData.BUGLE_STATUS_INCOMING_MANUAL_DOWNLOADING:
            case MessageData.BUGLE_STATUS_INCOMING_RETRYING_AUTO_DOWNLOAD:
            case MessageData.BUGLE_STATUS_INCOMING_RETRYING_MANUAL_DOWNLOAD:
                titleResId = R.string.message_title_downloading;
                statusResId = R.string.message_status_downloading;
                break;

            case MessageData.BUGLE_STATUS_INCOMING_YET_TO_MANUAL_DOWNLOAD:
                if (!OsUtil.isSecondaryUser()) {
                    titleResId = R.string.message_title_manual_download;
                    if (isSelected()) {
                        statusResId = R.string.message_status_download_action;
                    } else {
                        statusResId = R.string.message_status_download;
                    }
                }
                break;

            case MessageData.BUGLE_STATUS_INCOMING_EXPIRED_OR_NOT_AVAILABLE:
                if (!OsUtil.isSecondaryUser()) {
                    titleResId = R.string.message_title_download_failed;
                    statusResId = R.string.message_status_download_error;
                }
                break;

            case MessageData.BUGLE_STATUS_INCOMING_DOWNLOAD_FAILED:
                if (!OsUtil.isSecondaryUser()) {
                    titleResId = R.string.message_title_download_failed;
                    if (isSelected()) {
                        statusResId = R.string.message_status_download_action;
                    } else {
                        statusResId = R.string.message_status_download;
                    }
                }
                break;

            case MessageData.BUGLE_STATUS_OUTGOING_YET_TO_SEND:
            case MessageData.BUGLE_STATUS_OUTGOING_SENDING:
                //Bkav QuangNDb doi text sending ... thanh khong co gi
//                statusResId = R.string.message_status_sending;
                statusResId = getOutgoingAwaitOrSendingStatusText();
                break;

            case MessageData.BUGLE_STATUS_OUTGOING_RESENDING:
            case MessageData.BUGLE_STATUS_OUTGOING_AWAITING_RETRY:
                //Bkav QuangNDb doi text retry thanh khong co gi
//                statusResId = R.string.message_status_send_retrying;
                statusResId = getOutgoingAwaitOrSendingStatusText();
                break;

            case MessageData.BUGLE_STATUS_OUTGOING_FAILED_EMERGENCY_NUMBER:
                statusResId = R.string.message_status_send_failed_emergency_number;
                break;

            case MessageData.BUGLE_STATUS_OUTGOING_FAILED:
                // don't show the error state unless we're the default sms app
                if (PhoneUtils.getDefault().isDefaultSmsApp()) {
                    if (isSelected()) {
                        statusResId = R.string.message_status_resend;
                    } else {
                        statusResId = MmsUtils.mapRawStatusToErrorResourceId(mData.getStatus(), mData.getRawTelephonyStatus());
                    }
                    break;
                }
                // FALL THROUGH HERE

            case MessageData.BUGLE_STATUS_OUTGOING_COMPLETE:
            case MessageData.BUGLE_STATUS_INCOMING_COMPLETE:
            default:
                // Bkav Quangndb them ham hide timestamp
                hideTimeStamp();
                if (getConditionShowTimestamp()) {
                    statusText = mData.getFormattedReceivedTimeStamp();
                }
                // Bkav QuangNdB them ham danh dau 1 tin nhan da hoan thanh
                setCompletedMessage();
                break;
        }

        final boolean titleVisible = (titleResId >= 0);
        if (titleVisible) {
            final String titleText = getResources().getString(titleResId);
            mTitleTextView.setText(titleText);

            final String mmsInfoText = getResources().getString(
                    R.string.mms_info,
                    Formatter.formatFileSize(getContext(), mData.getSmsMessageSize()),
                    DateUtils.formatDateTime(
                            getContext(),
                            mData.getMmsExpiry(),
                            DateUtils.FORMAT_SHOW_DATE |
                            DateUtils.FORMAT_SHOW_TIME |
                            DateUtils.FORMAT_NUMERIC_DATE |
                            DateUtils.FORMAT_NO_YEAR));
            mMmsInfoTextView.setText(mmsInfoText);
            mMessageTitleLayout.setVisibility(View.VISIBLE);
        } else {
            mMessageTitleLayout.setVisibility(View.GONE);
        }

        final String subjectText = MmsUtils.cleanseMmsSubject(getResources(),
                mData.getMmsSubject());
        final boolean subjectVisible = !TextUtils.isEmpty(subjectText);

        final boolean senderNameVisible = !mOneOnOne && !mData.getCanClusterWithNextMessage()
                && mData.getIsIncoming();
        if (senderNameVisible) {
            mSenderNameTextView.setText(mData.getSenderDisplayName());
            mSenderNameTextView.setVisibility(View.VISIBLE);
        } else {
            mSenderNameTextView.setVisibility(View.GONE);
        }

        if (statusResId >= 0) {
            statusText = getResources().getString(statusResId);
        }

        // We set the text even if the view will be GONE for accessibility
        mStatusTextView.setText(statusText);
        final boolean statusVisible = !TextUtils.isEmpty(statusText);
        // Bkav QuangNDb tach code doan nay de override lai o class con
        visibleStatusText(statusVisible);
        final boolean deliveredBadgeVisible =
                mData.getStatus() == MessageData.BUGLE_STATUS_OUTGOING_DELIVERED;
        // Bkav QuangNDb visible delivery report view
        visibleDeliveryReportView(deliveredBadgeVisible);

        // Update the sim indicator.
        final boolean showSimIconAsIncoming = mData.getIsIncoming() &&
                (!mData.hasAttachments() || shouldShowMessageTextBubble());
        final SubscriptionListEntry subscriptionEntry =
                mHost.getSubscriptionEntryForSelfParticipant(mData.getSelfParticipantId(),
                        true /* excludeDefault */);
        final boolean simNameVisible = subscriptionEntry != null &&
                !TextUtils.isEmpty(subscriptionEntry.displayName) &&
                !mData.getCanClusterWithNextMessage();
        if (simNameVisible) {
            // Bkav QuangNDb tach code doan set sim name de override lai
            showSimName(subscriptionEntry, showSimIconAsIncoming);
        } else {
            // Bkav QuangNDb tach code doan hide simname
            hideSimName();
        }

        final boolean metadataVisible = senderNameVisible || statusVisible
                || deliveredBadgeVisible || simNameVisible;
        mMessageMetadataView.setVisibility(metadataVisible ? View.VISIBLE : View.GONE);

        final boolean messageTextAndOrInfoVisible = titleVisible || subjectVisible
                || mData.hasText() || metadataVisible;
        mMessageTextAndInfoView.setVisibility(
                messageTextAndOrInfoVisible ? View.VISIBLE : View.GONE);

        if (shouldShowIcon()) {
//            mContactIconView.setVisibility(View.GONE);

            // Bkav QuangNDb hide photoview khi can
            setPhotoViewVisibility(View.GONE);
        } else {
            // Bkav QuangNDb show photoview khi can
            setPhotoViewVisibility(View.VISIBLE);
            final Uri avatarUri = AvatarUriUtil.createAvatarUri(
                    mData.getSenderProfilePhotoUri(),
                    mData.getSenderFullName(),
                    mData.getSenderNormalizedDestination(),
                    mData.getSenderContactLookupKey());
            setContactImageResourceUri(avatarUri);
        }
    }

    protected int getOutgoingAwaitOrSendingStatusText(){ // Bkav HuyNQN su ly lai o lop con
       return R.string.message_status_send_retrying;
    }



    /**
     * Bkav QuangNDb tach code doan set hide show status text
     */
    protected void visibleStatusText(boolean statusVisible) {
        if (statusVisible) {
            mStatusTextView.setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setVisibility(View.GONE);
        }
    }


    private void updateMessageContent() {
        // We must update the text before the attachments since we search the text to see if we
        // should make a preview youtube image in the attachments
        updateMessageText();
        updateMessageAttachments();
        updateMessageSubject();
        mMessageBubble.bind(mData);
    }

    private void updateMessageAttachments() {
        // Bind video, audio, and VCard attachments. If there are multiple, they stack vertically.
        bindAttachmentsOfSameType(sVideoFilter,
                R.layout.message_video_attachment, mVideoViewBinder, VideoThumbnailView.class);
        // Bkav QuangNDb tach code doan bin attch audio
        binAudioAttachment();
        bindAttachmentsOfSameType(sVCardFilter,
                R.layout.message_vcard_attachment, mVCardViewBinder, PersonItemView.class);

        // Bind image attachments. If there are multiple, they are shown in a collage view.
        final List<MessagePartData> imageParts = mData.getAttachments(sImageFilter);
        if (imageParts.size() > 1) {
            Collections.sort(imageParts, sImageComparator);
            mMultiAttachmentView.bindAttachments(imageParts, null, imageParts.size());
            mMultiAttachmentView.setVisibility(View.VISIBLE);
        } else {
            mMultiAttachmentView.setVisibility(View.GONE);
        }

        // In the case that we have no image attachments and exactly one youtube link in a message
        // then we will show a preview.
        String youtubeThumbnailUrl = null;
        String originalYoutubeLink = null;
        if (mMessageTextHasLinks && imageParts.size() == 0) {
            CharSequence messageTextWithSpans = mMessageTextView.getText();
            final URLSpan[] spans = ((Spanned) messageTextWithSpans).getSpans(0,
                    messageTextWithSpans.length(), URLSpan.class);
            for (URLSpan span : spans) {
                String url = span.getURL();
                String youtubeLinkForUrl = YouTubeUtil.getYoutubePreviewImageLink(url);
                if (!TextUtils.isEmpty(youtubeLinkForUrl)) {
                    if (TextUtils.isEmpty(youtubeThumbnailUrl)) {
                        // Save the youtube link if we don't already have one
                        youtubeThumbnailUrl = youtubeLinkForUrl;
                        originalYoutubeLink = url;
                    } else {
                        // We already have a youtube link. This means we have two youtube links so
                        // we shall show none.
                        youtubeThumbnailUrl = null;
                        originalYoutubeLink = null;
                        break;
                    }
                }
            }
        }
        // We need to keep track if we have a youtube link in the message so that we will not show
        // the arrow
        mMessageHasYouTubeLink = !TextUtils.isEmpty(youtubeThumbnailUrl);

        // We will show the message image view if there is one attachment or one youtube link
        if (imageParts.size() == 1 || mMessageHasYouTubeLink) {
            // Get the display metrics for a hint for how large to pull the image data into
            final WindowManager windowManager = (WindowManager) getContext().
                    getSystemService(Context.WINDOW_SERVICE);
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);

            final int iconSize = getResources()
                    .getDimensionPixelSize(R.dimen.conversation_message_contact_icon_size);
            final int desiredWidth = displayMetrics.widthPixels - iconSize - iconSize;

            if (imageParts.size() == 1) {
                final MessagePartData imagePart = imageParts.get(0);
                // If the image is big, we want to scale it down to save memory since we're going to
                // scale it down to fit into the bubble width. We don't constrain the height.
                final ImageRequestDescriptor imageRequest =
                        new MessagePartImageRequestDescriptor(imagePart,
                                desiredWidth,
                                MessagePartData.UNSPECIFIED_SIZE,
                                false);
                adjustImageViewBounds(imagePart);
                mMessageImageView.setImageResourceId(imageRequest);
                mMessageImageView.setTag(imagePart);
            } else {
                // Youtube Thumbnail image
                final ImageRequestDescriptor imageRequest =
                        new UriImageRequestDescriptor(Uri.parse(youtubeThumbnailUrl), desiredWidth,
                            MessagePartData.UNSPECIFIED_SIZE, true /* allowCompression */,
                            true /* isStatic */, false /* cropToCircle */,
                            ImageUtils.DEFAULT_CIRCLE_BACKGROUND_COLOR /* circleBackgroundColor */,
                            ImageUtils.DEFAULT_CIRCLE_STROKE_COLOR /* circleStrokeColor */);
                mMessageImageView.setImageResourceId(imageRequest);
                mMessageImageView.setTag(originalYoutubeLink);
            }
            mMessageImageView.setVisibility(View.VISIBLE);
        } else {
            mMessageImageView.setImageResourceId(null);
            mMessageImageView.setVisibility(View.GONE);
        }

        // Show the message attachments container if any of its children are visible
        boolean attachmentsVisible = false;
        for (int i = 0, size = mMessageAttachmentsView.getChildCount(); i < size; i++) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(i);
            if (attachmentView.getVisibility() == View.VISIBLE) {
                attachmentsVisible = true;
                break;
            }
        }
        mMessageAttachmentsView.setVisibility(attachmentsVisible ? View.VISIBLE : View.GONE);
    }


    protected void bindAttachmentsOfSameType(final Predicate<MessagePartData> attachmentTypeFilter,
            final int attachmentViewLayoutRes, final AttachmentViewBinder viewBinder,
            final Class<?> attachmentViewClass) {
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        // Iterate through all attachments of a particular type (video, audio, etc).
        // Find the first attachment index that matches the given type if possible.
        int attachmentViewIndex = -1;
        View existingAttachmentView;
        do {
            existingAttachmentView = mMessageAttachmentsView.getChildAt(++attachmentViewIndex);
        } while (existingAttachmentView != null &&
                !(attachmentViewClass.isInstance(existingAttachmentView)));

        for (final MessagePartData attachment : mData.getAttachments(attachmentTypeFilter)) {
            View attachmentView = mMessageAttachmentsView.getChildAt(attachmentViewIndex);
            if (!attachmentViewClass.isInstance(attachmentView)) {
                attachmentView = layoutInflater.inflate(attachmentViewLayoutRes,
                        mMessageAttachmentsView, false /* attachToRoot */);
                attachmentView.setOnClickListener(this);
                attachmentView.setOnLongClickListener(this);
                mMessageAttachmentsView.addView(attachmentView, attachmentViewIndex);
            }
            viewBinder.bindView(attachmentView, attachment);
            attachmentView.setTag(attachment);
            attachmentView.setVisibility(View.VISIBLE);
            attachmentViewIndex++;
        }
        // If there are unused views left over, unbind or remove them.
        while (attachmentViewIndex < mMessageAttachmentsView.getChildCount()) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(attachmentViewIndex);
            if (attachmentViewClass.isInstance(attachmentView)) {
                mMessageAttachmentsView.removeViewAt(attachmentViewIndex);
            } else {
                // No more views of this type; we're done.
                break;
            }
        }
    }

    private void updateMessageSubject() {
        final String subjectText = MmsUtils.cleanseMmsSubject(getResources(),
                mData.getMmsSubject());
        final boolean subjectVisible = !TextUtils.isEmpty(subjectText);

        if (subjectVisible) {
            mSubjectText.setText(subjectText);
            mSubjectView.setVisibility(View.VISIBLE);
        } else {
            mSubjectView.setVisibility(View.GONE);
        }
    }

    private void updateMessageText() {
        final String text = mData.getText();
        if (!TextUtils.isEmpty(text)) {
            //Bkav QuangNDb insert contact Name and setText
            // Bkav QuangNDb tach code doan set message text
            setMessageText(text);
            // Linkify phone numbers, web urls, emails, and map addresses to allow users to
            // click on them and take the default intent.
            addLinkTextIfExist();
            mMessageTextView.setVisibility(View.VISIBLE);
        } else {
            mMessageTextView.setVisibility(View.GONE);
            mMessageTextHasLinks = false;
        }
    }



    private void updateViewAppearance() {
        final Resources res = getResources();
        final ConversationDrawables drawableProvider = getConversationDrawables();
        final boolean incoming = mData.getIsIncoming();
        final boolean outgoing = !incoming;
        final boolean showArrow =  shouldShowMessageBubbleArrow();

        // Bkav TienNAb: set gravity cho text_message va metaData_view
        LinearLayout.LayoutParams messageTextAndInfoViewParams = (LinearLayout.LayoutParams) mMessageTextAndInfoView.getLayoutParams();
        LinearLayout.LayoutParams messageMetadataViewParams = (LinearLayout.LayoutParams) mMessageMetadataView.getLayoutParams();
        if (incoming){
            messageTextAndInfoViewParams.gravity = Gravity.LEFT;
            messageMetadataViewParams.gravity = Gravity.LEFT;
        } else if (outgoing) {
            messageTextAndInfoViewParams.gravity = Gravity.RIGHT;
            messageMetadataViewParams.gravity = Gravity.RIGHT;
        }
        mMessageTextAndInfoView.setLayoutParams(messageTextAndInfoViewParams);
        mMessageMetadataView.setLayoutParams(messageMetadataViewParams);

        final int messageTopPaddingClustered =
                res.getDimensionPixelSize(getIdResDimenPaddingCluster());
        final int messageTopPaddingDefault =
                res.getDimensionPixelSize(getIdResDimenPaddingTopDefault());
        final int arrowWidth = res.getDimensionPixelOffset(R.dimen.message_bubble_arrow_width);
        // Bkav QuangNDb tach code de change lai min height cua box tin nhan
        final int messageTextMinHeightDefault = res.getDimensionPixelSize(
                getIdResIconSize());
        final int messageTextLeftRightPadding = res.getDimensionPixelOffset(
                getIdResTextLeftPadding());
        final int textTopPaddingDefault = res.getDimensionPixelOffset(
                getIdResTextTopPadding());
        final int textBottomPaddingDefault = res.getDimensionPixelOffset(
                getIdResTextBotPadding());

        // These values depend on whether the message has text, attachments, or both.
        // We intentionally don't set defaults, so the compiler will tell us if we forget
        // to set one of them, or if we set one more than once.
        final int contentLeftPadding, contentRightPadding;
        final Drawable textBackground;
        final int textMinHeight;
        final int textTopMargin;
        final int textTopPadding, textBottomPadding;
        final int textLeftPadding, textRightPadding;

        if (mData.hasAttachments()) {
            if (shouldShowMessageTextBubble()) {
                // Text and attachment(s)
                contentLeftPadding = incoming ? arrowWidth : 0;
                contentRightPadding = outgoing ? arrowWidth : 0;
//                textBackground = drawableProvider.getBubbleDrawable(
//                        isSelected(),
//                        incoming,
//                        false /* needArrow */,
//                        mData.hasIncomingErrorStatus());
                // Bkav QuangNDb tach code doan get bubble drawable cho box tin nhan
                textBackground = getBubbleDrawable(drawableProvider,isSelected(), incoming, false,
                        mData.hasIncomingErrorStatus(), mData.getCanClusterWithPreviousMessage(),
                        mData.getCanClusterWithNextMessage());
                textMinHeight = messageTextMinHeightDefault;
                textTopMargin = messageTopPaddingClustered;
                textTopPadding = textTopPaddingDefault;
                textBottomPadding = textBottomPaddingDefault;
                textLeftPadding = messageTextLeftRightPadding;
                textRightPadding = messageTextLeftRightPadding;
            } else {
                // Attachment(s) only
                contentLeftPadding = incoming ? arrowWidth : 0;
                contentRightPadding = outgoing ? arrowWidth : 0;
                textBackground = null;
                textMinHeight = 0;
                textTopMargin = 0;
                textTopPadding = 0;
                textBottomPadding = 0;
                textLeftPadding = 0;
                textRightPadding = 0;
            }
        } else {
            // Text only
            contentLeftPadding = (!showArrow && incoming) ? arrowWidth : 0;
            contentRightPadding = (!showArrow && outgoing) ? arrowWidth : 0;
//            textBackground = drawableProvider.getBubbleDrawable(
//                    isSelected(),
//                    incoming,
//                    shouldShowMessageBubbleArrow(),
//                    mData.hasIncomingErrorStatus());
            // Bkav QuangNDb tach code doan get bubble drawable cho box tin nhan
            textBackground = getBubbleDrawable(drawableProvider,isSelected(), incoming, shouldShowMessageBubbleArrow(),
                    mData.hasIncomingErrorStatus(), mData.getCanClusterWithPreviousMessage(),
                    mData.getCanClusterWithNextMessage());
            textMinHeight = messageTextMinHeightDefault;
            textTopMargin = 0;
            textTopPadding = textTopPaddingDefault;
            textBottomPadding = textBottomPaddingDefault;
            if (showArrow && incoming) {
                textLeftPadding = messageTextLeftRightPadding + arrowWidth;
            } else {
                textLeftPadding = messageTextLeftRightPadding;
            }
            if (showArrow && outgoing) {
                textRightPadding = messageTextLeftRightPadding + arrowWidth;
            } else {
                textRightPadding = messageTextLeftRightPadding;
            }
        }

        // These values do not depend on whether the message includes attachments
        final int gravity = incoming ? (Gravity.START | Gravity.CENTER_VERTICAL) :
                (Gravity.END | Gravity.CENTER_VERTICAL);
        final int messageTopPadding = shouldShowSimplifiedVisualStyle() ?
                messageTopPaddingClustered : messageTopPaddingDefault;

        // Bkav QuangNDb tach code custom lai cho message co padding bot de khong bi vuong voi layout compose nua
//        final int metadataTopPadding = res.getDimensionPixelOffset(
//                R.dimen.message_metadata_top_padding);

        // Update the message text/info views
        ImageUtils.setBackgroundDrawableOnView(mMessageTextAndInfoView, textBackground);
        mMessageTextAndInfoView.setMinimumHeight(textMinHeight);
        // Bkav QuangNDb them ham setMin width cho mMessageTextAndInfoView
        setMinWidthMessageBox(showArrow, arrowWidth);
        final LinearLayout.LayoutParams textAndInfoLayoutParams =
                (LinearLayout.LayoutParams) mMessageTextAndInfoView.getLayoutParams();
        textAndInfoLayoutParams.topMargin = textTopMargin;

        if (UiUtils.isRtlMode()) {
            // Need to switch right and left padding in RtL mode
            mMessageTextAndInfoView.setPadding(textRightPadding, textTopPadding, textLeftPadding,
                    textBottomPadding);
            mMessageBubble.setPadding(contentRightPadding, 0, contentLeftPadding, 0);
        } else {
            mMessageTextAndInfoView.setPadding(textLeftPadding, textTopPadding, textRightPadding,
                    textBottomPadding);
            mMessageBubble.setPadding(contentLeftPadding, 0, contentRightPadding, 0);
        }

        // Update the message row and message bubble views
        setPadding(getPaddingLeft(), messageTopPadding, getPaddingRight(), getPaddingBotMessage());
//        setMessagePadding(messageTopPadding);
        mMessageBubble.setGravity(gravity);
        updateMessageAttachmentsAppearance(gravity);

//        mMessageMetadataView.setPadding(0, metadataTopPadding, 0, 0);

        updateTextAppearance();

        requestLayout();
    }



    private void updateContentDescription() {
        StringBuilder description = new StringBuilder();

        Resources res = getResources();
        String separator = res.getString(R.string.enumeration_comma);

        // Sender information
        boolean hasPlainTextMessage = !(TextUtils.isEmpty(mData.getText()) ||
                mMessageTextHasLinks);
        if (mData.getIsIncoming()) {
            int senderResId = hasPlainTextMessage
                ? R.string.incoming_text_sender_content_description
                : R.string.incoming_sender_content_description;
            description.append(res.getString(senderResId, mData.getSenderDisplayName()));
        } else {
            int senderResId = hasPlainTextMessage
                ? R.string.outgoing_text_sender_content_description
                : R.string.outgoing_sender_content_description;
            description.append(res.getString(senderResId));
        }

        if (mSubjectView.getVisibility() == View.VISIBLE) {
            description.append(separator);
            description.append(mSubjectText.getText());
        }

        if (mMessageTextView.getVisibility() == View.VISIBLE) {
            // If the message has hyperlinks, we will let the user navigate to the text message so
            // that the hyperlink can be clicked. Otherwise, the text message does not need to
            // be reachable.
            if (mMessageTextHasLinks) {
                mMessageTextView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            } else {
                mMessageTextView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                description.append(separator);
                description.append(mMessageTextView.getText());
            }
        }

        if (mMessageTitleLayout.getVisibility() == View.VISIBLE) {
            description.append(separator);
            description.append(mTitleTextView.getText());

            description.append(separator);
            description.append(mMmsInfoTextView.getText());
        }

        if (mStatusTextView.getVisibility() == View.VISIBLE) {
            description.append(separator);
            description.append(mStatusTextView.getText());
        }

        if (mSimNameView.getVisibility() == View.VISIBLE) {
            description.append(separator);
            description.append(mSimNameView.getText());
        }

        if (mDeliveredBadge.getVisibility() == View.VISIBLE) {
            description.append(separator);
            description.append(res.getString(R.string.delivered_status_content_description));
        }

        setContentDescription(description);
    }

    private void updateMessageAttachmentsAppearance(final int gravity) {
        mMessageAttachmentsView.setGravity(gravity);

        // Tint image/video attachments when selected
        final int selectedImageTint = getResources().getColor(R.color.message_image_selected_tint);
        if (mMessageImageView.getVisibility() == View.VISIBLE) {
            if (isSelected()) {
                mMessageImageView.setColorFilter(selectedImageTint);
            } else {
                mMessageImageView.clearColorFilter();
            }
        }
        if (mMultiAttachmentView.getVisibility() == View.VISIBLE) {
            if (isSelected()) {
                mMultiAttachmentView.setColorFilter(selectedImageTint);
            } else {
                mMultiAttachmentView.clearColorFilter();
            }
        }
        for (int i = 0, size = mMessageAttachmentsView.getChildCount(); i < size; i++) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(i);
            if (attachmentView instanceof VideoThumbnailView
                    && attachmentView.getVisibility() == View.VISIBLE) {
                final VideoThumbnailView videoView = (VideoThumbnailView) attachmentView;
                if (isSelected()) {
                    videoView.setColorFilter(selectedImageTint);
                } else {
                    videoView.clearColorFilter();
                }
            }
        }

        // If there are multiple attachment bubbles in a single message, add some separation.
        final int multipleAttachmentPadding =
                getResources().getDimensionPixelSize(R.dimen.message_padding_same_author);

        boolean previousVisibleView = false;
        for (int i = 0, size = mMessageAttachmentsView.getChildCount(); i < size; i++) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(i);
            if (attachmentView.getVisibility() == View.VISIBLE) {
                final int margin = previousVisibleView ? multipleAttachmentPadding : 0;
                ((LinearLayout.LayoutParams) attachmentView.getLayoutParams()).topMargin = margin;
                // updateViewAppearance calls requestLayout() at the end, so we don't need to here
                previousVisibleView = true;
            }
        }
    }

    protected void updateTextAppearance() {
        int messageColorResId;
        int statusColorResId = -1;
        int infoColorResId = -1;
        int timestampColorResId;
        int subjectLabelColorResId;
        if (isSelected()) {
            // Bkav QuangNDb tach code lay mau incoming
            messageColorResId = getSelectedTextColorId();
            statusColorResId = R.color.message_action_status_text;
            infoColorResId = R.color.message_action_info_text;
            if (shouldShowMessageTextBubble()) {
                timestampColorResId = R.color.message_action_timestamp_text;
                subjectLabelColorResId = R.color.message_action_timestamp_text;
            } else {
                // If there's no text, the timestamp will be shown below the attachments,
                // against the conversation view background.
                // Bkav QuangNDb sua lai doan set mau text cho timestamp va subject khi selected
//                timestampColorResId = getTimestampOutgoingTextColorId();
                subjectLabelColorResId = getTimestampOutgoingTextColorId();
                timestampColorResId = getTimestampSelectedTextColorId();
            }
        } else {
            // Bkav Quang Ndb tach code doan lay id color timestamp
            messageColorResId = getMessageColorResId();
            statusColorResId = messageColorResId;
            infoColorResId = getTimestampIncomingTextColorId();
            switch(mData.getStatus()) {

                case MessageData.BUGLE_STATUS_OUTGOING_FAILED:
                case MessageData.BUGLE_STATUS_OUTGOING_FAILED_EMERGENCY_NUMBER:
                    timestampColorResId = R.color.message_failed_timestamp_text;
                    subjectLabelColorResId = getTimestampOutgoingTextColorId();
                    break;

                case MessageData.BUGLE_STATUS_OUTGOING_YET_TO_SEND:
                case MessageData.BUGLE_STATUS_OUTGOING_SENDING:
                case MessageData.BUGLE_STATUS_OUTGOING_RESENDING:
                case MessageData.BUGLE_STATUS_OUTGOING_AWAITING_RETRY:
                case MessageData.BUGLE_STATUS_OUTGOING_COMPLETE:
                case MessageData.BUGLE_STATUS_OUTGOING_DELIVERED:
                    timestampColorResId = getTimestampOutgoingTextColorId();
                    subjectLabelColorResId = getTimestampOutgoingTextColorId();
                    break;

                case MessageData.BUGLE_STATUS_INCOMING_EXPIRED_OR_NOT_AVAILABLE:
                case MessageData.BUGLE_STATUS_INCOMING_DOWNLOAD_FAILED:
                    messageColorResId = R.color.message_text_color_incoming_download_failed;
                    timestampColorResId = R.color.message_download_failed_timestamp_text;
                    subjectLabelColorResId = R.color.message_text_color_incoming_download_failed;
                    statusColorResId = R.color.message_download_failed_status_text;
                    infoColorResId = R.color.message_info_text_incoming_download_failed;
                    break;

                case MessageData.BUGLE_STATUS_INCOMING_AUTO_DOWNLOADING:
                case MessageData.BUGLE_STATUS_INCOMING_MANUAL_DOWNLOADING:
                case MessageData.BUGLE_STATUS_INCOMING_RETRYING_AUTO_DOWNLOAD:
                case MessageData.BUGLE_STATUS_INCOMING_RETRYING_MANUAL_DOWNLOAD:
                case MessageData.BUGLE_STATUS_INCOMING_YET_TO_MANUAL_DOWNLOAD:
                    //Bkav QuangNDB tach code doan lay id incoming color
                    timestampColorResId = getMessageIncomingTextColorId();
                    subjectLabelColorResId = getMessageIncomingTextColorId();
                    infoColorResId = getTimestampIncomingTextColorId();
                    break;

                case MessageData.BUGLE_STATUS_INCOMING_COMPLETE:
                default:
                    timestampColorResId = getTimestampIncomingTextColorId();
                    subjectLabelColorResId = getTimestampIncomingTextColorId();
                    infoColorResId = -1; // Not used
                    break;
            }
        }
        final int messageColor = getResources().getColor(messageColorResId);
        mMessageTextView.setTextColor(messageColor);
        setBodyLinkTextColor(messageColor);
        mSubjectText.setTextColor(messageColor);
        if (statusColorResId >= 0) {
            mTitleTextView.setTextColor(getResources().getColor(statusColorResId));
        }
        if (infoColorResId >= 0) {
            mMmsInfoTextView.setTextColor(getResources().getColor(infoColorResId));
        }
        if (timestampColorResId == getTimestampIncomingTextColorId() &&
                mData.hasAttachments() && !shouldShowMessageTextBubble()) {
            // Bkav QuangNDb tach code doan getId color timestamp
            timestampColorResId = getIdColorTimestampHasAttachments();
        }

//        mStatusTextView.setTextColor(getResources().getColor(timestampColorResId));

        mSubjectLabel.setTextColor(getResources().getColor(subjectLabelColorResId));
        mSenderNameTextView.setTextColor(getResources().getColor(timestampColorResId));

        // Bkav TienNAb: xu ly focus vao cac ket qua co ky tu trung voi tu khoa tim kiem
        // Bkav HienDTk: fix loi: BOS-2826 - Start
        if (mData.getSearchString() != null && mData.getText() != null) {
            // Bkav HienDTk: fix loi: BOS-2826 - End
            ArrayList<Integer> indices = new ArrayList<>();

            int index = convertToNotLatinCode(mData.getText().toLowerCase()).indexOf(mData.getSearchString().toLowerCase());
            while (index >= 0) {
                indices.add(index);
                index = mData.getText().toLowerCase().indexOf(mData.getSearchString().toLowerCase(), index + 1);
            }

            // Make all instances of the search query bold
            SpannableStringBuilder sb = new SpannableStringBuilder(mData.getText());
            for (int i : indices) {
                ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.btalk_orange_color));
                sb.setSpan(span, i, i + mData.getSearchString().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }

            mMessageTextView.setText(sb);
        }
    }

    // Bkav TienNAb: ham chuyen ky tu tieng viet sang tieng anh
    String convertToNotLatinCode(String latinCode) {
        String result = latinCode != null ? Normalizer.normalize(latinCode, Normalizer.Form.NFKD).replaceAll(
                "\\p{InCombiningDiacriticalMarks}+", "") : null;
        // Ham do java convert latin code ben tren chi bi loi chu đ và Đ nên xử lý bằng tay đoạn này
        if (result != null && (result.contains("đ") || result.contains("Đ"))) {
            result = result.replace("đ", "d");
            result = result.replace("Đ", "D");
        }
        return result;
    }



    /**
     * If we don't know the size of the image, we want to show it in a fixed-sized frame to
     * avoid janks when the image is loaded and resized. Otherwise, we can set the imageview to
     * take on normal layout params.
     */
    private void adjustImageViewBounds(final MessagePartData imageAttachment) {
        Assert.isTrue(ContentType.isImageType(imageAttachment.getContentType()));
        final ViewGroup.LayoutParams layoutParams = mMessageImageView.getLayoutParams();
        if (imageAttachment.getWidth() == MessagePartData.UNSPECIFIED_SIZE ||
                imageAttachment.getHeight() == MessagePartData.UNSPECIFIED_SIZE) {
            // We don't know the size of the image attachment, enable letterboxing on the image
            // and show a fixed sized attachment. This should happen at most once per image since
            // after the image is loaded we then save the image dimensions to the db so that the
            // next time we can display the full size.
            layoutParams.width = getResources()
                    .getDimensionPixelSize(R.dimen.image_attachment_fallback_width);
            layoutParams.height = getResources()
                    .getDimensionPixelSize(R.dimen.image_attachment_fallback_height);
            mMessageImageView.setScaleType(ScaleType.CENTER_CROP);
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            // ScaleType.CENTER_INSIDE and FIT_CENTER behave similarly for most images. However,
            // FIT_CENTER works better for small images as it enlarges the image such that the
            // minimum size ("android:minWidth" etc) is honored.
            mMessageImageView.setScaleType(ScaleType.FIT_CENTER);
        }
    }

    @Override
    public void onClick(final View view) {
        final Object tag = view.getTag();
        if (tag instanceof MessagePartData) {
            final Rect bounds = UiUtils.getMeasuredBoundsOnScreen(view);
            onAttachmentClick((MessagePartData) tag, bounds, false /* longPress */);
        } else if (tag instanceof String) {
            // Currently the only object that would make a tag of a string is a youtube preview
            // image
            UIIntents.get().launchBrowserForUrl(getContext(), (String) tag);
        }
    }

    @Override
    public boolean onLongClick(final View view) {
        if (view == mMessageTextView) {
            // Preemptively handle the long click event on message text so it's not handled by
            // the link spans.
            return performLongClick();
        }

        final Object tag = view.getTag();
        if (tag instanceof MessagePartData) {
            final Rect bounds = UiUtils.getMeasuredBoundsOnScreen(view);
            return onAttachmentClick((MessagePartData) tag, bounds, true /* longPress */);
        }

        return false;
    }

    @Override
    public boolean onAttachmentClick(final MessagePartData attachment,
            final Rect viewBoundsOnScreen, final boolean longPress) {
        return mHost.onAttachmentClick(this, attachment, viewBoundsOnScreen, longPress);
    }

    public ContactIconView getContactIconView() {
        return mContactIconView;
    }

    // Sort photos in MultiAttachLayout in the same order as the ConversationImagePartsView
    static final Comparator<MessagePartData> sImageComparator = new Comparator<MessagePartData>(){
        @Override
        public int compare(final MessagePartData x, final MessagePartData y) {
            return x.getPartId().compareTo(y.getPartId());
        }
    };

    static final Predicate<MessagePartData> sVideoFilter = new Predicate<MessagePartData>() {
        @Override
        public boolean apply(final MessagePartData part) {
            return part.isVideo();
        }
    };

    protected static Predicate<MessagePartData> sAudioFilter = new Predicate<MessagePartData>() {
        @Override
        public boolean apply(final MessagePartData part) {
            return part.isAudio();
        }
    };

    static final Predicate<MessagePartData> sVCardFilter = new Predicate<MessagePartData>() {
        @Override
        public boolean apply(final MessagePartData part) {
            return part.isVCard();
        }
    };

    static final Predicate<MessagePartData> sImageFilter = new Predicate<MessagePartData>() {
        @Override
        public boolean apply(final MessagePartData part) {
            return part.isImage();
        }
    };

    interface AttachmentViewBinder {
        void bindView(View view, MessagePartData attachment);
        void unbind(View view);
    }

    final AttachmentViewBinder mVideoViewBinder = new AttachmentViewBinder() {
        @Override
        public void bindView(final View view, final MessagePartData attachment) {
            ((VideoThumbnailView) view).setSource(attachment, mData.getIsIncoming());
        }

        @Override
        public void unbind(final View view) {
            ((VideoThumbnailView) view).setSource((Uri) null, mData.getIsIncoming());
        }
    };

    protected AttachmentViewBinder mAudioViewBinder = new AttachmentViewBinder() {
        @Override
        public void bindView(final View view, final MessagePartData attachment) {
            // Bkav QuangNDb tach code don concat AudioAttachmentView de override lai o class con
            mAudioView = concatAudioAttachmentView(view);
            mAudioView.bindMessagePartData(attachment, mData.getIsIncoming(), isSelected());
            // Bkav QuangNDb thay cach khoi tao ConversationDrawables
            mAudioView.setBackground(getAudioDrawable());
        }

        @Override
        public void unbind(final View view) {
            ((AudioAttachmentView) view).bindMessagePartData(null, mData.getIsIncoming(), false);
        }
    };


    final AttachmentViewBinder mVCardViewBinder = new AttachmentViewBinder() {
        @Override
        public void bindView(final View view, final MessagePartData attachment) {
            final PersonItemView personView = (PersonItemView) view;
            personView.bind(DataModel.get().createVCardContactItemData(getContext(),
                    attachment));
            // Bkav QuangNDb thay cach khoi tao ConversationDrawables
            personView.setBackground(getConversationDrawables().getBubbleDrawable(
                    isSelected(), mData.getIsIncoming(), false /* needArrow */,
                    mData.hasIncomingErrorStatus()));
            final int nameTextColorRes;
            final int detailsTextColorRes;
            if (isSelected()) {
                // Bkav QuangNDb tach code doan lay id color
                nameTextColorRes = getMessageIncomingTextColorId();
                detailsTextColorRes = getMessageIncomingTextColorId();
            } else {
                nameTextColorRes = mData.getIsIncoming() ? getMessageIncomingTextColorId()
                        : getMessageOutgoingTextColorId();
                detailsTextColorRes = mData.getIsIncoming() ? getTimestampIncomingTextColorId()
                        : getTimestampOutgoingTextColorId();
            }
            personView.setNameTextColor(getResources().getColor(nameTextColorRes));
            personView.setDetailsTextColor(getResources().getColor(detailsTextColorRes));
        }

        @Override
        public void unbind(final View view) {
            ((PersonItemView) view).bind(null);
        }
    };

    /**
     * A helper class that allows us to handle long clicks on linkified message text view (i.e. to
     * select the message) so it's not handled by the link spans to launch apps for the links.
     */
    private static class IgnoreLinkLongClickHelper implements OnLongClickListener, OnTouchListener {
        private boolean mIsLongClick;
        private final OnLongClickListener mDelegateLongClickListener;

        /**
         * Ignore long clicks on linkified texts for a given text view.
         * @param textView the TextView to ignore long clicks on
         * @param longClickListener a delegate OnLongClickListener to be called when the view is
         *        long clicked.
         */
        public static void ignoreLinkLongClick(final TextView textView,
                @Nullable final OnLongClickListener longClickListener) {
            final IgnoreLinkLongClickHelper helper =
                    new IgnoreLinkLongClickHelper(longClickListener);
            textView.setOnLongClickListener(helper);
            textView.setOnTouchListener(helper);
        }

        private IgnoreLinkLongClickHelper(@Nullable final OnLongClickListener longClickListener) {
            mDelegateLongClickListener = longClickListener;
        }

        @Override
        public boolean onLongClick(final View v) {
            // Record that this click is a long click.
            mIsLongClick = true;
            if (mDelegateLongClickListener != null) {
                return mDelegateLongClickListener.onLongClick(v);
            }
            return false;
        }

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_UP && mIsLongClick) {
                // This touch event is a long click, preemptively handle this touch event so that
                // the link span won't get a onClicked() callback.
                mIsLongClick = false;
                return true;
            }

            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mIsLongClick = false;
            }
            return false;
        }
    }

    /**
     * ---------------------------------------------------BKAV------------------------------------
     * Bkav QuangNDb: tach code doan khoi tao contact icon view de override lai o lop con
     */
    protected void initContactIconView() {
        mContactIconView = (ContactIconView) findViewById(R.id.conversation_icon);
        mContactIconView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                ConversationMessageView.this.performLongClick();
                return true;
            }
        });
    }

    /**
     * BKav QuangNDb: tach code doan set uri cho contact view de override lai o lop con
     */
    protected void setContactImageResourceUri(Uri avatarUri) {
        mContactIconView.setImageResourceUri(avatarUri, mData.getSenderContactId(),
                mData.getSenderContactLookupKey(), mData.getSenderNormalizedDestination());
    }

    /**
     * BKav QuangNDb: them ham de ve photo view cho giong btalk cu
     */
    protected void measurePhotoView(int iconMeasureSpec) {
        //Bkav QuangNDb khong lam gi, override lai o lop con
    }

    /**
     * BKav QuangNDb: them ham de ve photo view cho giong btalk cu
     */
    protected void layoutPhotoView(int iconLeft, int iconTop, int iconRight, int iconBot) {
        //Bkav QuangNDb khong lam gi, override lai o lop con
    }

    /**
     * BKav QuangNDb: set photo gone hoac visible o lop con
     */
    protected void setPhotoViewVisibility(int state) {
        if (state == View.GONE) {
            mContactIconView.setImageResourceUri(null);
        }else {
            mContactIconView.setVisibility(View.VISIBLE);
        }
        //Bkav QuangNDb khong lam gi, override lai o lop con
    }

    /**
     * Bkav QuangNDb: set mesure cho header layout
     */
    protected void measureHeader(int unspecifiedMeasureSpec) {
        //Bkav QuangNdb khong lam gi custom lai o lop con
    }

    /**
     * Bkav QuangNdb tach code doan set measure cho view group de override o class con
     */
    protected void setMeasuredDimensionForViewGroup(int with, int height) {
        setMeasuredDimension(with, height);
    }

    /**
     * Bkav QuangNDb tach code de override lai o class con
     */
    protected int getIconTop(){
        return getPaddingTop();
    }

    /**
     * Bkav QuangNDb tach code de override lai o class con
     */
    protected void layoutHeader(int right) {
        // Bkav QuangNDb: khong lam gi override lai o lop con
    }

    /**
     * Bkav QuangNDb tach code doan init ConversationDrawables de override lai o class con
     */
    protected ConversationDrawables getConversationDrawables() {
        return ConversationDrawables.get();
    }

    /**
     * Bkav QuangNDb tach code doan set text color Message de override lai o class con
     */
    protected int getMessageColorResId() {
        return (mData.getIsIncoming() ?
                R.color.message_text_color_incoming : R.color.message_text_color_outgoing);
    }

    /**
     * Bkav QuangNDb tach code doan bin audio attachment
     */
    protected void binAudioAttachment() {
        bindAttachmentsOfSameType(sAudioFilter,
                R.layout.message_audio_attachment, mAudioViewBinder, AudioAttachmentView.class);
    }

    /**
     * Bkav QuangNDb tach code doan concat Audio de override lai o class con
     */
    protected AudioAttachmentView concatAudioAttachmentView(View view) {
        return (AudioAttachmentView) view;
    }

    /**
     * BKav QuangNDb tach code doan get color timestamp neu co attchments
     */
    protected int getIdColorTimestampHasAttachments() {
        return R.color.timestamp_text_outgoing;
    }

    /**
     * Bkav QuangNdb tach code doan lay id color incoming
     */
    protected int getMessageIncomingTextColorId() {
        return R.color.message_text_color_incoming;
    }

    /**
     * Bkav QuangNDb tach code doan lay id color outgoing
     */
    protected int getMessageOutgoingTextColorId() {
        return R.color.message_text_color_outgoing;
    }
    /**
     * Bkav QuangNdb tach code doan lay id color incoming
     */
    protected int getTimestampIncomingTextColorId() {
        return R.color.timestamp_text_incoming;
    }

    /**
     * Bkav QuangNDb tach code doan lay id color outgoing
     */
    protected int getTimestampOutgoingTextColorId() {
        return R.color.timestamp_text_outgoing;
    }

    /**
     * check xem 1 tin nhan co phai la tin nhan fail hay k
     */
    protected boolean isMessageFail() {
        return mData.getStatus() == MessageData.BUGLE_STATUS_OUTGOING_FAILED ||
        mData.getStatus() == MessageData.BUGLE_STATUS_OUTGOING_FAILED_EMERGENCY_NUMBER;
    }
    /**
     * BKav QuangNDb tach code doan set selected
     */
    protected void setMessageSelected(String selectedMessageId) {
        setSelected(TextUtils.equals(mData.getMessageId(), selectedMessageId));
    }

    /**
     * Bkav QuangNDb them ham setclickable cho message text view
     */
    public void setMessageTextViewClickable(boolean clickAble){
        mMessageTextView.setClickable(clickAble);
        mMessageTextView.setLinksClickable(clickAble);
    }

    /**
     * BKav QuangNDb tach code doan init message textview de override lai o class con
     */
    protected void initMessageTextView() {
        mMessageTextView = (TextView) findViewById(R.id.message_text);
    }

    /**
     * BKav QuangNDb tach code doan setMessage text de override lai o class con
     */
    protected void setMessageText(CharSequence text) {
        mMessageTextView.setText(text);
    }

    /**
     * Bkav QuangNDb tach code doan hide sim name
     */
    protected void hideSimName() {
        mSimNameView.setText(null);
        mSimNameView.setVisibility(GONE);
    }

    /**
     * Bkav QuangNDb tach code doan show sim name
     */
    protected void showSimName(SubscriptionListEntry subscriptionEntry, boolean showSimIconAsIncoming) {
        final String simNameText = mData.getIsIncoming() ? getResources().getString(
                R.string.incoming_sim_name_text, subscriptionEntry.displayName) :
                subscriptionEntry.displayName;
        mSimNameView.setText(simNameText);
        mSimNameView.setTextColor(showSimIconAsIncoming ? getResources().getColor(
                R.color.timestamp_text_incoming) : subscriptionEntry.displayColor);
        mSimNameView.setVisibility(VISIBLE);
    }

    /**
     * Bkav QuangNDB tach code doan setLinks text de override lai
     */
    protected void setBodyLinkTextColor(int messageColor) {
        mMessageTextView.setLinkTextColor(messageColor);
    }

    /**
     * Bkav QuangNDb them ham lay mau chu cua tin nhan khi select
     */
    protected int getSelectedTextColorId() {
        return R.color.message_text_color_incoming;
    }

    /**
     * Bkav QuangNDb hide timestamp khi chua click vao tin nhan
     */
    protected void hideTimeStamp() {
        // Bkav QuangNDb khong lam gi custom lai o class con
    }

    /**
     * Bkav Quangndb tach code ham an hien DeliveryReportView de custom lai class con
     */
    protected void visibleDeliveryReportView(boolean deliveredBadgeVisible) {
        mDeliveredBadge.setVisibility(deliveredBadgeVisible ? View.VISIBLE : View.GONE);
    }

//    Bkav QuangNDb: show or hide sub info when click
    public void showOrHideSubInfoWhenClick() {
    }

    /**
     * Bkav QuangNDb tach code de custom lai size icon
     */
    protected int getIdResIconSize() {
        return R.dimen.conversation_message_contact_icon_size;
    }

    /**
     * Bkav QuangNDb tach code doan setpadding cua tin nhan de custom lai
     */
    protected int getMaxLeftoverSpace(int horizontalSpace, int arrowWidth) {
        return horizontalSpace - mContactIconView.getMeasuredWidth() * 2
                - arrowWidth - getPaddingLeft() - getPaddingRight();
    }

    /**
     * Bkav QuangNDb set Min width cho message box
     */
    protected void setMinWidthMessageBox(boolean showArrow, int arrowWidth) {
        // Bkav QuangNDb setMin width cho message box o class con
    }

    /**
     * Bkav QuangNDb tach code doan get condition show timestamp cua moi tin nhan
     */
    protected boolean getConditionShowTimestamp() {
        return !mData.getCanClusterWithNextMessage();
    }

    /**
     * Bkav QuangNDb tach code doan get id res text top padding
     */
    protected int getIdResTextBotPadding() {
        return R.dimen.message_text_bottom_padding;
    }

    /**
     * Bkav QuangNDb tach code doan get id res text top padding
     */
    protected int getIdResTextTopPadding() {
        return R.dimen.message_text_top_padding;
    }

    /**
     * Bkav QuangNDb tach code doan get id res text left/right padding
     */
    protected int getIdResTextLeftPadding() {
        return R.dimen.message_text_left_right_padding;
    }

    /**
     * Bkav QuangNDb tach code doan get id res text left/right padding
     */
    protected int getIdResTextRightPadding() {
        return R.dimen.message_text_left_right_padding;
    }

    /**
     * Bkav QuangNDb danh dau 1 tin nhan da hoan thanh(khong loi, khong resend, khong chua gui dc)
     */
    protected void setCompletedMessage() {
        // Bkav QuangNDb khong lam gi
    }

    protected MultiAttachmentLayout getMultiAttachmentView() {
        return (MultiAttachmentLayout) findViewById(R.id.multiple_attachments);
    }

    /**
     * Bkav QuangNDb tach code doan set padding 2 tin nhan cluster
     */
    protected int getIdResDimenPaddingCluster() {
        return R.dimen.message_padding_same_author;
    }

    /**
     * Bkav QuangNDb tach code doan add link text de override lai
     */
    protected void addLinkTextIfExist() {
        mMessageTextHasLinks = Linkify.addLinks(mMessageTextView, Linkify.ALL);
    }

    /**
     * Bkav QUangNDb them ham set padding bot cho tin nhan
     */
    protected int getPaddingBotMessage() {
        return 0;
    }

    /**
     * Bkav QUangNDb tach code doan set padding top cho tin nhan
     */
    protected int getIdResDimenPaddingTopDefault() {
        return R.dimen.message_padding_default;
    }

    protected void setMessagePadding(int messageTopPadding) {
        setPadding(getPaddingLeft(), messageTopPadding, getPaddingRight(), 0);
    }

    /**
     * Bkav QuangNDb get message text view
     */
    public TextView getMessageTextView() {
        return mMessageTextView;
    }

    /**
     * Bkav QuangNDb init click message
     */
    protected void initMessageClick() {
//        mMessageTextView.setOnClickListener(this);
    }

    /**
     * Bkav QuangNDb tach code doan measure icon
     */
    protected void measureIcon(int iconMeasureSpec) {
        mContactIconView.measure(iconMeasureSpec, iconMeasureSpec);
        // Bkav QuangNdb them ham measure lai photoview
        measurePhotoView(iconMeasureSpec);
    }

    /**
     * Bkav QuangNDB get max ion height
     */
    protected int getMaxIconHeight() {
        return mContactIconView.getMeasuredHeight();
    }

    protected int getIconMeasureHeight() {
        return mContactIconView.getMeasuredHeight();
    }

    protected int getIconMeasureWidth() {
        return mContactIconView.getMeasuredWidth();
    }

    protected void layoutIcon(int iconLeft, int iconTop, int iconWidth, int iconHeight) {
        layoutPhotoView(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconHeight);
        mContactIconView.layout(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconHeight);
    }

    /**
     * Bkav QuangNDB tach code doan get bubble message de override la o class con
     */
    protected Drawable getBubbleDrawable(ConversationDrawables drawableProvider, boolean selected, boolean incoming, boolean needArrow,
                                       boolean isError, boolean isPreCluster, boolean isNextCluster) {
        return drawableProvider.getBubbleDrawable(
                isSelected(),
                incoming,
                false /* needArrow */,
                mData.hasIncomingErrorStatus());
    }

    /**
     * Bkav QuangNDb get mau text timestamp text color khi selected
     */
    protected int getTimestampSelectedTextColorId() {
        return R.color.timestamp_text_outgoing;
    }

    /**
     * Bkav QuangNDb get audio drawable cho giong nhu box
     */
    protected Drawable getAudioDrawable() {
        return getConversationDrawables().getBubbleDrawable(
                isSelected(), mData.getIsIncoming(), false /* needArrow */,
                mData.hasIncomingErrorStatus());
    }

    public void unbind() {
        mHost = null;
    }
}
