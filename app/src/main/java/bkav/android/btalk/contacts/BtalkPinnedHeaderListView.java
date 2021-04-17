package bkav.android.btalk.contacts;

import android.content.Context;
import android.util.AttributeSet;

import com.android.contacts.common.list.PinnedHeaderListView;

import bkav.android.btalk.R;


/**
 * AnhNDd: listview với pinnerheader để mình custom
 */
public class BtalkPinnedHeaderListView extends PinnedHeaderListView {

    private int mHeightViewFadeEdge;

    public BtalkPinnedHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHeightViewFadeEdge = (int) getResources().getDimension(R.dimen.fadeEdge_length);
    }

    @Override
    public void hookToChangeSetFade(int bottom, int top, int headerHeight, PinnedHeaderListView.PinnedHeader header) {
        //AnhNDd: không làm mờ gì cả.
    }
}
