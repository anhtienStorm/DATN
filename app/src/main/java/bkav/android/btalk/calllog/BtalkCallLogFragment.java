package bkav.android.btalk.calllog;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.common.widget.FloatingActionButtonController;
import com.android.dialer.calllog.CallLogAdapter;
import com.android.dialer.calllog.CallLogFragment;
import com.android.dialer.calllog.CallLogNotificationsHelper;
import com.android.dialer.calllog.CallLogQueryHandler;
import com.android.dialer.calllog.ContactInfoHelper;
import com.android.dialer.calllog.FastScroller;
import com.android.dialer.database.DialerDatabaseHelper;
import com.android.dialer.util.TelecomUtil;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.UiUtils;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.calllog.adapter.BtalkCallLogAdapter;
import bkav.android.btalk.calllog.recoder.RecorderService;
import bkav.android.btalk.esim.dialog_choose_sim.DialogChooseSimFragment;
import bkav.android.btalk.mutilsim.SimUltil;
import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;
import bkav.android.btalk.suggestmagic.SuggestPopup;
import bkav.android.btalk.utility.BtalkConst;
import bkav.android.btalk.view.EditTextKeyListener;

/**
 * Created by trungth on 24/03/2017.
 * Bkav TrungTH : de sua lai cac ham minh can khoi tao lai tuong ung voi cac lop
 */

public class BtalkCallLogFragment extends CallLogFragment implements View.OnClickListener, SuggestPopup.ActionSmartSuggest, BtalkCallLogAdapter.ItemClickListener {

    private static final String TAG_SEARCH_FRAGMENT = "search_fragment_calllog";
    private TextView mMissCall; // Bkav TrungTH doi sang Text View vi gio tri can thay doi mau text

    private TextView mAllCall;

    private boolean mIsShowMissCall = false;

    private boolean mIsModeDelete = false;

    private ImageView mViewShowMore;

    private BtalkCallLogEditorFrameAdapter mFrameEditor;

    private FrameLayout mFrameSearch;

    private FrameLayout mFloatingActionButtonContainer;

    private FloatingActionButtonController mFloatingActionButtonController;

    private boolean mIsSearchMode;

    private View mSearchContainer;

    private EditTextKeyListener mSearchView;

    private String mQueryString;

    private View mClearSearchView;

    private View mBarView;

    private ImageButton mFloatingActionButton;// Bkav TienNAb: nut tim kiem lon

    private ImageButton mFloatingActionButtonSmall;// Bkav TienNAb: nut tim kiem nho

    private TextView mTitleTab;// Bkav TienNAb: Them tieu de cho tab Calllog

    private ImageView mIconTab;// Bkav TienNAb: Icon lon cua tab

    private AppBarLayout mAppBarLayout;// Bkav TienNAb: them appbar layout

    private ImageView mImageBackgroundExpandLayout;// Bkav TienNAb: them hinh icon lon khi appbar layout mo rong

    private TextView mMissCallExpand;// Bkav TienNAb: them nut goi nho khi appbar layout mo rong

    private TextView mAllCallExpand;// Bkav TienNAb: them nut tat ca cuoc goi khi appbar layout mo rong

    private NestedScrollView mEmptyListViewContainer;// Bkav TienNAb: Them nestscroll view chua danh sach trong khi khong co cuoc goi nao

    private BtalkCallLogSearchFragment mSmartDialSearchFragment;

    private RecorderService mService;
    private Intent mPlayIntent;

    // Bkav TienNAb: tao bien activity
    private BtalkActivity mBtalkActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof BtalkActivity) {
            mBtalkActivity = (BtalkActivity) getActivity();
        }
    }

    public BtalkCallLogFragment() {
        super();
    }

    public BtalkCallLogFragment(int callTypeAll) {
        super(callTypeAll);
    }

    // Bkav HuyNQN Tao serviceConnection
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.RecorderBinder recorderBinder = (RecorderService.RecorderBinder) service;
            mService = recorderBinder.getService();
            ((BtalkCallLogAdapter)mAdapter).setService(mService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected CallLogAdapter newCallLogAdapter(String currentCountryIso, VoicemailPlaybackPresenter voicemailPlaybackPresenter, int activityType) {
        return new BtalkCallLogAdapter(
                getActivity(),
                this,
                new ContactInfoHelper(getActivity(), currentCountryIso),
                voicemailPlaybackPresenter,
                activityType);
    }

    @Override
    protected void updateTabUnreadCounts() {
        // Bkav TrungTH - Tam thoi chua xu ly
    }

    @Override
    public int getLayoutCallLog() {
        return R.layout.btalk_call_log_fragment;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnMiss:
            case R.id.btnMissExpand: {
                updateButtonState(true);
                filterMissOrAllCall(true);
                break;
            }
            case R.id.btnAll:
            case R.id.btnAllExpand: {
                updateButtonState(false);
                filterMissOrAllCall(false);
                break;
            }
            case R.id.ivMoreAction: {
                showPopupMenu(v);
                break;
            }
            case R.id.floating_action_button_small:
            case R.id.floating_action_button: {
                changeModeSearch(true);
                break;
            }
            case R.id.search_close_button: {
                mSearchView.setText("");
                break;
            }
            case R.id.search_back_button: {
                changeModeSearch(false);
                break;
            }

            case R.id.search_add_contact:
                addNewContact();
                break;
        }
    }

    //Bkav QuangNDb them ham add contact
    // TODO: 23/03/2020 nen gop vao thanh 1 voi class Contact Fragment
    private void addNewContact() {
        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            intent.putExtras(extras);
        }
        try {
            ImplicitIntentsUtil.startActivityInApp(getActivity(), intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), R.string.missing_app,
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Anhdts cap nhat trang thai nut
    private void updateButtonState(boolean isMissActive) {
        if (isMissActive) {
//            mMissCall.setTextColor(ContextCompat.getColor(getActivity(), R.color.btalk_ab_text_and_icon_selected_color));
//            mAllCall.setTextColor(ContextCompat.getColor(getActivity(), R.color.btalk_ab_text_and_icon_normal_color));

            mMissCall.setTypeface(null, Typeface.BOLD);
            mMissCallExpand.setTypeface(null, Typeface.BOLD);
            mAllCall.setTypeface(null, Typeface.NORMAL);
            mAllCallExpand.setTypeface(null, Typeface.NORMAL);
        } else {
//            mMissCall.setTextColor(ContextCompat.getColor(getActivity(), R.color.btalk_ab_text_and_icon_normal_color));
//            mAllCall.setTextColor(ContextCompat.getColor(getActivity(), R.color.btalk_ab_text_and_icon_selected_color));

            mMissCall.setTypeface(null, Typeface.NORMAL);
            mMissCallExpand.setTypeface(null, Typeface.NORMAL);
            mAllCall.setTypeface(null, Typeface.BOLD);
            mAllCallExpand.setTypeface(null, Typeface.BOLD);
        }
    }

    @Override
    public void showOrHideFastScroller(FastScroller fastScroller, int itemsShown) {
        mBtalkActivity.delayVisibleFastscroll(getContext(), fastScroller, mAdapter.getItemCount(), itemsShown);
    }

    // Anhdts chuyen cuoc goi thuong va cuoc goi nho
    public void filterMissOrAllCall(boolean isShowMissCall) {
        if (mIsShowMissCall != isShowMissCall) {
            mIsShowMissCall = isShowMissCall;
            mRefreshDataRequired = true;
            mCallTypeFilter = isShowMissCall ? CallLog.Calls.MISSED_TYPE : CallLogQueryHandler.CALL_TYPE_ALL;
            // update when view empty
            // updateEmptyMessage(mCallTypeFilter);
            refreshData();
        }
    }

    /**
     * Anhdts su kien double click tab
     */
    public void doubleClickTab() {
        boolean isShowMissCall = !mIsShowMissCall;
        filterMissOrAllCall(isShowMissCall);
        updateButtonState(isShowMissCall);
    }

    /**
     * Anhdts
     * hien popup menu len
     */
    private void showPopupMenu(final View v) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.btalk_calllog_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.clear_recents) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.clearCallLogConfirmation_title)
                            .setIcon(null)
                            .setMessage(R.string.clearCallLogConfirmation)
                            .setNegativeButton(R.string.btn_cancel, null)
                            .setPositiveButton(R.string.btn_ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteAllCallLog();
                                        }
                                    }).setCancelable(false).create().show();
                } else if (item.getItemId() == R.id.mode_editor) {
                    changeModeEditor(true);
                } else if (item.getItemId() == R.id.add_contact) {
                    getActivity().sendBroadcast(new Intent("bkav.android.TEST_CONTACT_BTALK"));
                }
                return false;
            }
        });
        popupMenu.show();
    }

    /**
     * Anhdts
     * Xoa tat ca lich su cuoc goi
     */
    private void deleteAllCallLog() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getContext().getContentResolver().delete(CallLog.Calls.CONTENT_URI,
                null, null);
        updateEmptyMessage(mCallTypeFilter);
        refreshData();
        BtalkDialerDatabaseHelper.getInstance(getActivity().getApplicationContext()).removeCallLog();
    }

    @Override
    protected void setupView(View view, @Nullable VoicemailPlaybackPresenter voicemailPlaybackPresenter) {
        mIsSearchMode = mIsModeDelete = false;
        super.setupView(view, voicemailPlaybackPresenter);
        //Bkav QuangNDb them lang nghe chon goi khi khong co sim mac dinh
        ((BtalkCallLogAdapter) mAdapter).setItemClickListener(this);
    }

    // Anhdts khoi tao thanh bar chuyen giua call log va missed call log
    @Override
    protected void initBarCallLog(View view) {
        mMissCall = (TextView) view.findViewById(R.id.btnMiss);
        mMissCall.setOnClickListener(this);
        mAllCall = (TextView) view.findViewById(R.id.btnAll);
        mAllCall.setOnClickListener(this);
        mViewShowMore = (ImageView) view.findViewById(R.id.ivMoreAction);
        mViewShowMore.setOnClickListener(this);

        mTitleTab = view.findViewById(R.id.title_tab_calllog);
        mIconTab = view.findViewById(R.id.img_background_expand_layout);
        mAppBarLayout = view.findViewById(R.id.app_bar_layout);
        mImageBackgroundExpandLayout = view.findViewById(R.id.img_background_expand_layout);
        mAllCallExpand = view.findViewById(R.id.btnAllExpand);
        mMissCallExpand = view.findViewById(R.id.btnMissExpand);
        mEmptyListViewContainer = view.findViewById(R.id.empty_list_view_container);

        // Bkav TienNAb: khi click icon tab thi chuyen qua lai giua tab cuoc goi nho va tab tat ca cuoc goi
        mIconTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallTypeFilter == CallLog.Calls.MISSED_TYPE) {
                    updateButtonState(false);
                    filterMissOrAllCall(false);
                } else {
                    updateButtonState(true);
                    filterMissOrAllCall(true);
                }
            }
        });

        mMissCallExpand.setOnClickListener(this);
        mAllCallExpand.setOnClickListener(this);

        // Anhdts khoi tao mac dinh thanh bar o trang thai all log call
        updateButtonState(false);
        mFrameEditor = new BtalkCallLogEditorFrameAdapter(BtalkCallLogFragment.this,
                (FrameLayout) view.findViewById(R.id.editor_frame), (BtalkCallLogAdapter) mAdapter, view.findViewById(R.id.bar_view));
        mFrameSearch = (FrameLayout) view.findViewById(R.id.search_frame);

        mFloatingActionButton
                = (ImageButton) view.findViewById(R.id.floating_action_button);
        mFloatingActionButton.setOnClickListener(this);

        mFloatingActionButtonSmall = view.findViewById(R.id.floating_action_button_small);
        mFloatingActionButtonSmall.setOnClickListener(this);

        mFloatingActionButtonContainer = (FrameLayout) view.findViewById(R.id.floating_action_button_container);
        mFloatingActionButtonController = new FloatingActionButtonController(getActivity(),
                mFloatingActionButtonContainer, mFloatingActionButton);
        mBarView = view.findViewById(R.id.bar_view);

        mSearchContainer = null;
        initializeFabVisibility();
        setVisibleFabButtonSmall(View.VISIBLE);

        updateAppBarLayout(view);

        if (!mIsSearchMode) {
            hideSearchFragment();
        }

        // Bkav TienNAb - Fix bug BOS-2997 - Start
        // xuly dong mo appbar layout
        mBtalkActivity.appbarLayoutWithInMultiWindowMode(mBtalkActivity, mAppBarLayout, mRecyclerView);
        // Bkav TienNAb - Fix bug BOS-2997 - End
    }

    private void initializeFabVisibility() {
        mFloatingActionButtonContainer.setVisibility(View.VISIBLE);
        mFloatingActionButtonController.resetIn();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Bkav HuyNQN bind service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPlayIntent = new Intent(getContext(), RecorderService.class);
            getContext().bindService(mPlayIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            getContext().startService(mPlayIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Bkav TrungTh goi ham thong bao da vao onResume de bao da load xong fragment nay
        if (getActivity() != null && getActivity() instanceof BtalkActivity) {
            ((BtalkActivity) getActivity()).justFinishLoadingTab();
        }
        if (mIsSearchMode) {
            mSearchView.setSelected(true);
            if (!TextUtils.isEmpty(mSearchView.getText())) {
                mSearchView.setSelection(0, mSearchView.getText().length());
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Bkav HuyNQN kiem tra neu service ton tai moi unbind
        if (mService != null) {
            mService.onStopSelf();
            getActivity().unbindService(mServiceConnection);
        }
    }

    @Override
    protected void showDialpad(Activity activity) {
        if (getActivity() != null && getActivity() instanceof BtalkActivity) {
            ((BtalkActivity) getActivity()).setCurrentTab(BtalkActivity.TAB_PHONE_INDEX);
        }
    }

    @Override
    protected int getRecentCallsEmpty() {
        return R.string.call_log_all_empty;
    }

    /**
     * Anhdts cap nhat tat ca cuoc goi nho la da doc
     */
    public void markMissedCallsAsReadAndRemoveNotifications() {
        if (mCallLogQueryHandler != null) {
            mCallLogQueryHandler.markMissedCallsAsRead();
            CallLogNotificationsHelper.removeMissedCallNotifications(getActivity());
        }
    }

    /**
     * Anhdts neu dang trong che do xoa hang loat thi an back se thoat che do
     */
    public boolean onBackPress() {
        if (mIsModeDelete) {
            changeModeEditor(false);
            return true;
        } else if (mIsSearchMode) {
            changeModeSearch(false);
            return true;
        }
        return false;
    }

    /**
     * Anhdts chuyen che do chinh sua nhat ki
     */
    public void changeModeEditor(boolean isModeDelete) {
        if (isModeDelete == mIsModeDelete) {
            return;
        }
        mIsModeDelete = isModeDelete;
        if (getActivity() != null) {
            if (isModeDelete) {
                mBtalkActivity.collapseAppBarLayout(mAppBarLayout, mRecyclerView);
                hideAppBarItem(true);

                setVisibleFabButton(View.INVISIBLE);
                ((BtalkActivity) getActivity()).setStatusbarOnActionMode();
            } else {
                // Bkav TienNAb - Fix bug BOS-2997 - Start
                // check dieu kien co phai dang o che do chia doi man hinh khong truoc khi mo appbar layout
                if (!mBtalkActivity.isInMultiWindowMode()){
                    mBtalkActivity.expandAppBarLayout(mAppBarLayout, mRecyclerView);
                }
                // Bkav TienNAb - Fix bug BOS-2997 - End
                hideAppBarItem(false);

                setVisibleFabButton(View.VISIBLE);
                ((BtalkActivity) getActivity()).exitActionMode();
            }
        }
        mAdapter.notifyDataSetChanged();
        mFrameEditor.changeModeSelection(isModeDelete);
        ((BtalkCallLogAdapter) mAdapter).changeModeDelete(isModeDelete);
        ((BtalkCallLogAdapter) mAdapter).setCheckItemChangeListener(mCheckItemListener);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Anhdts chuyen che do search
     */
    private void changeModeSearch(boolean modeSearch) {
        mIsSearchMode = modeSearch;
        if (mIsSearchMode) {
            mBtalkActivity.collapseAppBarLayout(mAppBarLayout, mRecyclerView);
            hideAppBarItem(true);

            if (mSearchContainer == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                mSearchContainer = inflater.inflate(R.layout.btalk_search_bar_expanded, mFrameSearch,
                        false);
                mFrameSearch.addView(mSearchContainer);
                mSearchView = (EditTextKeyListener) mSearchContainer.findViewById(R.id.search_view);
                mSearchView.setHint(getString(R.string.hint_findContacts));
                mSearchView.addTextChangedListener(new SearchTextWatcher());
                mSearchContainer.findViewById(R.id.search_back_button).setOnClickListener(this);
                //Bkav QuangNDb
                mSearchContainer.findViewById(R.id.search_add_contact).setOnClickListener(this);

                mClearSearchView = mSearchContainer.findViewById(R.id.search_close_button);
                mClearSearchView.setVisibility(View.GONE);
                mClearSearchView.setOnClickListener(this);
                addSmartDialSearchFragment();
                mSearchView.setOnBackPressListener(new EditTextKeyListener.OnBackPressListener() {
                    @Override
                    public boolean onBackState() {
                        ((BtalkActivity) getActivity()).getSmartSuggestLoaderManage().hideViewSuggest();
                        if (TextUtils.isEmpty(mSearchView.getText())) {
                            changeModeSearch(false);
                        }
                        return false;
                    }
                });
            }

            if (!mSearchContainer.isShown()) {
                mBarView.setVisibility(View.INVISIBLE);
                mSearchContainer.findViewById(R.id.search_box_expanded).setVisibility(View.VISIBLE);
                mFrameSearch.setVisibility(View.VISIBLE);
                mEmptyListViewContainer.setVisibility(View.GONE);// Bkav HuyNQN gone view nay di de fix loi khong vuot duoc khi bam tim kiem
                mEmptyListView.setVisibility(View.GONE);
                showSearchFragment();
            }
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mSearchView.setEnabled(true);
                    mSearchView.requestFocus();
                    mSearchView.setCursorVisible(true);
                    showInputMethod(mSearchView);
                }
            });
        } else {
            // Bkav TienNAb - Fix bug BOS-2997 - Start
            // check dieu kien co phai dang o che do chia doi man hinh khong truoc khi mo appbar layout
            if (!mBtalkActivity.isInMultiWindowMode()){
                mBtalkActivity.expandAppBarLayout(mAppBarLayout, mRecyclerView);
            }
            // Bkav TienNAb - Fix bug BOS-2997 - End
            hideAppBarItem(false);
            hideSearchFragment();
            mSearchView.setText("");
            InputMethodManager imm = (InputMethodManager) mSearchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            mSearchContainer.setVisibility(View.INVISIBLE);
            mBarView.setVisibility(View.VISIBLE);
            fetchCalls();
        }
    }

    private void addSmartDialSearchFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentByTag(TAG_SEARCH_FRAGMENT) != null) {
            mSmartDialSearchFragment = (BtalkCallLogSearchFragment) fragmentManager.findFragmentByTag(TAG_SEARCH_FRAGMENT);
        } else {
            mSmartDialSearchFragment = new BtalkCallLogSearchFragment();
            ft.replace(R.id.content_search, mSmartDialSearchFragment, TAG_SEARCH_FRAGMENT).addToBackStack(null);
            ft.commit();
        }
        mSmartDialSearchFragment.setOnClickCallback(mIDialogCallback); // TrungTH them vao
    }

    public void hideSearchFragment() {
        final FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (mSmartDialSearchFragment == null) {
            FragmentManager fragmentManager = getChildFragmentManager();
            if (fragmentManager.findFragmentByTag(TAG_SEARCH_FRAGMENT) != null) {
                mSmartDialSearchFragment = (BtalkCallLogSearchFragment) fragmentManager.findFragmentByTag(TAG_SEARCH_FRAGMENT);
                mSmartDialSearchFragment.setOnClickCallback(mIDialogCallback); // TrungTH them vao
            } else {
                return;
            }
        }
        if (!mSmartDialSearchFragment.isHidden()) {
            ft.hide(mSmartDialSearchFragment);
            ft.commitAllowingStateLoss(); // TrungTH doi sang commit => commitAllowingStateLoss do loi cung voi onSaveInstanceState
        }
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showSearchFragment() {
        mRecyclerView.setVisibility(View.GONE);
        if (mSmartDialSearchFragment.isHidden()) {
            final FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.show(mSmartDialSearchFragment);
            ft.commitAllowingStateLoss();
        }
    }

    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager)
                view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    @Override
    public void onItemClickWithNotDefaultSim(String number) {
        DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(number);
        dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
    }

    /**
     * Anhdts: lằng nghe sự kiện text thay đổi.
     */
    private class SearchTextWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence queryString, int start, int before, int count) {
            if (queryString.equals(mQueryString)) {
                return;
            }
            mQueryString = queryString.toString();
            mSmartDialSearchFragment.setQueryString(mQueryString);
            ((BtalkActivity) getActivity()).querySmartContact(mQueryString, BtalkCallLogFragment.this);
            mClearSearchView.setVisibility(
                    TextUtils.isEmpty(queryString) ? View.GONE : View.VISIBLE);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    }

    CheckItemListener mCheckItemListener = new CheckItemListener() {
        @Override
        public void updateSelectionCount(int count) {
            mFrameEditor.setSelectionCount(count);
        }
    };

    /**
     * Anhdts listener thay doi chon item de xoa
     */
    public interface CheckItemListener {
        void updateSelectionCount(int count);
    }

    public void setVisibleFabButton(int state) {
        UiUtils.revealOrHideViewWithAnimationBtalk(mFloatingActionButtonContainer, state, null);
    }

    public void setVisibleFabButtonSmall(int state) {
        UiUtils.revealOrHideViewWithAnimationBtalk(mFloatingActionButtonSmall, state, null);
    }

    public void changeTab() {
        if (mIsModeDelete) {
            changeModeEditor(false);
        } else if (mIsSearchMode) {
            changeModeSearch(false);
        }
    }

    @Override
    public boolean onCallsFetched(Cursor cursor) {
        boolean showListView = cursor != null && cursor.getCount() > 0;
        if (mEmptyListViewContainer != null){
            if (!showListView){
                mEmptyListViewContainer.setVisibility(View.VISIBLE);
            } else {
                mEmptyListViewContainer.setVisibility(View.INVISIBLE);
            }
        }
        // Bkav TienNAb: fix loi khong xoa duoc cuoc goi trong giao dien chinh sua cuoc goi
        return !mIsSearchMode && super.onCallsFetched(cursor);
    }

    @Override
    public void fetchCalls() {
        if (!mIsSearchMode) {
            super.fetchCalls();
        }
    }

    @Override
    public void onClickMessage() {

    }

    @Override
    public void onClick(DialerDatabaseHelper.ContactNumber data) {
        clearTextSearch();
    }

    private void clearTextSearch() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSearchView.setText("");
                mSearchView.setCursorVisible(true);
                mSearchView.requestFocus();
                // TrungTH chot lai goi xong ra khoi giao dien search luonfl
                if (mIsSearchMode) {
                    changeModeSearch(false);
                }
            }
        }, BtalkConst.DELAY_CLEAR_TEXT_SEARCH); // Them delay vi xoa luon tao cam giac giat view khi goi dien
    }

    private BtalkCallLogSearchFragment.IOnClickCallback mIDialogCallback = new BtalkCallLogSearchFragment.IOnClickCallback() {
        @Override
        public void onClickActionFinish() {
            clearTextSearch();
        }
    };

    @Override
    public void onItemCallWithOtherSim(boolean isShow, String number) {
        if (isShow) {
            PhoneAccountHandle handleDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(getContext(), PhoneAccount.SCHEME_TEL);
            int slotDefault = SimUltil.getSlotSimByAccount(getContext(), handleDefault);
            DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(number);
            dialogChooseSimFragment.setSlotDefault(slotDefault);
            dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
        } else {
            UIIntents.get().makeACall(getContext(), getFragmentManager(), number);
        }
    }

    // Bkav TienNAb: update appbar layout
    public void updateAppBarLayout(View view) {

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                //Bkav QuangNDb
                final float imageAlpha = 1 - (Math.abs(verticalOffset) / (float) appBarLayout.getTotalScrollRange());

                if (imageAlpha == 0){
                    mFloatingActionButton.setClickable(true);
                } else {
                    mFloatingActionButton.setClickable(false);
                }

                if (imageAlpha == 1){
                    mFloatingActionButtonSmall.setClickable(true);
                } else {
                    mFloatingActionButtonSmall.setClickable(false);
                }

                if (mBtalkActivity != null && isAdded()){
                    // Bkav TienNAb: hieu ung text cua thanh title
                    if (imageAlpha == 0){
                        mTitleTab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_min));
                    } else if (imageAlpha == 1){
                        mTitleTab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_max));
                    } else if (imageAlpha > 0 && imageAlpha < 1){
                        mTitleTab.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_min)
                                +(getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_max)-getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_min))*imageAlpha);
                    }
                }

                if (scrollRange + verticalOffset <= 15) {
                    if (mIsModeDelete || mIsSearchMode){
                        hideAppBarItem(true);
                    } else {
                        mTitleTab.setVisibility(View.VISIBLE);
                        mAllCall.setVisibility(View.VISIBLE);
                        mMissCall.setVisibility(View.VISIBLE);
                        mMissCallExpand.setVisibility(View.GONE);
                        mAllCallExpand.setVisibility(View.GONE);
                    }
                    if (mImageBackgroundExpandLayout.getVisibility() == View.VISIBLE){
                        mImageBackgroundExpandLayout.setVisibility(View.GONE);
                    }
                    isShow = true;
                } else if (isShow) {
                    mImageBackgroundExpandLayout.setVisibility(View.VISIBLE);
                    if (mIsModeDelete || mIsSearchMode) {
                        hideAppBarItem(true);
                    } else {
                        mTitleTab.setVisibility(View.VISIBLE);
                        mAllCall.setVisibility(View.GONE);
                        mMissCall.setVisibility(View.GONE);
                        mMissCallExpand.setVisibility(View.VISIBLE);
                        mAllCallExpand.setVisibility(View.VISIBLE);
                    }
                    isShow = false;
                }
            }
        });
    }

    // Bkav TienNAb: an/hien cac item khi appbar layout dong/mo
    private void hideAppBarItem(boolean b) {
        if (b){
            mTitleTab.setVisibility(View.GONE);
            mAllCall.setVisibility(View.GONE);
            mMissCall.setVisibility(View.GONE);
            mMissCallExpand.setVisibility(View.GONE);
            mAllCallExpand.setVisibility(View.GONE);
        } else {
            mMissCallExpand.setVisibility(View.VISIBLE);
            mAllCallExpand.setVisibility(View.VISIBLE);
        }
    }
}
