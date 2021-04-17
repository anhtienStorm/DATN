package bkav.android.btalk.messaging.ui.contact;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.text.BidiFormatter;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.CallUtil;
import com.android.contacts.common.ContactPhotoManager;
import com.android.dialer.database.DialerDatabaseHelper;
import com.android.dialer.util.DialerUtils;
import com.android.ex.chips.RecipientEntry;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.android.messaging.Factory;
import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.ActionMonitor;
import com.android.messaging.datamodel.action.GetOrCreateConversationAction;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.binding.ImmutableBindingRef;
import com.android.messaging.datamodel.data.ContactListItemData;
import com.android.messaging.datamodel.data.ContactPickerData;
import com.android.messaging.datamodel.data.ConversationData;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.datamodel.data.ConversationParticipantsData;
import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.datamodel.data.SubscriptionListData;
import com.android.messaging.sms.MmsSmsUtils;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.ui.BugleActionBarActivity;
import com.android.messaging.ui.SnackBar;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.contact.AddContactsConfirmationDialog;
import com.android.messaging.ui.contact.ContactPickerFragment;
import com.android.messaging.ui.contact.ContactRecipientAutoCompleteView;
import com.android.messaging.ui.conversation.ComposeMessageView;
import com.android.messaging.ui.conversation.ConversationFragment;
import com.android.messaging.ui.conversation.ConversationInputManager;
import com.android.messaging.ui.conversation.ConversationMessageAdapter;
import com.android.messaging.ui.conversation.ConversationMessageView;
import com.android.messaging.ui.conversation.EnterSelfPhoneNumberDialog;
import com.android.messaging.ui.conversation.MessageDetailsDialog;
import com.android.messaging.ui.conversation.SimSelectorView;
import com.android.messaging.ui.mediapicker.MediaPicker;
import com.android.messaging.util.AccessibilityUtil;
import com.android.messaging.util.Assert;
import com.android.messaging.util.AvatarUriUtil;
import com.android.messaging.util.ChangeDefaultSmsAppHelper;
import com.android.messaging.util.ContactUtil;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.TextUtil;
import com.android.messaging.util.UiUtils;
import com.google.common.annotations.VisibleForTesting;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.esim.dialog_choose_sim.DialogChooseSimFragment;
import bkav.android.btalk.messaging.datamodel.data.QuickResponseData;
import bkav.android.btalk.messaging.ui.contact.activities.BtalkContactSelectionActivity;
import bkav.android.btalk.messaging.ui.contacts.common.list.BtalkPhoneNumberPickerFragment;
import bkav.android.btalk.messaging.ui.conversation.BtalkConversationInputManager;
import bkav.android.btalk.messaging.ui.conversation.BtalkConversationMessageAdapter;
import bkav.android.btalk.messaging.ui.conversation.BtalkConversationMessageView;
import bkav.android.btalk.messaging.ui.conversation.BtalkListParticipantDialog;
import bkav.android.btalk.messaging.ui.conversation.BtalkSimSelectorView;
import bkav.android.btalk.messaging.ui.conversation.MultiMessageSelectActionModeCallBack;
import bkav.android.btalk.messaging.ui.cutomview.BtalkPickerComposeMessageView;
import bkav.android.btalk.messaging.ui.mediapicker.BtalkMediaPicker;
import bkav.android.btalk.messaging.util.BtalkCharacterUtil;
import bkav.android.btalk.messaging.util.BtalkLinkify;
import bkav.android.btalk.suggestmagic.SuggestLoaderManager;
import bkav.android.btalk.suggestmagic.SuggestPopup;
import bkav.android.btalk.utility.BtalkUiUtils;
import bkav.android.btalk.utility.PrefUtils;

import static android.app.Activity.RESULT_OK;

/**
 * Created by quangnd on 26/03/2017.
 * fragment custom lai ContactPickerFragment cua source goc
 */

public class BtalkContactPickerFragment extends ContactPickerFragment
        implements BtalkPhoneNumberPickerFragment.BtalkPhoneNumberPickerFragmentHost
        , ConversationInputManager.ConversationInputHost, DraftMessageData.DraftMessageDataListener
        , ConversationData.ConversationDataListener
        , ComposeMessageView.IComposeMessageViewHost
        , ConversationMessageView.ConversationMessageViewHost
        , ConversationMessageAdapter.OnMessageAnimationEndListener
        , SearchView.OnQueryTextListener
        , SearchView.OnCloseListener
        , MultiMessageSelectActionModeCallBack.MultiMessageSelectListener
        , SuggestPopup.ActionSmartSuggest {

    private static final String CONVERSATION_KEY = "conversation_key";

    private BtalkPhoneNumberPickerFragment mContactPickerFragment;

    private RecipientEntry mRecipientEntry;

    // QuangNDb them bien compose message view
    private BtalkPickerComposeMessageView mComposeMessageView;

    //Bkav QuangNDb Bien check xem da tao thanh cong conversation hay chua
    private boolean mCreateConversationSucceeded = false;

    private static final String CONVERSATION_DRAF_ID = "-1";

    // Bkav TienNAb: them hang so thoi gian delay khi an giao dien mediapicker
    private static final int TIME_DELAY_HIDE_MEDIAPICKER_IS_FULLSCREEN = 150;
    private static final int IME_DELAY_HIDE_MEDIAPICKER = 450;


    protected String mConversationId = CONVERSATION_DRAF_ID;

    private boolean mIsMetaLoaded = false;

    private boolean mIsMessageLoaded = false;

    private boolean mIsParticipantLoaded = false;

    private boolean mIsSubscriptionLoaded = false;

    private RecyclerView mRecyclerViewEsim;

    //Bkav QuangNDb trong truong hop tin nhan nhom thi show dialog cho nguoi dung chon 1 nguoi de goi
    private BtalkListParticipantDialog mBtalkListParticipantDialog;

    /**
     * Bkav QuangNDb check xem du lieu da duoc load het chua
     */
    private boolean isLoadAllFinish() {
        return mIsMetaLoaded && mIsMessageLoaded && mIsParticipantLoaded && mIsSubscriptionLoaded;
    }

    /**
     * Bkav QuangNDb Reset all load data
     */
    private void resetAllLoad() {
        mIsMetaLoaded = false;
        mIsMessageLoaded = false;
        mIsParticipantLoaded = false;
        mIsSubscriptionLoaded = false;
    }


    @VisibleForTesting
    protected final Binding<ConversationData> mBinding = BindingBase.createBinding(this);


    public static BtalkContactPickerFragment newInstance(ArrayList<ParticipantData> data) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(CONVERSATION_KEY, data);
        BtalkContactPickerFragment fragment = new BtalkContactPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initBinding() {
        if (!mBinding.isBound()) {
            mBinding.bind(DataModel.get().createConversationData(getActivity(), this, mConversationId));
        }
    }


    //Bkav QuangNdb khoi tao textbox actionbar khi nguoi dung muon them lien he de nt
    private void initRecipientText() {
        ArrayList<ParticipantData> mParticipantDatas = getArguments().getParcelableArrayList(CONVERSATION_KEY);
        if (mParticipantDatas != null && !mParticipantDatas.isEmpty()) {
            for (ParticipantData participantData : mParticipantDatas) {
                ContactListItemData contactData = ContactListItemData.getExistingContactListItemData(getActivity()
                        , participantData.getSendDestination(), "");
                if (contactData != null) {
                    mRecipientTextView.appendRecipientEntry(contactData.getRecipientEntry());
                } else {
                    mRecipientTextView.appendRecipientEntry(RecipientEntry.constructFakeEntry(participantData.getSendDestination(), true));
                    mRecipientTextView.getRecipientParticipantDataForConversationCreation();
                }
            }
        }
        if (mRecipientTextView == null) {
            return;
        }
        mRecipientTextView.requestFocus();
        showImeKeyboard();
    }

    private void initRecipientTextRunnable() {
        new Handler().post(new InitRecipientRunnable(this));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mHost != null) {
            mHost.fixSizeToolbar();//Bkav QuangNDb fix lai size cua toolbar de hop voi actionbar cua picker
        }
    }

    @Override
    public void updateActionBar(ActionBar actionBar) {
        // Bkav TienNAb: them dieu kien de update actionbar khi mediapicker khong o che do fullscreen
        if (mComposeMessageView == null || !mComposeMessageView.updateActionBar(actionBar) || !inputManager.getMediaInput().isFullScreen()) {
            // Bkav QuangNDb xoa do bong tren action bar
            removeShadowActionbar(actionBar);
            updateActionAndStatusBarColor(actionBar);
            // We update this regardless of whether or not the action bar is showing so that we
            // don't get a race when it reappears.
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Reset the back arrow to its default
            actionBar.setHomeAsUpIndicator(0);
            View customView = actionBar.getCustomView();
            if (customView == null || customView.getId() != R.id.conversation_title_container) {
                final LayoutInflater inflater = (LayoutInflater)
                        getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                customView = inflater.inflate(R.layout.btalk_action_bar_picker_framgment, null);
                customView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        onBackPressed();
                    }
                });
                actionBar.setCustomView(customView);
                //Bkav QuangNDb init actionbar
                initActionBar(customView);
                super.initRecipientTextView(customView, inflater);
                if (mIsClickConfirm) {
                    displayContactName();
                } else {
                    if (mRecipientTextView.getAllRecipients().size() == 0) {
                        reInitRecipientTextViewRunnable();
                    }
                }

            }
            final TextView conversationNameView =
                    (TextView) customView.findViewById(R.id.conversation_title);
            // Bkav QUangNDb them su kien click vao text view tren action bar
            titleBarClick(conversationNameView);
            final String conversationName = getConversationName();
            if (!TextUtils.isEmpty(conversationName)) {
                // bkav QuangNDb set title bar cho action bar
                setTitleBar(conversationName, conversationNameView);
            }
            // When conversation is showing and media picker is not showing, then hide the action
            // bar only when we are in landscape mode, with IME open.
            if (mHost.isImeOpen() && UiUtils.isLandscapeMode()) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
        } else {
            // Bkav TienNAb: them dieu kien khi update actionbar
            if (mRecipientTextView.getAllRecipients().size() != 0 || inputManager.getMediaInput().isFullScreen()) {
                mInputContactView.setVisibility(View.GONE);
                mDisplayContactView.setVisibility(View.VISIBLE);
                exitActionMode();//Bkav QuangNDb them ham exit action mode khi action bar bi an
            } else {
                mRecipientTextView.clearFocus();
            }
        }
    }

    /**
     * Bkav QuangNDb re init recipient khi bi reset action bar trong runnable de khong bi loi giao dien
     */
    private void reInitRecipientTextViewRunnable() {
        new Handler().postDelayed(new ReInitRecipientRunnable(this), 100);// delay 100ms de bitmap ve kip contact
    }

    /**
     * Bkav QuangNDb re init recipient khi bi reset action bar
     */
    private void reInitRecipientTextView() {
        for (RecipientEntry recipientEntry : mRecipientEntries) {
            if (!mRecipientTextView.getAllRecipients().contains(recipientEntry)) {
                mRecipientTextView.appendRecipientEntry(recipientEntry);
            }
        }
    }

    @Override
    protected void initRecipientTextView(View view, LayoutInflater inflater) {
        //Khong lam gi
    }

    protected void updateActionAndStatusBarColor(ActionBar actionBar) {
        // doi mau actionbar va status bar
//        btalk_color_conversation_bg
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.btalk_picker_fragment_color))); // TrungTH NOte doi mau
        setStatusBar(R.color.btalk_picker_fragment_color);
        actionBar.setElevation(0f);
        // Bkav QuangNDb khong lam gi de giong mau actionbar trong style
    }


    protected void exitActionMode() {
        if (isShowActionMode()) {
            onExitActionMode();
        }
    }


    /**
     * Bkav QuangNDb thoat khoi che do action mode
     */
    public void onExitActionMode() {
        dismissActionMode();
        mAdapter.notifyDataSetChanged();
        mRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);// reset lai light status bar
        setStatusBar(R.color.btalk_picker_fragment_color);
    }

    /**
     * Bkav QuangNDb set status bar color
     */
    private void setStatusBar(int colorId) {
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), colorId));
    }


    private String getConversationName() {
        return mBinding.getData().getConversationName();
    }

    protected void titleBarClick(TextView conversationNameView) {
        conversationNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeACall(false);
            }
        });
    }

    /**
     * Bkav QuangNDb Cham vao ten tren actionbar de thuc hien cuoc goi
     */
    private void makeACall(boolean suggestShowOtherSim) {
        if (mBinding.getData().getParticipants().getNumberOfParticipantsExcludingSelf() >= 2) {
            ArrayList<ParticipantData> participantDatas = new ArrayList<>();
            if (mBinding.getData().getParticipants() != null) {
                for (ParticipantData participantData : mBinding.getData().getParticipants()) {
                    if (!participantData.isSelf()) {
                        participantDatas.add(participantData);
                    }
                }
            }
            mBtalkListParticipantDialog = BtalkListParticipantDialog.newInstance(participantDatas, getFragmentManager());
            mBtalkListParticipantDialog.show(getFragmentManager(), "Show");
        } else {
            final String phoneNumber = mBinding.getData().getParticipantPhoneNumber();
            if (phoneNumber != null) {
                if (!suggestShowOtherSim) {
                    UIIntents.get().makeACall(getContext(), getFragmentManager(), phoneNumber);
                }else {
                    List<ISimProfile> iSimProfiles = ESimUtils.getAllProfileWithNumber(phoneNumber);
                    if (iSimProfiles.size() <= 1) {
                        final Intent intentCall = CallUtil.getCallIntent(phoneNumber);
                        intentCall.putExtra("Cdma_Supp", true);
                        DialerUtils.startActivityWithErrorToast(getContext(), intentCall);
                    }else {
                        DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(phoneNumber);
                        dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
                    }
                }

            } else {
                Toast.makeText(getActivity(), R.string.btalk_notify_not_make_a_call, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Bkav QuangNDb Xoa vach ke duoi actionbar mac dinh
     */
    protected void removeShadowActionbar(ActionBar actionBar) {
        actionBar.setElevation(0);
    }

    @Override
    public void onEntryComplete() {
        //Bkav QuangNDb khong lam gi
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);// reset lai light status bar
    }

    @Override
    protected void onComplete() {
        //Bkav QuangNDb Khong lam gi
    }

    private List<RecipientEntry> mRecipientEntries = new ArrayList<>();//Bkav QuangNDb bien de luu tam contact da add de khi actionbar bi reset se khong bi mat

    private boolean mIsInitClick = false;//Bkav QuangNDb Bien check init click

    @Override
    public void onContactItemClick(RecipientEntry recipientEntry) {
        if (mContactPickingMode == MODE_PICK_INITIAL_CONTACT) {
            mRecipientEntry = recipientEntry;
            mLockQuery = true;
            mIsInitClick = true;
            mRecipientTextView.setText("");
            mRecipientTextView.appendRecipientEntry(recipientEntry);
            //Bkav QuangNDb gone giao dien list contact di khi click o che do init
            mComposeMessageView.getComposeEditText().requestFocus();
            mLockQuery = false;
//            maybeGetOrCreateConversation();
        } else {
            if (!onContactSelected(recipientEntry)) {
                // Bkav QuangNdb doan xu ly sau khi click vao item dc suggest thi xoa doan text vua danh di
                //Bkav QuangNDb Thay doi kich ban khi click vao item dc suggest thi boi den doan text vua type de search
                mRecipientTextView.appendRecipientEntry(recipientEntry);
                DrawableRecipientChip last = mRecipientTextView.getLastChip();
                if (last != null) {
                    mRecipientTextView.setSelection(mRecipientTextView.getText().getSpanEnd(last) + 1, mRecipientTextView.getText().length());
                }
            } else {
                mRecipientTextView.removeRecipientEntry(recipientEntry);
            }
        }
    }

    @Override
    public void maybeGetOrCreateConversation() {
        if (!mCreateConversationSucceeded) {
            new Handler().post(new GetOrCreateConversationRunnable(this));
        }
    }

    // Bkav TienNAb: Them bien luu string nhap vao RecipientTextView
    private String mTempText = "";
    public void maybeGetOrCreateConversationHandler() {
        // TODO: 25/05/2017 QuangNDb sdt khong co trong danh ba xu ly van chua toi uu(TH mRecipientEntry = NULL)
        if (mContactPickingMode == MODE_PICK_INITIAL_CONTACT) {
            final ArrayList<ParticipantData> participants = new ArrayList<>();
            if (mRecipientEntry != null) {
                participants.add(ParticipantData.getFromRecipientEntry(mRecipientEntry));
                setContactPickingMode(MODE_PICK_MORE_CONTACTS, false);
            } else {
                String phoneNumber = mRecipientTextView.getText().toString().replaceAll("[/s \\, \\< \\>]", "");
                if (phoneNumber.isEmpty()) {
                    phoneNumber = mTempText;
                }
                if (MmsSmsUtils.isPhoneNumber(phoneNumber)) {
                    //HienDTk: neu la so dien thoai thi cho hien cua so chat luon tranh loi giao dien hien thi list danh ba roi mat
                    mIsRecipientTextViewFocus = false;
                    showChatInterface();
                    participants.add(ParticipantData.getFromRawPhoneBySystemLocale(phoneNumber));
                    setContactPickingMode(MODE_PICK_MORE_CONTACTS, false);
                } else {
                    // Bkav QuangNDb them bien check neu nhan vao button back press thi k can phai hien toast nen
                    if (!mIsBackPress) {
                        // Them bien huy send
                        mIsRequestSend = false;
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.invalid_contact, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
            resetMonitor();
            if (participants.size() > 0 && mMonitor == null) {
                mMonitor = GetOrCreateConversationAction.getOrCreateConversation(participants,
                        null, this);
//                hideKeyboard();

            }
        } else {
            // Them doan neu an nut send thi chuyen luon sang giao dien conversation
            if (mIsRequestSend || mIsRequestQuickResponse) {
                mRecipientTextView.clearFocus();
            }
            resetMonitor();
            super.maybeGetOrCreateConversation();
        }
    }

    @Override
    protected void setBackgroundRecipientColor() {
        mRecipientTextView.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
    }

    /**
     * Bkav QuangNDb reset lai monitor de co the tao dc thread
     */
    private void resetMonitor() {
        if (mMonitor != null) {
            mMonitor.unregister();
        }
        mMonitor = null;
    }

    @Override
    public boolean onContactSelected(RecipientEntry recipientEntry) {
        return mSelectedPhoneNumbers != null &&
                recipientEntry != null &&
                mSelectedPhoneNumbers.contains(PhoneUtils.getDefault().getCanonicalBySystemLocale(recipientEntry.getDestination()));
    }

    @Override
    protected ContactRecipientAutoCompleteView initRecipient(View view) {
        return (BtalkContactRecipientAutoCompleteView)
                view.findViewById(R.id.recipient_text_view);
    }

    @Override
    public boolean onClose() {
        mSearchView.setIconifiedByDefault(true);
        mComposeMessageView.setVisibility(View.VISIBLE);
        mSearchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        mIsSearchMode = false;
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    private String mSearchString = null;

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
        // Don't do anything if the filter hasn't actually changed.
        // Prevents restarting the loader when restoring state.
        if (mSearchString == null && newFilter == null) {
            return true;
        }
        if (mSearchString != null && mSearchString.equals(newFilter)) {
            return true;
        }
        mSearchString = newFilter == null ? null : newFilter.replace("'", "''");
        mBinding.getData().setSearchString(BtalkCharacterUtil.get().convertToNotLatinCode(mSearchString));
        mBinding.getData().restart(getLoaderManager(), mBinding);
        return true;
    }


    /**
     * Bkav QuangNdb them runnable vao de load duoc icon ngay tren autocompletextview
     * do thuc hien binh thuong nhanh qua view khong ve duoc
     */
    private static class GetOrCreateConversationRunnable implements Runnable {

        private WeakReference<BtalkContactPickerFragment> weakReference;

        public GetOrCreateConversationRunnable(BtalkContactPickerFragment fragment) {
            this.weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void run() {
            if (weakReference != null) {
                weakReference.get().maybeGetOrCreateConversationHandler();
            }
        }
    }

    private static class InitRecipientRunnable implements Runnable {

        private WeakReference<BtalkContactPickerFragment> weakReference;

        public InitRecipientRunnable(BtalkContactPickerFragment fragment) {
            this.weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void run() {
            if (weakReference != null) {
                weakReference.get().initRecipientText();
            }
        }
    }

    private static class ReInitRecipientRunnable implements Runnable {

        private WeakReference<BtalkContactPickerFragment> weakReference;

        public ReInitRecipientRunnable(BtalkContactPickerFragment fragment) {
            this.weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void run() {
            if (weakReference != null) {
                weakReference.get().reInitRecipientTextView();
            }
        }
    }

    @Override
    protected int getIdLayout() {
        return R.layout.btalk_picker_fragment;
    }

    @Override
    protected void initViewPager(View view) {
        final FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        mContactPickerFragment = new BtalkPhoneNumberPickerFragment();
        mContactPickerFragment.setHost(this);
        fragmentTransaction.add(R.id.content_layout, mContactPickerFragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void setViewPagerVisible(int visible) {
        // Bkav QuangNDb khong lam gi
    }

    @Override
    protected void startExplodeTransitionForContactLists(boolean show) {
        // Bkav QuangNDb khong lam gi
    }

    @Override
    protected void toggleContactListItemsVisibilityForPendingTransition(boolean show) {
        // Bkav QuangNDb khong lam gi
    }

    @Override
    protected void showHideContactPagerWithAnimation(boolean show) {
        // Bkav QuangNDb khong lam gi
    }

    @Override
    protected void setComposeDividerVisible(int visible) {
        // Bkav QuangNDb khong lam gi
    }

    @Override
    protected void initLoader() {
        // Bkav QUangNDb khong lam gi
    }

    @Override
    protected void invalidateContactLists() {
        // Bkav QuangNDb khong lam gi
    }

    @Override
    protected void setRecipientTextAdapter() {
        mRecipientTextView.setAdapter(new BtalkContactRecipientAdapter(getActivity(), this));
    }

    private boolean mQueryFirst = true;// check xem co phai query lan dau khong

    private boolean mLockQuery = false;//Bkav QuangNDb Khoa query

    @Override
    protected void onRecipientTextChanged(CharSequence s) {
        String newText = s.toString();
        int count = 0;
        int length = newText.length();
        if ((length == 0 )) {// khong co text k hien thi list lien he nua
            if (mViewListContact != null) {
                showChatInterface();
                mRecyclerView.setAdapter(null);
            }
            if (mRecipientEntries != null) {
                mRecipientEntries.clear();
            }
            mQueryFirst = true;
        } else {
            // Bkav TienNAb: khi focus vao RecipientTextView moi hien thi list danh ba
            if (mIsRecipientTextViewFocus) {
                //TODO TrungTH co text thi hien thi list, khong text thi an
                showContactList();
                mQueryFirst = false;
            }
        }
        if (mIsPaste) {
            for (int i = 0; i < length; i++) {
                if (newText.charAt(i) == ',' || newText.charAt(i) == ';') {
                    count++;
                }
                if (count == 2) {
                    break;
                }
            }
        }


        if (count != 2) {
            if (s.toString().length() > 1 && !mRecipientTextView.getSelectedDestinations().isEmpty()) {
                while (newText.contains(",")) {
                    int commaIndex = newText.indexOf(",");
                    newText = newText.substring(commaIndex + 1, newText.length());
                }
            }
            if (!mLockQuery) {
                mContactPickerFragment.setQueryString(newText.replace("'", "''").trim(), true);
                querySmartContact(newText.trim()); // TODO trungTH ghep source dua vao hoi anhdts
            }
        }
    }


    @Override
    protected int getIdResIconIme() {
        return R.drawable.ic_btalk_ime;
    }

    @Override
    protected int getIdResMenu() {
        return R.menu.btalk_compose_menu;
    }

    @Override
    protected void updateColorStatusBar() {
        // Bkav QuangNDb khong lam gi de khong doi mau status ba nua
    }

    @Override
    protected int getIdResDialPad() {
        return R.drawable.ic_btalk_action_ime_toggle;
    }

    @Override
    protected int getIdResBackIcon() {
        //HienDTk: update icon back
        return R.drawable.ic_arrow_light_message_update;
    }

    @Override
    protected boolean getConditionMode(int mode) {
        return (mContactPickingMode == MODE_CREATE_GROUP_PARTICIPANTS && mode == MODE_PICK_MORE_CONTACTS) || super.getConditionMode(mode);
    }

    @Override
    protected void showAddKeyboard() {
        // Bkav QuangNDB khong lam gi
    }

    @Override
    protected void showStateGroupMode(MenuItem addMoreParticipantsItem, MenuItem confirmParticipantsItem) {
        addMoreParticipantsItem.setVisible(false);
        confirmParticipantsItem.setVisible(false);
        // BKAV quangNDb tach code set visible view pager
        setViewPagerVisible(View.VISIBLE);
        setComposeDividerVisible(View.INVISIBLE);
        mRecipientTextView.setEnabled(true);
        showImeKeyboard();
    }

    @Override
    protected boolean getConditionShowKeyboardIcon() {
        return mContactPickingMode == MODE_PICK_INITIAL_CONTACT || mContactPickingMode == MODE_CREATE_GROUP_PARTICIPANTS;
    }

    @Override
    protected boolean getConditionChangeToAddMoreParticipantState() {
        return mContactPickingMode == MODE_PICK_INITIAL_CONTACT && !mIsRequestSend && !mIsRequestQuickResponse;
    }

    @Override
    protected boolean getConditionComplete() {
        return mContactPickingMode == MODE_PICK_INITIAL_CONTACT ||
                mContactPickingMode == MODE_PICK_MAX_PARTICIPANTS ||
                mIsRequestSend || mIsRequestQuickResponse;
    }

    @Override
    public void onContactChipsChanged(int oldCount, int newCount) {
        super.onContactChipsChanged(oldCount, newCount);
        mRecipientTextView.sanitizeContactChips();
        if (mQueryFirst) {
            //Bkav QuangNDb Them xu ly khi text rong tu chuyen ve che do init contact
            if (mContactPickingMode != mFistPickingMode) {
                setContactPickingMode(mFistPickingMode, true);
            }
        }
    }

    @Override
    protected void showInvalidContactToast(int prunedCount) {
        if (mContactPickingMode == MODE_PICK_INITIAL_CONTACT || mIsBackPress) {
            // Bkav QuangNDb khong lam gi vi da xu ly so dien thoai khong hop le maybeGetOrCreateConversation
        } else {
            if (!mRecipientTextView.getSelectedDestinations().isEmpty()) {
                deleteExtractText();
            } else {
                super.showInvalidContactToast(prunedCount);
            }
        }
    }

    @Override
    protected void deleteAllText() {
        // TODO: 31/05/2017 QuangNDb bo Assert.equals(MODE_PICK_INITIAL_CONTACT, mContactPickingMode);
        // vi minh co them 1 trang thai group them vao se chet , thay bang doan code check khac
        if (mContactPickingMode == MODE_PICK_INITIAL_CONTACT || mContactPickingMode == MODE_CREATE_GROUP_PARTICIPANTS) {
            mRecipientTextView.setText("");
        }
    }

    private boolean mIsPaste = false;

    @Override
    public boolean onPasteParticipant(String participants) {
        mIsPaste = true;
        if (mContactPickingMode == MODE_PICK_INITIAL_CONTACT) {
            ClipboardManager clipboard = (ClipboardManager) Factory.get().getApplicationContext().getSystemService(
                    Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();
            if (clipData == null) {
                return false;
            }
            final ClipDescription clipDesc = clipData.getDescription();
            boolean containsSupportedType = clipDesc.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    || clipDesc.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML);
            if (!containsSupportedType) {
                return false;
            }
            final ClipDescription clipDescription = clipData.getDescription();
            final String mimeType = clipDescription.getMimeType(0);
            final boolean supportedType = ClipDescription.MIMETYPE_TEXT_PLAIN.equals(mimeType)
                    || ClipDescription.MIMETYPE_TEXT_HTML.equals(mimeType);
            if (!supportedType) {
                // Only plain text and html can be pasted.
                return false;
            }
            final CharSequence pastedItem = clipData.getItemAt(0).getText();
            if (TextUtils.isEmpty(pastedItem)) {
                return false;
            }
            //Bkav QuangNDb noi dung paste co chua dau ; hoac , ma la nhieu lien he thi chuyen sang che do group
            if ((pastedItem.toString().contains(",") || pastedItem.toString().contains(";"))
                    && (pastedItem.toString().indexOf(",") != pastedItem.toString().lastIndexOf(",")
                    || pastedItem.toString().indexOf(";") != pastedItem.toString().lastIndexOf(";")
                    || pastedItem.toString().trim().lastIndexOf(",") != (pastedItem.toString().trim().length() - 1)
                    || pastedItem.toString().trim().lastIndexOf(";") != (pastedItem.toString().trim().length() - 1))) {
                // Chuyen sang che do group khi nguoi dung dan nhieu so dien thoai
                mContactPickingMode = MODE_CREATE_GROUP_PARTICIPANTS;
                return false;
            }
        }
        return false;
    }

    //    -----------------------------------------------------------THEM COMPOSE VIEW--------------------------------------------
    private ImmutableBindingRef<DraftMessageData> mDraftMessageDataModel;

    // If the fragment receives a draft as part of the invocation this is set
    private MessageData mIncomingDraft;

    // be reloaded from db
    private boolean mClearLocalDraft;

    @Override
    public void onDraftChanged(DraftMessageData data, int changeFlags) {
        mDraftMessageDataModel.ensureBound(data);
        // We're specifically only interested in ATTACHMENTS_CHANGED from the widget. Ignore
        // other changes. When the widget changes an attachment, we need to reload the draft.
        if (changeFlags ==
                (DraftMessageData.WIDGET_CHANGED | DraftMessageData.ATTACHMENTS_CHANGED)) {
            mClearLocalDraft = true;        // force a reload of the draft in onResume
        }
    }

    @Override
    public void onDraftAttachmentLimitReached(DraftMessageData data) {

    }

    @Override
    public void onDraftAttachmentLoadFailed() {

    }

    @Override
    public int getConversationSelfSubId() {
        final String selfParticipantId = mComposeMessageView.getConversationSelfId();
        final ParticipantData self = mBinding.getData().getSelfParticipantById(selfParticipantId);
        // If the self id or the self participant data hasn't been loaded yet, fallback to
        // the default setting.
        return self == null ? ParticipantData.DEFAULT_SELF_SUB_ID : self.getSubId();
    }

    @Override
    public void invalidateActionBar() {
        mHost.invalidateActionBar();
    }

    @Override
    public void setOptionsMenuVisibility(boolean visible) {
        // Bkav TienNAb: sua lai logic hien thi option menu
        setHasOptionsMenu(true);
        if (inputManager != null) {
            if (inputManager.getMediaInput().isFullScreen()) {
                setHasOptionsMenu(false);
            }
        }
    }

    @Override
    public void dismissActionMode() {
        mHost.dismissActionMode();
    }

    @Override
    public void selectSim(SubscriptionListData.SubscriptionListEntry subscriptionData) {
        mComposeMessageView.selectSim(subscriptionData);
        // Bkav HuyNQN bam vao item se khong gui tin nhan di luon
        /*if (isAlwaysAskSim()) {
            mComposeMessageView.sendMessageInternal(true);
        }*/
        //Bkav QuangNDb doi bien selecsim sang true
        mIsSelectedSim = true;
    }

    @Override
    public void onStartComposeMessage() {

    }

    @Override
    public SimSelectorView getSimSelectorView() {
        return (BtalkSimSelectorView) getView().findViewById(R.id.sim_selector);
    }

    @Override
    public MediaPicker createMediaPicker() {
        return new BtalkMediaPicker();
    }

    @Override
    public void showHideSimSelector(boolean show) {

    }

    @Override
    public int getSimSelectorItemLayoutId() {
        return R.layout.btalk_sim_selector_item_view;
    }

    @Override
    public void onConversationMessagesCursorUpdated(ConversationData data, Cursor cursor, @Nullable ConversationMessageData newestMessage, boolean isSync) {
        mBinding.ensureBound(data);
        if (cursor != null) {
            mIsEmptyMessage = cursor.getCount() == 0;
        }
        // This needs to be determined before swapping cursor, which may change the scroll state.
        final boolean scrolledToBottom = isScrolledToBottom();
        final int positionFromBottom = getScrollPositionFromBottom();
        // If participants not loaded, assume 1:1 since that's the 99% case
        final boolean oneOnOne =
                !data.getParticipantsLoaded() || data.getOtherParticipant() != null;
        mAdapter.setOneOnOne(oneOnOne, false /* invalidate */);
        final Cursor oldCursor = mAdapter.swapCursor(cursor);
        invalidateOptionsMenu();
        if (isSync) {
            // This is a message sync. Syncing messages changes cursor item count, which would
            // implicitly change RV's scroll position. We'd like the RV to keep scrolled to the same
            // relative position from the bottom (because RV is stacked from bottom), so that it
            // stays relatively put as we sync.
            final int position = Math.max(mAdapter.getItemCount() - 1 - positionFromBottom, 0);
            scrollToPosition(position, false /* smoothScroll */);
        } else if (newestMessage != null) {
            // Show a snack bar notification if we are not scrolled to the bottom and the new
            // message is an incoming message.
            if (!scrolledToBottom && newestMessage.getIsIncoming()) {
                // If the conversation activity is started but not resumed (if another dialog
                // activity was in the foregrond), we will show a system notification instead of
                // the snack bar.
                if (mBinding.getData().isFocused()) {
                    UiUtils.showSnackBarWithCustomAction(getActivity(),
                            getView().getRootView(),
                            getString(com.android.messaging.R.string.in_conversation_notify_new_message_text),
                            SnackBar.Action.createCustomAction(new Runnable() {
                                                                   @Override
                                                                   public void run() {
                                                                       scrollToBottom(true /* smoothScroll */);
                                                                       mComposeMessageView.hideAllComposeInputs(false /* animate */);
                                                                   }
                                                               },
                                    getString(com.android.messaging.R.string.in_conversation_notify_new_message_action)),
                            null /* interactions */,
                            SnackBar.Placement.above(mComposeMessageView));
                }
            } else {
                // We are either already scrolled to the bottom or this is an outgoing message,
                // scroll to the bottom to reveal it.
                // Don't smooth scroll if we were already at the bottom; instead, we scroll
                // immediately so RecyclerView's view animation will take place.
                scrollToBottom(!scrolledToBottom);
            }
        }

        mIsMessageLoaded = true;
        if (isLoadAllFinish()) {
            sendMessageIfRequest();
        }

    }

    private void scrollToBottom(final boolean smoothScroll) {
        if (mAdapter.getItemCount() > 0) {
            scrollToPosition(mAdapter.getItemCount() - 1, smoothScroll);
        }
    }

    private static final int JUMP_SCROLL_THRESHOLD = 15;

    private void scrollToPosition(final int targetPosition, final boolean smoothScroll) {
        if (smoothScroll) {
            final int maxScrollDelta = JUMP_SCROLL_THRESHOLD;

            final LinearLayoutManager layoutManager =
                    (LinearLayoutManager) mRecyclerView.getLayoutManager();
            final int firstVisibleItemPosition =
                    layoutManager.findFirstVisibleItemPosition();
            final int delta = targetPosition - firstVisibleItemPosition;
            final int intermediatePosition;

            if (delta > maxScrollDelta) {
                intermediatePosition = Math.max(0, targetPosition - maxScrollDelta);
            } else if (delta < -maxScrollDelta) {
                final int count = layoutManager.getItemCount();
                intermediatePosition = Math.min(count - 1, targetPosition + maxScrollDelta);
            } else {
                intermediatePosition = -1;
            }
            if (intermediatePosition != -1) {
                mRecyclerView.scrollToPosition(intermediatePosition);
            }
            mRecyclerView.smoothScrollToPosition(targetPosition);
        } else {
            mRecyclerView.scrollToPosition(targetPosition);
        }
    }

    private int getScrollPositionFromBottom() {
        final LinearLayoutManager layoutManager =
                (LinearLayoutManager) mRecyclerView.getLayoutManager();
        final int lastVisibleItem =
                layoutManager.findLastVisibleItemPosition();
        return Math.max(mAdapter.getItemCount() - 1 - lastVisibleItem, 0);
    }

    private boolean isScrolledToBottom() {
        if (mRecyclerView.getChildCount() == 0) {
            return true;
        }
        final View lastView = mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1);
        int lastVisibleItem = ((LinearLayoutManager) mRecyclerView
                .getLayoutManager()).findLastVisibleItemPosition();
        if (lastVisibleItem < 0) {
            // If the recyclerView height is 0, then the last visible item position is -1
            // Try to compute the position of the last item, even though it's not visible
            final long id = mRecyclerView.getChildItemId(lastView);
            final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForItemId(id);
            if (holder != null) {
                lastVisibleItem = holder.getAdapterPosition();
            }
        }
        final int totalItemCount = mRecyclerView.getAdapter().getItemCount();
        final boolean isAtBottom = (lastVisibleItem + 1 == totalItemCount);
        return isAtBottom && lastView.getBottom() <= mRecyclerView.getHeight();
    }

    // ConversationMessageView that is currently selected
    private ConversationMessageView mSelectedMessage;

    @Override
    public void onConversationMetadataUpdated(ConversationData data) {
        mBinding.ensureBound(data);
        if (mSelectedMessage != null && mSelectedAttachment != null) {
            // We may have just sent a message and the temp attachment we selected is now gone.
            // and it was replaced with some new attachment.  Since we don't know which one it
            // is we shouldn't reselect it (unless there is just one) In the multi-attachment
            // case we would just deselect the message and allow the user to reselect, otherwise we
            // may act on old temp data and may crash.
            final List<MessagePartData> currentAttachments = mSelectedMessage.getData().getAttachments();
            if (currentAttachments.size() == 1) {
                mSelectedAttachment = currentAttachments.get(0);
            } else if (!currentAttachments.contains(mSelectedAttachment)) {
                selectMessage(null);
            }
        }
        // Ensure that the action bar is updated with the current data.
        mAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
        mIsMetaLoaded = true;
        if (isLoadAllFinish()) {
            sendMessageIfRequest();
        }
    }

    protected void selectMessage(final ConversationMessageView messageView) {
        selectMessage(messageView, null /* attachment */);
    }

    protected void selectMessage(final ConversationMessageView messageView,
                                 final MessagePartData attachment) {
        mSelectedMessage = messageView;
        if (mSelectedMessage == null) {
            mAdapter.setSelectedMessage(null);
            mHost.dismissActionMode();
            mSelectedAttachment = null;
            return;
        }
        mSelectedAttachment = attachment;
        mAdapter.setSelectedMessage(messageView.getData().getMessageId());
        // Bkav QuangNDb tach code doan start action mode
        startActionMode();
    }

    /**
     * Bkav QuangNDb Cap nhat lai action bar
     */
    private void invalidateOptionsMenu() {
        if (mIsSearchMode) {
            return;
        }
        final Activity activity = getActivity();
        // TODO: Add the supportInvalidateOptionsMenu call to the host activity.
        if (activity == null || !(activity instanceof BugleActionBarActivity)) {
            return;
        }
        ((BugleActionBarActivity) activity).supportInvalidateOptionsMenu();
    }

    @Override
    public void closeConversation(String conversationId) {
        if (TextUtils.equals(conversationId, mConversationId) && !CONVERSATION_DRAF_ID.equals(mConversationId)) {
            mHost.onFinishCurrentConversation();
            // TODO: Explicitly transition to ConversationList (or just go back)?
        }
    }

    @Override
    public void onConversationParticipantDataLoaded(ConversationData data) {
        mBinding.ensureBound(data);
        if (mBinding.getData().getParticipantsLoaded()) {
            final boolean oneOnOne = mBinding.getData().getOtherParticipant() != null;
            mAdapter.setOneOnOne(oneOnOne, true /* invalidate */);

            // refresh the options menu which will enable the "people & options" item.
            // Bkav QuangNDb tach code doan reset option menu
            invalidateOptionsMenu();
//            mHost.invalidateActionBar();
            mHost.onConversationParticipantDataLoaded
                    (mBinding.getData().getNumberOfParticipantsExcludingSelf());
        }

        mIsParticipantLoaded = true;
        if (isLoadAllFinish()) {
            sendMessageIfRequest();
        }
    }

    @Override
    public void onSubscriptionListDataLoaded(ConversationData data) {
        mBinding.ensureBound(data);
        mIsSubscriptionLoaded = true;
        if (isLoadAllFinish()) {
            sendMessageIfRequest();
        }
    }

    private boolean mIsSent = false;

    private MessageData mMessageSend;

    private QuickResponseData mQuickResponseData;

    protected boolean mIsRequestQuickResponse = false;

    @Override
    public void sendQuickResponse(Object data) {
        if (!mIsClickConfirm && mConfirmParticipantsItem != null && mConfirmParticipantsItem.isVisible()) {
            resetAllLoad();
            mRecipientTextView.clearFocus();
            mIsRequestQuickResponse = true;
            if (!(data instanceof QuickResponseData)) {
                return;
            } else {
                mQuickResponseData = (QuickResponseData) data;
            }
            clickConfirmButton();
        }

    }

    @Override
    public void keyboardShow() {
        mComposeMessageView.hideInput();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    @Override
    public void onMediaPickerStateChange(boolean isShow) {
        if (isShow) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }else {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public void emojiClick() {
        // Bkav TienNAb: luu lai string nhap vao RecipientTextView khi an icon emoji
        mTempText = mRecipientTextView.getText().toString();
    }

    @Override
    public void sendMessage(final MessageData message) {
        resetAllLoad();
        mRecipientTextView.clearFocus();
        mIsRequestSend = true;
        if (!mIsClickConfirm && mConfirmParticipantsItem != null && mConfirmParticipantsItem.isVisible()) {
            mMessageSend = message;
            clickConfirmButton();
            return;
        }
        if (!CONVERSATION_DRAF_ID.equals(mConversationId)) {
            sendInternalMessage(message);
        }

    }


    @Override
    public boolean isFinishLoadDraf() {
        return (mIsRequestSend && !mIsSent);
    }

    /**
     * Bkav QuangNDb send message truc tiep
     */
    private void sendInternalMessage(final MessageData message) {
        if (isReadyForAction()) {
            if (ensureKnownRecipients()) {
                // Merge the caption text from attachments into the text body of the messages
                message.consolidateText();

                mBinding.getData().sendMessage(mBinding, message);
                mComposeMessageView.resetMediaPickerState();
                mIsSent = true;
            } else {
                LogUtil.w(LogUtil.BUGLE_TAG, "Message can't be sent: conv participants not loaded");
            }
        } else {
            warnOfMissingActionConditions(true /*sending*/,
                    new Runnable() {
                        @Override
                        public void run() {
                            sendMessage(message);
                        }
                    });
        }
    }

    /**
     * Bkav QuangNDb Chac chan rang dia chi di la hop le
     */
    private boolean ensureKnownRecipients() {
        final ConversationData conversationData = mBinding.getData();

        if (!conversationData.getParticipantsLoaded()) {
            // We can't tell yet whether or not we have an unknown recipient
            return false;
        }

        final ConversationParticipantsData participants = conversationData.getParticipants();
        for (final ParticipantData participant : participants) {


            if (participant.isUnknownSender()) {
                UiUtils.showToast(com.android.messaging.R.string.unknown_sender);
                return false;
            }
        }

        return true;
    }

    @Override
    public void onComposeEditTextFocused() {
    }




    @Override
    public void onAttachmentsCleared() {
        // When attachments are removed, reset transient media picker state such as image selection.
        mComposeMessageView.resetMediaPickerState();
    }

    @Override
    public void onAttachmentsChanged(boolean haveAttachments) {

    }

    @Override
    public void displayPhoto(Uri photoUri, Rect imageBounds, boolean isDraft) {
        displayPhoto(photoUri, imageBounds, isDraft, mConversationId, getActivity());
    }

    public static void displayPhoto(final Uri photoUri, final Rect imageBounds,
                                    final boolean isDraft, final String conversationId, final Activity activity) {
        final Uri imagesUri =
                isDraft ? MessagingContentProvider.buildDraftImagesUri(conversationId)
                        : MessagingContentProvider.buildConversationImagesUri(conversationId);
        UIIntents.get().launchFullScreenPhotoViewer(
                activity, photoUri, imageBounds, imagesUri);
    }

    @Override
    public void promptForSelfPhoneNumber() {
        if (mComposeMessageView != null) {
            // Avoid bug in system which puts soft keyboard over dialog after orientation change
            hideKeyboard();
        }

        final FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        final EnterSelfPhoneNumberDialog dialog = EnterSelfPhoneNumberDialog
                .newInstance(getConversationSelfSubId());
        dialog.setTargetFragment(this, 0/*requestCode*/);
        dialog.show(ft, null/*tag*/);
    }

    @Override
    public boolean isReadyForAction() {
        return UiUtils.isReadyForAction();
    }

    private ChangeDefaultSmsAppHelper mChangeDefaultSmsAppHelper;

    @Override
    public void warnOfMissingActionConditions(boolean sending, Runnable commandToRunAfterActionConditionResolved) {
        if (mChangeDefaultSmsAppHelper == null) {
            mChangeDefaultSmsAppHelper = new ChangeDefaultSmsAppHelper();
        }
        if(getView().getRootView() != null) { // Bkav HuyNQN fix loi BOS-2804
            mChangeDefaultSmsAppHelper.warnOfMissingActionConditions(sending,
                    commandToRunAfterActionConditionResolved, mComposeMessageView,
                    getView().getRootView(),
                    getActivity(), this);
        }
    }

    @Override
    public void warnOfExceedingMessageLimit(boolean showAttachmentChooser, boolean tooManyVideos) {
        ConversationFragment.warnOfExceedingMessageLimit(showAttachmentChooser, mComposeMessageView, CONVERSATION_DRAF_ID,
                getActivity(), tooManyVideos);
    }

    @Override
    public void notifyOfAttachmentLoadFailed() {
        UiUtils.showToastAtBottom(com.android.messaging.R.string.attachment_load_failed_dialog_message);
    }

    @Override
    public void showAttachmentChooser() {
        ConversationFragment.showAttachmentChooser(mConversationId, getActivity());
    }

    @Override
    public boolean shouldShowSubjectEditor() {
        return true;
    }

    @Override
    public boolean shouldHideAttachmentsWhenSimSelectorShown() {
        return false;
    }

    @Override
    public Uri getSelfSendButtonIconUri() {
        return null;
    }

    @Override
    public int overrideCounterColor() {
        return -1;
    }

    @Override
    public int getAttachmentsClearedFlags() {
        return DraftMessageData.ATTACHMENTS_CHANGED;
    }

    @Override
    public void onReadyMessage() {
    }

    @Override
    protected void setUpComposeMessageView(Bundle savedInstanceState) {
        mBinding.ensureBound();
        mBinding.getData().init(getLoaderManager(), mBinding);
        // Bkav TienNAb: check mediapicker co hien thi hay khong
        boolean isMediaPickerShowing = false;
        if (inputManager != null && inputManager.getMediaInput().isShowing()) {
            isMediaPickerShowing = true;
        }
        inputManager = new BtalkConversationInputManager(
                getActivity(), this, mComposeMessageView, mHost, getChildFragmentManager(),
                mBinding, mComposeMessageView.getDraftDataModel(), savedInstanceState);
        // Bkav TienNAb: set che do hien thi cho mediapicker
        inputManager.getMediaInput().setShowing(isMediaPickerShowing);
        mComposeMessageView.setInputManager(inputManager);
        mComposeMessageView.setConversationDataModel(BindingBase.createBindingReference(mBinding));
        mDraftMessageDataModel =
                BindingBase.createBindingReference(mComposeMessageView.getDraftDataModel());
        mDraftMessageDataModel.getData().addListener(this);
    }

    private ConversationInputManager inputManager;

    @Override
    protected void initComposeMessageView(View view) {
        mComposeMessageView = (BtalkPickerComposeMessageView) view.findViewById(R.id.message_compose_view_container);
        mComposeMessageView.setIsAskBeforeSendSms(BtalkUiUtils.isAlwaysAskBeforeSendSms());
        mComposeMessageView.setParentView(mParentView);
        mComposeMessageView.setActionBarDeltaSizePixel();
        // Bind the compose message view to the DraftMessageData
        mComposeMessageView.bind(DataModel.get().createDraftMessageData(
                mBinding.getData().getConversationId()), this);
    }

    @Override
    protected void initParentView(View view) {
        mParentView = (ViewGroup) view.findViewById(R.id.parent_view);
        mRecyclerViewEsim = mParentView.findViewById(R.id.list_esim);
    }

    public boolean onNavigationUpPressed() {
        return mComposeMessageView.onNavigationUpPressed();
    }

    @Override
    protected void unbinComposeMessageView() {
        mComposeMessageView.unbind();
        // And unbind this fragment from its data
        mBinding.unbind();
    }

    public boolean onBackPressed() {
        return mComposeMessageView.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("HienDTk", "onResume: ");
        // Bkav HuyNQN fix loi khong mo ban phim len khi goi tu hotseat do mRecipientTextView luc do chua khoi tao xong
        //HienDTk: Neu bam vao de cho hien media thi khong cho ban phim nua
        if(!mComposeMessageView.isShowKeyboad()){
            initRecipientTextRunnable();
        }

        if (mIncomingDraft == null) {
            mComposeMessageView.requestDraftMessage(mClearLocalDraft);
        } else {
            mComposeMessageView.setDraftMessage(mIncomingDraft);
            mIncomingDraft = null;
        }
        mClearLocalDraft = false;
        setConversationFocus();
        // On resume, invalidate all message views to show the updated timestamp.
        mAdapter.notifyDataSetChanged();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mConversationSelfIdChangeReceiver,
                new IntentFilter(UIIntents.CONVERSATION_SELF_ID_CHANGE_BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mIsClickConfirm && mConfirmParticipantsItem != null
                && mConfirmParticipantsItem.isVisible()) {
            updateDraft();
        }
        if (mComposeMessageView != null && !CONVERSATION_DRAF_ID.equals(mConversationId)) {
            mComposeMessageView.writeDraftMessage();
        }else{
            //Bkav QuangNDb neu chua chon lien he thi luu draft vao preference
            final MessageData draftMessageData = mComposeMessageView.getDraftHasContentData();
            PrefUtils.get().saveDraftPreferences(getActivity(), PrefUtils.DRAFT_DATA, draftMessageData);
        }
        mBinding.getData().unsetFocus();
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mConversationSelfIdChangeReceiver);
    }

    /**
     * Bkav QuangNDb update draft khi quay lai
     */
    private void updateDraft() {
        final ArrayList<ParticipantData> participants =
                mRecipientTextView.getRecipientParticipantDataForConversationCreation();
        if (ContactPickerData.isTooManyParticipants(participants.size())) {
            UiUtils.showToast(R.string.too_many_participants);
        } else if (participants.size() > 0) {
            final ArrayList<String> recipients =
                    BugleDatabaseOperations.getRecipientsFromConversationParticipants(participants);
            final long threadId = MmsUtils.getOrCreateThreadId(Factory.get().getApplicationContext(),
                    recipients);
            if (threadId < 0) {
                return;
            }
            final DatabaseWrapper db = DataModel.get().getDatabase();
            mConversationId = BugleDatabaseOperations.getOrCreateConversation(db, threadId,
                    false, participants, false, false, null);
            //Bkav QuangNDb xoa draft cu neu da luu truoc do
            if (!CONVERSATION_DRAF_ID.equals(mComposeMessageView.getDraftMessageData().getConversationId())) {
                if (mComposeMessageView.getDraftMessageData().hasAttachments()) {
                    mComposeMessageView.getDraftMessageData().clearAttachments(DraftMessageData.ATTACHMENTS_CHANGED);
                }
                mComposeMessageView.clearDraftMessage();
            }

            //Bkav QuangNDb bin lai compose de luu draft moi
            mComposeMessageView.unbind();
            mBinding.unbind();
            initBinding();
            initAdapter();
            mRecyclerView.setAdapter(mAdapter);
            mComposeMessageView.bind(DataModel.get().createDraftMessageData(
                    mBinding.getData().getConversationId()), this);
            setUpComposeMessageView(mSaveInstanceState);
        }
    }

    private final BroadcastReceiver mConversationSelfIdChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String conversationId =
                    intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID);
            final String selfId =
                    intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_SELF_ID);
            Assert.notNull(conversationId);
            Assert.notNull(selfId);
            if (TextUtils.equals(mBinding.getData().getConversationId(), conversationId)) {
                mComposeMessageView.updateConversationSelfIdOnExternalChange(selfId);
            }
        }
    };


    protected boolean mIsEmptyMessage = false;//Bkav QuangNDb Bien check xem co phai cuoc hoi thoai rong hay khong
    protected boolean mIsSelectedSim = false;//Bkav QuangNDb Bien check nguoi dung da chon sim ngay tu dau hay chua

    @Override
    public boolean isAlwaysAskSim() {
        //Bkav QuangNDb sua loi cu always ask sim la send message luon, them check has draft moi send, them nguoi dung chua choose sim moi send
        return mIsEmptyMessage && BtalkUiUtils.isAlwaysAskBeforeSendSms()
                && mComposeMessageView.getDraftToView() != null
                && !mIsSelectedSim;
    }

    // Bkav HienDTk: dong lai vi da tao ham dung chung o class BtalkUiUtils
    /**
     * Bkav QuangNDb ham check xem nguoi dung co de luon hoi khi nhan tin khong
     */
//    private boolean  isAlwaysAskBeforeSendSms() {
//        try {
//            Method isSMSPromptEnabledMethod = SmsManager.class.getMethod("isSMSPromptEnabled");
//            isSMSPromptEnabledMethod.setAccessible(true);
//            return (boolean) isSMSPromptEnabledMethod.invoke(SmsManager.getDefault());
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    @Override
    public void onGetOrCreateConversationSucceeded(ActionMonitor monitor, Object data, String conversationId) {
        if (mContactPickingMode != MODE_CREATE_GROUP_PARTICIPANTS
                && !conversationId.equals(mConversationId)) {
            final MessageData draftMessageData = mComposeMessageView.getDraftHasContentData();
            final String currentSelfId = mComposeMessageView.getConversationSelfId();// self id hien tai
            mComposeMessageView.unbind();
            mBinding.unbind();
            mConversationId = conversationId;
            initBinding();
            initAdapter();
            mRecyclerView.setAdapter(mAdapter);
            mComposeMessageView.bind(DataModel.get().createDraftMessageData(
                    mBinding.getData().getConversationId()), this);
            setUpComposeMessageView(mSaveInstanceState);
            mComposeMessageView.getComposeEditText().requestFocus();
            showChatInterface();
            mContactPickingMode = MODE_PICK_MORE_CONTACTS;
            if (draftMessageData == null) {// neu nguoi dung chua soan draf thi moi load draf cua conversation
                mComposeMessageView.requestDraftMessage(true);
            } else {
                mComposeMessageView.setDraftMessage(draftMessageData);
            }
            //Bkav QuangNDb giu nguyen trang thai sim khi nguoi dung da chon sim tu truoc
            if (mIsSelectedSim && currentSelfId != null && !currentSelfId.equals("-1")) {
                mComposeMessageView.updateConversationSelfIdOnExternalChange(currentSelfId);
            }
            mClearLocalDraft = false;
            setConversationFocus();
            resetQueryIfNeed();
        } else if (conversationId.equals(mConversationId)) {// neu cung contact thi k refresh lai list tin nhan nua
            invalidateActionBar();
            invalidateOptionsMenu();
            sendMessageIfRequest();
            showChatInterface();
        }
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        //Bkav QuangNDb clear draft mac dinh khi cap nhat thanh cong 1 conversation
        PrefUtils.get().saveDraftPreferences(getActivity(), PrefUtils.DRAFT_DATA, null);
    }

    /**
     * Bkav QuangNDb reset query neu can
     */
    private void resetQueryIfNeed() {
        if (mIsInitClick) {
            mIsInitClick = false;
            mContactPickerFragment.setQueryString("", true);// reset query
        }
    }

    /**
     * Bkav QuangNDb gui tin nhan neu duoc request
     */
    private void sendMessageIfRequest() {
        if (mIsRequestSend && !mIsSent) {
            mComposeMessageView.sendMessageInternal(true);
        }
        if (mIsRequestQuickResponse && !mIsSent) {
            mComposeMessageView.sendMessageWithQuickResponse(mQuickResponseData);
        }
    }

    @Override
    public void onGetOrCreateConversationFailed(ActionMonitor monitor, Object data) {
        mIsRequestSend = false;
        mIsRequestQuickResponse = false;
        mIsClickConfirm = false;
        if (getActivity() != null) {
            Toast.makeText(getActivity(), R.string.invalid_contact, Toast.LENGTH_SHORT).show();
        }
        super.onGetOrCreateConversationFailed(monitor, data);
    }

    private boolean mIsClickConfirm = false;//Bkav QuangNDb Bien de nhan biet la click vao confirm button

    @Override
    protected void clickConfirmButton() {
        if (mQueryFirst) {
            return;
        }
        super.clickConfirmButton();
        mIsClickConfirm = true;
    }

    //Bkav QuangNDb Xoa text thua neu co
    private void deleteExtractText() {
        if (!mRecipientTextView.getSelectedDestinations().isEmpty()) {
            String newText = mRecipientTextView.getText().toString();
            int commaIndex = 0;
            if (newText.contains(",")) {
                commaIndex = newText.lastIndexOf(",");
            }
            mRecipientTextView.getText().delete(commaIndex + 2, mRecipientTextView.length());
        }
    }

    /**
     * Bkav QuangNDb Ham an ban phim di
     */
    private void hideKeyboard() {
        ImeUtil.hideSoftInput(Factory.get().getApplicationContext(), mComposeMessageView);
    }

    private View mConversationComposeDivider;

    protected RecyclerView mRecyclerView;

    protected ConversationMessageAdapter mAdapter;

    private View mViewListContact;//Bkav QuangNDb view chua list contact

    @Override
    protected void initListMessage(View view) {
        initAdapter();
        mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);
        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setStackFromEnd(true);
        manager.setReverseLayout(false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setScrollBarSize(0);
        mRecyclerView.setAdapter(mAdapter);
        mConversationComposeDivider = view.findViewById(R.id.conversation_compose_divider);
        mScrollToDismissThreshold = ViewConfiguration.get(getActivity()).getScaledTouchSlop();
//        mRecyclerView.addOnScrollListener(mListScrollListener);
        mViewListContact = view.findViewById(R.id.content_layout);
        mViewListContact.setVisibility(View.GONE);
    }

    private int mScrollToDismissThreshold;

    private final RecyclerView.OnScrollListener mListScrollListener =
            new RecyclerView.OnScrollListener() {
                // Keeps track of cumulative scroll delta during a scroll event, which we may use to
                // hide the media picker & co.
                private int mCumulativeScrollDelta;

                private boolean mScrollToDismissHandled;

                private boolean mWasScrolledToBottom = true;

                private boolean mWasScrolledToTop = false;

                private int mScrollState = RecyclerView.SCROLL_STATE_IDLE;

                @Override
                public void onScrollStateChanged(final RecyclerView view, final int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        // Reset scroll states.
                        mCumulativeScrollDelta = 0;
                        mScrollToDismissHandled = false;
                    } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        mRecyclerView.getItemAnimator().endAnimations();
                    }
                    mScrollState = newState;
                }

                @Override
                public void onScrolled(final RecyclerView view, final int dx, final int dy) {
                    if (mScrollState == RecyclerView.SCROLL_STATE_DRAGGING &&
                            !mScrollToDismissHandled) {
                        mCumulativeScrollDelta += dy;
                        // Dismiss the keyboard only when the user scroll up (into the past).
                        if (mCumulativeScrollDelta < -mScrollToDismissThreshold) {
                            mComposeMessageView.hideAllComposeInputs(false /* animate */);
                            mScrollToDismissHandled = true;
                        }
                    }
                    if (mWasScrolledToBottom != isScrolledToBottom()) {
//                        mConversationComposeDivider.animate().alpha(isScrolledToBottom() ? 0 : 1);
                        mWasScrolledToBottom = isScrolledToBottom();
                    }
                    // Bkav QuangNDb them doan xu ly khi recycler view scroll den top
                    if (mWasScrolledToTop != isScrolledToTop()) {
                        setAlphaShadowActionbar(isScrolledToTop());
                        mWasScrolledToTop = isScrolledToTop();
                    }
                }
            };

    /**
     * Bkav QuangNDb ham xu li khi action ba keo nen top hoac khong
     */
    protected void setAlphaShadowActionbar(boolean scrolledToTop) {
        // Bkav QuangNDb khong lam gi custom lai o class cha
    }

    /**
     * Bkav QuangNDb ham check recycler view scroll to top
     */
    protected boolean isScrolledToTop() {
        return false;
    }

    /**
     * Bkav QuangNDb Khoi tao adapter list message
     */
    protected void initAdapter() {

        mAdapter = new BtalkConversationMessageAdapter(Factory.get().getApplicationContext(), null, this,
                null,
                // Sets the item click listener on the Recycler item views.
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final ConversationMessageView messageView = (BtalkConversationMessageView) v;
                        if (isMessageListSelectMode()) {
                            selectMessage(messageView, null, false);
                        } else {
                            handleMessageClick(messageView);
                        }
                    }
                },
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View view) {
                        selectMessage((BtalkConversationMessageView) view, null, true);
                        return true;
                    }
                }
        );

        mAdapter.setListener(this);
    }


    protected void handleMessageClick(final ConversationMessageView messageView) {
        if (messageView != mSelectedMessage) {
            final ConversationMessageData data = messageView.getData();
            final boolean isReadyToSend = isReadyForAction();
            if (data.getOneClickResendMessage()) {
                // Directly resend the message on tap if it's failed
                retrySend(data.getMessageId());
                selectMessage(null);
            } else if (data.getShowResendMessage() && isReadyToSend) {
                // Select the message to show the resend/download/delete options
                selectMessage(messageView);
            } else if (data.getShowDownloadMessage() && isReadyToSend) {
                // Directly download the message on tap
                retryDownload(data.getMessageId());
            } else {
                // Let the toast from warnOfMissingActionConditions show and skip
                // selecting
//                 Bkav QuangNdb bo xung them ham show or hide sub info message
                messageView.showOrHideSubInfoWhenClick();
                clickCompleteMessage();
            }
        } else {
            selectMessage(null);
        }
    }

    public void retrySend(final String messageId) {
        if (isReadyForAction()) {
            if (ensureKnownRecipients()) {
                mBinding.getData().resendMessage(mBinding, messageId);
            }
        } else {
            warnOfMissingActionConditions(true /*sending*/,
                    new Runnable() {
                        @Override
                        public void run() {
                            retrySend(messageId);
                        }

                    });
        }
    }

    public void retryDownload(final String messageId) {
        if (isReadyForAction()) {
            mBinding.getData().downloadMessage(mBinding, messageId);
        } else {
            warnOfMissingActionConditions(false /*sending*/,
                    null /*commandToRunAfterActionConditionResolved*/);
        }
    }

    protected void clickCompleteMessage() {
        // Bkav QuangNDb khong lam gi
    }

    private BtalkConversationFragment.BtalkConversationFragmentHost mBtalkConversationFragmentHost;

    // Attachment data for the attachment within the selected message that was long pressed
    protected MessagePartData mSelectedAttachment;

    protected void selectMessage(ConversationMessageView messageView, MessagePartData attachment, boolean isLongClick) {
        if (isLongClick && !isMessageListSelectMode()) {
            startActionMode();
        }
        if (isMessageListSelectMode()) {
            final MultiMessageSelectActionModeCallBack modeCallBack
                    = (MultiMessageSelectActionModeCallBack) mBtalkConversationFragmentHost.getModeCallback();
            modeCallBack.toggleSelect(messageView.getData());
            mSelectedAttachment = attachment;
            mAdapter.notifyDataSetChanged();
        }
    }

    protected void startActionMode() {
        if (!mIsClickConfirm) {
            clickConfirmButton();
        }
        mHost.startActionMode(new MultiMessageSelectActionModeCallBack(this));
        setStatusBar(R.color.action_mode_color);//Bkav QuangNDb Doi mau status ba sang mau action mode
        mRootView.setSystemUiVisibility(0);//Bkav QuangNDb Tat che do light status bar
    }

    /**
     * Bkav QuangNdb kiem tra xem co dang o trang thai multi check hay khong
     */
    protected boolean isMessageListSelectMode() {
        return mBtalkConversationFragmentHost.getModeCallback() instanceof MultiMessageSelectActionModeCallBack;
    }

    @Override
    public boolean onAttachmentClick(ConversationMessageView view, MessagePartData attachment, Rect imageBounds, boolean longPress) {
        return false;
    }

    @Override
    public SubscriptionListData.SubscriptionListEntry getSubscriptionEntryForSelfParticipant(String selfParticipantId, boolean excludeDefault) {
        // TODO: ConversationMessageView is the only one using this. We should probably
        // inject this into the view during binding in the ConversationMessageAdapter.
        return mBinding.getData().getSubscriptionEntryForSelfParticipant(selfParticipantId,
                excludeDefault);
    }

    @Override
    public boolean isMessageSelected(String messageId) {
        final MultiMessageSelectActionModeCallBack modeCallBack
                = (MultiMessageSelectActionModeCallBack) mBtalkConversationFragmentHost.getModeCallback();
        return modeCallBack != null && modeCallBack.isSelected(messageId);
    }

    @Override
    public boolean isShowActionMode() {
        return isMessageListSelectMode();
    }

    @Override
    public boolean onMessageTextClick(ConversationMessageView view) {
        selectMessage(view, null, false);
        return true;
    }

    @Override
    public boolean isMessageHasLink(TextView messageText) {
        return BtalkLinkify.addLinks(messageText, BtalkLinkify.ALL, getFragmentManager());
    }

    @Override
    public void onMessageAnimationEnd() {
        ComposeMessageView.playSentSound();
    }

    @Override
    public void onResumeRequest() {
        if (mViewListContact.getVisibility() == View.GONE && !mIsClickConfirm) {
        //TODO TrungTH Dong lai , cm o ben duoi showContactList();
        }
    }

    private boolean mIsShowContactFirst = true;

    /**
     * Bkav QuangNDb hien thi ra danh sach chon lien he nhan tin
     * TODO TrungTH bo bien mIsShowContactFirst cu de an het khi nao firstQuery thi hien thi list ra
     * mIsShowContactFirst dung bien nay co truong hop nen cham len cham xuong thanh nhap contact thi no cung hien thi ra
     */
    private void showContactList() {
        mViewListContact.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    /**
     * Bkav QuangNDb Hien thi giao dien chat
     */
    private void showChatInterface() {
        mViewListContact.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private View mInputContactView;//Bkav QuangNDb view nhap contact

    private TextView mDisplayContactView;//Bkav QuangNDb View hien thi contact

    @Override
    protected void initActionBar(View view) {
        mInputContactView = view.findViewById(R.id.scrollContact);
        mDisplayContactView = (TextView) view.findViewById(R.id.conversation_title);
    }

    /**
     * Bkav QuangNDb doi hien thi action bar sau khi an dau tick
     */
    private void displayContactName() {
        mInputContactView.setVisibility(View.GONE);
        mDisplayContactView.setVisibility(View.VISIBLE);
        String conversationName = mBinding.getData().getConversationName();
        if (conversationName != null) {
            setTitleBar(conversationName, mDisplayContactView);
        }
        mComposeMessageView.getComposeEditText().requestFocus();
    }

    /**
     * Bkav QuangNDb tach code doan title bar
     */
    protected void setTitleBar(String conversationName, TextView conversationNameView) {
        // RTL : To format conversation title if it happens to be phone numbers.
        final BidiFormatter bidiFormatter = BidiFormatter.getInstance();
        final String formattedName = bidiFormatter.unicodeWrap(
                UiUtils.commaEllipsize(
                        conversationName,
                        conversationNameView.getPaint(),
                        conversationNameView.getWidth(),
                        getString(com.android.messaging.R.string.plus_one),
                        getString(com.android.messaging.R.string.plus_n)).toString(),
                TextDirectionHeuristicsCompat.LTR);
        conversationNameView.setText(formattedName);
        // In case phone numbers are mixed in the conversation name, we need to vocalize it.
        final String vocalizedConversationName =
                AccessibilityUtil.getVocalizedPhoneNumber(getResources(), conversationName);
        conversationNameView.setContentDescription(vocalizedConversationName);
        getActivity().setTitle(conversationName);
    }

    @Override
    protected void setupToolbar(View view) {
        // Khong lam gi do bo toolbar di
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mHost.getActionMode() != null) {
            return;
        }
        if (mIsClickConfirm) {
            inflater.inflate(R.menu.btalk_conversation_menu, menu);
            displayContactName();
            final ConversationData data = mBinding.getData();

            // Disable the "people & options" item if we haven't loaded participants yet.
            menu.findItem(com.android.messaging.R.id.action_people_and_options).setEnabled(data.getParticipantsLoaded());

            // See if we can show add contact action.
            final ParticipantData participant = data.getOtherParticipant();
            final boolean addContactActionVisible = (participant != null
                    && TextUtils.isEmpty(participant.getLookupKey()));
            menu.findItem(com.android.messaging.R.id.action_add_contact).setVisible(addContactActionVisible);

            // See if we should show archive or unarchive.
            final boolean isArchived = data.getIsArchived();
            menu.findItem(com.android.messaging.R.id.action_archive).setVisible(!isArchived);
            menu.findItem(com.android.messaging.R.id.action_unarchive).setVisible(isArchived);

            // Conditionally enable the phone call button.
            final boolean supportCallAction = (PhoneUtils.getDefault().isVoiceCapable() &&
                    data.getParticipantPhoneNumber() != null);
            menu.findItem(com.android.messaging.R.id.action_call).setVisible(supportCallAction);
            if (mHost.getActionMode() != null) {
                return;
            }
            mSearchItem = menu.findItem(R.id.action_search_sms);
            Assert.notNull(mSearchItem);
            mSearchView = (SearchView) mSearchItem.getActionView();
            setUpSearchView();
        } else {
            inflater.inflate(getIdResMenu(), menu);
            mMenu = menu;
            updateVisualsForContactPickingMode(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Bkav QuangNDb Search view hien ra khi click vao menu tim kiem tin nhan
    private SearchView mSearchView;

    private MenuItem mSearchItem;

    private boolean mIsSearchMode = false;

    /**
     * Bkav QuangNDb setUp search view
     */
    private void setUpSearchView() {
        // Bkav QuangNDb doi mau theo theme moi
        mSearchView.setQueryHint(getString(R.string.search_sms_hint));
        View underLine = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        underLine.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
        EditText searchEditText = (EditText) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(getActivity(), R.color.btalk_ab_text_and_icon_normal_color));
        searchEditText.setHintTextColor(ContextCompat.getColor(getActivity(), R.color.btalk_color_text_search_view_hint));
        searchEditText.setHint(R.string.search_sms_hint);
        ImageView closeButton = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        ImageView searchButton = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        searchButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.btalk_ic_search_light));
        closeButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_btalk_remove_small));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mIsClickConfirm) {
            int i = item.getItemId();
            if (i == com.android.messaging.R.id.action_people_and_options) {
                Assert.isTrue(mBinding.getData().getParticipantsLoaded());
                UIIntents.get().launchPeopleAndOptionsActivity(getActivity(), mConversationId);
                return true;
            } else if (i == com.android.messaging.R.id.action_call) {
                // tach code doan xu ly goi dien cho participant
                makeACall(true);
                return true;
            } else if (i == com.android.messaging.R.id.action_archive) {
                mBinding.getData().archiveConversation(mBinding);
                closeConversation(mConversationId);
                return true;
            } else if (i == com.android.messaging.R.id.action_unarchive) {
                mBinding.getData().unarchiveConversation(mBinding);
                return true;
            } else if (i == com.android.messaging.R.id.action_settings) {
                return true;
            } else if (i == com.android.messaging.R.id.action_add_contact) {
                final ParticipantData participant = mBinding.getData().getOtherParticipant();
                Assert.notNull(participant);
                final String destination = participant.getNormalizedDestination();
                final Uri avatarUri = AvatarUriUtil.createAvatarUri(participant);
                (new AddContactsConfirmationDialog(getActivity(), avatarUri, destination)).show();
                return true;
            } else if (i == com.android.messaging.R.id.action_delete) {
                if (isReadyForAction()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getQuantityString(
                                    com.android.messaging.R.plurals.delete_conversations_confirmation_dialog_title, 1))
                            .setPositiveButton(com.android.messaging.R.string.delete_conversation_confirmation_button,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog,
                                                            final int button) {
                                            deleteConversation();
                                        }
                                    })
                            .setNegativeButton(com.android.messaging.R.string.delete_conversation_decline_button, null)
                            .show();
                } else {
                    warnOfMissingActionConditions(false /*sending*/,
                            null /*commandToRunAfterActionConditionResolved*/);
                }
                return true;
            } else if (i == R.id.action_attach_contact) {
                //dinh kem lien he
                pickContact();
                return true;
            } else if (i == R.id.add_subject) {
                mComposeMessageView.showSubjectEditor();
                return true;
            } else if (i == R.id.action_search_sms) {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                mSearchView.setIconified(false);
                mSearchView.setOnQueryTextListener(this);
                mSearchView.setOnCloseListener(this);
                mComposeMessageView.setVisibility(View.GONE);
                mIsSearchMode = true;
                return true;
            } else if (i == R.id.action_edit_quick_responses) {
                UIIntents.get().launchBtalkQuickResponseEditActivity(getActivity());
            }
        } else {
            super.onMenuItemClick(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private static final int RESULT_PICK_CONTACT = 999;

    /**
     * Bkav QuangNDb: hien thi dialog dinh kem lien he
     */
    private void pickContact() {
        // Bkav QuangNDb sua de target truc tiep den class BtalkContactSelectionActivity thay vi chi setAction cho ung dung khac khong bat duoc su kien nay
        Intent contactPickerIntent = new Intent(getActivity(), BtalkContactSelectionActivity.class);
        contactPickerIntent.setAction(Intent.ACTION_PICK);
        contactPickerIntent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_PICK_CONTACT && resultCode == RESULT_OK) {
            if (data != null) {
                contactPicked(data);
            }
        }
    }

    /**
     * Bkav QuangNDb: ham xu ly sau khi 1 contact uri dc truyen ve
     */
    private void contactPicked(Intent data) {
        Cursor cursor = null;
        StringBuilder contactPicked = new StringBuilder();
        try {
            String phoneNo;
            String name;
            Uri uri = data.getData();
            cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                phoneNo = cursor.getString(phoneIndex);
                name = cursor.getString(nameIndex);
                contactPicked.append(name);
                contactPicked.append(" <");
                contactPicked.append(phoneNo);
                contactPicked.append("> ");
                String newText = mComposeMessageView.getComposeEditText().length() == 0 ?
                        contactPicked.toString() : mComposeMessageView.getComposeEditText().getText() + " " + contactPicked.toString();
                mComposeMessageView.getComposeEditText().append(newText);
                mComposeMessageView.getComposeEditText().setSelection(mComposeMessageView.getComposeEditText().length());
                mComposeMessageView.getComposeEditText().requestFocus();
                showComposeKeyboard();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Bkav QuangNDb Show keyboard va gan vao composeview
     */
    protected void showComposeKeyboard() {
        mComposeMessageView.getComposeEditText().requestFocus();
        // showImeKeyboard() won't work until the layout is ready, so wait until layout is complete
        // before showing the soft keyboard.
        UiUtils.doOnceAfterLayoutChange(mRootView, new Runnable() {
            @Override
            public void run() {
                final Activity activity = getActivity();
                if (activity != null) {
                    ImeUtil.get().showImeKeyboard(activity, mComposeMessageView.getComposeEditText());
                }
            }
        });
        mComposeMessageView.getComposeEditText().invalidate();
    }

    private void deleteConversation() {
        if (isReadyForAction()) {
            final Context context = getActivity();
            mBinding.getData().deleteConversation(mBinding);
            closeConversation(mConversationId);
        } else {
            warnOfMissingActionConditions(false /*sending*/,
                    null /*commandToRunAfterActionConditionResolved*/);
        }
    }

    @Override
    protected Menu getMenu() {
        return mMenu;
    }

    @Override
    public void onActionSelectAll() {
        // khong lam gi
    }

    @Override
    public void onActionUnSelectAll() {
        // khong lam gi
    }

    @Override
    public void onActionDelete(final Collection<ConversationMessageData> selectedMessages) {
        if (isReadyForAction()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(com.android.messaging.R.string.delete_message_confirmation_dialog_title)
                    .setMessage(com.android.messaging.R.string.delete_message_confirmation_dialog_text)
                    .setPositiveButton(com.android.messaging.R.string.delete_message_confirmation_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    for (ConversationMessageData messageData : selectedMessages) {
                                        mBinding.getData().deleteMessage(mBinding, messageData.getMessageId());
                                    }
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null);
            if (OsUtil.isAtLeastJB_MR1()) {
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(final DialogInterface dialog) {
                        onExitActionMode();
                    }
                });
            } else {
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        onExitActionMode();
                    }
                });
            }
            builder.create().show();
        } else {
            warnOfMissingActionConditions(false /*sending*/,
                    null /*commandToRunAfterActionConditionResolved*/);
            onExitActionMode();
        }
    }

    @Override
    public void onActionAttachmentSave(ConversationMessageData selectedMessages) {
        if (OsUtil.hasStoragePermission()) {
            final ConversationFragment.SaveAttachmentTask saveAttachmentTask = new ConversationFragment.SaveAttachmentTask(
                    getActivity());
            for (final MessagePartData part : selectedMessages.getAttachments()) {
                saveAttachmentTask.addAttachmentToSave(part.getContentUri(),
                        part.getContentType());
            }
            if (saveAttachmentTask.getAttachmentCount() > 0) {
                saveAttachmentTask.executeOnThreadPool();
                onExitActionMode();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getActivity().requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    @Override
    public void onActionReDownload(ConversationMessageData selectMessage) {
        retryDownload(selectMessage.getMessageId());
        onExitActionMode();
    }

    @Override
    public void onActionResend(Collection<ConversationMessageData> selectedMessages) {
        for (ConversationMessageData messageData : selectedMessages) {
            retrySend(messageData.getMessageId());
        }
        onExitActionMode();
    }

    @Override
    public void onActionCopyText(ConversationMessageData selectedMessage) {
        Assert.isTrue(selectedMessage.hasText());
        final ClipboardManager clipboard = (ClipboardManager) getActivity()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(
                ClipData.newPlainText(null /* label */, selectedMessage.getText()));
        onExitActionMode();
        //HienDTk: them thong bao copy noi dung tin nhan
        Toast.makeText(getContext(), R.string.string_copy_messager, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActionDetails(ConversationMessageData selectedMessage) {
        MessageDetailsDialog.show(
                getActivity(), selectedMessage, mBinding.getData().getParticipants(),
                mBinding.getData().getSelfParticipantById(selectedMessage.getSelfParticipantId()));
        onExitActionMode();
    }

    @Override
    public void onActionForward(Collection<ConversationMessageData> selectedMessages) {
        // QuangNDb xu ly forward nhieu tin nhan khac nhau
        List<ConversationMessageData> messageDatas = new ArrayList<>(selectedMessages);
        ConversationMessageData data = messageDatas.get(0);
        int size = messageDatas.size();
        for (int i = 1; i < size; i++) {
            ConversationMessageData messageData = messageDatas.get(i);
            for (MessagePartData partData : messageData.getParts()) {
                data.getParts().add(partData);
            }
        }
        final MessageData message = mBinding.getData().createForwardedMessage(data);
        UIIntents.get().launchForwardMessageActivity(getActivity(), message);
        onExitActionMode();
    }

    @Override
    public void onActionShare(ConversationMessageData selectedMessage) {
        shareMessage(selectedMessage);
        onExitActionMode();
    }

    /**
     * BKav QuangNDb copy lai ham share cua inner class do k ke thua ham o ben trong inner class
     */
    private void shareMessage(final ConversationMessageData data) {
        // Figure out what to share.
        MessagePartData attachmentToShare = mSelectedAttachment;
        // If the user long-pressed on the background, we will share the text (if any)
        // or the first attachment.
        if (mSelectedAttachment == null
                && TextUtil.isAllWhitespace(data.getText())) {
            final List<MessagePartData> attachments = data.getAttachments();
            if (attachments.size() > 0) {
                attachmentToShare = attachments.get(0);
            }
        }

        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        if (attachmentToShare == null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, data.getText());
            shareIntent.setType("text/plain");
        } else {
            shareIntent.putExtra(
                    Intent.EXTRA_STREAM, attachmentToShare.getContentUri());
            shareIntent.setType(attachmentToShare.getContentType());
        }
        final CharSequence title = getResources().getText(com.android.messaging.R.string.action_share);
        startActivity(Intent.createChooser(shareIntent, title));
    }


    @Override
    public void setBtalkPickerFragmentHost(Object o) {
        if (o instanceof BtalkConversationFragment.BtalkConversationFragmentHost) {
            this.mBtalkConversationFragmentHost = (BtalkConversationFragment.BtalkConversationFragmentHost) o;
        }
    }

    /**
     * Bkav QuangNDb focus de danh dau tin nhan trong conversation la da doc
     */
    public void setConversationFocus() {
        if (mHost.isActiveAndFocused()) {
            mBinding.getData().setFocus();
        }
    }

    /**
     * Bkav QuangNDb click vao delete chip
     */
    @Override
    public void onRecipientChipDeleted(RecipientEntry entry) {
//        mRecipientTextView.removeRecipientEntry(entry);// doan lam loi xoa contact di bi crash
//        mRecipientEntries.remove(entry);
    }

    private boolean mIsIgnoreKeepApp = true;

    @Override
    public void saveStatus() {
        if (mIsClickConfirm) {
            PrefUtils.get().saveStringPreferences(getActivity(), PrefUtils.CONVERSATION_ID, mConversationId);// save conversation_id
            PrefUtils.get().saveLongPreferences(getActivity(), PrefUtils.TIME_PAUSE_APP, System.currentTimeMillis());// save time khi bam phim pause
            PrefUtils.get().saveBooleanPreferences(getActivity(), PrefUtils.KEEP_STATUS_APP, true);// save time khi bam phim pause
            mIsIgnoreKeepApp = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIsIgnoreKeepApp) {
            PrefUtils.get().saveBooleanPreferences(Factory.get().getApplicationContext(), PrefUtils.KEEP_STATUS_APP, false);// huy time khi bam phim pause
        }
    }

    @Override
    public void showQuickResponse() {
    }

    private SuggestLoaderManager mSmartSuggestLoaderManage;

    public void querySmartContact(String query) {
        if (mSmartSuggestLoaderManage == null) {
            mSmartSuggestLoaderManage = new SuggestLoaderManager(getActivity(), this, mComposeMessageView.getHeight());
        }
        mSmartSuggestLoaderManage.startLoadMessage(query, true, mRecipientEntries);
    }

    @Override
    public void onClickMessage() {

    }

    /**
     * Anhdts action click chon view suggest
     */
    @Override
    public void onClick(DialerDatabaseHelper.ContactNumber data) {
        //Bkav QuangNDb sua loi nguoi dung chon so luu cache trong callog khong hien thi sai ten
        boolean isCallLog = false;
        String prefixDateFormat = getString(R.string.display_name_date_format);
        // Bkav QuangNDb dang date thi doi lai ten hien thi
        if (data.displayName.startsWith(prefixDateFormat)) {
            isCallLog = true;
            // displayName = DateUtil.formatDateRecentCall(mContext, Long.parseLong(displayName.replace(prefixDateFormat, "")));
        }
        final RecipientEntry recipientEntry = ContactUtil.createRecipientEntry(isCallLog?data.phoneNumber:data.displayName,
                ContactsContract.DisplayNameSources.STRUCTURED_NAME, data.phoneNumber, data.mCallType,
                data.mCustomLabel,
                data.id, data.lookupKey, -1, data.mThumpUri, false);
        onContactItemClick(recipientEntry);
        if (mContactPickingMode == MODE_PICK_INITIAL_CONTACT) {
            touchActivity();
        } else {// neu o trang thai group thi khong an popup ma quey tiep
            if (!mRecipientTextView.getSelectedDestinations().isEmpty()) {
                String newText = mRecipientTextView.getText().toString();
                while (newText.contains(",")) {
                    int commaIndex = newText.indexOf(",");
                    newText = newText.substring(commaIndex + 1, newText.length());
                }
                querySmartContact(newText.trim());
            }
        }
        //Bkav QuangNDb refresh adapter de cap nhat trang thai
        if (mContactPickerFragment != null) {
            mContactPickerFragment.refreshAdapter();
        }
    }

    /**
     * Anhdts su kien cham vao activity thi an suggest di
     */
    public void touchActivity() {
        if (mSmartSuggestLoaderManage != null && !mSmartSuggestLoaderManage.isInteractive()) {
            mSmartSuggestLoaderManage.hideViewSuggest();
        }
    }

    @Override
    public boolean isReadySendMessage() {
        return mIsClickConfirm;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBtalkListParticipantDialog = null;
        //Bkav QuangNDb cancel pending request bitmap de khong leak memory
        ContactPhotoManager.getInstance(getContext()).cancelPendingRequests(getView());
    }

    @Override
    protected void resetConversation() {
        mConversationId = CONVERSATION_DRAF_ID;
    }

    @Override
    public boolean isDeepShortcutTouch(MotionEvent ev) {
        return mComposeMessageView.isDeepShortcutTouch(ev);
    }

    @Override
    public void onInsertEntry(RecipientEntry recipientEntry) {
        if (!mRecipientEntries.contains(recipientEntry)) {
            mRecipientEntries.add(recipientEntry);
        }
    }

    @Override
    public void onRemoveEntry(RecipientEntry recipientEntry) {
        mRecipientEntries.remove(recipientEntry);
    }

    @Override
    protected void handlePaste() {
//        if (mIsPaste) {
//            mComposeMessageView.getComposeEditText().requestFocus();
//            mIsPaste = false;
//        }
    }

    /**
     * Bkav QuangNDb set incoming draf
     */
    @Override
    public void setIncomingDraft(final MessageData draftData) {
        mIncomingDraft = draftData;
        //Bkav QuangNDb neu khong setdraf thi se lay draft co trong preference
        if (draftData == null) {
            mIncomingDraft = PrefUtils.get().loadDraftPreferences(Factory.get().getApplicationContext(), PrefUtils.DRAFT_DATA);
        }
    }

    @Override
    public boolean isListEsimTouch(MotionEvent event) {
        if(isShowListSim() && !isFocusRecycleView(event)){
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                mRecyclerViewEsim.setVisibility(View.GONE);// Bkav HuyNQN neu bam ra ngoai recycleview se dong lai danh sach esim
            }
            return true;
        }
        return false;
    }

    private boolean isFocusRecycleView(MotionEvent event) {
        Rect outRect = new Rect();
        mRecyclerViewEsim.getGlobalVisibleRect(outRect);
        return outRect.contains((int) event.getRawX(), (int) event.getRawY());
    }

    public boolean isShowListSim() {
        return mRecyclerViewEsim != null && mRecyclerViewEsim.getVisibility() == View.VISIBLE;
    }

    // Bkav TienNAb: xu ly an mediapicker
    public void hideMediaPicker(){
        if (inputManager.getMediaInput().isFullScreen()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    inputManager.showHideMediaPicker(false, false);
                }
            }, TIME_DELAY_HIDE_MEDIAPICKER_IS_FULLSCREEN);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    inputManager.showHideMediaPicker(false, false);
                }
            }, IME_DELAY_HIDE_MEDIAPICKER);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //HienDTk: khi xoay ngang man hinh thi focus vao recipient text va cho hien ban phim
        showImeKeyboard();

    }
}
