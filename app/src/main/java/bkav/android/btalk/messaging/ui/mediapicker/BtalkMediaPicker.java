package bkav.android.btalk.messaging.ui.mediapicker;

import com.android.messaging.Factory;
import com.android.messaging.ui.mediapicker.MediaChooser;
import com.android.messaging.ui.mediapicker.MediaPicker;

import bkav.android.btalk.messaging.ui.mediapicker.audio.BtalkAudioMediaChooser;
import bkav.android.btalk.messaging.ui.mediapicker.camera.BtalkCameraMediaChooser;
import bkav.android.btalk.messaging.ui.mediapicker.contact.ContactChooser;
import bkav.android.btalk.messaging.ui.mediapicker.emoticon.EmoticonChooser;
import bkav.android.btalk.messaging.ui.mediapicker.gallery.BtalkGalleryMediaChooser;

/**
 * Created by quangnd on 14/04/2017.
 * class custom lai media picker cua source doc
 */

public class BtalkMediaPicker extends MediaPicker {

    // Bkav QuangNDb type cua emoticon
    public static final int MEDIA_TYPE_EMOTICON = 0x0012;
    private static final int EMOTICON_POSITION = 2;

    public BtalkMediaPicker() {
        super(Factory.get().getApplicationContext());
    }

    @Override
    protected boolean getChooserShowsActionBarInFullScreen() {
        return mSelectedChooser != null
                && mSelectedChooser.getActionBarTitleResId() != 0
                && mSelectedChooser.getSupportedMediaTypes() != MEDIA_TYPE_VCARD
                && mSelectedChooser.getSupportedMediaTypes() != MEDIA_TYPE_LOCATION;
    }

    @Override
    protected void initChooser() {
        mChoosers = new MediaChooser[]{
                new ContactChooser(this),
                new BtalkAudioMediaChooser(this),
                new EmoticonChooser(this),
                new BtalkCameraMediaChooser(this),
                new BtalkGalleryMediaChooser(this),
        };
    }

//    new LocationChooser(this),

    @Override
    protected void setViewPagerPosition() {
        if (mCurrentPosition != -1) {
            mViewPager.setCurrentItem(EMOTICON_POSITION);
        }
    }
}
