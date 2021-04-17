package bkav.android.btalk.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.contacts.common.CallUtil;
import com.android.contacts.common.list.OnPhoneNumberPickerActionListener;
import com.android.contacts.common.list.PhoneNumberPickerFragment;
import com.android.contacts.util.HelpUtils;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.database.DialerDatabaseHelper;
import com.android.dialer.dialpad.DialpadFragment;
import com.android.dialer.dialpad.SmartDialNameMatcher;
import com.android.dialer.interactions.PhoneNumberInteraction;
import com.android.dialer.list.SearchFragment;
import com.android.dialer.list.SmartDialSearchFragment;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.IntentUtil;
import com.android.dialer.util.TelecomUtil;
import com.android.dialer.widget.SearchEditTextLayout;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.UiUtils;
import com.android.phone.common.animation.AnimUtils;
import com.android.phone.common.animation.AnimationListenerAdapter;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.activities.BtalkSpeedDialActivity;
import bkav.android.btalk.esim.ISimProfile;
import bkav.android.btalk.esim.TelephonyStateListener;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.esim.Utils.OTTUtils;
import bkav.android.btalk.esim.adapter.BtalkEsimAdapter;
import bkav.android.btalk.esim.dialog_choose_sim.DialogChooseSimFragment;
import bkav.android.btalk.fragments.dialpad.BtalkDialpadFragment;
import bkav.android.btalk.fragments.dialpad.BtalkSmartDialSearchFragment;
import bkav.android.btalk.fragments.dialpad.BtalkSmartNumberListAdapter;
import bkav.android.btalk.mutilsim.SimUltil;
import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;
import bkav.android.btalk.suggestmagic.SuggestViewHolder;
import bkav.android.btalk.utility.BtalkUiUtils;
import bkav.android.btalk.view.OverlayButton;

/**
 * Created by trungth on 23/03/2017.
 * Bkav TrungTH:
 * 1. Do tab Phone ghep cua 2 fragment la DialpadFragment va SmartDialSearchFragment
 * nen trong 1 fragment chung, de add 2 thang nay len, roi moi add cai fragment chung nay len tab
 * 2. Code cu thuc hien add search view vao actionbar , nhung do minh ghep tab nen add truc
 * tiep view search vao file xml luon tam thoi voi 2 ly do
 * - Neu dung actionbar khi chuyen tab vua chuyen vua phai reset lai view actionbar cho phu hop => kha nang giat
 * - Sau tach view rieng de add vao popup nhung chuc nang dinh voi actionbar cua activity lai ko tach duoc
 */

public class BtalkPhoneFragment extends Fragment implements OnPhoneNumberPickerActionListener, View.OnClickListener, BtalkDialerDatabaseHelper.OnQueryPhoneSuccessListener,
        DialpadFragment.OnDialpadQueryChangedListener, BtalkDialpadFragment.LongClickCallBackListener, BtalkEsimAdapter.ESimAdapterListener, TelephonyStateListener.TelephonyListener, DialogChooseSimFragment.IDialogChooseSimFragment {

    private static final String TAG = "BtalkPhoneFragment";

    public static final boolean DEBUG = false;

    private static final String TAG_DIALPAD_FRAGMENT = "fragment_dialpad";

    private static final String TAG_DIALPAD_SMART = "fragment_dialpad_smart";

    private BtalkSmartDialSearchFragment mSmartDialSearchFragment;

    private BtalkDialpadFragment mDialpadFragment;

    private View mParentLayout;

    private View mFakeDialpad;

    private BtalkActivity mActivity;

    private int mActionBarHeight;

    private String mDialpadQuery;

    private boolean mIsDialpadShown;

    private String mSearchQuery;

    private ImageButton mDialpadButton;

    private View mContainerDigits;

    private SuggestViewHolder mSuggest;

    /**
     * Animation that slides in.
     */
    private Animation mSlideIn;

    /**
     * Animation that slides out.
     */
    private Animation mSlideOut;

    private View mOverlayList;

    private EditText mDigitText;

    //Bkav QuangNDb luu lai number truoc khi check signal de sau khi check thi goi
    private String mNumberBeforeCheckSignal;

    // Bkav HuyNQN dung de chanh viec lap cuoc goi vo han trong onResume khi goi cuoc goi bi nho tu notification
    private String mTempMissCall = "miss_call";
    // Bkav HuyNQN bugfix BOS-2529 start, danh dau da thuc hien cuoc goi voi so bi nho nay
    private boolean mHasCall = false;


    //TODO trungTH loi cache lam van luu cac gia tri cu trong khi chay lai oncrea khoi tao them 1 view moi
    public static BtalkPhoneFragment newInstance() {
        return new BtalkPhoneFragment();
    }

    public final static int CALL_REQUEST_CODE = 111;

    private TelephonyStateListener mTelephonyStateListener;
    private TelephonyManager mTelephonyManager;
    private static final int TIME_OUT_CHECK_SIGNAL = 30000/*30s*/;//Bkav QuangNDb thoi gian timeout check xem sim moi duoc kich hoat co song hay khong
    private static final int TIME_TICK = 1000/*1s*/;//Bkav QuangNDb cu 1s se check xem sim moi active da co song hay chua
    //Bkav QuangNDb bien dem nguoc check song
    private CountDownTimer mCountDownCheckSignal = new CountDownTimer(TIME_OUT_CHECK_SIGNAL, TIME_TICK) {
        @Override
        public void onTick(long millisUntilFinished) {
            if (mIsStateInService) {
                // Bkav HuyNQN delay de tranh bi loi khong kha dung
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onFinish();
                    }
                },2000);
            }
        }

        @Override
        public void onFinish() {
            makeACallAfterCheckSignal();
        }
    };

    //Bkav QuangNDb thuc hien cuoc goi sau khi sim da enable va sau khi check song
    private void makeACallAfterCheckSignal() {
        mIsShowDialogOrRequestFakeCall = false;
        mDialogSimEnable.dismiss();
        goneRecycleView();
        mDialpadFragment.setIdSimChooseCall(mTempProfile.getSimIdProfile().toString());
//        mDialpadFragment.setSimChange();
        mDialpadFragment.checkConfigTwoButton();
        makeACall(mTempProfile);
        mCountDownCheckSignal.cancel();
    }

    public static final String EXTRA_SIM_STATE = "ss";
    public static final String LOADED = "LOADED";
    //Bkav QuangNDb dang ky lang nghe trang thai sim thay doi
    private BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                String state = intent.getExtras().getString(EXTRA_SIM_STATE);
                if (mIsShowDialogOrRequestFakeCall && LOADED.equals(state)) {
                    if (mIsStateInService) {
                        makeACallAfterCheckSignal();
                    } else {
                        mCountDownCheckSignal.start();
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BtalkActivity) getActivity();
        mDetector = new GestureDetectorCompat(mActivity, new GestureListener());
        final Resources resources = mActivity.getResources();
        mActionBarHeight = resources.getDimensionPixelSize(R.dimen.action_bar_height);
        mSlideOut = AnimationUtils.loadAnimation(mActivity, R.anim.dialpad_slide_out_bottom);
        mSlideIn = AnimationUtils.loadAnimation(mActivity, R.anim.dialpad_slide_in_bottom);

        mSlideIn.setInterpolator(AnimUtils.EASE_IN);
        mSlideOut.setInterpolator(AnimUtils.EASE_OUT);

        mSlideIn.setAnimationListener(mSlideInListener);
        mSlideOut.setAnimationListener(mSlideOutListener);

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        addSmartDialSearchFragment(fragmentManager, ft);
        addBtalkDialpadFragment(fragmentManager, ft);
        mDialpadFragment.setListener(BtalkPhoneFragment.this);

        initSimStateReceiver();

        getSimDefault();

        ft.commit();
        setRetainInstance(true);
        mDialogSimEnable = new ProgressDialog(getContext());

        mTelephonyStateListener = new TelephonyStateListener();
        mTelephonyStateListener.setListener(BtalkPhoneFragment.this);
        mTelephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mTelephonyStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);


    }

    //Bkav QuangNDb dang ky lang nghe sim state change
    private void initSimStateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        getContext().registerReceiver(mSimStateReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mParentLayout = inflater.inflate(R.layout.btalk_phone_fragment, container, false);
        mContainerDigits = mParentLayout.findViewById(R.id.digits_container);
        mOverlayList = mParentLayout.findViewById(R.id.overlay_list);

        // BKav QuangNdb khoi tao digit text
        mDigitText = ((EditText) mContainerDigits.findViewById(R.id.digits));

        // Bkav HuyNQN
        mRecyclerViewEsim = mParentLayout.findViewById(R.id.list_sim);

        mProfileList = new ArrayList<>();

        mSuggest = new SuggestViewHolder(mParentLayout, this, mShowMoreListener);

        //Bkav TrungTh - Nut ban phim dung de hien thi lai giao dien phim bam khi  bi an di
        mDialpadButton = (ImageButton) mParentLayout.findViewById(R.id.floating_action_button);
        mDialpadButton.setOnClickListener(this);
        setVisibleFabButton(View.GONE);
        //Bkav TrungTh - show luon bam phim len khi moi vao
        showDialpadFragment(true);
        // Anhdts
        mDialpadFragment.setOverlayView(mOverlayList);
        mDigitText.addTextChangedListener(mPhoneSearchQueryTextListener);

        configureKeypadListeners(mParentLayout);
        return mParentLayout;
    }

    /**
     * Bkav QuangNDb them ham set an hien fab button
     */
    public void setVisibleFabButton(int state) {
        if (!mIsDialpadShown && mDialpadButton != null) {
            UiUtils.revealOrHideViewWithAnimationBtalk(mDialpadButton, state, null);
        } else {
            mDialpadFragment.setVisibleFabButton(state);
        }
    }

    /**
     * Anhdts listener cho nut show more contact
     */
    View.OnClickListener mShowMoreListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsDialpadShown) {
                hideDialpadFragment(true, false);
            } else {
                showDialpadFragment(true);
            }
        }
    };


    private void addSmartDialSearchFragment(FragmentManager fragmentManager, FragmentTransaction ft) {
        if (fragmentManager.findFragmentByTag(TAG_DIALPAD_SMART) != null) {
            mSmartDialSearchFragment = (BtalkSmartDialSearchFragment) fragmentManager.findFragmentByTag(TAG_DIALPAD_SMART);
            mSmartDialSearchFragment.setOnPhoneNumberPickerActionListener(this);
            mSmartDialSearchFragment.setQueryCompleteListener(BtalkDialerDatabaseHelper.getInstance(mActivity.getApplicationContext()));
            mSmartDialSearchFragment.resetActivity(mActivity);
        } else {
            mSmartDialSearchFragment = new BtalkSmartDialSearchFragment();
            mSmartDialSearchFragment.setOnPhoneNumberPickerActionListener(this);
            mSmartDialSearchFragment.setQueryCompleteListener(BtalkDialerDatabaseHelper.getInstance(mActivity.getApplicationContext()));
            if (!TextUtils.isEmpty(mDialpadQuery)) {
                mSmartDialSearchFragment.setAddToContactNumber(mDialpadQuery);
            }
            ft.replace(R.id.smartsearch_fragment, mSmartDialSearchFragment, TAG_DIALPAD_SMART).addToBackStack(null);
        }
    }

    /**
     * Bkav TrungTH - add giao dien ban phim len
     */
    private void addBtalkDialpadFragment(FragmentManager fragmentManager, FragmentTransaction ft) {
        if (fragmentManager.findFragmentByTag(TAG_DIALPAD_FRAGMENT) != null) {
            mDialpadFragment = (BtalkDialpadFragment) fragmentManager.findFragmentByTag(TAG_DIALPAD_FRAGMENT);
        } else {
            mDialpadFragment = new BtalkDialpadFragment();
            // Anhdts
            ft.replace(R.id.dialpad_fragment, mDialpadFragment, TAG_DIALPAD_FRAGMENT).addToBackStack(null);
        }
    }

    @Override
    public void onPickDataUri(Uri dataUri, boolean isVideoCall, int callInitiationType) {
        PhoneNumberInteraction.startInteractionForPhoneCall(
                mActivity, dataUri, isVideoCall, callInitiationType);
    }

    @Override
    public void onPickPhoneNumber(String phoneNumber, boolean isVideoCall, int callInitiationType) {
        if (phoneNumber == null) {
            // Invalid phone number, but let the call go through so that InCallUI can show
            // an error message.
            phoneNumber = "";
        }
        if (getResources().getBoolean(R.bool.config_regional_number_patterns_video_call) &&
                !CallUtil.isVideoCallNumValid(phoneNumber) &&
                isVideoCall && (CallUtil.isVideoEnabled(mActivity))) {
            Toast.makeText(mActivity, R.string.toast_make_video_call_failed, Toast.LENGTH_LONG).show();
            return;
        }

        final Intent intent = new IntentUtil.CallIntentBuilder(phoneNumber)
                .setIsVideoCall(isVideoCall)
                .setCallInitiationType(callInitiationType)
                .build();

        DialerUtils.startActivityWithErrorToast(mActivity, intent);
    }

    @Override
    public void onShortcutIntentCreated(Intent intent) {
        Log.w(TAG, "Unsupported intent has come (" + intent + "). Ignoring.");
    }

    @Override
    public void onHomeInActionBarSelected() {
        showDialpadFragment(true);
    }

    public SmartDialSearchFragment getSmartDialSearchFragment() {
        return mSmartDialSearchFragment;
    }

    public BtalkDialpadFragment getDialpadFragment() {
        checkDialpadFragmentExist();
        return mDialpadFragment;
    }

    @Override
    public void onDialpadQueryChanged(String query) {
        mDialpadQuery = query;
        if (mSmartDialSearchFragment != null) {
            mSmartDialSearchFragment.setAddToContactNumber(query);
        }
        final String normalizedQuery = SmartDialNameMatcher.normalizeNumber(query,
                SmartDialNameMatcher.LATIN_SMART_DIAL_MAP);

        try {
            if (checkDialpadFragmentExist() && mDialpadFragment.isVisible()) {
                mDialpadFragment.process_quote_emergency_unquote(normalizedQuery);
            }
        } catch (Exception ignored) {
            // Skip any exceptions for this piece of code
        }
    }


    /**
     * Bkav TrungTh - tra ve true do minh luc nao cung hien thi giao dien search
     *
     * @return
     */
    public boolean isInSearchUi() {
        return true;
    }

    public boolean hasSearchQuery() {
        return !TextUtils.isEmpty(mSearchQuery);
    }

    /**
     * @return Whether or not the action bar is currently showing (both slid down and visible)
     */
    public boolean isActionBarShowing() {
        return !mDialpadFragment.isVisible();
    }


    public boolean isDialpadShown() {
        return mIsDialpadShown;
    }


    /**
     * SelectionContactListener used to send search queries to the phone search fragment.
     */
    private final TextWatcher mPhoneSearchQueryTextListener = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String newText = s.toString().replaceAll(" ", "");
            newText = newText.replaceAll("-", "");
            if (newText.equals(mSearchQuery)) {
                // If the query hasn't changed (perhaps due to activity being destroyed
                // and restored, or user launching the same DIAL intent twice), then there is
                // no need to do anything here.
                return;
            }
            if (DEBUG) {
                Log.d(TAG, "onTextChange for mSearchView called with new query: " + newText);
                Log.d(TAG, "Previous Query: " + mSearchQuery);
            }
            mSearchQuery = newText;
            if (mSmartDialSearchFragment != null) {
                if (mSearchQuery.isEmpty() || (!(mSearchQuery.charAt(0) == '*') && !(mSearchQuery.charAt(0) == '#'))) {
                    mSmartDialSearchFragment.setQueryString(mSearchQuery, false /* delaySelection */);
                }
            }

            // Anhdts cap nhat lai nut xoa, neu dialpad khong hien thi hien thi len
            if (checkDialpadFragmentExist()) {
                if (newText.isEmpty()) {
                    mDialpadFragment.updateDeleteButton(true);
                    if (!mIsDialpadShown) {
                        showDialpadFragment(true);
                    }
                } else {
                    mDialpadFragment.updateDeleteButton(false);
                }
            }
            if (mIsDialpadShown && !BtalkUiUtils.isModeMultiScreen(mActivity)) {
                mSmartDialSearchFragment.resizeList();
            }

            if (TextUtils.isEmpty(newText)) {
                mDialpadFragment.showActionAddContact(false);
            }
        }

        @Override
        public void afterTextChanged(final Editable s) {
            if (s.toString().contains(" ")) {
                // Anhdts sau khi paste so thi dua con tro xuong cuoi
                if (mDialpadFragment.isPasteFromClipboard()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDigitText.setSelection(s.length());
                            mDialpadFragment.setPasteFromClipboard(false);
                        }
                    }, 500);
                }
            }
        }
    };


    /**
     * Initiates a fragment transaction to show the dialpad fragment. Animations and other visual
     * updates are handled by a callback which is invoked after the dialpad fragment is shown.
     */
    private void showDialpadFragment(boolean animate) {
        if (mIsDialpadShown) {
            return;
        }
        // mDialpadButton.setVisibility(View.GONE);
        if (checkDialpadFragmentExist() && !mDialpadFragment.isVisible()) {
            mSuggest.setButtonShowMoreDown(true);
            mIsDialpadShown = true;
            UiUtils.revealOrHideViewWithAnimation(mDialpadButton, View.GONE, null);
            if (mDialpadFragment.isHidden()) {
                final FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                ft.show(mDialpadFragment);

                mDialpadFragment.setAnimate(animate);
                // Anhdts fix loi cannot perform action after onSaveState
                ft.commitAllowingStateLoss();
                if (mFakeDialpad != null) {
                    mFakeDialpad.setVisibility(View.VISIBLE);
                }
            }
        }
        onDialpadUp(animate);
    }

    public void onListFragmentScrollStateChange(int scrollState) {
        // Bkav TrungTH co data thi moi cho hide ban phim di
        if (mSuggest.hasShowMore()) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL && (mSmartDialSearchFragment != null && mSmartDialSearchFragment.hasData()) && mSuggest.isVisibility()) {
                hideDialpadFragment(true, false);
                DialerUtils.hideInputMethod(mParentLayout);
            }
        }
        if (TextUtils.isEmpty(mDigitText.getText().toString())) {
            // Bkav HienDTk: fix bug bam ban phim so 2 lan moi quay lại tab dien thoai
            startActivity(new Intent(mActivity, BtalkSpeedDialActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }

    public void onListFragmentScroll(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO: No-op for now. This should eventually show/hide the actionBar based on
        // interactions with the ListsFragments.
    }

    /**
     * Initiates animations and other visual updates to hide the dialpad. The fragment is hidden in
     * a callback after the hide animation ends.
     *
     * @see #commitDialpadFragmentHide
     */
    public void hideDialpadFragment(boolean animate, boolean clearDialpad) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ||
                BtalkUiUtils.isModeMultiScreen(mActivity)) {
            return;
        }
        if (!checkDialpadFragmentExist() || mDialpadFragment.getView() == null || mDialpadFragment.isShowDialpad()) {
            return;
        }
        mSuggest.setButtonShowMoreDown(false);
        // Anhdts visible de khi listview bang 0 thi view van mau trang
        mOverlayList.setVisibility(View.VISIBLE);
        // bkav quangndb them hieu ung khi show dialpad
        UiUtils.revealOrHideViewWithAnimation(mDialpadButton, View.VISIBLE, null);
//        mDialpadButton.setVisibility(View.VISIBLE);
        if (clearDialpad) {
            // Temporarily disable accessibility when we clear the dialpad, since it should be
            // invisible and should not announce anything.
            mDialpadFragment.clearDialpad();
            mDialpadFragment.getDigitsWidget().setImportantForAccessibility(
                    View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        }
        if (!mIsDialpadShown && !mDialpadFragment.isRecipientsShown()) {
            return;
        }

        mIsDialpadShown = false;
        mDialpadFragment.setAnimate(animate);

        updateSearchFragmentPosition();

        if (animate) {
            mDialpadFragment.getView().startAnimation(mSlideOut);
        } else {
            commitDialpadFragmentHide();
        }

        onDialpadDown();

    }


    /**
     * Finishes hiding the dialpad fragment after any animations are completed.
     */
    public void commitDialpadFragmentHide() {
        if (checkDialpadFragmentExist() && !mDialpadFragment.isHidden()) {
            if (mFakeDialpad != null) {
                mFakeDialpad.setVisibility(View.GONE);
            }
            final FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.hide(mDialpadFragment);
            ft.commit();
        }
    }


    private void updateSearchFragmentPosition() {
        SearchFragment fragment = null;
        if (mSmartDialSearchFragment != null && mSmartDialSearchFragment.isVisible()) {
            fragment = mSmartDialSearchFragment;
        }
        if (fragment != null && fragment.isVisible()) {
            fragment.updatePosition(true /* animate */);
            // Bkav TrungTH thong bao ban phim show hoac an
            BtalkSmartNumberListAdapter adapter = (BtalkSmartNumberListAdapter) mSmartDialSearchFragment.getAdapter();
            adapter.updateTextColor(isDialpadShown());
        }
    }

    AnimationListenerAdapter mSlideInListener = new AnimationListenerAdapter() {
        @Override
        public void onAnimationEnd(Animation animation) {
            mDialpadFragment.hideOverlay();
        }
    };

    /**
     * SelectionContactListener for after slide out animation completes on dialer fragment.
     */
    AnimationListenerAdapter mSlideOutListener = new AnimationListenerAdapter() {
        @Override
        public void onAnimationEnd(Animation animation) {
            commitDialpadFragmentHide();
        }
    };


    @Override
    public void onClick(View view) {
        int resId = view.getId();
        if (resId == R.id.floating_action_button) {
            showDialpadFragment(true);
        }
    }


    /**
     * Callback from child DialpadFragment when the dialpad is shown.
     */
    public void onDialpadShown() {
        if (!checkDialpadFragmentExist()) {
            return;
        }
        if (mDialpadFragment.getAnimate()) {
            mDialpadFragment.getView().startAnimation(mSlideIn);
        } else {
            mDialpadFragment.setYFraction(0);
        }
        updateSearchFragmentPosition();
    }

    /**
     * Called to indicate that the user is trying to show the dialpad. Should be called before
     * any state changes have actually occurred.
     */
    public void onDialpadUp(boolean animate) {
        if (DEBUG) {
            Log.d(TAG, "OnDialpadUp: isInSearchUi " + isInSearchUi());
        }
    }

    /**
     * Called to indicate that the user is trying to hide the dialpad. Should be called before
     * any state changes have actually occurred.
     */
    public void onDialpadDown() {
//        if (DEBUG) {
//            Log.d(TAG, "OnDialpadDown: isInSearchUi " + isInSearchUi()
//                    + " hasSearchQuery: " + hasSearchQuery()
//                    + " isFadedOut: " + mSearchBox.isFadedOut()
//                    + " isExpanded: " + mSearchBox.isExpanded());
//        }
//        if (isInSearchUi()) {
//            if (!mSearchBox.isExpanded()) {
//                mSearchBox.expand(false /* animate */, false /* requestFocus */);
//            }
//            mSearchBox.fadeIn();
//        }
    }

    public int getDialpadHeight() {
        if (checkDialpadFragmentExist()) {
            return mDialpadFragment.getDialpadHeight();
        }
        return -1;
    }

    /**
     * Bkav TrungTH - callback cua actionbar => ko dung nua nhung ben fragment goc van sai
     * nen gan tao them ham de goi
     */
    public int getActionBarHideOffset() {
        return 0;
    }

    /**
     * Bkav TrungTH - callback cua get chieu cai actionbar => ko dung nua nhung ben fragment goc van sai
     * nen gan tao them ham de goi
     */
    public int getActionBarHeight() {
        return mActionBarHeight;
    }

    // Bkav TrungTH -
    public boolean onDialpadSpacerTouchWithEmptyQuery() {
        if (mSmartDialSearchFragment != null
                && !mSmartDialSearchFragment.isShowingPermissionRequest()
                && !TextUtils.isEmpty(mDialpadFragment.getDigitsWidget().getText())) { // Anhdts neu khong co so thi k cho cuon xuong
            hideDialpadFragment(true /* animate */, true /* clearDialpad */);
            return true;
        }
        return false;
    }

    private SearchEditTextLayout.Callback mSearchCallBack = new SearchEditTextLayout.Callback() {
        @Override
        public void onBackButtonClicked() {
            // Bkav TrungTH - an back thi show lai ban phim len
            showDialpadFragment(true);
        }

        @Override
        public void onSearchViewClicked() {
            // Bkav TrungTH - tam thoi khong lam gi
        }
    };

    // Anhdts Bind phan tu dau tien len
    @Override
    public void bindSuggestViewMain(DialerDatabaseHelper.ContactNumber data, boolean isShowMore) {
        if (checkDialpadFragmentExist()) {
            mSuggest.bindMainSuggestView(data, isShowMore);
            // TrungTh co mSuggest thi thoi an addcontact di
            mDialpadFragment.showActionAddContact(data.displayName.startsWith("?DATE:"));
            mDialpadFragment.showTooltip();
        }
    }

    @Override
    public void bindSecondarySuggestView(DialerDatabaseHelper.ContactNumber data) {
        mSuggest.bindSecondarySuggestView(data);
    }

    @Override
    public void clearSecondaryIfNeed() {
        mSuggest.cleanSecondarySuggestView();
    }

    @Override
    public boolean isStringQueryNotNull() {
        return !TextUtils.isEmpty(mDigitText.getText());
    }

    // Anhdts Xoa view suggest
    @Override
    public void cleanSuggestView() {
        if (checkDialpadFragmentExist()) {
            if (!getDialpadFragment().isPasteFromClipboard()) {
                mSuggest.cleanSuggestView();
                if (!TextUtils.isEmpty(mDigitText.getText())) {
                    mDigitText.setCursorVisible(true);
                }
            }
            // TrungTh ko  mSuggest thi hien  addcontact di
            if (mDigitText != null && !TextUtils.isEmpty(mDigitText.getText())) {
                mDialpadFragment.showActionAddContact(true);
            } else {
                mDialpadFragment.hideTooltip();
            }
        }
    }

    /**
     * Anhdts neu co so goi y thi se goi toi so duoc goi y khi an icon goi dien
     */
    public void actionCallSuggest() {
        if (mDialpadFragment != null) {
            mDialpadFragment.processPrefixCallAction(false);
        }
    }

    @Override
    public void onResume() {
        mActivity = (BtalkActivity) getActivity();
        super.onResume();
        // Bkav HuyNQN thuc hien cuoc goi tu notificatin cho cuoc goi nho
        reCallMissCalledFromeNotification();
    }

    // Bkav HuyNQN thuc hien cuoc goi tu notificatin cho cuoc goi nho
    private void reCallMissCalledFromeNotification() {
        String missCall = "";
        // Bkav HuyNQN fix bug BOS-23610 thuc hien lay misscall theo logic nay, tranh truong hop Home ra vao lai du lieu van duoc luu
        missCall = getActivity().getIntent().getStringExtra(BtalkActivity.MISS_CALL_NUMBER);
        // Bkav HuyNQN reset lai thong tin khi vua thuc hien goi dien toi cuoc goi bi nho
        if(mHasCall && mTempMissCall.equals(missCall)){
            mHasCall = false;
            mTempMissCall = "";
            return;
        }
        // Bkav HuyNQN bugfix BOS-2529 end
        if(!TextUtils.isEmpty(missCall)){
            mTempMissCall = missCall;
            List<PhoneAccountHandle> subscriptionAccountHandles =
                    PhoneAccountUtils.getSubscriptionPhoneAccounts(getContext());
            if (subscriptionAccountHandles.size() > 1) { // Bkav HuyNQN neu co 2 sim tro len thi bat dialog chon sim
                PhoneAccountHandle handleDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(getContext(), PhoneAccount.SCHEME_TEL);
                int slotCallDefault = SimUltil.getSlotSimByAccount(getContext(), handleDefault);
                if (slotCallDefault == -1) {
                    showDialogChooseSim(missCall);
                } else {
                    mHasCall = true;
                    SimUltil.callWithSlotSim(getActivity(), subscriptionAccountHandles.get(slotCallDefault).getId(), missCall);
                }
            } else { // Bkav HuyNQN neu 1 sim thi goi di luon
                mHasCall = true;
                SimUltil.callWithSlotSim(getActivity(), subscriptionAccountHandles.get(0).getId(), missCall);
            }
        }
    }

    public void setNumberDigitsActionFix(String number) {
        // Anhdts xoa view goi y di
        cleanSuggestView();
        mDigitText.setText(number);
        mDigitText.setCursorVisible(true);
        mDigitText.requestFocus();
        mDigitText.setSelection(mDigitText.getText().length());
    }

    // Anhdts
    public View getContainerDigit() {
        return mContainerDigits;
    }

    /**
     * Anhdts
     *
     * @return true neu mDialpadFragment ton tai
     */
    public boolean checkDialpadFragmentExist() {
        if (mDialpadFragment == null) {
            FragmentManager fragmentManager = getChildFragmentManager();
            mDialpadFragment = (BtalkDialpadFragment) fragmentManager.findFragmentByTag(TAG_DIALPAD_FRAGMENT);
            if (mDialpadFragment == null) {
                return false;
            }
        }
        return true;
    }

    public SuggestViewHolder getSuggestView() {
        return mSuggest;
    }

    // Bkav TrungTh bo xung ham xu ly intent view cua dialer
    public void setStartedFromNewIntent(boolean value) {
        if (mDialpadFragment != null)
            mDialpadFragment.setStartedFromNewIntent(value);
    }


    /**
     * TrungTH  Nhan  su kien truyen tu activity xuong de xu ly su kien vuot doc tren ban phim
     *
     * @param event
     * @return
     */
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Anhdts neu dang hien container chon sim thi dong container
        if (mDialpadFragment != null && mDialpadFragment.isShowContainerSim() && !mDialpadFragment.isFocusContainerSim(event)) {
//            mDialpadFragment.closeSimContainer();
            return true;
        }

        // Bkav HuyNQN thuc hien gone listsim khi click ra ngoai
        if (mDialpadFragment != null && isShowListSim() && !isFocusRecycleView(event)) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mProfileList.removeAll(mProfileList);
                mRecyclerViewEsim.setVisibility(View.GONE);
            }
            return true;
        }


        //TODO TrungTH them vao xu ly su kien bam vao slot sim
        if (mDialpadFragment != null && mDialpadFragment.actionChooseSimCall(event)) {
            return true;
        }


        if (mDialpadFragment != null && mIsDialpadShown && mDialpadFragment.getDialpadView() != null) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (mDialPadViewRect == null) {
                mDialPadViewRect = new Rect();
                mDialpadFragment.getDialpadView().getGlobalVisibleRect(mDialPadViewRect);
            }
            if (mDialPadViewRect.contains(x, y)) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mIsTouchInDialpad = true;
                }
                if (mIsTouchInDialpad) {
                    mDetector.onTouchEvent(event);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mIsTouchInDialpad = false;
                    mDialpadFragment.resetStateButtonDown();
                }
            } else if (mIsTouchInDialpad) {
                mIsTouchInDialpad = false;
            }
        }
        return false;
    }

    private boolean mIsTouchInDialpad = false;

    @Override
    public void onStop() {
        super.onStop();

    }

    public void setOnQueryCompleteListener(PhoneNumberPickerFragment.OnQueryCompleteListener onQueryCompleteListener) {
        if (mSmartDialSearchFragment != null) {
            mSmartDialSearchFragment.setQueryCompleteListener(onQueryCompleteListener);
        }
    }

    /**
     * Anhdts sau khi config change hoac sang da man hinh thi goi ham nay
     */
    public void resetValue() {
        mDialPadViewRect = null;
    }

    // TrungTH them xu ly vuot de show danh sach
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int scrollHeight = getResources().getDimensionPixelOffset(R.dimen.btalk_scroll_height);
            if (mDialpadFragment.isTouchButtonCall()) {
                scrollHeight *= 3;
            }
            if (distanceY > scrollHeight && Math.abs(distanceX) < scrollHeight && !mDialpadFragment.isShowContainerSim()) {
                //TrungTH neu vuot xuong thi show list goi y ra, => truyen state touch scroll de ben kia so sanh ok luon
                // Bkav HienDTk: fix bug bam ban phim so 2 lan moi quay lại tab dien thoai
                startActivity(new Intent(mActivity, BtalkSpeedDialActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            } else if (distanceY < -scrollHeight && Math.abs(distanceX) < scrollHeight && !mDialpadFragment.isShowContainerSim()) {
                onListFragmentScrollStateChange(AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    private GestureDetectorCompat mDetector;

    private Rect mDialPadViewRect;

    protected void configureKeypadListeners(View fragmentView) {
        if (fragmentView == null) {
            return;
        }
        mFakeDialpad = fragmentView.findViewById(R.id.layer_touch);
        if (fragmentView.findViewById(R.id.one) != null && fragmentView.findViewById(R.id.one) instanceof OverlayButton) {
            final int[] buttonIds = new int[]{R.id.one, R.id.two, R.id.three, R.id.four, R.id.five,
                    R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.star, R.id.zero, R.id.pound};

            OverlayButton dialpadKey;

            for (int i = 0; i < buttonIds.length; i++) {
                dialpadKey = (OverlayButton) fragmentView.findViewById(buttonIds[i]);
                dialpadKey.setOnPressedListener(mDialpadFragment);
                // Long-pressing button from two to nine will set up speed key dial.
                dialpadKey.setOnLongClickListener(mDialpadFragment);
            }
        }
    }

    // Bkav HuyNQN truong hop co 1 nut goi lay ra toan bo sim o 2 slot
    @Override
    public void longClick(View view) {
        updateProfileAdapter(-1);
    }

    // Bkav HuyNQN truong hop 2 nut bam lay danh sach sim cua slot 0
    @Override
    public void longClickSlot0(View view) {
        updateProfileAdapter(0);
    }

    // Bkav HuyNQN truong hop 2 nut bam lay danh sach sim o slot 1
    @Override
    public void longClickSlot1(View view) {
        updateProfileAdapter(1);
    }

    //Bkav QuangNDb update adapter list profile khi long click call button
    private void updateProfileAdapter(int slot) {
        if (mDialpadFragment.isDigitsEmpty()) {
            return;
        }
        mProfileList.clear();
        mProfileList = slot == -1 ? ESimUtils.getAllProfileWithNumber(mDialpadFragment.getSuggestNumber())
                : ESimUtils.getAllProfileFromSlotWithNumber(slot, mDialpadFragment.getSuggestNumber());
        if (mProfileList.size() <= 1) {
            return;
        }
        mEsimAdapter = new BtalkEsimAdapter(getContext(), mProfileList);
        mEsimAdapter.setListener(BtalkPhoneFragment.this);
        //Bkav QuangNDb neu hien thi 2 nut goi va long click button slot 0 thi inflate layout co first item quay ve ben trai
        if (slot == 0) {
            mEsimAdapter.setSpecialFirstItem(true);
        }
        RecyclerView.LayoutManager manager;
        int paddingStart = 0;
        int paddingRight = 0;
        if (mProfileList.size() <= 5) {
            if (slot == -1) {
                paddingStart = getResources().getDimensionPixelSize(R.dimen.list_esim_one_call_button_side_padding);
            } else if (slot == 0) {
                paddingRight = getResources().getDimensionPixelSize(R.dimen.list_esim_two_call_button_side_padding);
            } else {
                paddingStart = getResources().getDimensionPixelSize(R.dimen.list_esim_two_call_button_side_padding);
            }
            mRecyclerViewEsim.setPadding(paddingStart, 0, paddingRight, 0);
            manager = new LinearLayoutManager(getContext());
            ((LinearLayoutManager) manager).setReverseLayout(true);
        } else {
            mRecyclerViewEsim.setPadding(0, 0, 0, 0);
            manager = new GridLayoutManager(getContext(), 2);
            ((GridLayoutManager) manager).setReverseLayout(true);
        }
        mRecyclerViewEsim.setLayoutManager(manager);
        mRecyclerViewEsim.setAdapter(mEsimAdapter);
        mRecyclerViewEsim.setVisibility(View.VISIBLE);
    }


    private boolean isFocusRecycleView(MotionEvent event) {
        Rect outRect = new Rect();
        mRecyclerViewEsim.getGlobalVisibleRect(outRect);
        return outRect.contains((int) event.getRawX(), (int) event.getRawY());
    }

    public boolean isShowListSim() {
        return mRecyclerViewEsim != null && mRecyclerViewEsim.getVisibility() == View.VISIBLE;
    }

    @Override
    public void goneRecycleView() {
        if (mEsimAdapter != null) {
            mRecyclerViewEsim.setVisibility(View.GONE);
            mProfileList.removeAll(mProfileList);
            mEsimAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void setClickItemProfile(ISimProfile profile) {
        mTempProfile = profile;
        if ("Viber".equals(profile.getNameSimProfile())) {
            OTTUtils.get().callFreeWithViber(mDialpadFragment.getSuggestNumber());
            goneRecycleView();
            return;
        }
        if ("Zalo".equals(profile.getNameSimProfile())) {
            OTTUtils.get().callFreeWithZalo(mDialpadFragment.getSuggestNumber());
            goneRecycleView();
            return;
        }

        if (!profile.getSimProfileState()) {

            ESimUtils.enableProfile(profile, false);
            if (DialerUtils.isDialerBkav(getContext())) {
                try {
                    mNumberBeforeCheckSignal = mDialpadFragment.getSuggestNumber();
                    Uri handle = CallUtil.getCallIntent(mNumberBeforeCheckSignal).getData();
                    if (SimUltil.isPotentialInCallMMICode(handle) || SimUltil.isPotentialMMICode(handle)) { // Bkav HuyNQN neu la ma MMI se khong goi vao FakeCall ma bat showDialogEnableEsim
                        showDialogEnableEsim(profile);
                    } else {
                        UIIntents.get().requestFakeCall(mNumberBeforeCheckSignal, this, profile.getProfileIndex());
                        mIsShowDialogOrRequestFakeCall = true;
                    }
                } catch (Exception e) {
                    showDialogEnableEsim(profile);
                }
            } else {
                showDialogEnableEsim(profile);
            }
        } else {
            makeACall(profile);
        }
        goneRecycleView();
        /* }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == CALL_REQUEST_CODE) {
            if (data.getBooleanExtra("end_call", false)) {
                mIsShowDialogOrRequestFakeCall = false;
            }
        }
    }

    //Bkav QuangNDb neu da chon sim thi khong hien thi dialog hoi chon sim nua
    private void makeACall(ISimProfile profile) {
        List<PhoneAccountHandle> subscriptionAccountHandles =
                PhoneAccountUtils.getSubscriptionPhoneAccounts(getContext());
        if (subscriptionAccountHandles.size() > 1) {
            mDialpadFragment.setIdSimChooseCall(profile.getSlotSim() == 0 ? subscriptionAccountHandles.get(0).getId() : subscriptionAccountHandles.get(1).getId());
        } else {
            mDialpadFragment.setIdSimChooseCall(subscriptionAccountHandles.get(0).getId());
        }
        mDialpadFragment.actionCallWithIdSim(mNumberBeforeCheckSignal);
        //Bkav QuangNDb reset lai number sau khi goi
        mNumberBeforeCheckSignal = "";
    }

    private ISimProfile mTempProfile;
    private ProgressDialog mDialogSimEnable;
    private boolean mIsShowDialogOrRequestFakeCall;
    private RecyclerView mRecyclerViewEsim;
    private BtalkEsimAdapter mEsimAdapter;
    private List<ISimProfile> mProfileList;
    private int mSlotDefault;

    //Bkav QuangNDb logic dung cho truong hop khong dat bkav dialer lam mac dinh
    private void showDialogEnableEsim(ISimProfile profile) {
        mDialogSimEnable.setTitle(getResources().getString(R.string.title_enable_esim));
        mDialogSimEnable.setMessage(profile.getNameSimProfile() + " " + getResources().getString(R.string.message_enable_esim));
        mIsShowDialogOrRequestFakeCall = true;
        mDialogSimEnable.show();

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getSimDefault() {
        PhoneAccountHandle handleDefault = TelecomUtil.getDefaultOutgoingPhoneAccount(getContext(), PhoneAccount.SCHEME_TEL);
        mSlotDefault = SimUltil.getSlotSimByAccount(getContext(), handleDefault);
        mDialpadFragment.setSimDefault(mSlotDefault);
    }

    @Override
    public void stateInService(String s) {
        mIsStateInService = true;
    }

    @Override
    public void stateOutOfService(String s) {
        mIsStateInService = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            //Bkav QuangNDb huy ket noi voi receiver
            if (mSimStateReceiver != null) {
                getContext().unregisterReceiver(mSimStateReceiver);
            }
        } catch (IllegalArgumentException e) {
            Log.e("BtalkPhoneFragment:", "onDestroy: ", e);
        }

    }

    //Bkav QuangNDb bien check dien thoai dang trong trang thai inservice
    private boolean mIsStateInService;

    /**
     * HienDTk: show dialog chon sim
     */
    public void showDialogChooseSim(String number) {
        DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(number);
        dialogChooseSimFragment.setListener(this);
        dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
    }

    // Bkav HuyNQN thuc hien danh dau da goi doi voi truong hop luon hoi khi goi
    @Override
    public void chooseSimCall() {
        mHasCall = true;
    }
}
