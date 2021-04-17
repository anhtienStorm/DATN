package bkav.android.btalk.contacts;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Settings.System;
import android.support.v4.content.ContextCompat;
import android.telecom.TelecomManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;

import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.compat.TelecomManagerUtil;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.list.UiIntentActions;
import com.android.dialer.compat.FilteredNumberCompat;

import bkav.android.btalk.R;
import bkav.android.btalk.bmsblocked.BmsUtils;
import bkav.android.btalk.messaging.ui.contact.activities.BtalkContactSelectionActivity;
import bkav.android.btalk.utility.Config;
import bkav.android.btalk.view.EditTextKeyListener;

/**
 * AnhNDd: do không sử dụng toolbar nên tạo ra framelayout giống như vậy.
 */
public class BtalkFrameToolAdapter implements SearchView.OnCloseListener, View.OnClickListener {

    // These values needs to start at 2. See {@link ContactEntryListFragment}.
    private static final int SUBACTIVITY_ACCOUNT_FILTER = 2;

    //AnhNDd: cac action trước toolbar sử dụng
    //Các hành động ảnh hưởng đến toolbar hiện tại vẫn chưa xử dụng hết.
    public interface Listener {

        // Anhdts an view search khi back
        void hideSearchView();

        abstract class Action {

            public static final int CHANGE_SEARCH_QUERY = 0;

            public static final int START_SEARCH_MODE = 1;

            public static final int START_SELECTION_MODE = 2;

            public static final int STOP_SEARCH_AND_SELECTION_MODE = 3;

            public static final int BEGIN_STOPPING_SEARCH_AND_SELECTION_MODE = 4;
        }

        void onAction(int action);

        void onUpButtonPressed();

        void showImportDialogFragment();

        void showExportDialogFragment();

        /**
         * AnhNDd: hiển thị tab danh sách contact yêu thích.
         */
        void showFavorites();

        /**
         * AnhNDd: ẩn đi tab danh sách contact yêu thích.
         */
        void closeFavorites();

        /**
         * AnhNDd: thông báo chế độ tìm kiếm đã bắt đầu.
         */
        void startSearchMode();

        /**
         * TienNAb: thông báo thoát chế độ tìm kiếm
         */
        void closeSearchMode();

        /**
         * AnhNDd: Trường hợp lựa chọn danh bạ hiển thị trên option menu.
         */
        void contactsFilter();

        /**
         * AnhNDd: thuc hien them moi contact
         */
        void addNewContact();
    }

    private Listener mListener;

    private final FrameLayout mToolBarFrame;

    private Context mContext;

    //AnhNDd: view mặc định có search và more action.
    private View mToolDefault;

    private ImageView mImageAddContact;

    private View mSearchContainer;

    private EditTextKeyListener mSearchView;

    private Boolean mSearchMode = false;

    private View mClearSearchView;
    //Bkav QuangNDb them dau + contact o seach container
    private View mAddContactSearchView;

    private ImageView mImageViewShowSetting;

    private ImageView mImageViewFavorites;

    //AnhNDd: popup menu cho fragment danh ba
    private PopupMenu mPopupMenu;

    //AnhNDd: xau de tim kiem
    private String mQueryString;

    private boolean mIsFavoritesMode = false;

    //AnhNDd: chiều cao tối đa của frame tool bar
    private int mMaxPortraitToolBarHeight;

    // Anhdts
    private View mRootView;

    private int mTextSizeDensitySmall = 38;

    private String mTextSearch;

    public BtalkFrameToolAdapter(Context context, FrameLayout frameLayout, View view, Listener listener) {
        mToolBarFrame = frameLayout;
        mContext = context;
        mListener = listener;
        mRootView = view;
        setupSearchAndSelectionViews();
    }

    private void setupSearchAndSelectionViews() {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        //AnhNDd: tool gốc.
        mToolDefault = inflater.inflate(R.layout.btalk_contacts_tool, mToolBarFrame,
                /* attachToRoot = */ false);
        mToolBarFrame.addView(mToolDefault);
        mMaxPortraitToolBarHeight = mToolDefault.getHeight();
        // Bkav TrungTh doi lai search thanh icon add contact
        mImageAddContact = (ImageView) mToolDefault.findViewById(R.id.action_add_contact);
        mImageAddContact.setOnClickListener(this);

        mImageViewShowSetting = (ImageView) mToolDefault.findViewById(R.id.action_more_tool);
        mImageViewShowSetting.setOnClickListener(this);

        // Setup search bar
        mSearchContainer = inflater.inflate(R.layout.btalk_search_bar_expanded, mToolBarFrame,
                false);

        mSearchContainer.setVisibility(View.VISIBLE);
        mSearchView = (EditTextKeyListener) mSearchContainer.findViewById(R.id.search_view);
        mSearchView.setHint(mContext.getString(R.string.hint_findContacts));
        mSearchView.addTextChangedListener(new SearchTextWatcher());
        // Anhdts back thi thoat che do search
        mSearchView.setOnBackPressListener(new EditTextKeyListener.OnBackPressListener() {
            @Override
            public boolean onBackState() {
                mListener.hideSearchView();
                if (isSearchMode() && TextUtils.isEmpty(mSearchView.getText())) {
                    searchBackButtonPressed();
                    return true;
                }
                return false;
            }
        });
        mSearchContainer.findViewById(R.id.search_back_button).setOnClickListener(this);

        mClearSearchView = mSearchContainer.findViewById(R.id.search_close_button);

        // Bkav HuyNQN fix loi khi de density small thi nut close khong thu nho lai
        int padding = (int) mClearSearchView.getContext().getResources().getDimension(R.dimen.btalk_list_item_photo_padding_top);
        if(mSearchView != null){
            if(mSearchView.getTextSize() == mTextSizeDensitySmall){
                // Bkav HienDTk: khong setPadding khi chu nho
//                mClearSearchView.setPadding(padding,padding,padding,padding);
            }
        }
        mClearSearchView.setVisibility(View.GONE);
        mClearSearchView.setOnClickListener(this);

        //Bkav QuangNDb
        mAddContactSearchView = mSearchContainer.findViewById(R.id.search_add_contact);
        mAddContactSearchView.setOnClickListener(this);

        //AnhNDd: Lằng nghe sự kiện bầm vào nút yêu thích trên tool bar.
        mImageViewFavorites = (ImageView) mToolDefault.findViewById(R.id.action_filter_favorites);
        mImageViewFavorites.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            /*case R.id.action_search: //AnhNDd: click vao icon search
                actionSearch();
                break;*/
            case R.id.action_add_contact:
                //TrungTH: Them add contact
                if (mListener != null)
                    mListener.addNewContact();
                break;

            case R.id.action_more_tool:
                //AnhNDd: Hiển thị danh sách các setting.
                initPopupMenu(v);
                break;
            case R.id.search_back_button:
                searchBackButtonPressed();
                break;
            case R.id.search_close_button:
                setQueryString(null);
                break;
            case R.id.action_filter_favorites:
                // Anhdts neu khong co contact nao thi return, khong xu ly gi
                if (mRootView.findViewById(R.id.contacts_unavailable_view).isShown()) {
                    return;
                }
                mIsFavoritesMode = !mIsFavoritesMode;
                if (mIsFavoritesMode) {
                    BtalkContactUtils.showLog("isclick" + v.isClickable());
                    mListener.showFavorites();
                    //mImageViewFavorites.setBackgroundColor(mContext.getResources().getColor(R.color.background_favorites_icon));
                    mImageViewFavorites.setColorFilter(ContextCompat.getColor(mContext, R.color.btalk_ab_text_and_icon_selected_color));
//                    mImageViewFavorites.setColorFilter(Color.parseColor("#4DB2FF"));
                } else {
                    mListener.closeFavorites();
//                    mImageViewFavorites.setColorFilter(ContextCompat.getColor(mContext, R.color.btalk_ab_text_and_icon_normal_color));
                    mImageViewFavorites.setColorFilter(null);
                    //mImageViewFavorites.setBackgroundColor(Color.TRANSPARENT);
                }
                break;
            //Bkav QuangNDb them add contact o search view
            case R.id.search_add_contact:
                //TrungTH: Them add contact
                if (mListener != null)
                    mListener.addNewContact();
                break;
        }
    }

    public boolean isFavoriteMode(){
        return mIsFavoritesMode;
    }

    public void setFavoriteMode(boolean b){
        mIsFavoritesMode = b;
    }

    // Bkav TienNAb: update lai mau icon yeu thich khi chuyen qua lai giua cac che do
    public void updateColorImageViewFavorite(){
        if (mIsFavoritesMode){
            mImageViewFavorites.setColorFilter(ContextCompat.getColor(mContext, R.color.btalk_ab_text_and_icon_selected_color));
        } else {
            mImageViewFavorites.setColorFilter(null);
        }
    }

    public boolean isButtonFavoriteAndButtonAddContactVisible(){
        if (mImageAddContact.getVisibility() == View.VISIBLE || mImageViewFavorites.getVisibility() == View.VISIBLE){
            return true;
        }
        return false;
    }

    /**
     * AnhNDd: Xử lý việc bấm vào icon quay lại.
     */
    public void searchBackButtonPressed() {
        mListener.closeSearchMode();
        setSearchMode(false);
        if (mListener != null) {
            mListener.onAction(Listener.Action.STOP_SEARCH_AND_SELECTION_MODE);
        }
        mImageViewFavorites.setEnabled(true);
        updateViewFrameToolBar(false);
    }

    public void setQueryString(String query) {
        mQueryString = query;
        if (mSearchView != null) {
            mSearchView.setText(query);
            // When programmatically entering text into the search view, the most reasonable
            // place for the cursor is after all the text.
            mSearchView.setSelection(mSearchView.getText() == null ?
                    0 : mSearchView.getText().length());
        }
    }

    public void setFocusOnSearchView() {
        //HienDTk: set text cho search view
        if(mTextSearch != null){
            mSearchView.setText(mTextSearch);
        }
        mSearchView.requestFocus();
        mSearchView.setEnabled(true);
        showInputMethod(mSearchView); // Workaround for the "IME not popping up" issue.

        // Bkav HuyNQN fix loi mat focus searchview tren ROM9
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSearchView.requestFocus();
                mSearchView.setEnabled(true);
            }
        },200);
    }

    private void showInputMethod(final View view) {
        final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }
    /**
     * AnhNDd: lằng nghe sự kiện text thay đổi.
     */
    private class SearchTextWatcher implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence queryString, int start, int before, int count) {
            if (queryString.equals(mQueryString)) {
                return;
            }
            mQueryString = queryString.toString();
            if (!mSearchMode) {
                if (!TextUtils.isEmpty(queryString)) {
                    setSearchMode(true);
                }
            } else if (mListener != null) {
                mListener.onAction(Listener.Action.CHANGE_SEARCH_QUERY);
            }
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

    public void setSearchMode(boolean flag) {
        if (mSearchMode != flag) {
            mSearchMode = flag;
            if (mSearchView == null) {
                return;
            }
            if (mSearchMode) {
                mSearchView.setEnabled(true);
                setFocusOnSearchView();
            } else {
                // Disable search view, so that it doesn't keep the IME visible.
                mSearchView.setEnabled(false);
            }
            setQueryString(null);
        } else if (flag) {
            // Everything is already set up. Still make sure the keyboard is up
            if (mSearchView != null) setFocusOnSearchView();
        }
    }

    public String getQueryString() {
        return mSearchMode ? mQueryString : null;
    }

    public void initPopupMenu(View v) {
        mPopupMenu = new PopupMenu(mContext, v, Gravity.END);
        mPopupMenu.getMenuInflater().inflate(R.menu.btalk_contacts_menu, mPopupMenu.getMenu());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Call some material design APIs here
            mPopupMenu.getMenu().findItem(R.id.menu_blocked_numbers).setVisible(false);
        }
        // TrungTH them doan check rieng cho myanmar
        // Bkav HuyNQN dong lai tinh nang doi dau so
        /*if(Config.isMyanmar()){
            mPopupMenu.getMenu().findItem(R.id.menu_convert_phone).setVisible(false);
        }*/
        // Ban co yeu bo nhom di vi khong ho tro tai khoan google
        if (CompatUtils.isBCY()) {
            mPopupMenu.getMenu().findItem(R.id.menu_show_groups).setVisible(false);
        }
        mPopupMenu.setOnMenuItemClickListener(mMenuItemActionMoreClickListener);
        mPopupMenu.show();
    }

    public boolean isSearchMode() {
        return mSearchMode;
    }

    //AnhNDd: listen cho việc item được click trong popup menu action more.
    private PopupMenu.OnMenuItemClickListener mMenuItemActionMoreClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int i = item.getItemId();
            if (i == R.id.menu_add_contact) {
                mListener.addNewContact();
            } else if (i == R.id.menu_settings) {
                Intent intent = new Intent(mContext, BtalkContactsPreferenceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                return true;
            } else if (i == R.id.menu_contacts_filter) {
                //TODO: AnhNDd: do chưa có danh bạ để lựa chọn, nên chưa thực hiện.
                /*AccountFilterUtil.startAccountFilterActivityForResult((Activity) mContext, SUBACTIVITY_ACCOUNT_FILTER,
                        mContactListFilterController.getFilter());*/
                mListener.contactsFilter();
                return true;
            } else if (i == R.id.menu_import) {
                mListener.showImportDialogFragment();
            } else if (i == R.id.menu_export) {
                mListener.showExportDialogFragment();
            } else if (i == R.id.menu_accounts) {
                final Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[]{
                        ContactsContract.AUTHORITY
                });
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_NEW_TASK);
                ImplicitIntentsUtil.startActivityInAppIfPossible(mContext, intent);
                return true;
            } else if (i == R.id.menu_blocked_numbers) {
                // Bkav HuyNQN BOS-3712 thuc hien goi den giao dien chan so cua BMS
                Intent intent = BmsUtils.intentBlackListBms(mContext);
                if (intent != null) {
                    // Bkav TienNAb: fix loi chon chuc nang so bi chan trong danh ba bi crash
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } else {
                    intent = FilteredNumberCompat.createManageBlockedNumbersIntent(mContext);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
                return true;

            } else if (i == R.id.menu_merge_contacts) {
                //Bkav QuangNDb doi ten action de qua cts, gg khong cho dung action bat dau bang android.intent
                Intent intent = new Intent("bkav.intent.action.MERGE_DUPLICATED");
                // Anhdts su dung giao dien btalk
                intent.setPackage(mContext.getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                return true;
            } else if (i == R.id.menu_show_groups) {
                //AnhNDd: hiển thị activity groups
                final Intent intent = new Intent(mContext, BtalkContactsGroupsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
            // Anhdts convert phone
            // Bkav HuyNQN dong lai tinh nang doi dau so
            /*else if (i == R.id.menu_convert_phone) {
                final Intent intent = new Intent(mContext, BtalkContactSelectionActivity.class);
                intent.setAction(UiIntentActions.CONVERT_PHONE_ACTION);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }*/
            return false;
        }
    };

    @Override
    public boolean onClose() {
        setSearchMode(false);
        return false;
    }

    //AnhNDd: update giao diện của tool bar khi thay đổi chế độ
    public void updateViewFrameToolBar(boolean skipAnimation) {
        if (skipAnimation) {
            //AnhNDd: chua xu ly
        } else {
            if (mSearchMode) {
                addSearchContainer();
                mSearchContainer.setAlpha(0);
                mSearchContainer.animate().alpha(1);
                animateTabHeightChange(mMaxPortraitToolBarHeight, 0);
            } else {
                mToolBarFrame.removeView(mSearchContainer);
                mToolBarFrame.addView(mToolDefault);
                mToolDefault.setAlpha(0);
                mToolDefault.animate().alpha(1);
                animateTabHeightChange(mMaxPortraitToolBarHeight, 0);
            }
        }
    }

    private void addSearchContainer() {
        mToolBarFrame.removeView(mToolDefault);
        mToolBarFrame.removeView(mSearchContainer);
        mToolBarFrame.addView(mSearchContainer);
        //mSearchContainer.setAlpha(1);
    }

    private void animateTabHeightChange(int start, int end) {
        final ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                //setPortraitTabHeight(value);
            }
        });
        animator.setDuration(100).start();
    }

    /**
     * AnhNDd: thuc hien hien thi view search va bat dau search
     */
    // Bkav HienDTk: them bien da load xong view hay chua
    public void actionSearch() {
        if (!mSearchMode) {
            mListener.startSearchMode();
            boolean justFocus = mSearchMode;
            mSearchMode = true;
            updateViewFrameToolBar(false);
            if (justFocus) {
                if (mSearchView == null) {
                    return;
                }
                setQueryString(null);
            }
            if (mSearchView != null)
                setFocusOnSearchView();
            //AnhNDd: xử lý việc bấm nút search thì đổi giao diện.
            if (mIsFavoritesMode) {
                mIsFavoritesMode = false;
                mListener.closeFavorites();
                mImageViewFavorites.setColorFilter(null);
            }
            mImageViewFavorites.setEnabled(false);
            // Bkav HienDTk: dong lai
//        } else {
//            // Bkav HienDTk: bat truong hop chua load xong view thi khong bat ban phim
//            if(isLoadComplete){
//                final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
//                        Context.INPUT_METHOD_SERVICE);
//                if (imm != null) {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
//                        }
//                    }, 100);
//                }
//            }

        }
    }

    /**
     * Anhdts
     */
    void setCheckFavorite(boolean modeFavorite) {
        mIsFavoritesMode = !modeFavorite;
        onClick(mImageViewFavorites);
    }

    void selectionText() {
        if (!TextUtils.isEmpty(mSearchView.getText())) {
            mSearchView.setSelection(0, mSearchView.getText().length());
        }
    }

    /**
     * Anhdts goi xong thi xoa text di
     */
    void clearText() {
        mSearchView.setText("");
        mSearchView.setCursorVisible(true);
        mSearchView.requestFocus();
    }

    // Bkav TienNAb: ham hien thi nut them lien he moi khi appbar layout thu gon
    public void showHideButtonAddContact(boolean b){
        mImageAddContact.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    // Bkav TienNAb: ham hien thi nut yeu thich khi appbar layout thu gon
    public void showHideButtonFavorite(boolean b){
        mImageViewFavorites.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setTextSearch(String textSearch){
        mTextSearch = textSearch;
    }
}


