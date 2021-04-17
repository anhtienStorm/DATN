package bkav.android.btalk.contacts;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.contacts.common.list.CustomContactListFilterActivity;
import com.android.contacts.common.model.account.AccountType;

import bkav.android.btalk.R;

/**
 * AnhNDd: class giao diện xác định kiểm xem tùy chỉnh trong danh bạ hiển thị
 */
public class BtalkCustomContactListFilterActivity extends CustomContactListFilterActivity {
    @Override
    public void hookToSetContentView() {
        setContentView(R.layout.btalk_contact_list_filter_custom);
    }

    @Override
    public void createAdapter() {
        mAdapter = new BtalkDisplayAdapter(this);
    }

    protected static class BtalkDisplayAdapter extends DisplayAdapter {

        public BtalkDisplayAdapter(Context context) {
            super(context);
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(
                        R.layout.btalk_custom_contact_list_filter_group, parent, false);
            }

            final TextView title = (TextView) convertView.findViewById(android.R.id.text1);
            final TextView content = (TextView) convertView.findViewById(android.R.id.text2);
            final CheckBox checkbox = (CheckBox) convertView.findViewById(android.R.id.checkbox);

            final AccountDisplay account = mAccounts.get(groupPosition);
            final GroupDelta child = (GroupDelta) this.getChild(groupPosition, childPosition);
            if (child != null) {
                // Handle normal group, with title and checkbox
                final boolean groupVisible = child.getVisible();
                checkbox.setVisibility(View.VISIBLE);
                checkbox.setChecked(groupVisible);

                final CharSequence groupTitle = child.getTitle(mContext);
                title.setText(groupTitle);
                content.setVisibility(View.GONE);
            } else {
                // When unknown child, this is "more" footer view
                checkbox.setVisibility(View.GONE);
                title.setText(R.string.display_more_groups);
                content.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
}
