package bkav.android.btalk.messaging.ui.contacts.common.list;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.MoreContactUtils;
import com.android.contacts.common.SimContactsConstants;
import com.android.contacts.common.list.PhoneNumberListAdapter;
import com.android.contacts.common.list.PhoneNumberPickerFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.contacts.BtalkContactListItemView;

/**
 * Created by quangnd on 20/04/2017.
 * fragment pick contact custom lai cua sour goc
 */

public class BtalkContactPhoneNumberPickerFragment extends PhoneNumberPickerFragment {
    private TextView mNotifyText;

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        mNotifyText = (TextView) mView.findViewById(R.id.empty_list_title);
    }

    @Override
    protected PhoneNumberListAdapter concatPhoneNumberAdapter() {
        return (BtalkContactPhoneNumberListAdapter) getAdapter();
    }

    @Override
    protected PhoneNumberListAdapter getPhoneNumberListAdapter() {
        PhoneNumberListAdapter adapter = new BtalkContactPhoneNumberListAdapter(getActivity());
        if (mIsConvertPhone) {
            ((BtalkContactPhoneNumberListAdapter) adapter).setConvertPhoneMode();
        }
        return adapter;
    }

    @Override
    public CursorLoader createCursorLoader(Context context) {
        return new BtalkNumberAndContactLoader(context);
    }

    @Override
    protected int getIdContentLayout() {
        return R.layout.btalk_contact_picker_phone_num_list_content;
    }

    @Override

    public void setSectionHeaderDisplayEnabledInSearchMode(boolean bool) {
        //AnhNDd: luôn luôn hiển thị header.
        setSectionHeaderDisplayEnabled(true);
    }

    @Override
    public void setConfigureDefaultPartitionInSearchMode(boolean showIfEmty, boolean hasHeader) {
        //AnhNDd: khong hien thi header. mac dinh
        getAdapter().configureDefaultPartition(showIfEmty, false);
    }

    private int mCount = 0;

    @Override
    protected void configureVerticalScrollbar() {
        if (mListView != null) {
            mListView.setFastScrollEnabled(false);
            mListView.setFastScrollAlwaysVisible(false);
            mListView.setVerticalScrollbarPosition(mVerticalScrollbarPosition);
            mListView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        }

        if (mIsConvertPhone) {
            mListView.setPadding(0, 0, 0, mContext.getResources().getDimensionPixelOffset(R.dimen.height_confirm_button));
            mView.findViewById(R.id.container_confirm_button).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.container_confirm_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AsyncTaskConvertContact(getActivity()).execute();
                }
            });
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCount = mAdapter.getCount();
                    ((TextView) mView.findViewById(R.id.confirm_button)).setText(getString(R.string.confirm_convert_phone,
                            mCount));
                }
            }, 500);
        }
    }

    private class AsyncTaskConvertContact extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        WeakReference<Activity> mActivityWeakReference;

        AsyncTaskConvertContact(Activity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
            dialog = new ProgressDialog(mActivityWeakReference.get());
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(mActivityWeakReference.get().getString(R.string.converting));
            dialog.setCancelable(false);
            dialog.show();
        }

        protected Void doInBackground(Void... args) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            for (int i = 0; i < mAdapter.getCount(); i++) {
                Object object = mAdapter.getItem(i);
                if (object instanceof Cursor) {
                    Cursor cursor = (Cursor) object;
                    long dataId = cursor.getLong(PhoneNumberListAdapter.PhoneQuery.PHONE_ID);
                    if (((BtalkContactPhoneNumberListAdapter) mAdapter).
                            isExceptionContact(dataId)) {
                        continue;
                    }

                    String accountName = null;
                    String accountType = null;

                    if (!BtalkActivity.isAndroidQ()){
                        accountName = cursor.getString(PhoneNumberListAdapter.PhoneQuery.PHONE_ACCOUNT_NAME);
                        accountType = cursor.getString(PhoneNumberListAdapter.PhoneQuery.PHONE_ACCOUNT_TYPE);
                    }

                    String phoneNumber = cursor.getString(PhoneNumberListAdapter.PhoneQuery.PHONE_NUMBER);
                    String phoneConvert = convertPhone(phoneNumber);
                    if (TextUtils.isEmpty(phoneConvert)) {
                        continue;
                    }

                    if (SimContactsConstants.ACCOUNT_TYPE_SIM.equals(accountType)) {
                        final int subscription = MoreContactUtils.getSubscription(
                                accountType, accountName);
                        ContentValues values = new ContentValues();
                        values.put(SimContactsConstants.STR_TAG, cursor.getString(PhoneNumberListAdapter.PhoneQuery.DISPLAY_NAME));
                        values.put(SimContactsConstants.STR_NUMBER, cursor.getString(PhoneNumberListAdapter.PhoneQuery.PHONE_NUMBER));
                        values.put(SimContactsConstants.STR_NEW_TAG, cursor.getString(PhoneNumberListAdapter.PhoneQuery.DISPLAY_NAME));
                        values.put(SimContactsConstants.STR_NEW_NUMBER, phoneConvert);
                        update(values, subscription, mActivityWeakReference.get());
                    }
                    ContentProviderOperation.Builder builder =
                            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
                    builder.withSelection(ContactsContract.CommonDataKinds.Phone._ID + "=?", new String[]{String.valueOf(dataId)});
                    builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                            phoneConvert);
                    ops.add(builder.build());
                }
            }
            try {
                mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            // do UI work here
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (mAdapter.getCount() == 0) {
                        mActivityWeakReference.get().finish();
                        Toast.makeText(mActivityWeakReference.get(), mActivityWeakReference.get().getString(R.string.convert_success),
                                Toast.LENGTH_LONG).show();
                    } else {
                        mCount = mAdapter.getCount();
                        ((TextView) mView.findViewById(R.id.confirm_button)).setText(getString(R.string.confirm_convert_phone,
                                mCount - ((BtalkContactPhoneNumberListAdapter)mAdapter).getCountChoose()));
                    }
                    mActivityWeakReference.clear();
                }
            }, 500);
        }
    }

    private String convertPhone(String phoneNumber) {
        int headNumber;
        String formatNumber = phoneNumber.replaceAll("-", "");
        formatNumber = formatNumber.replaceAll(" ", "");

        String headText;
        String prefixNumber;
        String detailConvert;
        if (formatNumber.startsWith("+84")) {
            headNumber = Integer.parseInt(formatNumber.substring(1, 6));
            headText = formatNumber.substring(0, 6);
            prefixNumber = "+84";
        } else if (formatNumber.startsWith("84")) {
            headNumber = Integer.parseInt(formatNumber.substring(0, 5));
            headText = formatNumber.substring(0, 5);
            prefixNumber = "84";
        } else {
            headNumber = Integer.parseInt(formatNumber.substring(1, 4));
            headText = formatNumber.substring(0, 4);
            prefixNumber = "0";
        }
        int pos = 0;
        for (char c : headText.toCharArray()) {
            if (c != phoneNumber.charAt(pos)) {
                pos++;
                while (phoneNumber.charAt(pos) != c) {
                    pos++;
                }
                pos++;
            } else {
                pos++;
            }
        }
        detailConvert = phoneNumber.substring(pos);
        headNumber = BtalkContactPhoneNumberListAdapter.getHeader(headNumber);

        return prefixNumber + "" + headNumber + "" + detailConvert;
    }

    public int update(ContentValues values, int subscription, Context context) {
        Uri uri = getContentUri(subscription);

        int result = 0;
        if (uri == null)
            return result;
        String oldNumber = values.getAsString(SimContactsConstants.STR_NUMBER);
        String newNumber = values.getAsString(SimContactsConstants.STR_NEW_NUMBER);
        values.put(SimContactsConstants.STR_NUMBER, PhoneNumberUtils.stripSeparators(oldNumber));
        values.put(SimContactsConstants.STR_NEW_NUMBER, PhoneNumberUtils.stripSeparators(newNumber));
        result = context.getContentResolver().update(uri, values, null, null);
        return result;

    }

    private Uri getContentUri(int subscription) {
        Uri uri = null;
        SubscriptionManager sm = (SubscriptionManager) mContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        SubscriptionInfo subInfoRecord = null;
        try {
            subInfoRecord = sm.getActiveSubscriptionInfoForSimSlotIndex(subscription);
        } catch (SecurityException e) {
            Log.w("Anhdts", "SecurityException thrown, lack permission for"
                    + " getActiveSubscriptionInfoList", e);
        }

        if (subInfoRecord != null) {
            uri = Uri.parse(SimContactsConstants.SIM_SUB_URI
                    + subInfoRecord.getSubscriptionId());
        }
        return uri;
    }

    @Override
    protected void showEmptyList(boolean searchMode, int i) {
        mNotifyText.setText(searchMode ? R.string.notify_empty_contact_find : R.string.notify_empty_contact_new_message);
        mNotifyText.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onItemClick(View view, int position, long id) {
        if (mIsConvertPhone) {
            if (view instanceof BtalkContactListItemView) {
                ((BtalkContactListItemView) view).changeCheckPhoto();
            }
            ((BtalkContactPhoneNumberListAdapter) mAdapter).onItemClick(id);
            ((TextView) mView.findViewById(R.id.confirm_button)).setText(getString(R.string.confirm_convert_phone,
                    mCount - ((BtalkContactPhoneNumberListAdapter) mAdapter).getCountChoose()));
        }
    }

    @Override
    protected void onItemClick(int position, long id) {
        if (!mIsConvertPhone) {
            super.onItemClick(position, id);
        }
    }
}
