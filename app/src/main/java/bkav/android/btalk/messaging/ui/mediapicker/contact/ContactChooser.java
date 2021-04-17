package bkav.android.btalk.messaging.ui.mediapicker.contact;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.ui.mediapicker.MediaChooser;
import com.android.messaging.ui.mediapicker.MediaPicker;
import com.android.messaging.util.UiUtils;

import bkav.android.btalk.R;


/**
 * Created by quangnd on 14/04/2017.
 * Bkav QuangNdb: class hien thi view attch contact
 */

public class ContactChooser extends MediaChooser implements ContactChooserView.ContactChooserViewHost, View.OnClickListener {

    private MediaPicker mMediaPicker;

    private ContactChooserView mContactChooserView;

    private ImageButton mSearchIcon;

    private boolean mIsFirst = true;

    private boolean mCanShowIme = false;

    private boolean mCanSwipDown = false;

    /**
     * Initializes a new instance of the Chooser class
     *
     * @param mediaPicker The media picker that the chooser is hosted in
     */
    public ContactChooser(MediaPicker mediaPicker) {
        super(mediaPicker);
        mMediaPicker = mediaPicker;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = getLayoutInflater();
        if (mContactChooserView == null) {
            mContactChooserView = (ContactChooserView) inflater.inflate(R.layout.mediapicker_contact_chooser, container, false);
            mSearchIcon = (ImageButton) mContactChooserView.findViewById(R.id.btn_search);
            mSearchIcon.setOnClickListener(this);
        }
        return mContactChooserView;
    }

    @Override
    public void onFullScreenChanged(boolean fullScreen) {
        super.onFullScreenChanged(fullScreen);
        mCanSwipDown = fullScreen;
        mCanShowIme = fullScreen;
        if (mSearchIcon != null) {
            UiUtils.revealOrHideViewWithAnimation(mSearchIcon, fullScreen?View.GONE:View.VISIBLE, null);
        }
    }

    @Override
    public int getSupportedMediaTypes() {
        return MediaPicker.MEDIA_TYPE_VCARD;
    }

    @Override
    protected int getIconResource() {
        //HienDTk: thay icon dinh kem danh ba cho chuan
        return R.drawable.icon_btalk_contact_chooser;
    }
    @Override
    protected int getIconDescriptionResource() {
        return R.string.media_chooser_contact;
    }

    @Override
    public int getActionBarTitleResId() {
        mCanShowIme = false;
        if (mIsFirst && mContactChooserView != null) {
            mContactChooserView.setFragmentManager(getFragmentManager());
            mContactChooserView.setHost(this);
            mIsFirst = false;
        }
        return R.string.media_chooser_contact;
    }

    @Override
    public void onCreateOptionsMenu(MenuInflater inflater, Menu menu) {
        if (mView != null) {
            mContactChooserView.onCreateOptionMenu(inflater, menu);
        }
    }

    // override method nay de khi show ime khong bi dong media picker
    @Override
    public boolean canShowIme() {
        return mCanShowIme;
    }

    @Override
    public boolean canSwipeDown() {
        return mCanSwipDown;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onContactItemClick(MessagePartData messagePartData) {
        mCanShowIme = false;
        mMediaPicker.dispatchItemsSelected(messagePartData, true);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                mMediaPicker.setFullScreen(true);
                mContactChooserView.isClickSearch(true);
                break;
        }
    }
}
