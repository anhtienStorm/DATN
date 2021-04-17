package bkav.android.btalk.messaging.ui.conversation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.ui.conversation.SimSelectorItemView;
import com.android.messaging.ui.conversation.SimSelectorView;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 19/07/2017.
 */

public class BtalkSimSelectorView extends SimSelectorView {

    public BtalkSimSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
// TODO: bo BtalkListView di vi khong dung
//    @Override
//    protected void initSimListView() {
//
//        mSimListView = (BtalkSimListView) findViewById(R.id.sim_list);
//        mSimListView.setAdapter(mAdapter);
//    }

    @Override
    protected SimSelectorAdapter getInstanceAdapter() {
        return new BtalkSimSelectorAdapter(getContext(), R.layout.btalk_sim_selector_item_view);
    }

    protected class BtalkSimSelectorAdapter extends SimSelectorAdapter{

        public BtalkSimSelectorAdapter(Context context, int layoutRes) {
            super(context, layoutRes);
        }

        @Override
        protected SimSelectorItemView concatSimLayout(LayoutInflater inflater, ViewGroup parent) {
            return (BtalkSimSelectorItemView) inflater.inflate(mItemLayoutId,
                    parent, false);
        }

        @Override
        protected SimSelectorItemView concatSimSelector(View convertView) {
            return (BtalkSimSelectorItemView) convertView;
        }

        @Override
        protected boolean getConditionInsteanof(View convertView) {
            return convertView instanceof BtalkSimSelectorItemView;
        }
    }
}
