package bkav.android.btalk.messaging.ui.conversationlist;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.util.ViewUtil;
import com.android.messaging.Factory;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.DeleteConversationAction;
import com.android.messaging.datamodel.action.UpdateConversationArchiveStatusAction;
import com.android.messaging.datamodel.action.UpdateConversationOptionsAction;
import com.android.messaging.datamodel.action.UpdateDestinationBlockedAction;
import com.android.messaging.datamodel.data.ConversationData;
import com.android.messaging.datamodel.data.ConversationListData;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.SubscriptionListData;
import com.android.messaging.ui.SnackBar;
import com.android.messaging.ui.SnackBarInteraction;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.contact.AddContactsConfirmationDialog;
import com.android.messaging.ui.conversationlist.AbstractConversationListActivity;
import com.android.messaging.ui.conversationlist.ConversationListAdapter;
import com.android.messaging.ui.conversationlist.ConversationListFragment;
import com.android.messaging.ui.conversationlist.ConversationListItemView;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.UiUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import bkav.android.btalk.R;
import bkav.android.btalk.activities.BtalkActivity;
import bkav.android.btalk.messaging.datamodel.action.MarkAllReadAction;
import bkav.android.btalk.messaging.datamodel.data.BtalkConversationListItemData;
import bkav.android.btalk.messaging.ui.block.NotificationBlockReceiver;
import bkav.android.btalk.messaging.ui.cutomview.BtalkCustomActionModeView;
import bkav.android.btalk.messaging.ui.cutomview.BtalkSetAppAsSMSDefaultView;
import bkav.android.btalk.messaging.ui.searchSms.BtalkSearchActivity;
import bkav.android.btalk.messaging.ui.searchSms.RecyclerCursorAdapter;
import bkav.android.btalk.messaging.ui.searchSms.SearchAdapter;
import bkav.android.btalk.messaging.util.BtalkCharacterUtil;
import bkav.android.btalk.messaging.util.BtalkDataObserver;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.ALARM_SERVICE;
import static com.android.dialer.util.DialerUtils.hideInputMethod;

/**
 * Created by quangnd on 27/03/2017.
 * Custom lai lop ConversationListFragment cua code goc
 */

public class BtalkConversationListFragment extends ConversationListFragment
        implements View.OnClickListener, BtalkCustomActionModeView.Listener
        , BtalkDataObserver.OnChangeListener,
        RecyclerCursorAdapter.ItemClickListener<ConversationMessageData>,
        LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener,
        SearchView.OnCloseListener {

    private ImageButton mOverFollow;

    private SearchView mSearchSms;// BKav QuanngNDb them search view search sms

    // Anhdts
    private static final String TEXT_SEARCH_KEY = "text_search";

    private BtalkCustomActionModeView mBtalkCustomActionModeView;

    private View mActionbar;

    private TextView mTitleTab;// Bkav TienNAb: Them tieu de cho tab message

    private ImageView mIconTab;// Bkav TienNAb: Icon lon cua tab

    private AppBarLayout mAppBarLayout;// Bkav TienNAb: them appbar layout

    private ImageView mImageBackgroundExpandLayout;// Bkav TienNAb: them hinh icon lon khi appbar layout mo rong

    private ImageView mImageButtonSearchExpand;// Bkav TienNAb: Them button search khi appbar layout mo rong

    private boolean mIsAppBarLayoutExpand = false;

    private ImageView mStartNewConversationButtonSmall;// Bkav TienNAb: nut soan tin nhan moi khi appbar layout dang mo

    private BtalkSetAppAsSMSDefaultView mBtalkSetAppAsSMSDefaultView;

    private static final int REQUEST_SET_DEFAULT_SMS_APP = 1;

    private static final String BUNDLE_ARCHIVED_MODE = "archived_mode";

    private static final String BUNDLE_FORWARD_MESSAGE_MODE = "forward_message_mode";

    private ImageButton mCreateGroupConversation;

    private RecyclerView mListSearch;// Bkav QuangNDb them list search tin nhan

    private BtalkDataObserver mContactObserver;

    private BtalkDataObserver mParticipantObserver;

    private BtalkDataObserver mMessageObserver;

    private LoaderManager mLoaderManager;

    // bien string search text
    private String mSearchString;

    // search adapter
    private SearchAdapter mSearchAdapter;

    private LinearLayoutManager mLayoutManager;

    private BtalkActivity mActivity;

    private static final int ID_LOADER = 3;// id loader

    private EditText mVitualEditText;// text ao de format query

    private ContactPhotoManager mContactPhotoManager;

    /**
     * Bkav QuangNDb dang lang nghe su kien change cua contact
     */
    private void registerObserver(BtalkDataObserver observer, Uri uri) {
        getActivity().getContentResolver().registerContentObserver(uri, true, observer);
        observer.setOnChangeListener(this);
    }

    /**
     * Bkav QuangNDb huy dang lang nghe su kien change cua contact
     */
    private void unregisterContactObserver(BtalkDataObserver observer) {
        getActivity().getContentResolver().unregisterContentObserver(observer);
    }


    @Override
    protected ConversationListAdapter initAdapter() {
        return new BtalkConversationListAdapter(Factory.get().getApplicationContext(), null, this);
    }

    @Override
    public boolean isConversationSelected(String conversationId) {
        if (isArchivedLayout()) {
            return super.isConversationSelected(conversationId);
        } else {
            return isActionModeShow() && mBtalkCustomActionModeView.isSelected(conversationId);
        }
    }

    @Override
    public boolean isSwipeAnimatable() {
        return !isActionModeShow();
    }

    @Override
    public boolean isSelectionMode() {
        return isActionModeShow();
    }

    @Override
    protected int getResource() {
        return R.layout.btalk_conversation_list_fragment;
    }

    private View mContentView;

    public static ConversationListFragment createArchivedConversationListFragment() {
        return createConversationListFragment(BUNDLE_ARCHIVED_MODE);
    }

    public static ConversationListFragment createForwardMessageConversationListFragment() {
        return createConversationListFragment(BUNDLE_FORWARD_MESSAGE_MODE);
    }

    public static ConversationListFragment createConversationListFragment(String modeKeyName) {
        final ConversationListFragment fragment = new BtalkConversationListFragment();
        final Bundle bundle = new Bundle();
        bundle.putBoolean(modeKeyName, true);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = super.onCreateView(inflater, container, savedInstanceState);
        mOverFollow = (ImageButton) mContentView.findViewById(R.id.imb_over_flow);
        mOverFollow.setOnClickListener(this);
        mActionbar = mContentView.findViewById(R.id.layout_action_bar);
        mBtalkCustomActionModeView = (BtalkCustomActionModeView) mContentView.findViewById(R.id.btalk_action_made);
        mSearchSms = (SearchView) mContentView.findViewById(R.id.imb_search_conversation);
        ViewUtil.addRectangularOutlineProvider(mBtalkCustomActionModeView, getResources());
        mBtalkSetAppAsSMSDefaultView = (BtalkSetAppAsSMSDefaultView) mContentView.findViewById(R.id.layout_set_as_default);
        // Bkav HienDTk: neu dang o giao dien forward thi van hien actionbar => hien thi text nguoi dung tim kiem
//        mActionbar.setVisibility((isArchivedLayout() || isForwardLayout()) ? View.GONE : View.VISIBLE);
        mActionbar.setVisibility((isArchivedLayout()) ? View.GONE : View.VISIBLE);
//        mCreateGroupConversation = (ImageButton) mContentView.findViewById(R.id.imb_create_group_conversation);
//        mCreateGroupConversation.setVisibility(View.GONE); // Bkav HuyNQN thuc hien dong lai chuc nang chat nhom
        mListSearch = (RecyclerView) mContentView.findViewById(R.id.list_search);
        mSearchAdapter = new SearchAdapter(Factory.get().getApplicationContext());
        mSearchAdapter.setItemClickListener(this);
        mLayoutManager = new LinearLayoutManager(Factory.get().getApplicationContext());

        mListSearch.setHasFixedSize(true);
        mListSearch.setLayoutManager(mLayoutManager);
        mListSearch.setAdapter(mSearchAdapter);
//        mVitualEditText = (EditText) mContentView.findViewById(R.id.vitual_text);
//        PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(
//                getActivity(), mVitualEditText, /* formatAfterWatcherSet =*/ false);
//        groupClick();
        setUpSearchView();
        if (!mForwardMessageMode) {
            addButtonClick();
        }

        mTitleTab = mContentView.findViewById(R.id.title_tab_message);
        mIconTab = mContentView.findViewById(R.id.img_background_expand_layout);
        mAppBarLayout = mContentView.findViewById(R.id.app_bar_layout);
        mImageBackgroundExpandLayout = mContentView.findViewById(R.id.img_background_expand_layout);
        mImageButtonSearchExpand = mContentView.findViewById(R.id.img_ic_search_expand);
        mStartNewConversationButtonSmall = mContentView.findViewById(R.id.new_conversation_button);

        // Bkav TienNAb: khi nhan vao nut soan tin nhan moi tren appbar layout
        mStartNewConversationButtonSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bkav HienDTk: HienDTk: fix loi khong hien thi tin nhan khi chuyen tiep tin nhan
                MessageData mDraftMessage = getActivity().getIntent().getParcelableExtra(UIIntents.UI_INTENT_EXTRA_DRAFT_DATA);
                UIIntents.get().launchCreateNewConversationActivity(getActivity(), mDraftMessage);
            }
        });

        // Bkav TienNAb: khi nhan vao icon tab thi soan tin nhan moi
        mIconTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIIntents.get().launchCreateNewConversationActivity(getActivity(), null);
            }
        });

        // Bkav TienNAb: neu la che do luu tru thi an appbar layout
        if (isArchivedLayout()) {
            mAppBarLayout.setVisibility(View.GONE);
            mStartNewConversationButtonSmall.setVisibility(View.GONE);
        } else {
            mAppBarLayout.setVisibility(View.VISIBLE);
            mStartNewConversationButtonSmall.setVisibility(View.VISIBLE);
        }

        updateAppBarLayout(mContentView);

        // Bkav TienNAb: xu ly su kien click vao item search khi appbar layout dang mo rong
        mImageButtonSearchExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchSms.setIconified(false);
            }
        });

        //HienDTk: sroll list search thi cho an ban phim
        if (mListSearch != null) {
            mListSearch.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            hideInputMethod(v);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            hideInputMethod(v);
                            break;
                    }
                    return false;
                }
            });
        }

        // Bkav TienNAb: tao notification channel, fix loi khong hien thi tin nhan khi nang target version len 28
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            BugleNotifications.createChannelNotification(notificationManager, getString(R.string.message_notification_chanel_id),
                    getContext().getResources().getString(com.android.messaging.R.string.message_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH, true);
        }

        // Bkav TienNAb - Fix bug BOS-2997 - Start
        // xuly dong mo appbar layout
        mActivity.appbarLayoutWithInMultiWindowMode(mActivity, mAppBarLayout, mRecyclerView);
        // Bkav TienNAb - Fix bug BOS-2997 - End

        return mContentView;
    }

    /**
     * Anhdts xu ly su kien click vao icon search
     */
    private void setUpSearchView() {

        EditText searchEditText = (EditText) mSearchSms.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setHintTextColor(ContextCompat.getColor(getActivity(), R.color.color_hint_searchview));// Bkav HuyNQN sua lai hincolor
        searchEditText.setHint(R.string.search_sms_hint);
        // Bkav HuyNQN cai dat lai fontsize va hintcolor cho searchsms
        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_search_record_size));
        ImageView closeButton = (ImageView) mSearchSms.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        closeButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_btalk_remove_small));// Bkav HuyNQN sua lai fontsize
        mSearchSms.setOnQueryTextListener(this);
        mSearchSms.setOnCloseListener(this);
        mSearchSms.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchMode();
//                openConversationSearch();
            }
        });

        // Bkav HuyNQN fix loi khong dong lai ban phim khi chuyen sang cac tab khac
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    closeSearchMode();
                }
            }
        });
    }

    /**
     * Bkav QuangNDb mo giao dien search conversation
     */
    private void openConversationSearch() {
        //Bkav QuangNDb mo giao dien conversation search
    }


    /**
     * Anhdts override
     * restore gia tri search
     */
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        //Bkav QuangNDb comment lai de luc recreate app khong can mo search
//        if (mSearchSms != null) {
//            outState.putString(TEXT_SEARCH_KEY, String.valueOf(mSearchSms.getQuery()));
//        }
    }

    /**
     * Anhdts override
     * restore gia tri search
     */
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            closeSearchMode();
            //Bkav QuangNDb quangndb bo set query khi restore
//            mSearchSms.setQuery(savedInstanceState.getString(TEXT_SEARCH_KEY), true);
        }
    }

    private void openSearchActivity() {
        Intent intent = new Intent(getActivity(), BtalkSearchActivity.class);
        getActivity().startActivity(intent);
    }


    /**
     * Bkav QuangNDb: check xem trang thai fragment co dang o giao dien luu tru hay khong
     */
    private boolean isArchivedLayout() {
        return (getArguments() != null && (getArguments().getBoolean(BUNDLE_ARCHIVED_MODE)));
    }

    /**
     * Bkav QuangNDb check xem trang thai fragment co dang o giao dien forward hay khong
     */
    private boolean isForwardLayout() {
        return (getArguments() != null && (getArguments().getBoolean(BUNDLE_FORWARD_MESSAGE_MODE)));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBtalkSetAppAsSMSDefaultView.setFragment(this);
//        if (isArchivedLayout()) {
//            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SET_DEFAULT_SMS_APP) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getActivity(), R.string.notify_btalk_as_sms_default, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.notify_btalk_not_as_sms_default, Toast.LENGTH_SHORT).show();
            }
            updateSetAsDefaultSmsView();
        }
    }

    /**
     * Bkav QuangNDb: method kiem tra xem btalk co phai la phan mem nhan tin mac dinh khong
     * Neu co thi khong hien thi view set btalk lam phan mem nhan tin mac dinh
     * Neu khong thi hien thi view set btalk lam phan mem nhan tin mac dinh
     */
    private void updateSetAsDefaultSmsView() {
        if (isDefaultSmsApp()) {
            mBtalkSetAppAsSMSDefaultView.setVisibility(View.GONE);
        } else {
            mBtalkSetAppAsSMSDefaultView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Bkav QuangNDb check xem Btalk co phai la phan mem tin nhan mac dinh hay k
     */
    private boolean isDefaultSmsApp() {
        final PhoneUtils phoneUtils = PhoneUtils.getDefault();
        return phoneUtils.isDefaultSmsApp();
    }

    /**
     * Bkav QuangNDb: method xu ly su kien khi click vao button + them cuoc hoi thoai moi
     */
    private void addButtonClick() {
        mStartNewConversationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click vao dau +
                UIIntents.get().launchCreateNewConversationActivity(getActivity(), null);
            }
        });
    }

    /**
     * Bkav QuangNDb them ham set an hien fab button
     */
    @Override
    public void setVisibleFabButton(int state) {
        if (mStartNewConversationButton != null) {
            UiUtils.revealOrHideViewWithAnimationBtalk(mStartNewConversationButton, state, null);
        }
    }

    public void setVisibleFabButtonSmall(int state) {
        if (mStartNewConversationButtonSmall != null) {
            UiUtils.revealOrHideViewWithAnimationBtalk(mStartNewConversationButtonSmall, state, null);
        }
    }

    /**
     * Bkav QuangNDb click vao icon group
     */
//    protected void groupClick() {
//        mCreateGroupConversation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                UIIntents.get().launchCreateNewGroupConversationActivity(getActivity(), null);
//            }
//        });
//    }
    private boolean isActionModeShow() {
        return mBtalkCustomActionModeView != null && mBtalkCustomActionModeView.getVisibility() == View.VISIBLE;
    }

    /**
     * BKav QuangNdb: override lai ham click vao cac item conversation de hien thi tin nhan
     */
    @Override
    public void onConversationClicked(ConversationListItemData conversationListItemData,
                                      boolean isLongClick, ConversationListItemView conversationView) {
        if (isArchivedLayout() || isForwardLayout()) {
            super.onConversationClicked(conversationListItemData, isLongClick, conversationView);
        } else {
            final ConversationListData listData = mListBinding.getData();
            if (isLongClick && !isActionModeShow()) {
                mBtalkCustomActionModeView.setListener(this);
                showMultiSelectState();
            }
            if (isActionModeShow()) {
                mBtalkCustomActionModeView.toggleSelect(listData, conversationListItemData);
                updateUi();
            } else {
                final String conversationId = conversationListItemData.getConversationId();
                Bundle sceneTransitionAnimationOptions = null;
                boolean hasCustomTransitions = false;
                UIIntents.get().launchConversationActivity(
                        getActivity(), conversationId, null,
                        sceneTransitionAnimationOptions,
                        hasCustomTransitions);
            }
        }


    }

    private MarkAllReadAction.MarkAllReadMonitor mMarkAllReadMonitor;

    /**
     * Bkav QuangNDb: method xu ly su kien click vao nut overfollow tren actionbar
     * Khi click vao se hien thi nen 1 popup menu co cac chuc nang theo kich ban
     */
    private void clickOverFollow() {
        PopupMenu popupMenu = new PopupMenu(getActivity(), mOverFollow, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.btalk_conversation_list_fragmnet_menu
                , popupMenu.getMenu());
        MenuItem blockItem = popupMenu.getMenu().findItem(R.id.action_show_blocked_contacts);
        blockItem.setVisible(mBlockedAvailable);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_settings:
                        UIIntents.get().launchSettingsActivity(getActivity());
                        break;
                    case R.id.action_show_archived:
                        UIIntents.get().launchArchivedConversationsActivity(getActivity());
                        break;
                    case R.id.action_show_blocked_contacts:
                        UIIntents.get().launchBlockedParticipantsActivity(getActivity());
                        break;
                    case R.id.action_mark_all_read:// mark all read
                        showDialogMarkMessage();
                        mMarkAllReadMonitor = MarkAllReadAction.markAllRead(mMarkAllReadInterface);
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imb_over_flow:
                clickOverFollow();
                break;
        }
    }

    @Override
    public void onActionBarDelete(final Collection<BtalkCustomActionModeView.SelectedConversation>
                                          conversations) {
        if (!PhoneUtils.getDefault().isDefaultSmsApp()) {
            // TODO: figure out a good way to combine this with the implementation in
            // ConversationFragment doing similar things
            final Activity activity = getActivity();
            UiUtils.showSnackBarWithCustomAction(getActivity(),
                    getActivity().getWindow().getDecorView().getRootView(),
                    getString(com.android.messaging.R.string.requires_default_sms_app),
                    SnackBar.Action.createCustomAction(new Runnable() {
                                                           @Override
                                                           public void run() {
                                                               final Intent intent =
                                                                       UIIntents.get().getChangeDefaultSmsAppIntent(activity);
                                                               startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);
                                                           }
                                                       },
                            getString(com.android.messaging.R.string.requires_default_sms_change_button)),
                    null /* interactions */,
                    null /* placement */);
            return;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getQuantityString(
                        com.android.messaging.R.plurals.delete_conversations_confirmation_dialog_title,
                        conversations.size()))
                .setPositiveButton(com.android.messaging.R.string.delete_conversation_confirmation_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int button) {
                                for (BtalkCustomActionModeView.SelectedConversation conversation : conversations) {
                                    DeleteConversationAction.deleteConversation(
                                            conversation.conversationId,
                                            conversation.timestamp);
                                }
                                exitMultiSelectState();
                            }
                        })
                .setNegativeButton(com.android.messaging.R.string.delete_conversation_decline_button, null)
                .show();
    }

    /**
     * Bkav QuangNdb: method xu ly viec show action mode ra va an button + them tin nhan
     */
    private void showMultiSelectState() {
        mImageButtonSearchExpand.setVisibility(View.INVISIBLE);

        mActivity.setStatusbarOnActionModeMessage();    // Bkav TienNAb: update statusbar khi bat giao dien actionmode
        mBtalkCustomActionModeView.setVisibility(View.VISIBLE);
        setVisibleFabButton(View.GONE);
        setVisibleFabButtonSmall(View.GONE);
//        mActionbar.setVisibility(View.INVISIBLE);
    }

    /**
     * Bkav QuangNDb: method xu ly khi thoat khoi che do action mode
     */
    public void exitMultiSelectState() {
        if (mIsAppBarLayoutExpand) {
            mImageButtonSearchExpand.setVisibility(View.VISIBLE);
        }
        mTitleTab.setVisibility(View.VISIBLE);

        //update trang thai an action mode va show floating button
        mActivity.exitActionMode();//Bkav QuangNDb tro lai trang thai status ba cu
        mBtalkCustomActionModeView.setVisibility(View.GONE);
        setVisibleFabButton(View.VISIBLE);
        setVisibleFabButtonSmall(View.VISIBLE);
        mActionbar.setVisibility(View.VISIBLE);
        mBtalkCustomActionModeView.setListener(null);
        mBtalkCustomActionModeView.resetAllSelect();
        updateUi();
    }

    @Override
    public void onActionBarArchive(Iterable<BtalkCustomActionModeView.SelectedConversation>
                                           conversations, final boolean isToArchive) {
        final ArrayList<String> conversationIds = new ArrayList<String>();
        for (final BtalkCustomActionModeView.SelectedConversation conversation : conversations) {
            final String conversationId = conversation.conversationId;
            conversationIds.add(conversationId);
            if (isToArchive) {
                UpdateConversationArchiveStatusAction.archiveConversation(conversationId);
            } else {
                UpdateConversationArchiveStatusAction.unarchiveConversation(conversationId);
            }
        }

        final Runnable undoRunnable = new Runnable() {
            @Override
            public void run() {
                for (final String conversationId : conversationIds) {
                    if (isToArchive) {
                        UpdateConversationArchiveStatusAction.unarchiveConversation(conversationId);
                    } else {
                        UpdateConversationArchiveStatusAction.archiveConversation(conversationId);
                    }
                }
            }
        };

        final int textId =
                isToArchive ? R.string.archived_toast_message : R.string.unarchived_toast_message;
        final String message = getResources().getString(textId, conversationIds.size());
        UiUtils.showSnackBar(getActivity(), mRecyclerView, message, undoRunnable,
                SnackBar.Action.SNACK_BAR_UNDO,
                getSnackBarInteractions());
        exitMultiSelectState();

    }

    @Override
    public void onActionBarNotification(Iterable<BtalkCustomActionModeView.SelectedConversation> conversations
            , boolean isNotificationOn) {
        if (!isNotificationOn) {
            showDialogBlockNotification(conversations);
        } else {
            for (final BtalkCustomActionModeView.SelectedConversation conversation : conversations) {
                UpdateConversationOptionsAction.enableConversationNotifications(
                        conversation.conversationId, true);
                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                Intent myIntent = new Intent(getActivity(), NotificationBlockReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getActivity(), Integer.parseInt(conversation.conversationId), myIntent, 0);
                alarmManager.cancel(pendingIntent);
            }

            final int textId = R.string.notification_on_toast_message;
            final String message = getResources().getString(textId, 1);
            UiUtils.showSnackBar(getActivity(), mRecyclerView, message,
                    null /* undoRunnable */,
                    SnackBar.Action.SNACK_BAR_UNDO, getSnackBarInteractions());
            exitMultiSelectState();
        }
    }

    private final static String ACTION_BLOCK_NOTIFICATION =
            "btalk.action.open_block_notification";

    /**
     * Anhdts hien dialog chon thoi gian chan thong bao
     */
    private void showDialogBlockNotification(final Iterable<BtalkCustomActionModeView.SelectedConversation> conversations) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.mute_conversation_title);
        final View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.btalk_dialog_mute_conversation, null);

        builder.setView(contentView);
        final AlertDialog dialog = builder.create();
        // HienDTk: fix loi hien thi nen den xung quanh popup tat thong bao tren android 10
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(dialog.getWindow().getAttributes());
//        lp.width = getResources().getDimensionPixelOffset(R.dimen.width_dialog_block_notify);
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
//        dialog.getWindow().setAttributes(lp);
        contentView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        contentView.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioGroup radioGroup = (RadioGroup) contentView.findViewById(R.id.radio_group_mute_cvs);
                int id = radioGroup.getCheckedRadioButtonId();
                long time = System.currentTimeMillis();
                switch (id) {
                    case R.id.block_until_back:
                        time = 0;
                        break;
                    case R.id.block_15_minutes:
                        time += 900000;
                        break;
                    case R.id.block_1_hour:
                        time += 360000;
                        break;
                    case R.id.block_8_hours:
                        time += 2880000;
                        break;
                    case R.id.block_24_hours:
                        time += 86400000;
                        break;
                    case R.id.block_to_tomorrow:
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, 1);
                        calendar.set(Calendar.HOUR_OF_DAY, 7);
                        calendar.set(Calendar.MINUTE, 0);
                        time = calendar.getTimeInMillis();
                        break;
                }
                for (final BtalkCustomActionModeView.SelectedConversation conversation : conversations) {
                    UpdateConversationOptionsAction.enableConversationNotifications(
                            conversation.conversationId, false);
                    if (time != 0) {
                        Intent intent = new Intent(getActivity(), NotificationBlockReceiver.class);
                        intent.putExtra("conversationid", conversation.conversationId);
                        intent.setAction(ACTION_BLOCK_NOTIFICATION);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                getActivity(), Integer.parseInt(conversation.conversationId), intent, 0);
                        AlarmManager alarmManager = (AlarmManager)
                                getActivity().getSystemService(ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                    }
                }

                final int textId = R.string.notification_off_toast_message;
                final String message = getResources().getString(textId, 1);
                UiUtils.showSnackBar(getActivity(), mRecyclerView, message,
                        null /* undoRunnable */,
                        SnackBar.Action.SNACK_BAR_UNDO, getSnackBarInteractions());
                exitMultiSelectState();
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onActionBarAddContact(BtalkCustomActionModeView.SelectedConversation conversation) {
//        final Uri avatarUri;
//        if (conversation.icon != null) {
//            avatarUri = Uri.parse(conversation.icon);
//        } else {
//            avatarUri = null;
//        }
//        final AddContactsConfirmationDialog dialog = new AddContactsConfirmationDialog(
//                getActivity(), avatarUri, conversation.otherParticipantNormalizedDestination);
//        dialog.show();

        // Bkav HaiKH - Fix bug BOS-3728- Start
        // Sang activity contact luôn (không hiển thị dialog nữa)
        UIIntents.get().launchAddContactActivity(getActivity(), conversation.otherParticipantNormalizedDestination);
        // Bkav HaiKH - Fix bug BOS-3728- End
        exitMultiSelectState();
    }

    @Override
    public void onActionBarBlock(final BtalkCustomActionModeView.SelectedConversation conversation) {
        final Resources res = getResources();
        new AlertDialog.Builder(getActivity())
                .setTitle(res.getString(com.android.messaging.R.string.block_confirmation_title,
                        conversation.otherParticipantNormalizedDestination))
                .setMessage(res.getString(com.android.messaging.R.string.block_confirmation_message))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface arg0, final int arg1) {
                        final Context context = getActivity();
                        final List<SnackBarInteraction> interactions =
                                getSnackBarInteractions();
                        final UpdateDestinationBlockedAction.UpdateDestinationBlockedActionListener
                                undoListener =
                                new AbstractConversationListActivity.UpdateDestinationBlockedActionSnackBar(
                                        context, mRecyclerView, null /* undoRunnable */,
                                        interactions);
                        final Runnable undoRunnable = new Runnable() {
                            @Override
                            public void run() {
                                UpdateDestinationBlockedAction.updateDestinationBlocked(
                                        conversation.otherParticipantNormalizedDestination, false,
                                        conversation.conversationId,
                                        undoListener);
                            }
                        };
                        final UpdateDestinationBlockedAction.UpdateDestinationBlockedActionListener
                                listener = new AbstractConversationListActivity.UpdateDestinationBlockedActionSnackBar(
                                context, mRecyclerView, undoRunnable, interactions);
                        UpdateDestinationBlockedAction.updateDestinationBlocked(
                                conversation.otherParticipantNormalizedDestination, true,
                                conversation.conversationId,
                                listener);
                        // Bkav HienDTk: fix loi: Btalk - BOS 9 - Lỗi: Vẫn có âm thanh thông báo khi nhận được tin nhắn từ số điện thoại đã chặn => BOS-2335 - Start
                        UpdateConversationOptionsAction.enableConversationNotifications(
                                conversation.conversationId, false);
                        // Bkav HienDTk: fix loi: Btalk - BOS 9 - Lỗi: Vẫn có âm thanh thông báo khi nhận được tin nhắn từ số điện thoại đã chặn => BOS-2335 - End

                        exitMultiSelectState();
                    }
                })
                .create()
                .show();
    }


    @Override
    public void onActionBarHome() {
        //an action mode, change trang thai cua item conversation
        exitMultiSelectState();
    }

    @Override
    protected void initSwipeItem() {
        //Bkav QuangNDb khong lam gi de bo chuc nang vuot ngang cua lop cha di
    }

    @Override
    public void onResume() {
        super.onResume();
        // Bkav QuangNDb check xem Btalk co phai sms mac dinh hay khong thi update view
        updateSetAsDefaultSmsView();
        // Bkav TrungTh goi ham thong bao da vao onResume de bao da load xong fragment nay
        if (getActivity() != null && getActivity() instanceof BtalkActivity) {
//            ((BtalkActivity) getActivity()).justFinishLoadingTab();
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mLoaderManager = getLoaderManager();
        // Bkav QuangnDB dong doan dang ky lang nghe thay doi csdl lai
        mMessageObserver = new BtalkDataObserver(new Handler());
        registerObserver(mMessageObserver, Telephony.MmsSms.CONTENT_URI);
        mContactPhotoManager = ContactPhotoManager.getInstance(getContext());
//        mContactObserver = new BtalkDataObserver(new Handler());
//        mParticipantObserver = new BtalkDataObserver(new Handler());
//        registerObserver(mContactObserver, ContactsContract.Contacts.CONTENT_URI);
//        registerObserver(mParticipantObserver, PARTICIPANTS_URI);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Bkav QuangnDB dong doan huy dang ky lang nghe thay doi csdl lai
//        unregisterContactObserver(mContactObserver);
//        unregisterContactObserver(mParticipantObserver);
        unregisterContactObserver(mMessageObserver);
        mProgressDialog = null;
        if (mMarkAllReadMonitor != null) {
            mMarkAllReadMonitor.unregister();
        }
        mContactPhotoManager.cancelPendingRequests(getView());
        mAdapter.unbind();
    }

    @Override
    public void onChange(Uri uri) {
        if (uri.equals(Telephony.MmsSms.CONTENT_URI)) {
//            SendBroadcastUnreadMessage.sendBroadCast(Factory.get().getApplicationContext());
            DataModel.get().onActivityResume();
        }
    }


    /**
     * Bkav QuangNDB mo che do search mode
     */
    private void openSearchMode() {
        // Bkav TienNAb: dong appbar layout va an thanh tieu de
        mActivity.collapseAppBarLayout(mAppBarLayout, mRecyclerView);

        mTitleTab.setVisibility(View.GONE);
        mIsSearchShow = true;
        mRecyclerView.setVisibility(View.GONE);
        mListSearch.setVisibility(View.VISIBLE);
        mStartNewConversationButton.setVisibility(View.GONE);
        mStartNewConversationButtonSmall.setVisibility(View.GONE);
        mEmptyListMessageView.setTextHint(R.string.hint_open_search);
        mEmptyListMessageView.setVisibility(View.VISIBLE);
    }

    /**
     * Bkav QuangNDb dong che do searchMode lai
     */
    private void closeSearchMode() {
        // Bkav TienNAb: mo rong appbar layout va hien thi thanh tieu de
        // Bkav TienNAb - Fix bug BOS-2997 - Start
        // check dieu kien co phai dang o che do chia doi man hinh khong truoc khi mo appbar layout
        // Bkav TienNAb - Fix bug BOS-3736 - Start
        // them check activity khac null
        if (mActivity != null && !mActivity.isInMultiWindowMode()){
            mActivity.expandAppBarLayout(mAppBarLayout, mRecyclerView);
            mTitleTab.setVisibility(View.VISIBLE);
        }
        // Bkav TienNAb - Fix bug BOS-3736 - End
        // Bkav TienNAb - Fix bug BOS-2997 - End
        mIsSearchShow = false;
        mSearchSms.onActionViewCollapsed();
        mListSearch.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mStartNewConversationButton.setVisibility(View.VISIBLE);
        mStartNewConversationButtonSmall.setVisibility(View.VISIBLE);
        updateEmptyListUi(mIsEmptyMessage);
        mLoaderManager.destroyLoader(ID_LOADER);

    }

    @Override
    public boolean onClose() {
        if (!TextUtils.isEmpty(mSearchSms.getQuery())) {
            mSearchSms.setQuery(null, true);
        }
        closeSearchMode();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //Bkav QuangNDb doan tren danh cho search conversation
//        mVitualEditText.setText(newText);
//        String newFilter = !TextUtils.isEmpty(newText) ? mVitualEditText.getText().toString() : null;
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
        if (mSearchString == null && newFilter == null) {
            return true;
        }
        if (mSearchString != null && mSearchString.equals(newFilter)) {
            return true;
        }
        mSearchString = newFilter == null ? null : newFilter.replace("'", "''");
//        mVitualEditText.getText().clear();
//        updateSearchConversation();
        initLoader();
        return true;
    }

    /**
     * Bkav QuangNDb update du lieu search conversation
     */
    private void updateSearchConversation() {
        mListBinding.getData().setSearchString(BtalkCharacterUtil.get().convertToNotLatinCode(mSearchString));
        mListBinding.getData().restart(getLoaderManager(), mListBinding);
        mAdapter.setQuery(BtalkCharacterUtil.get().convertToNotLatinCode(mSearchString));
    }

    /**
     * Bkav QuangNDb them loader de load database
     */
    private void initLoader() {
        Loader loader = mLoaderManager.getLoader(ID_LOADER);
        if (loader != null && !loader.isReset()) {
            mLoaderManager.restartLoader(ID_LOADER, null, this);
        } else {
            mLoaderManager.initLoader(ID_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MessagingContentProvider.buildMessagesSearchUri(BtalkCharacterUtil.get().convertToNotLatinCode(mSearchString));
        CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, null, null, null, null);
        cursorLoader.loadInBackground();
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mSearchString == null || mSearchString.length() == 0) {
            notifyReadySearch();
        } else if (data.getCount() == 0) {
            notifyNotFoundSearch();
        } else {
            mEmptyListMessageView.setVisibility(View.GONE);
        }
        //Bkav QuangNDb sua loi bam close ma van hien ra item search nhung bi sai summary
        mSearchAdapter.changeCursor(mSearchString == null ? null : data);
        mSearchAdapter.setQuery(mSearchString);
    }

    /**
     * Bkav QuangNDb hien thi thong bao san sang search
     */
    private void notifyReadySearch() {
        mEmptyListMessageView.setVisibility(View.VISIBLE);
        mEmptyListMessageView.setTextHint(R.string.hint_open_search);
    }

    /**
     * Bkav QuangNDb hien thi thong bao khong search duoc ket qua
     */
    private void notifyNotFoundSearch() {
        mEmptyListMessageView.setVisibility(View.VISIBLE);
        mEmptyListMessageView.setTextHint(R.string.hint_not_found_message);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(ConversationMessageData object, View view) {
        openThread(object);
    }

    @Override
    public void onItemLongClick(ConversationMessageData object, View view) {
        openThread(object);
    }

    /**
     * Bkav QuangNDb mo conversation khi click or long click vao item search
     */
    private void openThread(ConversationMessageData data) {
        int positionMessage = getPositionMessage(data.getConversationId(), Integer.parseInt(data.getMessageId()));
        //HienDTk: fix loi chuyen tiep tin nhan ma khong hien tin nhan khi search
        MessageData mDraftMessage = getActivity().getIntent().getParcelableExtra(UIIntents.UI_INTENT_EXTRA_DRAFT_DATA);
        UIIntents.get().launchConversationActivitySearch(getActivity(), data.getConversationId(), mDraftMessage, null, false, positionMessage);// message id de co the scroll den vi tri cua tin nhan do khi mo conversation =
    }

    /**
     * Bkav QuangNDb lay vi tri cua tin nhan trong list de co the scroll den vi tri cua tin nhan do khi mo conversation
     */
    private int getPositionMessage(String conversationId, int messageId) {
        int position;
        Cursor cursor = null;
        Cursor reversedData = null;
        try {
            final Uri uri =
                    MessagingContentProvider.buildConversationMessagesUri(conversationId);
            cursor = Factory.get().getApplicationContext().getContentResolver().query(uri, ConversationMessageData.getProjection(), null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                reversedData = new ConversationData.ReversedCursor(cursor);
                reversedData.moveToFirst();
                do {
                    if (reversedData.getInt(cursor.getColumnIndex(DatabaseHelper.MessageColumns._ID)) == messageId) {
                        position = reversedData.getPosition();
                        return position;
                    }
                } while (reversedData.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (reversedData != null) {
                reversedData.close();
            }
        }
        return -1;
    }

    @Override
    protected void checkNullHost() {
        // Bkav QuangNDB khong lam gi
    }

    /**
     * Bkav QuangNDb Tach code doan get has focusWindow
     */
    protected boolean getHasFocusWinDow() {
        return getActivity().hasWindowFocus();
    }

    boolean mIsSearchShow = false;

    /**
     * Anhdts check thanh search co hien khong
     */
    public boolean isSearchBarShown() {
        return mIsSearchShow;
    }

    /**
     * Anhdts an thanh search
     */
    public void hideSearch() {
        closeSearchMode();
    }

    /**
     * Anhdts
     * mo giao dien nhan tin moi
     */
    public void doubleClickTab() {
        UIIntents.get().launchCreateNewConversationActivity(getActivity(), null);
    }

    /**
     * Bkav QuangNDb Hien thi dilog danh dau tin nhan da doc
     */
    private void showDialogMarkMessage() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(R.string.progress_mark_all_message);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    ProgressDialog mProgressDialog;// dialog hien thi mark tin nhan

    //Bkav QuangNDb Interface lang ghe su kien mark action
    private MarkAllReadAction.MarkAllReadInterface mMarkAllReadInterface = new MarkAllReadAction.MarkAllReadInterface() {
        @Override
        public void onMarkAllReadSucceed() {
            mProgressDialog.dismiss();
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof BtalkActivity) {
            mActivity = (BtalkActivity) getActivity();
        }
    }

    @Override
    public void onSelectAllConversation() {
        mBtalkCustomActionModeView.toggleAllSelect(mAllConversation);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUnSelectAllConversation() {
        mBtalkCustomActionModeView.toggleUnAllSelect(mAllConversation);
        exitMultiSelectState();
    }

    private List<ConversationListItemData> mAllConversation = new ArrayList<>();

    @Override
    public void onConversationListCursorUpdated(ConversationListData data, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                final ConversationListItemData conversationListData = new BtalkConversationListItemData();
                conversationListData.bind(cursor);
                mAllConversation.add(conversationListData);
            } while (cursor.moveToNext());
            cursor.moveToFirst();
        }
        super.onConversationListCursorUpdated(data, cursor);
    }

    @Override
    public SubscriptionListData.SubscriptionListEntry getSubscriptionEntryForSelfParticipant(String selfParticipantId, boolean excludeDefault) {
        return mListBinding.getData().getSubscriptionEntryForSelfParticipant(selfParticipantId, excludeDefault);
    }

    // Bkav TienNAb: update appbar layout
    public void updateAppBarLayout(View view) {

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                //Bkav QuangNDb
                final float imageAlpha = 1 - (Math.abs(verticalOffset) / (float) appBarLayout.getTotalScrollRange());

                if (imageAlpha == 0) {
                    if (!isSelectionMode()) {
                        mStartNewConversationButton.setClickable(true);
                    }
                } else {
                    mStartNewConversationButton.setClickable(false);
                }

                if (imageAlpha == 1) {
                    if (!isSelectionMode()) {
                        mStartNewConversationButtonSmall.setClickable(true);
                    }
                } else {
                    mStartNewConversationButtonSmall.setClickable(false);
                }

                if (mActivity != null && isAdded()) {
                    // Bkav TienNAb: hieu ung text cua thanh title
                    if (imageAlpha == 0) {
                        mTitleTab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_min));
                    } else if (imageAlpha == 1) {
                        mTitleTab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_max));
                    } else if (imageAlpha > 0 && imageAlpha < 1) {
                        mTitleTab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_min)
                                + (getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_max)
                                - getResources().getDimensionPixelOffset(R.dimen.title_tab_text_size_min)) * imageAlpha);
                    }
                }

                if (scrollRange + verticalOffset <= 35) {
                    mImageBackgroundExpandLayout.setVisibility(View.GONE);
                    mImageButtonSearchExpand.setVisibility(View.INVISIBLE);
                    mSearchSms.setVisibility(View.VISIBLE);

                    mIsAppBarLayoutExpand = false;
                } else if (!mIsAppBarLayoutExpand) {
                    mImageBackgroundExpandLayout.setVisibility(View.VISIBLE);
                    if (!isActionModeShow()) {
                        mImageButtonSearchExpand.setVisibility(View.VISIBLE);
                    }
                    mSearchSms.setVisibility(View.GONE);

                    mIsAppBarLayoutExpand = true;
                }
            }
        });
    }
}
