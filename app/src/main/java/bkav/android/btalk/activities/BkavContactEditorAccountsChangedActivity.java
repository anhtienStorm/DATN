package bkav.android.btalk.activities;

import android.support.annotation.NonNull;

import com.android.contacts.activities.ContactEditorAccountsChangedActivity;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.util.AccountsListAdapter;

import java.util.List;

import bkav.android.btalk.contacts.BtalkAccountsListAdapter;
import bkav.android.btalk.utility.Config;

public class BkavContactEditorAccountsChangedActivity extends ContactEditorAccountsChangedActivity {

    @NonNull
    @Override
    protected AccountsListAdapter initAccountListAdapter() {
        return new BtalkAccountsListAdapter(this,
                AccountsListAdapter.AccountListFilter.ACCOUNTS_CONTACT_WRITABLE);
    }
}
