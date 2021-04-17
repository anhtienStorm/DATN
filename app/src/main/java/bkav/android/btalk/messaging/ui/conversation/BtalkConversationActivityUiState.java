package bkav.android.btalk.messaging.ui.conversation;

import com.android.messaging.ui.conversation.ConversationActivityUiState;
import com.android.messaging.util.Assert;

/**
 * Created by quangnd on 27/03/2017.
 */

public class BtalkConversationActivityUiState extends ConversationActivityUiState {

    private static final String GROUP_ID = "group_id";

    BtalkConversationActivityUiState(String conversationId) {
        super(conversationId);
    }

    @Override
    public void onGetOrCreateConversation(String conversationId) {
        int newState = STATE_CONVERSATION_ONLY;
        if (mConversationContactUiState == STATE_CREATE_GROUP_CONVERSATION) {
            newState = STATE_CONTACT_PICKER_ONLY_ADD_MORE_CONTACTS;
        } else if (mConversationContactUiState == STATE_CONTACT_PICKER_ONLY_INITIAL_CONTACT
                || mConversationContactUiState == STATE_CONTACT_PICKER_ONLY_ADD_MORE_CONTACTS
                || mConversationContactUiState == STATE_CONTACT_PICKER_ONLY_MAX_PARTICIPANTS) {
            newState = STATE_CONVERSATION_ONLY;
        } else {
            // New conversation should only be created when we are in one of the contact picking
            // modes.
            Assert.fail("Invalid conversation activity state: can't create conversation!");
        }
        mConversationId = conversationId;
        performUiStateUpdate(newState, true);
    }

    @Override
    public boolean shouldShowConversationFragment() {
        return mConversationContactUiState == STATE_HYBRID_WITH_CONVERSATION_AND_CHIPS_VIEW
                || mConversationContactUiState == STATE_CONVERSATION_ONLY
                || mConversationContactUiState == STATE_CONVERSATION_ONLY_AND_CAN_ADD_MORE_PARTICIPANTS;
    }

    @Override
    public boolean shouldShowContactPickerFragment() {
        return mConversationContactUiState == STATE_CREATE_GROUP_CONVERSATION ||
                mConversationContactUiState == STATE_CONTACT_PICKER_ONLY_ADD_MORE_CONTACTS ||
                mConversationContactUiState == STATE_CONTACT_PICKER_ONLY_MAX_PARTICIPANTS ||
                mConversationContactUiState == STATE_CONTACT_PICKER_ONLY_INITIAL_CONTACT ||
                mConversationContactUiState == STATE_HYBRID_WITH_CONVERSATION_AND_CHIPS_VIEW;
    }

    @Override
    public void onAddMoreContact() {
        if (mConversationContactUiState == STATE_CONVERSATION_ONLY_AND_CAN_ADD_MORE_PARTICIPANTS) {
            performUiStateUpdate(STATE_CONTACT_PICKER_ONLY_ADD_MORE_CONTACTS, true);
        }
    }

    @Override
    protected boolean checkAddMoreContactState() {
        return true;
    }

    @Override
    protected boolean checkReady() {
        return mConversationContactUiState == STATE_HYBRID_WITH_CONVERSATION_AND_CHIPS_VIEW
                || mConversationContactUiState == STATE_CONTACT_PICKER_ONLY_ADD_MORE_CONTACTS;
    }

    @Override
    protected int getFirstState(String conversationId) {
        if (conversationId == null) {
            return STATE_CONTACT_PICKER_ONLY_INITIAL_CONTACT;
        } else if (conversationId.equals(GROUP_ID)) {
            return STATE_CREATE_GROUP_CONVERSATION;
        } else {
            return STATE_CONVERSATION_ONLY;
        }
    }
}
