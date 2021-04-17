package bkav.android.btalk.messaging.ui.contacts.common.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.contacts.common.list.PhoneNumberListAdapter;
import com.android.ex.chips.RecipientEntry;
import com.android.messaging.Factory;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 26/04/2017.
 * class chon cotact khi them moi 1 conversation
 */

public class BtalkPhoneNumberPickerFragment extends BtalkContactPhoneNumberPickerFragment {
    private View mBackGroundView;
    public interface BtalkPhoneNumberPickerFragmentHost {

        void onContactItemClick(RecipientEntry recipientEntry);

        boolean onContactSelected(RecipientEntry recipientEntry);
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        mBackGroundView = mView.findViewById(R.id.pinned_header_list_layout);
        mBackGroundView.setBackgroundColor(Factory.get().getApplicationContext().getResources().getColor(android.R.color.transparent));
    }

    private BtalkPhoneNumberPickerFragmentHost mHost;

    public void setHost(BtalkPhoneNumberPickerFragmentHost host) {
        this.mHost = host;
    }

    @Override
    protected int getIdContentLayout() {
        return R.layout.btalk_contact_picker_phone_num_list_content;
    }

    @Override
    protected void onItemClick(int position, long id) {
        if (mHost != null) {
            mHost.onContactItemClick((RecipientEntry) mAdapter.getRecipientEntry(position));
            mAdapter.notifyDataSetChanged();
        }
    }

    /**Bkav QuangNDb refesh adapter de update trang thai*/
    public void refreshAdapter() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }
    @Override
    protected PhoneNumberListAdapter getPhoneNumberListAdapter() {
        return (mHost == null) ? null : new BtalkPhoneNumberPickerAdapter(getActivity(), mHost);
    }

    public void setUnKnownPhoneNum(String unKnownPhoneNum) {
        mAdapter.setUknowPhoneNumber(unKnownPhoneNum);
    }

    @Override
    protected void hideSoftKeyboard() {
//        super.hideSoftKeyboard();
        //Bkav QuangNDb bo hide keyboard di vi khong cho an ban phim khi touch vao 1 contact nua
    }
}
