package bkav.android.btalk.contacts;

import android.content.Context;
import android.util.AttributeSet;

import com.android.contacts.common.list.ContactTileView;

/**
 * Created by anhdt on 17/08/2017.
 */

public class BtalkContactTileView extends ContactTileView {
    public BtalkContactTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getApproximateImageSize() {
        return 0;
    }

    @Override
    protected boolean isDarkTheme() {
        return false;
    }
}
