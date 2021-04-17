package bkav.android.btalk.messaging.ui.conversation;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.messaging.datamodel.data.ConversationParticipantsData;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.contact.ContactPickerFragment;
import com.android.messaging.ui.conversation.ConversationActivity;
import com.android.messaging.ui.conversation.ConversationFragment;
import com.android.messaging.util.ImeUtil;

import java.util.ArrayList;

import bkav.android.blur.activity.BkavBlurHelper;
import bkav.android.blur.activity.WallpaperBlurCompat;
import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.esim.ActiveDefaultProfileReceiver;
import bkav.android.btalk.messaging.ui.contact.BtalkContactPickerFragment;
import bkav.android.btalk.messaging.ui.contact.BtalkConversationFragment;

import static com.android.messaging.ui.conversation.ConversationActivityUiState.STATE_CONVERSATION_ONLY;
import static com.android.messaging.ui.conversation.ConversationActivityUiState.STATE_CONVERSATION_ONLY_AND_CAN_ADD_MORE_PARTICIPANTS;

/**
 * Created by quangnd on 26/03/2017.
 * class custom lai ConversationActivity cua source goc
 */

public class BtalkConversationActivity extends ConversationActivity
        implements BtalkConversationFragment.BtalkConversationFragmentHost, WallpaperBlurCompat.ChangeWallPaperListener {

    private ConversationParticipantsData mParticipantDatas;

    private static final int REQUEST_SET_DEFAULT_SMS_APP = 1;

    private BkavBlurHelper mBkavBlurHelper;

    private Toolbar mToolbar;

    // Anhdts bien kiem tra xem co phai duoc dua thang len tren dau stack
    private boolean mAloneInStack = false;

    @Override
    protected void initUIState(Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SENDTO)) {
            mAloneInStack = true;
        }
        if (mUiState == null) {
            final String conversationId = intent.getStringExtra(
                    UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID);
            mUiState = new BtalkConversationActivityUiState(conversationId);
        }
    }

    // Bkav HaiKH - Fix bug BOS-3727- Start
    // Show toast khi đặt btalk làm ứng dụng mặc định khi ở trong conversation
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SET_DEFAULT_SMS_APP) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.notify_btalk_as_sms_default, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.notify_btalk_not_as_sms_default, Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Bkav HaiKH - Fix bug BOS-3727- End

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        final ConversationFragment conversation = getConversationFragment();
        if (conversation != null && mUiState!=null && mUiState.shouldShowConversationFragment()){
            conversation.saveStatus();
        }
        final ContactPickerFragment picker = getContactPicker();
        if (picker != null && mUiState!=null && mUiState.shouldShowContactPickerFragment()){
            picker.saveStatus();
        }
        // Anhdts: neu o dau stack thi nen finish di vi neu mo lai Btalk se bi
        // kill stack
        if (mAloneInStack) {
            finish();
        }
    }

    @Override
    public void onNavigationUpPressed() {
        final BtalkContactPickerFragment pickerFragment = (BtalkContactPickerFragment)getContactPicker();
        if (pickerFragment != null && pickerFragment.onNavigationUpPressed()) {
            return;
        }
        super.onNavigationUpPressed();
    }

    @Override
    public void onFinishCurrentConversation() {
        //Bkav QuangNDb hide keyboard khi bam vao on backpress cua conversation fragment
        ImeUtil.get().hideImeKeyboard(this, mRootView);
        super.onFinishCurrentConversation();
    }

    //Bkav QuangNDb xu ly khi bam vao nut back tren toolbar
    @Override
    public void onBackButtonPressed() {
        //Bkav QuangNDb hide keyboard khi bam vao on backpress cua ContactPickerFragment
        ImeUtil.get().hideImeKeyboard(this, mRootView);
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        //HienDTk: goi su kien back ben ConversationFragment de cap nhat giao dien
        if(getConversationFragment() != null)
        getConversationFragment().onBackPressed();
        //Bkav QuangNDb gui su kien back de update lai profile mac dinh khi activity la root task
        if (isTaskRoot()) {
            Intent intent = new Intent(ActiveDefaultProfileReceiver.EVENT_BACK_PRESS);
            sendBroadcast(intent);

        }

        final BtalkContactPickerFragment pickerFragment = (BtalkContactPickerFragment)getContactPicker();
        if (pickerFragment != null && pickerFragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onAddMoreContact(ConversationParticipantsData data) {
        mParticipantDatas = data;
        mUiState.onAddMoreContact();
    }

    @Override
    public ActionMode.Callback getModeCallback() {
        return getActionModeCallback();
    }

    @Override
    protected ContactPickerFragment getContactPicker() {
        return (BtalkContactPickerFragment) getFragmentManager().findFragmentByTag(
                BtalkContactPickerFragment.FRAGMENT_TAG);
    }

    @Override
    protected ConversationFragment getConversationFragment() {
        return (BtalkConversationFragment) getFragmentManager().findFragmentByTag(
                BtalkConversationFragment.FRAGMENT_TAG);
    }


    @Override
    protected void updateInnerActionbar(ActionBar actionBar) {
        actionBar.setShowHideAnimationEnabled(false);// hide animation khi hide action bar
        super.updateInnerActionbar(actionBar);
    }

    @Override
    protected ConversationFragment initConversationFragment() {
        if (mUiState.getState() == STATE_CONVERSATION_ONLY_AND_CAN_ADD_MORE_PARTICIPANTS) {
            return BtalkConversationFragment.newInstance(true);
        } else {
            return BtalkConversationFragment.newInstance(false);
        }

    }

    @Override
    protected ContactPickerFragment getInstanceContactPicker() {
        // Khoi tao participants khi ma nguoi dung muon them nguoi vao cuoc hoi thoai
        ArrayList<ParticipantData> participantDatas = new ArrayList<>();
        if (mParticipantDatas != null) {
            for (ParticipantData participantData : mParticipantDatas) {
                if (!participantData.isSelf()) {
                    participantDatas.add(participantData);
                }
            }
        }
        return BtalkContactPickerFragment.newInstance(participantDatas);
    }

    @Override
    protected void setBtalkConversationFragmentHot(ConversationFragment conversationFragment) {
        conversationFragment.setBtalkConversationFragmentHost(this);
    }

    @Override
    protected void setBtalkPickerFragmentHot(ContactPickerFragment contactPickerFragment) {
        contactPickerFragment.setBtalkPickerFragmentHost(this);
    }

    @Override
    protected void setActivityContentView() {
        WallpaperBlurCompat blurCompat = WallpaperBlurCompat.getInstance(getApplicationContext());
        // Anhdts dang ki lang nghe doi man hinh
        blurCompat.addOnChangeWallpaperListener(this);
        mBkavBlurHelper = new BkavBlurHelper(this, R.layout.btalk_conversation_activity,
                blurCompat.isConfigBkav());
        View contentView = mBkavBlurHelper.createView(false);
        if (WallpaperBlurCompat.getInstance(getApplicationContext()).isConfigBkav()) {
            contentView.setBackground(new BitmapDrawable(getResources(), blurCompat.getWallpaperBlur()));
        }
        setContentView(contentView);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onBackButtonPressed();
            }
        });
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public void changeState() {
        mUiState.setConversationContactUiState(STATE_CONVERSATION_ONLY);
    }

    // Anhdts doi man hinh
    @Override
    public void onChangeWallpaper() {
        mBkavBlurHelper.changeWallpaper(WallpaperBlurCompat.getInstance(getApplicationContext()).getWallpaperBlur());
    }

    /**
     * Anhdts xoa listener change background
     */
    @Override
    public void onDestroy(){
        WallpaperBlurCompat.getInstance(getApplicationContext()).removeOnChangeWallpaperListener(this);
        super.onDestroy();
    }

    @Override
    protected Intent getParentIntent() {
        return new Intent(this, BtalkActivity.class);
    }

    @Override
    public void getDrafData(MessageData draf) {
        mDrafMessageData = draf;
    }
    @Override
    public void getSelfSimId(String selfSimId) {
        mSelfSimId = selfSimId;
    }

    @Override
    public void setUIState(int state) {
        mUiState.setConversationContactUiState(state);
    }

    @Override
    public void changeUiState(int state) {
        mUiState.performUiStateUpdate(state,true);
    }

    /**Bkav QuangNDb sua size toolbar khi o giao dien picker de khong bi loi giao dien*/
    @Override
    public void fixSizeToolbar() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mToolbar.getLayoutParams();
        layoutParams.height = getResources().getDimensionPixelSize(R.dimen.btalk_actionbar_picker_height);
        mToolbar.setLayoutParams(layoutParams);
        mToolbar.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.btalk_actionbar_picker_height));
        mToolbar.requestLayout();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        final ContactPickerFragment contactPickerFragment = getContactPicker();
        if (hasFocus && contactPickerFragment != null) {
            contactPickerFragment.setConversationFocus();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        ConversationFragment conversationFragment = getConversationFragment();
        if (conversationFragment != null) {
            if (conversationFragment.isDeepShortcutTouch(ev)) {
                return true;
            }

            // Bkav HuyNQN su kien khi co them danh sach esim
            if(conversationFragment.isListEsimTouch(ev)){
                return true;
            }
        }
        ContactPickerFragment contactPickerFragment = getContactPicker();
        if (contactPickerFragment != null) {
            if (contactPickerFragment.isDeepShortcutTouch(ev)) {
                return true;
            }

            // Bkav HuyNQN su kien khi co them danh sach esim
            if(contactPickerFragment.isListEsimTouch(ev)){
                return true;
            }
        }

        if (getContactPicker() != null) {
            ((BtalkContactPickerFragment) getContactPicker()).touchActivity();
        }

        try {
            return super.dispatchTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
