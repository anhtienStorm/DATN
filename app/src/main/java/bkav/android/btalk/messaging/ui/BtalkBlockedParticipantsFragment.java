package bkav.android.btalk.messaging.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.android.messaging.ui.BlockedParticipantsFragment;
import com.android.messaging.util.Assert;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 26/05/2017.
 */

public class BtalkBlockedParticipantsFragment extends BlockedParticipantsFragment {

    @Override
    protected BlockedParticipantListAdapter getAdapter() {
        return new BtalkBlockedParticipantListAdapter(getActivity(), null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public class BtalkBlockedParticipantListAdapter extends BlockedParticipantsFragment.BlockedParticipantListAdapter{

        public BtalkBlockedParticipantListAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        protected int getIdResLayout() {
            return R.layout.btalk_blocked_participant_list_item_view;
        }

        @Override
        protected void binAdapterView(View view, Cursor cursor) {
            Assert.isTrue(view instanceof BtalkBlockedParticipantListItemView);
            ((BtalkBlockedParticipantListItemView) view).bind(
                    mBinding.getData().createParticipantListItemData(cursor));
        }
    }
}
