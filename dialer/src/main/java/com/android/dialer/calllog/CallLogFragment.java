/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.dialer.calllog;

import android.app.Activity;
import android.app.Fragment;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.contacts.common.GeoUtil;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.dialer.R;
import com.android.dialer.list.ListsFragment;
import com.android.dialer.util.EmptyLoader;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.android.dialer.widget.EmptyContentView;
import com.android.dialer.widget.EmptyContentView.OnEmptyViewActionButtonClickedListener;
import com.android.dialerbind.ObjectFactory;

import static android.Manifest.permission.READ_CALL_LOG;

/**
 * Displays a list of call log entries. To filter for a particular kind of call
 * (all, missed or voicemails), specify it in the constructor.
 */
public class CallLogFragment extends Fragment implements CallLogQueryHandler.Listener,
        CallLogAdapter.CallFetcher, OnEmptyViewActionButtonClickedListener,
        FragmentCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "CallLogFragment";

    /**
     * ID of the empty loader to defer other fragments.
     */
    private static final int EMPTY_LOADER_ID = 0;

    private static final String KEY_FILTER_TYPE = "filter_type";
    private static final String KEY_LOG_LIMIT = "log_limit";
    private static final String KEY_DATE_LIMIT = "date_limit";
    private static final String KEY_IS_CALL_LOG_ACTIVITY = "is_call_log_activity";

    // No limit specified for the number of logs to show; use the CallLogQueryHandler's default.
    private static final int NO_LOG_LIMIT = -1;
    // No date-based filtering.
    private static final int NO_DATE_LIMIT = 0;

    private static final int READ_CALL_LOG_PERMISSION_REQUEST_CODE = 1;

    private static final int EVENT_UPDATE_DISPLAY = 1;

    private static final long MILLIS_IN_MINUTE = 60 * 1000;

    protected RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    protected CallLogAdapter mAdapter;
    protected CallLogQueryHandler mCallLogQueryHandler;
    private boolean mScrollToTop;


    protected EmptyContentView mEmptyListView;
    private KeyguardManager mKeyguardManager;

    private boolean mEmptyLoaderRunning;
    private boolean mCallLogFetched;
    private boolean mVoicemailStatusFetched;

    private final Handler mDisplayUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_UPDATE_DISPLAY:
                    refreshData();
                    rescheduleDisplayUpdate();
                    break;
            }
        }
    };

    private final Handler mHandler = new Handler();

    protected class CustomContentObserver extends ContentObserver {
        public CustomContentObserver() {
            super(mHandler);
        }
        @Override
        public void onChange(boolean selfChange) {
            mRefreshDataRequired = true;
        }
    }

    // See issue 6363009
    private final ContentObserver mCallLogObserver = new CustomContentObserver();
    private final ContentObserver mContactsObserver = new CustomContentObserver();
    protected boolean mRefreshDataRequired = true;

    private boolean mHasReadCallLogPermission = false;

    // Exactly same variable is in Fragment as a package private.
    private boolean mMenuVisible = true;

    // Default to all calls.
    protected int mCallTypeFilter = CallLogQueryHandler.CALL_TYPE_ALL;

    // Log limit - if no limit is specified, then the default in {@link CallLogQueryHandler}
    // will be used.
    private int mLogLimit = NO_LOG_LIMIT;

    // Date limit (in millis since epoch) - when non-zero, only calls which occurred on or after
    // the date filter are included.  If zero, no date-based filtering occurs.
    protected long mDateLimit = NO_DATE_LIMIT;

    /*
     * True if this instance of the CallLogFragment shown in the CallLogActivity.
     */
    private boolean mIsCallLogActivity = false;

    public interface HostInterface {
        public void showDialpad();
    }

    public CallLogFragment() {
        this(CallLogQueryHandler.CALL_TYPE_ALL, NO_LOG_LIMIT);
    }

    public CallLogFragment(int filterType) {
        this(filterType, NO_LOG_LIMIT);
    }

    public CallLogFragment(int filterType, boolean isCallLogActivity) {
        this(filterType, NO_LOG_LIMIT);
        mIsCallLogActivity = isCallLogActivity;
    }

    public CallLogFragment(int filterType, int logLimit) {
        this(filterType, logLimit, NO_DATE_LIMIT);
    }

    /**
     * Creates a call log fragment, filtering to include only calls of the desired type, occurring
     * after the specified date.
     * @param filterType type of calls to include.
     * @param dateLimit limits results to calls occurring on or after the specified date.
     */
    public CallLogFragment(int filterType, long dateLimit) {
        this(filterType, NO_LOG_LIMIT, dateLimit);
    }

    /**
     * Creates a call log fragment, filtering to include only calls of the desired type, occurring
     * after the specified date.  Also provides a means to limit the number of results returned.
     * @param filterType type of calls to include.
     * @param logLimit limits the number of results to return.
     * @param dateLimit limits results to calls occurring on or after the specified date.
     */
    public CallLogFragment(int filterType, int logLimit, long dateLimit) {
        mCallTypeFilter = filterType;
        mLogLimit = logLimit;
        mDateLimit = dateLimit;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) {
            mCallTypeFilter = state.getInt(KEY_FILTER_TYPE, mCallTypeFilter);
            mLogLimit = state.getInt(KEY_LOG_LIMIT, mLogLimit);
            mDateLimit = state.getLong(KEY_DATE_LIMIT, mDateLimit);
            mIsCallLogActivity = state.getBoolean(KEY_IS_CALL_LOG_ACTIVITY, mIsCallLogActivity);
        }

        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();
        String currentCountryIso = GeoUtil.getCurrentCountryIso(activity);
        mCallLogQueryHandler = new CallLogQueryHandler(activity, resolver, this, mLogLimit);
        mKeyguardManager =
                (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
        resolver.registerContentObserver(CallLog.CONTENT_URI, true, mCallLogObserver);
        resolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true,
                mContactsObserver);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    /** Called by the CallLogQueryHandler when the list of calls has been fetched or updated. */
    @Override
    public boolean onCallsFetched(Cursor cursor) {
        if (getActivity() == null || getActivity().isFinishing()) {
            // Return false; we did not take ownership of the cursor
            return false;
        }
        mAdapter.invalidatePositions();
        mAdapter.setLoading(false);
        mAdapter.changeCursor(cursor);
        // This will update the state of the "Clear call log" menu item.
        getActivity().invalidateOptionsMenu();

        boolean showListView = cursor != null && cursor.getCount() > 0;
        mRecyclerView.setVisibility(showListView ? View.VISIBLE : View.GONE);
        // Anhdts
        if (!showListView){
            updateEmptyMessage(mCallTypeFilter);
        }
        mEmptyListView.setVisibility(!showListView ? View.VISIBLE : View.GONE);

        if (mScrollToTop) {
            // The smooth-scroll animation happens over a fixed time period.
            // As a result, if it scrolls through a large portion of the list,
            // each frame will jump so far from the previous one that the user
            // will not experience the illusion of downward motion.  Instead,
            // if we're not already near the top of the list, we instantly jump
            // near the top, and animate from there.
            if (mLayoutManager.findFirstVisibleItemPosition() > 5) {
                // TODO: Jump to near the top, then begin smooth scroll.
                mRecyclerView.smoothScrollToPosition(0);
            }
            // Workaround for framework issue: the smooth-scroll doesn't
            // occur if setSelection() is called immediately before.
            mHandler.post(new Runnable() {
               @Override
               public void run() {
                   if (getActivity() == null || getActivity().isFinishing()) {
                       return;
                   }
                   mRecyclerView.smoothScrollToPosition(0);
               }
            });

            mScrollToTop = false;
        }
        mCallLogFetched = true;
        destroyEmptyLoaderIfAllDataFetched();
        return true;
    }

    /**
     * Called by {@link CallLogQueryHandler} after a successful query to voicemail status provider.
     */
    @Override
    public void onVoicemailStatusFetched(Cursor statusCursor) {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        mVoicemailStatusFetched = true;
        destroyEmptyLoaderIfAllDataFetched();
    }

    private void destroyEmptyLoaderIfAllDataFetched() {
        if (mCallLogFetched && mVoicemailStatusFetched && mEmptyLoaderRunning) {
            mEmptyLoaderRunning = false;
            getLoaderManager().destroyLoader(EMPTY_LOADER_ID);
        }
    }

    @Override
    public void onVoicemailUnreadCountFetched(Cursor cursor) {}

    @Override
    public void onMissedCallsUnreadCountFetched(Cursor cursor) {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(getLayoutCallLog(), container, false);
        setupView(view, null);
        // Anhdts
        initBarCallLog(view);
        return view;
    }

    protected void setupView(
            View view, @Nullable VoicemailPlaybackPresenter voicemailPlaybackPresenter) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        // Bkav TienNAb: them bien fastscroll
        final FastScroller fastScroller = view.findViewById(R.id.fastscroller);
        // Bkav TienNAb: chinh sua lai layout de hien thi fastscroll
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutCompleted(final RecyclerView.State state) {
                super.onLayoutCompleted(state);
                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
                final int lastVisibleItemPosition = findLastVisibleItemPosition();
                int itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1;

                // Bkav HuyNQN tach code de xu ly o lop con
                showOrHideFastScroller(fastScroller, itemsShown);
            }
        };
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Bkav HuyNQN tach code de xu ly lai o lop con
        setFastScroller(fastScroller);


        mEmptyListView = (EmptyContentView) view.findViewById(R.id.empty_list_view);
        mEmptyListView.setImage(R.drawable.empty_call_log);
        mEmptyListView.setActionClickedListener(this);

        int activityType = mIsCallLogActivity ? CallLogAdapter.ACTIVITY_TYPE_CALL_LOG :
                CallLogAdapter.ACTIVITY_TYPE_DIALTACTS;
        String currentCountryIso = GeoUtil.getCurrentCountryIso(getActivity());
        // Bkav TrungTH - sua lai thanh ham newCallLogAdapter cua minh
        mAdapter = newCallLogAdapter(currentCountryIso,
                        voicemailPlaybackPresenter,
                        activityType);
        mRecyclerView.setAdapter(mAdapter);
        fetchCalls();
    }

    public void setFastScroller(FastScroller fastScroller){
        // Bkav TienNAb: gan fastscroll vao recyclerview
        fastScroller.setRecyclerView(mRecyclerView);
        // Bkav TienNAb: tao giao dien cho thanh fastscroll
        fastScroller.setViewsToUse(R.layout.calllog_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
    }

    public void showOrHideFastScroller(FastScroller fastScroller, int itemsShown){
        //if all items are shown, hide the fast-scroller
        fastScroller.setVisibility(mAdapter.getItemCount() > itemsShown ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateEmptyMessage(mCallTypeFilter);
        mAdapter.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onStart() {
        // Start the empty loader now to defer other fragments.  We destroy it when both calllog
        // and the voicemail status are fetched.
        getLoaderManager().initLoader(EMPTY_LOADER_ID, null,
                new EmptyLoader.Callback(getActivity()));
        mEmptyLoaderRunning = true;
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Bkav HuyNQN tach code de xu ly o lop con
        refeshRecyclerView();
    }

    protected void refeshRecyclerView() {
        final boolean hasReadCallLogPermission =
                PermissionsUtil.hasPermission(getActivity(), READ_CALL_LOG);
        if (!mHasReadCallLogPermission && hasReadCallLogPermission) {
            // We didn't have the permission before, and now we do. Force a refresh of the call log.
            // Note that this code path always happens on a fresh start, but mRefreshDataRequired
            // is already true in that case anyway.
            mRefreshDataRequired = true;
            updateEmptyMessage(mCallTypeFilter);
        }

        mHasReadCallLogPermission = hasReadCallLogPermission;
        refreshData();
        mAdapter.onResume();

        rescheduleDisplayUpdate();
    }

    @Override
    public void onPause() {
        cancelDisplayUpdate();
        super.onPause();
    }

    @Override
    public void onStop() {
        updateOnTransition();
        mAdapter.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mAdapter.changeCursor(null);
        getActivity().getContentResolver().unregisterContentObserver(mCallLogObserver);
        getActivity().getContentResolver().unregisterContentObserver(mContactsObserver);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_FILTER_TYPE, mCallTypeFilter);
        outState.putInt(KEY_LOG_LIMIT, mLogLimit);
        outState.putLong(KEY_DATE_LIMIT, mDateLimit);
        outState.putBoolean(KEY_IS_CALL_LOG_ACTIVITY, mIsCallLogActivity);
        mAdapter.onSaveInstanceState(outState);
    }

    @Override
    public void fetchCalls() {
        mCallLogQueryHandler.fetchCalls(mCallTypeFilter, mDateLimit);
        if (!mIsCallLogActivity) {
            // Bkav TrungTH -
            updateTabUnreadCounts();
        }
    }

    protected void updateEmptyMessage(int filterType) {
        final Context context = getActivity();
        if (context == null) {
            return;
        }

        if (!PermissionsUtil.hasPermission(context, READ_CALL_LOG)) {
            mEmptyListView.setDescription(R.string.permission_no_calllog);
            mEmptyListView.setActionLabel(R.string.permission_single_turn_on);
            return;
        }

        final int messageId;
        switch (filterType) {
            case Calls.MISSED_TYPE:
                messageId = R.string.call_log_missed_empty;
                break;
            case Calls.VOICEMAIL_TYPE:
                messageId = R.string.call_log_voicemail_empty;
                break;
            case CallLogQueryHandler.CALL_TYPE_ALL:
                if (mIsCallLogActivity) {
                    messageId = R.string.no_call_log;
                } else {
                    messageId = getRecentCallsEmpty();
                }
                break;
            default:
                throw new IllegalArgumentException("Unexpected filter type in CallLogFragment: "
                        + filterType);
        }
        mEmptyListView.setDescription(messageId);
        if (mIsCallLogActivity) {
            mEmptyListView.setActionLabel(EmptyContentView.NO_LABEL);
        } else if (filterType == CallLogQueryHandler.CALL_TYPE_ALL) {
            mEmptyListView.setImage(R.drawable.empty_call_log);

            // Bkav HuyNQN tach code de xu ly o lop con
            hideEmptyListViewAction();
        }
        // Anhdts
        else {
            mEmptyListView.setActionLabel(EmptyContentView.NO_LABEL);
        }
    }

    // Bkav HuyNQN tach code de an di chu goi dien trong giao dien ghi am cuoc goi
    protected void hideEmptyListViewAction(){
        mEmptyListView.setActionLabel(R.string.call_log_all_empty_action);
    }

    CallLogAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (mMenuVisible != menuVisible) {
            mMenuVisible = menuVisible;
            if (!menuVisible) {
                updateOnTransition();
            } else if (isResumed()) {
                // Anhdts bo, gio onresume thi da cap nhat roi
                // refreshData();
            }
        }
    }

    /** Requests updates to the data to be shown. */
    protected void refreshData() {
        // Prevent unnecessary refresh.
        if (mRefreshDataRequired) {
            // Mark all entries in the contact info cache as out of date, so they will be looked up
            // again once being shown.
            mAdapter.invalidateCache();
            mAdapter.setLoading(true);

            fetchCalls();
            mCallLogQueryHandler.fetchVoicemailStatus();
            mCallLogQueryHandler.fetchMissedCallsUnreadCount();
            updateOnTransition();
            mRefreshDataRequired = false;
        } else {
            // Refresh the display of the existing data to update the timestamp text descriptions.
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Updates the voicemail notification state.
     *
     * TODO: Move to CallLogActivity
     */
    private void updateOnTransition() {
        // We don't want to update any call data when keyguard is on because the user has likely not
        // seen the new calls yet.
        // This might be called before onCreate() and thus we need to check null explicitly.
        if (mKeyguardManager != null && !mKeyguardManager.inKeyguardRestrictedInputMode()
                && mCallTypeFilter == Calls.VOICEMAIL_TYPE) {
            CallLogNotificationsHelper.updateVoicemailNotifications(getActivity());
        }
    }

    @Override
    public void onEmptyViewActionButtonClicked() {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (!PermissionsUtil.hasPermission(activity, READ_CALL_LOG)) {
          FragmentCompat.requestPermissions(this, new String[] {READ_CALL_LOG},
              READ_CALL_LOG_PERMISSION_REQUEST_CODE);
        } else if (!mIsCallLogActivity) {
            // Show dialpad if we are not in the call log activity.
            showDialpad(activity);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        if (requestCode == READ_CALL_LOG_PERMISSION_REQUEST_CODE) {
            if (grantResults.length >= 1 && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                // Force a refresh of the data since we were missing the permission before this.
                mRefreshDataRequired = true;
            }
        }
    }

    /**
     * Schedules an update to the relative call times (X mins ago).
     */
    private void rescheduleDisplayUpdate() {
        if (!mDisplayUpdateHandler.hasMessages(EVENT_UPDATE_DISPLAY)) {
            long time = System.currentTimeMillis();
            // This value allows us to change the display relatively close to when the time changes
            // from one minute to the next.
            long millisUtilNextMinute = MILLIS_IN_MINUTE - (time % MILLIS_IN_MINUTE);
            mDisplayUpdateHandler.sendEmptyMessageDelayed(
                    EVENT_UPDATE_DISPLAY, millisUtilNextMinute);
        }
    }

    /**
     * Cancels any pending update requests to update the relative call times (X mins ago).
     */
    private void cancelDisplayUpdate() {
        mDisplayUpdateHandler.removeMessages(EVENT_UPDATE_DISPLAY);
    }

    /*************************** Bkav**************************/

    /**
     * Bkav TrungTH - Tao them ham khoi tao calllogAdapter de ghi de thanh BkavCallLogAdapter
     * @param currentCountryIso
     * @param voicemailPlaybackPresenter
     * @param activityType
     * @return
     */
    protected CallLogAdapter newCallLogAdapter(String currentCountryIso, VoicemailPlaybackPresenter voicemailPlaybackPresenter,
                                               int activityType) {
        return ObjectFactory.newCallLogAdapter(
                getActivity(),
                this,
                new ContactInfoHelper(getActivity(), currentCountryIso),
                voicemailPlaybackPresenter,
                activityType);
    }
    // Bkav TrungTH - tao them ham de sua lai doan code duoc goi trong ListsFragment khong dung nua
    protected void updateTabUnreadCounts(){
        ((ListsFragment) getParentFragment()).updateTabUnreadCounts();
    }

    // Anhdts override by child
    public int getLayoutCallLog() {
        return R.layout.call_log_fragment;
    }

    // Anhdts them thanh lua chon hien thi tat ca cuoc goi hay cuoc goi nho
    protected void initBarCallLog(View view) {
    }
    // Bkav TrungTh bo xung truong hop lick vao emptyview
    protected void showDialpad(Activity activity){
        ((HostInterface) activity).showDialpad();
    }

    // Bkav TrungTH sua lai id
    protected int getRecentCallsEmpty() {
        return R.string.recentCalls_empty;
    }
}
