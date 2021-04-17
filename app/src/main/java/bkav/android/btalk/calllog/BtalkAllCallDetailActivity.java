package bkav.android.btalk.calllog;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telecom.PhoneAccountHandle;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.CallUtil;
import com.android.contacts.common.ClipboardUtils;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.GeoUtil;
import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.interactions.TouchPointManager;
import com.android.contacts.common.preference.ContactsPreferences;
import com.android.contacts.common.testing.NeededForTesting;
import com.android.contacts.common.util.UriUtils;
import com.android.dialer.CallDetailActivity;
import com.android.dialer.PhoneCallDetails;
import com.android.dialer.calllog.CallLogAsyncTaskUtil;
import com.android.dialer.calllog.CallLogQueryHandler;
import com.android.dialer.calllog.CallTypeHelper;
import com.android.dialer.calllog.ContactInfo;
import com.android.dialer.calllog.ContactInfoHelper;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.compat.CallsSdkCompat;
import com.android.dialer.compat.FilteredNumberCompat;
import com.android.dialer.database.FilteredNumberAsyncQueryHandler;
import com.android.dialer.filterednumber.BlockNumberDialogFragment;
import com.android.dialer.filterednumber.FilteredNumbersUtil;
import com.android.dialer.logging.InteractionEvent;
import com.android.dialer.logging.Logger;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.PhoneNumberUtil;
import com.android.dialer.util.TelecomUtil;
import com.android.messaging.ui.UIIntents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.bmsblocked.BmsUtils;
import bkav.android.btalk.calllog.dialer.BtalkCallDetailHistoryAdapter;
import bkav.android.btalk.calllog.recoder.RecorderService;
import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;

/**
 * Created by anhdt on 14/07/2017.
 *
 */

public class BtalkAllCallDetailActivity extends AppCompatActivity
        implements MenuItem.OnMenuItemClickListener, View.OnClickListener,
        BlockNumberDialogFragment.Callback {
    private static final String TAG = CallDetailActivity.class.getSimpleName();
    /** A long array extra containing ids of call log entries to display. */
    public static final String EXTRA_CALL_LOG_IDS = "EXTRA_CALL_LOG_IDS";
    /** A long array extra containing ids of call log entries to display. */
    public static final String EXTRA_NUMBER = "EXTRA_NUMBER";
    /** If we are started with a voicemail, we'll find the uri to play with this extra. */
    public static final String EXTRA_VOICEMAIL_URI = "EXTRA_VOICEMAIL_URI";
    /** If the activity was triggered from a notification. */
    public static final String EXTRA_FROM_NOTIFICATION = "EXTRA_FROM_NOTIFICATION";

    /** Anhdts type show all or show list*/
    public static final String EXTRA_TYPE = "EXTRA_TYPE";

    public static final int EXTRA_TYPE_SHOW_ALL = 0;

    public static final int EXTRA_TYPE_SHOW_LIST = 1;

    // Bkav HuyNQN them adapter
    private BtalkCallDetailHistoryAdapter mAdapter;

    public void onGetCallDetails(PhoneCallDetails[] details) {
        if (details == null) {
            // Somewhere went wrong: we're going to bail out and show error to users.
            Toast.makeText(mContext, R.string.toast_call_detail_error,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDetails = details[0];

        // Truong hop truyen listId thi phai get lai so
        if (mType == EXTRA_TYPE_SHOW_LIST) {
            mNumber = TextUtils.isEmpty(mDetails.number) ? null : mDetails.number.toString();
        }

        mPostDialDigits = TextUtils.isEmpty(mDetails.postDialDigits)
                ? "" : mDetails.postDialDigits;
        mDisplayNumber = mDetails.displayNumber;

        final CharSequence callLocationOrType = getNumberTypeOrLocation(mDetails);

        final CharSequence displayNumber;
        if (!TextUtils.isEmpty(mDetails.postDialDigits)) {
            displayNumber = mDetails.number + mDetails.postDialDigits;
        } else {
            displayNumber = mDetails.displayNumber;
        }

        final String displayNumberStr = mBidiFormatter.unicodeWrap(
                displayNumber.toString(), TextDirectionHeuristics.LTR);

        mDetails.nameDisplayOrder = mContactsPreferences.getDisplayOrder();

        if (!TextUtils.isEmpty(mDetails.getPreferredName())) {
            mCallerName.setText(mDetails.getPreferredName());
            mCallerNumber.setText(callLocationOrType + " " + displayNumberStr);
        } else {
            mCallerName.setText(displayNumberStr);
            if (!TextUtils.isEmpty(callLocationOrType)) {
                mCallerNumber.setText(callLocationOrType);
                mCallerNumber.setVisibility(View.VISIBLE);
            } else {
                mCallerNumber.setVisibility(View.GONE);
            }
        }

        CharSequence accountLabel = PhoneAccountUtils.getAccountLabel(mContext,
                mDetails.accountHandle);
        CharSequence accountContentDescription =
                PhoneCallDetails.createAccountLabelDescription(mResources, mDetails.viaNumber,
                        accountLabel);
        if (!TextUtils.isEmpty(mDetails.viaNumber)) {
            if (!TextUtils.isEmpty(accountLabel)) {
                accountLabel = mResources.getString(R.string.call_log_via_number_phone_account,
                        accountLabel, mDetails.viaNumber);
            } else {
                accountLabel = mResources.getString(R.string.call_log_via_number,
                        mDetails.viaNumber);
            }
        }
        if (!TextUtils.isEmpty(accountLabel)) {
            mAccountLabel.setText(accountLabel);
            mAccountLabel.setContentDescription(accountContentDescription);
            mAccountLabel.setVisibility(View.VISIBLE);
        } else {
            mAccountLabel.setVisibility(View.GONE);
        }

        final boolean canPlaceCallsTo =
                PhoneNumberUtil.canPlaceCallsTo(mNumber, mDetails.numberPresentation);
        mCallButton.setVisibility(canPlaceCallsTo ? View.VISIBLE : View.GONE);
        mCopyNumberActionItem.setVisibility(canPlaceCallsTo ? View.VISIBLE : View.GONE);

        updateBlockActionItemVisibility(canPlaceCallsTo ? View.VISIBLE : View.GONE);

        final boolean isSipNumber = PhoneNumberUtil.isSipNumber(mNumber);
        final boolean isVoicemailNumber =
                PhoneNumberUtil.isVoicemailNumber(mContext, mDetails.accountHandle, mNumber);
        final boolean showEditNumberBeforeCallAction =
                canPlaceCallsTo && !isSipNumber && !isVoicemailNumber;
        mEditBeforeCallActionItem.setVisibility(
                showEditNumberBeforeCallAction ? View.VISIBLE : View.GONE);

        final boolean showReportAction = mContactInfoHelper.canReportAsInvalid(
                mDetails.sourceType, mDetails.objectId);
        mReportActionItem.setVisibility(
                showReportAction ? View.VISIBLE : View.GONE);

        invalidateOptionsMenu();

        // Bkav HuyNQN su dung bien mAdapter toan cuc
        mAdapter=new BtalkCallDetailHistoryAdapter(mContext,mInflater,mCallTypeHelper,details);
        mHistoryList.setAdapter(mAdapter);

        updateFilteredNumberChanges();
        updateContactPhoto();

        findViewById(R.id.call_detail).setVisibility(View.VISIBLE);

        // Bkav HuyNQN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPlayIntent = new Intent(getApplicationContext(), RecorderService.class);
            bindService(mPlayIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            startService(mPlayIntent);
        }
    }

    /**
     * Determines the location geocode text for a call, or the phone number type
     * (if available).
     *
     * @param details The call details.
     * @return The phone number type or location.
     */
    private CharSequence getNumberTypeOrLocation(PhoneCallDetails details) {
        if (!TextUtils.isEmpty(details.namePrimary)) {
            return ContactsContract.CommonDataKinds.Phone.getTypeLabel(mResources, details.numberType,
                    details.numberLabel);
        } else {
            return details.geocode;
        }
    }

    private int mType;
    private long[] mCallIds;
    private Context mContext;
    private ContactInfoHelper mContactInfoHelper;
    private ContactsPreferences mContactsPreferences;
    private CallTypeHelper mCallTypeHelper;
    private ContactPhotoManager mContactPhotoManager;
    private FilteredNumberAsyncQueryHandler mFilteredNumberAsyncQueryHandler;
    private BidiFormatter mBidiFormatter = BidiFormatter.getInstance();
    private LayoutInflater mInflater;
    private Resources mResources;

    private PhoneCallDetails mDetails;
    protected String mNumber;
    private Uri mVoicemailUri;
    private String mPostDialDigits = "";
    private String mDisplayNumber;

    private ListView mHistoryList;
    private QuickContactBadge mQuickContactBadge;
    private TextView mCallerName;
    private TextView mCallerNumber;
    private TextView mAccountLabel;
    private View mCallButton;

    private TextView mBlockNumberActionItem;
    private View mEditBeforeCallActionItem;
    private View mReportActionItem;
    private View mCopyNumberActionItem;

    private Integer mBlockedNumberId;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mType = getIntent().getIntExtra(EXTRA_TYPE, EXTRA_TYPE_SHOW_ALL);
        if (mType == EXTRA_TYPE_SHOW_ALL) {
            mNumber = getIntent().getStringExtra(EXTRA_NUMBER);
        } else {
            setTitle(getString(R.string.callDetailTitle));
            mCallIds = getIntent().getLongArrayExtra(EXTRA_CALL_LOG_IDS);
        }
        mContext = this;
        mResources = getResources();
        mContactInfoHelper = new ContactInfoHelper(this, GeoUtil.getCurrentCountryIso(this));
        mContactsPreferences = new ContactsPreferences(mContext);
        mCallTypeHelper = new CallTypeHelper(getResources());
        mFilteredNumberAsyncQueryHandler =
                new FilteredNumberAsyncQueryHandler(getContentResolver());

        mVoicemailUri = getIntent().getParcelableExtra(EXTRA_VOICEMAIL_URI);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.call_detail);
        mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        mHistoryList = (ListView) findViewById(R.id.history);
        mHistoryList.addHeaderView(mInflater.inflate(R.layout.call_detail_header, null));
        mHistoryList.addFooterView(
                mInflater.inflate(R.layout.call_detail_footer, null), null, false);

        mQuickContactBadge = (QuickContactBadge) findViewById(R.id.quick_contact_photo);
        mQuickContactBadge.setOverlay(null);
        if (CompatUtils.hasPrioritizedMimeType()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mQuickContactBadge.setPrioritizedMimeType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            }
        }
        mCallerName = (TextView) findViewById(R.id.caller_name);
        mCallerNumber = (TextView) findViewById(R.id.caller_number);
        mAccountLabel = (TextView) findViewById(R.id.phone_account_label);
        mContactPhotoManager = ContactPhotoManager.getInstance(this);

        mCallButton = findViewById(R.id.call_back_button);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mNumber)) {
                    return;
                }
                /*Intent dialIntent = new IntentUtil.CallIntentBuilder(getDialableNumber())
                        .setCallInitiationType(Call.LogState.INITIATION_CALL_DETAILS).build();
                if (DialerUtils.isConferenceURICallLog(mNumber, mPostDialDigits)) {
                    dialIntent.putExtra("org.codeaurora.extra.DIAL_CONFERENCE_URI", true);
                }
                mContext.startActivity(dialIntent);*/

                // Bkav HuyNQN su dung logic chon sim moi
                UIIntents.get().makeACall(mContext, getFragmentManager(), mNumber);
            }
        });


        mBlockNumberActionItem = (TextView) findViewById(R.id.call_detail_action_block);
        updateBlockActionItemVisibility(View.VISIBLE);
        mBlockNumberActionItem.setOnClickListener(this);
        mEditBeforeCallActionItem = findViewById(R.id.call_detail_action_edit_before_call);
        mEditBeforeCallActionItem.setOnClickListener(this);
        mReportActionItem = findViewById(R.id.call_detail_action_report);
        mReportActionItem.setOnClickListener(this);

        mCopyNumberActionItem = findViewById(R.id.call_detail_action_copy);
        mCopyNumberActionItem.setOnClickListener(this);

        if (getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)) {
            closeSystemDialogs();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            int flags = findViewById(android.R.id.content).getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            findViewById(android.R.id.content).setSystemUiVisibility(flags);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BmsUtils.KEY_LOCKED);
        intentFilter.addAction(BmsUtils.KEY_UNLOCKED);
        registerReceiver(mReceiverCallBlocked, intentFilter);
    }

    private void updateBlockActionItemVisibility(int visibility) {
        if (!FilteredNumberCompat.canAttemptBlockOperations(mContext)) {
            visibility = View.GONE;
        }
        mBlockNumberActionItem.setVisibility(visibility);
    }

    @Override
    public void onResume() {
        super.onResume();
        mContactsPreferences.refreshValue(ContactsPreferences.DISPLAY_ORDER_KEY);
        getCallDetails(mNumber);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Bkav HuyNQN khi roi vao trang thai pause thi mediaplayer se stop lai va reset
        if(mService != null)
        mService.stopMediaPlayer();
    }

    // Bkav HuyNQN unbindService
    @Override
    protected void onStop() {
        super.onStop();
        // Bkav TienNAb: Fix loi crash app
        // Bkav HienDTk: them dieu kien khi unbindService de tranh truong hop chua bind xong ma da unbind => BOS-2519
        if(mIsBound && mService != null && mService.checkServiceBound()) {
            // Bkav HuyNQN kiem tra neu service ton tai moi unbind
            mService.onStopSelf();
            unbindService(mServiceConnection);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            TouchPointManager.getInstance().setPoint((int) ev.getRawX(), (int) ev.getRawY());
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Anhdts query cac cuoc goi theo so do
     * neu number khac null thi query tat ca, khong thi query theo list id
     */
    private class AsyncTaskGetAllCallDetail extends AsyncTask<String, Void, PhoneCallDetails[]> {

        private Context mContext;

        AsyncTaskGetAllCallDetail(Context context) {
            mContext = context;
        }

        @Override
        protected PhoneCallDetails[] doInBackground(String... params) {
            Cursor cursor;
            if (mType == EXTRA_TYPE_SHOW_LIST) {
                final StringBuilder selectionIds = new StringBuilder();
                for (long callId : mCallIds) {
                    if (selectionIds.length() != 0) {
                        selectionIds.append(",");
                    }
                    selectionIds.append(callId);
                }
                cursor = mContext.getContentResolver().query(TelecomUtil.getCallLogUri(mContext), CallDetailQuery.CALL_LOG_PROJECTION,
                        CallLog.Calls._ID + " IN (" + selectionIds.toString() + ") AND " + CallLog.Calls.TYPE + " != " + CallLog.Calls.VOICEMAIL_TYPE,
                        null, CallLog.Calls._ID + " desc");
            } else {
                String query = params[0];
                if (query.length() > 9) {
                    query = "%" + query.substring(query.length() - 9);
                }
                cursor = mContext.getContentResolver().query(TelecomUtil.getCallLogUri(mContext), CallDetailQuery.CALL_LOG_PROJECTION,
                        CallLog.Calls.NUMBER + " like ?" + " AND " + CallLog.Calls.TYPE + " != " + CallLog.Calls.VOICEMAIL_TYPE,
                        new String[]{query}, CallLog.Calls._ID + " desc");
            }
            if (cursor != null) {
                if (cursor.getCount() == 0) {
                    cursor.close();
                    return new PhoneCallDetails[0];
                }
                PhoneCallDetails[] listDetail = new PhoneCallDetails[cursor.getCount()];
                cursor.moveToFirst();
                int i = 0;
                do {
                    // Read call log.
                    if (i == 0) {
                        listDetail[i] = getFirstCallDetailElement(cursor);
                    } else {
                        listDetail[i] = getCallDetailElement(cursor);
                    }
                    i++;
                } while (cursor.moveToNext());
                cursor.close();
                return listDetail;
            }
            return new PhoneCallDetails[0];
        }

        @Override
        public void onPostExecute(PhoneCallDetails[] phoneCallDetails) {
            onGetCallDetails(phoneCallDetails);
        }

        private PhoneCallDetails getCallDetailElement(Cursor cursor) {
            PhoneCallDetails details = new PhoneCallDetails();
            details.callTypes = new int[]{
                    cursor.getInt(CallDetailQuery.CALL_TYPE_COLUMN_INDEX)
            };
            details.date = cursor.getLong(CallDetailQuery.DATE_COLUMN_INDEX);
            details.duration = cursor.getLong(CallDetailQuery.DURATION_COLUMN_INDEX);

            // Bkav HuyNQN kiem tra xem cuoc goi co duoc ghi am hay khong
            if(cursor.getString(cursor.getColumnIndex(CallLogQueryHandler.RECORD_CALL_DATA))!=null && !cursor.getString(cursor.getColumnIndex(CallLogQueryHandler.RECORD_CALL_DATA)).equals("")){
                details.isRecorder=true;
                details.mPathRecorder =cursor.getString(cursor.getColumnIndex(CallLogQueryHandler.RECORD_CALL_DATA));
            }else {
                details.isRecorder=false;
            }
            return details;
        }

        private PhoneCallDetails getFirstCallDetailElement(Cursor cursor) {
            final String countryIso = cursor.getString(CallDetailQuery.COUNTRY_ISO_COLUMN_INDEX);
            final String number = cursor.getString(CallDetailQuery.NUMBER_COLUMN_INDEX);
            final String postDialDigits = CompatUtils.isNCompatible()
                    ? cursor.getString(CallDetailQuery.POST_DIAL_DIGITS) : "";
            final String viaNumber = CompatUtils.isNCompatible() ?
                    cursor.getString(CallDetailQuery.VIA_NUMBER) : "";
            final int numberPresentation =
                    cursor.getInt(CallDetailQuery.NUMBER_PRESENTATION_COLUMN_INDEX);

            final PhoneAccountHandle accountHandle = PhoneAccountUtils.getAccount(
                    cursor.getString(CallDetailQuery.ACCOUNT_COMPONENT_NAME),
                    cursor.getString(CallDetailQuery.ACCOUNT_ID));

            // If this is not a regular number, there is no point in looking it up in the contacts.
            ContactInfoHelper contactInfoHelper =
                    new ContactInfoHelper(mContext, GeoUtil.getCurrentCountryIso(mContext));
            boolean isVoicemail = PhoneNumberUtil.isVoicemailNumber(mContext, accountHandle, number);
            boolean shouldLookupNumber =
                    PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation) && !isVoicemail;
            ContactInfo info = ContactInfo.EMPTY;
            Pattern pattern = Pattern.compile("[,;]");
            String[] num = pattern.split(number);
            boolean isConf = num != null && num.length > 1
                    && DialerUtils.isConferenceURICallLog(number, postDialDigits);
            String phoneNumber = num != null && num.length > 0 ? num[0] : "";
            String queryNumber = isConf ? number : phoneNumber;
            if (shouldLookupNumber) {
                ContactInfo lookupInfo = contactInfoHelper.lookupNumber(queryNumber, postDialDigits,
                        countryIso, isConf);
                info = lookupInfo != null ? lookupInfo : ContactInfo.EMPTY;
            }

            PhoneCallDetails details = new PhoneCallDetails(
                    mContext, queryNumber, numberPresentation, info.formattedNumber,
                    postDialDigits, isVoicemail);
            details.viaNumber = viaNumber;
            details.accountHandle = accountHandle;
            details.contactUri = info.lookupUri;
            details.namePrimary = info.name;
            details.nameAlternative = info.nameAlternative;
            details.numberType = info.type;
            details.numberLabel = info.label;
            details.photoUri = info.photoUri;
            details.sourceType = info.sourceType;
            details.objectId = info.objectId;

            details.callTypes = new int[]{
                    cursor.getInt(CallDetailQuery.CALL_TYPE_COLUMN_INDEX)
            };
            details.date = cursor.getLong(CallDetailQuery.DATE_COLUMN_INDEX);
            details.duration = cursor.getLong(CallDetailQuery.DURATION_COLUMN_INDEX);
            details.features = cursor.getInt(CallDetailQuery.FEATURES);
            details.geocode = cursor.getString(CallDetailQuery.GEOCODED_LOCATION_COLUMN_INDEX);
            details.transcription = cursor.getString(CallDetailQuery.TRANSCRIPTION_COLUMN_INDEX);

            details.countryIso = !TextUtils.isEmpty(countryIso) ? countryIso
                    : GeoUtil.getCurrentCountryIso(mContext);

            if (!cursor.isNull(CallDetailQuery.DATA_USAGE)) {
                details.dataUsage = cursor.getLong(CallDetailQuery.DATA_USAGE);
            }

            // Bkav HuyNQN kiem tra xem cuoc goi co duoc ghi am hay khong
            if(cursor.getString(cursor.getColumnIndex(CallLogQueryHandler.RECORD_CALL_DATA))!=null && !cursor.getString(cursor.getColumnIndex(CallLogQueryHandler.RECORD_CALL_DATA)).equals("")){
                details.isRecorder=true;
                details.mPathRecorder =cursor.getString(cursor.getColumnIndex(CallLogQueryHandler.RECORD_CALL_DATA));
            }else {
                details.isRecorder=false;
            }

            return details;
        }
    }

    public void getCallDetails(String number) {
        AsyncTaskGetAllCallDetail asyncTask = new AsyncTaskGetAllCallDetail(getApplicationContext());
        if (asyncTask.getStatus() != AsyncTask.Status.RUNNING) {
            asyncTask.execute(number);
        }
    }

    private static String[] SELECTION =
            {CallLog.Calls._ID};

    /**
     * Anhdts list id
     */
    private long[] getCallLogId() {
        Cursor cursor;
        if (mType == EXTRA_TYPE_SHOW_LIST) {
            final StringBuilder selectionIds = new StringBuilder();
            for (long callId : mCallIds) {
                if (selectionIds.length() != 0) {
                    selectionIds.append(",");
                }
                selectionIds.append(callId);
            }
            cursor = mContext.getContentResolver().query(TelecomUtil.getCallLogUri(mContext), SELECTION,
                    CallLog.Calls._ID + " IN (" + selectionIds.toString() + ") AND " + CallLog.Calls.TYPE + " != " + CallLog.Calls.VOICEMAIL_TYPE,
                    null, CallLog.Calls._ID + " desc");
        } else {
            cursor = mContext.getContentResolver().query(TelecomUtil.getCallLogUri(mContext), SELECTION,
                    CallLog.Calls.NUMBER + " = ?" + " AND " + CallLog.Calls.TYPE + " != " + CallLog.Calls.VOICEMAIL_TYPE,
                    new String[]{mNumber}, CallLog.Calls._ID + " desc");
        }
        if (cursor != null) {
            if (cursor.getCount() == 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();
            long[] listCallId = new long[cursor.getCount()];
            int i = 0;
            do {
                listCallId[i] = cursor.getLong(0);
                i++;
            } while (cursor.moveToNext());
            cursor.close();
            return listCallId;
        }
        return null;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuItem deleteMenuItem = menu.add(
                Menu.NONE,
                R.id.call_detail_delete_menu_item,
                Menu.NONE,
                R.string.call_details_delete);
        deleteMenuItem.setIcon(getDrawable());
        deleteMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        deleteMenuItem.setOnMenuItemClickListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.call_detail_delete_menu_item) {
            if (hasVoicemail()) {
                CallLogAsyncTaskUtil.deleteVoicemail(
                        this, mVoicemailUri, mCallLogAsyncTaskListener);
            } else {
                final StringBuilder callIds = new StringBuilder();
                long[] rowIds = getCallLogId();
                if (rowIds == null) {
                    return false;
                }
                for (long id : rowIds) {
                    if (callIds.length() != 0) {
                        callIds.append(",");
                    }
                    callIds.append(id);
                }
                BtalkDialerDatabaseHelper.getInstance(mContext).removeCallLog(callIds);
                CallLogAsyncTaskUtil.deleteCalls(
                        this, callIds.toString(), mCallLogAsyncTaskListener, getCallLogId().length);
            }
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        int resId = view.getId();
        if (resId == R.id.call_detail_action_block) {
            // Bkav HuyNQN su dung logic chan so bang bms
            if(BmsUtils.isHasNumberBlocks(BtalkAllCallDetailActivity.this, mNumber)){
                BmsUtils.showDialogUnblocked(BtalkAllCallDetailActivity.this, mNumber);
            }else {
                BmsUtils.showDialogAddCallLogBlocked(BtalkAllCallDetailActivity.this, mNumber);
            }
//            FilteredNumberCompat
//                    .showBlockNumberDialogFlow(mContext.getContentResolver(), mBlockedNumberId,
//                            mNumber, mDetails.countryIso, mDisplayNumber, R.id.call_detail,
//                            getFragmentManager(), this);
        } else if (resId == R.id.call_detail_action_copy) {
            // Bkav TienNAb: sua lai noi dung toast khi copy so dien thoai
            ClipboardUtils.copyText(mContext, null, mNumber, false);
            Toast.makeText(mContext, R.string.string_copy_number_contact, Toast.LENGTH_SHORT).show();
        } else if (resId == R.id.call_detail_action_edit_before_call) {
            // Anhdts su dung ham cua bkav
            if (useDefaultMethod(getDialableNumber())) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL,
                        CallUtil.getCallUri(getDialableNumber()));
                DialerUtils.startActivityWithErrorToast(mContext, dialIntent);
            }
        } else {
            Log.wtf(TAG, "Unexpected onClick event from " + view);
        }
    }

    @Override
    public void onFilterNumberSuccess() {
        Logger.logInteraction(InteractionEvent.BLOCK_NUMBER_CALL_DETAIL);
        updateFilteredNumberChanges();
    }

    @Override
    public void onUnfilterNumberSuccess() {
        Logger.logInteraction(InteractionEvent.UNBLOCK_NUMBER_CALL_DETAIL);
        updateFilteredNumberChanges();
    }

    @Override
    public void onChangeFilteredNumberUndo() {
        updateFilteredNumberChanges();
    }

    private void updateFilteredNumberChanges() {
        if (mDetails == null ||
                !FilteredNumbersUtil.canBlockNumber(this, mNumber, mDetails.countryIso)) {
            return;
        }

        final boolean success = mFilteredNumberAsyncQueryHandler.isBlockedNumber(
                new FilteredNumberAsyncQueryHandler.OnCheckBlockedListener() {
                    @Override
                    public void onCheckComplete(Integer id) {
                        mBlockedNumberId = id;
                        updateBlockActionItem();
                    }
                }, mNumber, mDetails.countryIso);

        if (!success) {
            updateBlockActionItem();
        }
    }

    // Loads and displays the contact photo.
    private void updateContactPhoto() {
        if (mDetails == null) {
            return;
        }

        final boolean isVoiceMailNumber =
                PhoneNumberUtil.isVoicemailNumber(mContext, mDetails.accountHandle, mNumber);
        final boolean isBusiness = mContactInfoHelper.isBusiness(mDetails.sourceType);
        int contactType = ContactPhotoManager.TYPE_DEFAULT;
        if (isVoiceMailNumber) {
            contactType = ContactPhotoManager.TYPE_VOICEMAIL;
        } else if (isBusiness) {
            contactType = ContactPhotoManager.TYPE_BUSINESS;
        }

        final String displayName = TextUtils.isEmpty(mDetails.namePrimary)
                ? mDetails.displayNumber : mDetails.namePrimary.toString();
        final String lookupKey = mDetails.contactUri == null
                ? null : UriUtils.getLookupKeyFromUri(mDetails.contactUri);

        final ContactPhotoManager.DefaultImageRequest request =
                new ContactPhotoManager.DefaultImageRequest(displayName, lookupKey, contactType, true /* isCircular */);

        mQuickContactBadge.assignContactUri(mDetails.contactUri);
        mQuickContactBadge.setContentDescription(
                mResources.getString(R.string.description_contact_details, displayName));

        mContactPhotoManager.loadDirectoryPhoto(mQuickContactBadge, mDetails.photoUri,
                false /* darkTheme */, true /* isCircular */, request);
    }

    private void updateBlockActionItem() {
        // Bkav HuyNQN khong su dung logic chan so nay nua ma dung cua BMS
        /*if (mBlockedNumberId == null) {
            mBlockNumberActionItem.setText(R.string.action_block_number);
            mBlockNumberActionItem.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_call_detail_block, 0, 0, 0);
        } else {
            mBlockNumberActionItem.setText(R.string.action_unblock_number);
            mBlockNumberActionItem.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_call_detail_unblock, 0, 0, 0);
        }*/

        if(BmsUtils.isHasNumberBlocks(getApplicationContext(), mNumber)){
            mBlockNumberActionItem.setText(R.string.action_unblock_number);
            mBlockNumberActionItem.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_call_detail_unblock, 0, 0, 0);
        }else {
            mBlockNumberActionItem.setText(R.string.action_block_number);
            mBlockNumberActionItem.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_call_detail_block, 0, 0, 0);
        }
    }

    private void closeSystemDialogs() {
        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    private String getDialableNumber() {
        return mNumber + mPostDialDigits;
    }

    @NeededForTesting
    public boolean hasVoicemail() {
        return mVoicemailUri != null;
    }

    /**
     * Anhdts su dung ham chinh sua so truoc khi goi
     */
    protected boolean useDefaultMethod(String number) {
        Bundle data = new Bundle();
        data.putString(BtalkActivity.ARGUMENT_NUMBER, number);
        Intent intent = new Intent(BtalkActivity.ACTION_FIX_BEFORE_CALL);
        intent.putExtras(data);
        startActivity(intent);
        return false;
    }

    /**
     * Anhdts
     */
    protected Drawable getDrawable() {
        // Bkav HienDTk: thay icon delete
        Drawable iconDelete = ContextCompat.getDrawable(this, R.drawable.btalk_ic_action_mode_delete_new).mutate();
        iconDelete.setColorFilter(ContextCompat.getColor(this, R.color.btalk_ab_text_and_icon_normal_color), PorterDuff.Mode.MULTIPLY);
        return iconDelete;
    }

    /**
     * Anhdts callback finish sau khi delete
     */
    private CallLogAsyncTaskUtil.CallLogAsyncTaskListener mCallLogAsyncTaskListener = new CallLogAsyncTaskUtil.CallLogAsyncTaskListener() {
        @Override
        public void onDeleteCall() {
            finish();
        }

        @Override
        public void onDeleteVoicemail() {
            finish();
        }

        @Override
        public void onGetCallDetails(PhoneCallDetails[] details) {
        }
    };

    // Bkav HuyNQN Tao serviceConnection
    private RecorderService mService;
    private Intent mPlayIntent;
    private boolean mIsBound;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.RecorderBinder recorderBinder = (RecorderService.RecorderBinder) service;
            mService = recorderBinder.getService();
            mAdapter.setService(mService);
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBound = false;
        }
    };

    /**
     * Anhdts du lieu query CallLog
     */
    private static final class CallDetailQuery {

        private static final String[] CALL_LOG_PROJECTION_INTERNAL = new String[]{
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.COUNTRY_ISO,
                CallLog.Calls.GEOCODED_LOCATION,
                CallLog.Calls.NUMBER_PRESENTATION,
                CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME,
                CallLog.Calls.PHONE_ACCOUNT_ID,
                CallLog.Calls.FEATURES,
                CallLog.Calls.DATA_USAGE,
                CallLog.Calls.TRANSCRIPTION,

        };
        static final String[] CALL_LOG_PROJECTION;

        static final int DATE_COLUMN_INDEX = 0;
        static final int DURATION_COLUMN_INDEX = 1;
        static final int NUMBER_COLUMN_INDEX = 2;
        static final int CALL_TYPE_COLUMN_INDEX = 3;
        static final int COUNTRY_ISO_COLUMN_INDEX = 4;
        static final int GEOCODED_LOCATION_COLUMN_INDEX = 5;
        static final int NUMBER_PRESENTATION_COLUMN_INDEX = 6;
        static final int ACCOUNT_COMPONENT_NAME = 7;
        static final int ACCOUNT_ID = 8;
        static final int FEATURES = 9;
        static final int DATA_USAGE = 10;
        static final int TRANSCRIPTION_COLUMN_INDEX = 11;
        static final int POST_DIAL_DIGITS = 12;
        static final int VIA_NUMBER = 13;

        static {
            ArrayList<String> projectionList = new ArrayList<>();
            projectionList.addAll(Arrays.asList(CALL_LOG_PROJECTION_INTERNAL));
            if (CompatUtils.isNCompatible()) {
                projectionList.add(CallsSdkCompat.POST_DIAL_DIGITS);
                projectionList.add(CallsSdkCompat.VIA_NUMBER);
                // Bkav HuyNQN Add RECORD_CALL_DATA
                projectionList.add(CallLogQueryHandler.RECORD_CALL_DATA);
            }
            projectionList.trimToSize();
            CALL_LOG_PROJECTION = projectionList.toArray(new String[projectionList.size()]);
        }
    }

    private BroadcastReceiver mReceiverCallBlocked = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BmsUtils.KEY_LOCKED) || action.equals(BmsUtils.KEY_UNLOCKED)){
                updateBlockActionItem();
            }
        }
    };
}
