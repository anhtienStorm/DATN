package bkav.android.btalk.contacts;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.android.contacts.activities.GroupDetailActivity;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.group.GroupDetailFragment;

import bkav.android.btalk.R;

/**
 * class kế thừa từ GroupDetailActivity để điều chỉnh thông tin chi tiết của groups
 */
public class BtalkGroupDetailActivity extends GroupDetailActivity {

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    @Override
    public void handleMenuItemHome() {
        finish();
    }

    @Override
    public void setFragmentListener() {
        mFragment = (BtalkGroupDetailFragment) getFragmentManager().findFragmentById(
                R.id.group_detail_fragment);
        mFragment.setListener(mFragmentListener);
    }

    private final GroupDetailFragment.Listener mFragmentListener =
            new GroupDetailFragment.Listener() {

                @Override
                public void onGroupSizeUpdated(String size) {
                    //getActionBar().setSubtitle(size);
                }

                @Override
                public void onGroupTitleUpdated(String title) {
                    getActionBar().setTitle(title);
                }

                @Override
                public void onAccountTypeUpdated(String accountTypeString, String dataSet) {
                    mAccountTypeString = accountTypeString;
                    mDataSet = dataSet;
                    invalidateOptionsMenu();
                }

                @Override
                public void onEditRequested(Uri groupUri) {
                    final Intent intent = new Intent(BtalkGroupDetailActivity.this, BtalkGroupEditorActivity.class);
                    intent.setData(groupUri);
                    intent.setAction(Intent.ACTION_EDIT);
                    startActivity(intent);
                }

                @Override
                public void onContactSelected(Uri contactUri) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
                    ImplicitIntentsUtil.startActivityInApp(BtalkGroupDetailActivity.this, intent);
                }

            };

    @Override
    public void hookToSetContentView() {
        setContentView(R.layout.btalk_group_detail_activity);
    }
}
