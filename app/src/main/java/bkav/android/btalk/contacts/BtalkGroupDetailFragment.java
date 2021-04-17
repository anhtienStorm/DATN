package bkav.android.btalk.contacts;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.contacts.group.GroupDetailFragment;

import bkav.android.btalk.R;

/**
 * AnhNDd: giao diện xem thông tin của nhóm này
 */
public class BtalkGroupDetailFragment extends GroupDetailFragment {
    @Override
    public void createRootView(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.btalk_group_detail_fragment, container, false);
    }
}
