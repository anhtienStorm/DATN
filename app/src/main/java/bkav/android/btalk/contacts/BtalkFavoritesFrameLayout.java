package bkav.android.btalk.contacts;


import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactTileLoaderFactory;
import com.android.contacts.common.list.ContactTileAdapter;
import com.android.contacts.common.list.ContactTileView;
import com.android.contacts.common.util.ContactListViewUtils;

import bkav.android.btalk.R;

/**
 * AnhNDd: Frame layout chứa danh sách contact yêu thích.
 */
public class BtalkFavoritesFrameLayout extends FrameLayout {

    /**
     * AnhNDd: Lắng nghe sự kiện click vào contact trong tab yêu thích.
     */
    public interface Listener {
        void onContactSelected(Uri contactUri, Rect targetRect);

        void onCallNumberDirectly(String phoneNumber);
    }

    private final int LOADER_ID = 5;

    private Context mContext;

    private FrameLayout mContentMain;

    private ContactTileAdapter mAdapter;

    private Listener mListener;

    private ContactTileAdapter.DisplayType mDisplayType;

    private TextView mEmptyView;

    private ListView mListView;

    private RecyclerView mRecyclerView;     // Bkav TienNAb: recyclerview chua danh sach lien he yeu thich

    private BtalkFavoriteContactListAdapter mFavoriteContactAdapter;

    private LoaderManager mLoaderManager;

    public BtalkFavoritesFrameLayout(@NonNull Context context, FrameLayout contentMain) {
        super(context);
        mContext = context;
        mContentMain = contentMain;
        mContentMain.setVisibility(GONE);

        Resources res = getResources();
        int columnCount = res.getInteger(R.integer.contact_tile_column_count_in_favorites);

        mAdapter = new BtalkContactTileAdapter(mContext, mAdapterListener,
                columnCount, mDisplayType);
        mAdapter.setPhotoLoader(ContactPhotoManager.getInstance(mContext));

        mFavoriteContactAdapter = new BtalkFavoriteContactListAdapter(getContext());

        setUpViews();
    }

    public void setUpViews() {
        mEmptyView = (TextView) mContentMain.findViewById(R.id.contact_tile_list_empty);
        mListView = (ListView) mContentMain.findViewById(R.id.contact_tile_list);
        mRecyclerView = mContentMain.findViewById(R.id.favorite_contact_list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),getResources().getInteger(R.integer.contact_tile_column_count_in_favorites)));
        mRecyclerView.setAdapter(mFavoriteContactAdapter);

        mListView.setItemsCanFocus(true);
        mListView.setAdapter(mAdapter);
        ContactListViewUtils.applyCardPaddingToView(getResources(), mListView, mContentMain);
    }

    public void start(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        // initialize the loader for this display type and destroy all others
        //AnhNDd: vì hiện tại đang có loader gốc để load tất cả contact, nên khi tạo loader mới cần
        //thay đổi ID để loader không bị trùng nhau.
        final ContactTileAdapter.DisplayType[] loaderTypes = mDisplayType.values();
        for (int i = 0; i < loaderTypes.length; i++) {
            if (loaderTypes[i] == mDisplayType) {
                mLoaderManager.initLoader(mDisplayType.ordinal() + LOADER_ID, null,
                        mContactTileLoaderListener);
            } else {
                mLoaderManager.destroyLoader(loaderTypes[i].ordinal() + LOADER_ID);
            }
        }
        mContentMain.setAlpha(0);
        mContentMain.animate().alpha(1);
        mContentMain.setVisibility(VISIBLE);
    }

    public void close() {
        mContentMain.setVisibility(GONE);

        //AnhNDd: huy cac loader.
        final ContactTileAdapter.DisplayType[] loaderTypes = mDisplayType.values();
        for (int i = 0; i < loaderTypes.length; i++) {
            if (mLoaderManager == null) {
                return;
            }
            mLoaderManager.destroyLoader(loaderTypes[i].ordinal() + LOADER_ID);
        }
    }

    private final LoaderManager.LoaderCallbacks<Cursor> mContactTileLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {

                @Override
                public CursorLoader onCreateLoader(int id, Bundle args) {
                    switch (mDisplayType) {
                        case STARRED_ONLY:
                            return ContactTileLoaderFactory.createStarredLoader(mContext);
                        case STREQUENT:
                            return ContactTileLoaderFactory.createStrequentLoader(mContext);
                        case FREQUENT_ONLY:
                            return ContactTileLoaderFactory.createFrequentLoader(mContext);
                        default:
                            throw new IllegalStateException(
                                    "Unrecognized DisplayType " + mDisplayType);
                    }
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    if (data != null && data.getCount() == 0){
                        mEmptyView.setVisibility(VISIBLE);
                    } else {
                        mEmptyView.setVisibility(GONE);
                    }
                    if (data == null || data.isClosed()) {
                        //Log.e(TAG, "Failed to load contacts");
                        return;
                    }
                    mAdapter.setContactCursor(data);
                    mFavoriteContactAdapter.setCursor(data);
                    mEmptyView.setText(getEmptyStateText());
//                    mListView.setEmptyView(mEmptyView);
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                }
            };

    public void setDisplayType(ContactTileAdapter.DisplayType displayType) {
        mDisplayType = displayType;
        mAdapter.setDisplayType(mDisplayType);
    }

    private String getEmptyStateText() {
        String emptyText;
        switch (mDisplayType) {
            case STREQUENT:
            case STARRED_ONLY:
                emptyText = mContext.getString(R.string.listTotalAllContactsZeroStarred);
                break;
            case FREQUENT_ONLY:
            case GROUP_MEMBERS:
                emptyText = mContext.getString(R.string.noContacts);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized DisplayType " + mDisplayType);
        }
        return emptyText;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private ContactTileView.Listener mAdapterListener =
            new ContactTileView.Listener() {
                @Override
                public void onContactSelected(Uri contactUri, Rect targetRect) {
                    if (mListener != null) {
                        mListener.onContactSelected(contactUri, targetRect);
                    }
                }

                @Override
                public void onCallNumberDirectly(String phoneNumber) {
                    if (mListener != null) {
                        mListener.onCallNumberDirectly(phoneNumber);
                    }
                }

                @Override
                public int getApproximateTileWidth() {
                    return mContentMain.getWidth() / mAdapter.getColumnCount();
                }

                @Override
                public void onActionShowDetail() {

                }
            };
}