/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.common.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.common.R;
import com.android.contacts.common.MoreContactUtils;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.model.account.SimAccountType;

import java.util.ArrayList;
import java.util.List;

/**
 * List-Adapter for Account selection
 */
public class AccountsListAdapter extends BaseAdapter {
    protected final LayoutInflater mInflater;
    protected final List<AccountWithDataSet> mAccounts;
    private final AccountTypeManager mAccountTypes;
    protected final Context mContext;
    private int mCustomLayout = -1;

    /**
     * Filters that affect the list of accounts that is displayed by this adapter.
     */
    public enum AccountListFilter {
        ALL_ACCOUNTS,                   // All read-only and writable accounts
        ACCOUNTS_CONTACT_WRITABLE,      // Only where the account type is contact writable
        ACCOUNTS_GROUP_WRITABLE,        // Only accounts where the account type is group writable
        ACCOUNTS_CONTACT_WRITABLE_WITHOUT_SIM
    }

    public AccountsListAdapter(Context context, AccountListFilter accountListFilter) {
        this(context, accountListFilter, null);
    }

    /**
     * @param currentAccount the Account currently selected by the user, which should come
     * first in the list. Can be null.
     */
    public AccountsListAdapter(Context context, AccountListFilter accountListFilter,
            AccountWithDataSet currentAccount) {
        mContext = context;
        mAccountTypes = AccountTypeManager.getInstance(context);
        mAccounts = getAccounts(accountListFilter);
        if (currentAccount != null
                && !mAccounts.isEmpty()
                && !mAccounts.get(0).equals(currentAccount)
                && mAccounts.remove(currentAccount)) {
            mAccounts.add(0, currentAccount);
        }
        mInflater = LayoutInflater.from(context);
    }

    protected List<AccountWithDataSet> getAccounts(AccountListFilter accountListFilter) {
        if (accountListFilter == AccountListFilter.ACCOUNTS_GROUP_WRITABLE) {
            return new ArrayList<AccountWithDataSet>(mAccountTypes.getAccounts(true,
                    AccountTypeManager.FLAG_ALL_ACCOUNTS_WITHOUT_LOCAL));
        }
        final List<AccountWithDataSet> writableAccountList = mAccountTypes
                .getAccounts(accountListFilter == AccountListFilter.ACCOUNTS_CONTACT_WRITABLE
                || accountListFilter == AccountListFilter.ACCOUNTS_CONTACT_WRITABLE_WITHOUT_SIM);
        List<AccountWithDataSet> deletedList = new ArrayList<AccountWithDataSet>();

        if (accountListFilter == AccountListFilter.ACCOUNTS_CONTACT_WRITABLE_WITHOUT_SIM) {
            for (AccountWithDataSet account : writableAccountList) {
                if (SimAccountType.ACCOUNT_TYPE.equals(account.type))
                    deletedList.add(account);
            }
            writableAccountList.removeAll(deletedList);
        }

        return writableAccountList;
    }

    public void setCustomLayout(int customLayout) {
        mCustomLayout = customLayout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View resultView = convertView != null && convertView.findViewById(R.id.checkbox) == null ?
                convertView : mInflater.inflate(mCustomLayout > 0 ? mCustomLayout :
                        R.layout.account_selector_list_item, parent, false);

        final TextView text1 = (TextView) resultView.findViewById(android.R.id.text1);
        final TextView text2 = (TextView) resultView.findViewById(android.R.id.text2);
        final ImageView icon = (ImageView) resultView.findViewById(android.R.id.icon);

        final AccountWithDataSet account = mAccounts.get(position);
        final AccountType accountType = mAccountTypes.getAccountType(account.type, account.dataSet);

        text1.setText(accountType.getDisplayLabel(mContext));

        // For email addresses, we don't want to truncate at end, which might cut off the domain
        // name.
        text2.setText(account.name);
        text2.setEllipsize(TruncateAt.MIDDLE);

        icon.setImageDrawable(accountType.getDisplayIcon(mContext));

        MoreContactUtils.setSimOperatorName(account.name, text2, mContext);
        Drawable customIcon = MoreContactUtils.getDisplayIcon(mContext, account.name);
        if (customIcon != null) {
            icon.setImageDrawable(customIcon);
        }
        return resultView;
    }

    @Override
    public int getCount() {
        return mAccounts.size();
    }

    @Override
    public AccountWithDataSet getItem(int position) {
        return mAccounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    // TrungTH
    public static final String ACC_GOOGLE = "com.google";
}

