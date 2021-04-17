package bkav.android.btalk.messaging.ui.mediapicker.gallery;

import android.content.Context;
import android.util.AttributeSet;

import com.android.messaging.ui.mediapicker.GalleryGridView;

import bkav.android.btalk.R;

/**
 * Created by quangnd on 06/05/2017.
 */

public class BtalkGalleryGridView extends GalleryGridView {

    public BtalkGalleryGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getIdMenuRes() {
        return R.menu.btalk_gallery_picker_menu;
    }

}
