package bkav.android.btalk.messaging.ui.conversationlist;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.contacts.common.ContactPhotoManager;
import com.android.messaging.ui.conversationlist.ConversationListAdapter;
import com.android.messaging.ui.conversationlist.ConversationListItemView;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 27/03/2017.
 */

public class BtalkConversationListAdapter extends ConversationListAdapter {

    public BtalkConversationListAdapter(Context context, Cursor cursor
            , ConversationListItemView.HostInterface clivHostInterface) {
        super(context, cursor, clivHostInterface);
    }

    @Override
    public ConversationListViewHolder createViewHolder(Context context, ViewGroup parent, int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        BtalkConversationListItemView itemView =
                (BtalkConversationListItemView) layoutInflater.inflate(
                        R.layout.btalk_conversation_list_item_view, parent,false);
        return new ConversationListViewHolder(itemView);
    }

}