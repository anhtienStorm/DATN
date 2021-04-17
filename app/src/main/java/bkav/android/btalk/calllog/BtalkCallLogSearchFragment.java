package bkav.android.btalk.calllog;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.contacts.common.CallUtil;
import com.android.contacts.common.list.ContactListFilterController;
import com.android.contacts.common.list.DefaultContactListAdapter;
import com.android.contacts.common.logging.ScreenEvent;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.list.MultiSelectContactsListFragment;
import com.android.contacts.list.ProviderStatusWatcher;
import com.android.dialer.calllog.IntentProvider;
import com.android.dialer.util.DialerUtils;
import com.android.messaging.Factory;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.BtalkDialogChooseMutilNumber;
import bkav.android.btalk.R;
import bkav.android.btalk.contacts.BtalkContactsActivity;
import bkav.android.btalk.contacts.BtalkQuickContactActivity;
import bkav.android.btalk.messaging.BtalkFactoryImpl;

/**
 * Created by anhdt on 04/12/2017.
 */

public class BtalkCallLogSearchFragment extends MultiSelectContactsListFragment implements
        ContactListFilterController.ContactListFilterListener,
        ProviderStatusWatcher.ProviderStatusListener,
        Loader.OnLoadCompleteListener<Cursor>,
        BtalkCallLogSearchContactListAdapter.OnSearchCallLogItemListener,
        BtalkDialogChooseMutilNumber.ChooseNumberListener {

    private ProviderStatusWatcher mProviderStatusWatcher;

    private Integer mProviderStatus;

    private ContactListFilterController mContactListFilterController;

    private final static int ACTION_NONE = 0;

    private final static int ACTION_SEND_MESSAGE = 1;

    private final static int ACTION_CALL = 2;

    private int mCurrentAction;

    private List<BtalkDialogChooseMutilNumber.DataPhone> mPhoneDataItemsList;

    private final static int LOADER_DATA_CONTACT = 1212;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        mContactListFilterController = ContactListFilterController.getInstance(getActivity().getApplicationContext());
        mContactListFilterController.checkFilterValidity(false);
        mContactListFilterController.addListener(this);

        mProviderStatusWatcher = ProviderStatusWatcher.getInstance(getActivity().getApplicationContext());
        mProviderStatusWatcher.addListener(this);
        getLoaderManager().initLoader(LOADER_DATA_CONTACT, null, this);
    }
    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        displayCheckBoxes(false);
        getAdapter().setListener(this);
    }



    @Override
    public void onDestroy() {
        if (mProviderStatusWatcher != null) {
            mProviderStatusWatcher.removeListener(this);
        }
        if (mContactListFilterController != null) {
            mContactListFilterController.removeListener(this);
        }
        super.onDestroy();
    }

    @Override
    public DefaultContactListAdapter createMultiSelectEntryContactListAdapter(Context context) {
        return new BtalkCallLogSearchContactListAdapter(context);
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.btalk_search_calllog_fragment, null);
    }


    @Override
    public void onContactListFilterChanged() {
        setFilter(mContactListFilterController.getFilter());
    }

    @Override
    public void onProviderStatusChange() {
        updateViewConfiguration(false);
    }

    @Override
    public BtalkCallLogSearchContactListAdapter getAdapter() {
        return (BtalkCallLogSearchContactListAdapter) super.getAdapter();
    }

    @Override
    protected void updateFilterHeaderView() {
        super.updateFilterHeaderView();
        if (mAccountFilterHeader == null) {
            if (mView != null) {
                mAccountFilterHeader = mView.findViewById(R.id.account_filter_header_container);
            }
        }
        if (mAccountFilterHeader != null) {
            mAccountFilterHeader.setVisibility(View.GONE);
        }
    }

    @Override
    public void setSectionHeaderDisplayEnabledInSearchMode(boolean bool) {
        setSectionHeaderDisplayEnabled(true);
    }

    @Override
    public void setConfigureDefaultPartitionInSearchMode(boolean showIfEmty, boolean hasHeader) {
        getAdapter().configureDefaultPartition(showIfEmty, false);
    }

    @Override
    public void shouldShowEmptyUserProfile(boolean bool) {
        if (BtalkContactsActivity.USE_BTALK) {
            super.shouldShowEmptyUserProfile(false);
        } else {
            super.shouldShowEmptyUserProfile(bool);
        }
    }

    @Override
    protected void configureVerticalScrollbar() {
        if (mListView != null) {
            mListView.setFastScrollEnabled(true);
            mListView.setFastScrollAlwaysVisible(true);
            mListView.setVerticalScrollbarPosition(mVerticalScrollbarPosition);
            mListView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        }
    }

    private void updateViewConfiguration(boolean forceUpdate) {
        int providerStatus = mProviderStatusWatcher.getProviderStatus();
        if (!forceUpdate && (mProviderStatus != null)
                && (mProviderStatus.equals(providerStatus))) return;
        mProviderStatus = providerStatus;

    }

    @Override
    public void onPause() {
        mProviderStatusWatcher.stop();
        super.onPause();
    }

    public void setQueryString(String queryString) {
        setQueryString(queryString, true);
    }

    private static final String PHONE_NUMBER_SELECTION =
            ContactsContract.Data.MIMETYPE + " IN ('"
                    + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "', "
                    + "'" + ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE + "') AND "
                    + ContactsContract.Data.DATA1 + " NOT NULL";

    private CursorLoader mLoader;

    private static final String[] PHONE_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone._ID,                  // 0
            ContactsContract.CommonDataKinds.Phone.NUMBER,              // 1
            ContactsContract.CommonDataKinds.Phone.LABEL,               // 2
            ContactsContract.CommonDataKinds.Phone.TYPE                 // 3
    };

    private void actionSendMessage(String number) {
        final IntentProvider intentProvider = IntentProvider.getSendSmsIntentProvider(number);
        final Intent intent = intentProvider.getIntent(getActivity());
        ((BtalkFactoryImpl) Factory.get()).registerSendMessage();
        DialerUtils.startActivityWithErrorToast(getActivity(), intent);
        onClickActionFinish();
    }

    @Override
    public void showContactAction(Uri contactLookupUri, boolean isEnterpriseContact) {
        if (isEnterpriseContact) {
            // No implicit intent as user may have a different contacts app in work profile.
            ContactsContract.QuickContact.showQuickContact(getActivity(), new Rect(), contactLookupUri,
                    BtalkQuickContactActivity.MODE_FULLY_EXPANDED, null);
        } else {
            final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(
                    contactLookupUri, BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
            intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                    ScreenEvent.ScreenType.ALL_CONTACTS);
            ImplicitIntentsUtil.startActivityInApp(getActivity(), intent);
        }
    }

    @Override
    public void sendMessageAction(Uri contactLookupUri, boolean isEnterpriseContact) {
        mCurrentAction = ACTION_SEND_MESSAGE;
        startInteraction(contactLookupUri);
    }

    @Override
    public void onClick(String number, int actionId, Activity activity) {
        if (number != null && !number.isEmpty()) {
            if (ACTION_SEND_MESSAGE == actionId) {
                actionSendMessage(number);
            } else if (ACTION_CALL == actionId) {
                actionCall(number);
            }
        }
    }

    @Override
    public void viewContactAndCall(Uri contactLookupUri, boolean isEnterpriseContact) {
//        mCurrentAction = ACTION_CALL;
//        super.setSelectedContactUri(contactLookupUri, false, false, true, false);
//        startInteraction(contactLookupUri);
        showContactAction(contactLookupUri, isEnterpriseContact);
    }

    public void startInteraction(Uri uri) {
        if (mLoader != null) {
            mLoader.reset();
        }
        final Uri queryUri;
        final String inputUriAsString = uri.toString();
        if (inputUriAsString.startsWith(ContactsContract.Contacts.CONTENT_URI.toString())) {
            if (!inputUriAsString.endsWith(ContactsContract.Contacts.Data.CONTENT_DIRECTORY)) {
                queryUri = Uri.withAppendedPath(uri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
            } else {
                queryUri = uri;
            }
        } else if (inputUriAsString.startsWith(ContactsContract.Data.CONTENT_URI.toString())) {
            queryUri = uri;
        } else {
            throw new UnsupportedOperationException(
                    "Input Uri must be contact Uri or data Uri (input: \"" + uri + "\")");
        }

        mLoader = new CursorLoader(mContext,
                queryUri,
                PHONE_PROJECTION,
                PHONE_NUMBER_SELECTION,
                null,
                null);
        mLoader.registerListener(LOADER_DATA_CONTACT, this);
        mLoader.startLoading();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return super.onCreateLoader(id, args);
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        if (getActivity() == null) {
            return;
        }
        if (data == null || data.getCount() == 0) {
            Toast.makeText(getActivity(), getString(R.string.toast_cannot_call_without_number), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean mMultiNumber = data.getCount() > 1;
        String mNumberQuery = null;
        if (!mMultiNumber) {
            if (data.getCount() > 0) {
                data.moveToFirst();
                mNumberQuery = data.getString(1);
            }
        } else {
            try {
                if (mPhoneDataItemsList == null) {
                    mPhoneDataItemsList = new ArrayList<>();
                } else {
                    mPhoneDataItemsList.clear();
                }
                data.moveToFirst();
                do {
                    BtalkDialogChooseMutilNumber.DataPhone dataPhone = new BtalkDialogChooseMutilNumber.DataPhone(data);
                    mPhoneDataItemsList.add(dataPhone);
                } while (data.moveToNext());
            } finally {
                data.close();
            }
        }
        if (mMultiNumber) {
            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();

            BtalkDialogChooseMutilNumber dialogChooseMutilNumber = new BtalkDialogChooseMutilNumber();
            dialogChooseMutilNumber.setListener(this, mCurrentAction); // TrungTH sua lai
            dialogChooseMutilNumber.setDataCursor(mPhoneDataItemsList, getActivity());

            dialogChooseMutilNumber.show(ft, BtalkDialogChooseMutilNumber.DIALOG_TAG);
        } else if (mCurrentAction == ACTION_CALL) {
            if (mNumberQuery != null && !TextUtils.isEmpty(mNumberQuery)) {
                actionCall(mNumberQuery);
            }
        } else if (mCurrentAction == ACTION_SEND_MESSAGE) {
            if (mNumberQuery != null && !TextUtils.isEmpty(mNumberQuery)) {
                actionSendMessage(mNumberQuery);
            }
        }
        mCurrentAction = ACTION_NONE;
    }

    private void actionCall(String mNumberQuery) {
        final Intent intentCall = CallUtil.getCallIntent(mNumberQuery);
        intentCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        DialerUtils.startActivityWithErrorToast(mContext, intentCall);
        onClickActionFinish();
    }

    interface IOnClickCallback {
        void onClickActionFinish();
    }

    private IOnClickCallback mIDialogCallback;

    public void setOnClickCallback(IOnClickCallback onclickCallback) {
        mIDialogCallback = onclickCallback;
    }

    public void onClickActionFinish() {
        if (mIDialogCallback != null) {
            mIDialogCallback.onClickActionFinish();
        }
    }

}
