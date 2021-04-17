package bkav.android.btalk.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.activities.MultiPickContactsActivity;

import bkav.android.btalk.R;

/**
 * AnhNDd: activity để lựa chọn nhiều contact
 */
public class BtalkMultiPickContactActivity extends MultiPickContactsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setElevation(0);
    }

    //AnhNDd: fragment khi lua chon contact de export
    //private BtalkExportMultiSelectContactsListFragment mBtalkExportMultiSelectContactsListFragment;

    @Override
    public int getNormalStatusBarColor() {
        return getColorBkav(R.color.btalk_actionbar_setting);
    }

    @Override
    public void inflateSelectionContainer(LayoutInflater inflater) {
        mSelectionContainer = inflater.inflate(R.layout.btalk_contacts_action_mode, null);
        // Bkav TienNAb: sua lai mau text de tranh trung voi mau nen
        ((ImageView) mSelectionContainer.findViewById(R.id.expand_more_view)).setColorFilter(
                getResources().getColor(R.color.btalk_ab_text_and_icon_normal_color));
        ((TextView) mSelectionContainer.findViewById(R.id.selection_menu)).setTextColor(
                getResources().getColor(R.color.btalk_ab_text_and_icon_normal_color));
    }

    @Override
    public void inflateOptionMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.btalk_contacts_search_menu, menu);
    }

    /*@Override
    public Fragment newContactsFragment() {
        mBtalkExportMultiSelectContactsListFragment = new BtalkExportMultiSelectContactsListFragment();
        mBtalkExportMultiSelectContactsListFragment.setCheckBoxListListener(new CheckListener());
        return mBtalkExportMultiSelectContactsListFragment;
    }*/

    /*@Override
    public void instantiateItemContactsFragment(Fragment f) {
        if (mBtalkExportMultiSelectContactsListFragment == null && f instanceof BtalkExportMultiSelectContactsListFragment) {
            mBtalkExportMultiSelectContactsListFragment = (BtalkExportMultiSelectContactsListFragment) f;
        }
    }*/


    /*@Override
    public void onFocusChange(View view, boolean hasFocus) {
        int i = view.getId();
        if (i == R.id.search_view) {
            if (hasFocus) {
                final InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSearchView.findFocus(), 0);
            }
        }
    }*/

    //AnhNDd: cập nhật số item được chọn
    /*@Override
    protected void updateActionBar() {
        int countAllContacts = mBtalkExportMultiSelectContactsListFragment.getAdapter().getAllVisibleContactIds().size();
        mSelectedNums = mBtalkExportMultiSelectContactsListFragment.getSelectedContactIds().size();
        String countTitle = mContext.getResources().getString(R.string.contacts_selected,
                mSelectedNums);
        mSelectionButton.setResponse(countTitle);
        mSelectionMenu.getPopupList().clearItems();
        mSelectionMenu.getPopupList().addItem(mSelectionMenu.SELECTED,
                String.valueOf(mSelectedNums));

        boolean hasSelectAll = mSelectedNums == countAllContacts;
        mSelectionMenu.getPopupList().addItem(mSelectionMenu.SELECT_OR_DESELECT,
                mContext.getString(hasSelectAll ? R.string.menu_select_none
                        : R.string.menu_select_all));
        invalidateOptionsMenu();

        if (mSelectedNums > 0) {
            mOKButton.setEnabled(true);
            mOKButton.setTextColor(getResources().getColor(R.color.btalk_ab_text_and_icon_selected_color));
        } else {
            mOKButton.setEnabled(false);
            mOKButton.setTextColor(getResources().getColor(R.color.btalk_ab_text_and_icon_normal_color));
        }
    }*/

    /*@Override
    protected void initResource() {
        super.initResource();
        mOKButton.setTextColor(getResources().getColor(R.color.btalk_ab_text_and_icon_normal_color));
    }

    *//**
     * AnhNDd: Class xử lý khi có 1 item contact được chọn. Trong lúc lựa chọn nhiều contact.
     *//*
    private final class CheckListener implements MultiSelectContactsListFragment.OnCheckBoxListActionListener {

        @Override
        public void onStartDisplayingCheckBoxes() {
        }

        @Override
        public void onSelectedContactIdsChanged() {
            updateActionBar();
        }

        @Override
        public void onStopDisplayingCheckBoxes() {
        }
    }*/


    /*@Override
    protected void setAllSelected() {
        int countAllContacts = mBtalkExportMultiSelectContactsListFragment.getAdapter().getAllVisibleContactIds().size();
        mSelectedNums = mBtalkExportMultiSelectContactsListFragment.getSelectedContactIds().size();
        boolean hasSelectAll = mSelectedNums == countAllContacts;
        mBtalkExportMultiSelectContactsListFragment.setSelectAll(!hasSelectAll);
        updateActionBar();
    }*/

    @Override
    public void inflateSearchViewContainer(LayoutInflater inflater) {
        mSearchViewContainer = (ViewGroup) inflater.inflate(R.layout.btalk_custom_pick_action_bar, null);
        mSearchViewContainer.setBackgroundColor(mContext.getResources().getColor(
                R.color.btalk_actionbar_and_tabbar_bg_no_transparent_color));

        mSearchView = (EditText) mSearchViewContainer.findViewById(R.id.search_view);
    }

    @Override
    protected void inflateSearchView() {
        super.inflateSearchView();
        mSearchView.setHint(getString(R.string.hint_findContacts));
    }

    //AnhNDd: xử lý khi thay đổi text
    /*@Override
    protected void updateState(String query) {
        mBtalkExportMultiSelectContactsListFragment.setQueryString(query, true);
        mBtalkExportMultiSelectContactsListFragment.configureVerticalScrollbar();
    }*/

    //AnhNDd: xử lý khi kết thúc query
    /*@Override
    public void startQueryContact() {
        mBtalkExportMultiSelectContactsListFragment.setQueryString("", true);
        mBtalkExportMultiSelectContactsListFragment.configureVerticalScrollbar();
    }*/

    /*@Override
    public Intent createIntenExportContact() {
        mChoiceSet.clear();
        int size = mBtalkExportMultiSelectContactsListFragment.getSelectedContactIds().size();
        *//*for (int i = 0; i < size; i++) {
            String[] value = null;
            cache.id = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
            cache.lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY_COLUMN_INDEX);
            cache.name = cursor.getString(SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX);
            cache.nameRawContactId = cursor.getLong(SUMMARY_CONTACT_COLUMN_RAW_CONTACT_ID);
            mChoiceSet.putStringArray(i + "", value);
        }*//*
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putBundle(SimContactsConstants.RESULT_KEY, mChoiceSet);
        intent.putExtras(bundle);
        return intent;
    }*/
}
