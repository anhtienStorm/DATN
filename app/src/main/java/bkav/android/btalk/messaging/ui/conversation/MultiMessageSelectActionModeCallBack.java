package bkav.android.btalk.messaging.ui.conversation;

import android.util.ArrayMap;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;

import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.util.Assert;

import java.util.Collection;
import java.util.List;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 13/04/2017.
 * Bkav QuangNDb class call back xu ly khi long click nhieu message
 */

public class MultiMessageSelectActionModeCallBack implements Callback {

    public interface MultiMessageSelectListener {

        // Bkav TienNAb: Them ham lang nghe su kien khi click chon tat ca
        void onActionSelectAll();
        void onActionUnSelectAll();

        void onActionDelete(Collection<ConversationMessageData> selectedMessages);

        void onActionAttachmentSave(ConversationMessageData selectedMessages);

        void onActionReDownload(ConversationMessageData selectMessage);

        void onActionResend(Collection<ConversationMessageData> selectedMessages);

        void onActionCopyText(ConversationMessageData selectedMessage);

        void onActionDetails(ConversationMessageData selectedMessage);

        void onActionForward(Collection<ConversationMessageData> selectedMessages);

        void onActionShare(ConversationMessageData selectedMessage);

        void onExitActionMode();
    }

    private MultiMessageSelectListener mListener;

    private MenuItem mShareItem;

    private MenuItem mForwardItem;

    private MenuItem mSaveItem;

    private MenuItem mCopyItem;

    private MenuItem mDetailsItem;

    private MenuItem mResendItem;

    private MenuItem mReDownloadItem;

    private MenuItem mDeleteItem;

    // Bkav TienNAb: Them option chon tat ca
    private MenuItem mSelectAll;

    private boolean mHasInflated;

    // Bkav TienNAb: Tao bien check co dang o che do chon tat ca hay khong
    private boolean mIsAllSelect = false;

    private final ArrayMap<String, ConversationMessageData> mSelectedMessages;

    public MultiMessageSelectActionModeCallBack(MultiMessageSelectListener listener) {
        this.mListener = listener;
        this.mSelectedMessages = new ArrayMap<>();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.btalk_conversation_fragment_select_menu, menu);
        mShareItem = menu.findItem(R.id.share_message_menu);
        mForwardItem = menu.findItem(R.id.forward_message_menu);
        mSaveItem = menu.findItem(R.id.save_attachment);
        mCopyItem = menu.findItem(R.id.copy_text);
        mDetailsItem = menu.findItem(R.id.details_menu);
        mResendItem = menu.findItem(R.id.action_send);
        mReDownloadItem = menu.findItem(R.id.action_download);
        mDeleteItem = menu.findItem(R.id.action_delete_message);
        // Bkav TienNAb: gan gia tri cho bien mSelectAll
        mSelectAll = menu.findItem(R.id.action_select_all);
        mHasInflated = true;
        updateActionIconsVisibility();
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.share_message_menu:
                Assert.isTrue(mSelectedMessages.size() == 1);
                mListener.onActionShare(mSelectedMessages.valueAt(0));
                break;
            case R.id.forward_message_menu:
                mListener.onActionForward(mSelectedMessages.values());
                break;
            case R.id.save_attachment:
                Assert.isTrue(mSelectedMessages.size() == 1);
                mListener.onActionAttachmentSave(mSelectedMessages.valueAt(0));
                break;
            case R.id.copy_text:
                Assert.isTrue(mSelectedMessages.size() == 1);
                mListener.onActionCopyText(mSelectedMessages.valueAt(0));
                break;
            case R.id.details_menu:
                Assert.isTrue(mSelectedMessages.size() == 1);
                mListener.onActionDetails(mSelectedMessages.valueAt(0));
                break;
            case R.id.action_send:
                mListener.onActionResend(mSelectedMessages.values());
                break;
            case R.id.action_download:
                Assert.isTrue(mSelectedMessages.size() == 1);
                mListener.onActionReDownload(mSelectedMessages.valueAt(0));
                break;
            case R.id.action_delete_message:
                mListener.onActionDelete(mSelectedMessages.values());
                break;
            // Bkav TienNAb: xu ly khi an nut chon tat ca
            case R.id.action_select_all:
                if (mIsAllSelect){
                    mListener.onActionUnSelectAll();
                } else {
                    mListener.onActionSelectAll();
                }
                break;
            case android.R.id.home:

                mListener.onExitActionMode();
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Bkav QuangNDb ham update giao dien khi check nhieu tin nhan
     */
    public void toggleSelect(final ConversationMessageData messageData) {
        Assert.notNull(messageData);
        final String id = messageData.getMessageId();
        if (mSelectedMessages.containsKey(id)) {
            mSelectedMessages.remove(id);
        }else {
            mSelectedMessages.put(id, messageData);
        }
        if (mSelectedMessages.isEmpty()) {
            mListener.onExitActionMode();
        }else {
            updateActionIconsVisibility();
        }
    }

    // Bkav TienNAb: xu ly chon chon tat ca
    public void toggleSelectAll(final List<ConversationMessageData> messageDatas){
        for (ConversationMessageData messageData : messageDatas){
            final String id = messageData.getMessageId();
            if (!mSelectedMessages.containsKey(id)){
                mSelectedMessages.put(id, messageData);
            }
        }
        // Bkav TienNAb: Set lai title cho chuc nang chon tat ca
        mSelectAll.setTitle(R.string.action_un_select_all);
        mIsAllSelect = true;
        updateActionIconsVisibility();
    }

    // Bkav TienNAb: xu ly bo chon tat ca
    public void toggleUnSelectAll(final List<ConversationMessageData> messageDatas){
        for (ConversationMessageData messageData : messageDatas){
            final String id = messageData.getMessageId();
            if (mSelectedMessages.containsKey(id)){
                mSelectedMessages.remove(id);
            }
        }
        // Bkav TienNAb: Set lai title cho chuc nang chon tat ca
        mSelectAll.setTitle(R.string.action_select_all);
        mIsAllSelect = false;
        if (mSelectedMessages.isEmpty())
            mListener.onExitActionMode();
        updateActionIconsVisibility();
    }


    public boolean isSelected(final String id) {
        return mSelectedMessages.containsKey(id);
    }
    /**
     * Bkav QuangNdb update trang thai hien an cua cac item menu cu action mode
     */
    private void updateActionIconsVisibility() {
        if (!mHasInflated) {
            return;
        }
        if (mSelectedMessages.isEmpty()){
            return;
        }
        if (mSelectedMessages.size() == 1) {
            ConversationMessageData messageData = mSelectedMessages.valueAt(0);
            mReDownloadItem.setVisible(messageData.getShowDownloadMessage());
            mShareItem.setVisible(messageData.getCanForwardMessage());
            mCopyItem.setVisible(messageData.getCanCopyMessageToClipboard());
            mSaveItem.setVisible(messageData.hasAttachments());
            mDetailsItem.setVisible(true);
        } else {
            mReDownloadItem.setVisible(false);
            mShareItem.setVisible(false);
            mCopyItem.setVisible(false);
            mSaveItem.setVisible(false);
            mDetailsItem.setVisible(false);
        }
        mResendItem.setVisible(isAllResend());
    }

    /**
     * Bkav QuangNDb check xem tat ca tin nhan da chon co phai tin nhan loi can gui lai hay khong
     */
    private boolean isAllResend() {
        for (ConversationMessageData messageData : mSelectedMessages.values()) {
            if (!messageData.getShowResendMessage()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mListener = null;
        mSelectedMessages.clear();
        mHasInflated = false;
    }
}
