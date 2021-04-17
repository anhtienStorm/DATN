package bkav.android.btalk.messaging.ui.conversationlist;

import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;

import com.android.messaging.ui.conversationlist.ConversationListFragment;
import com.android.messaging.ui.conversationlist.ForwardMessageActivity;
import com.android.messaging.util.Assert;

/**
 * Created by quangnd on 08/04/2017.
 */

public class BtalkForwardMessageActivity extends ForwardMessageActivity {
    private BtalkConversationListFragment mConversationListFragment;

    @Override
    protected ConversationListFragment setUpFragment() {
        return BtalkConversationListFragment.createForwardMessageConversationListFragment();
    }

    // Bkav HuyNQN fix loi chuyen tiep tin nhan bi crash app
    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ConversationListFragment) {
            mConversationListFragment = (BtalkConversationListFragment) fragment;
            mConversationListFragment.setHost(this);
        }
    }
}
