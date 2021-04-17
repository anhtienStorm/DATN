package bkav.android.btalk.messaging.ui.mediapicker.contact;

import bkav.android.btalk.messaging.ui.contacts.common.list.BtalkContactPhoneNumberPickerFragment;

/**
 * Created by quangnd on 03/05/2017.
 * class hien thi giao dien pick contact o media picker(Attach)
 */

public class BtalkContactChooserFragment extends BtalkContactPhoneNumberPickerFragment {

    public interface BtalkContactChooserFragmentHost {

        void onItemClick(String textContact);
    }

    private BtalkContactChooserFragmentHost mHost;


    public void setHost(BtalkContactChooserFragmentHost host) {
        this.mHost = host;
    }

    @Override
    protected void onItemClick(int position, long id) {
        if (mHost != null) {
            mHost.onItemClick(mAdapter.getContactDisplayName(position) + " <" + mAdapter.getPhoneNumber(position) + "> ");
        }
    }
}
