package bkav.android.btalk.messaging.ui.mediapicker.location;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.ui.mediapicker.MediaChooser;
import com.android.messaging.ui.mediapicker.MediaPicker;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 14/04/2017.
 * class hien thi giao dien location trong attch
 */

public class LocationChooser extends MediaChooser implements LocationChooserView.LocationChooserViewHost{

    private LocationChooserView mLocationChooserView;
    private boolean mIsFirst = true;
    private boolean mCanSwipDown = false;
    private MediaPicker mMediaPicker;

    /**
     * Initializes a new instance of the Chooser class
     *
     * @param mediaPicker The media picker that the chooser is hosted in
     */
    public LocationChooser(MediaPicker mediaPicker) {
        super(mediaPicker);
        mMediaPicker = mediaPicker;
    }

    @Override
    public void onFullScreenChanged(boolean fullScreen) {
        super.onFullScreenChanged(fullScreen);
        mCanSwipDown = fullScreen;
    }

    @Override
    public boolean canSwipeDown() {
        return mCanSwipDown;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = getLayoutInflater();
        if (mLocationChooserView == null) {
            mLocationChooserView = (LocationChooserView) inflater.inflate(R.layout.btalk_mediapicker_location_chooser, container, false);
        }
        return mLocationChooserView;
    }

    @Override
    public int getSupportedMediaTypes() {
        return MediaPicker.MEDIA_TYPE_LOCATION;
    }

    // Anhdts doi icon ic_btalk_emoticon_chooser =>>
    @Override
    protected int getIconResource() {
        return R.drawable.ic_btalk_location_chooser;
    }

    @Override
    protected int getIconDescriptionResource() {
        return R.string.media_chooser_location;
    }

    @Override
    public void onCreateOptionsMenu(MenuInflater inflater, Menu menu) {
        if (mView != null) {
            mLocationChooserView.onCreateOptionMenu(inflater, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (mView != null) && mLocationChooserView.onOptionsItemSelected(item);
    }

    @Override
    public int getActionBarTitleResId() {
        if (mIsFirst && mLocationChooserView != null) {
            mLocationChooserView.setFragmentManager(getFragmentManager());
            mLocationChooserView.setHost(this);
            mIsFirst = false;
        }
        return R.string.media_chooser_location;
    }
    @Override
    protected int getIdResBackIcon() {
        return R.drawable.ic_btalk_remove_small;
    }
    @Override
    protected void setAlphaTab(boolean selected) {
        //Bkav QuangNDB khong lam gi
    }

    @Override
    public void onSendLocationClick(MessagePartData messagePartData) {
        mMediaPicker.dispatchItemsSelected(messagePartData, true);
    }

    @Override
    public boolean canShowIme() {
        return false;
    }
}
