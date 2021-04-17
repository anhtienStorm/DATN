package bkav.android.btalk.contacts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.contacts.common.list.DefaultContactListAdapter;
import com.android.contacts.common.logging.ScreenEvent;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.list.MultiSelectContactsListFragment;
import com.android.contacts.list.MultiSelectEntryContactListAdapter;

import bkav.android.btalk.R;


/**
 * AnhNDd: class là giao diện trong khi export danh bạ
 */
public class BtalkExportMultiSelectContactsListFragment extends MultiSelectContactsListFragment {

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        displayCheckBoxes(true);
    }

    @Override
    public void shouldShowEmptyUserProfile(boolean bool) {
        super.shouldShowEmptyUserProfile(false);
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.btalk_export_contact_list, null);
        return view;
    }

    @Override
    public DefaultContactListAdapter createMultiSelectEntryContactListAdapter(Context context) {
        return new BtalkMultiSelectEntryContactListAdapter(context);
    }

    @Override
    public MultiSelectEntryContactListAdapter getAdapter() {
        //AnhNDd: setlistener
        BtalkMultiSelectEntryContactListAdapter adapter = (BtalkMultiSelectEntryContactListAdapter) super.getAdapter();
        adapter.setListener(new ContactActionListener());
        return adapter;
    }

    /**
     * AnhNDd: lớp thực thi việc gọi ngược từ việc click item trên adapter.
     */
    private final class ContactActionListener implements BtalkMultiSelectEntryContactListAdapter.OnBtalkContactListItemViewListener {

        @Override
        public void showContactAction(Uri contactLookupUri, boolean isEnterpriseContact) {
            if (isEnterpriseContact) {
                // No implicit intent as user may have a different contacts app in work profile.
                ContactsContract.QuickContact.showQuickContact(getContext(), new Rect(), contactLookupUri,
                        BtalkQuickContactActivity.MODE_FULLY_EXPANDED, null);
            } else {
                final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(
                        contactLookupUri, BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
                intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                        isSearchMode() ? ScreenEvent.ScreenType.SEARCH : ScreenEvent.ScreenType.ALL_CONTACTS);
                ImplicitIntentsUtil.startActivityInApp(getContext(), intent);
            }
        }
    }

    @Override
    protected void updateFilterHeaderView() {
        super.updateFilterHeaderView();
        mAccountFilterHeader.setVisibility(View.GONE);
    }

    @Override
    public void setSectionHeaderDisplayEnabledInSearchMode(boolean bool) {
        //AnhNDd: luôn luôn hiển thị header.
        setSectionHeaderDisplayEnabled(true);
    }

    @Override
    public void setConfigureDefaultPartitionInSearchMode(boolean showIfEmty, boolean hasHeader) {
        //AnhNDd: khong hien thi header.
        getAdapter().configureDefaultPartition(showIfEmty, false);
    }

    @Override
    protected void configureVerticalScrollbar() {
        if (mListView != null) {
            mListView.setFastScrollEnabled(false);
            mListView.setFastScrollAlwaysVisible(false);
            mListView.setVerticalScrollbarPosition(mVerticalScrollbarPosition);
            mListView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        }
    }
}
