
package bkav.android.btalk.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.BkavViewPager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.telecom.PhoneAccount;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.interactions.ImportExportDialogFragment;
import com.android.contacts.common.list.PhoneNumberPickerFragment;
import com.android.contacts.common.list.PinnedHeaderListView;
import com.android.contacts.common.logging.ScreenEvent;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.contacts.interactions.ContactMultiDeletionInteraction;
import com.android.contacts.list.ContactsIntentResolver;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.quickcontact.QuickContactActivity;
import com.android.dialer.TransactionSafeActivity;
import com.android.dialer.calllog.CallLogQueryHandler;
import com.android.dialer.calllog.FastScroller;
import com.android.dialer.dialpad.DialpadFragment;
import com.android.dialer.list.OnListFragmentScrolledListener;
import com.android.dialer.list.SearchFragment;
import com.android.dialer.util.AppCompatConstants;
import com.android.dialer.util.DialerUtils;
import com.android.ex.chips.ChipsUtil;
import com.android.messaging.Factory;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.data.ConversationListData;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversationlist.ConversationListFragment;
import com.android.messaging.ui.conversationlist.ConversationListItemView;
import com.android.messaging.util.BugleActivityUtil;
import com.android.messaging.util.SendBroadcastUnreadMessage;
import com.ms_square.etsyblur.BlurSupport;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import bkav.android.blur.activity.BkavBlurHelper;
import bkav.android.blur.activity.WallpaperBlurCompat;
import bkav.android.btalk.BtalkExecutors;
import bkav.android.btalk.R;
import bkav.android.btalk.calllog.BtalkCallLogFragment;
import bkav.android.btalk.calllog.recoder.AutoDelateRecoder;
import bkav.android.btalk.calllog.ulti.BtalkCallLogCache;
import bkav.android.btalk.contacts.BtalkContactsPreferenceActivity;
import bkav.android.btalk.contacts.BtalkMultiSelectContactsListFragment;
import bkav.android.btalk.esim.ActiveDefaultProfileReceiver;
import bkav.android.btalk.esim.Utils.ESimUtils;
import bkav.android.btalk.fragments.BtalkPhoneFragment;
import bkav.android.btalk.messaging.datamodel.action.InsertMMSRecordAction;
import bkav.android.btalk.messaging.ui.BtalkMissingStoragePermissionDialog;
import bkav.android.btalk.messaging.ui.appsettings.BtalkApplicationSettingsActivity;
import bkav.android.btalk.messaging.ui.conversationlist.BtalkConversationListFragment;
import bkav.android.btalk.messaging.util.BtalkDataObserver;
import bkav.android.btalk.messaging.util.BtalkPermissionUtil;
import bkav.android.btalk.settings.BtalkDialerSettingsActivity;
import bkav.android.btalk.settings.BtalkSettingOpenAppFragment;
import bkav.android.btalk.suggestmagic.BtalkDialerDatabaseHelper;
import bkav.android.btalk.suggestmagic.SuggestLoaderManager;
import bkav.android.btalk.suggestmagic.SuggestPopup;
import bkav.android.btalk.trial_mode.TrialModeUtils;
import bkav.android.btalk.utility.BtalkUiUtils;
import bkav.android.btalk.utility.BtalkLog;
import bkav.android.btalk.utility.Config;
import bkav.android.btalk.utility.ContactUtils;
import bkav.android.btalk.utility.PermissionUtil;
import bkav.android.btalk.utility.PrefUtils;
import bkav.android.btalk.utility.ShortcutUtils;
import bkav.android.btalk.utility.TelephoneExchangeUtils;
import bkav.android.btalk.utility.TooltipController;
import bkav.android.btalk.view.BtalkViewAnchorNav;

import static android.Manifest.permission.READ_SMS;
import static bkav.android.btalk.messaging.ui.BtalkUIIntentsImpl.MESSAGE_ACTION;
import static bkav.android.btalk.messaging.util.BtalkPermissionUtil.REQUEST_READ_SMS;
import static bkav.android.btalk.trial_mode.TrialModeUtils.IS_TRIAL_MODE_PREF_KEY;

/**
 * Created by trungth on 21/03/2017.
 */

public class BtalkActivity extends TransactionSafeActivity implements
//        ConversationListFragment.ConversationListFragmentHost, DialpadFragment.HostInterface,
//        SearchFragment.HostInterface, OnListFragmentScrolledListener,
//        ContactMultiDeletionInteraction.MultiContactDeleteListener,
//        WallpaperBlurCompat.ChangeWallPaperListener, BtalkDataObserver.OnChangeListener,
        BtalkMissingStoragePermissionDialog.OnRequestStoragePermission,
        PermissionUtil.CallbackCheckPermission /* TrungTH THem check quyen */ {

    public static final int TAB_PHONE_INDEX = 0;

    public static final int TAB_MESSAGES_INDEX = 1;

    public static final int TAB_CALLLOG_INDEX = 2;

    public static final int TAB_CONTACTS_INDEX = 3;

    public static final int TAB_COUNT_DEFAULT = 4;

    private static final float MAX_SIZE_TEXT_SCALE = 1.1f;

    public static final String ARGUMENT_NUMBER = "arg_number";

    public static final String PREF_IS_LIGHT_TAB_BAR = "pref_is_light_tab_bar"; // Bkav TrungTH
    // setting luu bien
    // tabbar mau trang
    // hay khong

    // Anhdts action startActivity
    public static final String ACTION_FIX_BEFORE_CALL = "btalk.android.FIX_BEFORE_CALL";
    public static final String ACTION_DIAL_BKAV_VIEW = "btalk.intent.action.DIAL_BKAV_VIEW";

    // Anhdts action mo cuoc goi nho tu notify
    private static final String ACTION_SHOW_CALL_LOG = "btalk.android.dialer.calllog.ACTION_SHOW_CALL_LOG_TAB";

    public static final String ACTION_UNREAD_CHANGED = "me.leolin.shortcutbadger.BADGE_COUNT_UPDATE";

    public static final String INTENT_EXTRA_BADGE_COUNT = "badge_count";
    // Anhdts khong hieu sao tren 3 btalk bi nhay vao nhanh ung dung thu 3
    private static final String ACTION_UNREAD_CHANGED_O = "android.intent.action.BADGE_COUNT_UPDATE";
    public static final String INTENT_EXTRA_PACKAGENAME = "badge_count_package_name";
    public static final String INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name";

    // Anhdts action bind service
    public static final String ACTION_BIND_DIALER_SERVICE = "bkav.android.BIND_DIALER_SERVICE";
    public static final String MISS_CALL_NUMBER = "miss_call_number";
    private TabLayout mTabLayout;

    private BkavViewPager mViewPager;

    private View mRootView;

    private BtalkPhoneFragment mPhoneFragment;

    private BtalkConversationListFragment mMessagesFragment;

    private BtalkMultiSelectContactsListFragment mContactsFragment;

    private BtalkCallLogFragment mCallLogFragment;

    private String[] mTabTitles;

//    private ViewPagerAdapter mViewPagerAdapter;

    private boolean mIsAccessCallLog = false;

    private BtalkDialerDatabaseHelper mDialerDatabaseHelper;

    private TooltipController mTooltipController;

    private BkavBlurHelper mBkavBlurHelper; // Bkav TrungTH lop ho tro blur

    // AnhNDd nav view
    private NavigationView mNavigationView;

    // QuangNDb
    private DrawerLayout mDrawerLayout;

    private View mDividerTabsView; // Bkav TrungTH view cho ke vach o duoi tabbar khi trung mau

    private boolean mIsLightTab = false; // Bkav TrungTh bo xung bien de set trang thai mau cua tab

    // Anhdts
    private ContactsRequest mRequest;

    private ContactsIntentResolver mIntentResolver;

    private BtalkDataObserver mCallRecordObserver;// Bkav QuangNDb bien lang nghe thay doi csdl cua
    // uri call record

    private SuggestLoaderManager mSmartSuggestLoaderManage;

    private int mTabOpen = -1;

    //HienDTk: ngay tu dong xoa ghi am cuoc goi
    private int mDayAutoDeleteAudio;
    //HienDTk: key tu dong xoa ghi am cuoc goi
    public static final String TIME_AUTO_DELETE_RECODER = "auto_delete_recoder";
    // Bkav HienDTk: thay doi duong dan file ghi am cuoc goi => BOS-1642 - Start
    //    public final static String PATH_RECODER = CompatUtils.isBCY() ? "/storage/emulated/0/Data/CallRecord" : "/storage/emulated/0/BkavData/CallRecord";
    public final static String PATH_RECODER = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recorders/CallRecorder";
    // Bkav HienDTk: thay doi duong dan file ghi am cuoc goi => BOS-1642 - End

    private AsyncTask mAsyncTaskDeleteRecoder;
    // Bkav HienDTk: BOS-3181 - Start
    // Bkav HienDTk: tab duoc chon de thuc hien thao tac
    public static int TAB_SELECT = 0;
    // Bkav HienDTk: BOS-3181 - End

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        mRootView = findViewById(R.id.main_fragment);

        //Bkav QuangNDb cache multi sim truoc de khong loi hien thi suggest popup
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            BtalkCallLogCache.getCallLogCache(getApplicationContext()).checkMutilSim();
        }

        // Bkav TienNAb: Them check quyen CALL_PHONE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PHONE_PERMISSION_REQUEST_CODE);
        }

//        adjustFontScale(getResources().getConfiguration());
        setStatusBarColor(1);
        // Bkav HuyNQN them xu ly setstatusbar mau trang khi o che do chia doi man hinh
        setWindowStatusBarColor();
        WallpaperBlurCompat blurCompat = WallpaperBlurCompat.getInstance(getApplicationContext());
        mBkavBlurHelper = new BkavBlurHelper(this, R.layout.btalk_activity_drawer,
                blurCompat.isConfigBkav());
        // Anhdts neu trong che do da man hinh thi khong set actionBar
        // mBkavBlurHelper.setIsNoActionBar(BkavUiUtils.isModeMultiScreen(this));
        // Anhdts dang ki lang nghe doi man hinh
//        blurCompat.addOnChangeWallpaperListener(this);
        // Anhdts
//        View contentView = mBkavBlurHelper.createView(false);
//        if (WallpaperBlurCompat.getInstance(getApplicationContext()).isConfigBkav()) {
//            contentView.setBackground(
//                    new BitmapDrawable(getResources(), blurCompat.getWallpaperBlur()));
//        }

        // Anhdts bo
        // RelativeLayout content = (RelativeLayout)
        // contentView.findViewById(R.id.content_viewpager);
        // DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) content.getLayoutParams();
        // params.topMargin = mBkavBlurHelper.getStatusBarHeight();

//        mViewPager = (BkavViewPager) findViewById(R.id.viewpager);
//        setupViewPager();
//        mDividerTabsView = findViewById(R.id.divider_tabs);
//        mTabLayout = (TabLayout) findViewById(R.id.tabs);
//        mTabLayout.setupWithViewPager(mViewPager, true);
//        setupTabView();
//        PrefUtils.get().saveBooleanPreferences(this, PrefUtils.KEEP_STATUS_APP, false);// huy keep giao dien message
//        displayFragment(getIntent());
//        initTooltipController();
        // Bkav TrungTh thay icon status bar sang mau xam va doi mau cua status bar
//        BtalkUiUtils.setSystemUiVisibility(mViewPager, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        // AnhNDd:
//        mNavigationView = (NavigationView) findViewById(R.id.btalk_activity_nav_view);

        // QuangNDb
//        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        setUpNavigationView();
//        initDrawerBlurView(mDrawerLayout);
        // ANhdts view hieu ung cho giao dien setting
//        BtalkViewAnchorNav anchorNav = (BtalkViewAnchorNav) findViewById(R.id.anchor_nav);
//        mDrawerLayout.addDrawerListener(anchorNav);
//        initTabBarColor();
//        mCallRecordObserver = new BtalkDataObserver(new Handler());
//        getContentResolver().registerContentObserver(MessagingContentProvider.CALL_RECORD_URI, true,
//                mCallRecordObserver);
//        mCallRecordObserver.setOnChangeListener(this);
//        mSmartSuggestLoaderManage = new SuggestLoaderManager(this);
        // Anhdts fake navigation
//        showFakeNavigationBackground();

//        registerListenAction();
        //HienDTk: lay key trong setting
//        mDayAutoDeleteAudio = Settings.System.getInt(getContentResolver(), TIME_AUTO_DELETE_RECODER, 0);
    }


//    private ActiveDefaultProfileReceiver mActiveDefaultReceiver;

    // Bkav HuyNQN lang nghe su kien vuot home, vuot back va tat man hinh cua nguoi dung
//    private void registerListenAction() {
//        mActiveDefaultReceiver = new ActiveDefaultProfileReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(ActiveDefaultProfileReceiver.ACTION_MOVE_TO_HOME);
//        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
//        intentFilter.addAction(ActiveDefaultProfileReceiver.EVENT_BACK_PRESS_BTALK_ACTIVITY);
//        intentFilter.addAction(ActiveDefaultProfileReceiver.EVENT_BACK_PRESS);
//        // Bkav HuyNQN  BPHONE4-236 end
//        registerReceiver(mActiveDefaultReceiver, intentFilter);
//    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        adjustFontScale(newConfig);
//    }

    /**
     * Bkav QuangNDb hien thi dialog thong bao thieu quen storage de luu ghi am cuoc goi
     */
//    private void showRequestPermissionDialog() {
//        if (checkExistCallRecord()) {
//            if (ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//                    && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                PermissionUtil.get().checkPermission(new String[]{
//                        Manifest.permission.READ_EXTERNAL_STORAGE
//                }, STORAGE_PERMISSION_REQUEST_CODE, this, this);
//            } else {
//                // KHi co quyen roi thi insertmms
//                InsertMMSRecordAction.insertMMSRecordAction();
//            }
//        }
//    }

//    public void adjustFontScale(Configuration configuration) {
//        if (configuration.fontScale > MAX_SIZE_TEXT_SCALE ||
//                (BtalkUiUtils.isModeMultiScreen(this) && configuration.fontScale > 1.1)) {
//
//            if (ChipsUtil.isRunningNOrLater()) {
//                if (BtalkUiUtils.isModeMultiScreen(this)) {
//                    configuration.fontScale = 1.1f;
//                } else {
//                    configuration.fontScale = MAX_SIZE_TEXT_SCALE;
//                }
//            } else {
//                configuration.fontScale = MAX_SIZE_TEXT_SCALE;
//            }
//            // TrungTH tach ham hoan nay
//            DisplayMetrics metrics = getResources().getDisplayMetrics();
//            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
//            wm.getDefaultDisplay().getMetrics(metrics);
//            metrics.scaledDensity = configuration.fontScale * metrics.density;
//            getResources().updateConfiguration(configuration, metrics);
//        }
//    }

//    private void initTooltipController() {
//        if (mTooltipController == null) {
//            mTooltipController = new TooltipController(this);
//        }
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Anhdts khi resume neu trong tab call log thi update cuoc goi nho la da doc hay chua
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
//        showRequestPermissionDialog();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            BtalkCallLogCache.getCallLogCache(getApplicationContext()).checkMutilSim();
            if (BtalkCallLogCache.getCallLogCache(getApplicationContext()).isSimChange()) {
                if (mPhoneFragment != null) {
//                    mPhoneFragment.getDialpadFragment().setSimChange();
                    mSmartSuggestLoaderManage.updateSim();
                }
            }
        }
//        mDialerDatabaseHelper.startSmartDialUpdateThread();
//        if (mPhoneFragment != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                mPhoneFragment.getDialpadFragment().checkConfigTwoButton();
//            }
//        }
        // Bkav QuangNDb them doan nay vao de luon sync du lieu tin nhan neu thay doi, (luc dua
        // source goc sang thieu mat doan nay lam no k cap nhat tin nhan o BMS khi khoi phuc)
//        BugleActivityUtil.onActivityResume(this, BtalkActivity.this);
//        mIsLightTab = PrefUtils.get().loadBooleanPreferences(this, PREF_IS_LIGHT_TAB_BAR, false);
//        if (mSmartSuggestLoaderManage != null) {
//            mSmartSuggestLoaderManage.hideViewSuggest();
//        }
        //Bkav QuangNDb check de update icon call recorder tren launcher la an hay hien
//        BtalkExecutors.runOnBGThread(new Runnable() {
//            @Override
//            public void run() {
//                ShortcutUtils.get().updateCallLogRecordShortcut();
//            }
//        });

//        SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
//        List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
//        for (SubscriptionInfo sub: subscriptionInfos) {
//            List<PhoneAccountHandle> subscriptionAccountHandles =
//                    PhoneAccountUtils.getSubscriptionPhoneAccounts(this.getApplicationContext());
//            Log.d("QuangNDb:", "onResume: " + subscriptionAccountHandles.get(sub.getSimSlotIndex()).getId()+" - "+sub.getSimSlotIndex()+" - "+sub.getSubscriptionId()+" - "+sub.getIccId());
//        }
//        List<PhoneAccountHandle> subscriptionAccountHandles =
//                PhoneAccountUtils.getSubscriptionPhoneAccounts(this.getApplicationContext());
//        for (PhoneAccountHandle phoneAccountHandle :
//                subscriptionAccountHandles) {
//            Log.d("QuangNDb:", "onResume: " + phoneAccountHandle.getId());
//        }


        if (!BtalkPermissionUtil.hasPermissionReadSMS(getApplicationContext())) {
            ActivityCompat.requestPermissions(this, new String[]{READ_SMS}, REQUEST_READ_SMS);
        }

        //HienDTk: xoa file ghi am
//        mAsyncTaskDeleteRecoder = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] objects) {
//                //HienDTk: tu dong xoa file ghi am theo ngay nguoi dung cai dat
//                AutoDelateRecoder.getInstance().getDay(PATH_RECODER, mDayAutoDeleteAudio);
//
//                return null;
//            }
//        };

        // Bkav HienDTk: BOS-3181 - Start
        // Bkav HienDTk: lang nghe su thay doi khi chuyen tu tab
//        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int i, float v, int i1) {
//
//            }
//
//            @Override
//            public void onPageSelected(int i) {
//                TAB_SELECT = i;
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int i) {
//
//            }
//        });
        // Bkav HienDTk:  BOS-3181 - End

        // Bkav HienDTk: fix bug - BOS-2782 - Start
        // Bkav HienDTk: moi lan vao deu lang nghe xem co su thay doi notification dot hay khong
//        DatabaseWrapper db = DataModel.get().getDatabase();
//        SendBroadcastUnreadMessage.sendLocalBroadCast(getApplicationContext(), db);
        // Bkav HienDTk: fix bug - BOS-2782 - End

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Bkav TienNAb: bo update database smartdial vi da update o ham onCreate roi
//        UpdateSmartDialIntentService.startActionUpdate(this.getApplicationContext());

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Bkav QuangNDb restore contact tu db smart dialer trong th nguoi dung bi mat contact
        // TODO: 05/05/2020 99% loi mat danh ba la do cach search bi loi search ư, ơ, ô không ra u, o nen dong tinh nang nay di
//        BtalkExecutors.runOnBGThread(new Runnable() {
//            @Override
//            public void run() {
//                RestoreContactUtils.get().restoreContactFromSmartDialer();
//            }
//        });
    }

    /**
     * Bkav TrungTH: cai dat view cho cac tab
     */
//    private void setupTabView() {
//        View tabPhones = inflateTabView(mTabTitles[TAB_PHONE_INDEX],
//                ContextCompat.getDrawable(this, R.drawable.ic_tab_phones_new));
//        ImageView iconPhone = (ImageView) tabPhones.findViewById(R.id.tabs_icon);
//        TextView textPhone = (TextView) tabPhones.findViewById(R.id.tabs_textview);
//        textPhone.setTextColor(ContextCompat.getColor(BtalkActivity.this,
//                R.color.btalk_ab_text_and_icon_selected_color));
//        iconPhone.setColorFilter(ContextCompat.getColor(BtalkActivity.this,
//                R.color.btalk_ab_text_and_icon_selected_color));
//        mTabLayout.getTabAt(TAB_PHONE_INDEX).setCustomView(tabPhones);
//
//        View tabMessages = inflateTabView(mTabTitles[TAB_MESSAGES_INDEX],
//                ContextCompat.getDrawable(this, R.drawable.ic_tab_messages_new));
//        mTabLayout.getTabAt(TAB_MESSAGES_INDEX).setCustomView(tabMessages);
//
//        View tabRecents = inflateTabView(mTabTitles[TAB_CALLLOG_INDEX],
//                ContextCompat.getDrawable(this, R.drawable.ic_tab_recents_new));
//        mTabLayout.getTabAt(TAB_CALLLOG_INDEX).setCustomView(tabRecents);
//
//        View tabContacts = inflateTabView(mTabTitles[TAB_CONTACTS_INDEX],
//                ContextCompat.getDrawable(this, R.drawable.ic_tab_contacts_new));
//        mTabLayout.getTabAt(TAB_CONTACTS_INDEX).setCustomView(tabContacts);
//
//        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                View selectedView = tab.getCustomView();
//                setColor(selectedView, ContextCompat.getColor(BtalkActivity.this,
//                        R.color.btalk_ab_text_and_icon_selected_color), ContextCompat.getColor(BtalkActivity.this,
//                        R.color.btalk_ab_text_and_icon_selected_color));
//                // Bkav QuangNDb an che do multi edit khi roi tab tin nhan
//                if (tab.getPosition() != TAB_MESSAGES_INDEX) {
//                    if (mMessagesFragment != null && mMessagesFragment.isSelectionMode()) {
//                        mMessagesFragment.exitMultiSelectState();
//                    }
//                }
//                // Anhdts chuyen sang tab danh ba thi hien luon ban phim len
//                if ((mTabOpen != TAB_CONTACTS_INDEX && tab.getPosition() == TAB_CONTACTS_INDEX)) {
//                    if (mContactsFragment != null) {
//                        mContactsFragment.onTabSelected();
//                    }
//                }
//                changeStateFabButton(tab.getPosition(), View.VISIBLE);
//                setBackgroundTabBar(tab.getPosition());
//                setStatusBarColor(tab.getPosition());
//
//                querySmartContact("", null);
//
//                if (PreferenceManager.getDefaultSharedPreferences(BtalkActivity.this)
//                        .getBoolean(BtalkSettingOpenAppFragment.OPTION_KEEP_STATE, true)) {
//                    if ((mTabOpen != TAB_CONTACTS_INDEX || tab.getPosition() != TAB_CONTACTS_INDEX)
//                            &&
//                            (mTabOpen != TAB_MESSAGES_INDEX
//                                    || tab.getPosition() != TAB_MESSAGES_INDEX)) {
//                        PrefUtils.get().saveIntPreferences(BtalkActivity.this, PrefUtils.TAB_CACHE_PHONE,
//                                tab.getPosition());
//                    } else {
//                        mTabOpen = -1;
//                    }
//                }
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//                View unselectedView = tab.getCustomView();
//                setColor(unselectedView, ContextCompat.getColor(BtalkActivity.this,
//                        R.color.color_icon_tablayout), ContextCompat.getColor(BtalkActivity.this,
//                        R.color.color_text_tablayout));
//                changeStateFabButton(tab.getPosition(), View.GONE);
//                if (mCallLogFragment != null && tab.getPosition() == TAB_CALLLOG_INDEX) {
//                    updateMissedCalls();
//                    mCallLogFragment.changeTab();
//                } else if (mContactsFragment != null && tab.getPosition() == TAB_CONTACTS_INDEX) {
//                    mContactsFragment.updateSelectionMode();
//                }
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//                switch (tab.getPosition()) {
//                    case TAB_PHONE_INDEX:
//                        if (mPhoneFragment != null && mPhoneFragment.getDialpadFragment() != null) {
//                            mPhoneFragment.getDialpadFragment().handleDialButtonPressed();
//                        }
//                        break;
//                    case TAB_MESSAGES_INDEX:
//                        if (mMessagesFragment != null) {
//                            mMessagesFragment.doubleClickTab();
//                        }
//                        break;
//                    case TAB_CALLLOG_INDEX:
//                        if (mCallLogFragment != null) {
//                            mCallLogFragment.doubleClickTab();
//                        }
//                        break;
//                    case TAB_CONTACTS_INDEX:
//                        if (mContactsFragment != null) {
//                            mContactsFragment.doubleClickTab();
//                        }
//                }
//            }
//        });
//    }

    /**
     * Bkav QuangNDb change state cua fab button
     */
//    private void changeStateFabButton(int position, int state) {
//        switch (position) {
//            case TAB_MESSAGES_INDEX:
//                if (mMessagesFragment != null) {
//                    BtalkLog.logD("BtalkActivity", "changeStateFabButton: remove notify");
//                    mMessagesFragment.setScrolledToNewestConversationIfNeeded();
//                    mMessagesFragment.setVisibleFabButton(state);
//                    mMessagesFragment.setVisibleFabButtonSmall(state);
//                }
//                break;
//            case TAB_CONTACTS_INDEX:
//                if (mContactsFragment != null) {
//                    mContactsFragment.setVisibleFabButton(state);
//                    mContactsFragment.setVisibleFabButtonSmall(state);
//                }
//                break;
//            case TAB_PHONE_INDEX:
//                if (mPhoneFragment != null) {
//                    mPhoneFragment.setVisibleFabButton(state);
//                }
//                break;
//            case TAB_CALLLOG_INDEX:
//                if (mCallLogFragment != null) {
//                    mCallLogFragment.setVisibleFabButton(state);
//                    mCallLogFragment.setVisibleFabButtonSmall(state);
//                }
//                break;
//        }
//    }

    // Bkav TienNAb: sua lai mau text va icon tablayout
//    private void setColor(View view, int colorIcon, int colorText) {
//        if (view != null) {
//            TextView tabText = (TextView) view.findViewById(R.id.tabs_textview);
//            tabText.setTextColor(colorText);
//            ImageView imageView = (ImageView) view.findViewById(R.id.tabs_icon);
//            imageView.setColorFilter(colorIcon);
//        }
//    }

    /**
     * Bkav TrungTH: custom lai view cua tab thanh dang tren icon duoi text
     *
     * @param text
     * @param icon
     * @return
     */
//    private View inflateTabView(final String text, final Drawable icon) {
//        View view = LayoutInflater.from(this).inflate(R.layout.tab_item_views, null, false);
//        ImageView image = (ImageView) view.findViewById(R.id.tabs_icon);
//        image.setImageDrawable(icon);
//        TextView tv = (TextView) view.findViewById(R.id.tabs_textview);
//        tv.setText(text);
//        return view;
//    }

    /**
     * Bkav TrungTH cai dat cho view pager
     */
//    private void setupViewPager() {
//
//        // Anhdts dang ki trang thai cuoc goi
//        if (mPhoneFragment != null) {
//            mPhoneFragment.getDialpadFragment().registerCallState();
//        }
//
//        // Bkav TrungTh bien dem so luong tab load , va bien xac dinh da load chua
//        mLoadedTabCount = 0;
//        mFinishedLoadTab = false;
//
//        mTabTitles = new String[TAB_COUNT_DEFAULT];
//        mTabTitles[TAB_PHONE_INDEX] = getResources().getString(R.string.title_tab_phone);
//        mTabTitles[TAB_MESSAGES_INDEX] = getResources().getString(R.string.title_tab_messages);
//        mTabTitles[TAB_CALLLOG_INDEX] = getResources().getString(R.string.title_tab_recents_call);
//        mTabTitles[TAB_CONTACTS_INDEX] = getResources().getString(R.string.title_tab_contacts);
//
//        android.app.FragmentManager ft = getFragmentManager();
//        mViewPagerAdapter = new ViewPagerAdapter(this, ft);
//        mViewPager.setAdapter(mViewPagerAdapter);
//        // QuangNDb dong lai
//        // mViewPager.addOnPageChangeListener(mOnPageChangeListener);
//        // based on the current position you can then cast the page to the correct Fragment class
//        // and call some method inside that fragment to reload the data:
//        if (Config.IS_BPHONE) {
//            // Bkav TrungTH : la Bphone thi chay them ham nay
//            mViewPager.enablePager(BkavViewPager.ENABLE_VIEWPAGER);
//            mViewPager.setOffscreenPageLimit(BkavViewPager.TAB_NUMBERS_FIRST_TIME);
//            mViewPager.enableKeepPage(true);
//            mViewPager.enableLoadOneTab(true);
//        } else {
//            mViewPager.setOffscreenPageLimit(TAB_COUNT_DEFAULT);
//        }
//    }

    /**
     * TrungTH doi mau cua status bar fake
     *
     * @param position
     */
    private void setStatusBarColor(int position) {
        if (mBkavBlurHelper != null) {
            // BKav TrungTh Rieng Tab Phone co mau khac
            mBkavBlurHelper.setFakeStatusBarColor(
                    ContextCompat.getColor(BtalkActivity.this, (position == TAB_PHONE_INDEX)
                            ? R.color.btalk_color_for_dialpad : R.color.btalk_white_opacity_bg));
        }
    }

    // Bkav HuyNQN set window statusbar mau trang de khi o che do chia doi man hinh statusbar ko bi doi mau
    private void setWindowStatusBarColor() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.btalk_white_opacity_bg));
    }


    /**
     * TrungTH doi mau cua TabBar tuy tung tab Tab phone thi mau trung voi mau cua ban phim
     *
     * @param position
     */
//    private void setBackgroundTabBar(int position) {
//        mTabLayout.setBackgroundColor(ContextCompat.getColor(BtalkActivity.this,
//                (position == TAB_PHONE_INDEX) ? R.color.btalk_color_for_dialpad
//                        : (mIsLightTab) ? R.color.btalk_white_opacity_bg
//                        : R.color.btalk_actionbar_and_tabbar_bg_color));
//        // Bkav TrungTh an hien mDivider Tab
//        mDividerTabsView.setVisibility(
//                (position != TAB_PHONE_INDEX && mIsLightTab) ? View.VISIBLE : View.GONE);
//
//        View view = findViewById(R.id.navigation_view);
//
//        view.setBackgroundColor(ContextCompat.getColor(BtalkActivity.this,
//                (position == TAB_PHONE_INDEX) ? R.color.btalk_color_for_dialpad
//                        : (mIsLightTab) ? R.color.btalk_white_opacity_bg
//                        : R.color.btalk_actionbar_and_tabbar_bg_color));
//    }

//    @Override
//    public boolean isActionBarShowing() {
//        return mPhoneFragment != null && mPhoneFragment.isActionBarShowing();
//    }

//    @Override
//    public boolean isDialpadShown() {
//        return mPhoneFragment != null && mPhoneFragment.isDialpadShown();
//    }

//    @Override
//    public int getDialpadHeight() {
//        if (mPhoneFragment != null) {
//            return mPhoneFragment.getDialpadHeight();
//        }
//        // return -1 de biet active chua kip khoi tao
//        return -1;
//    }

//    @Override
//    public int getActionBarHideOffset() {
//        if (mPhoneFragment != null)
//            return mPhoneFragment.getActionBarHideOffset();
//        return 0;
//    }

//    @Override
//    public int getActionBarHeight() {
//        if (mPhoneFragment != null)
//            return mPhoneFragment.getActionBarHeight();
//        return 0;
//    }

//    public void hideDialpadFragment(boolean animate, boolean b) {
//        // Toast.makeText(this, "hideDialpadFragment", Toast.LENGTH_SHORT).show();
//    }

    /**
     * Callback from child DialpadFragment when the dialpad is shown.
     */
//    public void onDialpadShown() {
//        if (mPhoneFragment != null)
//            mPhoneFragment.onDialpadShown();
//    }

//    @Override
//    public void onConversationClick(ConversationListData listData,
//                                    ConversationListItemData conversationListItemData, boolean isLongClick,
//                                    ConversationListItemView conversationView) {
//        // Bkav TrungTh - callback cua tab tin nhan => tim huong chuyen vao fragment sau
//    }

//    @Override
//    public void onCreateConversationClick() {
//        // Bkav TrungTh - callback cua tab tin nhan => tim huong chuyen vao fragment sau
//    }

//    @Override
//    public boolean isConversationSelected(String conversationId) {
//        // Bkav TrungTh - callback cua tab tin nhan => tim huong chuyen vao fragment sau
//        return false;
//    }

//    @Override
//    public boolean isSwipeAnimatable() {
//        // Bkav TrungTh - callback cua tab tin nhan => tim huong chuyen vao fragment sau
//        return false;
//    }

//    @Override
//    public boolean isSelectionMode() {
//        // Bkav TrungTh - callback cua tab tin nhan => tim huong chuyen vao fragment sau
//        return false;
//    }

//    @Override
//    public void onListFragmentScrollStateChange(int scrollState) {
//        if (mPhoneFragment != null) {
//            // Anhdts bo di, loi gay ra hien speedDial
//            // mPhoneFragment.onListFragmentScrollStateChange(scrollState);
//        }
//    }

//    @Override
//    public void onListFragmentScroll(int firstVisibleItem, int visibleItemCount,
//                                     int totalItemCount) {
//        if (mPhoneFragment != null) {
//            mPhoneFragment.onListFragmentScroll(firstVisibleItem, visibleItemCount, totalItemCount);
//        }
//    }

//    @Override
//    public boolean onDialpadSpacerTouchWithEmptyQuery() {
//        return mPhoneFragment != null && mPhoneFragment.onDialpadSpacerTouchWithEmptyQuery();
//    }

//    @Override
//    public void setConferenceDialButtonVisibility(boolean enabled) {
//        // Bkav TrungTh - Khong thuc hien
//    }

//    @Override
//    public void setConferenceDialButtonImage(boolean setAddParticipantButton) {
//        // Bkav TrungTh - Khong thuc hien
//    }

    // Anhdts doi man hinh
//    @Override
//    public void onChangeWallpaper() {
//        mBkavBlurHelper.changeWallpaper(
//                WallpaperBlurCompat.getInstance(getApplicationContext()).getWallpaperBlur());
//    }

    /**
     * Anhdts dang ki listener search du lieu va xu ly xong va bind du lieu goi y ra
     */
//    public void setOnQueryCompleteListener(
//            PhoneNumberPickerFragment.OnQueryCompleteListener onQueryCompleteListener) {
//        if (mPhoneFragment != null) {
//            mPhoneFragment.setOnQueryCompleteListener(onQueryCompleteListener);
//        }
//    }

    /**
     * Anhdts thong bao co cuoc goi nho chua doc
     */
//    public void setReadMissCall() {
//        mIsAccessCallLog = true;
//    }

//    @Override
//    public void onChange(Uri uri) {
//        if (MessagingContentProvider.CALL_RECORD_URI.equals(uri)) {// Th csdl cua bang call record
//            // bi thay doi
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                showRequestPermissionDialog();
//            }
//        }
//    }

//    private boolean checkExistCallRecord() {
//        Cursor cursor = null;
//        try {
//            cursor = getContentResolver().query(MessagingContentProvider.CALL_RECORD_URI, null,
//                    null, null, null);
//            if (cursor != null && cursor.getCount() > 0) {
//                return true;
//            }
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return false;
//    }

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 5;
    private static final int CALL_PHONE_PERMISSION_REQUEST_CODE = 6;

    @Override
    public void onRequest() {
        BtalkPermissionUtil.requestStoragePermission(this, STORAGE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case CALL_PHONE_PERMISSION_REQUEST_CODE | STORAGE_PERMISSION_REQUEST_CODE:
                PermissionUtil.get().onActivityPermissionResult(permissions, grantResults, this, this);
                break;
        }
    }

    @Override
    public void denyPermission(String[] pers) {
        finish();
    }

    @Override
    public void acceptPermission(String[] pers) {
        InsertMMSRecordAction.insertMMSRecordAction();
    }

    @Override
    public void alwaysDeny(String[] pers) {
        PermissionUtil.get().showDialogOpenSetting(this, this, pers, STORAGE_PERMISSION_REQUEST_CODE,
                R.string.enable_permission_gallery, R.string.enable_permission_gallery_summary);
    }

    /**
     * Bkav TrungTH: adapter cua viewpager
     */
//    public static class ViewPagerAdapter extends android.support.v13.app.FragmentPagerAdapter {
//
//        private WeakReference<BtalkActivity> mWeakReference;
//
//        public ViewPagerAdapter(BtalkActivity btalkActivity, android.app.FragmentManager fm) {
//            super(fm);
//            mWeakReference = new WeakReference<>(btalkActivity);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public android.app.Fragment getItem(int position) {
//            switch (position) {
//                case TAB_PHONE_INDEX:
//                    mWeakReference.get().mPhoneFragment = BtalkPhoneFragment.newInstance();
//                    // mWeakReference.get().mDialerDatabaseHelper.setOnQueryPhoneSuccessListener(mWeakReference.get().mPhoneFragment);
//                    return mWeakReference.get().mPhoneFragment;
//                case TAB_MESSAGES_INDEX:
//                    // QuangNdb sua lai doan nay de hop voi lop custom extend lai
//                    mWeakReference.get().mMessagesFragment = new BtalkConversationListFragment();
//                    // Bkav QuangNDb chuyen doan set Host xuong duoi ham instantiateItem de khong bi
//                    // bug chet null host
//                    // mWeakReference.get().mMessagesFragment.setHost(mWeakReference.get());
//                    // Log.d(TAG, "getItem: Test");
//                    return mWeakReference.get().mMessagesFragment;
//                case TAB_CALLLOG_INDEX:
//                    mWeakReference.get().mCallLogFragment = new BtalkCallLogFragment(
//                            CallLogQueryHandler.CALL_TYPE_ALL);
//                    return mWeakReference.get().mCallLogFragment;
//                case TAB_CONTACTS_INDEX:
//                    mWeakReference
//                            .get().mContactsFragment = new BtalkMultiSelectContactsListFragment();
//                    return mWeakReference.get().mContactsFragment;
//            }
//            throw new IllegalStateException("No fragment at position " + position);
//        }
//
//        private static final String TAG = "ViewPagerAdapter";
//
//        @Override
//        public android.app.Fragment instantiateItem(ViewGroup container, int position) {
//            // On rotation the FragmentManager handles rotation. Therefore getItem() isn't called.
//            // Copy the fragments that the FragmentManager finds so that we can store them in
//            // instance variables for later.
//            final android.app.Fragment fragment = (android.app.Fragment) super.instantiateItem(
//                    container, position);
//            if (fragment instanceof BtalkPhoneFragment) {
//                // Bkav HienDTk: fix bug - BOS-3305 - Start
//                // Bkav HienDTk: fix bug Fragment already active
//                if(mWeakReference.get().mPhoneFragment != null &&
//                        !mWeakReference.get().mPhoneFragment.isAdded())
//                mWeakReference.get().mPhoneFragment = (BtalkPhoneFragment) fragment;
//                if (mWeakReference.get().mDialerDatabaseHelper != null) {
//                    mWeakReference.get().mDialerDatabaseHelper
//                            .setOnQueryPhoneSuccessListener(mWeakReference.get().mPhoneFragment);
//                }
//                ((BtalkPhoneFragment) fragment).resetValue();
//
//            } else if (fragment instanceof ConversationListFragment
//                    && position == TAB_MESSAGES_INDEX) {
//                if(mWeakReference.get().mMessagesFragment != null &&
//                        !mWeakReference.get().mMessagesFragment.isAdded())
//                mWeakReference.get().mMessagesFragment = (BtalkConversationListFragment) fragment;
//            } else if (fragment instanceof BtalkCallLogFragment && position == TAB_CALLLOG_INDEX) {
//                if(mWeakReference.get().mCallLogFragment != null &&
//                        !mWeakReference.get().mCallLogFragment.isAdded())
//                    // Bkav HienDTk: fix bug - BOS-3305 - End
//                mWeakReference.get().mCallLogFragment = (BtalkCallLogFragment) fragment;
//            } else if (fragment instanceof BtalkMultiSelectContactsListFragment
//                    && position == TAB_CONTACTS_INDEX) {
//                // Bkav TienNAb - Fix bug #BOS-2316 - Start
//                // Check fragment chua duoc add thi moi tao
//                // Bkav HienDTk: Lỗi: Bị thoát app Btalk sau khi thay đổi kích thước phông chữ, kích thước hiển thị => BOS-2907 - Start
//                if(mWeakReference.get().mContactsFragment != null)
//                    // Bkav HienDTk: Lỗi: Bị thoát app Btalk sau khi thay đổi kích thước phông chữ, kích thước hiển thị => BOS-2907 - End
//                if (!mWeakReference.get().mContactsFragment.isAdded()){
//                    mWeakReference
//                            .get().mContactsFragment = (BtalkMultiSelectContactsListFragment) fragment;
//                }
//                // Bkav TienNAb - Fix bug #BOS-2316 - End
//            }
//            return fragment;
//        }
//
//        /**
//         * When {@link android.support.v4.view.PagerAdapter#notifyDataSetChanged} is called, this
//         * method is called on all pages to determine whether they need to be recreated. When the
//         * voicemail tab is removed, the view needs to be recreated by returning POSITION_NONE. If
//         * notifyDataSetChanged is called for some other reason, the voicemail tab is recreated only
//         * if it is active. All other tabs do not need to be recreated and POSITION_UNCHANGED is
//         * returned.
//         */
//        @Override
//        public int getItemPosition(Object object) {
//            return POSITION_UNCHANGED;
//        }
//
//        @Override
//        public int getCount() {
//            return TAB_COUNT_DEFAULT;
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return mWeakReference.get().mTabTitles[position];
//        }
//    }

    // AnhNDd: activity result.

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (mContactsFragment != null) {
//            // AnhNDd: trường hợp xuất danh bạ sẽ gọi vào đây.
//            if (requestCode == ImportExportDialogFragment.SUBACTIVITY_EXPORT_CONTACTS) {
//                if (resultCode == RESULT_OK) {
//                    mContactsFragment.exportContactsToVCF(data);
//                }
//            } else if (requestCode == ImportExportDialogFragment.SUBACTIVITY_MULTI_PICK_CONTACT) {
//                if (resultCode == RESULT_OK) {
//                    mContactsFragment.exportContactsToSIM(data);
//                }
//            }
//
//            // AnhNDd: trường hợp khi lọc danh bạ
//            if (requestCode == BtalkMultiSelectContactsListFragment.SUBACTIVITY_ACCOUNT_FILTER) {
//                mContactsFragment.handleFilterResult(resultCode, data);
//            }
//        }
//    }

//    @Override
//    public void onBackPressed() {
//        if (isTaskRoot()) {
//            //Bkav QuangNDb gui su kien back de update lai profile mac dinh
//            Intent intent = new Intent(ActiveDefaultProfileReceiver.EVENT_BACK_PRESS_BTALK_ACTIVITY);
//            sendBroadcast(intent);
//        }
//
//        // ANhd
//        if (mViewPager.getCurrentItem() == TAB_MESSAGES_INDEX && mMessagesFragment != null
//                && mMessagesFragment.isSearchBarShown()) {
//            mMessagesFragment.hideSearch();
//        } else if (mViewPager.getCurrentItem() == TAB_CONTACTS_INDEX && mContactsFragment != null) {
//            // AnhNDd: Nếu còn cửa sổ search hoac selection thì ẩn nó đi chứ ko thoát ứng dụng.
//            if (mContactsFragment.toolbarIsSearchMode() || mContactsFragment.isSelectionMode()) {
//                mContactsFragment.handleBackPressed();
//            } else {
//                super.onBackPressed();
//            }
//        } else if (mViewPager.getCurrentItem() == TAB_CALLLOG_INDEX) {
//            if (!mCallLogFragment.onBackPress()) {
//                if (!PinnedHeaderListView.isCheckKeyDown) {
//                    super.onBackPressed();
//                } else {
//                    PinnedHeaderListView.isCheckKeyDown = false;
//                }
//            }
//        } else {
//            super.onBackPressed();
//        }
//
//
//    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        displayFragment(intent); // Viec xu ly intent duoc viet trong day
//        setIntent(intent);
//        if (mViewPager.getCurrentItem() == TAB_CONTACTS_INDEX && mContactsFragment != null) {
//            mContactsFragment.configureFromRequest();
//        }
//    }

//    @Override
//    public void onDeletionFinished() {
//        // AnhNDd: xử lý delete contact thành công.
//        if (mContactsFragment != null) {
//            // Bkav TienNAb: update database smartdial
//            mDialerDatabaseHelper.startSmartDialUpdateThread();
//            mContactsFragment.handleStopSelectionMode();
//        }
//    }

    /**
     * Anhdts: Goi hien thi tooltip
     */
//    private void callShowTooltipDialpad() {
//        if (shouldShowTooltip()) {
//            mTooltipController.showMagicPadTooltip();
//        }
//    }

    /**
     * Anhdts: Check cac dieu kien co cho phep hien thi tooltip giua phone hay khong
     */
//    private boolean shouldShowTooltip() {
//        return mTooltipController.getShowTooltip() &&
//                mPhoneFragment != null &&
//                ((mPhoneFragment.getDialpadFragment() != null &&
//                        mPhoneFragment.getDialpadFragment().isDigitsEmpty()) ||
//                        mPhoneFragment.getDialpadFragment() == null)
//                &&
//                mTooltipController != null &&
//                !mTooltipController.isTooltipShowing();
//    }

    /**
     * Anhdts: Check cac dieu kien co cho phep hien thi tooltip message tab hay khong
     */
//    private boolean shouldShowTooltipMessage() {
//        int learnDoubleClickMessageTab = mTooltipController.getTimesUseTooltipMessage();
//        return learnDoubleClickMessageTab < TooltipController.LEARN_DOUBLE_CLICK_MESSAGE_TAB
//                && !mTooltipController.isTooltipMessageShowing();
//    }

    /**
     * Anhdts Check cac dieu kien co cho phep hien thi tooltip contact tab hay khong
     */
//    private boolean shouldShowTooltipContact() {
//        int learnDoubleClickContactTab = mTooltipController.getTimesUseTooltipContact();
//        return learnDoubleClickContactTab < TooltipController.LEARN_DOUBLE_CLICK_CONTACT_TAB
//                && !mTooltipController.isTooltipContactShowing();
//    }

    /**
     * Anhdts: Goi hien thi tooltip top contact
     */
//    private void callShowTooltipTop() {
//        if (shouldShowTooltip()) {
//            if (!mTooltipController.isTooltipTopShowing()) {
//                mTooltipController.showMagicPadTooltipTop();
//            }
//        }
//    }

    /**
     * Anhdts: Goi hien thi tooltip message tab
     */
//    private void callShowTooltipMessageTab() {
//        if (shouldShowTooltipMessage()) {
//            mTooltipController.showMessageTabTooltip();
//        }
//    }

    /**
     * Anhdts: Goi hien thi tooltip contact tab
     */
//    private void callShowTooltipContactTab() {
//        if (shouldShowTooltipContact()) {
//            mTooltipController.showContactTabTooltip();
//        }
//    }

    /**
     * Anhdts: get height tab
     */
    public int getTabHeight() {
        return mTabLayout.getHeight();
    }

    /**
     * Anhdts: get height tab
     */
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (mTooltipController.isTooltipShowing()) {
//            mTooltipController.hideToolTipDialpad();
//        }
//        if (mViewPager != null && mViewPager.getCurrentItem() == TAB_PHONE_INDEX
//                && mPhoneFragment != null) {
//            if (mPhoneFragment.dispatchTouchEvent(ev)) {
//                return true;
//            }
//        }
//        if (mSmartSuggestLoaderManage != null && !mSmartSuggestLoaderManage.isInteractive()) {
//            mSmartSuggestLoaderManage.hideViewSuggest();
//        }
//        try {
//            return super.dispatchTouchEvent(ev);
//        } catch (IllegalArgumentException ex) {
//            ex.printStackTrace();
//            return false;
//        }
//    }

//    private void setUpNavigationView() {
//        // Bkav TienNAb: tam khoa thao tac vuot ngang tu phai qua thi hien thi navigation drawer
//        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//        if (mNavigationView != null) {
//            mNavigationView.setNavigationItemSelectedListener(
//                    new NavigationView.OnNavigationItemSelectedListener() {
//                        @Override
//                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                            switch (item.getItemId()) {
//                                // AnhNDd: khi click vao item sẽ vào phần setting tương ứng
//                                case R.id.nav_setting_phones:
//                                    startActivity(new Intent(getApplicationContext(),
//                                            BtalkDialerSettingsActivity.class));
//                                    item.setChecked(false);
//                                    break;
//                                case R.id.nav_setting_messages:
//                                    startActivity(new Intent(getApplicationContext(),
//                                            BtalkApplicationSettingsActivity.class));
//                                    item.setChecked(false);
//                                    break;
//                                case R.id.nav_setting_contacts:
//                                    startActivity(new Intent(getApplicationContext(),
//                                            BtalkContactsPreferenceActivity.class));
//                                    item.setChecked(false);
//                                    break;
//                                // Bkav QuangNdb them su kien bam vao setting change mau tab
//                                case R.id.nav_setting_tab:
//                                    mIsLightTab = !mIsLightTab;
//                                    switchColorTabBar();
//                                    PrefUtils.get().saveBooleanPreferences(BtalkActivity.this,
//                                            PREF_IS_LIGHT_TAB_BAR, mIsLightTab);
//                                    break;
//                            }
//                            mDrawerLayout.closeDrawers();
//                            return true;
//                        }
//                    });
//        }
//    }

    /**
     * Anhdts hien navigation khi click vao button over flow
     */
//    public void showNavigation() {
//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {
//                mDrawerLayout.openDrawer(mNavigationView);
//            }
//        });
//    }

    // Bkav TrungTH them ham de xu ly sau khi load xong 1 tab thi load tiep cac tab khac
    private int mLoadedTabCount;

    private boolean mFinishedLoadTab;

    private static final int LOAD_MORE_TABS_DELAY = 400;

    private static final int MSG_LOAD_MORE_TABS = 1;

//    private PrivateHandler mPrivateHandler;

//    public void justFinishLoadingTab() {
//        mLoadedTabCount++;
//        // Bkav TrungTH load duoc tab dau thi send notify load them tab
//        if (mLoadedTabCount == 1 || mLoadedTabCount == TAB_COUNT_DEFAULT) {
//            if (!mFinishedLoadTab && mPrivateHandler != null) {
//                mPrivateHandler.sendEmptyMessageDelayed(MSG_LOAD_MORE_TABS, LOAD_MORE_TABS_DELAY);
//                mFinishedLoadTab = true;
//            }
//        }
//    }

    /**
     * Bkav TrungTH Handler xu ly viec
     */
//    private static class PrivateHandler extends Handler {
//
//        private WeakReference<BtalkActivity> mWeakReference;
//
//        PrivateHandler(BtalkActivity btalkActivity) {
//            mWeakReference = new WeakReference<>(btalkActivity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == MSG_LOAD_MORE_TABS) {
//                mWeakReference.get().reloadViewPager();
//            }
//        }
//    }

    /**
     * TrungTH reload lai view sau khi tab dau tien duoc load
     */
//    private void reloadViewPager() {
//        if (Config.IS_BPHONE) {
//            mViewPager.setOffscreenPageLimit(TAB_COUNT_DEFAULT);
//            mViewPager.populate(true); // Bkav TrungTH : la Bphone thi chay them ham nay
//        }
//        if (mBkavBlurHelper != null) {
//            mBkavBlurHelper.startBlur(true); // TrungTh lat co de thuc hien blur
//            setStatusBarColor(mViewPager.getCurrentItem());
//        }
//    }

//    public void setCurrentTab(int item) {
//        if (mViewPager != null) {
//            mViewPager.setCurrentItem(item);
//        }
//    }

    /**
     * Anhdts sua so truoc khi goi
     */
//    public void setActionFixBeforeCall(String phoneNumber) {
//        if (mPhoneFragment != null) {
//            mPhoneFragment.setNumberDigitsActionFix(phoneNumber);
//        }
//    }

    /**
     * Anhdts update tin call log chua doc Neu dang o tab CallLog thi xac nhan da doc nhung chua
     * thay doi gi khi chuyen tab khac thi doi view thanh trang thai da doc
     */
//    private void updateMissedCalls() {
//        // TODO: 25/03/2020 quangndb fix tam loi badge icon misscall tren icon goi dien khong het
////        if (mIsAccessCallLog) {
//        if (mCallLogFragment != null) {
//            mCallLogFragment.markMissedCallsAsReadAndRemoveNotifications();
//        }
//        updateMissCallNotificationLauncher();
////            mIsAccessCallLog = false;
////        }
//    }

    /**
     * Anhdts cap nhat cuoc goi nho
     */
//    @Override
//    protected void onPause() {
//        if (mViewPager.getCurrentItem() == TAB_CALLLOG_INDEX) {
//            updateMissedCalls();
//        }
//        // TrungTh khi giao dien pause dong popup search lai
//        if (mSmartSuggestLoaderManage != null) {
//            mSmartSuggestLoaderManage.hideViewSuggest();
//        }
//        super.onPause();
//    }

    /**
     * Anhdts xoa listener change background
     */
//    @Override
//    public void onDestroy() {
//
//        super.onDestroy();
//        WallpaperBlurCompat.getInstance(this).removeOnChangeWallpaperListener(this);
//        mTabLayout.clearOnTabSelectedListeners();
//        mTabLayout.removeAllTabs();
//        mViewPager.clearOnPageChangeListeners();
//
//        if (mContactsFragment != null) {
//            mContactsFragment.unRegisterReceiver();
//        }
//        if (mPhoneFragment != null) {
//            mPhoneFragment.getDialpadFragment().unRegisterCallState(this);
//        }
//
//        // Anhdts remove observer
//        if (mCallRecordObserver != null) {
//            getContentResolver().unregisterContentObserver(mCallRecordObserver);
//            mCallRecordObserver = null;
//        }
//        if (mActiveDefaultReceiver != null) {
//            unregisterReceiver(mActiveDefaultReceiver);
//        }
//        //HienDTk: dung aysnstask delete recoder
//        if (mAsyncTaskDeleteRecoder != null && !mAsyncTaskDeleteRecoder.isCancelled()) {
//            mAsyncTaskDeleteRecoder.cancel(true);
//        }
//
//    }

    /**
     * TrungTH them ham xu ly blur view nhung khong duoc
     *
     * @param drawerLayout
     */
//    private void initDrawerBlurView(@NonNull final DrawerLayout drawerLayout) {
//        BlurSupport.addTo(drawerLayout, mBkavBlurHelper.getBlurringView(),
//                R.color.btalk_blur_setting);
//        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
//            @Override
//            public void onDrawerStateChanged(int newState) {
//                // if(newState == DrawerLayout.STATE_DRAGGING){
//                // mBkavBlurHelper.setFakeStatusBarColor(ContextCompat.getColor(BtalkActivity.this,
//                // R.color.btalk_drawer_background_color));
//                // }
//                super.onDrawerStateChanged(newState);
//            }
//
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                // mBkavBlurHelper.setFakeStatusBarColor(ContextCompat.getColor(BtalkActivity.this,
//                // R.color.btalk_drawer_background_color));
//                supportInvalidateOptionsMenu();
//            }
//
//            @Override
//            public void onDrawerClosed(View drawerView) {
//                // setStatusBarColor(mViewPager.getCurrentItem());
//                supportInvalidateOptionsMenu();
//            }
//        });
//
//        drawerLayout.setScrimColor(ContextCompat.getColor(this, R.color.bg_glass));
//    }

    // bkav TrungTh them xu ly ve view dialIntent
    private static final String ACTION_TOUCH_DIALER = "com.android.phone.action.TOUCH_DIALER";

    /**
     * Returns true if the given intent contains a phone number to populate the dialer with
     */
//    private boolean isDialIntent(Intent intent) {
//        final String action = intent.getAction();
//
//        if ((Intent.ACTION_DIAL + "_BKAV").equals(action) || ACTION_TOUCH_DIALER.equals(action)) {
//            return true;
//        }
//        // Bkav TienNAb: sua action Intent.VIEW thanh ACTION_DIAL_BKAV_VIEW
//        if (ACTION_DIAL_BKAV_VIEW.equals(action)) {
//            final Uri data = intent.getData();
//            if (data != null && PhoneAccount.SCHEME_TEL.equals(data.getScheme())) {
//                return true;
//            }
//        }
//
//        if (ACTION_FIX_BEFORE_CALL.equals(action)) {
//            if (intent.getExtras() != null) {
//                setActionFixBeforeCall(intent.getExtras().getString(ARGUMENT_NUMBER));
//            }
//            return true;
//        }
//
//        // Vao tu icon phone
//        String classname = getIntent().getComponent().getClassName();
//        return Intent.ACTION_MAIN.equals(action) && classname != null
//                && classname.equals("bkav.android.btalk.activities.BtalkActivity");
//
//    }

    /**
     * Sets the current tab based on the intent's request type Bkav TrungTH: Them ham xu ly khi vao
     * tu icon nao thi ra tab day
     *
     * @param intent Intent that contains information about which tab should be selected
     */

//    private void displayFragment(Intent intent) {
//        // BKav TrungTH, xu ly ttruong hop vao tu notify goi nho
//        if (CallLog.Calls.CONTENT_TYPE.equals(intent.getType())) {
//            // Externally specified extras take precedence to EXTRA_SHOW_TAB, which is only
//            // used internally.
//            final Bundle extras = intent.getExtras();
//            if (extras != null
//                    && extras.getInt(
//                    CallLog.Calls.EXTRA_CALL_TYPE_FILTER) == CallLog.Calls.VOICEMAIL_TYPE) {
//                // Bkav TrungTH , hien ko co VoiceMail
//            } else {
//                mViewPager.setCurrentItem(TAB_CALLLOG_INDEX);
//                return;
//            }
//        }
//        int tabIndex;
//        String action = intent.getAction();
//        if (MESSAGE_ACTION.equalsIgnoreCase(action)) {
//            // Bkav QuangNDb them ham xu ly notification message khi nhan vao
//            tabIndex = TAB_MESSAGES_INDEX;
//            mTabOpen = TAB_MESSAGES_INDEX;
//            // Bkav QuangNDb chi mo thread khi click vao shortcut
//            if (intent.getBooleanExtra(BtalkMessageActivity.IS_MESSAGE_SHORT_CUT, false)) {
//                checkOpenConversation();
//            }
//        } else if (ACTION_SHOW_CALL_LOG.equals(action)) {
//            tabIndex = TAB_CALLLOG_INDEX;
//        } else if (isDialIntent(intent)) {
//            if (hasMissCalled()) {
//                tabIndex = TAB_CALLLOG_INDEX;
//            } else if (Intent.ACTION_MAIN.equals(action)) {
//                mTabOpen = TAB_PHONE_INDEX;
//                tabIndex = TAB_PHONE_INDEX;
//                if (PreferenceManager.getDefaultSharedPreferences(this)
//                        .getBoolean(BtalkSettingOpenAppFragment.OPTION_KEEP_STATE, true)) {
//                    tabIndex = PrefUtils.get().loadIntPreferences(this, PrefUtils.TAB_CACHE_PHONE, 0);
//                    if (tabIndex == TAB_CONTACTS_INDEX) {
//                        processIntentContacts(intent, false);
//                    }
//                }
//            } else {
//                tabIndex = TAB_PHONE_INDEX;
//                if (mPhoneFragment != null)
//                    mPhoneFragment.setStartedFromNewIntent(true);
//            }
//        } else if (processIntentContacts(intent, false)) {
//            mTabOpen = TAB_CONTACTS_INDEX;
//            tabIndex = TAB_CONTACTS_INDEX;
//        } else {
//            tabIndex = TAB_MESSAGES_INDEX;
//        }
//        if (tabIndex == TAB_PHONE_INDEX) {
//            setBackgroundTabBar(TAB_PHONE_INDEX);
//            setStatusBarColor(TAB_PHONE_INDEX);
//        }
//        mViewPager.setCurrentItem(tabIndex, false);
//        TAB_SELECT = tabIndex;
//        DialerUtils.sendBroadcastCount(getApplicationContext(), DialerUtils.convertTabSelectForOpen(tabIndex), 1);
//
//    }

    /**
     * Bkav QuangNDb mo conversation neu dang o pause khoi 1 conversation
     */

//    private void checkOpenConversation() {
//        if (PrefUtils.get().loadBooleanPreferences(this, PrefUtils.KEEP_STATUS_APP, false)) {
//            long deltaTime = System.currentTimeMillis() - PrefUtils.get().loadLongPreferences(this,
//                    PrefUtils.TIME_PAUSE_APP, System.currentTimeMillis());
//            if (convertMillisToMinutes(deltaTime) <= 15) {
//                final String conversationId = PrefUtils.get().loadStringPreferences(this,
//                        PrefUtils.CONVERSATION_ID, "-1");
//                UIIntents.get().launchConversationActivity(
//                        this, conversationId, null,
//                        null,
//                        false);
//            }
//        }
//    }

    /**
     * Bkav QuangNDb Chuyen doi thoi gian tu ms -> phut
     */
//    private long convertMillisToMinutes(long time) {
//        return time / 60000;
//    }

    /**
     * Khoi tao mau cho tab luc moi vao , get lai setting va set
     */
//    private void initTabBarColor() {
//        mIsLightTab = PrefUtils.get().loadBooleanPreferences(this, PREF_IS_LIGHT_TAB_BAR, false);
//        switchColorTabBar();
//    }

    /**
     * TrungTH doi mau cua Tab
     */
//    private void switchColorTabBar() {
//        mDividerTabsView.setVisibility(mIsLightTab ? View.VISIBLE : View.GONE);
//        setBackgroundTabBar(mViewPager.getCurrentItem());
//    }

    /**
     * Anhdts cap nhat so cuoc goi nho = 0 khi vuot home
     */
//    private void updateMissCallNotificationLauncher() {
//        int unRead = 0;
//        Intent intentUnread = new Intent();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            intentUnread.setAction(ACTION_UNREAD_CHANGED_O);
//            intentUnread.putExtra(INTENT_EXTRA_BADGE_COUNT, unRead);
//            intentUnread.putExtra(INTENT_EXTRA_PACKAGENAME, "bkav.android.btalk");
//            intentUnread.putExtra(INTENT_EXTRA_ACTIVITY_NAME, getComponentName().getClassName());
//            intentUnread.setClassName("bkav.android.launcher3", "com.android.launcher3.bkav.BkavUnreadReceive");
//        } else {
//            intentUnread.setAction(ACTION_UNREAD_CHANGED);
//            intentUnread.putExtra(INTENT_EXTRA_BADGE_COUNT, unRead);
//            intentUnread.putExtra(INTENT_EXTRA_PACKAGENAME, "bkav.android.btalk");
//            intentUnread.putExtra(INTENT_EXTRA_ACTIVITY_NAME, getComponentName().getClassName());
//            intentUnread.setClassName("bkav.android.launcher3", "com.android.launcher3.bkav.BkavUnreadReceive");
//        }
//        sendBroadcast(intentUnread);
//    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus && mMessagesFragment != null
//                && (mTabLayout.getSelectedTabPosition() == TAB_MESSAGES_INDEX)) {
//            BtalkLog.logD("BtalkActivity", "onWindowFocusChanged: remove notify");
//            mMessagesFragment.setScrolledToNewestConversationIfNeeded();
//        }
//        if (hasFocus) {
//            try {
//                Intent intent = new Intent();
//                intent.setAction(ACTION_BIND_DIALER_SERVICE);
//                intent.setComponent(
//                        new ComponentName("com.android.dialer",
//                                "com.android.incallui.customizebkav.receiver.BkavReceiver"));
//                sendBroadcast(intent);
//            } catch (Exception ignore) {
//            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                // Anhdts neu cua so phia tren thi hien fake status bar
//                if (isInMultiWindowMode()) {
//                    int[] rect = new int[2];
//                    findViewById(R.id.root_view).getLocationOnScreen(rect);
//                    DisplayMetrics metrics = new DisplayMetrics();
//
//                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
//                    // phia duoi thi khong hien status bar, phia tren thi bo view navigation bar
//                    if (rect[1] > metrics.heightPixels / 2) {
//                        findViewById(R.id.fake_statusBar).setVisibility(View.GONE);
//                        mBkavBlurHelper.setIsNoActionBar(true);
//                        mBkavBlurHelper.addFlagNoLimitsAndShowFakeView();
//                    }
//                } else {
//                    showFakeNavigationBackground();
//                }
//            }
//        }
//    }

    // Anhdts
//    private void showFakeNavigationBackground() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            if (hasNavigationBar() && !isInMultiWindowMode()) {
//                View view = findViewById(R.id.navigation_view);
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view
//                        .getLayoutParams();
//                params.height = getHeightOfNavigationBar();
//            } else {
//                View view = findViewById(R.id.navigation_view);
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view
//                        .getLayoutParams();
//                params.height = 0;
//            }
//        }
//    }

    /**
     * Anhdts
     *
     * @return true neu co cuoc goi nho
     */
//    private boolean hasMissCalled() {
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            // ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            // int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return false;
//        }
//        // Bkav TienNAb: Them Type MISSED_IMS_TYPE cua sim volte de truy van cuoc goi nho khi dung sim volte
//        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI,
//                new String[]{
//                        CallLog.Calls._ID
//                },
//                "(" + CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE
//                        + " or " + CallLog.Calls.TYPE + " = " + AppCompatConstants.MISSED_IMS_TYPE + ")"
//                        + " and " + CallLog.Calls.IS_READ + " = 0 ",
//                null,
//                CallLog.Calls._ID + " LIMIT 1");
//        if (cursor != null) {
//            if (cursor.getCount() > 0) {
//                cursor.close();
//                return true;
//            }
//            cursor.close();
//        }
//        return false;
//    }

    /**
     * Anhdts cac intent ben tab danh ba
     */
//    public boolean processIntentContacts(Intent intent, boolean forNewIntent) {
//        mRequest = mIntentResolver.resolveIntent(intent);
//        if (Log.isLoggable("", Log.DEBUG)) {
//            Log.d("", this + " processIntent: forNewIntent=" + forNewIntent
//                    + " intent=" + intent + " request=" + mRequest);
//        }
//        if (!mRequest.isValid()) {
//            setResult(RESULT_CANCELED);
//            return false;
//        }
//
//        if (mRequest.getActionCode() == ContactsRequest.ACTION_VIEW_CONTACT) {
//            final Intent intentGo = ImplicitIntentsUtil.composeQuickContactIntent(
//                    mRequest.getContactUri(), QuickContactActivity.MODE_FULLY_EXPANDED);
//            intentGo.putExtra(QuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
//                    ScreenEvent.ScreenType.UNKNOWN);
//            ImplicitIntentsUtil.startActivityInApp(this, intentGo);
//            return false;
//        }
//        // Anhdts vao search luon khi vao tab danh ba
//        if (mRequest.getActionCode() != ContactsRequest.ACTION_STARRED
//                && mRequest.getActionCode() != ContactsRequest.ACTION_FREQUENT) {
//            mRequest.setSearchMode(true);
//        }
//        return true;
//    }

//    public ContactsRequest getRequest() {
//        return mRequest;
//    }

    /**
     * Bkav QuangNDb Add connect phone Bkav
     */
//    private void addConnectPhone(Context context) {
//        String displayName;
//        String[] numbers;
//        if (Config.isMyanmar()) {
//            displayName = TelephoneExchangeUtils.BKAV_SUPPORT_MY;
//            numbers = new String[]{TelephoneExchangeUtils.BKAV_NUMBER_MY};
//        } else {
//            // Bkav TienNAb: sua text cho phu hop voi ban BCY
//            displayName = CompatUtils.isBCY() ? "CSKH" : "Bkav CSKH";
//
//            //Bkav QuangNDb bo "1900545499",  di
//            numbers = new String[]{"02473050069", "02473050050", "1800545448", "02862966626"};
//        }
//        ContactUtils.get().addConnectPhone(context, displayName, numbers);
//    }

//    private void removeOldConnectPhone(Context context) {
//        String displayName;
//        // Bkav TienNAb: sua text cho phu hop voi ban BCY
//        displayName = CompatUtils.isBCY() ? "CSKH" : "Bkav CSKH";
//        String number = "1900545499";
//        ContactUtils.get().removeOldConnectPhone(context, displayName, number);
//    }


    /**
     * Bkav QuangNDb doi mau status bar khi bat che do action mode
     */
    public void setStatusbarOnActionMode() {
        BtalkUiUtils.resetSystemUiVisibility(mViewPager);
        if (mBkavBlurHelper != null) {
            mBkavBlurHelper.setFakeStatusBarColor(
                    ContextCompat.getColor(BtalkActivity.this, R.color.action_mode_color));
        }
    }

    /**
     * Bkav QuangNDb doi mau status bar khi bat che do action mode
     */
    public void exitActionMode() {
        if (mBkavBlurHelper != null) {
            mBkavBlurHelper.setFakeStatusBarColor(
                    ContextCompat.getColor(BtalkActivity.this, R.color.btalk_white_opacity_bg));
        }
        BtalkUiUtils.setSystemUiVisibility(mRootView, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    /**
     * Bkav TienNAb doi mau status bar khi bat che do action mode trong message
     */
    public void setStatusbarOnActionModeMessage() {
        BtalkUiUtils.resetSystemUiVisibility(mRootView);
        if (mBkavBlurHelper != null) {
            mBkavBlurHelper.setFakeStatusBarColor(
                    ContextCompat.getColor(BtalkActivity.this, R.color.action_mode_message_color));
        }
    }

//    public void querySmartContact(String query, SuggestPopup.ActionSmartSuggest listener) {
//        if (mSmartSuggestLoaderManage != null) {
//            mSmartSuggestLoaderManage.startLoad(query, false, listener);
//        }
//    }

    public SuggestLoaderManager getSmartSuggestLoaderManage() {
        return mSmartSuggestLoaderManage;
    }

    private static final String ACTION_SIM_CHANGE = "android.intent.action.SIM_STATE_CHANGED";

    /**
     * Anhdts broadcast thay doi sim, neu thay doi thi clear cache
     */
//    public static class BroadcastSimChange extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action != null && action.equals(ACTION_SIM_CHANGE)) {
//                BtalkCallLogCache.getCallLogCache(context).checkMutilSim();
//                BtalkCallLogCache.getCallLogCache(context).setSimChange();
//                BtalkCallLogCache.getCallLogCache(context).clearSimIconCache();
//            }
//        }
//    }

    private boolean navigationBarIsVisible() {
        Display d = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth > displayWidth) || (realHeight > displayHeight);
    }

    // Bkav HuyNQN su dung check NavigationBar
    @SuppressLint("PrivateApi")
    public static boolean hasNavigationBar() {
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            IBinder serviceBinder = (IBinder) serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
            Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
            Object windowManagerService = stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
            Method hasNavigationBar;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar", int.class);
                return (boolean) hasNavigationBar.invoke(windowManagerService, 0);
            } else {
                hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar");
                return (boolean) hasNavigationBar.invoke(windowManagerService);
            }
        } catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return true;
        }
    }

    private int getHeightOfNavigationBar() {
        int navigationBarHeight = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }

    // Bkav TienNAb: ham dong appbar layout
    public static void collapseAppBarLayout(AppBarLayout appBarLayout, RecyclerView recyclerView) {
        appBarLayout.setExpanded(false, true);
        // Bkav TienNAb: chan scroll appbar layout khi scroll recyclerview
        recyclerView.setNestedScrollingEnabled(false);
        // Bkav TienNAb: chan scroll appbar layout khi vuot tu thanh toolbar
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if (behavior != null) {
            behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    return false;
                }
            });
        }
    }

    // Bkav TienNAb: ham mo appbar layout
    public static void expandAppBarLayout(AppBarLayout appBarLayout, RecyclerView recyclerView) {
//        appBarLayout.setExpanded(true, true);
        // Bkav TienNAb: bo chan scroll appbar layout khi scroll recyclerview
        recyclerView.setNestedScrollingEnabled(true);
        // Bkav TienNAb: bo chan scroll appbar layout khi vuot tu thanh toolbar
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        //Bkav ToanNTe fix BOS-3694
        if (behavior != null) {
            behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    return true;
                }
            });
        }
    }

    // Bkav TienNAb: ham check co phai la android Q hay khong
    public static boolean isAndroidQ() {
        return PermissionsUtil.isAndroidQ();
    }

    // Bkav TienNAb: delay hien thi thanh fastsroll theo CT chốt, để ẩn ban đầu để khi mở app ko nhìn thấy thanh scoller
    public static void delayVisibleFastscroll(Context context, FastScroller fastScroller, int itemCount, int itemShow) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (itemCount > itemShow && fastScroller.getVisibility() != View.VISIBLE) {
                    fastScroller.startAnimation(AnimationUtils.loadAnimation(context, R.anim.animation_show_fastscroll));
                    fastScroller.setVisibility(View.VISIBLE);
                } else if (itemCount == 0) {
                    fastScroller.setVisibility(View.GONE);
                }
            }
        }, 1000);
    }

    // Bkav TienNAb - Fix bug BOS-2997 - Start
    // ham xu ly dong mo appbar layout khi chuyen giua che do fullscreen va chia doi man hinh
    public static void appbarLayoutWithInMultiWindowMode(Activity activity,
                                                         AppBarLayout appBarLayout,
                                                         RecyclerView recyclerView){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Bkav HienDTk: fix loi - BOS-3341 - Start
                if ( activity != null && activity.isInMultiWindowMode()){
                    // Bkav HienDTk: fix loi - BOS-3341 - End
                    collapseAppBarLayout(appBarLayout, recyclerView);
                } else {
                    expandAppBarLayout(appBarLayout, recyclerView);
                }
            }
        },200);
    }
    // Bkav TienNAb - Fix bug BOS-2997 - End
}
