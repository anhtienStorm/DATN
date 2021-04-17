package bkav.android.btalk.messaging.ui.searchSms;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.data.ConversationData;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.ImeUtil;

import bkav.android.btalk.R;
import bkav.android.btalk.messaging.util.BtalkCharacterUtil;

/**
 * Created by quangnd on 10/06/2017.
 * class search toan bo tin nhan trong he thong
 */

public class SearchFragment extends Fragment implements RecyclerCursorAdapter.ItemClickListener<ConversationMessageData>,
        LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private SearchView mQuery;
    private String mSearchString;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mListMessageView;
    private ImageButton mBack;
    private SearchAdapter mAdapter;
    private LoaderManager mLoaderManager;

    private static final int ID_LOADER = -1;
    private View mContentView;

    private static final String TAG = "SearchFragment";

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoaderManager = getLoaderManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.btalk_fragment_search_sms, container, false);
        mQuery = (SearchView) mContentView.findViewById(R.id.search_view);
        mListMessageView = (RecyclerView) mContentView.findViewById(R.id.list_message);
        mBack = (ImageButton) mContentView.findViewById(R.id.back);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new SearchAdapter(getActivity());
        mAdapter.setItemClickListener(this);
        mListMessageView.setLayoutManager(mLayoutManager);
        mListMessageView.setAdapter(mAdapter);
        mQuery.setOnQueryTextListener(this);
        clickBackButton();
        hideSearchHintIcon();
        return mContentView;
    }

    private void hideSearchHintIcon() {
        ImageView magImage = (ImageView) mQuery.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        magImage.setVisibility(View.GONE);
        magImage.setImageDrawable(null);
    }

    private void clickBackButton() {
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImeUtil.get().hideImeKeyboard(getActivity(), mQuery);
                getActivity().finish();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MessagingContentProvider.buildMessagesSearchUri(BtalkCharacterUtil.get().convertToNotLatinCode(mSearchString));
        return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
        mAdapter.setQuery(mSearchString);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
        if (mSearchString == null && newFilter == null) {
            return true;
        }
        if (mSearchString != null && mSearchString.equals(newFilter)) {
            return true;
        }
        mSearchString = newFilter;
        initLoader();
        return true;

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
    public boolean onClose() {
        if (!TextUtils.isEmpty(mQuery.getQuery())) {
            mQuery.setQuery(null, true);
        }
        return true;

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
        UIIntents.get().launchConversationActivitySearch(getActivity(), data.getConversationId(), null, null, false, positionMessage);// message id de co the scroll den vi tri cua tin nhan do khi mo conversation =
        getActivity().finish();
    }
}
