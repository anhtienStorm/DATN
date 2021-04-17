package bkav.android.btalk.messaging.ui.mediapicker.contact;

import android.app.FragmentManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.datamodel.data.MessagePartData;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 20/04/2017.
 * class giao dien cua contactChooser trong attach
 */

public class ContactChooserView extends RelativeLayout implements SearchView.OnQueryTextListener, BtalkContactChooserFragment.BtalkContactChooserFragmentHost {

    private BtalkContactChooserFragment mBtalkContactChooserFragment;

    private FragmentManager mFragmentManager;

    private static final String FRAGMENT_TAG = "CONTACT_PICKER_TAG";

    private SearchView mSearchView;

    private Context mContext;

    private boolean mIsClickSearch = false;

    public interface ContactChooserViewHost extends DraftMessageData.DraftMessageSubscriptionDataProvider {

        void onContactItemClick(MessagePartData messagePartData);
    }

    private ContactChooserViewHost mHost;

    public void isClickSearch(boolean isClickSearch) {
        this.mIsClickSearch = isClickSearch;
    }

    public void setHost(ContactChooserViewHost host) {
        this.mHost = host;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        if (mFragmentManager != null) {
            mBtalkContactChooserFragment = new BtalkContactChooserFragment();
            mBtalkContactChooserFragment.setHost(this);
            mFragmentManager.beginTransaction().replace(R.id.contact_picker_container, mBtalkContactChooserFragment, FRAGMENT_TAG).commit();
        }
    }

    public ContactChooserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void onCreateOptionMenu(final MenuInflater inflater, final Menu menu) {
        inflater.inflate(R.menu.btalk_contact_chooser_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) menuItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        EditText searchEditText = (EditText)mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.btalk_ab_text_and_icon_normal_color));
        searchEditText.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        searchEditText.setHint(R.string.contact_chooser_search_title);
        ImageView searchButton = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        searchButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_btalk_search));
        ImageView closeButton = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        closeButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_btalk_remove_small));
        View underLine = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        underLine.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.transparent));
        // Bkav TienNAb: luon cho focus search view khi click vao icon tim kiem
//        if (mIsClickSearch) {
//            mIsClickSearch = false;
            mSearchView.setIconified(false);
            mSearchView.requestFocus();
//        }
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mBtalkContactChooserFragment.setQueryString(newText, true);
        return false;
    }

    @Override
    public void onItemClick(String textContact) {
        if (mHost != null) {
            if (mSearchView != null) {
                mSearchView.setQuery("", false);
            }
            final MessagePartData textItem = new MessagePartData(textContact);
            mHost.onContactItemClick(textItem);
        }
    }
}
