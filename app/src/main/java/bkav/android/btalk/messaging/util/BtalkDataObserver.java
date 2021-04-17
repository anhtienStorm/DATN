package bkav.android.btalk.messaging.util;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Created by quangnd on 27/05/2017.
 *
 */

public class BtalkDataObserver extends ContentObserver {

    private OnChangeListener mOnChangeListener;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public BtalkDataObserver(Handler handler) {
        super(handler);
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.mOnChangeListener = onChangeListener;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if (mOnChangeListener != null) {
            mOnChangeListener.onChange(uri);
        }
    }

    public interface OnChangeListener {

        void onChange(Uri uri);
    }
}
