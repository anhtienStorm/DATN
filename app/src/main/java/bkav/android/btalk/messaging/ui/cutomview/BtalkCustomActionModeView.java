package bkav.android.btalk.messaging.ui.cutomview;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.android.messaging.datamodel.data.ConversationListData;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.datamodel.data.BtalkConversationListItemData;

/**
 * Created by quangnd on 28/03/2017.
 * Bkav QuangNDB: custom lai actionMode de chuyen xuong ben duoi view duoc
 */

public class BtalkCustomActionModeView extends RelativeLayout implements View.OnClickListener, ConversationListData.ConversationListDataListener {


    public interface Listener {

        void onActionBarDelete(Collection<SelectedConversation> conversations);

        void onActionBarArchive(Iterable<SelectedConversation> conversations,
                                boolean isToArchive);

        void onActionBarNotification(Iterable<SelectedConversation> conversations,
                                     boolean isNotificationOn);

        void onActionBarAddContact(final SelectedConversation conversation);

        void onActionBarBlock(final SelectedConversation conversation);

        void onActionBarHome();

        void onSelectAllConversation();

        void onUnSelectAllConversation();
    }

    public static class SelectedConversation {

        public final String conversationId;

        public final long timestamp;

        public final String icon;

        public final String otherParticipantNormalizedDestination;

        public final CharSequence participantLookupKey;

        public final boolean isGroup;

        public final boolean isArchived;

        public final boolean notificationEnabled;

        public SelectedConversation(ConversationListItemData data) {
            conversationId = data.getConversationId();
            timestamp = data.getTimestamp();
            icon = data.getIcon();
            otherParticipantNormalizedDestination = data.getOtherParticipantNormalizedDestination();
            participantLookupKey = data.getParticipantLookupKey();
            isGroup = data.getIsGroup();
            isArchived = data.getIsArchived();
            notificationEnabled = data.getNotificationEnabled();
        }
    }

    private ArrayMap<String, SelectedConversation> mSelectedConversations;

    private Listener mListener;

    private ImageButton mArchiveButton;

    private ImageButton mAddContactButton;

    private ImageButton mBlockButton;

    private ImageButton mDeleteButton;

    private ImageButton mNotificationOnButton;

    private ImageButton mNotificationOffButton;

    private ImageButton mHomeButton;

    private boolean mHasInflated;

    private HashSet<String> mBlockedSet;

    private Context mContext;

    private int mItemCount;//Bkav QuangNDb bien dem so conversation da selected

    private ImageView mExpandSelect;//Bkav QuangNDb icon expand item select

    private Button mSelectionMenu;//Bkav QuangNDb bien hien thi so icon count

    private View mMoreView;//Bkav QuangNDb view more

    public BtalkCustomActionModeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.btalk_custom_action_mode_view, this, true);
        mArchiveButton = (ImageButton) view.findViewById(R.id.action_archive);
        mAddContactButton = (ImageButton) view.findViewById(R.id.action_add_contact);
        mBlockButton = (ImageButton) view.findViewById(R.id.action_block);
        mDeleteButton = (ImageButton) view.findViewById(R.id.action_delete);
        mNotificationOnButton = (ImageButton) view.findViewById(R.id.action_notification_on);
        mNotificationOffButton = (ImageButton) view.findViewById(R.id.action_notification_off);
        mHomeButton = (ImageButton) view.findViewById(R.id.action_home);
        mExpandSelect = (ImageView) view.findViewById(R.id.expand_more_view);
        mExpandSelect.setColorFilter(ContextCompat.getColor(context, android.R.color.white));
        mSelectionMenu = (Button) view.findViewById(R.id.selection_menu);
        mSelectionMenu.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        mMoreView = view.findViewById(R.id.navigation_bar);
        handleClick();
        mHasInflated = true;
    }

    private void handleClick() {
        mArchiveButton.setOnClickListener(this);
        mAddContactButton.setOnClickListener(this);
        mBlockButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        mNotificationOnButton.setOnClickListener(this);
        mNotificationOffButton.setOnClickListener(this);
        mHomeButton.setOnClickListener(this);
        mMoreView.setOnClickListener(this);
        mSelectionMenu.setOnClickListener(this);
        mExpandSelect.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_archive:
                mListener.onActionBarArchive(mSelectedConversations.values(), true);
                break;
            case R.id.action_add_contact:
                mListener.onActionBarAddContact(mSelectedConversations.valueAt(0));
                break;
            case R.id.action_delete:
                mListener.onActionBarDelete(mSelectedConversations.values());
                break;
            case R.id.action_block:
                Assert.isTrue(mSelectedConversations.size() == 1);
                mListener.onActionBarBlock(mSelectedConversations.valueAt(0));
                break;
            case R.id.action_notification_on:
                mListener.onActionBarNotification(mSelectedConversations.values(), true);
                break;
            case R.id.action_notification_off:
                mListener.onActionBarNotification(mSelectedConversations.values(), false);
                break;
            case R.id.action_home:
                mListener.onActionBarHome();
                break;
            case R.id.navigation_bar:
                clickMoreView();
                break;
            case R.id.selection_menu:
                clickMoreView();
                break;
            case R.id.expand_more_view:
                clickMoreView();
                break;
        }
    }

    private void clickMoreView() {
        PopupMenu popupMenu = new PopupMenu(mContext, mExpandSelect, Gravity.START);
        popupMenu.getMenuInflater().inflate(R.menu.btalk_message_action_mode_menu
                , popupMenu.getMenu());
        MenuItem numberConversationItem = popupMenu.getMenu().findItem(R.id.action_show_num_conversation);
        numberConversationItem.setTitle(String.valueOf(mItemCount));
        MenuItem selecAllConversation = popupMenu.getMenu().findItem(R.id.action_select_all_conversation);
        selecAllConversation.setTitle(mIsAllSelect ? R.string.menu_select_none : R.string.select_all);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_select_all_conversation:
                        if (mIsAllSelect) {
                            mListener.onUnSelectAllConversation();
                        } else {
                            mListener.onSelectAllConversation();
                        }
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    public void setListener(Listener listener) {
        mListener = listener;
        mSelectedConversations = new ArrayMap<>();
    }

    private boolean mIsFirst = true;

    public void toggleSelect(final ConversationListData listData,
                             final ConversationListItemData conversationListItemData) {
        Assert.notNull(conversationListItemData);
        mBlockedSet = listData.getBlockedParticipants();
        final String id = conversationListItemData.getConversationId();
        if (mSelectedConversations.containsKey(id)) {
            mSelectedConversations.remove(id);
        } else {
            mSelectedConversations.put(id, new SelectedConversation(conversationListItemData));
        }

        if (mSelectedConversations.isEmpty()) {
            mListener.onActionBarHome();
        } else {
            updateActionIconsVisiblity();
        }
    }

    private boolean mIsAllSelect = false;

    /**
     * Bkav QuangNDb chon tat ca conversation hien co
     */
    public void toggleAllSelect(List<ConversationListItemData> allConversations) {
        for (ConversationListItemData conversationListItemData : allConversations) {
            final String id = conversationListItemData.getConversationId();
            if (!mSelectedConversations.containsKey(id)) {
                mSelectedConversations.put(id, new SelectedConversation(conversationListItemData));
            }
        }
        mIsAllSelect = true;
        updateActionIconsVisiblity();
    }

    /**
     * Bkav QuangNDb bo chon tat ca conversation hien co
     */
    public void toggleUnAllSelect(List<ConversationListItemData> allConversations) {
        for (ConversationListItemData conversationListItemData : allConversations) {
            final String id = conversationListItemData.getConversationId();
            if (mSelectedConversations.containsKey(id)) {
                mSelectedConversations.remove(id);
            }
        }
        mIsAllSelect = false;
        updateActionIconsVisiblity();
    }

    public void resetAllSelect() {
        mIsAllSelect = false;
    }

    private List<ConversationListItemData> mAllConversation = new ArrayList<>();

    @Override
    public void onConversationListCursorUpdated(ConversationListData data, Cursor cursor) {
        ConversationListItemData conversationListData = new BtalkConversationListItemData();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                conversationListData.bind(cursor);
                mAllConversation.add(conversationListData);
            } while (cursor.moveToNext());
        }
    }

    @Override
    public void setBlockedParticipantsAvailable(boolean blockedAvailable) {

    }

    @Override
    public void onSubscriptionListDataLoaded(ConversationListData data) {

    }

    public boolean isSelected(final String selectedId) {
        return mSelectedConversations.containsKey(selectedId);
    }

    private void updateActionIconsVisiblity() {
        if (!mHasInflated) {
            return;
        }
        mItemCount = mSelectedConversations.size();
        mSelectionMenu.setText(String.valueOf(mItemCount));
        if (mSelectedConversations.size() == 1) {
            final SelectedConversation conversation = mSelectedConversations.valueAt(0);
            // The look up key is a key given to us by contacts app, so if we have a look up key,
            // we know that the participant is already in contacts.
            final boolean isInContacts = !TextUtils.isEmpty(conversation.participantLookupKey);
            if (!conversation.isGroup && !isInContacts) {
                mAddContactButton.setVisibility(VISIBLE);
            } else {
                mAddContactButton.setVisibility(GONE);
            }
            // ParticipantNormalizedDestination is always null for group conversations.
            final String otherParticipant = conversation.otherParticipantNormalizedDestination;
            if (otherParticipant != null && !mBlockedSet.contains(otherParticipant)) {
                mBlockButton.setVisibility(VISIBLE);
            } else {
                mBlockButton.setVisibility(GONE);
            }
        } else {
            mBlockButton.setVisibility(GONE);
            mAddContactButton.setVisibility(GONE);
        }

        boolean hasCurrentlyArchived = false;
        boolean hasCurrentlyUnarchived = false;
        boolean hasCurrentlyOnNotification = false;
        boolean hasCurrentlyOffNotification = false;
        final Iterable<SelectedConversation> conversations = mSelectedConversations.values();
        for (final SelectedConversation conversation : conversations) {
            if (conversation.notificationEnabled) {
                hasCurrentlyOnNotification = true;
            } else {
                hasCurrentlyOffNotification = true;
            }

            if (conversation.isArchived) {
                hasCurrentlyArchived = true;
            } else {
                hasCurrentlyUnarchived = true;
            }

            // If we found at least one of each example we don't need to keep looping.
            if (hasCurrentlyOffNotification && hasCurrentlyOnNotification &&
                    hasCurrentlyArchived && hasCurrentlyUnarchived) {
                break;
            }
        }
        // If we have notification off conversations we show on button, if we have notification on
        // conversation we show off button. We can show both if we have a mixture.
        if (hasCurrentlyOnNotification) {
            mNotificationOffButton.setVisibility(VISIBLE);
        } else {
            mNotificationOffButton.setVisibility(GONE);
        }

        if (hasCurrentlyOffNotification) {
            mNotificationOnButton.setVisibility(VISIBLE);
        } else {
            mNotificationOnButton.setVisibility(GONE);
        }
        //Quangndb do doan nay o lop ngoai giao dien nen khong bao h hien off ca
        mArchiveButton.setVisibility(VISIBLE);
//        mUnarchiveMenuItem.setVisible(hasCurrentlyArchived);
    }
}
