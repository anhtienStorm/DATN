package bkav.android.btalk.contacts;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.Trace;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.activities.ActionBarAdapter;
import com.android.contacts.activities.PopupList;
import com.android.contacts.common.CallUtil;
import com.android.contacts.common.Experiments;
import com.android.contacts.common.MoreContactUtils;
import com.android.contacts.common.SimContactsConstants;
import com.android.contacts.common.compat.ContactsCompat;
import com.android.contacts.common.interactions.ImportExportDialogFragment;
import com.android.contacts.common.list.ContactListFilter;
import com.android.contacts.common.list.ContactListFilterController;
import com.android.contacts.common.list.ContactTileAdapter;
import com.android.contacts.common.list.DefaultContactListAdapter;
import com.android.contacts.common.list.DirectoryListLoader;
import com.android.contacts.common.list.ProfileAndContactsLoader;
import com.android.contacts.common.logging.ScreenEvent;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.Contact;
import com.android.contacts.common.model.ContactLoader;
import com.android.contacts.common.model.RawContact;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.account.SimAccountType;
import com.android.contacts.common.model.dataitem.DataItem;
import com.android.contacts.common.model.dataitem.DataKind;
import com.android.contacts.common.model.dataitem.PhoneDataItem;
import com.android.contacts.common.preference.ContactsPreferences;
import com.android.contacts.common.util.AccountFilterUtil;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.common.util.MaterialColorMapUtils;
import com.android.contacts.common.vcard.VCardCommonArguments;
import com.android.contacts.common.widget.FloatingActionButtonController;
import com.android.contacts.commonbind.experiments.Flags;
import com.android.contacts.editor.EditorIntents;
import com.android.contacts.interactions.ContactMultiDeletionInteraction;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.ContactsUnavailableFragment;
import com.android.contacts.list.MultiSelectContactsListFragment;
import com.android.contacts.list.OnContactsUnavailableActionListener;
import com.android.contacts.list.ProviderStatusWatcher;
import com.android.contacts.quickcontact.QuickContactActivity;
import com.android.contacts.quickcontact.QuickContactActivity.Cp2DataCardModel;
import com.android.dialer.calllog.FastScroller;
import com.android.dialer.database.DialerDatabaseHelper;
import com.android.dialer.util.DialerUtils;
import com.android.messaging.Factory;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.UiUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.messaging.BtalkFactoryImpl;
import bkav.android.btalk.suggestmagic.SuggestPopup;
import bkav.android.btalk.utility.BtalkUiUtils;
import bkav.android.btalk.utility.BtalkConst;
import bkav.android.btalk.utility.ContactUtils;

import static com.android.contacts.common.interactions.ImportExportDialogFragment.MAX_COUNT_ALLOW_SHARE_CONTACT;
import static com.android.messaging.datamodel.DatabaseHelper.PartColumns.TEXT_SEARCH;

/**
 * AnhNDd: class kế thừa từ MultiSelectContactsListFragment là một fragment chứa danh sách các contact
 * dùng để custom adapter.
 */
public class BtalkMultiSelectContactsListFragment extends MultiSelectContactsListFragment implements BtalkFrameToolAdapter.Listener,
        ContactListFilterController.ContactListFilterListener, View.OnClickListener, BtalkFrameToolBarBottomAdapter.SelectionContactListener, ProviderStatusWatcher.ProviderStatusListener, SuggestPopup.ActionSmartSuggest, BtalkContactsListAdapter.OnBtalkItemListener, BtalkContactsListAdapter.SelectedContactsListener {

    // These values needs to start at 2. See {@link ContactEntryListFragment}.
    public static final int SUBACTIVITY_ACCOUNT_FILTER = 2;

    // Bkav HaiKH: Thêm các giá trị để kiểm tra cho việc update âm báo tin nhắn .
    public static final String UPDATE_RINGTONE_KEY = "update";
    public static final String UPDATE_RINGTONE_VALUE = "ringtone";
    public static final String NO_UPDATE_RINGTONE_VALUE = "no_ringtone";

    // Bkav TienNAb: id loader danh ba
    private static final int LOADER_CONTACT = 1234;

    //AnhNDd: Framelayout giống toolbar.
    private BtalkFrameToolAdapter mBtalkFrameToolAdapter;

    private TextView mTitleTab; // Bkav TienNAb: them tieu de cho tab danh ba

    private AppBarLayout mAppBarLayout; // Bkav TienNAb: them appbar layout

    private CollapsingToolbarLayout mCollapsingToolbarLayout; // Bkav TienNAb: them collapsing toolbar layout

    private ImageView mImageBackgroundExpandLayout; // Bkav TienNAb: them hinh icon lon khi appbar layout mo rong

    private ImageView mAddContactExpand; // Bkav TienNAb: them nut them lien he moi khi appbar layout mo rong

    private ImageView mFavoriteExpand; // Bkav TienNAb: them nut danh sach yeu thich khi appbar layout mo rong

    boolean mActionUp = true;// Bkav TienNAb: bien check action up cua recyclerview va appbar layout

    private ImageButton mFloatingActionButton;// Bkav TienNAb: nut tim kiem lon

    private ImageButton mFloatingActionButtonSmall;// Bkav TienNAb: nut tim kiem nho

    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private BtalkContactsListAdapter mContactAdapter;
    private ContactsPreferences mContactsPrefs;
    private ContactListFilter mFilter;
    // Bkav TienNAb: them bien fastscroll
    private FastScroller mFastScroller;

    private LinearLayout mEmptyRecyclerView;  // Bkav TienNAb: view hien thi khi recyclerview rong

    private int mDisplayOrder;
    private int mSortOrder;

    private OnCheckBoxActionListener mCheckBoxActionListener;

    //AnhNDd: Framelayout chua toolbar o bottom
    private BtalkFrameToolBarBottomAdapter mBtalkFrameToolBarBottomAdapter;

    //AnhNDd: Framelayout chứa tab yêu thích.
    private BtalkFavoritesFrameLayout mBtalkFavoritesFrameLayout;

    //AnhNDd: framelayout chưa tab all contac.
    private FrameLayout mBtalkContentMain;

    //AnhNDd: bộ lọc danh bạ hiển thị.
    private ContactListFilterController mContactListFilterController;

    //AnhNDd: listener lắng nghe sự kiện click ở bên tab yêu thích.
    private BtalkFavoritesFrameLayout.Listener mFavoritesFragmentListener =
            new StrequentContactListFragmentListener();

    //AnhNDd: frame chứa nút thêm mới liên hệ.
    private FrameLayout mFloatingActionButtonContainer;

    private FloatingActionButtonController mFloatingActionButtonController;

    private boolean mWasLastFabAnimationScaleIn = false;

    private static final int REQUEST_CODE_CONTACT_EDITOR_ACTIVITY = 1;

    //AnhNDd: projection cho viec query so dien thoai
    private static final String[] PHONE_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.NUMBER,              // 0
            ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
    };

    // Bkav TienNAb: them cac hang so de query contact
    private static final String[] CONTACT_PROJECTION_PRIMARY = !BtalkActivity.isAndroidQ() ?
            new String[]{
                    ContactsContract.Contacts._ID,                           // 0
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,          // 1
                    ContactsContract.Contacts.CONTACT_PRESENCE,              // 2
                    ContactsContract.Contacts.CONTACT_STATUS,                // 3
                    ContactsContract.Contacts.PHOTO_ID,                      // 4
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,           // 5
                    ContactsContract.Contacts.LOOKUP_KEY,                    // 6
                    ContactsContract.Contacts.IS_USER_PROFILE,               // 7
                    ContactsContract.Contacts.PHONETIC_NAME,                 // 8
                    ContactsContract.RawContacts.ACCOUNT_TYPE,               // 9
                    ContactsContract.RawContacts.ACCOUNT_NAME,               // 10
            } : new String[]{
            ContactsContract.Contacts._ID,                           // 0
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,          // 1
            ContactsContract.Contacts.CONTACT_PRESENCE,              // 2
            ContactsContract.Contacts.CONTACT_STATUS,                // 3
            ContactsContract.Contacts.PHOTO_ID,                      // 4
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,           // 5
            ContactsContract.Contacts.LOOKUP_KEY,                    // 6
            ContactsContract.Contacts.IS_USER_PROFILE,               // 7
            ContactsContract.Contacts.PHONETIC_NAME,                 // 8
//            ContactsContract.RawContacts.ACCOUNT_TYPE,               // 9
//            ContactsContract.RawContacts.ACCOUNT_NAME,               // 10
    };

    private static final String[] FILTER_PROJECTION_PRIMARY = !BtalkActivity.isAndroidQ() ?
            new String[]{
                    ContactsContract.Contacts._ID,                           // 0
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,          // 1
                    ContactsContract.Contacts.CONTACT_PRESENCE,              // 2
                    ContactsContract.Contacts.CONTACT_STATUS,                // 3
                    ContactsContract.Contacts.PHOTO_ID,                      // 4
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,           // 5
                    ContactsContract.Contacts.LOOKUP_KEY,                    // 6
                    ContactsContract.Contacts.IS_USER_PROFILE,               // 7
                    ContactsContract.Contacts.PHONETIC_NAME,                 // 8
                    ContactsContract.RawContacts.ACCOUNT_TYPE,               // 9
                    ContactsContract.RawContacts.ACCOUNT_NAME,               // 10
                    ContactsContract.Contacts.LAST_TIME_CONTACTED,           // 11
                    ContactsContract.Contacts.STARRED,                       // 12
                    ContactsContract.SearchSnippets.SNIPPET,                 // 13
            } : new String[]{
            ContactsContract.Contacts._ID,                           // 0
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,          // 1
            ContactsContract.Contacts.CONTACT_PRESENCE,              // 2
            ContactsContract.Contacts.CONTACT_STATUS,                // 3
            ContactsContract.Contacts.PHOTO_ID,                      // 4
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,           // 5
            ContactsContract.Contacts.LOOKUP_KEY,                    // 6
            ContactsContract.Contacts.IS_USER_PROFILE,               // 7
            ContactsContract.Contacts.PHONETIC_NAME,                 // 8
//            ContactsContract.RawContacts.ACCOUNT_TYPE,               // 9
//            ContactsContract.RawContacts.ACCOUNT_NAME,               // 10
            ContactsContract.Contacts.LAST_TIME_CONTACTED,           // 11
            ContactsContract.Contacts.STARRED,                       // 12
            ContactsContract.SearchSnippets.SNIPPET,                 // 13
    };

    private static final String[] CONTACT_PROJECTION_ALTERNATIVE = !BtalkActivity.isAndroidQ() ?
            new String[]{
                    ContactsContract.Contacts._ID,                           // 0
                    ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE,      // 1
                    ContactsContract.Contacts.CONTACT_PRESENCE,              // 2
                    ContactsContract.Contacts.CONTACT_STATUS,                // 3
                    ContactsContract.Contacts.PHOTO_ID,                      // 4
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,           // 5
                    ContactsContract.Contacts.LOOKUP_KEY,                    // 6
                    ContactsContract.Contacts.IS_USER_PROFILE,               // 7
                    ContactsContract.Contacts.PHONETIC_NAME,                 // 8
                    ContactsContract.RawContacts.ACCOUNT_TYPE,               // 9
                    ContactsContract.RawContacts.ACCOUNT_NAME,               // 10
            } : new String[]{
            ContactsContract.Contacts._ID,                           // 0
            ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE,      // 1
            ContactsContract.Contacts.CONTACT_PRESENCE,              // 2
            ContactsContract.Contacts.CONTACT_STATUS,                // 3
            ContactsContract.Contacts.PHOTO_ID,                      // 4
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,           // 5
            ContactsContract.Contacts.LOOKUP_KEY,                    // 6
            ContactsContract.Contacts.IS_USER_PROFILE,               // 7
            ContactsContract.Contacts.PHONETIC_NAME,                 // 8
//            ContactsContract.RawContacts.ACCOUNT_TYPE,               // 9
//            ContactsContract.RawContacts.ACCOUNT_NAME,               // 10
    };

    private static final String[] FILTER_PROJECTION_ALTERNATIVE = !BtalkActivity.isAndroidQ() ?
            new String[]{
                    ContactsContract.Contacts._ID,                           // 0
                    ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE,      // 1
                    ContactsContract.Contacts.CONTACT_PRESENCE,              // 2
                    ContactsContract.Contacts.CONTACT_STATUS,                // 3
                    ContactsContract.Contacts.PHOTO_ID,                      // 4
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,           // 5
                    ContactsContract.Contacts.LOOKUP_KEY,                    // 6
                    ContactsContract.Contacts.IS_USER_PROFILE,               // 7
                    ContactsContract.Contacts.PHONETIC_NAME,                 // 8
                    ContactsContract.RawContacts.ACCOUNT_TYPE,               // 9
                    ContactsContract.RawContacts.ACCOUNT_NAME,               // 10
                    ContactsContract.Contacts.LAST_TIME_CONTACTED,           // 11
                    ContactsContract.Contacts.STARRED,                       // 12
                    ContactsContract.SearchSnippets.SNIPPET,                 // 13
            } : new String[]{
            ContactsContract.Contacts._ID,                           // 0
            ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE,      // 1
            ContactsContract.Contacts.CONTACT_PRESENCE,              // 2
            ContactsContract.Contacts.CONTACT_STATUS,                // 3
            ContactsContract.Contacts.PHOTO_ID,                      // 4
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,           // 5
            ContactsContract.Contacts.LOOKUP_KEY,                    // 6
            ContactsContract.Contacts.IS_USER_PROFILE,               // 7
            ContactsContract.Contacts.PHONETIC_NAME,                 // 8
//            ContactsContract.RawContacts.ACCOUNT_TYPE,               // 9
//            ContactsContract.RawContacts.ACCOUNT_NAME,               // 10
            ContactsContract.Contacts.LAST_TIME_CONTACTED,           // 11
            ContactsContract.Contacts.STARRED,                       // 12
            ContactsContract.SearchSnippets.SNIPPET,                 // 13
    };

    // Contacts contacted within the last 3 days (in seconds)
    private static final long LAST_TIME_USED_3_DAYS_SEC = 3L * 24 * 60 * 60;

    // Contacts contacted within the last 7 days (in seconds)
    private static final long LAST_TIME_USED_7_DAYS_SEC = 7L * 24 * 60 * 60;

    // Contacts contacted within the last 14 days (in seconds)
    private static final long LAST_TIME_USED_14_DAYS_SEC = 14L * 24 * 60 * 60;

    // Contacts contacted within the last 30 days (in seconds)
    private static final long LAST_TIME_USED_30_DAYS_SEC = 30L * 24 * 60 * 60;

    private static final String TIME_SINCE_LAST_USED_SEC =
            "(strftime('%s', 'now') - " + ContactsContract.Contacts.LAST_TIME_CONTACTED + "/1000)";

    private static final String STREQUENT_SORT =
            "(CASE WHEN " + TIME_SINCE_LAST_USED_SEC + " < " + LAST_TIME_USED_3_DAYS_SEC +
                    " THEN 0 " +
                    " WHEN " + TIME_SINCE_LAST_USED_SEC + " < " + LAST_TIME_USED_7_DAYS_SEC +
                    " THEN 1 " +
                    " WHEN " + TIME_SINCE_LAST_USED_SEC + " < " + LAST_TIME_USED_14_DAYS_SEC +
                    " THEN 2 " +
                    " WHEN " + TIME_SINCE_LAST_USED_SEC + " < " + LAST_TIME_USED_30_DAYS_SEC +
                    " THEN 3 " +
                    " ELSE 4 END), " +
                    ContactsContract.Contacts.TIMES_CONTACTED + " DESC, " +
                    ContactsContract.Contacts.STARRED + " DESC";


    //AnhNDd: fragment khi không có số điện thoại nào.
    private ContactsUnavailableFragment mContactsUnavailableFragment;

    private ProviderStatusWatcher mProviderStatusWatcher;

    private Integer mProviderStatus;

    //AnhNDd: mảng các mầu cho contacts.
    private TypedArray mColors;

    private int mDefaultColor;

    // Anhdts check xem da load xong view chuwa
    private boolean mIsLoadComplete = false;

//    private static final String TEXT_SEARCH = "text_search";

    private String mTextSearch;

    public interface ContactsFragmentListener {
        /**
         * AnhNDd: thông báo cần load lại view contact
         */
        void reloadContactsFragment();
    }

    // TODO: we need to refactor the export code in future release.
    // QRD enhancement: contacts list for multi contact pick
    private ArrayList<String[]> mContactList;

    private BroadcastReceiver mExportToSimCompleteListener = null;

    private ImportExportDialogFragment.ExportToSimThread mExportThread = null;

    private TreeSet<Long> mSaveContactIdsDelete = new TreeSet<>();

    private static final String ACTION_SEND_REPORT_BUG = "bkav.com.android.ACTION_SEND_REPORT_BUG";

    @Override
    public DefaultContactListAdapter createMultiSelectEntryContactListAdapter(Context context) {
        if (BtalkContactsActivity.USE_BTALK) {
            return new BtalkMultiSelectEntryContactListAdapter(context);
        } else {
            return super.createMultiSelectEntryContactListAdapter(context);
        }
    }

    /**
     * Bkav QuangNDb them bien activity de change color status bar
     */
    private BtalkActivity mBtalkActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof BtalkActivity) {
            mBtalkActivity = (BtalkActivity) getActivity();
        }
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        if (childFragment instanceof ContactsUnavailableFragment) {
            mContactsUnavailableFragment = (ContactsUnavailableFragment) childFragment;
            mContactsUnavailableFragment.setOnContactsUnavailableActionListener(
                    new ContactsUnavailableFragmentListener());
        }
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setOnContactListActionListener(new ContactBrowserActionListener());

        mContactListFilterController = ContactListFilterController.getInstance(getActivity().getApplicationContext());
        mContactListFilterController.checkFilterValidity(false);
        mContactListFilterController.addListener(this);

        setCheckBoxListListener(new CheckListener());
        setCheckBoxActionListListener(new CheckBoxListener());

        mProviderStatusWatcher = ProviderStatusWatcher.getInstance(getActivity().getApplicationContext());
        mProviderStatusWatcher.addListener(this);

        if (mColors == null) {
            mColors = getResources().obtainTypedArray(R.array.letter_tile_colors);
            mDefaultColor = getResources().getColor(R.color.letter_tile_default_color);
        }

        //HienDTk: lay text search
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mTextSearch = intent.getStringExtra(TEXT_SEARCH);
        }


        registerReceiver();

        // Bkav TienNAb: lang nghe khi cai dat hien thi danh ba thay doi
        mContactsPrefs = new ContactsPreferences(mContext);
        mContactsPrefs.registerChangeListener(mPreferencesChangeListener);
        if (mContactsPrefs != null) {
            mDisplayOrder = mContactsPrefs.getDisplayOrder();
            mSortOrder = mContactsPrefs.getSortOrder();
        }
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
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        //return super.inflateView(inflater, container);
        //AnhNDd: View có framelayout tool.
        View view = inflater.inflate(R.layout.btalk_contact_list_content, null);
        mFloatingActionButton
                = (ImageButton) view.findViewById(R.id.floating_action_button);
        mFloatingActionButton.setOnClickListener(this);

        mFloatingActionButtonSmall = view.findViewById(R.id.floating_action_button_small);
        mFloatingActionButtonSmall.setOnClickListener(this);

        mFloatingActionButtonContainer = (FrameLayout) view.findViewById(R.id.floating_action_button_container);
        mFloatingActionButtonController = new FloatingActionButtonController(getActivity(),
                mFloatingActionButtonContainer, mFloatingActionButton);
        return view;
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);

        //AnhNDd: view của mục toàn bộ danh sách.
        mBtalkContentMain = (FrameLayout) getView().findViewById(R.id.btalk_content_main);

        //AnhNDd: view của mục danh sách contact yêu thích.
        mBtalkFavoritesFrameLayout = new BtalkFavoritesFrameLayout(getContext(), (FrameLayout) getView().findViewById(R.id.btalk_content_favorites));
        mBtalkFavoritesFrameLayout.setDisplayType(ContactTileAdapter.DisplayType.STARRED_ONLY); // Bkav HuyNQN thuc hien chi load len danh sach contact duoc danh dau sao
        mBtalkFavoritesFrameLayout.setListener(mFavoritesFragmentListener);

        //AnhNDd: View giong toolbar
        mBtalkFrameToolAdapter = new BtalkFrameToolAdapter(getContext(), (FrameLayout) getView().findViewById(R.id.btalk_frame_tool), getView(), this);

        mTitleTab = getView().findViewById(R.id.title_tab_contact);
        mAppBarLayout = getView().findViewById(R.id.app_bar_layout);
        mCollapsingToolbarLayout = getView().findViewById(R.id.collapsing_layout);
        mImageBackgroundExpandLayout = getView().findViewById(R.id.img_background_expand_layout);
        mAddContactExpand = getView().findViewById(R.id.bt_add_expand);
        mFavoriteExpand = getView().findViewById(R.id.bt_favorite_expand);

        // Bkav TienNAb: xu ly khi click vao icon add contact khi mo appbar layout
        mAddContactExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewContact();
            }
        });
        // Bkav TienNAb: xu ly khi click vao icon favorite khi mo appbar layout
        mFavoriteExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBtalkFrameToolAdapter.isFavoriteMode()) {
                    showFavorites();
                } else {
                    closeFavorites();
                }
            }
        });

        //AnhNDd: view toolbar o bottom
        mBtalkFrameToolBarBottomAdapter = new BtalkFrameToolBarBottomAdapter(getActivity(), (FrameLayout) getView().findViewById(R.id.btalk_frame_tool_bar_bottom), this);
        initializeFabVisibility();
        // Anhdts
        configureFromRequest();

        // Bkav TienNAb: tam thoi dong appbar layout cua tab danh ba
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                collapseAppBarLayout();
//            }
//        }, 200);

        mFastScroller = getView().findViewById(R.id.fastscroller);
        mRecyclerView = getView().findViewById(R.id.recycler_view_contact);
        mGridLayoutManager = new GridLayoutManager(getActivity(), 1) {
            @Override
            public void onLayoutCompleted(final RecyclerView.State state) {
                super.onLayoutCompleted(state);
                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
                final int lastVisibleItemPosition = findLastVisibleItemPosition();
                int itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1;
                mBtalkActivity.delayVisibleFastscroll(getContext(), mFastScroller, mContactAdapter.getItemCount(), itemsShown);

            }
        };
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        // Bkav TienNAb: gan fastscroll vao recyclerview
        mFastScroller.setRecyclerView(mRecyclerView);
        // Bkav TienNAb: tao giao dien cho thanh fastscroll
        mFastScroller.setViewsToUse(R.layout.calllog_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);

        mContactAdapter = new BtalkContactsListAdapter(getContext(), null);
        mRecyclerView.setAdapter(mContactAdapter);
        startLoader();

        mEmptyRecyclerView = getView().findViewById(R.id.recycler_view_empty);

        updateAppBarLayout(getView());

        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mActionUp = false;
                        if (mBtalkFrameToolAdapter.isSearchMode()) {
                            hideKeyBoard();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mActionUp = false;
                        if (mBtalkFrameToolAdapter.isSearchMode()) {
                            hideKeyBoard();
                        }
                        break;
                }
                return false;
            }
        });

        // Bkav TienNAb - Fix bug BOS-2997 - Start
        // xuly dong mo appbar layout
        mBtalkActivity.appbarLayoutWithInMultiWindowMode(mBtalkActivity, mAppBarLayout, mRecyclerView);
        // Bkav TienNAb - Fix bug BOS-2997 - End
    }

    // Bkav TienNAb: them ham an ban phim khi vuot tim danh ba
    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();
        if (view == null) {
            view = new View(getActivity());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void startLoader() {
        getLoaderManager().initLoader(LOADER_CONTACT, null, mLoaderCallbacks);
    }

    public ContactListFilter getFilter() {
        return mContactListFilterController.getFilter();
    }

    private final LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            if (i == LOADER_CONTACT) {

                CursorLoader loader = new BtalkProfileAndContactsLoader(getContext());
                if (loader instanceof ProfileAndContactsLoader) {
                    ((ProfileAndContactsLoader) loader).setLoadProfile(shouldIncludeProfile());
                }

                mFilter = getFilter();
                String sortOrder = null;
                if (mBtalkFrameToolAdapter.isSearchMode() && !TextUtils.isEmpty(mBtalkFrameToolAdapter.getQueryString())) {
                    final Flags flags = Flags.getInstance(mContext);
                    String query = mBtalkFrameToolAdapter.getQueryString();
                    if (query == null) query = "";
                    query = query.trim();

                    //AnhNDd: custom.
                    setUpToQuerySTREQUENT(loader, query, 0);

                    if (TextUtils.isEmpty(query)) {
                        // Regardless of the directory, we don't want anything returned,
                        // so let's just send a "nothing" query to the local directory.
                        //loader.setUri(Contacts.CONTENT_URI);

                        //AnhNDd: custom
                        loader.setUri(appendUriBuildSectionIndexer(ContactsContract.Contacts.CONTENT_URI));

                        loader.setProjection(getProjection(false));
                        loader.setSelection("0");
                    } else {
                        final Uri.Builder builder = ContactsCompat.getContentUri().buildUpon();
                        appendSearchParameters(builder, query, 0);
                        //loader.setUri(builder.build());

                        //AnhNDd: custom
                        loader.setUri(appendUriBuildSectionIndexer(builder.build()));

                        if (mFilter != null && mFilter.filterType == ContactListFilter.FILTER_TYPE_ACCOUNT
                                && !("com.skype.raider").equals(mFilter.accountType)) {
                            if (!BtalkActivity.isAndroidQ()) {
                                // Bkav HienDTk: fix loi: Danh bạ - BOS 8.6 - Lỗi: Tìm kiếm được liên hệ lưu tại SIM 1 khi để chế độ Danh bạ hiển thị tại SIM 2 => BOS-2449 - Start
                                loader.setSelection(ContactsContract.RawContacts.ACCOUNT_TYPE + " = '" + mFilter.accountType + "' AND " + ContactsContract.RawContacts.ACCOUNT_NAME  + " = '" + mFilter.accountName +"'");
                                // Bkav HienDTk: fix loi: Danh bạ - BOS 8.6 - Lỗi: Tìm kiếm được liên hệ lưu tại SIM 1 khi để chế độ Danh bạ hiển thị tại SIM 2 => BOS-2449 - End
                            }
                        } else {
                            configureSelection(loader, ContactsContract.Directory.DEFAULT, mFilter);
                        }

                        loader.setProjection(getProjection(true));
                        if (flags.getBoolean(Experiments.FLAG_SEARCH_STREQUENTS_FIRST, false)) {
                            sortOrder = STREQUENT_SORT;
                        }
                    }

                    if (!BtalkActivity.isAndroidQ()) {
                        if (null != mFilter
                                && mFilter.filterType == ContactListFilter.FILTER_TYPE_ALL_WITHOUT_SIM) {
                            appendUriQueryParameterWithoutSim(loader,
                                    ContactsContract.RawContacts.ACCOUNT_TYPE, SimAccountType.ACCOUNT_TYPE);
                        }
                    }
                } else {

                    configureUri(loader, 0, mFilter);
                    loader.setProjection(getProjection(false));
                    configureSelection(loader, 0, mFilter);
                }

                if (getContactSortOrder() == ContactsPreferences.SORT_ORDER_PRIMARY) {
                    if (sortOrder == null) {
                        sortOrder = ContactsContract.Contacts.SORT_KEY_PRIMARY;
                    } else {
                        sortOrder += ", " + ContactsContract.Contacts.SORT_KEY_PRIMARY;
                    }
                } else {
                    if (sortOrder == null) {
                        sortOrder = ContactsContract.Contacts.SORT_KEY_ALTERNATIVE;
                    } else {
                        sortOrder += ", " + ContactsContract.Contacts.SORT_KEY_ALTERNATIVE;
                    }
                }
                loader.setSortOrder(sortOrder);

                return loader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            // Bkav TienNAb: neu danh ba rong thi hien thi empty view
            if (cursor.getCount() == 0) {
                mEmptyRecyclerView.setVisibility(View.VISIBLE);
            } else {
                mEmptyRecyclerView.setVisibility(View.GONE);
            }

            mContactAdapter.setSearchMode(mBtalkFrameToolAdapter.isSearchMode() && !TextUtils.isEmpty(mBtalkFrameToolAdapter.getQueryString()));
            mContactAdapter.changeCursor(cursor);
            mContactAdapter.setListener(new ContactListActionListener());
            mContactAdapter.setItemListener(BtalkMultiSelectContactsListFragment.this);
            mContactAdapter.setSelectedContactsListener(BtalkMultiSelectContactsListFragment.this);
            mContactAdapter.setDisplayOrder(getContactDisplayOrder());
            // Bkav TienNAb: an search view khi nhan vao icon tin nhan
            mContactAdapter.setOnclickMessageButton(new BtalkContactsListAdapter.IOnClickMessageButton() {
                @Override
                public void onClickMessageButton() {
                    if (toolbarIsSearchMode()) {
                        mBtalkFrameToolAdapter.searchBackButtonPressed();
                    }
                }
            });
            mContactAdapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mContactAdapter.changeCursor(null);
            mContactAdapter.notifyDataSetChanged();
        }
    };

    protected final String[] getProjection(boolean forSearch) {
        if (forSearch) {
            if (getContactDisplayOrder() == ContactsPreferences.DISPLAY_ORDER_PRIMARY) {
                return FILTER_PROJECTION_PRIMARY;
            } else {
                return FILTER_PROJECTION_ALTERNATIVE;
            }
        } else {
            if (getContactDisplayOrder() == ContactsPreferences.DISPLAY_ORDER_PRIMARY) {
                return CONTACT_PROJECTION_PRIMARY;
            } else {
                return CONTACT_PROJECTION_ALTERNATIVE;
            }
        }
    }

    public void setUpToQuerySTREQUENT(CursorLoader loader, String query, long directoryId) {
        if (loader instanceof BtalkProfileAndContactsLoader) {
            BtalkProfileAndContactsLoader btalkProfileAndContactsLoader = (BtalkProfileAndContactsLoader) loader;
            btalkProfileAndContactsLoader.setUpToSearch(query, directoryId, mBtalkFrameToolAdapter.isSearchMode());
        }
    }

    public Uri appendUriBuildSectionIndexer(Uri uri) {
        return uri.buildUpon()
                .appendQueryParameter(ContactsContract.Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true").build();
    }

    private void appendSearchParameters(Uri.Builder builder, String query, long directoryId) {
        builder.appendPath(query); // Builder will encode the query
        builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
                String.valueOf(directoryId));
//        if (directoryId != ContactsContract.Directory.DEFAULT && directoryId != ContactsContract.Directory.LOCAL_INVISIBLE) {
//            builder.appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY,
//                    String.valueOf(getDirectoryResultLimit(getDirectoryById(directoryId))));
//        }
        builder.appendQueryParameter(ContactsContract.SearchSnippets.DEFERRED_SNIPPETING_KEY, "1");
    }

    protected void configureUri(CursorLoader loader, long directoryId, ContactListFilter filter) {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
//        if (filter != null && filter.filterType == ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
//            String lookupKey = getSelectedContactLookupKey();
//            if (lookupKey != null) {
//                uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
//            } else {
//                uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, getSelectedContactId());
//            }
//        }

        if (directoryId == ContactsContract.Directory.DEFAULT && isSectionHeaderDisplayEnabled()) {
            uri = uri.buildUpon()
                    .appendQueryParameter(ContactsContract.Contacts.EXTRA_ADDRESS_BOOK_INDEX, "true").build();
        }

        // The "All accounts" filter is the same as the entire contents of Directory.DEFAULT
        if (filter != null
                && filter.filterType != ContactListFilter.FILTER_TYPE_CUSTOM
                && filter.filterType != ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
            final Uri.Builder builder = uri.buildUpon();
            builder.appendQueryParameter(
                    ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(ContactsContract.Directory.DEFAULT));
            if (filter.filterType == ContactListFilter.FILTER_TYPE_ACCOUNT) {
                filter.addAccountQueryParameterToUrl(builder);
            }
            uri = builder.build();
        }

        loader.setUri(uri);
    }

    private void configureSelection(
            CursorLoader loader, long directoryId, ContactListFilter filter) {
        if (filter == null) {
            return;
        }

        if (directoryId != ContactsContract.Directory.DEFAULT) {
            return;
        }

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<String>();

        switch (filter.filterType) {
            case ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS: {
                // We have already added directory=0 to the URI, which takes care of this
                // filter
                // Anhdts tam thoi bo hien thi cac danh ba skype di trong tab danh ba
                if (!BtalkActivity.isAndroidQ()) {
                    selection.append(ContactsContract.RawContacts.ACCOUNT_TYPE + " != 'com.skype.raider' or " + ContactsContract.RawContacts.ACCOUNT_TYPE + " is null ");
                }
                break;
            }
            case ContactListFilter.FILTER_TYPE_SINGLE_CONTACT: {
                // We have already added the lookup key to the URI, which takes care of this
                // filter
                break;
            }
            case ContactListFilter.FILTER_TYPE_STARRED: {
                selection.append(ContactsContract.Contacts.STARRED + "!=0");
                break;
            }
            case ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY: {
                selection.append(ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1");
                break;
            }
            case ContactListFilter.FILTER_TYPE_CUSTOM: {
                selection.append(ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1");
                if (isCustomFilterForPhoneNumbersOnly()) {
                    selection.append(" AND " + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1");
                }
                // Do not show contacts in SIM card when airplane mode is on
                break;
            }
            case ContactListFilter.FILTER_TYPE_ACCOUNT: {
                // We use query parameters for account filter, so no selection to add here.
                break;
            }
            case ContactListFilter.FILTER_TYPE_ALL_WITHOUT_SIM: {
                if (!BtalkActivity.isAndroidQ()) {
                    appendUriQueryParameterWithoutSim(loader, ContactsContract.RawContacts.ACCOUNT_TYPE,
                            SimAccountType.ACCOUNT_TYPE);
                }
                break;
            }
            case ContactListFilter.FILTER_TYPE_CAN_SAVE_EMAIL: {
                String emailFilter = MoreContactUtils.getSimFilter(mContext);
                if (!TextUtils.isEmpty(emailFilter)) {
                    appendUriQueryParameterWithoutSim(
                            loader, ContactsContract.RawContacts.ACCOUNT_NAME, emailFilter);
                }
                break;
            }
        }
        loader.setSelection(selection.toString());
        loader.setSelectionArgs(selectionArgs.toArray(new String[0]));
    }

    // Bkav TienNAb: check neu dang o search mode thi khong hien thi contact user
    private boolean shouldIncludeProfile() {
        if (mBtalkFrameToolAdapter.isSearchMode()) {
            if (TextUtils.isEmpty(mBtalkFrameToolAdapter.getQueryString())) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    // Bkav TienNAb: xu ly khi cai dat hien thi danh ba thay doi
    private ContactsPreferences.ChangeListener mPreferencesChangeListener =
            new ContactsPreferences.ChangeListener() {
                @Override
                public void onChange() {
                    loadContactPreferences();
                    reStartContactLoader();
                }
            };

    // Bkav TienNAb: doc cac cai dat hien thi danh ba
    protected boolean loadContactPreferences() {
        boolean changed = false;
        if (getContactDisplayOrder() != mContactsPrefs.getDisplayOrder()) {
            setContactDisplayOrder(mContactsPrefs.getDisplayOrder());
            changed = true;
        }

        if (getContactSortOrder() != mContactsPrefs.getSortOrder()) {
            setContactSortOrder(mContactsPrefs.getSortOrder());
            changed = true;
        }

        return changed;
    }

    private int getContactDisplayOrder() {
        return mDisplayOrder;
    }

    private int getContactSortOrder() {
        return mSortOrder;
    }

    private void setContactDisplayOrder(int displayOrder) {
        mDisplayOrder = displayOrder;
    }

    private void setContactSortOrder(int sortOrder) {
        mSortOrder = sortOrder;
    }

    private boolean isCustomFilterForPhoneNumbersOnly() {
        // TODO: this flag should not be stored in shared prefs.  It needs to be in the db.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getBoolean(ContactsPreferences.PREF_DISPLAY_ONLY_PHONES,
                ContactsPreferences.PREF_DISPLAY_ONLY_PHONES_DEFAULT);
    }

    private void appendUriQueryParameterWithoutSim(CursorLoader loader, String key, String value) {
        if (null == loader || null == key || null == value) {
            return;
        }

        Uri uri = loader.getUri();
        if (null != uri) {
            uri = uri.buildUpon().appendQueryParameter(key, value)
                    // Anhdts tren Android 8 khong dung duoc type FILTER_TYPE_ALL_WITHOUT_SIM
                    // .appendQueryParameter(SimContactsConstants.WITHOUT_SIM_FLAG, "true")
                    .build();
            loader.setUri(uri);
        }
    }

    /**
     * Xử lý các sự kiện thay đổi trên toolbar.
     *
     * @param action
     */
    @Override
    public void onAction(int action) {
        switch (action) {
            case BtalkFrameToolAdapter.Listener.Action.CHANGE_SEARCH_QUERY:
                final String queryString = mBtalkFrameToolAdapter.getQueryString();
                setQueryString(queryString, true);
                setVisibleScrollbarEnabled(!isSearchMode());
                mContactAdapter.setQueryString(queryString);
                reStartContactLoader();
                mBtalkActivity.querySmartContact(mBtalkFrameToolAdapter.getQueryString(), this);
                break;
            case BtalkFrameToolAdapter.Listener.Action.STOP_SEARCH_AND_SELECTION_MODE:
                setQueryString("", true);
                setVisibleScrollbarEnabled(!isSearchMode());
                showFabWithAnimation(true);
                mContactAdapter.setQueryString("");
                reStartContactLoader();
                mBtalkActivity.querySmartContact("", this);
                break;
        }
    }

    public void reStartContactLoader() {
        getLoaderManager().restartLoader(LOADER_CONTACT, null, mLoaderCallbacks);
    }

    @Override
    public void onUpButtonPressed() {
        //AnhNDd: Xử lý khi thoát tìm kiếm.
        mFloatingActionButtonContainer.setVisibility(View.VISIBLE);
    }

    //AnhNDd: Xử lý việc selection
    @Override
    public void onActionSeclection(int action) {
        switch (action) {
            case BtalkFrameToolBarBottomAdapter.SelectionContactListener.Action.STOP_SELECTION_MODE:
                handleStopSelectionMode();
                handleStopSelectMode();
                break;
        }
    }

    /**
     * AnhNDd: xử lý khi stop selection mode
     */
    public void handleStopSelectionMode() {
        displayCheckBoxes(false);
        mBtalkActivity.exitActionMode();//Bkav QuangNDb reset tro ve khong action mode
        // Anhdts doi lai mau action bar
        setSelectionMode(false);
        mBtalkFrameToolBarBottomAdapter.setSelectionMode(false);
        getView().findViewById(R.id.btalk_frame_tool).setVisibility(View.VISIBLE);
        showFabWithAnimation(true);
    }

    // Bkav TienNAb: xu ly khi stop selection mode
    public void handleStopSelectMode() {
        displayCheckBoxesContactItem(false);
        mBtalkActivity.exitActionMode();//Bkav QuangNDb reset tro ve khong action mode
        // Anhdts doi lai mau action bar
        setSelectionMode(false);
        mBtalkFrameToolBarBottomAdapter.setSelectionMode(false);
        getView().findViewById(R.id.btalk_frame_tool).setVisibility(View.VISIBLE);
        showFabWithAnimation(true);
    }

    @Override
    public void onPopupItemClick(boolean selectAll) {
        setSelectAll(selectAll);
        if (!selectAll) {
            handleStopSelectionMode();
            handleStopSelectMode();
        }
    }

    // Bkav TienNAb: chon tat ca hoac bo chon tat ca
    public void setSelectAll(boolean selectAll) {
        if (selectAll) {
            // Bkav HienDTk: chon tat ca thi an ban phim
            clearTextSearch();
            fillCheckBoxesContact();
        } else {
            clearCheckBoxesContact();
        }
        mContactAdapter.notifyDataSetChanged();
    }

    @Override
    public BtalkMultiSelectEntryContactListAdapter getAdapter() {
        //AnhNDd: setlistener
        BtalkMultiSelectEntryContactListAdapter adapter = (BtalkMultiSelectEntryContactListAdapter) super.getAdapter();
        //HienDTk: bat su kien bam vao message button
        adapter.setOnclickMessageButton(new BtalkMultiSelectEntryContactListAdapter.IOnClickMessageButton() {
            @Override
            public void onClickMessageButton() {
                if (toolbarIsSearchMode()) {
                    //HienDTk: cho an thanh search view
                    mBtalkFrameToolAdapter.searchBackButtonPressed();
                }
            }
        });
        adapter.setListener(new ContactActionListener());
        return adapter;
    }

//    @Override
//    public void viewContactAndCall(Uri contactLookupUri, boolean isEnterpriseContact) {
//        super.setSelectedContactUri(contactLookupUri, false, false, true, false);
//        if (mListener != null) {
//            BtalkOnContactBrowserActionListener listener = (BtalkOnContactBrowserActionListener) mListener;
//            listener.showPhoneNumberOrCall(contactLookupUri, isEnterpriseContact);
//        }
//    }

    //AnhNDd: Thực hiện cuộc gọi đến 1 số.
    public void callAction(String phoneNumber) {
//        Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
//        startActivity(in);

        // Anhdts doi intent, vi du la 1 so dang tra ma thi khong dung intent tren duoc
        final Intent intent = CallUtil.getCallIntent(phoneNumber);
        intent.putExtra("com.android.phone.force.slot", true);
        intent.putExtra("Cdma_Supp", true);
        DialerUtils.startActivityWithErrorToast(mBtalkActivity == null ? getContext() : mBtalkActivity, intent);
    }

    //AnhNDd: Hiển thị dialog chứa danh sách các số điện thoại của 1 liên hệ.
    public void showDialogChosePhoneNumber(List<DataItem> phoneDataItems) {
        if (isSavedInstanceStateDone) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            BtalkDialogChosePhone dialogChosePhone = BtalkDialogChosePhone.newInstance(0);
            dialogChosePhone.setDialogCallback(mIDialogCallback); // TrungTH them vao
            dialogChosePhone.setData(getContext(), phoneDataItems);
            dialogChosePhone.show(ft, BtalkDialogChosePhone.DIALOG_TAG);
        }
    }

    @Override
    public void onContactListFilterChanged() {
        setFilter(mContactListFilterController.getFilter());
        reStartContactLoader();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.floating_action_button || i == R.id.floating_action_button_small) {
            //AnhNDd: thuc hien them moi
            /*Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
            Bundle extras = getActivity().getIntent().getExtras();
            if (extras != null) {
                intent.putExtras(extras);
            }
            try {
                ImplicitIntentsUtil.startActivityInApp(getActivity(), intent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), R.string.missing_app,
                        Toast.LENGTH_SHORT).show();
            }*/

            // Anhdts thoat mode select
            if (mBtalkFrameToolBarBottomAdapter.isSelectionMode()) {
                handleStopSelectionMode();
                handleStopSelectMode();
            }
            mBtalkFrameToolAdapter.actionSearch();
        } else {
            //Log.wtf(TAG, "Unexpected onClick event from " + view);
        }
    }

    @Override
    public void addNewContact() {
        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            intent.putExtras(extras);
        }
        try {
            // Bkav TienNAb: fix lỗi hiển thị 2 activity thêm liên hệ mới chồng lên nhau
            ImplicitIntentsUtil.startActivityQuickContactInApp(getActivity(), intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), R.string.missing_app,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProviderStatusChange() {
        updateViewConfiguration(false);
    }

    /**
     * AnhNDd: lớp thực thi việc gọi ngược từ việc click item trên adapter.
     */
    private final class ContactActionListener implements BtalkMultiSelectEntryContactListAdapter.OnBtalkContactListItemViewListener {

        @Override
        public void showContactAction(Uri contactLookupUri, boolean isEnterpriseContact) {
            if (isEnterpriseContact) {
                // No implicit intent as user may have a different contacts app in work profile.
                ContactsContract.QuickContact.showQuickContact(getActivity() == null ? getContext() : getActivity(), new Rect(), contactLookupUri,
                        BtalkQuickContactActivity.MODE_FULLY_EXPANDED, null);
            } else {
                final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(
                        contactLookupUri, BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
                intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                        isSearchMode() ? ScreenEvent.ScreenType.SEARCH : ScreenEvent.ScreenType.ALL_CONTACTS);
                ImplicitIntentsUtil.startActivityInApp(getActivity() == null ? getContext() : getActivity(), intent);
            }
        }

        @Override
        public void showDialog(String number, String action) {
            createDialog(number, action);
        }

        @Override
        public void actionCall(String number) {
            UIIntents.get().makeACall(mContext, getFragmentManager(), number);
        }

        // Bkav HuyNQN tao dialog cho contact co nhieu so dien thoai khi goi hay nhan tin
        private void createDialog(String number, final String action) {
            StringTokenizer stringTokenizer = new StringTokenizer(number, ";", false);

            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.select_phone_number);

            final ArrayAdapter<String> menu = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            final ArrayList<Integer> mapId = new ArrayList<>();

            int index = 0;
            String[] arrNumber = new String[stringTokenizer.countTokens()];
            while (stringTokenizer.hasMoreTokens()) {
                // Bkav TienNAb: xoa khoang trong dau va cuoi chuoi, fix loi cac item khong thang hang
                arrNumber[index] = stringTokenizer.nextToken().trim();
                mapId.add(index);
                index++;
            }
            for (String ss : arrNumber) {
                menu.add(ss);
            }
            builder.setAdapter(menu, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int id = mapId.get(which);

                    if (action.equals(BtalkContactListItemView.ACTION_SELECT_SEND)) {// Bkav HuyNQN thuc hien nhan tin
                        Uri uri = Uri.parse("smsto:" + menu.getItem(id));
                        Intent messageIntent = new Intent(Intent.ACTION_SENDTO, uri);
                        registerFactoryActionSend(messageIntent);
                        getContext().startActivity(messageIntent);

                    } else if (action.equals(BtalkContactListItemView.ACTION_SELECT_CALL)) {// Bkav HuyNQN thuc hien goi dien
                        // Bkav TienNAb: sua lai code, chi goi so duoc chon.
                        //UIIntents.get().makeACall(mContext, getFragmentManager(), number);
                        UIIntents.get().makeACall(mContext, getFragmentManager(), menu.getItem(id));
                    }
                }
            });
            builder.show();
        }

        // Bkav HuyNQN dang ki gui tin nhan trong app
        protected void registerFactoryActionSend(Intent intent) {
            ((BtalkFactoryImpl) Factory.get()).registerSendMessage();
        }
    }


    private final class ContactListActionListener implements BtalkContactsListAdapter.OnBtalkContactListItemViewListener {

        @Override
        public void showContactAction(Uri contactLookupUri, boolean isEnterpriseContact) {
            if (isEnterpriseContact) {
                // No implicit intent as user may have a different contacts app in work profile.
                ContactsContract.QuickContact.showQuickContact(getActivity() == null ? getContext() : getActivity(), new Rect(), contactLookupUri,
                        BtalkQuickContactActivity.MODE_FULLY_EXPANDED, null);
            } else {
                // Bkav HienDTk: bam icon avatar thi cho an ban phim
                hideKeyBoard();
                final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(
                        contactLookupUri, BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
                intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                        isSearchMode() ? ScreenEvent.ScreenType.SEARCH : ScreenEvent.ScreenType.ALL_CONTACTS);
                // Bkav HienDTk: fix loi  No Activity found to handle Intent { act=android.provider.action.QUICK_CONTACT} => BOS-2859 - Start
                if(intent.resolveActivity(mContext.getPackageManager()) != null)
                    // Bkav HienDTk: fix loi  No Activity found to handle Intent { act=android.provider.action.QUICK_CONTACT} => BOS-2859 - End
                    ImplicitIntentsUtil.startActivityInApp(getActivity() == null ? getContext() : getActivity(), intent);
            }
        }

        @Override
        public void showDialog(String number, String action) {
            createDialog(number, action);
        }

        @Override
        public void actionCall(String number) {
            // Bkav HuyNQN bam vaof action goi thi an search view de dong ban phim.
            if (toolbarIsSearchMode()) {
                mBtalkFrameToolAdapter.searchBackButtonPressed();
            }
            UIIntents.get().makeACall(mContext, getFragmentManager(), number);
        }

        // Bkav HuyNQN tao dialog cho contact co nhieu so dien thoai khi goi hay nhan tin
        private void createDialog(String number, final String action) {
            StringTokenizer stringTokenizer = new StringTokenizer(number, ";", false);

            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.select_phone_number);

            final ArrayAdapter<String> menu = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            final ArrayList<Integer> mapId = new ArrayList<>();

            int index = 0;
            String[] arrNumber = new String[stringTokenizer.countTokens()];
            while (stringTokenizer.hasMoreTokens()) {
                // Bkav TienNAb: xoa khoang trong dau va cuoi chuoi, fix loi cac item khong thang hang
                arrNumber[index] = stringTokenizer.nextToken().trim();
                mapId.add(index);
                index++;
            }
            for (String ss : arrNumber) {
                menu.add(ss);
            }
            builder.setAdapter(menu, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int id = mapId.get(which);

                    if (action.equals(BtalkContactListItemView.ACTION_SELECT_SEND)) {// Bkav HuyNQN thuc hien nhan tin
                        Uri uri = Uri.parse("smsto:" + menu.getItem(id));
                        Intent messageIntent = new Intent(Intent.ACTION_SENDTO, uri);
                        registerFactoryActionSend(messageIntent);
                        // Bkav TienNAb: fix loi khong gui duoc tin nhan khi nang target version len 28
                        messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(messageIntent);

                    } else if (action.equals(BtalkContactListItemView.ACTION_SELECT_CALL)) {// Bkav HuyNQN thuc hien goi dien
                        // Bkav TienNAb: sua lai code, chi goi so duoc chon.
                        //UIIntents.get().makeACall(mContext, getFragmentManager(), number);
                        UIIntents.get().makeACall(mContext, getFragmentManager(), menu.getItem(id));
                    }
                }
            });
            builder.show();
        }

//        // Bkav HuyNQN dang ki gui tin nhan trong app
//        protected void registerFactoryActionSend(Intent intent) {
//            ((BtalkFactoryImpl) Factory.get()).registerSendMessage();
//        }
    }

    // Bkav HuyNQN dang ki gui tin nhan trong app
    protected void registerFactoryActionSend(Intent intent) {
        ((BtalkFactoryImpl) Factory.get()).registerSendMessage();
    }


    private final class ContactBrowserActionListener implements BtalkOnContactBrowserActionListener {
        ContactBrowserActionListener() {
        }

        @Override
        public void onSelectionChange() {

        }

        @Override
        public void showPhoneNumberOrCall(Uri contactLookupUri, boolean isEnterpriseContact) {
            mOnlyOnePhoneNumber = false;
            mLookupUri = contactLookupUri;
            mContactLoader = (ContactLoader) getLoaderManager().initLoader(
                    LOADER_CONTACT_ID, null, mLoaderContactCallbacks);
        }

        @Override
        public void onViewContactAction(Uri contactLookupUri, boolean isEnterpriseContact) {
            if (isEnterpriseContact) {
                // No implicit intent as user may have a different contacts app in work profile.
                ContactsContract.QuickContact.showQuickContact(getActivity(), new Rect(), contactLookupUri,
                        QuickContactActivity.MODE_FULLY_EXPANDED, null);
            } else {
                final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(
                        contactLookupUri, QuickContactActivity.MODE_FULLY_EXPANDED);
                intent.putExtra(QuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE, ScreenEvent.ScreenType.ALL_CONTACTS);
                ImplicitIntentsUtil.startActivityInApp(getActivity(), intent);
            }
        }

        @Override
        public void onDeleteContactAction(Uri contactUri) {
            //AnhNDd: Chưa thực hiện
        }

        @Override
        public void onFinishAction() {
            //AnhNDd: Chưa thực hiện
        }

        @Override
        public void onInvalidSelection() {
            //AnhNDd:
            ContactListFilter filter;
            ContactListFilter currentFilter = getFilter();
            if (currentFilter != null
                    && currentFilter.filterType == ContactListFilter.FILTER_TYPE_SINGLE_CONTACT) {
                filter = ContactListFilter.createFilterWithType(
                        ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS);
                setFilter(filter);
            } else {
                filter = ContactListFilter.createFilterWithType(
                        ContactListFilter.FILTER_TYPE_SINGLE_CONTACT);
                setFilter(filter, false);
            }
            mContactListFilterController.setContactListFilter(filter, true);
        }
    }

    //AnhNDd: Các thuộc tính liên quan đến Đến việc load contact từ uri.
    private Uri mLookupUri;

    private Contact mContactData;

    private ContactLoader mContactLoader;

    private boolean mOnlyOnePhoneNumber;

    /**
     * Id for the background contact loader
     * AnhNDd: Hiện tại đang có ProfileAndContactLoader đang chạy với ID là 0, cần đặt giá trị khác đi.
     * TODO TrungTH day len 1000 de khong bi trung id voi loader contact all
     */
    private static final int LOADER_CONTACT_ID = 1000;

    //AnhNDd: AsyncTask thực hiện việc load dữ liệu contact tu uri.
    private AsyncTask<Void, Void, BtalkQuickContactActivity.Cp2DataCardModel> mEntriesAndActionsTask;

    private final LoaderManager.LoaderCallbacks<Contact> mLoaderContactCallbacks =
            new LoaderManager.LoaderCallbacks<Contact>() {
                @Override
                public void onLoaderReset(Loader<Contact> loader) {
                    mContactData = null;
                }

                @Override
                public void onLoadFinished(Loader<Contact> loader, Contact data) {
                    Trace.beginSection("onLoadFinished()");
                    try {

                        if (getActivity().isFinishing()) {
                            return;
                        }
                        if (data.isError()) {
                            // This means either the contact is invalid or we had an
                            // internal error such as an acore crash.
                            Toast.makeText(getContext(), R.string.invalidContactMessage,
                                    Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        if (data.isNotFound()) {
                            Toast.makeText(getContext(), R.string.invalidContactMessage,
                                    Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        bindDataFromContactLoader(data);

                    } finally {
                        Trace.endSection();
                        //AnhNDd: Hủy luôn loader khi query xong.
                        getLoaderManager().destroyLoader(LOADER_CONTACT_ID);
                    }
                }

                @Override
                public Loader<Contact> onCreateLoader(int id, Bundle args) {
                    if (mLookupUri == null) {
                        Log.wtf("", "Lookup uri wasn't initialized. Loader was started too early");
                    }
                    // Load all contact data. We need loadGroupMetaData=true to determine whether the
                    // contact is invisible. If it is, we need to display an "Add to Contacts" MenuItem.
                    return new ContactLoader(getContext(), mLookupUri,
                            true /*loadGroupMetaData*/, false /*loadInvitableAccountTypes*/,
                            true /*postViewNotification*/, true /*computeFormattedPhoneNumber*/);
                }
            };

    /**
     * AnhNDd: Thực hiện xử lý dữ liệu từ contact loader.
     */
    private void bindDataFromContactLoader(final Contact data) {
        Trace.beginSection("bindContactData");
        mContactData = data;
        Trace.endSection();

        mEntriesAndActionsTask = new AsyncTask<Void, Void, Cp2DataCardModel>() {

            @Override
            protected Cp2DataCardModel doInBackground(
                    Void... params) {
                return generateDataModelFromContact(data);
            }

            @Override
            protected void onPostExecute(Cp2DataCardModel cardDataModel) {
                super.onPostExecute(cardDataModel);
                // Check that original AsyncTask parameters are still valid and the activity
                // is still running before binding to UI. A new intent could invalidate
                // the results, for example.
                if (data == mContactData && !isCancelled()) {
                    bindDataToCards(cardDataModel);
                }
            }
        };
        mEntriesAndActionsTask.execute();
    }

    private void bindDataToCards(Cp2DataCardModel cp2DataCardModel) {
        startInteractionLoaders(cp2DataCardModel);
    }

    private void startInteractionLoaders(Cp2DataCardModel cp2DataCardModel) {
        //AnhNDd: Thực hiện query ra số điện thoại.
        final Map<String, List<DataItem>> dataItemsMap = cp2DataCardModel.dataItemsMap;
        final List<DataItem> phoneDataItems = dataItemsMap.get(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        if (phoneDataItems != null && phoneDataItems.size() == 1) {
            mOnlyOnePhoneNumber = true;
            //AnhNDd: Nếu chỉ có một số điện thoại thì thực hiện gọi luôn.
            callAction(((PhoneDataItem) phoneDataItems.get(0)).getNumber());
            clearTextSearch();// TrungTH them vao
        } else if (phoneDataItems != null) {
            for (int i = 0; i < phoneDataItems.size(); i++) {
                if (phoneDataItems.get(i).isSuperPrimary()) {
                    //AnhNDd: Nếu chỉ có số điện thoại mặc định thì thực hiện gọi luôn.
                    callAction(((PhoneDataItem) phoneDataItems.get(i)).getNumber());
                    return;
                }
            }
            showDialogChosePhoneNumber(phoneDataItems);
        } else {
            // Anhdts xu ly du lieu zalo
            final List<DataItem> zaloDataItem = dataItemsMap.get("vnd.android.cursor.item/com.zing.zalo.call");
            if (zaloDataItem != null && zaloDataItem.size() > 0) {
                final ContentValues contentValues = zaloDataItem.get(0).getContentValues();
                String textDescription = contentValues.getAsString(ContactsContract.CommonDataKinds.Phone.DATA3);
                final String mNumberQuery = textDescription.substring(textDescription.indexOf("(") + 1,
                        textDescription.length() - 1);
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.notify_use_number_zalo)
                        .setPositiveButton(R.string.speed_dial_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog,
                                                        final int button) {
                                        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                                        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                .withValue(ContactsContract.Data.RAW_CONTACT_ID, contentValues.getAsLong(ContactsContract.Data.RAW_CONTACT_ID))
                                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mNumberQuery).
                                                        withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                                                .build());
                                        try {
                                            getContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                                        } catch (RemoteException | OperationApplicationException e) {
                                            e.printStackTrace();
                                        }
                                        if (!TextUtils.isEmpty(mNumberQuery)) {
                                            callAction(mNumberQuery);
                                        }
                                    }
                                })
                        .setNegativeButton(R.string.delete_conversation_decline_button, null)
                        .show();
            }
            Toast.makeText(getActivity(), getString(R.string.toast_cannot_call_without_number), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Builds the {@link DataItem}s Map out of the Contact.
     * AnhNDd: Hiện tại chỉ cần để lấy số điện thoại ,nên không xử lý sắp xếp dữ liệu.
     *
     * @param data
     * @return
     */
    private Cp2DataCardModel generateDataModelFromContact(
            Contact data) {
        Trace.beginSection("Build data items map");

        final Map<String, List<DataItem>> dataItemsMap = new HashMap<>();
        for (RawContact rawContact : data.getRawContacts()) {
            for (DataItem dataItem : rawContact.getDataItems()) {
                dataItem.setRawContactId(rawContact.getId());

                final String mimeType = dataItem.getMimeType();
                if (mimeType == null) continue;

                final AccountType accountType = rawContact.getAccountType(getContext());
                final DataKind dataKind = AccountTypeManager.getInstance(getContext())
                        .getKindOrFallback(accountType, mimeType);
                if (dataKind == null) continue;

                dataItem.setDataKind(dataKind);

                final boolean hasData = !TextUtils.isEmpty(dataItem.buildDataString(getContext(),
                        dataKind));

                if (/*isMimeExcluded(mimeType)||*/ !hasData) continue;

                List<DataItem> dataItemListByType = dataItemsMap.get(mimeType);
                if (dataItemListByType == null) {
                    dataItemListByType = new ArrayList<>();
                    dataItemsMap.put(mimeType, dataItemListByType);
                }
                dataItemListByType.add(dataItem);
            }
        }
        Trace.endSection();
        final Cp2DataCardModel dataModel = new Cp2DataCardModel();
        BtalkContactUtils.showLog("dataItemsMap=" + dataItemsMap.toString());
        dataModel.dataItemsMap = dataItemsMap;
        return dataModel;
    }

    //AnhNDd: tao lop cursor loader de query
    @Override
    public CursorLoader createCursorLoader(Context context) {
        return new BtalkProfileAndContactsLoader(context);
    }

    @Override
    public void onDestroy() {
        mProviderStatusWatcher.removeListener(this);
        if (mContactListFilterController != null) {
            mContactListFilterController.removeListener(this);
        }
        super.onDestroy();
    }

    /**
     * AnhNDd: hiển thị dialog import danh bạ.
     */
    @Override
    public void showImportDialogFragment() {
        BtalkImportExportDialogFragment.show(getFragmentManager(), false,
                BtalkMultiSelectContactsListFragment.class, BtalkImportExportDialogFragment.EXPORT_MODE_ALL_CONTACTS);
    }

    /**
     * AnhNDd: Hiển thị dialog export danh bạ.
     */
    @Override
    public void showExportDialogFragment() {
        BtalkImportExportDialogFragment.show(getFragmentManager(), true,
                BtalkMultiSelectContactsListFragment.class, BtalkImportExportDialogFragment.EXPORT_MODE_ALL_CONTACTS);
    }

    /**
     * AnhNDd: Hiển thị giao diện danh sách  yêu thích.
     */
    @Override
    public void showFavorites() {
        mBtalkFrameToolAdapter.setFavoriteMode(true);
        // Bkav TienNAb: update lai mau icon favorite khi chuyen sang danh sach yeu thich
        mFavoriteExpand.setColorFilter(ContextCompat.getColor(mContext, R.color.btalk_ab_text_and_icon_selected_color));
        mBtalkFrameToolAdapter.updateColorImageViewFavorite();

        mBtalkContentMain.setVisibility(View.GONE);
        mBtalkFavoritesFrameLayout.start(getLoaderManager());
    }

    @Override
    public void closeFavorites() {
        mBtalkFrameToolAdapter.setFavoriteMode(false);
        // Bkav TienNAb: update lai mau icon favorite khi thoat danh sach yeu thich
        mFavoriteExpand.setColorFilter(null);
        mBtalkFrameToolAdapter.updateColorImageViewFavorite();

        BtalkUiUtils.visibilityViewWithAnimation(mBtalkContentMain);
        mBtalkFavoritesFrameLayout.close();
    }

    /**
     * AnhNDd: Thực hiện việc export contacts.
     */
    public void exportContactsToVCF(Intent data) {
        Bundle result = data.getExtras().getBundle(
                SimContactsConstants.RESULT_KEY);
        Set<String> keySet = result.keySet();
        Iterator<String> it = keySet.iterator();
        StringBuilder selExportBuilder = new StringBuilder();
        while (it.hasNext()) {
            String id = it.next();
            if (0 != selExportBuilder.length()) {
                selExportBuilder.append(",");
            }
            selExportBuilder.append(id);
        }
        selExportBuilder.insert(0, " IN (");
        selExportBuilder.append(")");
        Intent exportIntent = new Intent(getActivity(),
                BtalkExportVCardActivity.class);
        exportIntent.putExtra(BtalkContactUtils.SEL_EXPORT, selExportBuilder.toString());
        exportIntent.putExtra(
                VCardCommonArguments.ARG_CALLING_ACTIVITY,
                getActivity().getClass().getName());
        this.startActivity(exportIntent);
    }

    /**
     * AnhNDd: Thực hiện xuất danh bạ ra sim
     */
    public void exportContactsToSIM(Intent data) {
        mContactList = new ArrayList<String[]>();
        Bundle b = data.getExtras();
        Bundle choiceSet = b.getBundle(SimContactsConstants.RESULT_KEY);
        Set<String> set = choiceSet.keySet();
        Iterator<String> i = set.iterator();
        while (i.hasNext()) {
            String contactInfo[] = choiceSet.getStringArray(i.next());
            mContactList.add(contactInfo);
        }
        if (!mContactList.isEmpty()) {
            if (!ImportExportDialogFragment.isExportingToSIM()) {
                ImportExportDialogFragment.destroyExportToSimThread();
                mExportThread =
                        new BtalkImportExportDialogFragment().createExportToSimThread(
                                ImportExportDialogFragment.mExportSub, mContactList,
                                getActivity());
                mExportThread.start();
            }
        }
    }


    /**
     * AnhNDd: class lăng nghe sự kiện khi click vào contact trong tab yêu thích.
     */
    private final class StrequentContactListFragmentListener
            implements BtalkFavoritesFrameLayout.Listener {
        StrequentContactListFragmentListener() {
        }

        @Override
        public void onContactSelected(Uri contactUri, Rect targetRect) {
//            final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(contactUri,
//                    BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
//            intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE, ScreenEvent.ScreenType.FAVORITES);
//            ImplicitIntentsUtil.startActivityInApp(getActivity(), intent);

            mOnlyOnePhoneNumber = false;
            mLookupUri = contactUri;
            mContactLoader = (ContactLoader) getLoaderManager().initLoader(
                    LOADER_CONTACT_ID, null, mLoaderContactCallbacks);
        }

        @Override
        public void onCallNumberDirectly(String phoneNumber) {
            // No need to call phone number directly from People app.
            //Log.w(TAG, "unexpected invocation of onCallNumberDirectly()");
        }
    }

    @Override
    protected boolean onItemLongClick(int position, long id) {
        //AnhNDd: Thay đổi màu nền và thay đổi ảnh đại diện.
        if (isNotPositionProfile(position)) {
            displayCheckBoxes(true);
            return super.onItemLongClick(position, id);
        } else {
            return true;
        }
    }

    @Override
    public void onContactItemClick(Cursor cursor, int position, boolean isEnterpriseContact) {
        final Uri uri = mContactAdapter.getContactUri(cursor, position);
        if (mContactAdapter.isDisplayingCheckBoxes()) {
            final String contactId = uri.getLastPathSegment();
            if (!TextUtils.isEmpty(contactId)) {
                mContactAdapter.toggleSelectionOfContactId(Long.valueOf(contactId));
            }
            if (mCheckBoxActionListener != null && mContactAdapter.getSelectedContactIds().size() == 0) {
                mCheckBoxActionListener.onStopDisplayingCheckBoxes();
            }
        } else {
            // Bkav HienDTk: an ban phim khi bam vao 1 item trong tab danh ba
            hideKeyBoard();
            showContact(uri, isEnterpriseContact);
        }
    }

    private void showContact(Uri uri, boolean isEnterpriseContact) {
        if (isEnterpriseContact) {
            // No implicit intent as user may have a different contacts app in work profile.
            ContactsContract.QuickContact.showQuickContact(getActivity() == null ? getContext() : getActivity(), new Rect(), uri,
                    BtalkQuickContactActivity.MODE_FULLY_EXPANDED, null);
        } else {
            final Intent intent = ImplicitIntentsUtil.composeQuickContactIntent(
                    uri, BtalkQuickContactActivity.MODE_FULLY_EXPANDED);
            // Bkav HaiKH - Fix bug BOS-3732- Start
            // Put giá trị để check cho việc update
            intent.putExtra(UPDATE_RINGTONE_KEY, UPDATE_RINGTONE_VALUE);
            // Bkav HaiKH - Fix bug BOS-3732- End
            intent.putExtra(BtalkQuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                    isSearchMode() ? ScreenEvent.ScreenType.SEARCH : ScreenEvent.ScreenType.ALL_CONTACTS);
            ImplicitIntentsUtil.startActivityInApp(getActivity() == null ? getContext() : getActivity(), intent);
        }
    }

    @Override
    public boolean onContactItemLongClick(Cursor cursor, int position) {
        if (isNotPositionUser(position)) {
            displayCheckBoxesContactItem(true);

            final Uri uri = mContactAdapter.getContactUri(cursor, position);
//            final int partition = getAdapter().getPartitionForPosition(position);
            if (uri != null && (/*position > 0*//*AnhNDd: custom*/isNotPositionUser(position) || !mContactAdapter.hasProfile())) {
                final String contactId = uri.getLastPathSegment();
                if (!TextUtils.isEmpty(contactId)) {
                    if (mCheckBoxActionListener != null) {
                        mCheckBoxActionListener.onStartDisplayingCheckBoxes();
                    }
                    mContactAdapter.toggleSelectionOfContactId(Long.valueOf(contactId));
                    // Manually send clicked event if there is a checkbox.
                    // See b/24098561.  TalkBack will not read it otherwise.
//                final int index = position + getListView().getHeaderViewsCount() - getListView()
//                        .getFirstVisiblePosition();
//                if (index >= 0 && index < getListView().getChildCount()) {
//                    getListView().getChildAt(index).sendAccessibilityEvent(AccessibilityEvent
//                            .TYPE_VIEW_CLICKED);
//                }
                }
            }
            if (mCheckBoxActionListener != null && mContactAdapter.getSelectedContactIds().size() == 0) {
                mCheckBoxActionListener.onStopDisplayingCheckBoxes();
            }
            return true;
        } else {
            return true;
        }
    }

    public void displayCheckBoxesContactItem(boolean b) {
        mContactAdapter.setDisplayCheckBoxes(b);
        if (!b) {
            clearCheckBoxesContact();
        }
    }

    public void clearCheckBoxesContact() {
        mContactAdapter.setSelectedContactIds(new TreeSet<Long>());
    }

    // TODO: tam chua sua lai thanh adapter minh custom
    public void fillCheckBoxesContact() {
        mContactAdapter.setSelectedContactIds(getAdapter().getAllVisibleContactIds());
    }

    public interface OnCheckBoxActionListener {
        void onStartDisplayingCheckBoxes();

        void onSelectedContactIdsChanged();

        void onStopDisplayingCheckBoxes();
    }

    public void setCheckBoxActionListListener(OnCheckBoxActionListener listener) {
        mCheckBoxActionListener = listener;
    }

    /**
     * AnhNDd: Class xử lý khi có 1 item contact được chọn. Trong lúc lựa chọn nhiều contact.
     */
    private final class CheckListener implements OnCheckBoxListActionListener {

        @Override
        public void onStartDisplayingCheckBoxes() {
            getView().findViewById(R.id.btalk_frame_tool).setVisibility(View.INVISIBLE);
            //AnhNDd: Hiển thị toolbar ở bottom
            mBtalkActivity.setStatusbarOnActionMode();// quangndb chuyen sang mau cam
            // Anhdts: doi action bar sang mau cam
            setSelectionMode(true);
            // Bkav TienNAb: khong can thoat che do search, fix loi khi dang search chon nhieu lien he khong bi thoat khoi che do search
            // Anhdts thoat che do search
//            if (mBtalkFrameToolAdapter.isSearchMode()) {
//                mBtalkFrameToolAdapter.searchBackButtonPressed();
//            }

            mBtalkFrameToolBarBottomAdapter.setSelectionMode(true);
            showFabWithAnimation(false);
        }

        @Override
        public void onSelectedContactIdsChanged() {
            if (mBtalkFrameToolBarBottomAdapter.isSelectionMode()) {
                int selectedCount = getSelectedContactIds().size();
                int allContactsCount = getAdapter().getAllVisibleContactIds().size();
                mBtalkFrameToolBarBottomAdapter.setSelectionCount(selectedCount);
                // TrungTH o trong giao dien contact doi sang mau trang
                mBtalkFrameToolBarBottomAdapter.setColorForSelectMenuButton(Color.WHITE);
                // When screen rotate, contacts cursor need reload, before cursor
                // reload complete, the allContactsCount is 0.
                if (allContactsCount != 0) {
                    //AnhNDd: TODO xử trong trường hợp xay màn hình.
                }
            }
        }

        @Override
        public void onStopDisplayingCheckBoxes() {
            if (!mBtalkFrameToolAdapter.isSearchMode())
                handleStopSelectionMode();
        }
    }

    private final class CheckBoxListener implements OnCheckBoxActionListener {

        @Override
        public void onStartDisplayingCheckBoxes() {
            getView().findViewById(R.id.btalk_frame_tool).setVisibility(View.INVISIBLE);
            //AnhNDd: Hiển thị toolbar ở bottom
            mBtalkActivity.setStatusbarOnActionMode();// quangndb chuyen sang mau cam
            // Anhdts: doi action bar sang mau cam
            setSelectionMode(true);
            // Bkav TienNAb: khong can thoat che do search, fix loi khi dang search chon nhieu lien he khong bi thoat khoi che do search
            // Anhdts thoat che do search
//            if (mBtalkFrameToolAdapter.isSearchMode()) {
//                mBtalkFrameToolAdapter.searchBackButtonPressed();
//            }

            mBtalkFrameToolBarBottomAdapter.setSelectionMode(true);
            showFabWithAnimation(false);
        }

        @Override
        public void onSelectedContactIdsChanged() {
            if (mBtalkFrameToolBarBottomAdapter.isSelectionMode()) {
                int selectedCount = mContactAdapter.getSelectedContactIds().size();
                int allContactsCount = getAdapter().getAllVisibleContactIds().size();
                mBtalkFrameToolBarBottomAdapter.setSelectionCount(selectedCount);
                // TrungTH o trong giao dien contact doi sang mau trang
                mBtalkFrameToolBarBottomAdapter.setColorForSelectMenuButton(Color.WHITE);
                // When screen rotate, contacts cursor need reload, before cursor
                // reload complete, the allContactsCount is 0.
                if (allContactsCount != 0) {
                    //AnhNDd: TODO xử trong trường hợp xay màn hình.
                }
            }
        }

        @Override
        public void onStopDisplayingCheckBoxes() {
            if (!mBtalkFrameToolAdapter.isSearchMode())
                handleStopSelectMode();
        }
    }

    @Override
    public void onSelectedContactsChanged() {
        if (mCheckBoxActionListener != null) {
            mCheckBoxActionListener.onSelectedContactIdsChanged();
        }
    }

    @Override
    public void startSearchMode() {
        mBtalkActivity.collapseAppBarLayout(mAppBarLayout, mRecyclerView);
        mAddContactExpand.setVisibility(View.GONE);
        mFavoriteExpand.setVisibility(View.GONE);
        //AnhNDd: an di nut them moi lien he
        //mFloatingActionButtonContainer.setVisibility(View.GONE);
        showFabWithAnimation(/* showFabWithAnimation = */ false);

        //AnhNDd: bat dau tim kiem thi bo selection di
        mBtalkActivity.exitActionMode();
        // Anhdts doi lai mau action bar
        setSelectionMode(false);
        mBtalkFrameToolBarBottomAdapter.setSelectionMode(false);
        displayCheckBoxes(false);
    }

    @Override
    public void closeSearchMode() {
        // Bkav TienNAb - Fix bug BOS-2997 - Start
        // check dieu kien co phai dang o che do chia doi man hinh khong truoc khi mo appbar layout
        if (!mBtalkActivity.isInMultiWindowMode()){
            mBtalkActivity.expandAppBarLayout(mAppBarLayout, mRecyclerView);
            mAddContactExpand.setVisibility(View.VISIBLE);
            mFavoriteExpand.setVisibility(View.VISIBLE);
        }
        // Bkav TienNAb - Fix bug BOS-2997 - Start
    }

    /**
     * Anhdts chuyen tab hien luon ban phim len va search
     */
    public void onTabSelected() {
        actionSearch();
    }

    private void actionSearch() {
        // Bkav HienDTk: fix loi - BOS-3392 - Start
        // Bkav HienDTk: khong cho delay de tranh TH ban phim khong thu lai khi nguoi dung thao tac qua nhanh giu cac tab
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
        mBtalkFrameToolAdapter.actionSearch();
//            }
//        }, (mIsLoadComplete ? 300 : 400)); // Bkav HuyNQN thuc hien bat ban phim len sau 300ms de ko bi giat
        // Bkav HienDTk: fix loi - BOS-3392 - Start
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        super.onFocusChange(view, hasFocus);
        mIsLoadComplete = true;
    }

    @Override
    public void setSectionHeaderDisplayEnabledInSearchMode(boolean bool) {
        //AnhNDd: luôn luôn hiển thị header.
        setSectionHeaderDisplayEnabled(true);
    }

    @Override
    public void setConfigureDefaultPartitionInSearchMode(boolean showIfEmty, boolean hasHeader) {
        //AnhNDd: khong hien thi header.
        getAdapter().configureDefaultPartition(showIfEmty, false);
    }

    /**
     * AnhNDd: kiểm tra xem toolbar có đang trong quá tình search hay không.
     *
     * @return
     */
    public boolean toolbarIsSearchMode() {
        if (mBtalkFrameToolAdapter != null) {
            return mBtalkFrameToolAdapter.isSearchMode();
        }
        return false;
    }

    /**
     * AnhNDd: xử lý khi người dùng bấm phím cứng quay lại.
     */
    public void handleBackPressed() {
        if (toolbarIsSearchMode()) {
            mBtalkFrameToolAdapter.searchBackButtonPressed();
        }
        if (mBtalkFrameToolBarBottomAdapter.isSelectionMode()) {
            handleStopSelectionMode();
            handleStopSelectMode();
        }
    }


    @Override
    public void onDestroyView() {
        if (mContactListFilterController != null) {
            mContactListFilterController.removeListener(this);
        }
        super.onDestroyView();
    }

    //AnhNDd: Doạn code cho việc filter contact.
    @Override
    public void contactsFilter() {
         /* AccountFilterUtil.startAccountFilterActivityForResult(
                getActivity(), SUBACTIVITY_ACCOUNT_FILTER,
                mContactListFilterController.getFilter());*/
        BtalkAccountFilterUtil.startBtalkAccountFilterActivityForResult(getActivity(), SUBACTIVITY_ACCOUNT_FILTER,
                mContactListFilterController.getFilter());
    }

    public void contactListFilterControllerCheckFilterValidity(boolean bool) {
        mContactListFilterController.checkFilterValidity(bool);
    }

    private void configureContactList() {
        // Filter may be changed when this Activity is in background.
        setFilter(mContactListFilterController.getFilter());
        //AnhNDd: TODO: kiem tra xem co ho tro giao diện từ phải sang trái không.
        //setVerticalScrollbarPosition(getActivity().getScrollBarPosition());
        setSelectionVisible(false);
    }

    /**
     * AnhNDd: Xử lý sau khi biết lọc danh bạ theo cách nào,
     */
    public void handleFilterResult(int resultCode, Intent data) {
        AccountFilterUtil.handleAccountFilterResult(
                mContactListFilterController, resultCode, data);
    }
    //===================================END===============

    //AnhNDd: có hiển thị nút thêm contact hay không đi kèm theo animation.
    public void showFabWithAnimation(boolean showFab) {
        if (mFloatingActionButtonContainer == null) {
            return;
        }
        if (showFab) {
            if (!mWasLastFabAnimationScaleIn) {
                mFloatingActionButtonContainer.setVisibility(View.VISIBLE);
                mFloatingActionButtonController.scaleIn(0);
            }
            mWasLastFabAnimationScaleIn = true;

        } else {
            // Anhdts bo an fab button search khi vao che do search
//            if (mWasLastFabAnimationScaleIn) {
//                mFloatingActionButtonContainer.setVisibility(View.VISIBLE);
//                mFloatingActionButtonController.scaleOut();
//            }
//            mWasLastFabAnimationScaleIn = false;
        }
    }

    // Bkav QuangNDb them animation khi an hien fab luc vuot
    public void setVisibleFabButton(int state) {
        UiUtils.revealOrHideViewWithAnimationBtalk(mFloatingActionButtonContainer, state, null);
    }

    public void setVisibleFabButtonSmall(int state) {
        UiUtils.revealOrHideViewWithAnimationBtalk(mFloatingActionButtonSmall, state, null);
    }

    private void initializeFabVisibility() {
        final boolean hideFab = mBtalkFrameToolAdapter.isSearchMode()
                /*|| mBtalkFrameToolAdapter.isSelectionMode()*/;
        mFloatingActionButtonContainer.setVisibility(hideFab ? View.GONE : View.VISIBLE);
        mFloatingActionButtonController.resetIn();
        mWasLastFabAnimationScaleIn = !hideFab;
    }

    //======================AnhNDd: lien quan den bottom tool=================
    public boolean isSelectionMode() {
        return mBtalkFrameToolBarBottomAdapter.isSelectionMode();
    }

    @Override
    public void deleteSelectedContacts() {
//        ContactMultiDeletionInteraction.start(getActivity(),
//                getSelectedContactIds());
        mSaveContactIdsDelete.addAll(mContactAdapter.getSelectedContactIds());
        ContactMultiDeletionInteraction.start(getActivity(),
                mContactAdapter.getSelectedContactIds());
        handleStopSelectMode();
    }

    @Override
    public void shareSelectedContacts() {
        handleShareSelectedContacts();
    }

    @Override
    public void editSelectedContact() {
        //AnhNDd: có một contact thì mới chỉnh sửa được.
//        if (getSelectedContactIds().size() == 1) {
//            String lookUp = getLookupKey(getSelectedContactIds());
//            long contactId = getSelectedContactIds().first();
//            Uri uri = ContactsContract.Contacts.getLookupUri(contactId, lookUp);
//            Intent intent = EditorIntents.createCompactEditContactIntent(
//                    uri,
//                    new MaterialColorMapUtils.MaterialPalette(pickColor(lookUp), mDefaultColor),
//                    0/*photoID*/);
//            startActivityForResult(intent, REQUEST_CODE_CONTACT_EDITOR_ACTIVITY);
//        }
        if (mContactAdapter.getSelectedContactIds().size() == 1) {
            String lookUp = getLookupKey(mContactAdapter.getSelectedContactIds());
            long contactId = mContactAdapter.getSelectedContactIds().first();
            Uri uri = ContactsContract.Contacts.getLookupUri(contactId, lookUp);
            Intent intent = EditorIntents.createCompactEditContactIntent(
                    uri,
                    new MaterialColorMapUtils.MaterialPalette(pickColor(lookUp), mDefaultColor),
                    0/*photoID*/);
            startActivityForResult(intent, REQUEST_CODE_CONTACT_EDITOR_ACTIVITY);
        }
    }

    /**
     * Returns a deterministic color based on the provided contact identifier string.
     */
    private int pickColor(final String identifier) {
        if (TextUtils.isEmpty(identifier)) {
            return mDefaultColor;
        }
        // String.hashCode() implementation is not supposed to change across java versions, so
        // this should guarantee the same email address always maps to the same color.
        // The email should already have been normalized by the ContactRequest.
        final int color = Math.abs(identifier.hashCode()) % mColors.length();
        return mColors.getColor(color, mDefaultColor);
    }

    @Override
    public void sendMessageSelectedContact() {
        int countID = mContactAdapter.getSelectedContactIds().size();
        if (countID > 100) {
            getAllNumberAsyntask aystask = new getAllNumberAsyntask(this.getActivity().getWindow().getContext());
            aystask.execute();
        } else {
            ArrayList<String> allPhone = getAllNumberWhenSelected();

            String phoneToSend = "";
            for (String phone : allPhone) {
                phoneToSend += phone + ";";
            }

            //AnhNDd: Thực hiện gửi tin nhắn đến các số.
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneToSend));
            smsIntent.setPackage(getContext().getPackageName());
            //ImplicitIntentsUtil.startActivityOutsideApp(getActivity(), smsIntent);
            registerFactoryActionSend(smsIntent);
            startActivity(smsIntent);
        }

    }

    public ArrayList<String> getAllNumberWhenSelected() {

        //AnhNDd: lấy ra cấc số điện thoại khi selected contact
        //TODO: hàm chưa tối ưu cần chỉnh sửa.
        ArrayList<String> allPhoneNumber = new ArrayList<String>();
        for (Long contactId : mContactAdapter.getSelectedContactIds()) {

            //AnhNDd: temp chứa tất cả các số điện thoại của 1 contact.
            ArrayList<String> temp = new ArrayList<String>();
            boolean hasPrimary = false;
            final Cursor cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONE_PROJECTION,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Integer isPrimary = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY));

                    if (isPrimary > 0) {
                        //AnhNDd: nếu là số mặc định thì chỉ add mỗi số này, bỏ qua các số còn lại
                        allPhoneNumber.add(phone);
                        hasPrimary = true;
                        break;
                    }
                    temp.add(phone);
                } while (cursor.moveToNext());

                if (!hasPrimary) {
                    allPhoneNumber.addAll(temp);
                }
            }
            cursor.close();
        }
        return allPhoneNumber;
    }

    //AnhNDd: lấy ra các số điện thoại về tên người dùng tương ứng để chia se.
    public ArrayList<String> getContactsToShare() {
        ArrayList<String> contactsToShare = new ArrayList<String>();
//        for (Long contactId : getSelectedContactIds()) {
        for (Long contactId : mContactAdapter.getSelectedContactIds()) {
            //AnhNDd: lấy ra tất cả các tên liên hệ
            final Cursor cursorName = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY},
                    ContactsContract.Contacts._ID + "=" + contactId, null, null);
            if (cursorName.moveToFirst()) {
                do {
                    String name = cursorName.getString(cursorName.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));

                    //AnhNDd: lấy ra số điện thoại theo tên
                    final Cursor cursorPhone = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONE_PROJECTION,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
                    if (cursorPhone.moveToFirst()) {
                        if (cursorPhone.getCount() > 1) {
                            //AnhNDd: co nhieu hon 1 so dien thoai
                            name += " <";
                            String phone;
                            phone = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            name += phone;
                            cursorPhone.moveToNext();
                            do {
                                phone = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                name += "; " + phone;
                            } while (cursorPhone.moveToNext());
                            name += ">";
                        } else {
                            String phone = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            name += "<" + phone + ">";
                        }
                    }
                    contactsToShare.add(name);
                    cursorPhone.close();
                } while (cursorName.moveToNext());
            }
            cursorName.close();
        }
        return contactsToShare;
    }

    private PopupList mPopupList;

    /**
     * Anhdts share contact
     */
    private void handleShareSelectedContacts() {
        //Bkav ToanNTe fix Danh bạ - BOS 8.7 - Lỗi: Chức năng chia sẻ liên hệ trong danh bạ không giống với chức năng chia sẻ liên hệ trong tab Gần đây
        //Hiển thị luôn các ứng dụng có thể chia sẻ liên hệ
        ArrayList<String> allPhone = getContactsToShare();
        String numberToShare = "";
        for (String number : allPhone) {
            numberToShare += number + "; ";
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, numberToShare);
        intent.setType("text/plain");
        Intent chooser = Intent.createChooser(
                intent, null);
        DialerUtils.startActivityWithErrorToast(mContext, chooser);
    }

    // AnhNDd: Query contact lookupKey instead of {@link Contacts#getLookupUri()} which is pretty
    // inefficient
    private String getLookupKey(Set<Long> mSelectedIds) {
        StringBuilder sb = new StringBuilder();
        sb.append(ContactsContract.Contacts._ID);
        sb.append(" IN ( ");
        for (Long contactId : mSelectedIds) {
            sb.append(contactId);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" )");

        final Cursor c = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new String[]{
                ContactsContract.Contacts.LOOKUP_KEY}, sb.toString(), null, null);

        if (c == null) {
            return null;
        }

        sb = new StringBuilder();
        try {
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                final String lookupKey = c.getString(0);
                sb.append(lookupKey);
                sb.append(":");
            }
        } finally {
            c.close();
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public boolean isNotPositionProfile(int position) {
        return (position != getAdapter().getPositionUserContact());
    }

    public boolean isNotPositionUser(int position) {
        return (position != mContactAdapter.getPositionUserContact());
    }

    private class ContactsUnavailableFragmentListener
            implements OnContactsUnavailableActionListener {
        ContactsUnavailableFragmentListener() {
        }

        @Override
        public void onCreateNewContactAction() {
            ImplicitIntentsUtil.startActivityInApp(getActivity(),
                    EditorIntents.createCompactInsertContactIntent());
        }

        @Override
        public void onAddAccountAction() {
            final Intent intent = ImplicitIntentsUtil.getIntentForAddingAccount();
            ImplicitIntentsUtil.startActivityOutsideApp(getActivity(), intent);
        }

        @Override
        public void onImportContactsFromFileAction() {
            showImportDialogFragment();
        }
    }

    @Override
    public void onPause() {
        mProviderStatusWatcher.stop();
        super.onPause();

        // Anhdts sua loi IllegalStateException saveState
        isSavedInstanceStateDone = false;

        // Bkav TienNAb: an menu share neu dang hien thi
        if (mPopupList != null && mPopupList.isShowing()) {
            mPopupList.dismiss();
        }
    }

    @Override
    public void onResume() {
        mProviderStatusWatcher.start();
        updateViewConfiguration(true);
        super.onResume();

        if (isSearchMode()) {
            mBtalkFrameToolAdapter.selectionText();
        }

        // Bkav TienNAb: Khi dang trong selection mode thi khong show ban phim
        if (mBtalkFrameToolAdapter.isSearchMode() && !isSelectionMode()) {
            actionSearch();
        }

        // Bkav TrungTh goi ham thong bao da vao onResume de bao da load xong fragment nay
        if (getActivity() != null && getActivity() instanceof BtalkActivity) {
            ((BtalkActivity) getActivity()).justFinishLoadingTab();
        }
        ((BtalkMultiSelectEntryContactListAdapter) mAdapter).setShowPhoneNumber(PreferenceManager.getDefaultSharedPreferences(getActivity()).
                getBoolean(getActivity().getString(R.string.show_phone_number), false));
        mContactAdapter.setShowPhoneNumber(PreferenceManager.getDefaultSharedPreferences(getActivity()).
                getBoolean(getActivity().getString(R.string.show_phone_number), false));
    }

    /**
     * AnhNDd Kiểm tra xem có số điện thoại hay không.
     *
     * @return
     */
    public boolean areContactsAvailable() {
        return (mProviderStatus != null) && mProviderStatus.equals(ContactsContract.ProviderStatus.STATUS_NORMAL);
    }

    /**
     * AnhNDd: cập nhật view khi cần thiết.
     *
     * @param forceUpdate
     */
    private void updateViewConfiguration(boolean forceUpdate) {
        int providerStatus = mProviderStatusWatcher.getProviderStatus();
        if (!forceUpdate && (mProviderStatus != null)
                && (mProviderStatus.equals(providerStatus))) return;
        mProviderStatus = providerStatus;

        View contactsUnavailableView = getView().findViewById(R.id.contacts_unavailable_view);

        if (mProviderStatus.equals(ContactsContract.ProviderStatus.STATUS_NORMAL)) {
            //AnhNDd: trường hợp có số điện thoại.
            contactsUnavailableView.setVisibility(View.GONE);
            setEnabled(true);
        } else {
            // Setting up the page so that the user can still use the app
            // even without an account.
            setEnabled(false);
            if (mContactsUnavailableFragment == null) {
                mContactsUnavailableFragment = new ContactsUnavailableFragment();
                mContactsUnavailableFragment.setOnContactsUnavailableActionListener(
                        new ContactsUnavailableFragmentListener());
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.contacts_unavailable_container, mContactsUnavailableFragment)
                        .commitAllowingStateLoss();
            }
            mContactsUnavailableFragment.updateStatus(mProviderStatus);
            mContactsUnavailableFragment.setTabInfo(R.string.noContacts, ActionBarAdapter.TabState.ALL);

            // Show the contactsUnavailableView, and hide the mTabPager so that we don't
            // see it sliding in underneath the contactsUnavailableView at the edges.
            contactsUnavailableView.setVisibility(View.VISIBLE);
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


    @Override
    protected void updateFilterHeaderView() {
        super.updateFilterHeaderView();
        mAccountFilterHeader.setVisibility(View.GONE);
    }

    //AnhNDd: đăng kí lắng nghe sự kiện export ra sim thành công
    public void registerReceiver() {
        if (mExportToSimCompleteListener != null) {
            return; // TrungTH them code
        }
        mExportToSimCompleteListener = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(SimContactsConstants.INTENT_EXPORT_COMPLETE)) {
                    ImportExportDialogFragment.destroyExportToSimThread();
                    mExportThread = null;
                }
            }
        };
        IntentFilter exportCompleteFilter = new IntentFilter(
                SimContactsConstants.INTENT_EXPORT_COMPLETE);
        getContext().registerReceiver(mExportToSimCompleteListener, exportCompleteFilter);
    }

    /**
     * AnhNDd: khôi phục quá trình export ra sim nếu cần
     */
    private void restoreExportToSimProgressBar() {

        // Judge whether contacts is exporting to sim card.
        if (ImportExportDialogFragment.isExportingToSIM()) {
            // Get export thread
            mExportThread = ImportExportDialogFragment.getExportingToSimThread();
            if (mExportThread != null) {
                // Restore ProgressDialog
                if (mExportThread.getProgressDialog() != null) {
                    mExportThread.getProgressDialog().dismiss();
                }
                new ImportExportDialogFragment().showExportToSIMProgressDialog(getActivity());
            }
        }
    }

    @Override
    public void onStart() {
        restoreExportToSimProgressBar();
        super.onStart();
    }


    // TrungTH them ham
    public void unRegisterReceiver() {
        if (mExportToSimCompleteListener != null) {
            getContext().unregisterReceiver(mExportToSimCompleteListener);
            mExportToSimCompleteListener = null;
        }
    }

    /**
     * Anhdts su kien double click tab
     */
    public void doubleClickTab() {
        mBtalkFrameToolAdapter.actionSearch();
    }

    /**
     * Anhdts xu ly intent den
     */
    public void configureFromRequest() {
        final ContactsRequest request = ((BtalkActivity) getActivity()).getRequest();

        boolean searchMode = request.isSearchMode();
        ContactListFilter filter = null;
        int actionCode = request.getActionCode();
        switch (actionCode) {
            case ContactsRequest.ACTION_ALL_CONTACTS:
                filter = ContactListFilter.createFilterWithType(
                        ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS);
                mBtalkFrameToolAdapter.setCheckFavorite(false);
                break;
            case ContactsRequest.ACTION_CONTACTS_WITH_PHONES:
                filter = ContactListFilter.createFilterWithType(
                        ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY);
                mBtalkFrameToolAdapter.setCheckFavorite(false);
                break;
            case ContactsRequest.ACTION_STARRED:
            case ContactsRequest.ACTION_STREQUENT:
            case ContactsRequest.ACTION_FREQUENT:
                mBtalkFrameToolAdapter.setCheckFavorite(true);
                break;
        }

        if (filter != null) {
            mContactListFilterController.setContactListFilter(filter, false);
        }

        if (searchMode) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mBtalkFrameToolAdapter.isSearchMode()) {
                        //HienDTk: set text cho thanh search view
                        mBtalkFrameToolAdapter.setTextSearch(mTextSearch);
                        mBtalkFrameToolAdapter.actionSearch();
                        configureContactListForRequest(request);
                    }
                }
            }, 500);
        }
    }

    private void configureContactListForRequest(ContactsRequest request) {
        Uri contactUri = request.getContactUri();
        if (contactUri != null) {
            setSelectedContactUri(contactUri);
        }

        setFilter(mContactListFilterController.getFilter());
        setQueryString(mBtalkFrameToolAdapter.getQueryString(), true);
        setVisibleScrollbarEnabled(!isSearchMode());

        if (request.isDirectorySearchEnabled()) {
//            setDirectorySearchMode(DirectoryListLoader.SEARCH_MODE_DEFAULT);
//        } else {
            setDirectorySearchMode(DirectoryListLoader.SEARCH_MODE_NONE);
        }
    }

    /**
     * Anhdts thay doi mau action bar
     */
    private void setSelectionMode(boolean isSelection) {
        if (isSelection) {
            mAddContactExpand.setVisibility(View.GONE);
            mFavoriteExpand.setVisibility(View.GONE);
            setVisibleFabButton(View.GONE);
            setVisibleFabButtonSmall(View.GONE);
        } else {
            if (!mBtalkFrameToolAdapter.isButtonFavoriteAndButtonAddContactVisible()) {
                mAddContactExpand.setVisibility(View.VISIBLE);
                mFavoriteExpand.setVisibility(View.VISIBLE);
            }
            setVisibleFabButton(View.VISIBLE);
            setVisibleFabButtonSmall(View.VISIBLE);
        }
        getView().findViewById(R.id.actionbar).
                setBackgroundColor(ContextCompat.getColor(Factory.get().getApplicationContext(),
                        isSelection ? R.color.action_mode_color : R.color.btalk_transparent_view));
    }

    /**
     * Anhdts: keo sang trang khac thi tro ve trang thai binh thuong
     */
    public void updateSelectionMode() {
        if (!getView().findViewById(R.id.btalk_frame_tool).isShown()) {
            handleStopSelectionMode();
            handleStopSelectMode();
        }
        if (toolbarIsSearchMode()) {
            mBtalkFrameToolAdapter.searchBackButtonPressed();
        }
    }

    /**
     * Anhdts an view goi y neu co
     */
    @Override
    public void hideSearchView() {
        mBtalkActivity.getSmartSuggestLoaderManage().hideViewSuggest();
    }

    //ToanNTe thực hiện đóng search view và bàn phím
    @Override
    public void onClickMessage() {
        mBtalkFrameToolAdapter.searchBackButtonPressed();
    }

    @Override
    public void onClick(DialerDatabaseHelper.ContactNumber data) {
        clearTextSearch();
    }

    // Bkav HuyNQN thuc hien kiem tra xem co bi mat danh ba hay khong
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void checkDataContact(String dataId, long idContactSuggest, String name, String phone) {
        long contactId = 0;
        boolean isHasContact = false;
        Cursor cursorGetContactById = getContext().getApplicationContext().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone._ID + " = ?",
                new String[]{dataId},
                null);
        if (cursorGetContactById != null) {
            while (cursorGetContactById.moveToNext()) {
                contactId = cursorGetContactById.getLong(cursorGetContactById.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                return;
            }
            cursorGetContactById.close();
            if (contactId == 0 && mSaveContactIdsDelete.size() > 0) {
                for (long idContact : mSaveContactIdsDelete) {
                    if (idContactSuggest == idContact) {
                        isHasContact = true;
                        break;
                    }
                }
            } else if (contactId == 0 && mSaveContactIdsDelete.size() == 0) {
                isHasContact = false;
            }

            // Bkav HuyNQN neu mat danh ba thuc hien gui log
            if (!isHasContact) {
                String contactInfor = "contactId: " + idContactSuggest + ", contactName: " + name + ", contactNumber: " + phone;
                String imei = ContactUtils.getImeiBySlot(getContext(), 0, getActivity()) + " : " +
                        ContactUtils.getImeiBySlot(getContext(), 1, getActivity());
                Date date = Calendar.getInstance().getTime();
                String messages = imei + "\n" + contactInfor + "\n" + "time_delete: " + date.toString();
                Intent intent = new Intent(ACTION_SEND_REPORT_BUG);
                intent.setComponent(new ComponentName("bkav.android.bkavbugreport", "bkav.android.bkavbugreport.BugReportReceiver"));
                intent.putExtra("messages", messages);
                getContext().sendBroadcast(intent);
            }
        }
    }

    // TrungTH them vao
    public void clearTextSearch() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBtalkFrameToolAdapter != null) {
                    mBtalkFrameToolAdapter.clearText();
                    // TrungTH chot lai goi xong ra khoi giao dien search luon
                    if (toolbarIsSearchMode()) {
                        mBtalkFrameToolAdapter.searchBackButtonPressed();
                    }
                }
            }
        }, BtalkConst.DELAY_CLEAR_TEXT_SEARCH); // Them delay vi xoa luon tao cam giac giat view khi goi dien
    }

    private BtalkDialogChosePhone.IDialogCallback mIDialogCallback = new BtalkDialogChosePhone.IDialogCallback() {
        @Override
        public void onCallActionFinish() {
            clearTextSearch();
        }
    };

    private boolean isSavedInstanceStateDone = true;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isSavedInstanceStateDone = true;
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

                final float imageAlpha = 1 - (Math.abs(verticalOffset) / (float) appBarLayout.getTotalScrollRange());

                if (imageAlpha == 0) {
                    mFloatingActionButton.setClickable(true);
                } else {
                    mFloatingActionButton.setClickable(false);
                }

                if (imageAlpha == 1) {
                    if (!isSelectionMode()) {
                        mFloatingActionButtonSmall.setClickable(true);
                    }
                } else {
                    mFloatingActionButtonSmall.setClickable(false);
                }

                if (mBtalkActivity != null && isAdded()) {
                    // Bkav TienNAb: hieu ung text cua thanh title
                    mTitleTab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_min)
                            + (getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_max) - getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_min)) * imageAlpha);
                }

                if (scrollRange + verticalOffset <= 35) {
                    mImageBackgroundExpandLayout.setVisibility(View.GONE);
                    mAddContactExpand.setVisibility(View.GONE);
                    mFavoriteExpand.setVisibility(View.GONE);
                    mBtalkFrameToolAdapter.showHideButtonAddContact(true);
                    mBtalkFrameToolAdapter.showHideButtonFavorite(true);
                    isShow = true;
                } else if (isShow) {
                    mImageBackgroundExpandLayout.setVisibility(View.VISIBLE);
                    if (!mBtalkFrameToolAdapter.isSearchMode() && !isSelectionMode()) {
                        mAddContactExpand.setVisibility(View.VISIBLE);
                        mFavoriteExpand.setVisibility(View.VISIBLE);
                    }
                    mBtalkFrameToolAdapter.showHideButtonAddContact(false);
                    mBtalkFrameToolAdapter.showHideButtonFavorite(false);
                    isShow = false;
                }
            }
        });
    }

    //HienDTk: asyntask chon sdt
    // Theo cach cu thi load danh ba nhieu se bi treo dao dien do db nhieu lan
    // => tach nho ra và xu ly bang lenh sql in (lenh nay cung han che do dai)
    public class getAllNumberAsyntask extends AsyncTask<Void, Integer, ArrayList> {
        ArrayList<String> allPhoneNumber = new ArrayList<String>();
        int mCount = 0;
        private int mTotal;
        String phoneToSend = "";

        private BkavProgressbar mDialogSelectNumber;

        public getAllNumberAsyntask(Context context) {
            mDialogSelectNumber = new BkavProgressbar(context);
        }

        @Override
        protected ArrayList doInBackground(Void... voids) {
            mTotal = mContactAdapter.getSelectedContactIds().size();
            mDialogSelectNumber.setMaxProgress(mTotal);
            String id = "";
            int count = 0;
            for (Long contactId : mContactAdapter.getSelectedContactIds()) {
                count++;
                if (count % 100 == 0) {
                    id += contactId + " /";
                }

                id += contactId + " ";
            }
            String[] arrID = id.split("/", 100);
            for (int i = 0; i < arrID.length; i++) {
                arrID[i] = "(" + arrID[i].trim().replace(" ", ",") + ")";
                //AnhNDd: temp chứa tất cả các số điện thoại của 1 contact.
                ArrayList<String> temp = new ArrayList<String>();
                boolean hasPrimary = false;
                final Cursor cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONE_PROJECTION,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " IN " + arrID[i], null, null);
                if (cursor.moveToFirst()) {

                    do {
                        mCount++;
                        String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Integer isPrimary = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY));
                        phoneToSend += phone + ";";
                         if (isPrimary > 0) {
                            //AnhNDd: nếu là số mặc định thì chỉ add mỗi số này, bỏ qua các số còn lại
                            allPhoneNumber.add(phone);
                            hasPrimary = true;
                             // Bkav HienDTk: Sua lai logic de chon het sdt
//                            break;
                        }
                        if (!hasPrimary)
                            temp.add(phone);

                        publishProgress(mCount);
                    } while (cursor.moveToNext());

                    if (!hasPrimary) {
                        allPhoneNumber.addAll(temp);
                    }
                }
                cursor.close();
            }

            return allPhoneNumber;
        }

        @Override
        protected void onPreExecute() {
            if (mDialogSelectNumber != null && !mDialogSelectNumber.isShowing()) {
                mDialogSelectNumber.show();
            }
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mDialogSelectNumber.setProgress(values[0]);
            mDialogSelectNumber.setPerCent("" + (int) ((mCount * 100) / mTotal) + "%");
            mDialogSelectNumber.setPercentNumber(Integer.toString(mCount));
            mDialogSelectNumber.setPercentTotal("/" + mTotal);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(ArrayList aVoid) {
            super.onPostExecute(aVoid);
            startIntentSms(phoneToSend);
            if (mDialogSelectNumber != null) {
                mDialogSelectNumber.dismiss();
            }

        }
    }

    public void startIntentSms(String phoneToSend) {
        //AnhNDd: Thực hiện gửi tin nhắn đến các số.
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneToSend));
        smsIntent.setPackage(getContext().getPackageName());
        //ImplicitIntentsUtil.startActivityOutsideApp(getActivity(), smsIntent);
        registerFactoryActionSend(smsIntent);
        startActivity(smsIntent);
    }

}