package bkav.android.btalk.speeddial;

/**
 * Created by anhdt on 27/11/2017.
 *
 */

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.android.contacts.common.ContactPhotoManager;
import com.android.dialer.SpeedDialUtils;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.incallui.Log;
import com.android.messaging.util.PhoneUtils;

import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.utility.BtalkUiUtils;


public class BtalkSpeedDialListActivity extends ListActivity
        implements
        AdapterView.OnItemClickListener, PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "SpeedDial";
    private static final String ACTION_ADD_VOICEMAIL =
            "com.android.phone.CallFeaturesSetting.ADD_VOICEMAIL";
    public static final String EXTRA_INITIAL_PICK_NUMBER = "initialPickNumber";

    // Extra on intent containing the id of a subscription.
    public static final String SUB_ID_EXTRA =
            "com.android.phone.settings.SubscriptionInfoHelper.SubscriptionId";
    // Extra on intent containing the label of a subscription.
    private static final String SUB_LABEL_EXTRA =
            "com.android.phone.settings.SubscriptionInfoHelper.SubscriptionLabel";
    public static final String SUBSCRIPTION_KEY = "subscription";

    private static final String[] LOOKUP_PROJECTION = new String[]{
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_ID,
            ContactsContract.PhoneLookup.NUMBER,
            ContactsContract.PhoneLookup.NORMALIZED_NUMBER,
            ContactsContract.Contacts.LOOKUP_KEY
    };

    private static final String[] PICK_PROJECTION = new String[]{
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.Data.PHOTO_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.Contacts.LOOKUP_KEY
    };
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_NAME = 1;
    private static final int COLUMN_PHOTO = 2;
    private static final int COLUMN_NUMBER = 3;
    private static final int COLUMN_NORMALIZED = 4;
    private static final int COLUMN_LOOKUP_KEY = 5;
    private static final int MENU_REPLACE = 1001;
    private static final int MENU_DELETE = 1002;

    public static final String KEY_ADD = "key_add";

    private int mItemPosition;
    private static String SPEAD_DIAL_NUMBER = "SpeedDialNumber";
    private static String SAVE_CLICKED_POS = "Clicked_pos";
    private String mInputNumber;
    private boolean mConfigChanged;

    private static final String PROPERTY_RADIO_ATEL_CARRIER = "persist.radio.atel.carrier";
    private static final String CARRIER_ONE_DEFAULT_MCC_MNC = "405854";

    private static class Record {
        long contactId;
        String name;
        String number;
        String normalizedNumber;
        String lookupKey;
        long photoId;

        Record(String number) {
            this.number = number;
            this.contactId = -1;
        }
    }

    private SparseArray<Record> mRecords;

    private int mPickNumber;
    private int mInitialPickNumber;
    private SpeedDialAdapter mAdapter;
    private AlertDialog mAddSpeedDialDialog;
    private EditText mEditNumber;

    private static final int PICK_CONTACT_RESULT = 0;

    private SubscriptionManager mSubscriptionManager;

    private boolean mEmergencyCallSpeedDial = true;
    private int mSpeedDialKeyforEmergncyCall = -1;


    /** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BtalkUiUtils.setSystemUiVisibility(getListView(), View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        mInitialPickNumber = getIntent().getIntExtra(EXTRA_INITIAL_PICK_NUMBER, -1);
        mRecords = new SparseArray<>();

        //the first item is the "1.voice mail", it never changes
        mRecords.put(1, new Record(getString(R.string.voicemail)));

        mSubscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        ListView listview = getListView();
        listview.setOnItemClickListener(this);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mAdapter = new SpeedDialAdapter();
        setListAdapter(mAdapter);

//        String property = (String) System.getProperties().get(PROPERTY_RADIO_ATEL_CARRIER);
//        mEmergencyCallSpeedDial = CARRIER_ONE_DEFAULT_MCC_MNC.equals(property);
        mSpeedDialKeyforEmergncyCall = getResources().getInteger(
                R.integer.speed_dial_emergency_number_assigned_key);

        if (savedInstanceState == null && getIntent() != null) {
            if (!TextUtils.isEmpty(getIntent().getStringExtra(KEY_ADD))) {
                int number = Integer.parseInt(getIntent().getStringExtra(KEY_ADD));
                getIntent().removeExtra(KEY_ADD);
//                if (mEmergencyCallSpeedDial && (number == mSpeedDialKeyforEmergncyCall)) {
//                    Toast.makeText(this, R.string.speed_dial_can_not_be_set,
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
                mItemPosition = number;
                final Record record = mRecords.get(number);
                if (record == null) {
                    showAddSpeedDialDialog(number);
                } else {
                    PopupMenu pm = new PopupMenu(this, getListView(), Gravity.START);
                    pm.getMenu().add(number, MENU_REPLACE, 0, R.string.speed_dial_replace);
                    pm.getMenu().add(number, MENU_DELETE, 0, R.string.speed_dial_delete);
                    pm.setOnMenuItemClickListener(this);
                    pm.show();
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAddSpeedDialDialog == null || !mAddSpeedDialDialog.isShowing()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                outState.clear();
                return;
            }
        }
        outState.putInt(SAVE_CLICKED_POS, mItemPosition);
        outState.putString(SPEAD_DIAL_NUMBER, mEditNumber.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (state.isEmpty()) {
            return;
        }
        mConfigChanged = true;
        mInputNumber = state.getString(SPEAD_DIAL_NUMBER, "");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mInputNumber = state.getString(SPEAD_DIAL_NUMBER, "");
            showAddSpeedDialDialog(state.getInt(SAVE_CLICKED_POS, mItemPosition));
        } else {
            mItemPosition = mPickNumber = state.getInt(SAVE_CLICKED_POS, mItemPosition);
            mInputNumber = state.getString(SPEAD_DIAL_NUMBER, "");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get number from shared preferences
        for (int i = 2; i <= 9; i++) {
            String phoneNumber = SpeedDialUtils.getNumber(this, i);
            Record record = null;
            if (phoneNumber != null) {
                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(phoneNumber));
                record = getRecordFromQuery(uri, LOOKUP_PROJECTION);
                if (record == null) {
                    record = new Record(phoneNumber);
                }
            }
            mRecords.put(i, record);
        }

        mAdapter.notifyDataSetChanged();

        if (mInitialPickNumber >= 2 && mInitialPickNumber <= 9) {
            pickContact(mInitialPickNumber);
            // we only want to trigger the picker once
            mInitialPickNumber = -1;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Record getRecordFromQuery(Uri uri, String[] projection) {
        Record record = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                record = new Record(cursor.getString(COLUMN_NUMBER));
                record.contactId = cursor.getLong(COLUMN_ID);
                record.photoId = cursor.getLong(COLUMN_PHOTO);
                record.name = cursor.getString(COLUMN_NAME);
                record.normalizedNumber = cursor.getString(COLUMN_NORMALIZED);
                record.lookupKey = cursor.getString(COLUMN_LOOKUP_KEY);
                if (record.normalizedNumber == null) {
                    record.normalizedNumber = record.number;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return record;
    }

    private void showAddSpeedDialDialog(final int number) {
        mPickNumber = number;
        mItemPosition = number;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.speed_dial_settings);
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.btalk_add_speed_dial_dialog, null);
        builder.setView(contentView);
        ImageButton pickContacts = (ImageButton) contentView
                .findViewById(R.id.select_contact);
        pickContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickContact(number);
                dismissDialog();
            }
        });
        mEditNumber = (EditText) contentView.findViewById(R.id.edit_container);

        // Bkav TienNAb: tu dong mo ban phim khi show dialog
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mEditNumber.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mEditNumber, InputMethodManager.SHOW_IMPLICIT);
            }
        },100);

        if (null != mRecords.get(number)) {
            mEditNumber.setText(SpeedDialUtils.getNumber(this, number));
        }
        if (mConfigChanged && !mInputNumber.isEmpty()) {
            mEditNumber.setText(mInputNumber);
            mConfigChanged = false;
            mInputNumber = "";
        }
        Button cancelButton = (Button) contentView
                .findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });
        Button mCompleteButton = (Button) contentView.findViewById(R.id.btn_complete);
        mCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditNumber.getText().toString().isEmpty()) {
                    dismissDialog();
                    return;
                }
                saveSpeedDial();
                dismissDialog();
            }
        });
        mAddSpeedDialDialog = builder.create();
        mAddSpeedDialDialog.show();
    }

    private void saveSpeedDial() {
        String number = mEditNumber.getText().toString();
        Record record = null;
        if (!TextUtils.isEmpty(number)) {
            Uri uri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(number));
            record = getRecordFromQuery(uri, LOOKUP_PROJECTION);
            if (record == null) {
                record = new Record(number);
                record.normalizedNumber = number;
            }
        }
        if (record != null) {
            SpeedDialUtils.saveNumber(this, mPickNumber,
                    record.normalizedNumber);
            mRecords.put(mPickNumber, record);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void dismissDialog() {
        if (null != mAddSpeedDialDialog && mAddSpeedDialDialog.isShowing()) {
            mAddSpeedDialDialog.dismiss();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            Intent intent = new Intent(ACTION_ADD_VOICEMAIL);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (PhoneUtils.getDefault().getActiveSubscriptionCount() > 1) {
                int sub = SubscriptionManager.getDefaultVoiceSubscriptionId();
                SubscriptionInfo subInfo = mSubscriptionManager.getActiveSubscriptionInfo(sub);
                if (subInfo != null) {
                    intent.putExtra(SUB_ID_EXTRA, subInfo.getSubscriptionId());
                    intent.putExtra(SUB_LABEL_EXTRA, subInfo.getDisplayName().toString());
                }
            }
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "Could not find voice mail setup activity");
            }
        } else {
            int number = position + 1;
//            if (mEmergencyCallSpeedDial && (number == mSpeedDialKeyforEmergncyCall)) {
//                Toast.makeText(this, R.string.speed_dial_can_not_be_set,
//                        Toast.LENGTH_SHORT).show();
//                return;
//            }
            mItemPosition = number;
            final Record record = mRecords.get(number);
            if (record == null) {
                showAddSpeedDialDialog(number);
            } else {
                PopupMenu pm = new PopupMenu(this, view, Gravity.START);
                pm.getMenu().add(number, MENU_REPLACE, 0, R.string.speed_dial_replace);
                pm.getMenu().add(number, MENU_DELETE, 0, R.string.speed_dial_delete);
                pm.setOnMenuItemClickListener(this);
                pm.show();
            }
        }
    }

    private boolean isMultiAccountAvailable() {
        TelecomManager telecomManager = getTelecomManager(this);
        return (PhoneAccountUtils.getSubscriptionPhoneAccounts(this) == null)
                && (PhoneUtils.getDefault().getActiveSubscriptionCount() > 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showSelectAccountDialog(Context context) {
        TelecomManager telecomManager = getTelecomManager(context);
        List<PhoneAccountHandle> accountsList =
                PhoneAccountUtils.getSubscriptionPhoneAccounts(context);
        ;
        final PhoneAccountHandle[] accounts = accountsList
                .toArray(new PhoneAccountHandle[accountsList.size()]);
        CharSequence[] accountEntries = new CharSequence[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            CharSequence label = telecomManager.getPhoneAccount(accounts[i])
                    .getLabel();
            accountEntries[i] = (label == null) ? null : label.toString();
        }
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.select_account_dialog_title)
                .setItems(accountEntries, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ACTION_ADD_VOICEMAIL);
                        int sub = Integer.parseInt(accounts[which].getId());
                        intent.setClassName("com.android.phone",
                                "com.android.phone.MSimCallFeaturesSubSetting");
                        intent.putExtra(SUBSCRIPTION_KEY, sub);
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Log.w(TAG, "can not find activity deal with voice mail");
                        }
                    }
                })
                .create();
        dialog.show();
    }

    private TelecomManager getTelecomManager(Context context) {
        return (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
    }


    /**goto contacts,
     used to
     set or
     replace speed
     number*/
    private void pickContact(int number) {
        mPickNumber = number;
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Anhdts
        intent.setPackage(getPackageName());
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PICK_CONTACT_RESULT) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (resultCode == RESULT_OK) {
            Record record = getRecordFromQuery(data.getData(), PICK_PROJECTION);
            if (record != null) {
                SpeedDialUtils.saveNumber(this, mPickNumber, record.normalizedNumber);
                mRecords.put(mPickNumber, record);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int number = item.getGroupId();

        switch (item.getItemId()) {
            case MENU_REPLACE:
                showAddSpeedDialDialog(number);
                return true;
            case MENU_DELETE:
                mRecords.put(number, null);
                SpeedDialUtils.saveNumber(this, number, null);
                mAdapter.notifyDataSetChanged();
                return true;
        }
        return false;
    }

    private class SpeedDialAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private ContactPhotoManager mPhotoManager;

        SpeedDialAdapter() {
            mInflater = LayoutInflater.from(BtalkSpeedDialListActivity.this);
            mPhotoManager = ContactPhotoManager.getInstance(BtalkSpeedDialListActivity.this);
        }

        @Override
        public int getCount() {
            return mRecords.size();
        }

        @Override
        public long getItemId(int position) {
            return position + 1;
        }

        @Override
        public Object getItem(int position) {
            return mRecords.get(position + 1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.speed_dial_item, parent, false);
            }

            TextView index = (TextView) convertView.findViewById(R.id.index);
            TextView name = (TextView) convertView.findViewById(R.id.name);
            TextView number = (TextView) convertView.findViewById(R.id.number);
            QuickContactBadge photo = (QuickContactBadge) convertView.findViewById(R.id.photo);
            Record record = mRecords.get(position + 1);

            index.setText(String.valueOf(position + 1));
            if (record != null && record.name != null) {
                name.setText(record.name);
                number.setText(record.number);
                number.setVisibility(View.VISIBLE);
            } else {
                name.setText(record != null ?
                        record.number : getString(R.string.speed_dial_not_set));
                number.setVisibility(View.GONE);
            }

            if (record != null && record.contactId != -1) {
                // Bkav TienNAb: sua lai logic hien thi mau cho giong voi danh ba
                ContactPhotoManager.DefaultImageRequest request = new ContactPhotoManager.DefaultImageRequest(record.name,
                        record.lookupKey, true
                        // isCircular
                );
                mPhotoManager.removePhoto(photo);
                mPhotoManager.loadThumbnail(photo, record.photoId,
                        false
                        // darkTheme
                        , true
                        // isCircular
                        , request);
                photo.assignContactUri(ContentUris.withAppendedId(
                        ContactsContract.Contacts.CONTENT_URI, record.contactId));
                photo.setVisibility(View.VISIBLE);
            } else {
                photo.setVisibility(View.GONE);
            }
            photo.setOverlay(null);

            return convertView;
        }
    }

}
