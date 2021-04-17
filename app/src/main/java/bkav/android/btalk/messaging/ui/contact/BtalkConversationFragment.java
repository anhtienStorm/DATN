package bkav.android.btalk.messaging.ui.contact;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.CallUtil;
import com.android.contacts.common.ContactPhotoManager;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.TelecomUtil;
import com.android.messaging.Factory;
import com.android.messaging.datamodel.data.ConversationData;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.datamodel.data.ConversationParticipantsData;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.UIIntentsImpl;
import com.android.messaging.ui.animation.PopupTransitionAnimation;
import com.android.messaging.ui.conversation.ComposeMessageView;
import com.android.messaging.ui.conversation.ConversationFragment;
import com.android.messaging.ui.conversation.ConversationInputManager;
import com.android.messaging.ui.conversation.ConversationMessageAdapter;
import com.android.messaging.ui.conversation.ConversationMessageView;
import com.android.messaging.ui.conversation.MessageDetailsDialog;
import com.android.messaging.ui.conversation.SimSelectorView;
import com.android.messaging.ui.mediapicker.MediaPicker;
import com.android.messaging.util.Assert;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.TextUtil;
import com.android.messaging.util.UiUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.esim.dialog_choose_sim.DialogChooseSimFragment;
import bkav.android.btalk.messaging.ui.BtalkUIIntentsImpl;
import bkav.android.btalk.messaging.ui.animation.BtalkPopupTransitionAnimation;
import bkav.android.btalk.messaging.ui.contact.activities.BtalkContactSelectionActivity;
import bkav.android.btalk.messaging.ui.conversation.BtalkConversationInputManager;
import bkav.android.btalk.messaging.ui.conversation.BtalkConversationMessageAdapter;
import bkav.android.btalk.messaging.ui.conversation.BtalkConversationMessageView;
import bkav.android.btalk.messaging.ui.conversation.BtalkListParticipantDialog;
import bkav.android.btalk.messaging.ui.conversation.BtalkSimSelectorView;
import bkav.android.btalk.messaging.ui.conversation.MultiMessageSelectActionModeCallBack;
import bkav.android.btalk.messaging.ui.cutomview.BtalkComposeMessageView;
import bkav.android.btalk.messaging.ui.mediapicker.BtalkMediaPicker;
import bkav.android.btalk.messaging.util.BtalkCharacterUtil;
import bkav.android.btalk.messaging.util.BtalkDataObserver;
import bkav.android.btalk.messaging.util.BtalkLinkify;
import bkav.android.btalk.utility.BtalkUiUtils;
import bkav.android.btalk.utility.PrefUtils;

import static android.app.Activity.RESULT_OK;

/**
 * Created by quangnd on 26/03/2017.
 * class custom lai class ConversationFragment cua source goc
 */

public class BtalkConversationFragment extends ConversationFragment
        implements MultiMessageSelectActionModeCallBack.MultiMessageSelectListener
        , ConversationMessageAdapter.OnMessageAnimationEndListener, SearchView.OnQueryTextListener,
        SearchView.OnCloseListener {

    private static final int RESULT_PICK_CONTACT = 999;

    private boolean mIsFirst = true;// bien check lan dau vao fragment

    // View do bong action bar
//    private View mShadowActionbar;
    // key co the them nguoi vao fragment
    private static final String CAN_ADD_MORE_PAR = "can_add_more";

    private MenuItem mSearchItem;

    // Anhdts item filter audio and mms
    private MenuItem mAudioFilterItem;

    private MenuItem mMMSFilterItem;

    // Bkav TienNAb: Them item filter sms
    private MenuItem mSMSFilterItem;
    // Bkav HienDTk: list empty chua thong bao khong tim thay tin nhan
    protected LinearLayout mListEmpty;

    // Bien lang nghe thay doi csdl tin nhan
//    private BtalkDataObserver mMessageObserver;

    // Anhdts co phai che do mms khong
    private int mModeFilter = ConversationData.FILTER_NONE;

    private boolean mIsCanAddMorePar = false;
    private RecyclerView mRecyclerViewEsim;

    public static BtalkConversationFragment newInstance(boolean canAddMoreParticipant) {
        Bundle args = new Bundle();
        args.putBoolean(CAN_ADD_MORE_PAR, canAddMoreParticipant);
        BtalkConversationFragment fragment = new BtalkConversationFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsCanAddMorePar = getArguments().getBoolean(CAN_ADD_MORE_PAR);
        // them broadcast lang nghe cuoc goi den
        registerIncomingReceiver();
        // Bkav QuangNDb lang nghe su kien message db thay doi
//        mMessageObserver = new BtalkDataObserver(new Handler());
//        registerObserver(mMessageObserver, Telephony.MmsSms.CONTENT_URI);

        // Bkav HuyNQN mo ban phim khi cac app ben ngoai goi toi tin nhan.
        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                if (intent.getIntExtra(BtalkUIIntentsImpl.FLAG_SEND_TO, 0) == UIIntentsImpl.SHOW_KEYBOARD) {
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                } else {
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Bkav TienNAb: an mediapicker
//        mComposeMessageView.hideInput();
    }

    @Override
    public void onExitActionMode() {
        dismissActionMode();
        mAdapter.notifyDataSetChanged();
        mRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);// reset lai light status bar
        setStatusBar(R.color.btalk_conversation_fragment_bg);
    }

    // doan xu ly animation end thi moi play sound tin nhan
    @Override
    public void onMessageAnimationEnd() {
        ComposeMessageView.playSentSound();
    }

    @Override
    public boolean onClose() {
        mSearchView.setIconifiedByDefault(true);
        mComposeMessageView.setVisibility(View.VISIBLE);
        mSearchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    String mSearchString = null;

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


    public interface BtalkConversationFragmentHost {

        void onAddMoreContact(ConversationParticipantsData data);

        ActionMode.Callback getModeCallback();
    }

    @Override
    public MediaPicker createMediaPicker() {
        return new BtalkMediaPicker();
    }

    private BtalkConversationFragmentHost mBtalkConversationFragmentHost;

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.btalk_conversation_fragment, container, false);
    }

    @Override
    protected void initComposeView(View view) {
        mComposeMessageView = (BtalkComposeMessageView) view.findViewById(R.id.message_compose_view_container);
    }

    @Override
    public boolean isListEsimTouch(MotionEvent event) {
        if (isShowListSim() && !isFocusRecycleView(event)) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
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

    //Bkav QuangNDb check xem profile list co dang show hay khong
    public boolean isShowListSim() {
        return mRecyclerViewEsim != null && mRecyclerViewEsim.getVisibility() == View.VISIBLE;
    }

    @Override
    protected ConversationMessageView getConversationMessageView(View itemView) {
        return (BtalkConversationMessageView) itemView;
    }

    @Override
    public void setBtalkConversationFragmentHost(Object o) {
        if (o instanceof BtalkConversationFragmentHost) {
            this.mBtalkConversationFragmentHost = (BtalkConversationFragmentHost) o;
        }
    }

    @Override
    protected void inflateMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.btalk_conversation_menu, menu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);// reset lai light status bar
//        showImeKeyboard();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (mHost.getActionMode() != null) {
            return;
        }
        mSearchItem = menu.findItem(R.id.action_search_sms);

        mMMSFilterItem = menu.findItem(R.id.action_filter_mms);
        mMMSFilterItem.setVisible(false);
        mAudioFilterItem = menu.findItem(R.id.action_filter_audio);
        // Bkav TienNAb: Gan gia tri cho item SMSFiter
        mSMSFilterItem = menu.findItem(R.id.action_filter_sms);

        Assert.notNull(mSearchItem);
        mSearchView = (SearchView) mSearchItem.getActionView();
        setUpSearchView();
    }

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
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //HienDTk: bam close search view thi dong ban phim
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                mSearchView.clearFocus();
                mSearchView.setIconified(true);
            }
        });
    }

    // Bkav QuangNDb Search view hien ra khi click vao menu tim kiem tin nhan
    private SearchView mSearchView;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_attach_contact:
                //dinh kem lien he
                pickContact();
                break;
            case R.id.add_subject:
                mComposeMessageView.showSubjectEditor();
                break;
            case R.id.action_search_sms:
                mActionSearch = true;
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                mSearchView.setIconified(false);
                mSearchView.setOnQueryTextListener(this);
                mSearchView.setOnCloseListener(this);
                mComposeMessageView.setVisibility(View.GONE);
                break;
            case R.id.action_edit_quick_responses:
                UIIntents.get().launchBtalkQuickResponseEditActivity(getActivity());
                break;
            // Anhdts action loc tin nhan mms
            case R.id.action_filter_mms:
                mModeFilter = mModeFilter != ConversationData.FILTER_MMS ? ConversationData.FILTER_MMS : ConversationData.FILTER_NONE;
                item.setTitle(mModeFilter == ConversationData.FILTER_MMS ? R.string.action_show_all_message : R.string.action_filter_mms);
                mAudioFilterItem.setTitle(R.string.action_filter_audio);
                // Bkav TienNAb: dat lai tieu de cho item SMSFilter
                mSMSFilterItem.setTitle(R.string.action_filter_sms);
                mBinding.getData().filterMMSAudio(getLoaderManager(), mBinding, mModeFilter);
                break;
            case R.id.action_filter_audio:
                mModeFilter = mModeFilter != ConversationData.FILTER_AUDIO ? ConversationData.FILTER_AUDIO : ConversationData.FILTER_NONE;
                item.setTitle(mModeFilter == ConversationData.FILTER_AUDIO ? R.string.action_show_all_message : R.string.action_filter_audio);
                mMMSFilterItem.setTitle(R.string.action_filter_mms);
                // Bkav TienNAb: dat lai tieu de cho item SMSFilter
                mSMSFilterItem.setTitle(R.string.action_filter_sms);
                mBinding.getData().filterMMSAudio(getLoaderManager(), mBinding, mModeFilter);
                break;
            // Bkav TienNAb: xu ly khi chon chuc nang loc sms
            case R.id.action_filter_sms:
                mModeFilter = mModeFilter != ConversationData.FILTER_SMS ? ConversationData.FILTER_SMS : ConversationData.FILTER_NONE;
                item.setTitle(mModeFilter == ConversationData.FILTER_SMS ? R.string.action_show_all_message : R.string.action_filter_sms);
                mMMSFilterItem.setTitle(R.string.action_filter_mms);
                mAudioFilterItem.setTitle(R.string.action_filter_audio);
                mBinding.getData().filterSMS(getLoaderManager(), mBinding, mModeFilter);
                break;
            case R.id.action_call_sim2:
//                makeACall(1);
                break;
        }
        return super.onOptionsItemSelected(item);
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

    //Bkav QuangNdb:show keyboard khi vao activity giup nguoi dung nhap tin nhan nhanh hon
    private void showImeKeyboard() {
        Assert.notNull(mComposeMessageView.getComposeEditText());
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
    }


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
                mComposeMessageView.getComposeEditText().setText(newText);
                mComposeMessageView.getComposeEditText().setSelection(mComposeMessageView.getComposeEditText().length());
                showImeKeyboard();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    @Override
    protected void titleBarClick(TextView conversationNameView) {
        conversationNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsCanAddMorePar) {
                    mBtalkConversationFragmentHost.onAddMoreContact(mBinding.getData().getParticipants());
                } else {

                    /*
                     * BKAV HuyNQn thuc hien truyen vao cac gia tri:
                     * -1 chi co 1 sim, hoac co 2 sim nhung ko dat sim mac dinh thuc hien cuoc goi
                     * 0 sim mac dinh o slot 0
                     * 1 sim mac dinh o slot 1
                     * */
                    makeACall(false);
                }
            }
        });
    }

    @Override
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
                        selectMessage((BtalkConversationMessageView) view, ((BtalkConversationMessageView) view).getAttach(), true);
                        return true;
                    }
                }
        );

        mAdapter.setListener(this);
    }

    @Override
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

    /**
     * Bkav QuangNdb kiem tra xem co dang o trang thai multi check hay khong
     */
    protected boolean isMessageListSelectMode() {
        return mBtalkConversationFragmentHost.getModeCallback() instanceof MultiMessageSelectActionModeCallBack;
    }

    @Override
    protected void startActionMode() {
        mHost.startActionMode(new MultiMessageSelectActionModeCallBack(this));
        setStatusBar(R.color.action_mode_message_color);//Bkav QuangNDb Doi mau status ba sang mau action mode
        mRootView.setSystemUiVisibility(0);//Bkav QuangNDb Tat che do light status bar
    }

    // Bkav TienNAb: lang nghe su kien click vao chon tat ca
    @Override
    public void onActionSelectAll() {
        final MultiMessageSelectActionModeCallBack modeCallBack
                = (MultiMessageSelectActionModeCallBack) mBtalkConversationFragmentHost.getModeCallback();
        modeCallBack.toggleSelectAll(mConversationMessageDatas);
        mAdapter.notifyDataSetChanged();
    }

    // Bkav TienNAb: lang nghe su kien click vao bo chon tat ca
    @Override
    public void onActionUnSelectAll() {
        final MultiMessageSelectActionModeCallBack modeCallBack
                = (MultiMessageSelectActionModeCallBack) mBtalkConversationFragmentHost.getModeCallback();
        modeCallBack.toggleUnSelectAll(mConversationMessageDatas);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActionDelete(Collection<ConversationMessageData> selectedMessages) {
        deleteListMessage(selectedMessages);
    }

    /**
     * Bkav QuangNDb them ham xoa list tin nhan khi check nhieu tin nhan
     */
    protected void deleteListMessage(final Collection<ConversationMessageData> messageDatas) {
        if (isReadyForAction()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(com.android.messaging.R.string.delete_message_confirmation_dialog_title)
                    .setMessage(com.android.messaging.R.string.delete_message_confirmation_dialog_text)
                    .setPositiveButton(com.android.messaging.R.string.delete_message_confirmation_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    for (ConversationMessageData messageData : messageDatas) {
                                        mBinding.getData().deleteMessage(mBinding, messageData.getMessageId());
                                    }
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null);
            if (OsUtil.isAtLeastJB_MR1()) {
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(final DialogInterface dialog) {
//                        mHost.dismissActionMode();
                        onExitActionMode();
                    }
                });
            } else {
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
//                        mHost.dismissActionMode();
                        onExitActionMode();
                    }
                });
            }
            builder.create().show();
        } else {
            warnOfMissingActionConditions(false /*sending*/,
                    null /*commandToRunAfterActionConditionResolved*/);
//            mHost.dismissActionMode();
            onExitActionMode();
        }
    }

    @Override
    public void onActionAttachmentSave(ConversationMessageData selectedMessages) {
        if (OsUtil.hasStoragePermission()) {
            final SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(
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
        //HienDTk: them thong cao copy noi dung tin nhan thanh cong
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
        // Anhdts sua loi concurrent eception
        for (int i = 1; i < size; i++) {
            ConversationMessageData messageData = messageDatas.get(i);
            Iterator<MessagePartData> iter = messageData.getParts().iterator();
            while (iter.hasNext()) {
                data.getParts().add(iter.next());
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
    public boolean onAttachmentClick(ConversationMessageView messageView, MessagePartData attachment, Rect imageBounds, boolean longPress) {
        if (longPress) {
            selectMessage(messageView, attachment, true);
            return true;
        } else {
            if (isMessageListSelectMode()) {
                selectMessage(messageView, attachment, false);
            } else {
                if (messageView.getData().getOneClickResendMessage()) {
                    handleMessageClick(messageView);
                    return true;
                }

                if (attachment.isImage()) {
                    displayPhoto(attachment.getContentUri(), imageBounds, false /* isDraft */);
                }

                if (attachment.isVCard()) {
                    UIIntents.get().launchVCardDetailActivity(getActivity(), attachment.getContentUri());
                }
            }
        }
        return false;
    }

    @Override
    public boolean onMessageTextClick(ConversationMessageView view) {
        selectMessage(view, null, false);
        return true;
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
    protected ConversationInputManager initInputManager(Bundle savedInstanceState) {
        return new BtalkConversationInputManager(
                getActivity(), this, mComposeMessageView, mHost, getFragmentManagerToUse(),
                mBinding, mComposeMessageView.getDraftDataModel(), savedInstanceState);
    }

    @Override
    protected void clickCompleteMessage() {
        // Bkav QuangNDb khong lam gi
    }

    @Override
    protected void updateActionAndStatusBarColor(ActionBar actionBar) {
        // doi mau actionbar va status bar
//        btalk_color_conversation_bg
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.btalk_conversation_fragment_bg))); // TrungTH NOte doi mau
        setStatusBar(R.color.btalk_conversation_fragment_bg);
        actionBar.setElevation(0f);
        // Bkav QuangNDb khong lam gi de giong mau actionbar trong style
    }

    /**
     * Bkav QuangNDb set status bar color
     */
    private void setStatusBar(int colorId) {
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), colorId));
    }


    @Override
    protected void removeShadowActionbar(ActionBar actionBar) {
        actionBar.setElevation(0);
    }

//    @Override
//    protected void initShadowActionbar(View view) {
//        mShadowActionbar = view.findViewById(R.id.action_bar_divider);
//    }

//    protected boolean isScrolledToTop() {
//        int positonTop = ((LinearLayoutManager) mRecyclerView
//                .getLayoutManager()).findFirstCompletelyVisibleItemPosition();
//        return positonTop == 0;
//    }

//    @Override
//    protected void setAlphaShadowActionbar(boolean scrolledToTop) {
//        mShadowActionbar.animate().alpha(scrolledToTop ? 0 : 1);
//    }

//    @Override
//    protected void checkTopVisible() {
//        mShadowActionbar.animate().alpha(isScrolledToTop() ? 0 : 1);
//    }

    @Override
    protected void setDefaultName(TextView conversationNameView) {
        // Bkav QuangNDb khong lam gi
    }

    @Override
    protected int getIdResLayoutCustomActionBar() {
        return R.layout.btalk_action_bar_conversation_name;
    }

    @Override
    protected void initFastScroll() {
        // Bkav QUangNDb khong lam gi de bo fast scroll
    }

    private TextView mConversationNameView;

    private String mConversationName;

    @Override
    public void onReadyMessage() {
        // xu ly viec thay doi actionbar
        if (mIsCanAddMorePar) {
            mIsCanAddMorePar = false;
            mConversationNameView.setText(mConversationName);
            getActivity().setTitle(mConversationName);
            mHost.changeState();
        }
    }

    @Override
    protected void setTitleBar(String conversationName, TextView conversationNameView) {
        mConversationNameView = conversationNameView;
        mConversationName = conversationName;
        if (mIsCanAddMorePar) {
            StringBuffer nameBuff = new StringBuffer();
            nameBuff.append(conversationName);
            nameBuff.append(" +");
            conversationNameView.setText(nameBuff.toString());
            getActivity().setTitle(nameBuff.toString());
        } else {
            super.setTitleBar(conversationName, conversationNameView);
        }
    }

    private BtalkListParticipantDialog mBtalkListParticipantDialog;

    //Bkav QuangNDb thuc hien cuoc goi khi click vao ten nguoi nhan/gui
    @Override
    protected void makeACall(boolean suggestShowOtherSim) {
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
                // Bkav TienNAb: sua lai logic goi dien trong tab tin nhan
//                if (!suggestShowOtherSim) {
                    UIIntents.get().makeACall(getContext(), getFragmentManager(), phoneNumber);
//                } else {
//                    List<ISimProfile> iSimProfiles = ESimUtils.getAllProfileWithNumber(phoneNumber);
//                    if (iSimProfiles.size() <= 1) {
//                        final Intent intentCall = CallUtil.getCallIntent(phoneNumber);
//                        intentCall.putExtra("Cdma_Supp", true);
//                        DialerUtils.startActivityWithErrorToast(getContext(), intentCall);
//                    } else {
//                        //HienDTk: lay sim mac dinh khi goi
//                        PhoneAccountHandle accountDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(getContext(),
//                                PhoneAccount.SCHEME_TEL);
//                        //HienDTk: neu khong lay duoc sim mac dinh thi la dang de che do hoi truoc khi goi
//                        if (accountDefault == null && BtalkCallLogCache.getCallLogCache(getContext()).isHasSimOnAllSlot()) {
//
//                            DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(phoneNumber);
//                            dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
//
//                        }else {
//                            final Intent intentCall = CallUtil.getCallIntent(phoneNumber);
//                            intentCall.putExtra("Cdma_Supp", true);
//                            DialerUtils.startActivityWithErrorToast(getContext(), intentCall);
//
//                        }
//                    }
//                }

            } else {
                Toast.makeText(getActivity(), R.string.btalk_notify_not_make_a_call, Toast.LENGTH_SHORT).show();
            }
        }

    }



    @Override
    protected PopupTransitionAnimation getPopupTransitionAnimation(Rect startRect, ConversationMessageView view) {
        return new BtalkPopupTransitionAnimation(startRect, view);
    }

    @Override
    protected void setRecyclerAnimator() {
        // Khong lam gi
    }

    @Override
    protected void resetOptionMenu() {
        if (mIsFirst) {
            mIsFirst = false;
            super.resetOptionMenu();
        }
    }

    /**
     * Bkav QuangNDb dang lang nghe su kien change cua contact
     */
    private void registerObserver(BtalkDataObserver observer, Uri uri) {
//        getActivity().getContentResolver().registerContentObserver(uri, true, observer);
//        observer.setOnChangeListener(this);
    }

    /**
     * Bkav QuangNDb huy dang lang nghe su kien change cua contact
     */
    private void unregisterContactObserver(BtalkDataObserver observer) {
        getActivity().getContentResolver().unregisterContentObserver(observer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplicationContext().unregisterReceiver(mIncomingCallReceiver);
        mBtalkListParticipantDialog = null;
        mBtalkConversationFragmentHost = null;
        if (mAdapter != null) {
            mAdapter.releaseListener();
        }
        mParentView = null;
        //Bkav QuangNDb cancel pending request bitmap de khong leak memory
        ContactPhotoManager.getInstance(getContext()).cancelPendingRequests(getView());
        // dang ky lang nghe thay doi csdl tin nhan
//        unregisterContactObserver(mMessageObserver);
    }

    @Override
    public SimSelectorView getSimSelectorView() {
        return (BtalkSimSelectorView) getView().findViewById(R.id.sim_selector);
    }

    @Override
    public int getSimSelectorItemLayoutId() {
        return R.layout.btalk_sim_selector_item_view;
    }

    /**
     * Bkav QuangNDb Stop phat media lai
     */
    private void stopAudioPlayback() {
        AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    /**
     * Bkav QuangNDb Register cuoc goi den de xu ly tat ghi am dang phat
     */
    private BroadcastReceiver mIncomingCallReceiver;

    // FIXME: 17/03/2020 co cach khac thay the
    private void registerIncomingReceiver() {
        mIncomingCallReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                    try {
                        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                        //Incoming call
                        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                            stopAudioPlayback();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        getActivity().getApplicationContext().registerReceiver(mIncomingCallReceiver, intentFilter);
    }

    @Override
    protected void exitActionMode() {
        if (isShowActionMode()) {
            onExitActionMode();
        }
    }

    private boolean mIgnoreKeepApp = true;

    @Override
    public void saveStatus() {
        PrefUtils.get().saveStringPreferences(getContext(), PrefUtils.CONVERSATION_ID, mConversationId);// save conversation_id
        PrefUtils.get().saveLongPreferences(getContext(), PrefUtils.TIME_PAUSE_APP, System.currentTimeMillis());// save time khi bam phim pause
        PrefUtils.get().saveBooleanPreferences(getContext(), PrefUtils.KEEP_STATUS_APP, true);// save time khi bam phim pause
        mIgnoreKeepApp = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIgnoreKeepApp) {
            PrefUtils.get().saveBooleanPreferences(getActivity(), PrefUtils.KEEP_STATUS_APP, false);// huy time khi bam phim pause
        }
    }
    // Bkav HienDTk: dong lai vi da tao ham dung chung o class BtalkUiUtils
    /**
     * Bkav QuangNDb ham check xem nguoi dung co de luon hoi khi nhan tin khong
     */
//    private boolean isAlwaysAskBeforeSendSms() {
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
    public boolean isAlwaysAskSim() {
        return mIsEmptyMessage && BtalkUiUtils.isAlwaysAskBeforeSendSms();
    }

    @Override
    public void showQuickResponse() {
    }

    @Override
    public void keyboardShow() {
        // Bkav TienNAb: khong can dung nua
//        mComposeMessageView.hideInput();
//        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    @Override
    public void onMediaPickerStateChange(boolean isShow) {
        //HienDTk: tranh crash getActivity().getWindow() = null
        if (getActivity() != null) {
            if (isShow) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            } else {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        }
    }

    @Override
    public void emojiClick() {
        // Bkav TienNAb: khong can dung nua
//        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mParentView = (ViewGroup) mRootView.findViewById(R.id.view_group);
        mListEmpty = (LinearLayout) mRootView.findViewById(R.id.emptyView);

        mRecyclerViewEsim = mParentView.findViewById(R.id.list_esim);
        mComposeMessageView.setParentView(mParentView);
        return mRootView;
    }

    @Override
    public void showEmptyView(boolean isCheckSearch) {
        if (isCheckSearch){
            mRecyclerView.setVisibility(View.GONE);
            mListEmpty.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mListEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public String getSearchString() {
        return mSearchString;
    }

    @Override
    public boolean isDeepShortcutTouch(MotionEvent ev) {
        return mComposeMessageView.isDeepShortcutTouch(ev);
    }

    @Override
    public boolean isMessageHasLink(TextView messageText) {
        return BtalkLinkify.addLinks(messageText, BtalkLinkify.ALL, getFragmentManager());
    }
}
