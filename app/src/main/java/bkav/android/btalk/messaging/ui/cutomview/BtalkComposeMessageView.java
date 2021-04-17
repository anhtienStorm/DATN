package bkav.android.btalk.messaging.ui.cutomview;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.datamodel.data.SubscriptionListData;
import com.android.messaging.ui.AttachmentPreview;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversation.ComposeMessageView;
import com.android.messaging.util.AccessibilityUtil;
import com.android.messaging.util.Assert;
import com.android.messaging.util.AvatarUriUtil;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.UiUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.messaging.BtalkSendEsimAdapter;
import bkav.android.btalk.messaging.custom_view.BtalkSimIconView;
import bkav.android.btalk.messaging.datamodel.data.QuickResponseData;
import bkav.android.btalk.messaging.datamodel.manager.QuickResponseManager;
import bkav.android.btalk.messaging.ui.BtalkAttachmentPreview;
import bkav.android.btalk.messaging.ui.BtalkComposeEditText;
import bkav.android.btalk.messaging.util.ImageButtonSendCustom;
import bkav.android.btalk.text_shortcut.ActionItem;
import bkav.android.btalk.text_shortcut.ApiCompatibilityUtils;
import bkav.android.btalk.text_shortcut.DeepShortcutsContainer;
import bkav.android.btalk.utility.BtalkUiUtils;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.telephony.SmsManager.RESULT_ERROR_NO_SERVICE;
import static bkav.android.btalk.esim.ActiveDefaultProfileReceiver.GET_CODE_RESULT;

/**
 * Created by quangnd on 29/03/2017.
 * Bkav QuangNDb class custom lai ComposeMessageView cua code goc
 */

public class BtalkComposeMessageView extends ComposeMessageView
        implements DeepShortcutsContainer.OnActionItemClickListener
        , BtalkSendEsimAdapter.EsimMessageAdapterListener {

    private ImageView mEmoticonButton;
    private View mMarginBot;
    private BtalkSimIconView mSimIconView;

    public Uri getSendButtonUri() {
        return mSendButtonUri;
    }

    private Uri mSendButtonUri;

    private static final int EMOTICON_POSITION = 2;
    protected UpdateDraftChangeHandler mUpdateDraftChangeHandler;

    private DeepShortcutsContainer mDeepShortcutsContainer;//Bkav QuangNDb Bien tao shortcut quick response
    private boolean mIsKeyboardShow = false;//Bkav QuangNDb bien check xem ban phim co dang show khong
    private boolean mIsLandscape = false;//Bkav QuangNDb bien check xem man hinh ngang hay doc

    // Bkav TienNAb: them hang so thoi gian delay khi an giao dien mediapicker
    private static final int TIME_DELAY_HIDE_MEDIAPICKER_IS_FULLSCREEN = 150;
    private static final int IME_DELAY_HIDE_MEDIAPICKER = 450;
    // Bkav HienDTk: check xem co dang o trong giao dien conversation fragment hay khong
    private static final String IN_CONTACT_PICKER_FRAGMENT = "-1";

    //Bkav QuangNDb xu ly show list sim de chon
    private RecyclerView mRecyclerViewEsim;
    private BtalkSendEsimAdapter mSendEsimAdapter;
    private List<ISimProfile> mProfileList = new ArrayList<>();

    //HienDTk: Bien cho khay sim 1 va khay sim 2
    private static final int SLOT_INDEX_SIM_1 = 0;
    private static final int SLOT_INDEX_SIM_2 = 1;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mEmoticonButton = (ImageView) findViewById(R.id.img_add_emoticon);
        mMarginBot = findViewById(R.id.bot_margin);
        mEmoticonButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHost.emojiClick();
                mInputManager.getMediaInput().setPagerPosition(EMOTICON_POSITION);

                // Bkav TienNAb: xu ly khi click vao icon emoij
                boolean isShow = mInputManager.getMediaInput().isOpen();
                if (!isShow) {
                    showOrHideMediaPicker(isShow);
                } else {
                    hideMediaPicker();
                }
            }
        });
        mEmoticonButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mHost != null) {
                    showQuickResponse();
                }
                return true;
            }
        });
    }

    public void initRecyclerProfileList() {
        mRecyclerViewEsim = mParentView.findViewById(R.id.list_esim);
    }

    @Override
    protected void initComposeEditText() {
        mComposeEditText = (BtalkComposeEditText) findViewById(
                R.id.compose_message_text);
        mComposeEditText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboard();
            }
        });
    }

    public BtalkComposeMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mUpdateDraftChangeHandler = new UpdateDraftChangeHandler(this);
    }

    @Override
    public void onMediaItemsSelected(Collection<MessagePartData> items) {
        if (checkNotMMS(items)) {
            String newText = getMessageText(items);
//            mComposeEditText.append(newText);
//            mComposeEditText.setSelection(mComposeEditText.length());
            mComposeEditText.input(newText);
        } else {
            super.onMediaItemsSelected(items);
        }
    }

    /**
     * Bkav QuangNDb: ham check xem co phai 1 list tin nhan toan sms hay khong
     * neu co 1 tin nhan la mms thi return false
     */
    private boolean checkNotMMS(Collection<MessagePartData> items) {
        List<MessagePartData> messagePartDatas = new ArrayList<>(items);
        for (MessagePartData partData : messagePartDatas) {
            if (partData.isAttachment()) {
                return false;
            }
        }
        return true;
    }

    private String getMessageText(Collection<MessagePartData> items) {
        List<MessagePartData> messagePartDatas = new ArrayList<>(items);
        String result = "";
        for (MessagePartData partData : messagePartDatas) {
            result += partData.getText();
        }
        return result;
    }

    @Override
    protected void onLongClickSelfSendButton() {
        mSimIconView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                //Bkav QuangNDb show profile
                showProfileList();
                return true;
            }
        });
    }


    @Override
    protected void onLongClickSendButton() {
        mSendButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                //Bkav QuangNDb show profile
                showProfileList();
                return true;
            }
        });
    }

    /**
     * Bkav QuangNDb them ham show hoac hide giao dien sim selector khi nhan vao icon sim hoac icon send
     */
    // TODO: 17/03/2020 dang khong dung do dung giao dien list profile
    public void showOrHideSimSelector() {
        boolean shown = mInputManager.toggleSimSelector(true /* animate */,
                getSelfSubscriptionListEntry());
        hideAttachmentsWhenShowingSims(shown);
    }

    @Override
    protected void setVisibleMmsIndicator(boolean isMms) {
        mMmsIndicator.setVisibility(isMms ? VISIBLE : GONE);
        mMarginBot.setVisibility(isMms ? GONE : VISIBLE);
    }

    @Override
    protected void inVisibleMMsIndicator() {
        mMmsIndicator.setVisibility(GONE);
        mMarginBot.setVisibility(VISIBLE);
    }

    @Override
    protected void showCharCounter() {
        mCharCounter.setVisibility(View.VISIBLE);
        mMarginBot.setVisibility(GONE);
    }

    @Override
    protected void hideCharCounter() {
        mCharCounter.setVisibility(View.GONE);
        mMarginBot.setVisibility(VISIBLE);
    }

    @Override
    protected void setHintMultiSim(SubscriptionListData.SubscriptionListEntry subscriptionListEntry) {
        // Bkav HienDTk: thay doi kich ban hien thi text khi gui tin nha
        mComposeEditText.setHint(R.string.btalk_hint_compose_message_not_multi_sim);
//        //HienDTk: neu dang o che do luon hoi truoc khi nhan tin thi set lai text
//        if (BtalkUiUtils.isAlwaysAskBeforeSendSms()) {
//
//
//        } else {
//            mComposeEditText.setHint(getResources().getString(
//                    R.string.btalk_compose_message_view_hint_text_multi_sim,
//                    subscriptionListEntry.displayName));
//        }
    }

    @Override
    public Uri getSelfSendButtonIconUri() {
        final SubscriptionListData.SubscriptionListEntry subscriptionListEntry = getSelfSubscriptionListEntry();
        if (subscriptionListEntry != null) {
            return subscriptionListEntry.selectedIconUri;
        }
        return null;
    }

    /**
     * Bkav QuangNDb ham xu ly khi co 1 sim thi hien luon button send
     */
    @Override
    protected void setImageResourceWithNullUri(boolean workingDraft, boolean hasWorkingDraft, boolean isMms) {
//        mSelfSendIcon.setVisibility(GONE);
        mSimIconView.setVisibility(GONE);
        mSendButton.setVisibility(VISIBLE);

        if (hasWorkingDraft && workingDraft) {
            setVisibleMmsIndicator(isMms);
            sendWidgetMode = SEND_WIDGET_MODE_SEND_BUTTON;
        } else {
            inVisibleMMsIndicator();
        }
    }

    @Override
    protected void goneSendButton() {
        if (isMultiSim()) {
            super.goneSendButton();
        }
        // Bkav QuangNDb khong lam gi
    }


    private boolean mIsFirst = true;

    @Override
    protected void updateDraftChange(String text) {
        if (mIsFirst && !text.isEmpty()) {
            super.updateDraftChange(text);
            mIsFirst = false;
        } else {
            mCurrentString = text;
            mUpdateDraftChangeHandler.removeMessages(UPDATE_DRAFT);
            final Message msg = mUpdateDraftChangeHandler.obtainMessage(UPDATE_DRAFT);
            mUpdateDraftChangeHandler.sendMessageDelayed(msg, TIME_DELAYS);
        }
        if (text.isEmpty()) {
            super.updateDraftChange(text);
            mIsFirst = true;
        }
    }

    private static final int TIME_DELAYS = 300;
    private static final int UPDATE_DRAFT = 0;


    /**
     * Bkav QuangNDb Handler update change draft
     */
    private static class UpdateDraftChangeHandler extends Handler {

        private final WeakReference<ComposeMessageView> mComposeMessageViewWeakReference;

        public UpdateDraftChangeHandler(ComposeMessageView mComposeMessageViewWeakReference) {
            this.mComposeMessageViewWeakReference = new WeakReference<>(mComposeMessageViewWeakReference);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_DRAFT) {
                if (!TextUtils.isEmpty(mComposeMessageViewWeakReference.get().mCurrentString)) {
                    mComposeMessageViewWeakReference.get()
                            .updateVisualsOnDraftChanged(mComposeMessageViewWeakReference.get().mCurrentString);
                }
            }
        }
    }

    @Override
    protected void setHintNotMultiSim() {
        mComposeEditText.setHint(R.string.btalk_hint_compose_message_not_multi_sim);
    }

    @Override
    protected void onReadySendMessage(CharSequence s) {
        if (!TextUtils.isEmpty(s) && !s.toString().equalsIgnoreCase(mFirstDraf)) {
            mHost.onReadyMessage();
        }
    }

    @Override
    protected AttachmentPreview getAttachmentView() {
        return (BtalkAttachmentPreview) findViewById(R.id.attachment_draft_view);
    }

    @Override
    protected void hideKeyboard() {
        ImeUtil.get().hideImeKeyboard(Factory.get().getApplicationContext(), mComposeEditText);
    }

    @Override
    protected void showKeyboard() {
        // Bkav HuyNQN thuc hien dong lai ban phim enmoji truoc sau do mo ban phim len cho do giat
        ImeUtil.get().showImeKeyboard(Factory.get().getApplicationContext(), mComposeEditText);
//        mInputManager.showHideMediaPicker(false, false);

        // Bkav TienNAb: mediapicker dang o che do full screen thi an nhanh hon
        if (mInputManager.getMediaInput().isFullScreen()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mInputManager.showHideMediaPicker(false, false);
                }
            }, TIME_DELAY_HIDE_MEDIAPICKER_IS_FULLSCREEN);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mInputManager.showHideMediaPicker(false, false);
                }
            }, IME_DELAY_HIDE_MEDIAPICKER);
        }
    }

    @Override
    protected boolean isOverriddenAvatarAGroup() {
        // TODO: 25/05/2017 Bkav QuangNDb doan nay cho return true de lay animation an icon, chua hieu vi sao code goc lai de mac dinh la false
        return true;
    }

    @Override
    protected void shouldShowSubject() {
        // Bkav QuangNDb khong lam gi de khong show subject nen nua
    }

    @Override
    protected void playSentSoundMessage() {
        // Bkav QuangNDb khong lam gi
    }

    @Override
    protected boolean isTurnOff3G() {
        if (mBinding.getData().getIsMms()) {
            ConnectivityManager manager = (ConnectivityManager) mOriginalContext.getSystemService(CONNECTIVITY_SERVICE);
            //For 3G check
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            return activeNetwork == null || activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE;
        } else {
            return false;
        }
    }

    @Override
    protected void showToastHasNotConnect3G() {
        Toast.makeText(mOriginalContext, R.string.toast_show_has_not_connect_3g, Toast.LENGTH_SHORT).show();
    }

    /**
     * Quangndb an media picker di
     */
    protected void hideMediaPicker() {

        mInputManager.showHideMediaPicker(false /* show */, false /* animate */);
    }

    /**
     * QuangNDb clear forcus khi bat attach
     */
    protected void clearEditTextFocus() {
        mComposeEditText.clearFocus();
        mComposeSubjectText.clearFocus();
    }

    @Override
    protected void requestEditTextFocus() {
        mComposeEditText.requestFocus();
    }

    @Override
    protected void showOrHideMediaPicker(boolean isShow) {
        //Bkav QuangNDb sua lai cho show cham lai de do giat
        hideKeyboard();
//        clearEditTextFocus();
        SystemClock.sleep(100);
        mInputManager.showHideMediaPicker(true /* show */, false /* animate */);
    }


    @Override
    protected boolean getConditionSetHint() {
        return mComposeEditText.length() == 0;
    }

    @Override
    protected void initSimIconView() {
        mSimIconView = (BtalkSimIconView) findViewById(R.id.self_send_icon);
        mSendImageButton = (ImageButtonSendCustom) findViewById(R.id.send_message_button);
    }

    private ImageButtonSendCustom mSendImageButton;


    @Override
    protected void setSimIconUri(Uri selfSendButtonUri) {
        mSendButtonUri = selfSendButtonUri;
        // Bkav HuyNQN cai dat icon cho sim
        mSlotId = AvatarUriUtil.getIdentifier(selfSendButtonUri);
        if (mSlotId != null && !mSlotId.isEmpty()) {
            //Bkav QuangNDb lay ra profile trong 1 slot de set giao dien icon sim cho chuan
            // Bkav HuyNQN fix bug BOS-3185 start
            // Thuc hien lay ra dung profile sim thuc hien gui tin nhan
            int getSlotIndex = getDefaultSimSMS(getContext());
            if(getSlotIndex == -1){// Bkav HuyNQN day la truong hop khi khong co sim mac dinh
                updateResFromProfile(ESimUtils.getActivateProfileFromSlot(Integer.parseInt(mSlotId)));
            }else {
                updateResFromProfile(ESimUtils.getActivateProfileFromSlot(getSlotIndex));
            }
        }
    }

    @Override
    protected ImageView getSimIconView() {
        return mSimIconView;
    }

    @Override
    protected void clickSendButton() {
        if (mHost != null && mHost.isAlwaysAskSim() && !mIsSimSelect) {
            showProfileList();
        } else {
            notifySendMessageToReceiver();
            sendMessageInternal(true /* checkMessageSize */);
        }
    }

    //Bkav QuangNDb bao cho receiver quan ly kich hoat lai sim cu biet la biet la gui tin nhan
    private void notifySendMessageToReceiver() {
        Intent toSimStateReceiver = new Intent();
        toSimStateReceiver.setAction(GET_CODE_RESULT);
        toSimStateReceiver.putExtra("code", RESULT_ERROR_NO_SERVICE);
        getContext().sendBroadcast(toSimStateReceiver);
    }


    /**
     * Bkav QuangNDb send message with quick response
     */
    @Override
    public void sendMessageWithQuickResponse(final Object data) {
        QuickResponseData quickResponseData;
        if (!(data instanceof QuickResponseData)) {
            return;
        } else {
            quickResponseData = (QuickResponseData) data;
        }
        //Bkav QuangNDb them th ng dung an send khi dang o giao dien picker
        if ("-1".equals(mBinding.getData().getConversationId())) {
            //Bkav QuangNDb neu chua co lien he de gui thi append text cho o nhap tin nhan
            mComposeEditText.append(quickResponseData.getResponse());
            mComposeEditText.requestFocus();
            return;
        }
        //Bkav QuangNDb Check xem client da san sang gui tin hay chua
        if (!mHost.isReadySendMessage()) {
            mHost.sendQuickResponse(data);
            return;
        }
        // Check the host for pre-conditions about any action.
        if (mHost.isReadyForAction()) {
            // Continue sending after check succeeded.
            mBinding.getData().setMessageText(quickResponseData.getResponse());
            // Asynchronously check the draft against various requirements before sending.
            mBinding.getData().checkDraftForAction(true,
                    mHost.getConversationSelfSubId(), new DraftMessageData.CheckDraftTaskCallback() {
                        @Override
                        public void onDraftChecked(DraftMessageData data, int result) {
                            mBinding.ensureBound(data);
                            switch (result) {
                                case DraftMessageData.CheckDraftForSendTask.RESULT_PASSED:
                                    // Continue sending after check succeeded.
                                    final MessageData message = mBinding.getData()
                                            .prepareMessageForSending(mBinding);
                                    if (message != null && message.hasContent()) {
                                        // BKav QuangNDb tach code doan play sound message
                                        playSentSoundMessage();
                                        mHost.sendMessage(message);
                                        hideSubjectEditor();
                                        if (AccessibilityUtil.isTouchExplorationEnabled(getContext())) {
                                            AccessibilityUtil.announceForAccessibilityCompat(
                                                    BtalkComposeMessageView.this, null,
                                                    R.string.sending_message);
                                        }
                                    }
                                    break;

                                case DraftMessageData.CheckDraftForSendTask.RESULT_HAS_PENDING_ATTACHMENTS:
                                    // Cannot send while there's still attachment(s) being loaded.
                                    UiUtils.showToastAtBottom(
                                            R.string.cant_send_message_while_loading_attachments);
                                    break;

                                case DraftMessageData.CheckDraftForSendTask.RESULT_NO_SELF_PHONE_NUMBER_IN_GROUP_MMS:
                                    mHost.promptForSelfPhoneNumber();
                                    break;

                                case DraftMessageData.CheckDraftForSendTask.RESULT_MESSAGE_OVER_LIMIT:
                                    Assert.isTrue(true);
                                    mHost.warnOfExceedingMessageLimit(
                                            true /*sending*/, false /* tooManyVideos */);
                                    break;

                                case DraftMessageData.CheckDraftForSendTask.RESULT_VIDEO_ATTACHMENT_LIMIT_EXCEEDED:
                                    Assert.isTrue(true);
                                    mHost.warnOfExceedingMessageLimit(
                                            true /*sending*/, true /* tooManyVideos */);
                                    break;

                                case DraftMessageData.CheckDraftForSendTask.RESULT_SIM_NOT_READY:
                                    // Cannot send if there is no active subscription
                                    UiUtils.showToastAtBottom(
                                            R.string.cant_send_message_without_active_subscription);
                                    break;

                                default:
                                    break;
                            }
                        }
                    }, mBinding);
        } else {
            mHost.warnOfMissingActionConditions(true /*sending*/,
                    new Runnable() {
                        @Override
                        public void run() {
                            sendMessageWithQuickResponse(data);
                        }

                    });
        }
    }

    @Override
    protected void sendMessageIfHaveDraf() {
        if (!isSend()) {
            return;
        }
        if (mSimIconView.getVisibility() == GONE) {
            sendMessageInternal(true);
        }
    }

    @Override
    public View getEmoticonView() {
        return mEmoticonButton;
    }

    @Override
    public void onItemClick(final QuickResponseData data) {
        if (data.getId() == -1) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    onEditClick(data);
                }
            });
        } else {
            sendMessageWithQuickResponse(data);
        }
    }

    @Override
    public void onEditClick(QuickResponseData data) {
        UIIntents.get().launchBtalkQuickResponseEditActivity(mOriginalContext);
    }

    /**
     * Bkav QuangNDb lay y theo screen
     */
    private float getYScreen(View view) {
        int[] array = new int[2];
        view.getLocationInWindow(array);
        return array[1];
    }

    /**
     * Bkav QuangNDb lay x theo screen
     */
    private float getXScreen(View view) {
        int[] array = new int[2];
        view.getLocationInWindow(array);
        return array[0];
    }

    private int mActionBarDeltaSizePixel = 0;// Bkav QuangNDb

    /**
     * Bkav QuangNDb set delta pixel
     */
    public void setActionBarDeltaSizePixel() {
        this.mActionBarDeltaSizePixel = 72;
    }

    /**
     * Bkav QuangNDb show quick response
     */
    private void showQuickResponse() {
        final boolean isInputShow = mInputManager.getMediaInput().isOpen();//Bkav QuangNDb bien check input show(ex: emoji)
        if (mIsLandscape && (mIsKeyboardShow || isInputShow)) {
            Toast.makeText(mOriginalContext, R.string.dont_support_quick_response, Toast.LENGTH_SHORT).show();
        } else {
            ArrayList<ActionItem> listAction = createListActionItem();

            // Anhdts them action them phan hoi nhanh
            if (listAction.isEmpty()) {
                final QuickResponseData data = new QuickResponseData();
                data.setResponse(mOriginalContext.getString(R.string.add_response_title));
                listAction.add(new ActionItem(ApiCompatibilityUtils.getDrawable(getResources(),
                        R.drawable.icon_edit_quick_response), data));
            }

            mDeepShortcutsContainer = DeepShortcutsContainer.showForIconTest(getXScreen(mEmoticonButton)//.getX()//816.0f
                    , getYScreen(mEmoticonButton) - mEmoticonButton.getHeight() - mActionBarDeltaSizePixel//1608.0f
                    , mParentView, listAction
                    , new DeepShortcutsContainer.UpdateDeepShortcutsContainerCallback() {
                        @Override
                        public void setDeepShortcutContainer(DeepShortcutsContainer obj) {
                            mDeepShortcutsContainer = obj;
                        }
                    });
            if (mDeepShortcutsContainer != null) {
                mDeepShortcutsContainer.setOnActionItemClickListener(this);
            }
        }
    }

    @Override
    public boolean isDeepShortcutTouch(MotionEvent ev) {
        return mDeepShortcutsContainer != null && DeepShortcutsContainer.isShowing()
                && mDeepShortcutsContainer.onProcessEvent(ev);
    }

    /**
     * Bkav QuangNDb tao temp shortcut
     */
    protected ArrayList<ActionItem> createListActionItem() {
        ArrayList<ActionItem> list = new ArrayList<>();
        List<QuickResponseData> quickResponseDataList = QuickResponseManager.getQuickResponseDataList();

        for (QuickResponseData quickResponseData : quickResponseDataList) {
            list.add(new ActionItem(ApiCompatibilityUtils.getDrawable(getResources(), R.drawable.icon_edit_quick_response)
                    , quickResponseData));
        }
        return list;
    }

    /**
     * Bkav QuangNDb show keyboard with input view
     */
    private void showImeKeyboard(final EditText view) {
        Assert.notNull(view);
        view.requestFocus();

        // showImeKeyboard() won't work until the layout is ready, so wait until layout is complete
        // before showing the soft keyboard.
        UiUtils.doOnceAfterLayoutChange(mParentView, new Runnable() {

            public void run() {
                ImeUtil.get().showImeKeyboard(getContext(), view);
            }
        });
    }

    @Override
    public void setParentView(final ViewGroup mParentView) {
        super.setParentView(mParentView);
        initRecyclerProfileList();
        //Bkav QuangNDb phat hien ra keyboard dang show hoac hide
        mParentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                mParentView.getWindowVisibleDisplayFrame(r);
                int screenHeight = mParentView.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom - getNavigationBarSize(getContext()).y;
                if (keypadHeight > screenHeight * 0.25) { // 0.15 ratio is perhaps enough to determine keypad height.
                    if (!mIsKeyboardShow && !mInputManager.getMediaInput().isFullScreen()) {
                        mIsKeyboardShow = true;
                    }
                } else {
                    if (mIsKeyboardShow && mDeepShortcutsContainer != null && DeepShortcutsContainer.isShowing()) {
                        mDeepShortcutsContainer.animateClose();
                        mIsKeyboardShow = false;
                    }
                }
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        if (mDeepShortcutsContainer != null && DeepShortcutsContainer.isShowing()) {
            mDeepShortcutsContainer.animateClose();
        }
        return super.onBackPressed();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        //HienDTk: show key board de tranh loi giao dien khi xoay ngang
        showKeyboard();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mIsLandscape = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mIsLandscape = false;
        }
    }

    // Bkav HuyNQN cai dat icon sim
    @Override
    public void setImageResourceButtonSend(Uri uri) {
        mSendImageButton.setImageResourceUriEsim(mSimIconView.getIdSim(), getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size));
    }

    // Bkav TienNAb: ham do chieu cao navigation bar
    public Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
        }
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }
        return new Point();
    }

    public Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            } catch (NoSuchMethodException e) {
            }
        }
        return size;
    }

    @Override
    protected void checkMultiSim() {
        mIsMultiSim = BtalkCallLogCache.getCallLogCache(Factory.get().getApplicationContext()).isHasSimOnAllSlot() || ESimUtils.isMultiProfile();
    }


    @Override
    public void updateResFromProfile(Object o) {
        if (o instanceof ISimProfile) {
            ISimProfile iSimProfile = (ISimProfile) o;
            //HienDTk: mau cua btn gui tin nhan
            int colorSim1 = getContext().getColor(R.color.esim_01);
            int colorSim2 = getContext().getColor(R.color.esim_02);
            int isAlwaysAskBeforeSendSms = getContext().getColor(R.color.btalk_ab_text_and_icon_selected_color);

            int getSlotIndex = getDefaultSimSMS(getContext());
            //HienDTk: neu dang o che do luon hoi truoc khi nhan tin thi cap nhat lai icon send message
            if (BtalkUiUtils.isAlwaysAskBeforeSendSms()) {
                // Bkav HienDTk: cap nhat sim duoc chon
                if (!mIsSimSelect) {
                    mSimIconView.setImageResourceUriEsim("",
                            getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size), getResources().getColor(R.color.btalk_orange_color));
                    mSendImageButton.setImageResourceUriEsim("", getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size));
                } else { // Bkav HienDTk: truong hop mac dinh trong setting
//                    mSimIconView.setImageResourceUriEsim("",
//                            getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size), isAlwaysAskBeforeSendSms);
                    // Bkav HienDTk: fix bug Không lưu được lựa chọn sim gửi tin nhắn sau khi chọn từ danh sách lựa chọn sim => BOS-2899 - Start
                    mSimIconView.setImageResourceUriEsim(String.valueOf(iSimProfile.getProfileIndex()),
                            getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size), iSimProfile.getColor());
                    mSendImageButton.setImageResourceUriEsim(String.valueOf(iSimProfile.getProfileIndex()), getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size));
                    // Bkav HienDTk: fix bug Không lưu được lựa chọn sim gửi tin nhắn sau khi chọn từ danh sách lựa chọn sim => BOS-2899 - End
                }

            } else {   // Bkav HienDTk: neu o trong cuoc hoi thoai thi load sim gan nhat nhan tin
                if (mIsSimSelect || !IN_CONTACT_PICKER_FRAGMENT.equals(mBinding.getData().getConversationId())) {// Bkav HienDTk: cap nhat sim duoc chon
                    mSimIconView.setImageResourceUriEsim(String.valueOf(iSimProfile.getProfileIndex()),
                            getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size), iSimProfile.getColor());
                    mSendImageButton.setImageResourceUriEsim(String.valueOf(iSimProfile.getProfileIndex()), getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size));
                } else {
                    // Bkav HuyNQN luc nao cung phai lay ra profile duoc active, vi con ho tro ca Esim
                    //HienDTk: cap nhat lai icon cho sim mac dinh trong setting
                    if (getSlotIndex == SLOT_INDEX_SIM_1) { //HienDTk: sim mac dinh dang o khay sim 1
                        ISimProfile profile = ESimUtils.getActivateProfileFromSlot(SLOT_INDEX_SIM_1);
                        mSimIconView.setImageResourceUriEsim(String.valueOf(profile.getProfileIndex()),
                                getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size), profile.getColor());
                        mSendImageButton.setImageResourceUriEsim(String.valueOf(profile.getProfileIndex()), getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size));
                    } else if (getSlotIndex == SLOT_INDEX_SIM_2) { //HienDTk: sim mac dinh dang o khay sim 2
                        ISimProfile profile = ESimUtils.getActivateProfileFromSlot(SLOT_INDEX_SIM_2);
                        mSimIconView.setImageResourceUriEsim(String.valueOf(profile.getProfileIndex()),
                                getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size), profile.getColor());
                        mSendImageButton.setImageResourceUriEsim(String.valueOf(profile.getProfileIndex()), getResources().getDimensionPixelSize(R.dimen.icon_compose_contact_message_size));
                    }
                    // Bkav HuyNQN fix bug BÓ-3185 end
                }

            }
        }
    }


    // Bkav HuyNQN lay ra danh sach esim
    private void showProfileList() {
        mProfileList = ESimUtils.getAllProfileExcludeOTT();
        if (mProfileList.size() <= 1) {
            return;
        }
        mSendEsimAdapter = new BtalkSendEsimAdapter(getContext(), mProfileList);
        mSendEsimAdapter.setListener(this);
        RecyclerView.LayoutManager manager;
        if (mProfileList.size() <= 5) {
            manager = new LinearLayoutManager(getContext());
            ((LinearLayoutManager) manager).setReverseLayout(true);
        } else {
            manager = new GridLayoutManager(getContext(), 2);
            ((GridLayoutManager) manager).setReverseLayout(true);
        }
        mRecyclerViewEsim.setLayoutManager(manager);
        mRecyclerViewEsim.setAdapter(mSendEsimAdapter);
        mRecyclerViewEsim.setVisibility(View.VISIBLE);
    }

    //Bkav QuangNDb xu ly khi click vao item sim tren list sim
    @Override
    public void itemEsimOnClickListener(int position, ISimProfile profile) {
        for (SubscriptionListData.SubscriptionListEntry entry : mListSubEntry) {
            //Bkav QuangNDb them logic iccid
            if (entry.slotId == profile.getSlotSim()) {
                mIsSimSelect = true;
                selectSim(entry);
                break;
            }
        }
        if (!profile.getSimProfileState()) {
            ESimUtils.enableProfile(profile, false);
        }
        //Bkav QuangNDb set iccid cho draf khi chon sim de khong bi loi hien thi
        final String iccid = new String(profile.getSimIdProfile());
        mBinding.getData().setIccid(iccid);
        updateResFromProfile(profile);
        mRecyclerViewEsim.setVisibility(View.GONE);
    }

    //Bkav QuangNDb custom lai su kien click sim icon thi show list profile
    @Override
    protected void clickSelfSendIcon() {
        showProfileList();
    }

    @Override
    public void unbind() {
        super.unbind();
        mParentView = null;
        if (mDeepShortcutsContainer != null) {
            mDeepShortcutsContainer.unbind();
        }
        if (mSendEsimAdapter != null) {
            mSendEsimAdapter.unbind();
        }
    }

    // Bkav HuyNQN danh dau da lua chon sim de gui
    private boolean mIsSimSelect;

    //HienDTk: check xem co cho hien ban phim hay khong thi mo mediapicker
    public boolean isShowKeyboad() {
        return mIsShowKeyboad;
    }
}
