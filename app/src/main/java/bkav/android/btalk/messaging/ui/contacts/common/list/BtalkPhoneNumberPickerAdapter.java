package bkav.android.btalk.messaging.ui.contacts.common.list;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import com.android.contacts.common.list.ContactListItemView;
import com.android.ex.chips.RecipientEntry;

import bkav.android.btalk.contacts.BtalkContactListItemView;

/**
 * Created by quangnd on 27/04/2017.
 * adapter dung trong class BtalkPhoneNumberListFragment
 */

public class BtalkPhoneNumberPickerAdapter extends BtalkContactPhoneNumberListAdapter {

    private BtalkPhoneNumberPickerFragment.BtalkPhoneNumberPickerFragmentHost mHost;

    public BtalkPhoneNumberPickerAdapter(Context context, BtalkPhoneNumberPickerFragment.BtalkPhoneNumberPickerFragmentHost host) {
        super(context);
        mHost = host;
    }

    @Override
    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        super.bindView(itemView, partition, cursor, position);
        ContactListItemView contactView = (BtalkContactListItemView) itemView;
        boolean isSelected = mHost.onContactSelected((RecipientEntry) getRecipientEntry(position));
        itemView.setSelected(isSelected);
        contactView.setChecked(isSelected, false);
    }

    @Override
    public void bindDividerSection(ContactListItemView view, int position) {
        final BtalkContactListItemView btalkContactListItemView = (BtalkContactListItemView) view;
        Placement placement = getItemPlacementInSection(position);
        btalkContactListItemView.getViewDividerSection().setVisibility(placement.sectionHeader != null ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void setNotFoundSearchResult(ContactListItemView view) {
//        if (MmsSmsUtils.isPhoneNumber(mUnknownPhoneNumber)) {
//            view.setUnknownNameText();
//        }
    }
}
