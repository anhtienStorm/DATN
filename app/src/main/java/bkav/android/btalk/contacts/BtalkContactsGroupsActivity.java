package bkav.android.btalk.contacts;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.contacts.ContactsActivity;
import com.android.contacts.activities.GroupDetailActivity;
import com.android.contacts.common.ContactsUtils;
import com.android.contacts.group.GroupBrowseListFragment;

import bkav.android.btalk.R;

/**
 * AnhNDd: activity để hiển thị giao diện groups.
 */
public class BtalkContactsGroupsActivity extends ContactsActivity {

    private GroupBrowseListFragment mGroupsFragment;

    private static final int SUBACTIVITY_NEW_GROUP = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.btalk_contacts_groups);
        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState != null) {
                return;
            }
            mGroupsFragment = new GroupBrowseListFragment();

            final FragmentManager fragmentManager = getFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.fragment_container, mGroupsFragment).commit();

            mGroupsFragment.setListener(new GroupBrowserActionListener());

            mGroupsFragment.setSelectionVisible(false);
            mGroupsFragment.setAddAccountsVisibility(!areGroupWritableAccountsAvailable());
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private final class GroupBrowserActionListener implements GroupBrowseListFragment.OnGroupBrowserActionListener {

        GroupBrowserActionListener() {
        }

        @Override
        public void onViewGroupAction(Uri groupUri) {
            Intent intent = new Intent(BtalkContactsGroupsActivity.this, BtalkGroupDetailActivity.class);
            intent.setData(groupUri);
            startActivity(intent);
        }
    }

    private boolean areGroupWritableAccountsAvailable() {
        return ContactsUtils.areGroupWritableAccountsAvailable(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.btalk_groups_contacts_option, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_add_group) {
            createNewGroup();
            return true;
        } else if (i == android.R.id.home) {
            finish();
        }
        return false;
    }

    private void createNewGroup() {
        final Intent intent = new Intent(this, BtalkGroupEditorActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        startActivityForResult(intent, SUBACTIVITY_NEW_GROUP);
    }
}
