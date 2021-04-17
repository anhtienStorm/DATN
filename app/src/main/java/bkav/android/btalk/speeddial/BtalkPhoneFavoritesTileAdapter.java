package bkav.android.btalk.speeddial;

import android.content.Context;

import com.android.contacts.common.list.ContactTileView;
import com.android.dialer.list.PhoneFavoritesTileAdapter;

import bkav.android.btalk.R;

/**
 * Created by anhdt on 20/11/2017.
 *
 */

public class BtalkPhoneFavoritesTileAdapter extends PhoneFavoritesTileAdapter {

    public BtalkPhoneFavoritesTileAdapter(Context context, ContactTileView.Listener listener, OnDataSetChangedForAnimationListener dataSetChangedListener) {
        super(context, listener, dataSetChangedListener);
    }

    /**
     * Anhdts doi layout
     */
    @Override
    public int getLayoutTileViewRes() {
        return R.layout.btalk_phone_favorite_tile_view;
    }

    /**
     * Anhdts chi hien thi max la 10
     */
    @Override
    public int getCount() {
        if (mContactEntries == null) {
            return 0;
        }
        return mContactEntries.size() > 10 ? 10 : mContactEntries.size();
    }
}
