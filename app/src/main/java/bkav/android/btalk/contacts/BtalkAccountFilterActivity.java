package bkav.android.btalk.contacts;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.contacts.common.list.AccountFilterActivity;
import com.android.contacts.common.list.ContactListFilter;
import com.android.contacts.common.list.ContactListFilterView;

import java.util.List;

import bkav.android.btalk.R;


/**
 * AnhNDd class dùng để hiển thị "danh bạ hiển thị"
 */
public class BtalkAccountFilterActivity extends AccountFilterActivity {
    @Override
    public void hookToSetContentView() {
        setContentView(R.layout.btalk_contact_list_filter);
    }

    protected static class BtalkFilterListAdapter extends FilterListAdapter {

        public BtalkFilterListAdapter(Context context, List<ContactListFilter> filters, ContactListFilter current) {
            super(context, filters, current);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ContactListFilterView view;
            if (convertView != null) {
                view = (ContactListFilterView) convertView;
            } else {
                view = (ContactListFilterView) mLayoutInflater.inflate(
                        R.layout.btalk_contact_list_filter_item, parent, false);
            }
            view.setSingleAccount(mFilters.size() == 1);
            final ContactListFilter filter = mFilters.get(position);
            view.setContactListFilter(filter);
            view.bindView(mAccountTypes);
            view.setTag(filter);
            view.setActivated(filter.equals(mCurrentFilter));
            return view;
        }
    }

    @Override
    public void setAdapterForFilterList(List<ContactListFilter> data) {
        mListView.setAdapter(
                new BtalkFilterListAdapter(BtalkAccountFilterActivity.this, data, mCurrentFilter));
    }

    @Override
    public Intent createIntentCustomContactListFilterActivity() {
        final Intent intent = new Intent(this,
                BtalkCustomContactListFilterActivity.class);
        return intent;
    }
}
