package bkav.android.btalk.contacts;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.util.AccountsListAdapter;

import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.utility.Config;

/**
 * Anhdts lop chon tai khoan luu
 */
public class BtalkAccountsListAdapter extends AccountsListAdapter {

    private boolean mIsSetDefault;

    public BtalkAccountsListAdapter(Context context, AccountListFilter accountListFilter) {
        super(context, accountListFilter);
    }

    public BtalkAccountsListAdapter(Context context, AccountListFilter accountListFilter, AccountWithDataSet currentAccount,
                             CheckDefaultListener listener, boolean isSetDefault) {
        super(context, accountListFilter, currentAccount);
        mListener = listener;
        mIsSetDefault = isSetDefault;
    }

    private boolean mIsCheck = false;

    @Override
    public int getCount() {
        return  mAccounts.size() + (mListener !=null ? 1: 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == getCount() - 1 && mListener !=null) {
            View view = mInflater.inflate(R.layout.checkbox_make_primary, parent, false);
            final CheckBox checkbox = view.findViewById(R.id.checkbox);
            checkbox.setChecked(mIsSetDefault);
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mIsCheck = isChecked;
                    mListener.setCheckDefault(isChecked);
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIsCheck = !checkbox.isChecked();
                    checkbox.setChecked(mIsCheck);
                    mListener.setCheckDefault(mIsCheck);
                }
            });
            return view;
        } else {
            return super.getView(position, convertView, parent);
        }
    }

    @Override
    public AccountWithDataSet getItem(int position) {
        return mAccounts.get(position);
    }

    public boolean isCheck() {
        return mIsCheck;
    }

    private CheckDefaultListener mListener;

    public interface CheckDefaultListener {
        void setCheckDefault(boolean isCheck);
    }
}
