package bkav.android.btalk.messaging.ui.conversationlist;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.data.SubscriptionListData;
import com.android.messaging.ui.conversationlist.ConversationListItemView;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.TextUtil;
import com.android.messaging.util.UiUtils;

import java.util.ArrayList;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.messaging.custom_view.BtalkSimIconView;
import bkav.android.btalk.messaging.datamodel.data.BtalkConversationListItemData;
import bkav.android.btalk.messaging.ui.BtalkContactPhotoView;
import bkav.android.btalk.messaging.util.BtalkCharacterUtil;
import bkav.android.btalk.messaging.util.BtalkIconParser;
import bkav.android.btalk.messaging.util.BtalkTypefaces;

/**
 * Created by quangnd on 27/03/2017.
 * class custom lai ConversationListItemView cua source goc
 */

public class BtalkConversationListItemView extends ConversationListItemView {

    private Context mContext;

    private BtalkContactPhotoView mContactPhotoView;
    private BtalkSimIconView mSimView;

    public BtalkConversationListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mData = new BtalkConversationListItemData();
    }

    @Override
    protected boolean isContactIconView(View v) {
        return v == mContactPhotoView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        mListItemReadTypeface = BtalkTypefaces.sRobotoLightFont;
//        mListItemReadTypeface = Typeface.create("sans-serif-light", Typeface.NORMAL);
        mListItemReadTypeface = Typeface.create("sans-serif-light", Typeface.NORMAL);
        mSimView = (BtalkSimIconView) findViewById(R.id.icon_sim);
    }

    @Override
    public void bind(Cursor cursor, HostInterface hostInterface) {
        super.bind(cursor, hostInterface);
    }

    @Override
    protected void initContactItemView() {
//        mContactIconView = (BtalkContactIconView) findViewById(R.id.conversation_icon);
        mContactPhotoView = (BtalkContactPhotoView) findViewById(R.id.conversation_icon);
    }

    @Override
    protected void setSubject() {
        //Bkav QuangNDb khong lam gi vi sua thanh giong giao dien btalk cu khong co phan nay
    }

    @Override
    protected void setTimeStamp(Resources resources, boolean isDefaultSmsApp, int typefaceStyle) {
        // don't show the error state unless we're the default sms app
//        mTimestampTextView.setTypeface(BtalkTypefaces.sRobotoLightFont);
        final String formattedTimestamp = mData.getFormattedTimestamp();
        mTimestampTextView.setText(formattedTimestamp);
    }

    @Override
    protected void setBackgroundColorNormal() {
        setBackgroundColor(ContextCompat.getColor(mContext, R.color.btalk_transparent_view));
    }

    @Override
    protected void setBackgroundColorSelected() {
        setBackgroundColor(ContextCompat.getColor(mContext, R.color.conversation_pressed_color));
    }


    @Override
    protected void setConversationNameType() {
//        mConversationNameView.setTypeface(BtalkTypefaces.sRobotoRegularFont);
    }

    @Override
    protected View getPhotoView() {
        return mContactPhotoView;
    }

    @Override
    protected void setUpUriPhotoView(Uri iconUri) {
        mContactPhotoView.setImageResourceUriFromConversation(iconUri, mData.getParticipantContactId(),
                mData.getParticipantLookupKey(), mData.getOtherParticipantNormalizedDestination()
                , mData.getConversationId(), mData.getName());

    }


    @Override
    protected void setSnippetMaxLines(int maxLines) {
        mSnippetTextView.setMaxLines(1);
    }

    @Override
    protected void initSubjectTextView() {
        // Khong lam gi
    }

    protected void initCrossSwipeBackground() {
        // Khong lam gi
    }

    protected void initCrossLeft() {
        // Khong lam  gi
    }

    protected void initCrossRight() {
        // Khong lam gi
    }

    @Override
    public void setSwipeTranslationX(float translationX) {
        // Khong lam gi
    }

    @Override
    protected void setSnippetAndSubjectColor(int color) {
        mSnippetTextView.setTextColor(color);
    }

    @Override
    protected void setSnippetAndSubjectTypeFace(Typeface typeface, int typefaceStyle) {
        mSnippetTextView.setTypeface(typeface, typefaceStyle);
    }

    @Override
    protected CharSequence getSnippetText() {
        CharSequence text = super.getSnippetText();
        CharSequence result = BtalkIconParser.getInstance().addSmileySpansWithTextSize(text, mSnippetTextView.getLineHeight());
        return result == null ? text : result;
    }

    @Override
    protected void initAudioAttachmentView() {
        // khong lam gi
    }

    @Override
    protected void binAudioAttachmentView(Uri previewUri, boolean incoming) {
        // KHONG LAM GI
    }

    @Override
    protected void setClickAudioAttachmentView(int audioPreviewVisiblity) {
        // Khong lam gi
    }

    @Override
    protected void setConversationNameWithFormat(String bidiFormattedName) {
        if (mQuery != null && !TextUtils.isEmpty(mQuery)) {
            ArrayList<Integer> indices = new ArrayList<>();
            int index = BtalkCharacterUtil.get().convertToNotLatinCode(bidiFormattedName).toLowerCase().indexOf(mQuery.toLowerCase());
            while (index >= 0) {
                indices.add(index);
                index = bidiFormattedName.toLowerCase().indexOf(mQuery.toLowerCase(), index + 1);
            }

            // Make all instances of the search query bold
            SpannableStringBuilder sb = new SpannableStringBuilder(bidiFormattedName);
            for (int i : indices) {
                ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.btalk_orange_color));
                sb.setSpan(span, i, i + mQuery.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            mConversationNameView.setText(sb);
        }else {
            super.setConversationNameWithFormat(bidiFormattedName);
        }
    }

    @Override
    protected void setSimIconVew() {
        if (shouldShowInfoSim()) {
            final SubscriptionListData.SubscriptionListEntry subscriptionEntry =
                    mHostInterface.getSubscriptionEntryForSelfParticipant(mData.getSelfId(),
                            false /* excludeDefault */);
            // QuangNDB sua dieu kien hien sim info
//            final boolean simNameVisible = subscriptionEntry != null &&
//                    !TextUtils.isEmpty(subscriptionEntry.displayName);
            // QuangNDB sua dieu kien hien sim info
            final boolean simNameVisible = BtalkCallLogCache.getCallLogCache(mContext).isHasSimOnAllSlot() || ESimUtils.isMultiProfile();
            if (simNameVisible) {
//                final Uri iconUri = subscriptionEntry.iconUri;
//                if (iconUri != null) {
//                    mSimView.setVisibility(VISIBLE);
//                    if (subscriptionEntry.iccid != null) {
//                        mSimView.setImageDrawable(BtalkCallLogCache.getCallLogCache(getContext()).getSimIconWithIccid(subscriptionEntry.iccid));
//                    }else {
//                        mSimView.setVisibility(GONE);
//                    }
//                }
                //Bkav QuangNDb doi logic dung iccid trong sim
                mSimView.setVisibility(VISIBLE);
                mSimView.setImageDrawable(BtalkCallLogCache.getCallLogCache(mContext).getSimIconWithIccid(mData.getLastMessageIccid()));
            }else {
                mSimView.setVisibility(GONE);
            }
        }else {
            mSimView.setVisibility(GONE);
        }

    }

    /**
     * Bkav QuangNDb Ham check setting co always show sub info khong
     */
    private boolean shouldShowInfoSim() {
        // Now check prefs (i.e. settings) to see if the user turned off notifications.
        final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();
        final Context context = Factory.get().getApplicationContext();
        final String prefKey = context.getString(R.string.option_show_info_sim_key);
        final boolean defaultValue = context.getResources().getBoolean(
                R.bool.show_info_sim_conversation);
        return prefs.getBoolean(prefKey, defaultValue);
    }
}